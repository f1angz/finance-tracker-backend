package com.finance.tracker.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос для входа в систему")
public class LoginRequest {
    @Email @NotBlank
    @Schema(example = "user@example.com")
    private String email;

    @NotBlank
    @Schema(example = "password123")
    private String password;

    @Schema(example = "false")
    private boolean rememberMe;
}
