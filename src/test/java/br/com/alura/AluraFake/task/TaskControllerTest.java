package br.com.alura.AluraFake.task;

import br.com.alura.AluraFake.course.CourseRepository;
import br.com.alura.AluraFake.task.dto.OpenTextTaskRequest;
import br.com.alura.AluraFake.task.dto.OptionRequest;
import br.com.alura.AluraFake.task.dto.SingleChoiceTaskRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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



    @Test
    void newSingleChoice_shouldReturnCreatedWhenValidRequest() throws Exception {
        SingleChoiceTaskRequest validRequest = new SingleChoiceTaskRequest();
        validRequest.setCourseId(1L);
        validRequest.setStatement("Qual linguagem é usada no Spring Boot?");
        validRequest.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Java", true),
                new OptionRequest("Python", false),
                new OptionRequest("Ruby", false)
        );
        validRequest.setOptions(options);

        doNothing().when(taskService).createSingleChoiceTask(any(SingleChoiceTaskRequest.class));

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void newSingleChoice_shouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        SingleChoiceTaskRequest invalidRequest = new SingleChoiceTaskRequest();
        invalidRequest.setCourseId(999L);
        invalidRequest.setStatement("Pergunta inválida");
        invalidRequest.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Java", true),
                new OptionRequest("Python", false)
        );
        invalidRequest.setOptions(options);

        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Curso não encontrado"))
                .when(taskService).createSingleChoiceTask(any(SingleChoiceTaskRequest.class));

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void newSingleChoice_shouldReturnBadRequestWhenDtoValidationFails() throws Exception {
        SingleChoiceTaskRequest invalidRequest = new SingleChoiceTaskRequest();
        invalidRequest.setCourseId(1L);
        invalidRequest.setStatement("");
        invalidRequest.setOrder(0);
        invalidRequest.setOptions(null);

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void newSingleChoice_shouldReturnBadRequestWhenOptionsHaveLessThan2Items() throws Exception {
        SingleChoiceTaskRequest invalidRequest = new SingleChoiceTaskRequest();
        invalidRequest.setCourseId(1L);
        invalidRequest.setStatement("Pergunta com poucas opções");
        invalidRequest.setOrder(1);

        List<OptionRequest> options = Arrays.asList(
                new OptionRequest("Única opção", true)
        );
        invalidRequest.setOptions(options);

        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "A atividade deve ter no mínimo 2 e no máximo 5 alternativas"))
                .when(taskService).createSingleChoiceTask(any(SingleChoiceTaskRequest.class));

        mockMvc.perform(post("/task/new/singlechoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
