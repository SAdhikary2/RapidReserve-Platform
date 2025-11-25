package com.rapidreserve.inventory_service.response;

import com.rapidreserve.inventory_service.entity.Venue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInventoryResponse {
    private Long eventId;
    private String event;
    private Long totalCapacity;
    private Long availableCapacity;
    private VenueInventoryResponse venue;
    private BigDecimal ticketPrice;
}
