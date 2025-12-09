package com.example.service;

import com.example.entity.Vehicle;
import com.example.entity.Valuation;
import com.example.service.ValuationEngineService.ValuationResult;
import com.example.service.ValuationEngineService.VehicleData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValuationWorkflowService {

    private final ValuationEngineService valuationEngineService;
    private final VehicleManagementService vehicleManagementService;

    /**
     * Run valuation, persist results, and return the valuation entity.
     */
    public Valuation runValuation(Vehicle vehicle, VehicleData data) {
        ValuationResult result = valuationEngineService.calculateValuation(data);
        return vehicleManagementService.saveValuation(
                vehicle,
                result.getMidEstimate(),
                result.getConfidenceScore(),
                result.getBreakdownJson());
    }
}
