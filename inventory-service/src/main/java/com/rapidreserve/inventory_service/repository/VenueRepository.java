package com.rapidreserve.inventory_service.repository;

import com.rapidreserve.inventory_service.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
}
