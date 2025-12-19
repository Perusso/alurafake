package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.task.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, TaskRepository taskRepository) {
        this.courseRepository = courseRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void publishCourse(Long courseId) {
        Course course = validatePublishCourseRequest(courseId);

        course.setStatus(Status.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        courseRepository.save(course);
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
}
