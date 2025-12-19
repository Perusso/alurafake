package br.com.alura.AluraFake.task.dto.response;

import br.com.alura.AluraFake.task.Type;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse{
    private final Long id;
    private final Long courseId;
    private final String statement;
    private final Integer order;
    private final Type type;
    private final LocalDateTime createdAt;
    private final List<OptionResponse> options;

    public TaskResponse(Long id, Long courseId, String statement, Integer order,
                        Type type, LocalDateTime createdAt, List<OptionResponse> options) {
        this.id = id;
        this.courseId = courseId;
        this.statement = statement;
        this.order = order;
        this.type = type;
        this.createdAt = createdAt;
        this.options = options;
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public String getStatement() { return statement; }
    public Integer getOrder() { return order; }
    public Type getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OptionResponse> getOptions() { return options; }
}
