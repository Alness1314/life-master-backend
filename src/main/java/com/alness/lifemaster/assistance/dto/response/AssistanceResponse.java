package com.alness.lifemaster.assistance.dto.response;

import java.time.LocalDate;
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
public class AssistanceResponse {
    private UUID id;
    private LocalDate workDate;
    private String timeEntry;
    private String departureTime;
    private Boolean onTime;
    private Boolean retard;
    private Boolean justifiedAbsence;
    private Boolean unjustifiedAbsence;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
