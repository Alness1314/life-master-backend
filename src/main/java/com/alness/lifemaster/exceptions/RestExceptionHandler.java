package com.alness.lifemaster.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RestExceptionHandler extends RuntimeException{
    private final String code;
    private final HttpStatus status;

    public RestExceptionHandler(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
