package com.b1.mysawit.auth.controller;

import com.b1.mysawit.auth.dto.*;
import com.b1.mysawit.auth.service.AuthService;
import com.b1.mysawit.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AppController {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;

    // --- AUTHENTICATION ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        boolean isValid = authService.login(request.email(), request.password());
        if (isValid) return ResponseEntity.ok("Login Successful");
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    // --- ADMIN CRUD ---
    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestParam Long currentAdminId) {
        userService.deleteUser(id, currentAdminId);
        return ResponseEntity.ok("User deleted");
    }

    // --- ASSIGNMENT ---
    @PostMapping("/admin/assign")
    public ResponseEntity<?> assignWorker(@RequestBody AssignmentRequest request) {
        userService.assignWorkerToMandor(request.workerId(), request.mandorId());
        return ResponseEntity.ok("Worker assigned successfully");
    }
}