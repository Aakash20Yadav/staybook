package com.staybook.repository;

import com.staybook.entity.RoomInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {

    /** Inventory rows for nights in [from, to). */
    @Query("""
            select i from RoomInventory i
            where i.roomTypeId = :roomTypeId
              and i.stayDate >= :from and i.stayDate < :to
            order by i.stayDate
            """)
    List<RoomInventory> findRange(@Param("roomTypeId") Long roomTypeId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to);

    /**
     * Same as {@link #findRange} but as a locking read (SELECT ... FOR UPDATE).
     * Used when changing counts: under MySQL's REPEATABLE READ a plain SELECT can
     * return an older snapshot, while a locking read always sees the latest
     * committed values, so no update is ever lost.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select i from RoomInventory i
            where i.roomTypeId = :roomTypeId
              and i.stayDate >= :from and i.stayDate < :to
            order by i.stayDate
            """)
    List<RoomInventory> findRangeForUpdate(@Param("roomTypeId") Long roomTypeId,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);

    /** Busiest night per room type within [from, to): rows of {roomTypeId, maxBooked}. */
    @Query("""
            select i.roomTypeId, max(i.bookedCount) from RoomInventory i
            where i.roomTypeId in :roomTypeIds
              and i.stayDate >= :from and i.stayDate < :to
            group by i.roomTypeId
            """)
    List<Object[]> findPeakBookedCounts(@Param("roomTypeIds") Collection<Long> roomTypeIds,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to);
}
