package com.example.controller;

import com.example.entity.Auction;
import com.example.entity.Vehicle;
import com.example.entity.User;
import com.example.service.DocumentParsingService;
import com.example.service.VehicleManagementService;
import com.example.service.WebScrapingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UploadController {

    private final WebScrapingService webScrapingService;
    private final VehicleManagementService vehicleManagementService;
    private final DocumentParsingService documentParsingService;

    @GetMapping({ "/", "/upload" })
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(
            @RequestParam("email") String email,
            @RequestParam("fullName") String fullName,
            @RequestParam("auctionUrl") String auctionUrl,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            Model model) {
        try {
            User user = vehicleManagementService.ensureUser(email, fullName);

            Map<String, String> scraped = webScrapingService.scrapeVehicleListing(auctionUrl);
            scraped.put("source", webScrapingService.detectAuctionSource(auctionUrl));
            Auction auction = vehicleManagementService.saveAuctionSnapshot(scraped, auctionUrl);

            Vehicle vehicle = new Vehicle();
            vehicle.setUser(user);
            vehicle.setMake(scraped.get("make"));
            vehicle.setModel(scraped.get("model"));
            vehicle.setYear(parseInt(scraped.get("year")));
            vehicle.setMileage(parseInt(scraped.get("mileage")));
            vehicle = vehicleManagementService.upsertVehicle(user, vehicle);

            if (files != null) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        vehicleManagementService.saveReport(vehicle, file);
                    }
                }
            }

            model.addAttribute("vehicle", vehicle);
            model.addAttribute("auction", auction);
            model.addAttribute("scraped", scraped);
            return "review";

        } catch (IOException e) {
            log.error("Upload failed", e);
            model.addAttribute("error", "Upload failed: " + e.getMessage());
            return "upload";
        }
    }

    private Integer parseInt(String value) {
        try {
            return value == null ? null : Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
