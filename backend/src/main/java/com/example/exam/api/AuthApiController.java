package com.example.exam.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.exam.config.jwt.JwtTokenProvider;
import com.example.exam.dto.ApiResponse;
import com.example.exam.dto.LoginRequest;
import com.example.exam.dto.LoginResponse;
import com.example.exam.dto.RegisterRequest;
import com.example.exam.dto.UserDTO;
import com.example.exam.mapper.UserMapper;
import com.example.exam.model.User;
import com.example.exam.repository.UserRepository;
import com.example.exam.service.AuthService;
import com.example.exam.util.SecurityUtils;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private static final Logger logger = LoggerFactory.getLogger(AuthApiController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final com.example.exam.service.UserService userService;
    private final AuthService authService;

    public AuthApiController(AuthenticationManager authenticationManager,
                              JwtTokenProvider jwtTokenProvider,
                              UserRepository userRepository,
                              com.example.exam.service.UserService userService,
                              AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest request) {
        try {
            if (request == null
                    || request.getEmail() == null
                    || request.getEmail().isBlank()
                    || request.getPassword() == null
                    || request.getPassword().isBlank()
                    || request.getConfirmPassword() == null
                    || request.getConfirmPassword().isBlank()
                    || request.getFirstName() == null
                    || request.getFirstName().isBlank()
                    || request.getLastName() == null
                    || request.getLastName().isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid registration data"));
            }

            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Passwords do not match"));
            }

            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid email format"));
            }

            if (request.getMobileNumber() != null && !request.getMobileNumber().isBlank()) {
                if (!request.getMobileNumber().matches("^\\d{10}$")) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Mobile number must be exactly 10 digits"));
                }
            }

            if (request.getPassword().length() < 8
                    || !request.getPassword().matches(".*[A-Z].*")
                    || !request.getPassword().matches(".*[a-z].*")
                    || !request.getPassword().matches(".*\\d.*")
                    || !request.getPassword().matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\\\",.<>/?].*")) {
                return ResponseEntity.badRequest().body(ApiResponse.error(
                        "Password must be at least 8 characters and include uppercase, lowercase, number, and special character"));
            }

            if (userRepository.findByUsername(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Email is already registered"));
            }

            User user = authService.mapRegisterRequestToUser(request);
            userService.saveStudent(user);
            return ResponseEntity.ok(ApiResponse.success("Registration successful"));
        } catch (Exception e) {
            logger.error("Registration failed", e);
            return ResponseEntity.status(500).body(ApiResponse.error("Registration failed. Please try again later."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest == null
                || loginRequest.getUsername() == null
                || loginRequest.getUsername().isBlank()
                || loginRequest.getPassword() == null
                || loginRequest.getPassword().isBlank()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid username or password"));
        }

        logger.info("API login attempt for user: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_STUDENT");

            String token = jwtTokenProvider.generateToken(username, role);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            UserDTO userDTO = UserMapper.toDTO(user);

            LoginResponse loginResponse = new LoginResponse(token, userDTO);

            logger.info("API login successful for user: {}, role: {}", username, role);
            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));

        } catch (Exception e) {
            logger.warn("API login failed for user: {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid username or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(ApiResponse.success(UserMapper.toDTO(user))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("User not found")));
    }
}
