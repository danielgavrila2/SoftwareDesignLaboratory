package com.sd.enterprise.mvcapp.controllers;

import com.sd.enterprise.mvcapp.services.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/diet")
@RequiredArgsConstructor
public class DietController {

    private final DietService dietService;

    @GetMapping("/plan")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getDietPlan(
            @RequestParam(defaultValue = "2000") int calories,
            @RequestParam(defaultValue = "maintenance") String goal
    ) {
        return ResponseEntity.ok(dietService.generateDietPlan(calories, goal));
    }

    @GetMapping("/nutrition")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getDietNutrition(@RequestParam String query) {
        return ResponseEntity.ok(dietService.getNutritionInfo(query));
    }
}
