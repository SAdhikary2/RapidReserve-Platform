# RapidReserve-Platform Documentation

## Overview

**RapidReserve-Platform** is a scalable, event-driven ticket booking platform built using Spring Boot 3. It follows a microservices architecture with asynchronous communication via Apache Kafka, utilizes MySQL for persistent storage, and employs Flyway for database migrations. Authentication and authorization are managed using Keycloak. The system is designed for real-time event and ticket management, providing resilience and high performance.

## Architecture

The platform is composed of several microservices:

- **API Gateway**: Central entry point for all requests, routing them to appropriate services.
- **Inventory Service**: Manages events, venues, and ticket inventories.
- **Booking Service**: Handles customer bookings and orchestrates booking events.
- **Order Service**: Responsible for managing orders and processing payments.

Communication between services is handled asynchronously through Apache Kafka, enhancing scalability and resilience.

## Microservices Breakdown

### 1. API Gateway

- **Location**: `/api-gateway`
- **Purpose**: Serves as the single entry point for client requests, forwarding them to the respective microservice.
- **Technology**: Spring Cloud Gateway.

### 2. Inventory Service

- **Location**: `/inventory-service`
- **Purpose**: Manages the inventory of events and venues.
- **Entities**:
  - **Venue**: Stores venue details such as name, address, total capacity.

### 3. Booking Service

- **Location**: `/booking-service`
- **Purpose**: Handles customer bookings and publishes booking events to Kafka.
- **Entities**: 
  - **Customer**: Represents a booking customer.

### 4. Order Service

- **Location**: `/order-service`
- **Purpose**: Manages customer orders and payment processing.

## Technology Stack

- **Spring Boot 3**: Core framework for all microservices.
- **Apache Kafka**: Event-driven asynchronous communication.
- **MySQL**: Relational database for storing entities.
- **Flyway**: Database migration management.
- **Keycloak**: Authentication and authorization.
- **Spring Cloud Gateway**: API Gateway functionality.

## Features

- **Microservices Architecture**: Decoupled services for scalability.
- **Event-Driven**: Services communicate via Kafka for resilience.
- **Authentication/Authorization**: Secure access using Keycloak.
- **Database Migrations**: Managed using Flyway for consistency.
- **Real-Time Inventory Management**: Accurate event and ticket tracking.

## Getting Started

1. **Clone the Repository**
   ```sh
   git clone https://github.com/SAdhikary2/RapidReserve-Platform.git
   ```

2. **Set Up MySQL Database**
   - Create databases for each microservice (if needed).
   - Configure connection credentials in each service's `application.properties`.

3. **Run Keycloak for Authentication**
   - Download and start Keycloak server.
   - Configure realms, clients, and users as needed.

4. **Start Kafka Broker**
   - Download and run Apache Kafka.
   - Ensure the broker is running and accessible to microservices.

5. **Build and Start Services**
   - Use Maven or Gradle to build each service.
   - Start each microservice individually (API Gateway, Inventory Service, Booking Service, Order Service).

## Folder Structure

- `api-gateway/` : API Gateway microservice.
- `inventory-service/` : Inventory management microservice.
- `booking-service/` : Booking microservice.
- `order-service/` : Order processing microservice.

Each service contains its own source code, configuration, and resources.

## Example API Flows

1. **User Authentication**
   - Users authenticate via Keycloak before accessing APIs.

2. **Event Listing**
   - API Gateway routes requests to Inventory Service to list available events and venues.

3. **Booking a Ticket**
   - User submits booking request via API Gateway.
   - Booking Service validates customer, checks inventory, creates booking, and publishes a booking event.

4. **Order Processing**
   - Order Service listens for booking events, processes payment, and confirms order.
