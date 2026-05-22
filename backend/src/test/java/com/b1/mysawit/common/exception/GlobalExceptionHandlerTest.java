package com.b1.mysawit.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_returns404() {
        ResponseEntity<Map<String, Object>> res =
                handler.handleResourceNotFound(new ResourceNotFoundException("Kebun", "id", 99L));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).containsKey("message");
        assertThat(res.getBody().get("status")).isEqualTo(404);
    }

    @Test
    void handleDuplicateResource_returns409() {
        ResponseEntity<Map<String, Object>> res =
                handler.handleDuplicateResource(new DuplicateResourceException("Kebun", "kodeKebun", "KB-001"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(res.getBody().get("status")).isEqualTo(409);
    }

    @Test
    void handleBusinessRuleViolation_returns422() {
        ResponseEntity<Map<String, Object>> res =
                handler.handleBusinessRuleViolation(new BusinessRuleViolationException("rule violated"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(res.getBody().get("status")).isEqualTo(422);
        assertThat(res.getBody().get("message")).isEqualTo("rule violated");
    }

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<Map<String, Object>> res =
                handler.handleIllegalArgument(new IllegalArgumentException("bad input"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().get("status")).isEqualTo(400);
        assertThat(res.getBody().get("message")).isEqualTo("bad input");
    }

    @Test
    void handleValidation_returns400WithErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "kodeKebun", "tidak boleh kosong"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> res = handler.handleValidation(ex);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().get("message")).isEqualTo("Validation failed");
        assertThat(res.getBody()).containsKey("errors");
    }

    @Test
    void responseBody_containsTimestampAndError() {
        ResponseEntity<Map<String, Object>> res =
                handler.handleIllegalArgument(new IllegalArgumentException("test"));
        assertThat(res.getBody()).containsKey("timestamp");
        assertThat(res.getBody()).containsKey("error");
    }
}
