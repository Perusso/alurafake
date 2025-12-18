package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.task.dto.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final CourseRepository courseRepository;

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(CourseRepository courseRepository, TaskRepository taskRepository) {
        this.courseRepository = courseRepository;
        this.taskRepository = taskRepository;
    }


    public void createOpenTextTask(OpenTextTaskRequest request) {
    }

    public void createSingleChoiceTask(SingleChoiceTaskRequest request) {
    }

    public void createMultipleChoiceTask(MultipleChoiceTaskRequest request) {
    }
}
