package br.com.alura.AluraFake.user.dto;

import br.com.alura.AluraFake.course.Status;

import java.time.LocalDateTime;

public class InstructorCourseDTO {

    private Long id;
    private String title;
    private Status status;
    private LocalDateTime publishedAt;
    private Long taskCount;

    public InstructorCourseDTO(Long id, String title, Status status,
                               LocalDateTime publishedAt, Long taskCount) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.publishedAt = publishedAt;
        this.taskCount = taskCount;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getPublishedAt() { return publishedAt; }

    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public Long getTaskCount() { return taskCount; }

    public void setTaskCount(Long taskCount) { this.taskCount = taskCount; }
}
