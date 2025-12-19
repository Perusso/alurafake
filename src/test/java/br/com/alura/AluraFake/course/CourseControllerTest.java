package br.com.alura.AluraFake.course;

import br.com.alura.AluraFake.course.dto.CourseListItemDTO;
import br.com.alura.AluraFake.course.dto.CourseResponse;
import br.com.alura.AluraFake.course.dto.NewCourseDTO;
import br.com.alura.AluraFake.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CourseRepository courseRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CourseService courseService;

    @Test
    void newCourseDTO__should_return_bad_request_when_email_is_invalid() throws Exception {

        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("paulo@alura.com.br");

        when(courseService.createCourse(any(NewCourseDTO.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "User not found with email: paulo@alura.com.br"));

        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void newCourseDTO__should_return_created_when_new_course_request_is_valid() throws Exception {

        NewCourseDTO newCourseDTO = new NewCourseDTO();
        newCourseDTO.setTitle("Java");
        newCourseDTO.setDescription("Curso de Java");
        newCourseDTO.setEmailInstructor("paulo@alura.com.br");

        User user = mock(User.class);
        doReturn(true).when(user).isInstructor();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(newCourseDTO.getEmailInstructor());

        mockMvc.perform(post("/course/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourseDTO)))
                .andExpect(status().isCreated());

        verify(courseService, times(1)).createCourse(any(NewCourseDTO.class));
    }

    @Test
    void listAllCourses__should_list_all_courses() throws Exception {
        User paulo = new User("Paulo", "paulo@alua.com.br", Role.INSTRUCTOR);

        CourseListItemDTO java = new CourseListItemDTO(1L, "Java", "Curso de java", Status.BUILDING, null);
        CourseListItemDTO hibernate = new CourseListItemDTO(2L, "Hibernate", "Curso de hibernate", Status.PUBLISHED, LocalDateTime.now());
        CourseListItemDTO spring = new CourseListItemDTO(3L, "Spring", "Curso de spring", Status.BUILDING, null);

        List<CourseListItemDTO> courses = Arrays.asList(java, hibernate, spring);

        when(courseService.getAllCourses()).thenReturn(courses);

        mockMvc.perform(get("/course/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java"))
                .andExpect(jsonPath("$[0].description").value("Curso de java"))
                .andExpect(jsonPath("$[1].title").value("Hibernate"))
                .andExpect(jsonPath("$[1].description").value("Curso de hibernate"))
                .andExpect(jsonPath("$[2].title").value("Spring"))
                .andExpect(jsonPath("$[2].description").value("Curso de spring"));
    }


    @Test
    void publishCourse_shouldReturnOkWhenSuccessful() throws Exception {
        Long courseId = 42L;

        User instructor = new User("Paulo", "paulo@alura.com.br", Role.INSTRUCTOR);
        CourseResponse mockResponse = new CourseResponse(
                courseId, "Java Avançado", "Curso de Java Avançado",
                instructor, Status.PUBLISHED,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(courseService.publishCourse(courseId)).thenReturn(mockResponse);

        mockMvc.perform(post("/course/{id}/publish", courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // 200 OK (ou 201 se você mudou)
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.title").value("Java Avançado"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").exists());

        verify(courseService, times(1)).publishCourse(courseId);
    }

    @Test
    void publishCourse_shouldReturnNotFoundWhenCourseNotFound() throws Exception {
        Long courseId = 999L;

        doThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Course not found"))
                .when(courseService).publishCourse(courseId);

        mockMvc.perform(post("/course/{id}/publish", courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void publishCourse_shouldReturnBadRequestWhenCourseNotBuilding() throws Exception {
        Long courseId = 42L;

        doThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Course is not in BUILDING status"))
                .when(courseService).publishCourse(courseId);

        mockMvc.perform(post("/course/{id}/publish", courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publishCourse_shouldReturnBadRequestWhenMissingTaskTypes() throws Exception {
        Long courseId = 42L;

        doThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Course must have at least one activity of each type (Open Text, Single Choice, Multiple Choice)"))
                .when(courseService).publishCourse(courseId);

        mockMvc.perform(post("/course/{id}/publish", courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publishCourse_shouldReturnBadRequestWhenTaskOrderNotContinuous() throws Exception {
        Long courseId = 42L;

        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Activity orders are not continuous (e.g., 1, 2, 3...)"))
                .when(courseService).publishCourse(courseId);

        mockMvc.perform(post("/course/{id}/publish", courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}