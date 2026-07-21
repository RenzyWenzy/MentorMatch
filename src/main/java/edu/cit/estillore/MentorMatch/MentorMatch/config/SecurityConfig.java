package edu.cit.estillore.MentorMatch.MentorMatch.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import edu.cit.estillore.MentorMatch.MentorMatch.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Allows the React dev server (and any other configured origin) to call
     * this API from the browser. Update the allowed origins for production.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable) // not needed for a stateless, token-based API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/users/dashboard/student").hasRole("STUDENT")
                .requestMatchers("/api/users/dashboard/mentor").hasRole("MENTOR")
                .requestMatchers("/api/users/dashboard/admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                // FR-011: account management — id-scoped routes must be listed before the bare "/api/users" rule.
                .requestMatchers(HttpMethod.PUT, "/api/users/*/activate").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/*/deactivate").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("ADMIN")
                .requestMatchers("/api/users").hasRole("ADMIN")
                // FR-012: subject catalog. GET covers both the list and a single subject.
                .requestMatchers(HttpMethod.GET, "/api/subjects/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/subjects").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/subjects/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/subjects/**").hasRole("ADMIN")
                // FR-010: review listing needs to resolve before the generic tutor-profiles rule below.
                .requestMatchers(HttpMethod.GET, "/api/tutor-profiles/*/reviews").authenticated()
                .requestMatchers("/api/tutor-profiles/me").hasRole("MENTOR")
                // BR-002: profile-approval workflow, must also resolve before the generic rule below.
                .requestMatchers(HttpMethod.GET, "/api/tutor-profiles/pending").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tutor-profiles/*/approve").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tutor-profiles/*/reject").hasRole("ADMIN")
                .requestMatchers("/api/tutor-profiles/**").authenticated()
                .requestMatchers("/api/availability/me").hasRole("MENTOR")
                .requestMatchers("/api/availability/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/bookings/me").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/api/bookings/tutor/me").hasRole("MENTOR")
                // NOTE: fixed to match BookingController, which exposes /confirm (not /accept),
                // and added the previously-missing /complete matcher (FR-008).
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/confirm").hasRole("MENTOR")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/decline").hasRole("MENTOR")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/complete").hasRole("MENTOR")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/cancel").hasRole("STUDENT")
                // FR-009: only the student who booked the session can submit its review.
                .requestMatchers(HttpMethod.POST, "/api/bookings/*/review").hasRole("STUDENT")
                .requestMatchers("/api/bookings/**").authenticated()
                // FR-013: tutoring-activity reports.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}