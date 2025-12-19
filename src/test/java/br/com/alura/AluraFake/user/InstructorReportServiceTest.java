package br.com.alura.AluraFake.user;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.user.dto.InstructorCoursesReportResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = InstructorReportService.class)
public class InstructorReportServiceTest {

    @Autowired
    private InstructorReportService instructorReportService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    void getInstructorCoursesReport_shouldReturnReportWhenInstructorExistsAndHasCourses() {
        Long instructorId = 1L;

        User instructor = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);

        Course course1 = new Course("Java", "Curso de Java", instructor);
        Course course2 = new Course("Spring", "Curso de Spring", instructor);

        course1.setStatus(Status.PUBLISHED);
        course1.setPublishedAt(LocalDateTime.now());
        course2.setStatus(Status.BUILDING);

        List<Course> courses = Arrays.asList(course1, course2);

        when(userRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByInstructor(instructor)).thenReturn(courses);
        when(taskRepository.countByCourse(course1)).thenReturn(5L);
        when(taskRepository.countByCourse(course2)).thenReturn(3L);

        InstructorCoursesReportResponse report = instructorReportService.getInstructorCoursesReport(instructorId);

        assertThat(report.getCourses()).hasSize(2);
        assertThat(report.getTotalPublishedCourses()).isEqualTo(1);

        assertThat(report.getCourses().get(0).getTitle()).isEqualTo("Java");
        assertThat(report.getCourses().get(0).getStatus()).isEqualTo(Status.PUBLISHED);
        assertThat(report.getCourses().get(0).getTaskCount()).isEqualTo(5L);

        assertThat(report.getCourses().get(1).getTitle()).isEqualTo("Spring");
        assertThat(report.getCourses().get(1).getStatus()).isEqualTo(Status.BUILDING);
        assertThat(report.getCourses().get(1).getTaskCount()).isEqualTo(3L);
    }

    @Test
    void getInstructorCoursesReport_shouldThrow404WhenUserNotFound() {

        Long nonExistentId = 999L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instructorReportService.getInstructorCoursesReport(nonExistentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found")
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode().value() == 404);
    }

    @Test
    void getInstructorCoursesReport_shouldThrow400WhenUserIsNotInstructor() {
        Long studentId = 2L;
        User student = new User("João", "joao@email.com", Role.STUDENT);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> instructorReportService.getInstructorCoursesReport(studentId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User is not an instructor")
                .matches(ex -> ((ResponseStatusException) ex).getStatusCode().value() == 400);
    }

    @Test
    void getInstructorCoursesReport_shouldReturnEmptyListWhenInstructorHasNoCourses() {
        Long instructorId = 3L;
        User instructor = new User("Maria", "maria@alura.com.br", Role.INSTRUCTOR);

        when(userRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByInstructor(instructor)).thenReturn(Arrays.asList());

        InstructorCoursesReportResponse report = instructorReportService.getInstructorCoursesReport(instructorId);

        assertThat(report.getCourses()).isEmpty();
        assertThat(report.getTotalPublishedCourses()).isEqualTo(0);
    }

    @Test
    void getInstructorCoursesReport_shouldCountOnlyPublishedCourses() {
        Long instructorId = 4L;
        User instructor = new User("Carlos", "carlos@alura.com.br", Role.INSTRUCTOR);

        Course published1 = new Course("Course 1", "Desc 1", instructor);
        published1.setStatus(Status.PUBLISHED);
        published1.setPublishedAt(LocalDateTime.now());

        Course published2 = new Course("Course 2", "Desc 2", instructor);
        published2.setStatus(Status.PUBLISHED);
        published2.setPublishedAt(LocalDateTime.now());

        Course building = new Course("Course 3", "Desc 3", instructor);
        building.setStatus(Status.BUILDING);

        List<Course> courses = Arrays.asList(published1, published2, building);

        when(userRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByInstructor(instructor)).thenReturn(courses);
        when(taskRepository.countByCourse(any(Course.class))).thenReturn(2L);

        InstructorCoursesReportResponse report = instructorReportService.getInstructorCoursesReport(instructorId);

        assertThat(report.getTotalPublishedCourses()).isEqualTo(2);
        assertThat(report.getCourses()).hasSize(3);
    }
}
