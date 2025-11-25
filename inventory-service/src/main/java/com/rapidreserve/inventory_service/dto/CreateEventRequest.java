package com.rapidreserve.inventory_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateEventRequest {
    @NotBlank(message = "Event name is required")
    private String event;

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Long totalCapacity;

    @NotNull(message = "Venue ID is required")
    private Long venueId;

    @NotNull(message = "Ticket price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Ticket price must be greater than 0")
    private BigDecimal ticketPrice;
}