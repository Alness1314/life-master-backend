package com.alness.lifemaster.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.alness.lifemaster.common.messages.Messages;
import com.alness.lifemaster.exceptions.dto.ErrorResponse;
import com.alness.lifemaster.utils.ApiCodes;

class CustomExceptionHandlerTests {

    private static final String DATABASE_DETAILS =
            "duplicate key value violates unique constraint \"users_username_key\" "
                    + "Detail: Key (username)=(private@example.com) already exists. "
                    + "SQL [insert into users ...]";

    private final CustomExceptionHandler handler = new CustomExceptionHandler();

    @Test
    void accessDeniedErrorsReturnForbiddenInsteadOfInternalServerError() {
        ResponseEntity<ErrorResponse> response =
                handler.accessDeniedExceptionHandler(new AccessDeniedException("Access Denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ApiCodes.API_CODE_403);
        assertThat(response.getBody().getMessage())
                .isEqualTo("No tienes permisos para realizar esta operación.")
                .doesNotContain("Access Denied");
    }

    @Test
    void springRoutesDataIntegrityErrorsToTheSafeConflictResponse() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new FailingController())
                .setControllerAdvice(handler)
                .build();

        mockMvc.perform(get("/test/data-integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ApiCodes.API_CODE_409))
                .andExpect(jsonPath("$.message").value(Messages.DATA_CONFLICT))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("users_username_key"))))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private@example.com"))));
    }

    @Test
    void dataIntegrityErrorsReturnConflictWithoutDatabaseDetails() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(DATABASE_DETAILS);

        ResponseEntity<ErrorResponse> response =
                handler.dataIntegrityViolationExceptionHandler(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ApiCodes.API_CODE_409);
        assertThat(response.getBody().getMessage()).isEqualTo(Messages.DATA_CONFLICT);
        assertThat(response.getBody().getMessage())
                .doesNotContain("users_username_key", "private@example.com", "insert into users");
    }

    @Test
    void unexpectedErrorsReturnGenericMessageWithoutInternalDetails() {
        RuntimeException exception = new RuntimeException(DATABASE_DETAILS);

        ResponseEntity<ErrorResponse> response = handler.runtimeExceptionHandler(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(ApiCodes.API_CODE_500);
        assertThat(response.getBody().getMessage()).isEqualTo(Messages.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage())
                .doesNotContain("users_username_key", "private@example.com", "insert into users");
    }

    @RestController
    private static class FailingController {
        @GetMapping("/test/data-integrity")
        void dataIntegrity() {
            throw new DataIntegrityViolationException(DATABASE_DETAILS);
        }
    }
}
