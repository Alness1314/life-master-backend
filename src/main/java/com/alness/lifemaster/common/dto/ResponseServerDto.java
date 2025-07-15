package com.alness.lifemaster.common.dto;

import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResponseServerDto {
    private String message;
    private HttpStatus code;
    private Boolean status;
    private Map<String, Object> data;

    public ResponseServerDto(String message, HttpStatus code, Boolean status) {
        this.message = message;
        this.code = code;
        this.status = status;
    }
}
