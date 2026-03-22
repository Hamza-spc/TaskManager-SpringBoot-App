package com.hamza.taskmanager.dto.common;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ApiErrorResponse {
    private int status;
    private String message;
    private Map<String, String> errors;
}
