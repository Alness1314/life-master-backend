package com.alness.lifemaster.exceptions;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.alness.lifemaster.exceptions.dto.ErrorResponse;
import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.utils.LoggerUtil;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedExceptionHandler(AccessDeniedException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .code(ApiCodes.API_CODE_403)
                .message("No tienes permisos para realizar esta operación.")
                .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage));
        errors.put("code", ApiCodes.API_CODE_400);

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundError(NoHandlerFoundException ex) {
        String endpoint = ex.getMessage().replace("No", "").trim();
        ErrorResponse error = ErrorResponse.builder().code(ApiCodes.API_CODE_404)
                .message("No se encontró el recurso solicitado, " + endpoint).build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> runtimeExceptionHandler(RuntimeException ex) {
        LoggerUtil.logError(ex);
        ErrorResponse error = ErrorResponse.builder()
                .code(ApiCodes.API_CODE_500)
                .message(Messages.INTERNAL_SERVER_ERROR)
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrityViolationExceptionHandler(DataIntegrityViolationException ex) {
        LoggerUtil.logError(ex);
        ErrorResponse error = ErrorResponse.builder()
                .code(ApiCodes.API_CODE_409)
                .message(Messages.DATA_CONFLICT)
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RestExceptionHandler.class)
    public ResponseEntity<ErrorResponse> globalExceptionHandler(RestExceptionHandler ex) {
        ErrorResponse error = ErrorResponse.builder().code(ex.getCode()).message(ex.getMessage()).build();
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> responseStatusExceptionHandler(ResponseStatusException ex) {
        String code = "QM-" + ex.getStatusCode().value();
        return new ResponseEntity<>(Map.of("code", code, "message", ex.getReason()), ex.getStatusCode());
    }
}
