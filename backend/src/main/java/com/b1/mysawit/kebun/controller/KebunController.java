package com.b1.mysawit.kebun.controller;

import com.b1.mysawit.kebun.dto.AssignMandorRequest;
import com.b1.mysawit.kebun.dto.AssignSupirRequest;
import com.b1.mysawit.kebun.dto.KebunCreateRequest;
import com.b1.mysawit.kebun.dto.KebunResponse;
import com.b1.mysawit.kebun.dto.KebunUpdateRequest;
import com.b1.mysawit.kebun.dto.ReassignMandorRequest;
import com.b1.mysawit.kebun.dto.ReassignSupirRequest;
import com.b1.mysawit.kebun.service.KebunService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kebun")
@PreAuthorize("hasRole('ADMIN')")
public class KebunController {

    private final KebunService kebunService;

    public KebunController(KebunService kebunService) {
        this.kebunService = kebunService;
    }

    @PostMapping
    public ResponseEntity<KebunResponse> createKebun(@Valid @RequestBody KebunCreateRequest request) {
        KebunResponse response = kebunService.createKebun(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<KebunResponse>> getAllKebun(
            @RequestParam(required = false) String nama,
            @RequestParam(required = false) String kode) {
        return ResponseEntity.ok(kebunService.getAllKebun(nama, kode));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KebunResponse> getKebunById(@PathVariable Long id) {
        return ResponseEntity.ok(kebunService.getKebunById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KebunResponse> updateKebun(
            @PathVariable Long id,
            @Valid @RequestBody KebunUpdateRequest request) {
        return ResponseEntity.ok(kebunService.updateKebun(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKebun(@PathVariable Long id) {
        kebunService.deleteKebun(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign-mandor")
    public ResponseEntity<Void> assignMandor(@Valid @RequestBody AssignMandorRequest request) {
        kebunService.assignMandor(request.getMandorId(), request.getKebunId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign-supir")
    public ResponseEntity<Void> assignSupir(@Valid @RequestBody AssignSupirRequest request) {
        kebunService.assignSupir(request.getSupirId(), request.getKebunId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reassign-mandor")
    public ResponseEntity<Void> reassignMandor(@Valid @RequestBody ReassignMandorRequest request) {
        kebunService.reassignMandor(
                request.getMandorId(),
                request.getOldKebunId(),
                request.getNewKebunId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reassign-supir")
    public ResponseEntity<Void> reassignSupir(@Valid @RequestBody ReassignSupirRequest request) {
        kebunService.reassignSupir(
                request.getSupirId(),
                request.getOldKebunId(),
                request.getNewKebunId());
        return ResponseEntity.noContent().build();
    }
}
