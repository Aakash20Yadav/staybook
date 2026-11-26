package com.staybook.repository;

import com.staybook.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /** Matches the search text against city, address or hotel name (pattern is pre-lowercased). */
    @Query("""
            select h from Hotel h
            where lower(h.city) like :pattern
               or lower(h.address) like :pattern
               or lower(h.name) like :pattern
            order by h.name
            """)
    List<Hotel> searchByLocation(@Param("pattern") String pattern);

    List<Hotel> findAllByOrderByNameAsc();

    @Query("select distinct h.city from Hotel h order by h.city")
    List<String> findDistinctCities();
}
