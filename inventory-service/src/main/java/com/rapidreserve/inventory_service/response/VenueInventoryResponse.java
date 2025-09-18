package com.rapidreserve.inventory_service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueInventoryResponse {
    private Long venueId;
    private String venueName;
    private Long totalCapacity;
}
