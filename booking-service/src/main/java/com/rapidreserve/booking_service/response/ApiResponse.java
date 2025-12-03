package com.rapidreserve.booking_service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private Meta meta;
    private T data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private int code;
        private boolean success;
        private String message;
    }

    // Success helper
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .meta(Meta.builder()
                        .code(200)
                        .success(true)
                        .message(message)
                        .build())
                .data(data)
                .build();
    }

    // Error helper with code
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .meta(Meta.builder()
                        .code(code)
                        .success(false)
                        .message(message)
                        .build())
                .data(null)
                .build();
    }

    // Overloaded error helper without code (defaults to 500)
    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }
}