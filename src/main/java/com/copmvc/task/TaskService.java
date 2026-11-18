package com.copmvc.task;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        return taskRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(TaskService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return toResponse(findTask(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task(request.title(), request.description(), request.completed());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse replace(Long id, TaskRequest request) {
        Task task = findTask(id);
        task.replace(request.title(), request.description(), request.completed());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long id) {
        taskRepository.delete(findTask(id));
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    private static TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.isCompleted());
    }
}
