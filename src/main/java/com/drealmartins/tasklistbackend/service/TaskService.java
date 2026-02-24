package com.drealmartins.tasklistbackend.service;


import com.drealmartins.tasklistbackend.dto.TaskRequest;
import com.drealmartins.tasklistbackend.dto.TaskResponse;
import com.drealmartins.tasklistbackend.entity.Task;
import com.drealmartins.tasklistbackend.entity.TaskStatus;
import com.drealmartins.tasklistbackend.entity.User;
import com.drealmartins.tasklistbackend.exception.ResourceNotFoundException;
import com.drealmartins.tasklistbackend.repository.TaskRepository;
import com.drealmartins.tasklistbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public TaskResponse createTask(TaskRequest request) {
        User user = getCurrentUser();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }

    public List<TaskResponse> getAllTasks() {
        User user = getCurrentUser();
        return taskRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        User user = getCurrentUser();
        return taskRepository.findByUserIdAndStatus(user.getId(), status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long id) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return mapToResponse(task);
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    public void deleteTask(Long id) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        taskRepository.delete(task);
    }
}

