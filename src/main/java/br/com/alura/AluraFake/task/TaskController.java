package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.request.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.request.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.request.SingleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.response.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/task/new/opentext")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse newOpenTextExercise(@Valid @RequestBody OpenTextTaskRequest request) {
        return taskService.createOpenTextTask(request);
    }

    @PostMapping("/task/new/singlechoice")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse newSingleChoice(@Valid @RequestBody SingleChoiceTaskRequest request) {
        return taskService.createSingleChoiceTask(request);
    }

    @PostMapping("/task/new/multiplechoice")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse newMultipleChoice(@Valid @RequestBody MultipleChoiceTaskRequest request) {
        return taskService.createMultipleChoiceTask(request);
    }

}