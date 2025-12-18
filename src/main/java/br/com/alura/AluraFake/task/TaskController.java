package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.task.dto.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/task/new/opentext")
    public ResponseEntity newOpenTextExercise(@Valid @RequestBody OpenTextTaskRequest request) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/task/new/singlechoice")
    public ResponseEntity newSingleChoice(@Valid @RequestBody SingleChoiceTaskRequest request) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/task/new/multiplechoice")
    public ResponseEntity newMultipleChoice(@Valid @RequestBody MultipleChoiceTaskRequest request) {
        return ResponseEntity.ok().build();
    }

}