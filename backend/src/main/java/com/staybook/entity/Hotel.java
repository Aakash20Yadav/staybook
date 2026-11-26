package com.staybook.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "hotel")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "star_rating", nullable = false)
    private int starRating;

    @Column(name = "cover_image_url", nullable = false, length = 500)
    private String coverImageUrl;

    @ElementCollection
    @CollectionTable(name = "hotel_amenity", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "amenity", nullable = false, length = 60)
    private Set<String> amenities = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "hotel_image", joinColumns = @JoinColumn(name = "hotel_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_url", nullable = false, length = 500)
    private List<String> images = new ArrayList<>();

    protected Hotel() { }

    public Hotel(String name, String city, String address, String description,
                 int starRating, String coverImageUrl) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.description = description;
        this.starRating = starRating;
        this.coverImageUrl = coverImageUrl;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public int getStarRating() { return starRating; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public Set<String> getAmenities() { return amenities; }
    public List<String> getImages() { return images; }
}
