package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = CourseService.class)
public class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    void publishCourse_shouldPublishCourseWhenAllConditionsAreMet() {
        Long courseId = 1L;

        Course course = mock(Course.class);
        when(course.getStatus()).thenReturn(Status.BUILDING);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(taskRepository.countDistinctTypeByCourse(course)).thenReturn(3L);
        when(taskRepository.countByCourse(course)).thenReturn(5L);
        when(taskRepository.findMaxOrderByCourse(course)).thenReturn(Optional.of(5));

        courseService.publishCourse(courseId);

        verify(course).setStatus(Status.PUBLISHED);
        verify(course).setPublishedAt(any(LocalDateTime.class));
        verify(courseRepository).save(course);
    }

    @Test
    void publishCourse_shouldThrowExceptionWhenCourseNotFound() {
        Long courseId = 999L;

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.publishCourse(courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Course not found");

        verify(courseRepository, never()).save(any());
    }

    @Test
    void publishCourse_shouldThrowExceptionWhenCourseIsNotBuilding() {
        Long courseId = 1L;

        Course course = mock(Course.class);
        when(course.getStatus()).thenReturn(Status.PUBLISHED);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.publishCourse(courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Course is not in BUILDING status");

        verify(courseRepository, never()).save(any());
    }

    @Test
    void publishCourse_shouldThrowExceptionWhenMissingTaskTypes() {
        Long courseId = 1L;

        Course course = mock(Course.class);
        when(course.getStatus()).thenReturn(Status.BUILDING);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(taskRepository.countDistinctTypeByCourse(course)).thenReturn(2L);

        assertThatThrownBy(() -> courseService.publishCourse(courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Course must have at least one activity of each type");

        verify(courseRepository, never()).save(any());
    }

    @Test
    void publishCourse_shouldThrowExceptionWhenNoTasks() {
        Long courseId = 1L;

        Course course = mock(Course.class);
        when(course.getStatus()).thenReturn(Status.BUILDING);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(taskRepository.countDistinctTypeByCourse(course)).thenReturn(0L);
        when(taskRepository.countByCourse(course)).thenReturn(0L);
        when(taskRepository.findMaxOrderByCourse(course)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.publishCourse(courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Course must have at least one activity of each type");

        verify(courseRepository, never()).save(any());
    }

    @Test
    void publishCourse_shouldThrowExceptionWhenTaskOrderNotContinuous_Gap() {
        Long courseId = 1L;

        Course course = mock(Course.class);
        when(course.getStatus()).thenReturn(Status.BUILDING);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(taskRepository.countDistinctTypeByCourse(course)).thenReturn(3L);
        when(taskRepository.countByCourse(course)).thenReturn(3L);
        when(taskRepository.findMaxOrderByCourse(course)).thenReturn(Optional.of(5));

        assertThatThrownBy(() -> courseService.publishCourse(courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Activity orders are not continuous");

        verify(courseRepository, never()).save(any());
    }

    @Test
    void publishCourse_shouldThrowExceptionWhenTaskOrderNotContinuous_NonSequential() {
        Long courseId = 1L;

        Course course = mock(Course.class);
        when(course.getStatus()).thenReturn(Status.BUILDING);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(taskRepository.countDistinctTypeByCourse(course)).thenReturn(3L);
        when(taskRepository.countByCourse(course)).thenReturn(4L);
        when(taskRepository.findMaxOrderByCourse(course)).thenReturn(Optional.of(3));

        assertThatThrownBy(() -> courseService.publishCourse(courseId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Activity orders are not continuous");

        verify(courseRepository, never()).save(any());
    }
}
