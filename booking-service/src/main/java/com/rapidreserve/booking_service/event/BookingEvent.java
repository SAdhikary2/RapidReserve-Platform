package com.rapidreserve.booking_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEvent {
    private Long bookingId;
    private Long userId;
    private Long eventId;
    private Long ticketCount;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime timestamp;
}
