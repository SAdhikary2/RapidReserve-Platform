package com.rapidreserve.inventory_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateEventRequest {
    @Size(max = 100, message = "Event name cannot exceed 100 characters")
    private String event;

    @DecimalMin(value = "0.0", inclusive = false, message = "Ticket price must be greater than 0")
    private BigDecimal ticketPrice;
}