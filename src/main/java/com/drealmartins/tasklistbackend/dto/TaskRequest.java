package com.drealmartins.tasklistbackend.dto;


import com.drealmartins.tasklistbackend.entity.TaskPriority;
import com.drealmartins.tasklistbackend.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDateTime dueDate;
}