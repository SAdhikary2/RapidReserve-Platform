package com.rapidreserve.inventory_service.controller;

import com.rapidreserve.inventory_service.dto.CreateEventRequest;
import com.rapidreserve.inventory_service.dto.CreateVenueRequest;
import com.rapidreserve.inventory_service.dto.UpdateEventRequest;
import com.rapidreserve.inventory_service.dto.UpdateVenueRequest;
import com.rapidreserve.inventory_service.response.ApiResponse;
import com.rapidreserve.inventory_service.response.EventInventoryResponse;
import com.rapidreserve.inventory_service.response.VenueInventoryResponse;
import com.rapidreserve.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class InventoryController {

    private InventoryService inventoryService;

    @Autowired
    public InventoryController(final InventoryService inventoryService){
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory/events")
    public @ResponseBody ApiResponse<List<EventInventoryResponse>> inventoryGetAllEvents(){
        List<EventInventoryResponse> events = inventoryService.getAllEvents();
        return ApiResponse.success(events, "Events retrieved successfully");
    }

    @GetMapping("/inventory/venue/{venueId}")
    public @ResponseBody ApiResponse<VenueInventoryResponse> inventoryByVenueId(@PathVariable("venueId") Long venueId){
        VenueInventoryResponse venue = inventoryService.getVenueInformation(venueId);
        return ApiResponse.success(venue, "Venue retrieved successfully");
    }

    @GetMapping("/inventory/event/{eventId}")
    public @ResponseBody ApiResponse<EventInventoryResponse> inventoryForEvent (@PathVariable("eventId") Long eventId){
        EventInventoryResponse event = inventoryService.getEventInventory(eventId);
        return ApiResponse.success(event, "Events retrieved successfully");
    }

    @PostMapping("/inventory/events/create")
    public @ResponseBody ApiResponse<EventInventoryResponse> createEvent(
            @RequestBody @Valid CreateEventRequest request) {
        EventInventoryResponse event = inventoryService.createEvent(request);
        return ApiResponse.success(event, "Event created successfully");
    }

    @PutMapping("/inventory/events/update/{eventId}")
    public @ResponseBody ApiResponse<EventInventoryResponse> updateEvent(
            @PathVariable("eventId") Long eventId,
            @RequestBody @Valid UpdateEventRequest request) {
        EventInventoryResponse event = inventoryService.updateEvent(eventId, request);
        return ApiResponse.success(event, "Event updated successfully");
    }

    @DeleteMapping("/inventory/events/delete/{eventId}")
    public @ResponseBody ApiResponse<Void> deleteEvent(@PathVariable("eventId") Long eventId) {
        inventoryService.deleteEvent(eventId);
        return ApiResponse.success(null, "Event deleted successfully");
    }

    @PutMapping("/inventory/event/{eventId}/capacity/{ticketsBooked}")
    public @ResponseBody ApiResponse<Void> updateEventCapacity(
            @PathVariable("eventId") Long eventId,
            @PathVariable("ticketsBooked") Long ticketsBooked) {
        inventoryService.updateEventCapacity(eventId, ticketsBooked);
        return ApiResponse.success(null, "Event capacity updated successfully");
    }

    //  Venue endpoints
    @GetMapping("/inventory/venues")
    public @ResponseBody ApiResponse<List<VenueInventoryResponse>> getAllVenues() {
        List<VenueInventoryResponse> venues = inventoryService.getAllVenues();
        return ApiResponse.success(venues, "Venues retrieved successfully");
    }

    @PostMapping("/inventory/venues/create")
    public @ResponseBody ApiResponse<VenueInventoryResponse> createVenue(
            @RequestBody @Valid CreateVenueRequest request) {
        VenueInventoryResponse venue = inventoryService.createVenue(request);
        return ApiResponse.success(venue, "Venue created successfully");
    }

    @PutMapping("/inventory/venues/{venueId}")
    public @ResponseBody ApiResponse<VenueInventoryResponse> updateVenue(
            @PathVariable("venueId") Long venueId,
            @RequestBody @Valid UpdateVenueRequest request) {
        VenueInventoryResponse venue = inventoryService.updateVenue(venueId, request);
        return ApiResponse.success(venue, "Venue updated successfully");
    }

    @DeleteMapping("/inventory/venues/{venueId}")
    public @ResponseBody ApiResponse<Void> deleteVenue(@PathVariable("venueId") Long venueId) {
        inventoryService.deleteVenue(venueId);
        return ApiResponse.success(null, "Venue deleted successfully");
    }


}