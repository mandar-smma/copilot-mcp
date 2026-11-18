package com.copmvc.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void findAllReturnsTasksSortedById() {
        when(taskRepository.findAll(Sort.by(Sort.Direction.ASC, "id")))
                .thenReturn(List.of(new Task("First", null, false), new Task("Second", "Details", true)));

        List<TaskResponse> result = taskService.findAll();

        assertThat(result).extracting(TaskResponse::title).containsExactly("First", "Second");
        assertThat(result.get(1).description()).isEqualTo("Details");
        assertThat(result.get(1).completed()).isTrue();
    }

    @Test
    void createSavesAndReturnsTask() {
        TaskRequest request = new TaskRequest("Write tests", "API tests", false);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse result = taskService.create(request);

        assertThat(result.title()).isEqualTo("Write tests");
        assertThat(result.description()).isEqualTo("API tests");
        assertThat(result.completed()).isFalse();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void replaceUpdatesTaskFields() {
        Task task = new Task("Old title", "Old description", false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse result = taskService.replace(1L, new TaskRequest("New title", null, true));

        assertThat(result.title()).isEqualTo("New title");
        assertThat(result.description()).isNull();
        assertThat(result.completed()).isTrue();
        verify(taskRepository).save(task);
    }

    @Test
    void deleteRemovesTask() {
        Task task = new Task("Remove me", null, false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.delete(1L);

        verify(taskRepository).delete(task);
    }

    @Test
    void missingTasksRaiseNotFoundForReadUpdateAndDelete() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(TaskNotFoundException.class);
        assertThatThrownBy(() -> taskService.replace(99L, new TaskRequest("Title", null, false)))
                .isInstanceOf(TaskNotFoundException.class);
        assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
