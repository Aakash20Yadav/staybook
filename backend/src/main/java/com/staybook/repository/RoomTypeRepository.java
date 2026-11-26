package com.staybook.repository;

import com.staybook.entity.RoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    Optional<RoomType> findFirstByHotel_IdOrderByIdAsc(Long hotelId);

    List<RoomType> findByHotel_IdIn(Collection<Long> hotelIds);

    /**
     * SELECT ... FOR UPDATE on the room type row. Every booking and cancellation
     * for a room type takes this lock first, so changes to its inventory are
     * serialised and two guests can never both take the last room.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomType r where r.id = :id")
    Optional<RoomType> findByIdForUpdate(@Param("id") Long id);

    /** Same lock, looked up by hotel (one room type per hotel). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomType r where r.hotel.id = :hotelId")
    List<RoomType> findByHotelIdForUpdate(@Param("hotelId") Long hotelId);
}
