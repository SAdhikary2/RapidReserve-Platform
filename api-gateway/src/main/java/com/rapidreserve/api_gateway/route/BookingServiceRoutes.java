package com.rapidreserve.api_gateway.route;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;

@Configuration
public class BookingServiceRoutes {

    @Bean
    public RouterFunction<ServerResponse> bookingRoutes() {
        return GatewayRouterFunctions.route("booking-service")
                // Create booking
                .route(RequestPredicates.POST("/api/v1/booking"),
                        HandlerFunctions.http("http://localhost:8081/api/v1/booking"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("bookingServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))

                // Get booking by ID
                .route(RequestPredicates.GET("/api/v1/booking/{id}"),
                        request -> forwardWithPathVariable(request, "id",
                                "http://localhost:8081/api/v1/booking/"))

                // Get user bookings
                .route(RequestPredicates.GET("/api/v1/booking/user/{userId}"),
                        request -> forwardWithPathVariable(request, "userId",
                                "http://localhost:8081/api/v1/booking/user/"))

                // Update booking
                .route(RequestPredicates.PUT("/api/v1/booking/{id}"),
                        request -> forwardWithPathVariable(request, "id",
                                "http://localhost:8081/api/v1/booking/"))

                // Delete booking (cancel)
                .route(RequestPredicates.DELETE("/api/v1/booking/{id}"),
                        request -> forwardWithPathVariable(request, "id",
                                "http://localhost:8081/api/v1/booking/"))

                // Confirm booking
                .route(RequestPredicates.POST("/api/v1/booking/{id}/confirm"),
                        request -> forwardWithPathVariable(request, "id",
                                "http://localhost:8081/api/v1/booking/confirm/"))
                .build();
    }

    private static ServerResponse forwardWithPathVariable(ServerRequest request,
                                                          String pathVariable,
                                                          String baseUrl) throws Exception {
        String value = request.pathVariable(pathVariable);
        return HandlerFunctions.http(baseUrl + value).handle(request);
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return GatewayRouterFunctions.route("fallbackRoute")
                .POST("/fallbackRoute",
                        request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Booking service is down"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> bookingServiceApiDocs() {
        return GatewayRouterFunctions.route("booking-service-api-docs")
                .route(RequestPredicates.path("/docs/booking-service/v3/api-docs"),
                        HandlerFunctions.http("http://localhost:8081"))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}