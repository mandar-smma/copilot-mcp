package com.copmvc.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void clearTasks() {
        taskRepository.deleteAll();
    }

    @Test
    void getTasksReturnsAllTasks() throws Exception {
        taskRepository.save(new Task("First", null, false));
        taskRepository.save(new Task("Second", "Details", true));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("First"))
                .andExpect(jsonPath("$[1].completed").value(true));
    }

    @Test
    void createReturnsLocationAndTaskCanBeRead() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("New task", "Description", false))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("New task"))
                .andReturn();

        TaskResponse created = objectMapper.readValue(result.getResponse().getContentAsString(), TaskResponse.class);
        mockMvc.perform(get("/api/tasks/{id}", created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id()))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void getMissingTaskReturnsProblemDetail() throws Exception {
        mockMvc.perform(get("/api/tasks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Task not found"));
    }

    @Test
    void putReplacesTaskAndDeleteRemovesIt() throws Exception {
        Task saved = taskRepository.save(new Task("Old title", "Old description", false));

        mockMvc.perform(put("/api/tasks/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("Updated", null, true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.description").doesNotExist())
                .andExpect(jsonPath("$.completed").value(true));

        mockMvc.perform(delete("/api/tasks/{id}", saved.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/tasks/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingTaskForPutAndDeleteReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/tasks/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("Title", null, false))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        mockMvc.perform(delete("/api/tasks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void invalidTaskRequestReturnsValidationProblemDetail() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","description":"Optional","completed":false}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }
}
