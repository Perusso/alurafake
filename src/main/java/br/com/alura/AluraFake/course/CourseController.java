package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.course.dto.CourseListItemDTO;
import br.com.alura.AluraFake.course.dto.CourseResponse;
import br.com.alura.AluraFake.course.dto.NewCourseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/course/new")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createCourse(@Valid @RequestBody NewCourseDTO newCourse) {
        return courseService.createCourse(newCourse);
    }

    @GetMapping("/course/all")
    public List<CourseListItemDTO> createCourse() {
        return courseService.getAllCourses();
    }

    @PostMapping("/course/{id}/publish")
    public CourseResponse createCourse(@PathVariable("id") Long id) {
        return courseService.publishCourse(id);
    }

}
