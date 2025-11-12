package com.newsletter.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "content",
        indexes = {
                @Index(name = "idx_status_time", columnList = "status, scheduled_time")
        }
)
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    private String text;
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    private boolean sent = false;

    @ManyToOne
    private Topic topic;
}

