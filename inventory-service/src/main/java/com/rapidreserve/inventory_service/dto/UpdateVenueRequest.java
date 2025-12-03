package com.rapidreserve.inventory_service.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateVenueRequest{
    private String name;
    private String address;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Long totalCapacity;
}

