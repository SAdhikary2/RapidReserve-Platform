package com.rapidreserve.inventory_service.controller;

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
    public @ResponseBody List<EventInventoryResponse> ineventoryGetAllEvents(){
        return inventoryService.getAllEvents();
    }

    @GetMapping("/inventory/venue/{venueId}")
    public @ResponseBody VenueInventoryResponse inevntoryByVenueId(@PathVariable("venueId") Long venueId){
        return inventoryService.getVenueInformation(venueId);
    }



}
