package com.alness.lifemaster.sender.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class EmailRequest {
    @NotEmpty(message = "Debe haber al menos un destinatario")
    private List<@Email(message = "Correo inválido") String> to;

    @NotBlank
    private String subject;

    private List<AttachmentsDto> attachments;

    @NotBlank
    private String templateName;

    @NotNull(message = "El mapa de variables no debe ser nulo, debe estar inicializado.")
    private Map<String, Object> variables;

    private Map<String, ImageDto> imagenes;
}
