package com.alness.lifemaster.modules.dto.response;

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
public class ModuleResponse {
    private UUID id;
    private String name;
    private String route;
    private String permissionKey;
    private String iconName;
    private Boolean erased;
    private String level;
    private String description;
    private Boolean isParent;
    private List<ModuleResponse> children;
    private List<ProfileResponse> profiles;
}
