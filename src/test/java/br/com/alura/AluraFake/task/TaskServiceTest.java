package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

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
}
