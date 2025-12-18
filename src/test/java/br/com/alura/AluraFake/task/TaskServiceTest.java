package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.MultipleChoiceTaskRequest;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.OptionRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

        taskService.createOpenTextTask(request);

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
                .hasMessageContaining("mesmo enunciado");
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

        taskService.createSingleChoiceTask(request);

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
                .hasMessageContaining("no mínimo 2");
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
                .hasMessageContaining("no máximo 5");
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
                .hasMessageContaining("uma única alternativa correta");
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
                .hasMessageContaining("uma única alternativa correta");
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
                .hasMessageContaining("no mínimo 4");
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
                .hasMessageContaining("iguais entre si");
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
                .hasMessageContaining("iguais ao enunciado");
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

        taskService.createMultipleChoiceTask(request);

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
                .hasMessageContaining("no mínimo 3");
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
                .hasMessageContaining("no máximo 5");
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
                .hasMessageContaining("duas ou mais alternativas corretas");
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
                .hasMessageContaining("ao menos uma alternativa incorreta");
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
                .hasMessageContaining("no mínimo 4");
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
                .hasMessageContaining("iguais entre si");
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
                .hasMessageContaining("iguais ao enunciado");
    }
}
