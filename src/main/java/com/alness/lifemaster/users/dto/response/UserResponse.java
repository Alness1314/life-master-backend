package com.alness.lifemaster.users.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.alness.lifemaster.profiles.dto.response.ProfileResponse;

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
public class UserResponse {
    private UUID id;
    private String username;
    private String password;
    private String fullName;
    private String imageId;
    private List<ProfileResponse> profiles;
    private Boolean verified;
    private Boolean erased;   
    private LocalDateTime created;
    private LocalDateTime updated;
}
