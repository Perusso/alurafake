package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TaskService taskService;
    @MockBean
    private CourseRepository courseRepository;

    @Test
    void newOpenTextExercise_shouldReturnCreatedWhenValidRequest() throws Exception {
        OpenTextTaskRequest validRequest = new OpenTextTaskRequest();
        validRequest.setCourseId(1L);
        validRequest.setStatement("Explique o conceito de herança.");
        validRequest.setOrder(1);

        // ACT & ASSERT
        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void newOpenTextExercise_shouldReturnBadRequestWhenInvalidRequest() throws Exception {
        OpenTextTaskRequest invalidRequest = new OpenTextTaskRequest();
        invalidRequest.setCourseId(-1L);
        invalidRequest.setStatement("");
        invalidRequest.setOrder(1);

        mockMvc.perform(post("/task/new/opentext")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
