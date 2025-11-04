package com.jugantar.RiskMod.controller;

import com.jugantar.RiskMod.dto.AuthRequest;
import com.jugantar.RiskMod.service.AuthService;
import com.jugantar.RiskMod.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Base URL for all auth endpoints
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles POST requests to /api/auth/register
     * @param authRequest A JSON object with "username" and "password".
     * @return A success or error message.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest authRequest) {
        try {
            // Call the service to perform the registration logic
            authService.registerUser(
                    authRequest.getUsername(),
                    authRequest.getPassword()
            );

            // Return a 201 CREATED status
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("User registered successfully!");

        } catch (RuntimeException e) {
            // Catches the "Username is already taken!" error
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    /**
     * Handles POST requests to /api/auth/login
     * @param authRequest A JSON object with "username" and "password".
     * @return A JSON object with the JWT, or an error message.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthRequest authRequest) {
        try {
            // Call the service to attempt login
            String token = authService.loginUser(
                    authRequest.getUsername(),
                    authRequest.getPassword()
            );

            // If successful, return 200 OK with the token in the response body
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (Exception e) {
            // Catches bad credentials (user not found, bad password)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Invalid username or password");
        }
    }
}