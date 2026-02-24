package com.drealmartins.tasklistbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OtpRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}

