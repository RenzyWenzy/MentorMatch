package edu.cit.estillore.MentorMatch.MentorMatch.controller;

import edu.cit.estillore.MentorMatch.MentorMatch.dto.AuthResponse;
import edu.cit.estillore.MentorMatch.MentorMatch.dto.LoginRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.dto.RegistrationRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.dto.UserResponseRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.entities.User;
import edu.cit.estillore.MentorMatch.MentorMatch.security.JwtService;
import edu.cit.estillore.MentorMatch.MentorMatch.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints consumed by the React frontend.
 * Registration and login both return a JWT + user profile so the
 * frontend can immediately authenticate subsequent requests.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest request) {
        User user = userService.registerUser(request);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token, UserResponseRequest.fromEntity(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        User user = userService.findByEmail(request.getEmail());

        return ResponseEntity.ok(new AuthResponse(token, UserResponseRequest.fromEntity(user)));
    }
}
