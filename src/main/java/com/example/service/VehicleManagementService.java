package com.example.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Auction;
import com.example.entity.Report;
import com.example.entity.Role;
import com.example.entity.SearchHistory;
import com.example.entity.User;
import com.example.entity.Valuation;
import com.example.entity.Vehicle;
import com.example.repository.AuctionRepository;
import com.example.repository.ReportRepository;
import com.example.repository.SearchHistoryRepository;
import com.example.repository.UserRepository;
import com.example.repository.ValuationRepository;
import com.example.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleManagementService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ReportRepository reportRepository;
    private final AuctionRepository auctionRepository;
    private final ValuationRepository valuationRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final DocumentParsingService documentParsingService;

    /** Save or fetch the user performing the operation (simplified stub). */
    public User ensureUser(String email, String fullName) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            Role defaultRole = new Role();
            defaultRole.setRoleId(2); // dealer by default
            User user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPasswordHash("placeholder");
            user.setRole(defaultRole);
            return userRepository.save(user);
        });
    }

    /** Create or update a vehicle record. */
    public Vehicle upsertVehicle(User user, Vehicle vehicle) {
        vehicle.setUser(user);
        return vehicleRepository.save(vehicle);
    }

    /** Persist scraped auction snapshot. */
    public Auction saveAuctionSnapshot(Map<String, String> scraped, String url) {
        Auction auction = new Auction();
        auction.setVehicleUrl(url);
        auction.setSource(scraped.getOrDefault("source", "Unknown"));
        auction.setMake(scraped.get("make"));
        auction.setModel(scraped.get("model"));
        auction.setYear(parseInt(scraped.get("year")));
        auction.setMileage(parseInt(scraped.get("mileage")));
        auction.setAuctionPrice(parseDecimal(scraped.get("price")));
        auction.setAuctionDate(LocalDate.now());
        return auctionRepository.save(auction);
    }

    /** Save uploaded report and extracted text. */
    public Report saveReport(Vehicle vehicle, MultipartFile file) throws IOException {
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .map(name -> name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "")
                .orElse("");

        String extractedText = "";
        if ("pdf".equalsIgnoreCase(ext)) {
            extractedText = documentParsingService.parsePdfFile(file);
        } else {
            documentParsingService.saveImageFile(file);
        }

        Report report = new Report();
        report.setVehicle(vehicle);
        report.setFilePath(file.getOriginalFilename());
        report.setFileType(ext);
        report.setExtractedText(extractedText);
        return reportRepository.save(report);
    }

    /** Save valuation and search history. */
    public Valuation saveValuation(Vehicle vehicle, BigDecimal midEstimate, BigDecimal confidence,

            String breakdownJson) {
        Valuation valuation = new Valuation();
        valuation.setVehicle(vehicle);
        valuation.setEstimatedValue(midEstimate);
        valuation.setConfidenceScore(confidence);
        valuation.setValuationBreakdown(breakdownJson);
        valuation = valuationRepository.save(valuation);

        SearchHistory history = new SearchHistory();
        history.setUser(vehicle.getUser());
        history.setVehicle(vehicle);
        searchHistoryRepository.save(history);

        return valuation;
    }

    public List<Report> getReports(Integer vehicleId) {
        return reportRepository.findByVehicleVehicleId(vehicleId);
    }

    public List<Valuation> getValuations(Integer vehicleId) {
        return valuationRepository.findByVehicleVehicleId(vehicleId);
    }

    private Integer parseInt(String value) {
        try {
            return value == null ? null : Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String value) {
        try {
            return value == null ? null : new BigDecimal(value.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
