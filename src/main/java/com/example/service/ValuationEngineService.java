package com.example.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ValuationEngineService {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Calculate vehicle valuation based on rules-based approach
     */
    public ValuationResult calculateValuation(VehicleData vehicleData) {
        log.info("Calculating valuation for: {} {} ({})",
                vehicleData.getMake(), vehicleData.getModel(), vehicleData.getYear());

        // Step 1: Get base market price (simplified - in production, use external API)
        BigDecimal basePrice = getBaseMarketPrice(vehicleData);

        // Step 2: Apply adjustments
        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("basePrice", basePrice);

        BigDecimal adjustedPrice = basePrice;

        // Age/Depreciation adjustment
        BigDecimal ageAdjustment = calculateAgeDepreciation(vehicleData.getYear());
        adjustedPrice = adjustedPrice.multiply(ageAdjustment);
        breakdown.put("ageDepreciation", ageAdjustment);

        // Mileage adjustment
        BigDecimal mileageAdjustment = calculateMileageAdjustment(vehicleData.getMileage());
        adjustedPrice = adjustedPrice.multiply(mileageAdjustment);
        breakdown.put("mileageAdjustment", mileageAdjustment);

        // Accident history adjustment
        if (vehicleData.isHasAccidents()) {
            BigDecimal accidentAdjustment = new BigDecimal("0.85"); // 15% penalty
            adjustedPrice = adjustedPrice.multiply(accidentAdjustment);
            breakdown.put("accidentAdjustment", accidentAdjustment);
        }

        // Previous owners adjustment
        BigDecimal ownerAdjustment = calculateOwnerAdjustment(vehicleData.getNumberOfOwners());
        adjustedPrice = adjustedPrice.multiply(ownerAdjustment);
        breakdown.put("ownerAdjustment", ownerAdjustment);

        // Roadworthy status
        if (!vehicleData.isRoadworthyPass()) {
            BigDecimal roadworthyAdjustment = new BigDecimal("0.90"); // 10% penalty
            adjustedPrice = adjustedPrice.multiply(roadworthyAdjustment);
            breakdown.put("roadworthyAdjustment", roadworthyAdjustment);
        }

        // Tire condition
        if (vehicleData.isTireWorn()) {
            BigDecimal tireAdjustment = new BigDecimal("0.95"); // 5% penalty
            adjustedPrice = adjustedPrice.multiply(tireAdjustment);
            breakdown.put("tireAdjustment", tireAdjustment);
        }

        // Suspension issues
        if (vehicleData.isSuspensionIssues()) {
            BigDecimal suspensionAdjustment = new BigDecimal("0.92"); // 8% penalty
            adjustedPrice = adjustedPrice.multiply(suspensionAdjustment);
            breakdown.put("suspensionAdjustment", suspensionAdjustment);
        }

        // Step 3: Calculate low, mid, high estimates with variance
        BigDecimal lowEst = adjustedPrice.multiply(new BigDecimal("0.90")); // 10% lower
        BigDecimal midEst = adjustedPrice;
        BigDecimal highEst = adjustedPrice.multiply(new BigDecimal("1.10")); // 10% higher

        // Calculate suggested buy price (slightly below mid estimate)
        BigDecimal suggestedPrice = adjustedPrice.multiply(new BigDecimal("0.95"));

        // Round to 2 decimal places
        lowEst = lowEst.setScale(2, RoundingMode.HALF_UP);
        midEst = midEst.setScale(2, RoundingMode.HALF_UP);
        highEst = highEst.setScale(2, RoundingMode.HALF_UP);
        suggestedPrice = suggestedPrice.setScale(2, RoundingMode.HALF_UP);

        breakdown.put("finalEstimates", Map.of(
                "lowEstimate", lowEst,
                "midEstimate", midEst,
                "highEstimate", highEst,
                "suggestedBuyPrice", suggestedPrice));

        // Calculate confidence score based on data completeness
        BigDecimal confidenceScore = calculateConfidenceScore(vehicleData, breakdown);

        ValuationResult result = new ValuationResult();
        result.setLowEstimate(lowEst);
        result.setMidEstimate(midEst);
        result.setHighEstimate(highEst);
        result.setSuggestedBuyPrice(suggestedPrice);
        result.setConfidenceScore(confidenceScore);
        result.setBreakdownJson(gson.toJson(breakdown));

        log.info("Valuation calculated - Low: {}, Mid: {}, High: {}", lowEst, midEst, highEst);

        return result;
    }

    /**
     * Get base market price for vehicle (simplified)
     * In production, integrate with real market data API
     */
    private BigDecimal getBaseMarketPrice(VehicleData vehicleData) {
        // Simplified pricing - in production use external API
        Map<String, Map<String, BigDecimal>> priceTable = new HashMap<>();

        // This is a simplified mock - replace with real market data
        Map<String, BigDecimal> toyotaModels = new HashMap<>();
        toyotaModels.put("Corolla", new BigDecimal("150000"));
        toyotaModels.put("Camry", new BigDecimal("200000"));
        toyotaModels.put("Hilux", new BigDecimal("250000"));

        Map<String, BigDecimal> hondaModels = new HashMap<>();
        hondaModels.put("Civic", new BigDecimal("140000"));
        hondaModels.put("Accord", new BigDecimal("190000"));

        priceTable.put("Toyota", toyotaModels);
        priceTable.put("Honda", hondaModels);

        // Fallback price if make/model not found
        BigDecimal basePrice = new BigDecimal("150000");

        if (priceTable.containsKey(vehicleData.getMake())) {
            Map<String, BigDecimal> modelPrices = priceTable.get(vehicleData.getMake());
            if (modelPrices.containsKey(vehicleData.getModel())) {
                basePrice = modelPrices.get(vehicleData.getModel());
            }
        }

        return basePrice;
    }

    /**
     * Calculate age and depreciation adjustment
     */
    private BigDecimal calculateAgeDepreciation(Integer year) {
        int currentYear = java.time.Year.now().getValue();
        int age = currentYear - year;

        if (age <= 0) {
            return new BigDecimal("1.0"); // New car
        } else if (age <= 3) {
            return new BigDecimal("0.90"); // 10% depreciation
        } else if (age <= 5) {
            return new BigDecimal("0.80"); // 20% depreciation
        } else if (age <= 10) {
            return new BigDecimal("0.65"); // 35% depreciation
        } else {
            return new BigDecimal("0.50"); // 50% depreciation
        }
    }

    /**
     * Calculate mileage adjustment
     */
    private BigDecimal calculateMileageAdjustment(Integer mileage) {
        if (mileage == null || mileage <= 0) {
            return new BigDecimal("1.0"); // Unknown mileage, no adjustment
        }

        if (mileage <= 50000) {
            return new BigDecimal("1.0"); // Low mileage, no penalty
        } else if (mileage <= 100000) {
            return new BigDecimal("0.95"); // 5% penalty
        } else if (mileage <= 150000) {
            return new BigDecimal("0.88"); // 12% penalty
        } else if (mileage <= 200000) {
            return new BigDecimal("0.80"); // 20% penalty
        } else {
            return new BigDecimal("0.70"); // 30% penalty for high mileage
        }
    }

    /**
     * Calculate adjustment based on number of owners
     */
    private BigDecimal calculateOwnerAdjustment(Integer numberOfOwners) {
        if (numberOfOwners == null || numberOfOwners <= 0) {
            return new BigDecimal("0.95"); // Unknown owners, slight penalty
        }

        if (numberOfOwners == 1) {
            return new BigDecimal("1.05"); // Single owner, slight premium
        } else if (numberOfOwners == 2) {
            return new BigDecimal("1.0");
        } else if (numberOfOwners == 3) {
            return new BigDecimal("0.97"); // 3% penalty
        } else if (numberOfOwners <= 5) {
            return new BigDecimal("0.93"); // 7% penalty
        } else {
            return new BigDecimal("0.85"); // 15% penalty for many owners
        }
    }

    /**
     * Calculate confidence score based on data completeness
     */
    private BigDecimal calculateConfidenceScore(VehicleData data, Map<String, Object> breakdown) {
        int completenessScore = 0;
        int totalChecks = 9;

        if (data.getMake() != null && !data.getMake().isEmpty())
            completenessScore++;
        if (data.getModel() != null && !data.getModel().isEmpty())
            completenessScore++;
        if (data.getYear() != null && data.getYear() > 0)
            completenessScore++;
        if (data.getMileage() != null && data.getMileage() > 0)
            completenessScore++;
        if (data.getVin() != null && !data.getVin().isEmpty())
            completenessScore++;
        if (data.getFuelType() != null && !data.getFuelType().isEmpty())
            completenessScore++;
        if (data.getTransmission() != null && !data.getTransmission().isEmpty())
            completenessScore++;
        if (data.isRoadworthyPass())
            completenessScore++;
        if (!data.isHasAccidents())
            completenessScore++;

        // Calculate percentage and convert to 0-100 scale
        BigDecimal score = new BigDecimal(completenessScore)
                .divide(new BigDecimal(totalChecks), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return score;
    }

    /**
     * Data class for vehicle information
     */
    public static class VehicleData {
        private String make;
        private String model;
        private Integer year;
        private String vin;
        private Integer mileage;
        private String fuelType;
        private String transmission;
        private boolean hasAccidents;
        private Integer numberOfOwners;
        private boolean roadworthyPass;
        private boolean tireWorn;
        private boolean suspensionIssues;

        // Getters and Setters
        public String getMake() {
            return make;
        }

        public void setMake(String make) {
            this.make = make;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public String getVin() {
            return vin;
        }

        public void setVin(String vin) {
            this.vin = vin;
        }

        public Integer getMileage() {
            return mileage;
        }

        public void setMileage(Integer mileage) {
            this.mileage = mileage;
        }

        public String getFuelType() {
            return fuelType;
        }

        public void setFuelType(String fuelType) {
            this.fuelType = fuelType;
        }

        public String getTransmission() {
            return transmission;
        }

        public void setTransmission(String transmission) {
            this.transmission = transmission;
        }

        public boolean isHasAccidents() {
            return hasAccidents;
        }

        public void setHasAccidents(boolean hasAccidents) {
            this.hasAccidents = hasAccidents;
        }

        public Integer getNumberOfOwners() {
            return numberOfOwners;
        }

        public void setNumberOfOwners(Integer numberOfOwners) {
            this.numberOfOwners = numberOfOwners;
        }

        public boolean isRoadworthyPass() {
            return roadworthyPass;
        }

        public void setRoadworthyPass(boolean roadworthyPass) {
            this.roadworthyPass = roadworthyPass;
        }

        public boolean isTireWorn() {
            return tireWorn;
        }

        public void setTireWorn(boolean tireWorn) {
            this.tireWorn = tireWorn;
        }

        public boolean isSuspensionIssues() {
            return suspensionIssues;
        }

        public void setSuspensionIssues(boolean suspensionIssues) {
            this.suspensionIssues = suspensionIssues;
        }
    }

    /**
     * Result class for valuation output
     */
    public static class ValuationResult {
        private BigDecimal lowEstimate;
        private BigDecimal midEstimate;
        private BigDecimal highEstimate;
        private BigDecimal suggestedBuyPrice;
        private BigDecimal confidenceScore;
        private String breakdownJson;

        // Getters and Setters
        public BigDecimal getLowEstimate() {
            return lowEstimate;
        }

        public void setLowEstimate(BigDecimal lowEstimate) {
            this.lowEstimate = lowEstimate;
        }

        public BigDecimal getMidEstimate() {
            return midEstimate;
        }

        public void setMidEstimate(BigDecimal midEstimate) {
            this.midEstimate = midEstimate;
        }

        public BigDecimal getHighEstimate() {
            return highEstimate;
        }

        public void setHighEstimate(BigDecimal highEstimate) {
            this.highEstimate = highEstimate;
        }

        public BigDecimal getSuggestedBuyPrice() {
            return suggestedBuyPrice;
        }

        public void setSuggestedBuyPrice(BigDecimal suggestedBuyPrice) {
            this.suggestedBuyPrice = suggestedBuyPrice;
        }

        public BigDecimal getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(BigDecimal confidenceScore) {
            this.confidenceScore = confidenceScore;
        }

        public String getBreakdownJson() {
            return breakdownJson;
        }

        public void setBreakdownJson(String breakdownJson) {
            this.breakdownJson = breakdownJson;
        }
    }
}
