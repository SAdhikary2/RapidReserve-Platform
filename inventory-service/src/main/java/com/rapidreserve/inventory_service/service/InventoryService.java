package com.rapidreserve.inventory_service.service;

import com.rapidreserve.inventory_service.dto.CreateEventRequest;
import com.rapidreserve.inventory_service.dto.UpdateEventRequest;
import com.rapidreserve.inventory_service.entity.Event;
import com.rapidreserve.inventory_service.entity.Venue;
import com.rapidreserve.inventory_service.exception.EventNotFoundException;
import com.rapidreserve.inventory_service.exception.InsufficientCapacityException;
import com.rapidreserve.inventory_service.exception.VenueNotFoundException;
import com.rapidreserve.inventory_service.repository.EventRepository;
import com.rapidreserve.inventory_service.repository.VenueRepository;
import com.rapidreserve.inventory_service.response.EventInventoryResponse;
import com.rapidreserve.inventory_service.response.VenueInventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InventoryService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    @Autowired
    public InventoryService(final EventRepository eventRepository, final VenueRepository venueRepository ){
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    // Existing methods
    public List<EventInventoryResponse> getAllEvents(){
        final List<Event> events = eventRepository.findAll();
        return events.stream().map(this::mapToEventInventoryResponse).collect(Collectors.toList());
    }

    public VenueInventoryResponse getVenueInformation(final Long venueId) {
        final Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found with id: " + venueId));

        return VenueInventoryResponse.builder()
                .venueId(venue.getId())
                .venueName(venue.getName())
                .totalCapacity(venue.getTotalCapacity())
                .build();
    }

    public EventInventoryResponse getEventInventory(final Long eventId){
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        return mapToEventInventoryResponse(event);
    }

    @Transactional
    public EventInventoryResponse createEvent(CreateEventRequest request) {
        // Validate venue exists
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException("Venue not found with id: " + request.getVenueId()));

        // Validate venue capacity
        if (venue.getTotalCapacity() < request.getTotalCapacity()) {
            throw new InsufficientCapacityException(
                    "Event capacity (" + request.getTotalCapacity() + ") exceeds venue capacity (" + venue.getTotalCapacity() + ")");
        }

        Event event = new Event();
        event.setName(request.getEvent());
        event.setTotalCapacity(request.getTotalCapacity());
        event.setAvailableCapacity(request.getTotalCapacity());
        event.setVenue(venue);
        event.setTicketPrice(request.getTicketPrice());

        Event savedEvent = eventRepository.save(event);
        log.info("Created new event: {} with ID: {}", request.getEvent(), savedEvent.getId());

        return mapToEventInventoryResponse(savedEvent);
    }

    @Transactional
    public EventInventoryResponse updateEvent(Long eventId, UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        // Update only provided fields
        if (request.getEvent() != null) {
            event.setName(request.getEvent());
        }
        if (request.getTicketPrice() != null) {
            event.setTicketPrice(request.getTicketPrice());
        }

        Event updatedEvent = eventRepository.save(event);
        log.info("Updated event with ID: {}", eventId);

        return mapToEventInventoryResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        // Check if event has bookings (available capacity < total capacity)
        if (!event.getAvailableCapacity().equals(event.getTotalCapacity())) {
            throw new IllegalStateException("Cannot delete event with existing bookings");
        }

        eventRepository.delete(event);
        log.info("Deleted event with ID: {}", eventId);
    }

    @Transactional
    public void updateEventCapacity(final Long eventId, final Long ticketsBooked) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        // Validate available capacity
        if (event.getAvailableCapacity() < ticketsBooked) {
            throw new InsufficientCapacityException(
                    "Not enough tickets available. Requested: " + ticketsBooked + ", Available: " + event.getAvailableCapacity());
        }

        // Update capacity
        event.setAvailableCapacity(event.getAvailableCapacity() + ticketsBooked);
        eventRepository.save(event);

        log.info("Updated event capacity for event ID: {}. Tickets booked: {}, Remaining: {}",
                eventId, ticketsBooked, event.getAvailableCapacity());
    }

    // Helper method to map Entity to Response DTO
    private EventInventoryResponse mapToEventInventoryResponse(Event event) {
        VenueInventoryResponse venueResponse = VenueInventoryResponse.builder()
                .venueId(event.getVenue().getId())
                .venueName(event.getVenue().getName())
                .totalCapacity(event.getVenue().getTotalCapacity())
                .build();

        return EventInventoryResponse.builder()
                .eventId(event.getId())
                .event(event.getName())
                .totalCapacity(event.getTotalCapacity())
                .availableCapacity(event.getAvailableCapacity())
                .venue(venueResponse)
                .ticketPrice(event.getTicketPrice())
                .build();
    }
}