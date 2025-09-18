package com.rapidreserve.inventory_service.service;

import com.rapidreserve.inventory_service.entity.Event;
import com.rapidreserve.inventory_service.entity.Venue;
import com.rapidreserve.inventory_service.repository.EventRepository;
import com.rapidreserve.inventory_service.repository.VenueRepository;
import com.rapidreserve.inventory_service.response.EventInventoryResponse;
import com.rapidreserve.inventory_service.response.VenueInventoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    public InventoryService(final EventRepository eventRepository, final VenueRepository venueRepository ){
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    public List<EventInventoryResponse> getAllEvents(){
        final List<Event> events = eventRepository.findAll();
        return events.stream().map(event -> EventInventoryResponse.builder()
                .event(event.getName())
                .capacity(event.getLeftCapacity())
                .venue(event.getVenue())
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



}
