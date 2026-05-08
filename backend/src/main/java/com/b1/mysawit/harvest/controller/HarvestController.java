package com.b1.mysawit.harvest.controller;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.harvest.dto.HarvestRequest;
import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.harvest.dto.RejectRequest;
import com.b1.mysawit.harvest.service.HarvestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/harvest")
@RequiredArgsConstructor
public class HarvestController {

    private final HarvestService harvestService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HarvestResponse> submitHarvest(@ModelAttribute HarvestRequest request) {
        User currentUser = new User(); // Dummy
        currentUser.setId(2L);

        HarvestResponse response = harvestService.createHarvest(currentUser, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<HarvestResponse> approveHarvest(@PathVariable Long id) {
        return ResponseEntity.ok(harvestService.approveHarvest(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<HarvestResponse> rejectHarvest(@PathVariable Long id, @RequestBody RejectRequest request) {
        return ResponseEntity.ok(harvestService.rejectHarvest(id, request.getAlasan()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<HarvestResponse>> getMyHarvests() {
        User currentUser = new User(); // Dummy
        currentUser.setId(2L);

        List<HarvestResponse> history = harvestService.getMyHarvestHistory(currentUser);
        return ResponseEntity.ok(history);
    }
}