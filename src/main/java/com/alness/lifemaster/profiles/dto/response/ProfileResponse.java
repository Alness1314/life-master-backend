package com.alness.lifemaster.profiles.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

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
public class ProfileResponse {
    private UUID id;
    private String name;
    private Boolean erased;
    private LocalDateTime created;
    private LocalDateTime updated;
}
