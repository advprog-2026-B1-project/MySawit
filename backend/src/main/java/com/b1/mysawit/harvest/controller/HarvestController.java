package com.b1.mysawit.harvest.controller;

import com.b1.mysawit.domain.User;
import com.b1.mysawit.harvest.dto.HarvestRequest;
import com.b1.mysawit.harvest.dto.HarvestResponse;
import com.b1.mysawit.harvest.service.HarvestService;
import com.b1.mysawit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/harvest")
@RequiredArgsConstructor
public class HarvestController {

    private final HarvestService harvestService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof OAuth2User) {
            email = ((OAuth2User) principal).getAttribute("email");
        }
        else if (principal instanceof org.springframework.security.core.userdetails.User) {
            email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        }
        else if (principal instanceof String) {
            email = (String) principal; 
        }

        if (email == null) {
            throw new IllegalStateException("Could not extract email from authentication context");
        }

        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found in database"));
    }

    private boolean validateUserMandor(User user) {
        return user.getRole() == User.Role.Mandor;
    }

    private boolean validateUserBuruh(User user) {
        return user.getRole() == User.Role.Buruh;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HarvestResponse> submitHarvest(@ModelAttribute HarvestRequest request) {
        User currentUser = getCurrentUser();
        if (!validateUserBuruh(currentUser)) {
            throw new AccessDeniedException("Anda tidak memiliki akses untuk melaporkan hasil panen");
        }
        HarvestResponse response = harvestService.createHarvest(currentUser, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<HarvestResponse> approveHarvest(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (validateUserMandor(currentUser)) {
            return ResponseEntity.ok(harvestService.approveHarvest(id, currentUser));
        }
        throw new AccessDeniedException("Anda tidak memiliki akses untuk menyetujui hasil panen");
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<HarvestResponse> rejectHarvest(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        if (validateUserMandor(currentUser)) {
            return ResponseEntity.ok(harvestService.rejectHarvest(id, request.get("alasan"), currentUser));
        }
        throw new AccessDeniedException("Anda tidak memiliki akses untuk menolak hasil panen");
    }

    @GetMapping("/me")
    public ResponseEntity<List<HarvestResponse>> getMyHarvests(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status
    ) {
        User currentUser = getCurrentUser();
        if (!validateUserBuruh(currentUser)) {
            throw new AccessDeniedException("Anda tidak memiliki akses untuk melihat hasil panen pribadi");
        }
        List<HarvestResponse> history = harvestService.getMyHarvestHistory(currentUser, startDate, endDate, status);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/mandor")
    public ResponseEntity<List<HarvestResponse>> getHarvestsForMandor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String searchNama) {

        User currentUser = getCurrentUser();
        List<HarvestResponse> history = harvestService.getMandorHarvestHistory(currentUser, startDate, endDate, status, searchNama);
        return ResponseEntity.ok(history);
    }
}