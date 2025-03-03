package com.alness.lifemaster.app.dto;

import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseServer {
    private String message;
    private Map<String, Object> data;
    private Boolean status;
    private HttpStatus code;
}
