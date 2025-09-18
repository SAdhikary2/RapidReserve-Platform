package com.rapidreserve.inventory_service.repository;

import com.rapidreserve.inventory_service.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
