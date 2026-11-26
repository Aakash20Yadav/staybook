package com.staybook.repository;

import com.staybook.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"hotel", "roomType"})
    Optional<Booking> findByReference(String reference);

    boolean existsByReference(String reference);
}
