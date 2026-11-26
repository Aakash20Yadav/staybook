package com.staybook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/**
 * How many rooms of a room type are taken on one night. A row exists only for
 * nights that have (or had) at least one booking; a missing row means zero booked.
 * The night of {@code stayDate} is the night that starts on that date.
 */
@Entity
@Table(name = "room_inventory",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_room_date",
                columnNames = {"room_type_id", "stay_date"}))
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Column(name = "stay_date", nullable = false)
    private LocalDate stayDate;

    @Column(name = "booked_count", nullable = false)
    private int bookedCount;

    protected RoomInventory() { }

    public RoomInventory(Long roomTypeId, LocalDate stayDate) {
        this.roomTypeId = roomTypeId;
        this.stayDate = stayDate;
        this.bookedCount = 0;
    }

    public void reserveOne() { bookedCount++; }

    public void releaseOne() { bookedCount = Math.max(0, bookedCount - 1); }

    public Long getId() { return id; }
    public Long getRoomTypeId() { return roomTypeId; }
    public LocalDate getStayDate() { return stayDate; }
    public int getBookedCount() { return bookedCount; }
}
