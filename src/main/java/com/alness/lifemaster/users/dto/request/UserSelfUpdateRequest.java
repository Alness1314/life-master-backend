package com.alness.lifemaster.users.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSelfUpdateRequest {

    @Pattern(regexp = ".*\\S.*", message = "no debe estar vacío")
    @Size(max = 256)
    private String fullName;

    @Size(min = 12, max = 128)
    private String password;

    @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "debe ser un UUID válido")
    private String imageId;
}
