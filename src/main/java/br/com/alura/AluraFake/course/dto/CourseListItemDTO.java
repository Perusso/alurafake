package br.com.alura.AluraFake.course.dto;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.Status;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CourseListItemDTO implements Serializable {

    private Long id;
    private String title;
    private String description;
    private Status status;

    public CourseListItemDTO() {
    }

    public CourseListItemDTO(Course course) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.status = course.getStatus();
    }

    public CourseListItemDTO(Long id, String title, String description, Status status, LocalDateTime publishedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }
}
