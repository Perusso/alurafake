package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.OptionRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class TaskService {

    private final CourseRepository courseRepository;

    private final TaskRepository taskRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public TaskService(CourseRepository courseRepository, TaskRepository taskRepository) {
        this.courseRepository = courseRepository;
        this.taskRepository = taskRepository;
    }

    //TODO: Create specific exception classes for better error handling
    public void createOpenTextTask(OpenTextTaskRequest request) {
        Course course = validateCommonRequirements(request.getCourseId(), request.getStatement(), request.getOrder());
        shiftTasksForNewOrder(course, request.getOrder());
        persistTask(course, request.getStatement(), request.getOrder(), Type.OPEN_TEXT, null);
    }

    public void createSingleChoiceTask(SingleChoiceTaskRequest request) {
        Course course = validateCommonRequirements(request.getCourseId(), request.getStatement(), request.getOrder());
        validateSingleChoiceOptions(request.getOptions(), request.getStatement());
        shiftTasksForNewOrder(course, request.getOrder());
        String optionsJson = convertOptionsToJson(request.getOptions());
        persistTask(course, request.getStatement(), request.getOrder(), Type.SINGLE_CHOICE, optionsJson);
    }

    private Course validateCommonRequirements(Long courseId, String statement, Integer order) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Curso não encontrado"));

        if (course.getStatus() != Status.BUILDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Um curso só pode receber atividades se seu status for BUILDING");
        }

        boolean statementExists = taskRepository.existsByCourseAndStatement(course, statement);
        if (statementExists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O curso não pode ter duas questões com o mesmo enunciado");
        }

        int maxOrder = taskRepository.findMaxOrderByCourse(course).orElse(0);
        if (order > maxOrder + 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Ordem inválida. A sequência deve ser contínua. " +
                            "A próxima ordem permitida é %d", maxOrder + 1));
        }
        return course;
    }

    private void shiftTasksForNewOrder(Course course, Integer newOrder) {
        List<Task> tasksToShift = taskRepository.findByCourseAndOrderGreaterThanEqualOrderByOrderDesc(course, newOrder);
        for (Task task : tasksToShift) {
            task.setOrder(task.getOrder() + 1);
        }
        taskRepository.saveAll(tasksToShift);
    }

    private void validateSingleChoiceOptions(List<OptionRequest> options, String statement) {
        if (options == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Atividade de alternativa única deve ter opções");
        }

        int size = options.size();
        if (size < 2 || size > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A atividade deve ter no mínimo 2 e no máximo 5 alternativas");
        }

        for (OptionRequest option : options) {
            String text = option.getOption();
            if (text == null || text.length() < 4 || text.length() > 80) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "As alternativas devem ter no mínimo 4 e no máximo 80 caracteres");
            }

            if (text.equals(statement)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "As alternativas não podem ser iguais ao enunciado da atividade");
            }
        }

        long distinctCount = options.stream()
                .map(OptionRequest::getOption)
                .distinct()
                .count();

        if (distinctCount != options.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "As alternativas não podem ser iguais entre si");
        }

        long correctCount = options.stream()
                .filter(OptionRequest::getIsCorrect)
                .count();

        if (correctCount != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A atividade deve ter uma única alternativa correta");
        }
    }

    private String convertOptionsToJson(List<OptionRequest> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erro ao processar opções da atividade");
        }
    }

    private void persistTask(Course course, String statement, Integer order, Type type, String optionsJson) {
        Task task = new Task();
        task.setCourse(course);
        task.setStatement(statement);
        task.setOrder(order);
        task.setType(type);

        if (optionsJson != null) {
            task.setOptions(optionsJson);
        }
        taskRepository.save(task);
    }

    public void createMultipleChoiceTask(MultipleChoiceTaskRequest request) {
    }
}
