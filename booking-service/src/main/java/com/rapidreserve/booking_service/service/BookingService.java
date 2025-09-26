package com.rapidreserve.booking_service.service;

import com.rapidreserve.booking_service.client.InventoryServiceClient;
import com.rapidreserve.booking_service.entity.Customer;
import com.rapidreserve.booking_service.event.BookingEvent;
import com.rapidreserve.booking_service.repository.CustomerRepository;
import com.rapidreserve.booking_service.request.BookingRequest;
import com.rapidreserve.booking_service.response.BookingResponse;
import com.rapidreserve.booking_service.response.InventoryResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BookingService {

    private final CustomerRepository customerRepository;
    private final InventoryServiceClient inventoryServiceClient;

    public BookingService(final CustomerRepository customerRepository,
                          final InventoryServiceClient inventoryServiceClient){
        this.customerRepository = customerRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    public BookingResponse createBooking(final BookingRequest request){

       //check if user exists
        final Customer customer = customerRepository.findById(request.getUserId()).orElse(null);
        if (customer == null){
            throw new RuntimeException("User not found");
        }

        //check if there is enough inventory
        final InventoryResponse inventoryResponse = inventoryServiceClient.getInventory(request.getEventId());
        System.out.println("Inventory Service Response" + inventoryResponse);
        if (inventoryResponse.getCapacity() < request.getTicketCount()){
            throw new RuntimeException("Not enough inventory");
        }

        //create booking
        final BookingEvent bookingEvent = createBookingEvent(request, customer,  inventoryResponse);

        //Send booking to order service on a kafka topic
        

        return BookingResponse.builder().build();
    }


    private BookingEvent createBookingEvent(final BookingRequest request,
                                            final Customer customer,
                                            final InventoryResponse inventoryResponse){
        return BookingEvent.builder()
                .userId(customer.getId())
                .eventId(request.getEventId())
                .ticketCount(request.getTicketCount())
                .totalPrice(inventoryResponse.getTicketPrice().multiply(BigDecimal.valueOf(request.getTicketCount())))
                .build();
    }


}
