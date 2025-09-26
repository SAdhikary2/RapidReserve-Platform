package com.rapidreserve.booking_service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {
    private Long bookingId;
    private Long userId;
    private Long eventId;
    private Long ticketCount;
    private String ticketPrice;
}
