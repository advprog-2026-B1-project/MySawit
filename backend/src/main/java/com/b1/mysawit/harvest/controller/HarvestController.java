package com.b1.mysawit.harvest.controller;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.harvest.dto.HarvestRequest;
import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.harvest.service.HarvestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/harvest")
@RequiredArgsConstructor
public class HarvestController {

    private final HarvestService harvestService;

    @PostMapping
    public ResponseEntity<HarvestResponse> submitHarvest(@RequestBody HarvestRequest request) {
        User currentUser = new User(); // Dummy
        currentUser.setId(1L);

        HarvestResponse response = harvestService.createHarvest(currentUser, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<HarvestResponse>> getMyHarvests() {
        User currentUser = new User(); // Dummy
        currentUser.setId(1L);

        List<HarvestResponse> history = harvestService.getMyHarvestHistory(currentUser);
        return ResponseEntity.ok(history);
    }
}