package com.staybook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * The bookable room category of a hotel. The brief needs one category per hotel,
 * enforced by a unique key on hotel_id; dropping that key is all it takes to
 * support several categories later.
 */
@Entity
@Table(name = "room_type")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false, unique = true)
    private Hotel hotel;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "bed_type", length = 60)
    private String bedType;

    @Column(name = "max_guests", nullable = false)
    private int maxGuests;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "total_rooms", nullable = false)
    private int totalRooms;

    protected RoomType() { }

    public RoomType(Hotel hotel, String name, String description, String bedType,
                    int maxGuests, BigDecimal pricePerNight, int totalRooms) {
        this.hotel = hotel;
        this.name = name;
        this.description = description;
        this.bedType = bedType;
        this.maxGuests = maxGuests;
        this.pricePerNight = pricePerNight;
        this.totalRooms = totalRooms;
    }

    public Long getId() { return id; }
    public Hotel getHotel() { return hotel; }
    public Long getHotelId() { return hotel.getId(); }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBedType() { return bedType; }
    public int getMaxGuests() { return maxGuests; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public int getTotalRooms() { return totalRooms; }
}
