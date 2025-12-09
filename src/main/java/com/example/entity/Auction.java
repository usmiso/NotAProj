package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Integer auctionId;

    @Column(name = "source", length = 100) // SMD, Copart, Cars.co.za, etc.
    private String source;

    @Column(name = "make", length = 100)
    private String make;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "year")
    private Integer year;

    @Column(name = "mileage")
    private Integer mileage;

    @Column(name = "auction_price", precision = 12, scale = 2)
    private BigDecimal auctionPrice;

    @Column(name = "auction_date")
    private LocalDate auctionDate;

    @Column(name = "vehicle_url", length = 255)
    private String vehicleUrl;

    @Column(name = "scraped_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime scrapedAt;

    @PrePersist
    protected void onCreate() {
        scrapedAt = LocalDateTime.now();
    }
}
