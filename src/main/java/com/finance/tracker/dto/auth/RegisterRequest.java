package com.finance.tracker.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос для регистрации нового пользователя")
public class RegisterRequest {
    @NotBlank @Size(min = 2, max = 100)
    @Schema(example = "Иван Иванов")
    private String name;

    @Email @NotBlank
    @Schema(example = "user@example.com")
    private String email;

    @NotBlank @Size(min = 6, max = 100)
    @Schema(example = "password123")
    private String password;

    @NotBlank
    @Schema(example = "password123")
    private String confirmPassword;
}
