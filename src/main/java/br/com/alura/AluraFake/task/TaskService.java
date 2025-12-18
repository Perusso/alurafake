package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class TaskService {

    private final CourseRepository courseRepository;

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(CourseRepository courseRepository, TaskRepository taskRepository) {
        this.courseRepository = courseRepository;
        this.taskRepository = taskRepository;
    }

    //TODO: Refactor validation logic into separate methods
    //TODO: Create specific exception classes for better error handling
    public void createOpenTextTask(OpenTextTaskRequest request) {

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Curso não encontrado"));

        if (course.getStatus() != Status.BUILDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Um curso só pode receber atividades se seu status for BUILDING");
        }


        boolean statementExists = taskRepository.existsByCourseAndStatement(course, request.getStatement());
        if (statementExists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O curso não pode ter duas questões com o mesmo enunciado");
        }


        int maxOrder = taskRepository.findMaxOrderByCourse(course).orElse(0);
        if (request.getOrder() > maxOrder + 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Ordem inválida. A sequência deve ser contínua. " +
                            "A próxima ordem permitida é %d", maxOrder + 1));
        }

        shiftTasksForNewOrder(course, request.getOrder());


        Task task = new Task();
        task.setCourse(course);
        task.setStatement(request.getStatement());
        task.setOrder(request.getOrder());
        task.setType(Type.OPEN_TEXT);

        taskRepository.save(task);
    }

    private void shiftTasksForNewOrder(Course course, Integer newOrder) {
        List<Task> tasksToShift = taskRepository.findByCourseAndOrderGreaterThanEqual(course, newOrder);
        for (Task task : tasksToShift) {
            task.setOrder(task.getOrder() + 1);
        }
        taskRepository.saveAll(tasksToShift);
    }

    public void createSingleChoiceTask(SingleChoiceTaskRequest request) {
    }

    public void createMultipleChoiceTask(MultipleChoiceTaskRequest request) {
    }
}
