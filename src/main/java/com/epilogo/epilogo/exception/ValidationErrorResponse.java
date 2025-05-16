package com.epilogo.epilogo.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> fieldErrors;

    public ValidationErrorResponse(int status, String error, String message, Map<String, String> fieldErrors) {
        super(status, error, message);
        this.fieldErrors = fieldErrors;
    }
}