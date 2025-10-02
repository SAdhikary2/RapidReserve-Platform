package com.rapidreserve.api_gateway.route;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;

@Configuration
public class InventoryServiceRoutes {

    @Bean
    public RouterFunction<ServerResponse> inventoryRoutes() {
        return GatewayRouterFunctions.route("inventory-service")
                //Get All Events
                .route(RequestPredicates.GET("/api/v1/inventory/events"),
                        request -> HandlerFunctions.http("http://localhost:8080/api/v1/inventory/events").handle(request))

                //Get Venue by ID
                .route(RequestPredicates.GET("/api/v1/inventory/venue/{venueId}"),
                        request -> forwardWithPathVariable(request, "venueId",
                                "http://localhost:8080/api/v1/inventory/venue/"))

                //Get Event by ID
                .route(RequestPredicates.GET("/api/v1/inventory/event/{eventId}"),
                        request -> forwardWithPathVariable(request, "eventId",
                                "http://localhost:8080/api/v1/inventory/event/"))

                //Update Event Capacity
                .route(RequestPredicates.PUT("/api/v1/inventory/event/{eventId}/capacity/{capacity}"),
                        request -> {
                            String eventId = request.pathVariable("eventId");
                            String capacity = request.pathVariable("capacity");
                            return HandlerFunctions
                                    .http("http://localhost:8080/api/v1/inventory/event/" + eventId + "/capacity/" + capacity)
                                    .handle(request);
                        })
                .build();
    }

    private static ServerResponse forwardWithPathVariable(ServerRequest request,
                                                          String pathVariable,
                                                          String baseUrl) throws Exception {
        String value = request.pathVariable(pathVariable);
        return HandlerFunctions.http(baseUrl + value).handle(request);
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceApiDocs() {
        return GatewayRouterFunctions.route("inventory-service-api-docs")
                .route(RequestPredicates.path("/docs/inventory-service/v3/api-docs"),
                        HandlerFunctions.http("http://localhost:8080"))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}
