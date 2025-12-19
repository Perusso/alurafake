package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.request.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.request.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.request.OptionRequest;
import br.com.alura.AluraFake.task.dto.request.SingleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.response.OptionResponse;
import br.com.alura.AluraFake.task.dto.response.TaskResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


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
    public TaskResponse createOpenTextTask(OpenTextTaskRequest request) {
        Course course = validateCommonRequirements(request.getCourseId(), request.getStatement(), request.getOrder());
        shiftTasksForNewOrder(course, request.getOrder());
        Task task = persistTask(course, request.getStatement(), request.getOrder(), Type.OPEN_TEXT, null);
        return taskToResponseDTO(task);
    }

    public TaskResponse createSingleChoiceTask(SingleChoiceTaskRequest request) {
        Course course = validateCommonRequirements(request.getCourseId(), request.getStatement(), request.getOrder());
        validateSingleChoiceOptions(request.getOptions(), request.getStatement());
        shiftTasksForNewOrder(course, request.getOrder());
        String optionsJson = convertOptionsToJson(request.getOptions());
        Task task = persistTask(course, request.getStatement(), request.getOrder(), Type.SINGLE_CHOICE, optionsJson);
        return taskToResponseDTO(task);
    }

    public TaskResponse createMultipleChoiceTask(MultipleChoiceTaskRequest request) {
        Course course = validateCommonRequirements(request.getCourseId(), request.getStatement(), request.getOrder());
        validateMultipleChoiceOptions(request.getOptions(), request.getStatement());
        shiftTasksForNewOrder(course, request.getOrder());
        String optionsJson = convertOptionsToJson(request.getOptions());
        Task task = persistTask(course, request.getStatement(), request.getOrder(), Type.MULTIPLE_CHOICE, optionsJson);
        return taskToResponseDTO(task);
    }


    private Course validateCommonRequirements(Long courseId, String statement, Integer order) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Course not found"));

        if (course.getStatus() != Status.BUILDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot add tasks to a course that is not in BUILDING status");
        }

        boolean statementExists = taskRepository.existsByCourseAndStatement(course, statement);
        if (statementExists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Already exists a task with the same statement in this course");
        }

        int maxOrder = taskRepository.findMaxOrderByCourse(course).orElse(0);
        if (order > maxOrder + 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Invalid Order, must be continuous" +
                            "Next allowed order is %d", maxOrder + 1));
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
                    "Single choice task must have options");
        }

        int size = options.size();
        if (size < 2 || size > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The activity must have at least 2 and at most 5 alternatives");
        }

        validateCommonOptionRules(options, statement);

        long correctCount = options.stream()
                .filter(OptionRequest::getIsCorrect)
                .count();

        if (correctCount != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity must have one correct option, Found: " + correctCount);
        }
    }

    private String convertOptionsToJson(List<OptionRequest> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error trying to process options");
        }
    }

    private void validateMultipleChoiceOptions(List<OptionRequest> options, String statement) {
        if (options == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Multiple choice task must have options");
        }

        int size = options.size();
        if (size < 3 || size > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity must have at least 3 and at most 5 alternatives");
        }

        validateCommonOptionRules(options, statement);

        long correctCount = options.stream()
                .filter(OptionRequest::getIsCorrect)
                .count();

        if (correctCount < 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity must have at least two correct options");
        }

        long incorrectCount = size - correctCount;
        if (incorrectCount < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity must have at least one incorrect option");
        }
    }

    private void validateCommonOptionRules(List<OptionRequest> options, String statement) {
        for (OptionRequest option : options) {
            String text = option.getOption();
            if (text == null || text.length() < 4 || text.length() > 80) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Options must have between 4 and 80 characters. " +
                                "Problem in option: '" + text + "'");
            }

            if (text.equals(statement)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Options cannot be the same as the task statement. " +
                                "Problem in option: '" + text + "'");
            }
        }

        long distinctCount = options.stream()
                .map(OptionRequest::getOption)
                .distinct()
                .count();

        if (distinctCount != options.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "All options must be unique");
        }
    }

    private Task persistTask(Course course, String statement, Integer order, Type type, String optionsJson) {
        Task task = new Task();
        task.setCourse(course);
        task.setStatement(statement);
        task.setOrder(order);
        task.setType(type);

        if (optionsJson != null) {
            task.setOptions(optionsJson);
        }
        return taskRepository.save(task);
    }

    private TaskResponse taskToResponseDTO(Task task) {
        List<OptionResponse> options = null;
        if (task.getOptions() != null) {
            options = convertJsonToOptionResponses(task.getOptions());
        }
        return new TaskResponse(task.getId(), task.getCourse().getId(), task.getStatement(), task.getOrder(), task.getType(), task.getCreatedAt(), options);
    }

    private List<OptionResponse> convertJsonToOptionResponses(String optionsJson) {
        try {
            List<OptionRequest> optionRequests = objectMapper.readValue(
                    optionsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, OptionRequest.class)
            );

            return optionRequests.stream()
                    .map(req -> new OptionResponse(req.getOption(), req.getIsCorrect()))
                    .collect(Collectors.toList());

        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
