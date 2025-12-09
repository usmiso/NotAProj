package com.example.controller;

import com.example.entity.Vehicle;
import com.example.entity.Valuation;
import com.example.repository.VehicleRepository;
import com.example.service.ValuationEngineService.VehicleData;
import com.example.service.ValuationWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ValuationController {

    private final VehicleRepository vehicleRepository;
    private final ValuationWorkflowService valuationWorkflowService;

    @GetMapping("/valuation/{vehicleId}")
    public String valuationForm(@PathVariable Integer vehicleId, Model model) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) {
            model.addAttribute("error", "Vehicle not found");
            return "upload";
        }
        model.addAttribute("vehicle", vehicleOpt.get());
        return "valuation";
    }

    @PostMapping("/valuation/{vehicleId}")
    public String runValuation(
            @PathVariable Integer vehicleId,
            @RequestParam(required = false) Integer mileage,
            @RequestParam(required = false) Integer owners,
            @RequestParam(defaultValue = "false") boolean accidents,
            @RequestParam(defaultValue = "true") boolean roadworthy,
            @RequestParam(defaultValue = "false") boolean tiresWorn,
            @RequestParam(defaultValue = "false") boolean suspension,
            Model model) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) {
            model.addAttribute("error", "Vehicle not found");
            return "upload";
        }
        Vehicle vehicle = vehicleOpt.get();

        VehicleData data = new VehicleData();
        data.setMake(vehicle.getMake());
        data.setModel(vehicle.getModel());
        data.setYear(vehicle.getYear());
        data.setVin(vehicle.getVin());
        data.setMileage(mileage != null ? mileage : vehicle.getMileage());
        data.setFuelType(vehicle.getFuelType());
        data.setTransmission(vehicle.getTransmission());
        data.setHasAccidents(accidents);
        data.setNumberOfOwners(owners);
        data.setRoadworthyPass(roadworthy);
        data.setTireWorn(tiresWorn);
        data.setSuspensionIssues(suspension);

        Valuation valuation = valuationWorkflowService.runValuation(vehicle, data);
        model.addAttribute("valuation", valuation);
        model.addAttribute("vehicle", vehicle);
        return "results";
    }
}
