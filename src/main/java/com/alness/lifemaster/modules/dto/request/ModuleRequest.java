package com.alness.lifemaster.modules.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
public class ModuleRequest {
    @NotBlank
    @Size(max = 128)
    private String name;
    @NotBlank
    @Size(max = 256)
    private String route;
    @Size(max = 64)
    private String permissionKey;
    @Size(max = 128)
    private String iconName;
    @NotBlank
    @Size(max = 64)
    private String level;
    @Size(max = 512)
    private String description;
    private Boolean isParent;
    @NotEmpty
    private List<String> profile;
}
