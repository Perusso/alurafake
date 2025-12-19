package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.course.dto.CourseListItemDTO;
import br.com.alura.AluraFake.course.dto.CourseResponse;
import br.com.alura.AluraFake.course.dto.NewCourseDTO;
import br.com.alura.AluraFake.task.TaskRepository;
import br.com.alura.AluraFake.user.User;
import br.com.alura.AluraFake.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public CourseResponse createCourse(NewCourseDTO newCourseDTO) {
        User instructor = validateInstructor(newCourseDTO.getEmailInstructor());
        Course course = new Course(newCourseDTO.getTitle(), newCourseDTO.getDescription(), instructor);
        course = courseRepository.save(course);
        return toCourseResponse(course);
    }

    @Transactional
    public CourseResponse publishCourse(Long courseId) {
        Course course = validatePublishCourseRequest(courseId);
        course.setStatus(Status.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        Course response = courseRepository.save(course);
        return toCourseResponse(response);
    }

    private Course validatePublishCourseRequest(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Course not found"));

        if (course.getStatus() != Status.BUILDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course is not in BUILDING status. Current status: " + course.getStatus());
        }

        Long distinctTypes = taskRepository.countDistinctTypeByCourse(course);
        if (distinctTypes < 3) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Course must have at least one activity of each type (Open Text, Single Choice, Multiple Choice)");
        }

        boolean isContinuous = isOrderSequenceContinuous(course);
        if (!isContinuous) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity orders are not continuous (e.g., 1, 2, 3...)");
        }
        return course;
    }

    private boolean isOrderSequenceContinuous(Course course) {
        Long totalTasks = taskRepository.countByCourse(course);
        if (totalTasks == 0) {
            return false;
        }

        int maxOrder = taskRepository.findMaxOrderByCourse(course).orElse(0);
        return totalTasks == maxOrder;
    }

    private User validateInstructor(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User not found with email: " + email);
        }
        User foundUser = user.get();
        if (!foundUser.isInstructor()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not an instructor");
        }
        return foundUser;
    }

    public CourseResponse toCourseResponse(Course course) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getDescription(), course.getInstructor(), course.getStatus(), course.getCreatedAt(), course.getPublishedAt());
    }

    public List<CourseListItemDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::toCourseListItemDTO)
                .toList();
    }

    private CourseListItemDTO toCourseListItemDTO(Course course) {
        return new CourseListItemDTO(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getStatus(),
                course.getPublishedAt()
        );
    }
}
