package com.alness.lifemaster.app.dto;

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
public class BodyDto {
    private String sub;
    private String userKey;
    private Long iat;
    private Long exp; 
}
