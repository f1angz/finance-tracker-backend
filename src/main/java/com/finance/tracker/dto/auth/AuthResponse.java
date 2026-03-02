package com.finance.tracker.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
@Schema(description = "Ответ с JWT-токеном и данными пользователя")
public class AuthResponse {
    private String token;
    private UserDto user;

    @Data @AllArgsConstructor
    public static class UserDto {
        private String id;
        private String name;
        private String email;
    }
}
