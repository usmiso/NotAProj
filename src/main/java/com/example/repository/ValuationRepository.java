package com.example.repository;

import com.example.entity.Valuation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValuationRepository extends JpaRepository<Valuation, Integer> {
    List<Valuation> findByVehicleVehicleId(Integer vehicleId);
}
