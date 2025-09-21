package com.rapidreserve.inventory_service.controller;

import com.rapidreserve.inventory_service.response.ApiResponse;
import com.rapidreserve.inventory_service.response.EventInventoryResponse;
import com.rapidreserve.inventory_service.response.VenueInventoryResponse;
import com.rapidreserve.inventory_service.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
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
}