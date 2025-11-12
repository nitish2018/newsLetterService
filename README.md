# Newsletter Distribution Service

A scalable, idempotent, and event-driven **Newsletter Scheduling and Subscription Service** built with **Spring Boot**, supporting **bulk operations**, **Kafka-based dispatch**, **multi-threaded scheduling**, and **horizontal scalability** on **AWS**.

The service allows:
- Creating Topics with specified names
- Subscribing users to topics (single or bulk)
- Scheduling content for future dispatch
- Multi-threaded and distributed message delivery
- Kafka integration for asynchronous event processing
- Cron & FixedRate schedulers for recurring dispatch checks

---

## Table of Contents

1. [Requirements](#-requirements)
2. [Architecture & Infrastructure](#-architecture--infrastructure)
3. [Design Overview](#-design-overview)
4. [Data Model & Entities](#-data-model--entities)
5. [API Reference](#-api-reference)
6. [Service Layer](#-service-layer)
7. [Thread Safety & Concurrency](#-thread-safety--concurrency)
8. [AWS Deployment](#-aws-deployment)
9. [Application Properties](#-applicationproperties)
10. [How to Run](#-how-to-run)
11. [Dispatch Flow](#-dispatch-flow)

---

## Requirements

| Component | Version / Recommendation       |
|-----------|--------------------------------|
| Java | 17+                            |
| Maven | 3.8+                           |
| Spring Boot | 3.x                            |
| Database | PostgreSQL / MySQL / In Memory |
| Message Broker | Apache Kafka / In Memory       |
| Scheduler | Spring Task Scheduler / Cron   |
| Cloud | AWS EC2                        |
| Tools | Postman, cURL                  |

---

## Architecture & Infrastructure

The service follows a **modular, event-driven architecture** designed for scalability and resilience.

### Components
- **REST APIs** → For content & subscriber management.
- **Scheduler** → For periodic content dispatch.
- **Kafka Producers/Consumers** → For asynchronous message delivery.
- **Thread Pool Executor** → For parallel processing of subscribers.
- **Database** → Persistence of topics, subscribers, and content.
- **AWS EC2** → Stateless horizontal scaling.

### Scheduling Strategies

| Type | Annotation | Use |
|------|------------|-----|
| **Cron Scheduler** | `@Scheduled(cron = "...")` | For periodic content dispatch every minute/hour |
| **Fixed Rate** | `@Scheduled(fixedRate = 30000)` | Default fallback scheduler every 30s |
| **Async Tasks** | `@Async` | Concurrent email dispatch per subscriber |

### Kafka Topics
- **newsletter-events** → Published when content is ready to be sent.
- **newsletter-status** → Optional consumer for tracking send status.

---

## Design Overview

| Concern | Approach |
|---------|----------|
| **Idempotency** | `(email, topic_id)` unique constraint in DB + deduplication in bulk requests |
| **Bulk Processing** | Uses `saveAll()` for efficient inserts; deduplicates repeated entries in input |
| **Indexing** | Composite index created on `(status, scheduled_time)` which provides range queries in `O(log n + k)` times instead of full table scan |
| **Thread Safety** | No shared mutable state; relies on database & stateless services |
| **Distributed Safety** | Enforced at DB-level and through application-level checks |
| **Error Handling** | Structured JSON responses with `"status"` keys |
| **Scalability** | Stateless REST APIs, async jobs, and Kafka-based decoupling |
| **Observability** | Logs every dispatch event and error per subscriber |

---

## Data Model & Entities

### Topic

```java
@Entity
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}
```

### Subscriber

```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"email", "topic_id"}))
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    private Topic topic;
}
```

### Content

```java
@Entity
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    private Topic topic;
}
```

### Status Enum

```java
public enum Status {
    PENDING, SENT, FAILED
}
```

## Indexing

**Composite index (status, scheduled_time) ensures that range query is O(log N) instead of full table scan**


## API Reference

### Topic APIs

**POST** `/api/topics`

**Request**
```json
{
  "name": "Tech News"
}
```

**Response**
```json
{
    "id": 1,
    "name": "Tech News"
}
```

### Subscriber APIs

#### ➤ Single Subscriber

**POST** `/api/subscribers`

**Request**
```json
{
  "email": "nitish.joshi1995@gmail.com",
  "topicId": 1
}
```

**Response**
```json
{
  "email": "nitish.joshi1995@gmail.com",
  "topicId": 1,
  "status": "SUBSCRIBED"
}
```

**Error Cases**

| Condition | Response |
|-----------|----------|
| Topic doesn't exist | `"status": "TOPIC_NOT_FOUND"` |
| Already subscribed | `"status": "ALREADY_EXISTS"` |

#### ➤ Bulk Subscriber

**POST** `/api/subscribers/bulk`

**Request**
```json
[
  { "email": "nitish.joshi1995@gmail.com", "topicId": 1 },
  { "email": "nitish.rajat1995@gmail.com", "topicId": 1 },
  { "email": "nitish.joshi1995@gmail.com", "topicId": 1 }
]
```

**Response**
```json
{
  "details": [
    {
      "status": "DUPLICATE_IN_REQUEST",
      "topicId": 1,
      "email": "nitish.joshi1995@gmail.com"
    },
    {
      "status": "SUBSCRIBED",
      "topicId": 1,
      "email": "nitish.rajat1995@gmail.com"
    },
    {
      "status": "SUBSCRIBED",
      "topicId": 1,
      "email": "nitish.joshi1995@gmail.com"
    }
  ],
  "status": "SUCCESS",
  "message": "All subscribers saved successfully.",
  "subscribedCount": 2
}
```

### Content APIs

#### ➤ Single Content

**POST** `/api/content`

**Request**
```json
{
  "text": "Breaking News: Market Update",
  "topicId": 1,
  "scheduledTime": "2025-11-12T14:15:00"
}
```

**Response**
```json
{
  "text": "Breaking News: Market Update",
  "topicId": 1,
  "status": "PENDING"
}
```

#### ➤ Bulk Content

**POST** `/api/content/bulk`

**Request**
```json
[
  {
    "text": "This is from Nitish",
    "scheduledTime": "2025-11-12T15:12:00",
    "topicId": 1
  },
  {
    "text": "This is from Nitish Joshi",
    "scheduledTime": "2025-11-12T15:16:00",
    "topicId": 1
  }
]
```

**Response**
```json
[
  {
    "text": "This is from Nitish",
    "topicId": 1,
    "status": "SAVED"
  },
  {
    "text": "This is from Nitish Joshi",
    "topicId": 1,
    "status": "SAVED"
  }
]
```

### Content Status API

#### ➤ Get All Statuses

**Request**
**GET** `/api/status`

**Response**
```json
[
  {
    "id": 1,
    "status": "PENDING",
    "text": "This is from Nitish",
    "scheduledTime": "2025-11-12T15:12:00",
    "sent": false,
    "topic": {
      "id": 1,
      "name": "Tech News"
    }
  },
  {
    "id": 2,
    "status": "PENDING",
    "text": "This is from Nitish Joshi",
    "scheduledTime": "2025-11-12T15:16:00",
    "sent": false,
    "topic": {
      "id": 1,
      "name": "Tech News"
    }
  }
]
```


#### ➤ By Id

**Request**
**GET** `/api/status/{id}`

**Response**
```json
{
  "id": 2,
  "status": "PENDING",
  "text": "This is from Nitish Joshi",
  "scheduledTime": "2025-11-12T15:16:00",
  "sent": false,
  "topic": {
    "id": 1,
    "name": "Tech News"
  }
}
```

#### ➤ All Pending

**Request**
**GET** `/api/status/pending`

**Response**
```json
[
  {
    "id": 2,
    "status": "PENDING",
    "text": "This is from Nitish Joshi",
    "scheduledTime": "2025-11-12T15:16:00",
    "sent": false,
    "topic": {
      "id": 1,
      "name": "Tech News"
    }
  }
]
```

#### ➤ All Sent

**Request**
**GET** `/api/status/sent`

**Response**
```json
[
    {
        "id": 1,
        "status": "SENT",
        "text": "This is from Nitish",
        "scheduledTime": "2025-11-12T15:12:00",
        "sent": true,
        "topic": {
            "id": 1,
            "name": "Tech News"
        }
    },
    {
        "id": 2,
        "status": "SENT",
        "text": "This is from Nitish Joshi",
        "scheduledTime": "2025-11-12T15:16:00",
        "sent": true,
        "topic": {
            "id": 1,
            "name": "Tech News"
        }
    }
]
```

---

## Service Layer

| Service | Description |
|---------|-------------|
| **SubscriberService** | Manages subscriber registration and deduplication |
| **ContentService** | Handles content persistence and scheduling |
| **ContentDispatcher** | Periodically polls for pending content and dispatches |
| **EmailDispatchService** | Sends content to subscribers (mock / SMTP / Kafka) |
| **DispatchStrategyFactory** | Chooses between Kafka / Email strategy |
| **TopicRepository, SubscriberRepository, ContentRepository** | JPA repositories |


---

## Thread Safety & Concurrency

- **Stateless beans**: All Spring services are stateless and thread-safe.
- **Database-level locking**: Guarantees no duplicate dispatch.
- **ExecutorService / @Async**: Enables concurrent email dispatch per topic.
- **Safe in multiple EC2 instances**: Database enforces unique (email, topicId) keys.
- **Transactional updates**: Guarantee consistency between send and update.


## Application Properties

```properties
# Mail configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=nitish.joshi1995@gmail.com
spring.mail.password=**** **** **** ****
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
newsletter.email.from=nitish.joshi1995@gmail.com

# H2 DB (auto creates tables)
spring.datasource.url=jdbc:h2:mem:newsletterdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create

# Dispatcher strategy
newsletter.dispatch.strategy=scheduler   # or kafka

# Scheduling strategy
newsletter.scheduler.strategy=fixedRate

# Default email sender type
newsletter.email.sender=gmail
```

---

## How to Run

### Steps

1. **Access the API**
    - Get EC2 instance public hostname
    - Base URL: `http://<EC2_HOST>:8080`
    - Example: `http://ec2-54-123-45-67.compute-1.amazonaws.com:8080/api/subscribers`
    - Test endpoints using Postman or cURL
    - Upon hitting the /content endpoint with a scheduled time, the system should dispatch emails to Gmail (default email provider) at the specified schedule

---

## Dispatch Flow

1. **Content Creation**: Content is created via API with scheduled time
2. **Scheduler Activation**: `@Scheduled` method runs periodically (every 30s or via cron)
3. **Pending Content Retrieval**: Queries database for `PENDING` status content
4. **Subscriber Lookup**: Fetches all subscribers for the content's topic
5. **Parallel Dispatch**: Uses thread pool to send emails concurrently
6. **Kafka Event**: Publishes dispatch events to `newsletter-events` topic
7. **Status Update**: Updates content status to `SENT` or `FAILED`
8. **Error Handling**: Failed dispatches are logged and marked as `FAILED`

---

**Author**: Nitish Joshi    
**Version**: 1.0.0  
**Last Updated**: November 12, 2025  
**License**: Apache License
