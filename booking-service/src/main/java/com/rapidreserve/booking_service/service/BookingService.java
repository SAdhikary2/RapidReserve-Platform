package com.rapidreserve.booking_service.service;

import com.rapidreserve.booking_service.client.InventoryServiceClient;
import com.rapidreserve.booking_service.entity.Booking;
import com.rapidreserve.booking_service.entity.Customer;
import com.rapidreserve.booking_service.event.BookingEvent;
import com.rapidreserve.booking_service.repository.BookingRepository;
import com.rapidreserve.booking_service.repository.CustomerRepository;
import com.rapidreserve.booking_service.request.BookingRequest;
import com.rapidreserve.booking_service.response.BookingResponse;
import com.rapidreserve.booking_service.response.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BookingService {

    private final CustomerRepository customerRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;
    private final BookingRepository bookingRepository;

    public BookingService(final CustomerRepository customerRepository,
                          final InventoryServiceClient inventoryServiceClient,
                          final KafkaTemplate<String, BookingEvent> kafkaTemplate,
                          final BookingRepository bookingRepository) {
        this.customerRepository = customerRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.kafkaTemplate = kafkaTemplate;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Create a new booking
     */
    @Transactional
    public BookingResponse createBooking(final BookingRequest request) {
        log.info("Creating booking for user: {}, event: {}, tickets: {}",
                request.getUserId(), request.getEventId(), request.getTicketCount());

        // Validate user exists
        final Customer customer = customerRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // Get event inventory information
        final InventoryResponse inventoryResponse = inventoryServiceClient.getInventory(request.getEventId());
        log.info("Inventory Service Response: {}", inventoryResponse);

        // Validate inventory response
        validateInventoryResponse(inventoryResponse);

        // Validate available capacity
        if (inventoryResponse.getAvailableCapacity() < request.getTicketCount()) {
            throw new RuntimeException("Not enough tickets available. Requested: " +
                    request.getTicketCount() + ", Available: " + inventoryResponse.getAvailableCapacity());
        }

        // Create and save booking in database
        Booking booking = createAndSaveBooking(request, customer, inventoryResponse);

        // Create booking event for Kafka
        final BookingEvent bookingEvent = createBookingEvent(booking, inventoryResponse);

        // Send booking event to Kafka
        kafkaTemplate.send("booking", bookingEvent);
        log.info("Booking sent to Kafka: {}", bookingEvent);

        // Return booking response
        return mapToBookingResponse(booking);
    }

    /**
     * Get booking by ID
     */
    public BookingResponse getBookingById(Long id) {
        log.info("Fetching booking with ID: {}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        return mapToBookingResponse(booking);
    }

    /**
     * Get all bookings for a user
     */
    public List<BookingResponse> getUserBookings(Long userId) {
        log.info("Fetching bookings for user ID: {}", userId);

        // Verify user exists
        if (!customerRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(this::mapToBookingResponse)
                .toList();
    }

    /**
     * Update an existing booking
     */
    @Transactional
    public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
        log.info("Updating booking ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Only allow updates for pending bookings
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Cannot update booking with status: " + booking.getStatus());
        }

        // Validate user exists and matches booking user
        if (!booking.getUserId().equals(request.getUserId())) {
            throw new RuntimeException("User ID mismatch. Cannot update booking for different user");
        }

        Customer customer = customerRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If event is changed, validate new event
        if (!booking.getEventId().equals(request.getEventId())) {
            InventoryResponse inventory = inventoryServiceClient.getInventory(request.getEventId());
            validateInventoryResponse(inventory);

            if (inventory.getAvailableCapacity() < request.getTicketCount()) {
                throw new RuntimeException("Not enough tickets available in new event");
            }
        } else {
            // Same event, check if we have enough capacity for additional tickets
            if (request.getTicketCount() > booking.getTicketCount()) {
                long additionalTickets = request.getTicketCount() - booking.getTicketCount();
                InventoryResponse inventory = inventoryServiceClient.getInventory(request.getEventId());
                validateInventoryResponse(inventory);

                if (inventory.getAvailableCapacity() < additionalTickets) {
                    throw new RuntimeException("Not enough tickets available for additional booking");
                }
            }
        }

        // Update booking details
        booking.setEventId(request.getEventId());
        booking.setTicketCount(request.getTicketCount());

        // Recalculate price if event or ticket count changed
        if (!booking.getEventId().equals(request.getEventId()) ||
                !booking.getTicketCount().equals(request.getTicketCount())) {
            InventoryResponse inventory = inventoryServiceClient.getInventory(request.getEventId());
            booking.setTotalPrice(inventory.getTicketPrice()
                    .multiply(BigDecimal.valueOf(request.getTicketCount())));
        }

        booking.setUpdatedAt(LocalDateTime.now());
        Booking updatedBooking = bookingRepository.save(booking);

        // Send update event to Kafka
        BookingEvent updateEvent = createBookingEvent(updatedBooking, null);
        kafkaTemplate.send("booking-update", updateEvent);

        log.info("Booking updated successfully: {}", updatedBooking.getId());
        return mapToBookingResponse(updatedBooking);
    }

    /**
     * Cancel a booking
     */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        log.info("Cancelling booking ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Check if already cancelled
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        // Only allow cancellation for pending or confirmed bookings
        if (booking.getStatus() != Booking.BookingStatus.PENDING &&
                booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Cannot cancel booking with status: " + booking.getStatus());
        }

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking cancelledBooking = bookingRepository.save(booking);

        // Send cancellation event to Kafka
        BookingEvent cancellationEvent = BookingEvent.builder()
                .userId(cancelledBooking.getUserId())
                .eventId(cancelledBooking.getEventId())
                .ticketCount(cancelledBooking.getTicketCount())
                .totalPrice(cancelledBooking.getTotalPrice())
                .status(Booking.BookingStatus.CANCELLED.toString())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaTemplate.send("booking-cancellation", cancellationEvent);

        log.info("Booking cancelled successfully: {}", bookingId);
        return mapToBookingResponse(cancelledBooking);
    }

    /**
     * Confirm a pending booking
     */
    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        log.info("Confirming booking ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Only pending bookings can be confirmed
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be confirmed. Current status: " + booking.getStatus());
        }

        // Double-check inventory before confirming
        InventoryResponse inventory = inventoryServiceClient.getInventory(booking.getEventId());
        validateInventoryResponse(inventory);

        if (inventory.getAvailableCapacity() < booking.getTicketCount()) {
            throw new RuntimeException("Cannot confirm booking. Not enough tickets available");
        }

        // Update booking status
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());
        Booking confirmedBooking = bookingRepository.save(booking);

        // Send confirmation event to Kafka
        BookingEvent confirmationEvent = BookingEvent.builder()
                .userId(confirmedBooking.getUserId())
                .eventId(confirmedBooking.getEventId())
                .ticketCount(confirmedBooking.getTicketCount())
                .totalPrice(confirmedBooking.getTotalPrice())
                .status(Booking.BookingStatus.CONFIRMED.toString())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaTemplate.send("booking-confirmation", confirmationEvent);

        log.info("Booking confirmed successfully: {}", bookingId);
        return mapToBookingResponse(confirmedBooking);
    }

    // =============================================
    // PRIVATE HELPER METHODS
    // =============================================

    /**
     * Validate inventory response
     */
    private void validateInventoryResponse(InventoryResponse inventoryResponse) {
        if (inventoryResponse == null) {
            throw new RuntimeException("Inventory service returned null response");
        }
        if (inventoryResponse.getAvailableCapacity() == null) {
            throw new RuntimeException("Event capacity information is not available");
        }
        if (inventoryResponse.getTicketPrice() == null) {
            throw new RuntimeException("Ticket price information is not available");
        }
        if (inventoryResponse.getEventId() == null) {
            throw new RuntimeException("Event ID is not available");
        }
    }

    /**
     * Create and save booking in database
     */
    private Booking createAndSaveBooking(BookingRequest request, Customer customer, InventoryResponse inventoryResponse) {
        Booking booking = Booking.builder()
                .userId(customer.getId())
                .eventId(request.getEventId())
                .ticketCount(request.getTicketCount())
                .totalPrice(inventoryResponse.getTicketPrice()
                        .multiply(BigDecimal.valueOf(request.getTicketCount())))
                .status(Booking.BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created in database with ID: {}", savedBooking.getId());
        return savedBooking;
    }

    /**
     * Create booking event for Kafka
     */
    private BookingEvent createBookingEvent(Booking booking, InventoryResponse inventoryResponse) {
        return BookingEvent.builder()
                .userId(booking.getUserId())
                .eventId(booking.getEventId())
                .ticketCount(booking.getTicketCount())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus().toString())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Map Booking entity to BookingResponse DTO
     */
    private BookingResponse mapToBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .eventId(booking.getEventId())
                .ticketCount(booking.getTicketCount())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus().toString())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    /**
     * Old method kept for backward compatibility
     */
    private BookingEvent createBookingEvent(final BookingRequest request,
                                            final Customer customer,
                                            final InventoryResponse inventoryResponse) {
        return createBookingEvent(
                Booking.builder()
                        .userId(customer.getId())
                        .eventId(request.getEventId())
                        .ticketCount(request.getTicketCount())
                        .totalPrice(inventoryResponse.getTicketPrice()
                                .multiply(BigDecimal.valueOf(request.getTicketCount())))
                        .status(Booking.BookingStatus.PENDING)
                        .build(),
                inventoryResponse
        );
    }
}