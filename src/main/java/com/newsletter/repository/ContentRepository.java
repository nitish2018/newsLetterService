package com.newsletter.repository;

import com.newsletter.model.Content;
import com.newsletter.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByStatus(Status status);

    @Query("SELECT c FROM Content c WHERE c.status = :status AND c.scheduledTime BETWEEN :start AND :end")
    List<Content> findByStatusAndScheduledTimeBetween(
            @Param("status") Status status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
