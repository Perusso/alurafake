package br.com.alura.AluraFake.user.dto;

import java.util.List;

public class InstructorCoursesReportResponse {

    private List<InstructorCourseDTO> courses;
    private Long totalPublishedCourses;

    public InstructorCoursesReportResponse(List<InstructorCourseDTO> courses,
                                           Long totalPublishedCourses) {
        this.courses = courses;
        this.totalPublishedCourses = totalPublishedCourses;
    }

    public List<InstructorCourseDTO> getCourses() { return courses; }

    public void setCourses(List<InstructorCourseDTO> courses) { this.courses = courses; }

    public Long getTotalPublishedCourses() { return totalPublishedCourses; }

    public void setTotalPublishedCourses(Long totalPublishedCourses) {
        this.totalPublishedCourses = totalPublishedCourses;
    }
}
