package com.rapidreserve.api_gateway.route;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class BookingServiceRoutes {

    @Bean
    public RouterFunction<ServerResponse> BookingRoutes(){
        return GatewayRouterFunctions.route("booking-service")
                .route(RequestPredicates.POST("/api/v1/booking"),
                        HandlerFunctions.http("http://localhost:8081/api/v1/booking")
                ).build();
    }
}
