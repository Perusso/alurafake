package br.com.alura.AluraFake.course.dto;

import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.user.User;

import java.time.LocalDateTime;

public class CourseResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final User instructor;
    private final Status status;
    private final LocalDateTime createdAt;
    private final LocalDateTime publishedAt;

    public CourseResponse(Long id, String title, String description,
                          User instructor, Status status,
                          LocalDateTime createdAt, LocalDateTime publishedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.status = status;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public User getInstructor() { return instructor; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
}
