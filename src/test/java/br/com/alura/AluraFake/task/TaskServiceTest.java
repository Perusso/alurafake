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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TaskService.class)
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private CourseRepository courseRepository;

    @Test
    void createOpenTextTask_shouldCreateTaskWhenRequestIsValid() {
        Long courseId = 1L;
        OpenTextTaskRequest request = new OpenTextTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Fake Statement");
        request.setOrder(1);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setStatement("Fake Statement");
        savedTask.setOrder(1);
        savedTask.setType(Type.OPEN_TEXT);
        savedTask.setCourse(mockCourse);
        savedTask.setCreatedAt(LocalDateTime.now());

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createOpenTextTask(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatement()).isEqualTo("Fake Statement");
        assertThat(response.getType()).isEqualTo(Type.OPEN_TEXT);
        assertThat(response.getOptions()).isNull();
        verify(courseRepository).findById(courseId);
        verify(taskRepository).existsByCourseAndStatement(mockCourse, request.getStatement());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createOpenTextTask_shouldThrowExceptionWhenCourseIsNotBuilding() {
        Long courseId = 2L;
        OpenTextTaskRequest request = new OpenTextTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Fake Statement");
        request.setOrder(1);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.PUBLISHED); // Status INVÁLIDO
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));

        assertThatThrownBy(() -> taskService.createOpenTextTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("BUILDING");
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createOpenTextTask_shouldThrowExceptionWhenStatementIsNotUnique() {
        Long courseId = 1L;
        String duplicateStatement = "Duplicate Statement";
        OpenTextTaskRequest request = new OpenTextTaskRequest();
        request.setCourseId(courseId);
        request.setStatement(duplicateStatement);
        request.setOrder(1);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, duplicateStatement)).thenReturn(true);

        assertThatThrownBy(() -> taskService.createOpenTextTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Already exists a task with the same statement in this course");
    }

    @Test
    void createSingleChoiceTask_shouldCreateTaskWhenRequestIsValid() {
        Long courseId = 1L;
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Qual linguagem usamos no Spring Boot?");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Java", true),
                new OptionRequest("Python", false),
                new OptionRequest("Flutter", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());
        when(taskRepository.findByCourseAndOrderGreaterThanEqualOrderByOrderDesc(any(), any())).thenReturn(List.of());


        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setStatement("Qual linguagem usamos no Spring Boot?");
        savedTask.setOrder(1);
        savedTask.setType(Type.SINGLE_CHOICE);
        savedTask.setCourse(mockCourse);
        savedTask.setCreatedAt(LocalDateTime.now());
        savedTask.setOptions("[{\"option\":\"Java\",\"isCorrect\":true},{\"option\":\"Python\",\"isCorrect\":false},{\"option\":\"Flutter\",\"isCorrect\":false}]");

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        List<OptionResponse> optionResponses = Arrays.asList(
                new OptionResponse("Java", true),
                new OptionResponse("Python", false),
                new OptionResponse("Flutter", false)
        );

        TaskResponse response = taskService.createSingleChoiceTask(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatement()).isEqualTo("Qual linguagem usamos no Spring Boot?");
        assertThat(response.getType()).isEqualTo(Type.SINGLE_CHOICE);
        verify(courseRepository).findById(courseId);
        verify(taskRepository).existsByCourseAndStatement(mockCourse, request.getStatement());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createSingleChoiceTask_shouldThrowExceptionWhenLessThan2Options() {
        Long courseId = 1L;
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta com poucas opções");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Única opção", true)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createSingleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("The activity must have at least 2 and at most 5 alternatives");
    }

    @Test
    void createSingleChoiceTask_shouldThrowExceptionWhenMoreThan5Options() {
        Long courseId = 1L;
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta com muitas opções");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Opção 1", true),
                new OptionRequest("Opção 2", false),
                new OptionRequest("Opção 3", false),
                new OptionRequest("Opção 4", false),
                new OptionRequest("Opção 5", false),
                new OptionRequest("Opção 6", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createSingleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("The activity must have at least 2 and at most 5 alternatives");
    }

    @Test
    void createSingleChoiceTask_shouldThrowExceptionWhenNoCorrectOption() {
        Long courseId = 1L;
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta sem alternativa correta");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Opção A", false),
                new OptionRequest("Opção B", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createSingleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Activity must have one correct option");
    }

    @Test
    void createSingleChoiceTask_shouldThrowExceptionWhenMultipleCorrectOptions() {
        Long courseId = 1L;
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta com múltiplas corretas");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Opção A", true),
                new OptionRequest("Opção B", true),
                new OptionRequest("Opção C", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createSingleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Activity must have one correct option");
    }

    @Test
    void createSingleChoiceTask_shouldThrowExceptionWhenOptionTextTooShort() {
        Long courseId = 1L;
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta com opção curta");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Sim", true),
                new OptionRequest("Não", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createSingleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Options must have between 4 and 80 characters");
    }

    @Test
    void createSingleChoiceTask_shouldThrowExceptionWhenDuplicateOptions() {
        Long courseId = 1L;
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta com opções duplicadas");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Java", true),
                new OptionRequest("Java", false),
                new OptionRequest("Python", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createSingleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("All options must be unique");
    }

    @Test
    void createSingleChoiceTask_shouldThrowExceptionWhenOptionEqualsStatement() {
        String statement = "Qual é a melhor linguagem?";
        Long courseId = 1L;
        SingleChoiceTaskRequest request = new SingleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement(statement);
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Java", true),
                new OptionRequest(statement, false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createSingleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Options cannot be the same as the task statement");
    }

    @Test
    void createMultipleChoiceTask_shouldCreateTaskWhenRequestIsValid() {
        Long courseId = 1L;
        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Quais são frameworks Java?");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Spring", true),
                new OptionRequest("Hibernate", true),
                new OptionRequest("Django", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());
        when(taskRepository.findByCourseAndOrderGreaterThanEqualOrderByOrderDesc(any(), any())).thenReturn(List.of());

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setStatement("Quais são frameworks Java?");
        savedTask.setOrder(1);
        savedTask.setType(Type.MULTIPLE_CHOICE);
        savedTask.setCourse(mockCourse);
        savedTask.setCreatedAt(LocalDateTime.now());
        savedTask.setOptions("[{\"option\":\"Spring\",\"isCorrect\":true},{\"option\":\"Hibernate\",\"isCorrect\":true},{\"option\":\"Django\",\"isCorrect\":false}]");

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createMultipleChoiceTask(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatement()).isEqualTo("Quais são frameworks Java?");
        assertThat(response.getType()).isEqualTo(Type.MULTIPLE_CHOICE);
        verify(courseRepository).findById(courseId);
        verify(taskRepository).existsByCourseAndStatement(mockCourse, request.getStatement());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createMultipleChoiceTask_shouldThrowExceptionWhenLessThan3Options() {
        Long courseId = 1L;
        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta com poucas opções");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Spring", true),
                new OptionRequest("Hibernate", true)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createMultipleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Activity must have at least 3 and at most 5 alternatives");
    }

    @Test
    void createMultipleChoiceTask_shouldThrowExceptionWhenMoreThan5Options() {
        Long courseId = 1L;
        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta com muitas opções");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Opção 1", true),
                new OptionRequest("Opção 2", true),
                new OptionRequest("Opção 3", false),
                new OptionRequest("Opção 4", false),
                new OptionRequest("Opção 5", false),
                new OptionRequest("Opção 6", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createMultipleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("at least 3 and at most 5");
    }

    @Test
    void createMultipleChoiceTask_shouldThrowExceptionWhenLessThan2CorrectOptions() {
        Long courseId = 1L;
        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta com apenas 1 correta");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Spring", true),
                new OptionRequest("Hibernate", false),
                new OptionRequest("Django", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createMultipleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Activity must have at least two correct options");
    }

    @Test
    void createMultipleChoiceTask_shouldThrowExceptionWhenNoIncorrectOption() {
        Long courseId = 1L;
        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta sem alternativas incorretas");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Spring", true),
                new OptionRequest("Hibernate", true),
                new OptionRequest("JPABuddy", true)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createMultipleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Activity must have at least one incorrect option");
    }

    @Test
    void createMultipleChoiceTask_shouldThrowExceptionWhenOptionTextTooShort() {
        Long courseId = 1L;
        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta com opção curta");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Spring Framework", true),
                new OptionRequest("Hi", true),
                new OptionRequest("Django Framework", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createMultipleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Options must have between 4 and 80 characters");
    }

    @Test
    void createMultipleChoiceTask_shouldThrowExceptionWhenDuplicateOptions() {
        Long courseId = 1L;
        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement("Pergunta com opções duplicadas");
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Spring", true),
                new OptionRequest("Spring", true),
                new OptionRequest("Hibernate", false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createMultipleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("All options must be unique");
    }

    @Test
    void createMultipleChoiceTask_shouldThrowExceptionWhenOptionEqualsStatement() {
        String statement = "Quais são frameworks Java?";
        Long courseId = 1L;
        MultipleChoiceTaskRequest request = new MultipleChoiceTaskRequest();
        request.setCourseId(courseId);
        request.setStatement(statement);
        request.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Spring", true),
                new OptionRequest("Hibernate", true),
                new OptionRequest(statement, false)
        );
        request.setOptions(options);

        Course mockCourse = mock(Course.class);
        when(mockCourse.getStatus()).thenReturn(Status.BUILDING);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mockCourse));
        when(taskRepository.existsByCourseAndStatement(mockCourse, request.getStatement())).thenReturn(false);
        when(taskRepository.findMaxOrderByCourse(mockCourse)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createMultipleChoiceTask(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Options cannot be the same as the task statement");
    }
}
