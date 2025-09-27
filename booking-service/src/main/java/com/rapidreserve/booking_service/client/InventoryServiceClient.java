package com.rapidreserve.booking_service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidreserve.booking_service.response.InventoryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class InventoryServiceClient {

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public InventoryResponse getInventory(final Long eventId){
        final RestTemplate restTemplate = new RestTemplate();
        String url = inventoryServiceUrl + "/event/" + eventId;

        // Get the response as a Map
        Map responseBody = restTemplate.getForObject(url, Map.class);

        if (responseBody != null && responseBody.containsKey("data")) {
            // Convert the "data" map to InventoryResponse using ObjectMapper
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            return objectMapper.convertValue(data, InventoryResponse.class);
        } else {
            throw new RuntimeException("Invalid response from inventory service");
        }
    }
}