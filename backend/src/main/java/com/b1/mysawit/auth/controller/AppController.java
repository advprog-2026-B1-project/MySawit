package com.b1.mysawit.auth.controller;

import com.b1.mysawit.auth.dto.AssignmentRequest;
import com.b1.mysawit.auth.dto.LoginRequest;
import com.b1.mysawit.auth.dto.RegisterRequest;
import com.b1.mysawit.auth.dto.UserResponse;
import com.b1.mysawit.auth.service.AuthService;
import com.b1.mysawit.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class AppController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AppController(AuthService authService,
                         UserService userService,
                         AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // --- AUTHENTICATION ---

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request,
                                        HttpServletRequest servletRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = servletRequest.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
            return ResponseEntity.ok("Login Successful");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(userService.getUserByEmail(principal.getName()));
    }

    // --- USER LISTING ---

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // --- ADMIN CRUD ---

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/admin/users")
    public ResponseEntity<UserResponse> createUserByAdmin(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/admin/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/admin/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id,
                                             @RequestParam Long currentAdminId) {
        userService.deleteUser(id, currentAdminId);
        return ResponseEntity.ok("User deleted");
    }

    // --- ASSIGNMENT ---

    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/admin/assign")
    public ResponseEntity<String> assignWorker(@RequestBody AssignmentRequest request) {
        userService.assignWorkerToMandor(request.workerId(), request.mandorId());
        return ResponseEntity.ok("Worker assigned successfully");
    }
}
