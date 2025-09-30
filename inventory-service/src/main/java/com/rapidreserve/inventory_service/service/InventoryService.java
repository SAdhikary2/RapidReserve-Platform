package com.rapidreserve.inventory_service.service;

import com.rapidreserve.inventory_service.entity.Event;
import com.rapidreserve.inventory_service.entity.Venue;
import com.rapidreserve.inventory_service.repository.EventRepository;
import com.rapidreserve.inventory_service.repository.VenueRepository;
import com.rapidreserve.inventory_service.response.EventInventoryResponse;
import com.rapidreserve.inventory_service.response.VenueInventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public List<EventInventoryResponse> getAllEvents(){
        final List<Event> events = eventRepository.findAll();
        return events.stream().map(event -> EventInventoryResponse.builder()
                .eventId(event.getId())
                .event(event.getName())
                .capacity(event.getAvailableCapacity())
                .venue(event.getVenue())
                .ticketPrice(event.getTicketPrice())
                .build()).collect(Collectors.toList());
    }


    public VenueInventoryResponse getVenueInformation(final Long venueId) {
        final Venue venue = venueRepository.findById(venueId).orElse(null);
        if (venue == null) {
            return null;
        }
        return VenueInventoryResponse.builder()
                .venueId(venue.getId())
                .venueName(venue.getName())
                .totalCapacity(venue.getTotalCapacity())
                .build();
    }

    public EventInventoryResponse getEventInventory(final Long eventId){
        final Event event = eventRepository.findById(eventId).orElse(null);

        return EventInventoryResponse.builder()
                .event(event.getName())
                .capacity(event.getAvailableCapacity())
                .venue(event.getVenue())
                .ticketPrice(event.getTicketPrice())
                .eventId(event.getId())
                .build();
    }

    public void updateEventCapacity(final Long eventId, final Long ticketsBooked){
        final Event event = eventRepository.findById(eventId).orElse(null);
        event.setAvailableCapacity(event.getAvailableCapacity() - ticketsBooked);
        eventRepository.saveAndFlush(event);
        log.info("Updated event capacity for event is {} with tickets booked: {}", eventId, ticketsBooked);
    }



}
