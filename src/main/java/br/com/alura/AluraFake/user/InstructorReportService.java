package br.com.alura.AluraFake.user;

import br.com.alura.AluraFake.course.Course;
import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.course.Status;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.user.dto.InstructorCourseDTO;
import br.com.alura.AluraFake.user.dto.InstructorCoursesReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstructorReportService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public InstructorReportService(UserRepository userRepository,
                                   CourseRepository courseRepository,
                                   TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.taskRepository = taskRepository;
    }

    public InstructorCoursesReportResponse getInstructorCoursesReport(Long instructorId) {
        User user = validateReportRequest(instructorId);
        InstructorCoursesRecord count = getCountInstructorCourses(user);
        return new InstructorCoursesReportResponse(count.courseDTOs(), count.totalPublishedCourses());
    }

    private InstructorCoursesRecord getCountInstructorCourses(User user) {
        List<Course> courses = courseRepository.findByInstructor(user);

        List<InstructorCourseDTO> courseDTOs = courses.stream()
                .map(course -> {
                    Long taskCount = taskRepository.countByCourse(course);
                    return new InstructorCourseDTO(
                            course.getId(),
                            course.getTitle(),
                            course.getStatus(),
                            course.getPublishedAt(),
                            taskCount
                    );
                })
                .collect(Collectors.toList());

        long totalPublishedCourses = courses.stream()
                .filter(course -> course.getStatus() == Status.PUBLISHED)
                .count();
        return new InstructorCoursesRecord(courseDTOs, totalPublishedCourses);
    }

    private record InstructorCoursesRecord(List<InstructorCourseDTO> courseDTOs, long totalPublishedCourses) {
    }

    private User validateReportRequest(Long instructorId) {
        User user = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        if (!user.isInstructor()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an instructor");
        }
        return user;
    }
}
