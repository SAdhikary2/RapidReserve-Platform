package com.rapidreserve.inventory_service.response;

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

    // Helper method to create success responses
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

    // You can add more helper methods for errors if needed
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
}