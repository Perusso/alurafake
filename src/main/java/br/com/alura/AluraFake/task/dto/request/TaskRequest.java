package br.com.alura.AluraFake.task.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskRequest {

    @NotNull
    private Long courseId;

    @NotBlank
    @Size(min = 4, max = 255)
    private String statement;

    @Min(1)
    @NotNull
    private Integer order;

    public Long getCourseId() { return courseId; }

    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getStatement() { return statement; }

    public void setStatement(String statement) { this.statement = statement; }

    public Integer getOrder() { return order; }

    public void setOrder(Integer order) { this.order = order; }
}
