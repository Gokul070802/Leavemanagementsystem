package org.kumaran.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.kumaran.dto.CreateUserRequest;
import org.kumaran.dto.LoginRequest;
import org.kumaran.dto.UserResponse;
import org.kumaran.entity.AppNotification;
import org.kumaran.entity.UserAccount;
import org.kumaran.repository.AppNotificationRepository;
import org.kumaran.repository.LeaveTrackerRepository;
import org.kumaran.repository.UserAccountRepository;
import org.kumaran.security.JwtRequestHelper;
import org.kumaran.service.LeaveTrackerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Tag(name = "Authentication & User Management", description = "APIs for user authentication, profile management, and employee administration")
public class AuthController {
    private final UserAccountRepository userRepository;
    private final LeaveTrackerRepository leaveTrackerRepository;
    private final LeaveTrackerService leaveTrackerService;
    private final AppNotificationRepository appNotificationRepository;
    private final JwtRequestHelper jwtHelper;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Pattern NATIONALITY_PATTERN = Pattern.compile("^[A-Za-z\\s]+$");
    private static final Pattern BLOOD_GROUP_PATTERN = Pattern.compile("^(A|B|AB|O)[+-]$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+91[6-9]\\d{9}$");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[a-zA-Z0-9 /().,'-]{1,255}$");

    public AuthController(UserAccountRepository userRepository,
            LeaveTrackerRepository leaveTrackerRepository,
            LeaveTrackerService leaveTrackerService,
            AppNotificationRepository appNotificationRepository,
            JwtRequestHelper jwtHelper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.leaveTrackerRepository = leaveTrackerRepository;
        this.leaveTrackerService = leaveTrackerService;
        this.appNotificationRepository = appNotificationRepository;
        this.jwtHelper = jwtHelper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/login")
    @Operation(summary = "User Login", description = "Authenticate user with username, password and role. Returns user profile on successful authentication.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid username, password, or role", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<UserAccount> account = userRepository.findByUsername(request.getUsername());
        if (account.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        UserAccount user = account.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        String requestedRole = request.getRole() == null ? "" : request.getRole().trim();
        if (!requestedRole.isBlank()) {
            if (requestedRole.equalsIgnoreCase("workforce")) {
                boolean allowedWorkforceRole = user.getRole() != null && (user.getRole().equalsIgnoreCase("manager")
                        || user.getRole().equalsIgnoreCase("employee"));
                if (!allowedWorkforceRole) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
                }
            } else if (!user.getRole().equalsIgnoreCase(requestedRole)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
        }

        if (user.isForcePasswordChange()) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                    .body(Map.of(
                            "code", "PASSWORD_CHANGE_REQUIRED",
                            "message", "Temporary password detected. Please create a new password to continue.",
                            "username", user.getUsername()));
        }

        String token = jwtHelper.generateToken(user.getUsername(), user.getRole());
        UserResponse response = toUserResponse(user);
        response.setToken(token);
        response.setRedirectUrl(user.getRole().equalsIgnoreCase("admin") ? "/admin-dashboard.html" : "/dashboard.html");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/forgot-password-request")
    @Operation(summary = "Request Password Reset", description = "Creates a password reset request for an existing employee or manager account. Unknown usernames/emails are rejected, admin accounts are not eligible, and repeat requests return an already-pending message.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Password reset request payload", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "By username", value = "{\n  \"usernameOrEmail\": \"john.doe\"\n}"),
            @ExampleObject(name = "By email", value = "{\n  \"usernameOrEmail\": \"john.doe@leavepal.com\"\n}")
    }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset request queued successfully", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Password reset request submitted to admin notification queue."))),
            @ApiResponse(responseCode = "400", description = "Username or email is missing or account is not eligible", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "Workforce account not found", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> requestBody) {
        String identifier = requestBody.getOrDefault("usernameOrEmail", requestBody.getOrDefault("email", ""));
        if (identifier == null || identifier.trim().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username or email is required");
        }

        String value = identifier.trim();
        Optional<UserAccount> userOpt = userRepository.findByUsername(value);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmailId(value);
        }
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPersonalEmail(value);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No employee or manager account was found for that email or username");
        }

        UserAccount user = userOpt.get();
        String role = user.getRole() == null ? "" : user.getRole().trim().toLowerCase();
        if (!role.equals("employee") && !role.equals("manager")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password reset requests are only available for employee and manager accounts");
        }

        if (user.isPasswordResetRequested()) {
            return ResponseEntity.ok("Password reset request is already pending with admin review");
        }

        user.setPasswordResetRequested(true);
        user.setPasswordResetRequestedAt(Instant.now().toString());
        userRepository.save(user);

        String requesterDisplay = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        if (requesterDisplay.isBlank()) {
            requesterDisplay = user.getUsername();
        }

        final String adminMessage = requesterDisplay
                + " requested a password reset. Generate a temporary password from Employee Details.";
        userRepository.findAll().stream()
                .filter(account -> account.getRole() != null && account.getRole().equalsIgnoreCase("admin"))
                .map(UserAccount::getUsername)
                .filter(Objects::nonNull)
                .filter(usernameValue -> !usernameValue.isBlank())
                .forEach(adminUsername -> createNotification(
                        adminUsername,
                        "Password Reset Request",
                        adminMessage,
                        "password-reset-request"));

        return ResponseEntity.ok("Password reset request submitted to admin notification queue.");
    }

    @PostMapping("/users/{username}/generate-temporary-password")
    @Operation(summary = "Generate Temporary Password", description = "Admin-only endpoint to generate and assign a temporary password for employee/manager accounts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Temporary password generated successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\n  \"temporaryPassword\": \"Qm9@4vXk2P\",\n  \"username\": \"john.doe@leavepal.com\",\n  \"message\": \"Temporary password generated. Share this password securely with the employee.\"\n}"))),
            @ApiResponse(responseCode = "400", description = "Unsupported target role", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "403", description = "Only admin users can generate passwords", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> generateTemporaryPassword(
            @Parameter(description = "Username of target workforce user", required = true, example = "john.doe@company.com") @PathVariable String username,
            HttpServletRequest request) {
        if (!jwtHelper.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin users can generate passwords");
        }

        Optional<UserAccount> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UserAccount user = userOpt.get();
        String role = user.getRole() == null ? "" : user.getRole().trim().toLowerCase();
        if (!role.equals("employee") && !role.equals("manager")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Temporary passwords are only supported for manager/employee accounts");
        }

        String tempPassword = generateRandomPassword(10);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setForcePasswordChange(true);
        user.setPasswordResetRequested(false);
        user.setTemporaryPasswordIssuedAt(Instant.now().toString());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "temporaryPassword", tempPassword,
                "username", user.getUsername(),
                "message", "Temporary password generated. Share this password securely with the employee."));
    }

    @PostMapping("/auth/reset-temporary-password")
    @Operation(summary = "Reset Temporary Password", description = "Completes first-login password change using the temporary password.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Temporary password reset payload", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\n  \"username\": \"john.doe@leavepal.com\",\n  \"currentPassword\": \"Qm9@4vXk2P\",\n  \"newPassword\": \"Password@123\",\n  \"confirmPassword\": \"Password@123\"\n}")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully", content = @Content(mediaType = "text/plain", examples = @ExampleObject(value = "Password updated successfully. You can now login."))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or reset not required", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Temporary password is incorrect", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> resetTemporaryPassword(@RequestBody Map<String, String> requestBody) {
        String username = requestBody.getOrDefault("username", "").trim();
        String currentPassword = requestBody.getOrDefault("currentPassword", "");
        String newPassword = requestBody.getOrDefault("newPassword", "");
        String confirmPassword = requestBody.getOrDefault("confirmPassword", "");

        if (username.isBlank() || currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("All fields are required");
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New password and confirm password must match");
        }

        if (newPassword.length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New password must be at least 8 characters");
        }

        Optional<UserAccount> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UserAccount user = userOpt.get();
        String storedPassword = Optional.ofNullable(user.getPassword()).orElse("");
        if (storedPassword.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Temporary password is not set for this account. Contact admin.");
        }

        boolean passwordMatches;
        try {
            passwordMatches = passwordEncoder.matches(currentPassword, storedPassword);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Stored password format is invalid. Please regenerate a temporary password.");
        }

        if (!passwordMatches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Temporary password is incorrect");
        }

        if (!user.isForcePasswordChange()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password reset is not required for this account");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setForcePasswordChange(false);
        user.setTemporaryPasswordIssuedAt(null);
        user.setPasswordResetRequested(false);
        user.setPasswordResetRequestedAt(null);
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully. You can now login.");
    }

    @GetMapping("/users/{username}")
    @Operation(summary = "Get User Profile", description = "Retrieve user profile information by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getUserProfile(
            @Parameter(description = "Username of the user", required = true, example = "admin") @PathVariable String username,
            HttpServletRequest request) {
        if (!jwtHelper.isSelfOrAdmin(username, request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Optional<UserAccount> account = userRepository.findByUsername(username);
        if (account.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(toUserResponse(account.get()));
    }

    @PutMapping("/users/{username}")
    @Operation(summary = "Update User Profile", description = "Update user profile information. Only mutable fields (phone, nationality, etc.) can be updated for employees. Immutable fields are preserved.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> updateUserProfile(
            @Parameter(description = "Username of the user to update", required = true, example = "employee@company.com") @PathVariable String username,
            @RequestBody UserResponse request,
            HttpServletRequest httpRequest) {
        if (!jwtHelper.isSelfOrAdmin(username, httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Optional<UserAccount> account = userRepository.findByUsername(username);
        if (account.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UserAccount user = account.get();
        boolean adminUser = user.getRole() != null && user.getRole().equalsIgnoreCase("admin");

        Optional<String> mutableValidationError = validateMutableProfileFields(
                request.getPhoneNumber(),
                request.getNationality(),
                request.getBloodGroup(),
                request.getPersonalEmail(),
                request.getDob(),
                request.getAddress(),
                adminUser,
                true);
        if (mutableValidationError.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mutableValidationError.get());
        }

        Optional<String> profileValidationError = validateMutableProfileFields(
                request.getNationality(),
                request.getBloodGroup(),
                request.getPersonalEmail());
        if (profileValidationError.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(profileValidationError.get());
        }

        if (user.getRole() != null && user.getRole().equalsIgnoreCase("admin")) {
            user.setPhoneNumber(request.getPhoneNumber());
        } else {
            // Preserve immutable profile values for workforce users
            user.setPhoneNumber(request.getPhoneNumber());
            user.setNationality(normalizeBlank(request.getNationality()));
            user.setBloodGroup(normalizeBloodGroup(request.getBloodGroup()));
            user.setMaritalStatus(request.getMaritalStatus());
            user.setDob(request.getDob());
            user.setPersonalEmail(normalizeEmail(request.getPersonalEmail()));
            user.setGender(request.getGender());
            user.setAddress(request.getAddress());
        }

        userRepository.save(user);
        UserResponse response = toUserResponse(user);
        // Include success message in response
        response.setMessage("Profile updated successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    @Operation(summary = "Create New User", description = "Create a new user account. For employees, auto-generates employee ID if not provided. Joining date cannot be in the future and must be within the last 7 days (or defaults to today).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "409", description = "Username or Employee ID already exists", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request,
            HttpServletRequest requestContext) {
        if (!jwtHelper.isAdmin(requestContext)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin users can create accounts");
        }

        Optional<String> createValidationError = validateCreateUserRequest(request);
        if (createValidationError.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createValidationError.get());
        }

        String requestedRole = request.getRole() == null ? "" : request.getRole().trim();
        boolean isEmployeeRole = requestedRole.equalsIgnoreCase("employee");
        boolean isManagerRole = requestedRole.equalsIgnoreCase("manager");
        boolean isAdminRole = requestedRole.equalsIgnoreCase("admin");

        if (!isEmployeeRole && !isManagerRole && !isAdminRole) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Role must be one of admin, manager, or employee");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        if (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()) {
            if (userRepository.findByEmployeeId(request.getEmployeeId()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee ID already exists");
            }
        }

        List<UserAccount> managers = userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && user.getRole().equalsIgnoreCase("manager"))
                .toList();

        if (isEmployeeRole) {
            if (managers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Cannot create employee: create at least one manager first");
            }

            if (request.getReportingEmployeeId() == null || request.getReportingEmployeeId().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Reporting manager is required when creating an employee");
            }

            String reportingId = request.getReportingEmployeeId().trim();
            boolean managerExists = managers.stream()
                    .anyMatch(manager -> equalsIgnoreCase(reportingId, manager.getEmployeeId()) ||
                            equalsIgnoreCase(reportingId, manager.getUsername()));
            if (!managerExists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Reporting manager does not exist");
            }
        }

        LocalDate today = LocalDate.now();
        LocalDate oldestAllowedJoiningDate = today.minusDays(7);
        String joiningInput = request.getJoining() == null ? "" : request.getJoining().trim();
        LocalDate joiningDate;

        if (joiningInput.isBlank()) {
            joiningDate = today;
        } else {
            try {
                joiningDate = LocalDate.parse(joiningInput);
            } catch (DateTimeParseException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Joining date must be a valid ISO date (yyyy-MM-dd)");
            }
        }

        if (joiningDate.isAfter(today)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Joining date cannot be in the future");
        }

        if (joiningDate.isBefore(oldestAllowedJoiningDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Joining date cannot be older than 7 days from today");
        }

        Optional<String> profileValidationError = validateMutableProfileFields(
                request.getNationality(),
                request.getBloodGroup(),
                request.getPersonalEmail());
        if (profileValidationError.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(profileValidationError.get());
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        String resolvedEmployeeId = request.getRole() != null
                && (request.getRole().equalsIgnoreCase("employee") || request.getRole().equalsIgnoreCase("manager"))
                        ? (request.getEmployeeId() != null && !request.getEmployeeId().isBlank()
                                ? request.getEmployeeId()
                                : generateNextEmployeeId())
                        : null;
        user.setEmployeeId(resolvedEmployeeId);
        user.setEmailId(request.getEmailId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDepartment(request.getDepartment());
        user.setDesignation(request.getDesignation());
        user.setReportingEmployeeId(request.getReportingEmployeeId());
        user.setLocation(request.getLocation());
        user.setJoining(joiningDate.toString());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationality(normalizeBlank(request.getNationality()));
        user.setBloodGroup(normalizeBloodGroup(request.getBloodGroup()));
        user.setMaritalStatus(request.getMaritalStatus());
        user.setDob(request.getDob());
        user.setPersonalEmail(normalizeEmail(request.getPersonalEmail()));
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());

        userRepository.save(user);

        // Auto-sync leave tracker for employee/manager workforce accounts
        if (user.getRole() != null &&
                (user.getRole().equalsIgnoreCase("employee") || user.getRole().equalsIgnoreCase("manager"))) {
            leaveTrackerService.syncLeaveTrackerForEmployee(user, 0, 0, 0);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponse(user));
    }

    private String generateNextEmployeeId() {
        List<UserAccount> allUsers = userRepository.findAll();
        int maxSequence = allUsers.stream()
                .map(UserAccount::getEmployeeId)
                .filter(Objects::nonNull)
                .map(id -> {
                    Matcher matcher = Pattern.compile("LP-(\\d+)").matcher(id);
                    if (matcher.matches()) {
                        return Integer.parseInt(matcher.group(1));
                    }
                    return 0;
                })
                .max(Integer::compareTo)
                .orElse(0);
        return String.format("LP-%03d", maxSequence + 1);
    }

    @GetMapping("/users")
    @Operation(summary = "Get All Users", description = "Retrieve a list of all users in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<List<UserResponse>> getAllUsers(HttpServletRequest request) {
        if (!jwtHelper.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<UserAccount> allUsers = userRepository.findAll();
        List<UserResponse> users = toUserResponses(allUsers);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/next-employee-id")
    @Operation(summary = "Get Next Employee ID", description = "Generate and retrieve the next sequential employee ID (admin only). Returns ID in format LP-XXX.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Next employee ID generated successfully", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\n  \"nextEmployeeId\": \"LP-042\"\n}"))),
            @ApiResponse(responseCode = "403", description = "Only admin users can access this endpoint", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getNextEmployeeId(HttpServletRequest request) {
        if (!jwtHelper.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin users can access this endpoint");
        }

        String nextId = generateNextEmployeeId();
        return ResponseEntity.ok(Map.of("nextEmployeeId", nextId));
    }

    @GetMapping("/users/subordinates")
    @Operation(summary = "Get Subordinates", description = "Retrieve employees reporting to the current manager. Admin users receive all employees.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subordinates retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getSubordinates(HttpServletRequest request) {
        String username = jwtHelper.extractUsername(request);
        String role = jwtHelper.extractRole(request);
        if (username == null || role == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        List<UserAccount> employees = userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && user.getRole().equalsIgnoreCase("employee"))
                .toList();

        if (role.equalsIgnoreCase("admin")) {
            return ResponseEntity.ok(employees.stream().map(this::toUserResponse).collect(Collectors.toList()));
        }

        if (role.equalsIgnoreCase("employee")) {
            Optional<UserAccount> employeeOpt = userRepository.findByUsername(username);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            UserAccount employee = employeeOpt.get();
            String reporting = employee.getReportingEmployeeId();
            if (reporting == null || reporting.isBlank()) {
                return ResponseEntity.ok(List.of());
            }

            List<UserAccount> managers = userRepository.findAll().stream()
                    .filter(user -> user.getRole() != null && user.getRole().equalsIgnoreCase("manager"))
                    .toList();

            Optional<UserAccount> managerOpt = managers.stream()
                    .filter(manager -> {
                        String managerName = ((manager.getFirstName() == null ? "" : manager.getFirstName()) + " " +
                                (manager.getLastName() == null ? "" : manager.getLastName())).trim();
                        return equalsIgnoreCase(reporting, manager.getEmployeeId())
                                || equalsIgnoreCase(reporting, manager.getUsername())
                                || (!managerName.isEmpty() && equalsIgnoreCase(reporting, managerName));
                    })
                    .findFirst();

            if (managerOpt.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            UserAccount manager = managerOpt.get();
            String managerEmpId = manager.getEmployeeId();
            String managerUsername = manager.getUsername();
            String managerName = ((manager.getFirstName() == null ? "" : manager.getFirstName()) + " " +
                    (manager.getLastName() == null ? "" : manager.getLastName())).trim();

            List<UserResponse> peers = employees.stream()
                    .filter(candidate -> !equalsIgnoreCase(candidate.getUsername(), employee.getUsername()))
                    .filter(candidate -> {
                        String candidateReporting = candidate.getReportingEmployeeId();
                        if (candidateReporting == null || candidateReporting.isBlank()) {
                            return false;
                        }
                        return equalsIgnoreCase(candidateReporting, managerEmpId)
                                || equalsIgnoreCase(candidateReporting, managerUsername)
                                || (!managerName.isEmpty() && equalsIgnoreCase(candidateReporting, managerName));
                    })
                    .map(this::toUserResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(peers);
        }

        if (!role.equalsIgnoreCase("manager")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Optional<UserAccount> managerOpt = userRepository.findByUsername(username);
        if (managerOpt.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        UserAccount manager = managerOpt.get();
        String managerEmpId = manager.getEmployeeId();
        String managerUsername = manager.getUsername();
        String managerName = ((manager.getFirstName() == null ? "" : manager.getFirstName()) + " " +
                (manager.getLastName() == null ? "" : manager.getLastName())).trim();

        List<UserResponse> subordinates = employees.stream()
                .filter(employee -> {
                    String reporting = employee.getReportingEmployeeId();
                    if (reporting == null || reporting.isBlank()) {
                        return false;
                    }
                    return equalsIgnoreCase(reporting, managerEmpId)
                            || equalsIgnoreCase(reporting, managerUsername)
                            || (!managerName.isEmpty() && equalsIgnoreCase(reporting, managerName));
                })
                .map(this::toUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(subordinates);
    }

    @DeleteMapping("/users/{username}")
    @Operation(summary = "Delete User", description = "Delete an employee, manager, or admin account by username (admin only). Self-delete and deleting the last admin are blocked.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "403", description = "Only admin users can delete accounts", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "409", description = "Manager has linked subordinates", content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<String> deleteUser(
            @Parameter(description = "Username of the employee account", required = true, example = "john.doe@company.com") @PathVariable String username,
            HttpServletRequest request) {
        // Decode URL-encoded username (e.g., john%40example.com -> john@example.com)
        try {
            username = URLDecoder.decode(username, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid username encoding");
        }

        if (!jwtHelper.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin users can delete accounts");
        }

        String authUsername = jwtHelper.extractUsername(request);
        if (authUsername != null && authUsername.equalsIgnoreCase(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cannot delete your own admin account");
        }

        Optional<UserAccount> account = userRepository.findByUsername(username);
        if (account.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        UserAccount user = account.get();
        if (user.getRole() != null && user.getRole().equalsIgnoreCase("admin")) {
            long adminCount = userRepository.findAll().stream()
                    .filter(candidate -> candidate.getRole() != null && candidate.getRole().equalsIgnoreCase("admin"))
                    .count();
            if (adminCount <= 1) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Cannot delete the last remaining admin account");
            }
        }

        if (user.getRole() != null && user.getRole().equalsIgnoreCase("manager")) {
            String managerEmployeeId = user.getEmployeeId();
            String managerUsername = user.getUsername();

            boolean hasLinkedSubordinates = userRepository.findAll().stream()
                    .filter(candidate -> candidate.getRole() != null
                            && candidate.getRole().equalsIgnoreCase("employee"))
                    .map(UserAccount::getReportingEmployeeId)
                    .filter(Objects::nonNull)
                    .anyMatch(reporting -> equalsIgnoreCase(reporting, managerEmployeeId)
                            || equalsIgnoreCase(reporting, managerUsername));

            if (hasLinkedSubordinates) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Cannot delete manager: reassign or update subordinates first");
            }
        }

        if (user.getEmployeeId() != null && !user.getEmployeeId().isBlank()) {
            leaveTrackerRepository.findByEmployeeId(user.getEmployeeId())
                    .ifPresent(leaveTrackerRepository::delete);
        }

        userRepository.delete(user);
        return ResponseEntity.ok("User deleted successfully");
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private UserResponse toUserResponse(UserAccount user) {
        UserResponse response = UserResponse.from(user);
        enrichReportingManagerContext(response);
        return response;
    }

    private List<UserResponse> toUserResponses(List<UserAccount> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        Map<String, UserAccount> byEmployeeId = new HashMap<>();
        Map<String, UserAccount> byUsername = new HashMap<>();
        Map<String, UserAccount> byFullName = new HashMap<>();

        for (UserAccount candidate : users) {
            if (candidate == null) {
                continue;
            }

            String employeeId = normalize(candidate.getEmployeeId());
            if (!employeeId.isBlank()) {
                byEmployeeId.put(employeeId, candidate);
            }

            String username = normalize(candidate.getUsername());
            if (!username.isBlank()) {
                byUsername.put(username, candidate);
            }

            String fullName = normalize(((candidate.getFirstName() == null ? "" : candidate.getFirstName()) + " "
                    + (candidate.getLastName() == null ? "" : candidate.getLastName())).trim());
            if (!fullName.isBlank()) {
                byFullName.put(fullName, candidate);
            }
        }

        return users.stream()
                .map(UserResponse::from)
                .peek(response -> enrichReportingManagerContext(response, byEmployeeId, byUsername, byFullName))
                .collect(Collectors.toList());
    }

    private void enrichReportingManagerContext(UserResponse response) {
        if (response == null) {
            return;
        }

        String reportingValue = response.getReportingEmployeeId();
        if (reportingValue == null || reportingValue.isBlank()) {
            return;
        }

        String normalizedReporting = reportingValue.trim();
        response.setReporting(normalizedReporting);

        Optional<UserAccount> manager = userRepository.findByEmployeeId(normalizedReporting);
        if (manager.isEmpty()) {
            manager = userRepository.findByUsername(normalizedReporting);
        }
        if (manager.isEmpty()) {
            manager = userRepository.findAll().stream()
                    .filter(candidate -> {
                        String fullName = ((candidate.getFirstName() == null ? "" : candidate.getFirstName()) + " "
                                + (candidate.getLastName() == null ? "" : candidate.getLastName())).trim();
                        return !fullName.isEmpty() && equalsIgnoreCase(fullName, normalizedReporting);
                    })
                    .findFirst();
        }

        if (manager.isEmpty()) {
            return;
        }

        UserAccount managerUser = manager.get();
        String managerName = ((managerUser.getFirstName() == null ? "" : managerUser.getFirstName()) + " "
                + (managerUser.getLastName() == null ? "" : managerUser.getLastName())).trim();
        String managerIdentifier = (managerUser.getEmployeeId() != null && !managerUser.getEmployeeId().isBlank())
                ? managerUser.getEmployeeId()
                : managerUser.getUsername();

        response.setReportingEmployeeId(managerIdentifier);
        response.setReportingUsername(managerUser.getUsername());
        response.setReportingEmail(managerUser.getEmailId());
        response.setReportingName(!managerName.isEmpty() ? managerName : managerIdentifier);
        response.setReporting(!managerName.isEmpty() ? managerName : managerIdentifier);
    }

    private void enrichReportingManagerContext(UserResponse response,
            Map<String, UserAccount> byEmployeeId,
            Map<String, UserAccount> byUsername,
            Map<String, UserAccount> byFullName) {
        if (response == null) {
            return;
        }

        String reportingValue = response.getReportingEmployeeId();
        if (reportingValue == null || reportingValue.isBlank()) {
            return;
        }

        String normalizedReporting = reportingValue.trim();
        response.setReporting(normalizedReporting);

        String key = normalize(normalizedReporting);
        UserAccount managerUser = byEmployeeId.get(key);
        if (managerUser == null) {
            managerUser = byUsername.get(key);
        }
        if (managerUser == null) {
            managerUser = byFullName.get(key);
        }

        if (managerUser == null) {
            return;
        }

        String managerName = ((managerUser.getFirstName() == null ? "" : managerUser.getFirstName()) + " "
                + (managerUser.getLastName() == null ? "" : managerUser.getLastName())).trim();
        String managerIdentifier = (managerUser.getEmployeeId() != null && !managerUser.getEmployeeId().isBlank())
                ? managerUser.getEmployeeId()
                : managerUser.getUsername();

        response.setReportingEmployeeId(managerIdentifier);
        response.setReportingUsername(managerUser.getUsername());
        response.setReportingEmail(managerUser.getEmailId());
        response.setReportingName(!managerName.isEmpty() ? managerName : managerIdentifier);
        response.setReporting(!managerName.isEmpty() ? managerName : managerIdentifier);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeEmail(String value) {
        String normalized = normalizeBlank(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeBloodGroup(String value) {
        String normalized = normalizeBlank(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private Optional<String> validateMutableProfileFields(String nationality, String bloodGroup, String personalEmail) {
        String normalizedNationality = normalizeBlank(nationality);
        if (normalizedNationality != null && !NATIONALITY_PATTERN.matcher(normalizedNationality).matches()) {
            return Optional.of("Nationality must contain only letters and spaces");
        }

        String normalizedBloodGroup = normalizeBloodGroup(bloodGroup);
        if (normalizedBloodGroup != null && !BLOOD_GROUP_PATTERN.matcher(normalizedBloodGroup).matches()) {
            return Optional.of("Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, or O-");
        }

        String normalizedPersonalEmail = normalizeEmail(personalEmail);
        if (normalizedPersonalEmail != null && !EMAIL_PATTERN.matcher(normalizedPersonalEmail).matches()) {
            return Optional.of("Personal email must be a valid lowercase email address");
        }

        return Optional.empty();
    }

    private Optional<String> validateCreateUserRequest(CreateUserRequest request) {
        if (request == null) {
            return Optional.of("Request body is required");
        }

        String username = normalizeBlank(request.getUsername());
        if (username == null) {
            return Optional.of("Username is required");
        }

        if (username.contains("@")) {
            if (!EMAIL_PATTERN.matcher(username).matches()) {
                return Optional.of("Username email format is invalid");
            }
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            return Optional.of("Username can only contain letters, digits, dot, underscore, and hyphen");
        }

        String password = request.getPassword() == null ? "" : request.getPassword();
        if (password.length() < 8) {
            return Optional.of("Password must be at least 8 characters");
        }

        Optional<String> mutableValidationError = validateMutableProfileFields(
                request.getPhoneNumber(),
                request.getNationality(),
                request.getBloodGroup(),
                request.getPersonalEmail(),
                request.getDob(),
                request.getAddress(),
                request.getRole() != null && request.getRole().equalsIgnoreCase("admin"),
                false);
        if (mutableValidationError.isPresent()) {
            return mutableValidationError;
        }

        return Optional.empty();
    }

    private Optional<String> validateMutableProfileFields(String phoneNumber,
            String nationality,
            String bloodGroup,
            String personalEmail,
            String dob,
            String address,
            boolean adminUser,
            boolean requireMandatoryFields) {
        String normalizedPhone = normalizeBlank(phoneNumber);
        if (requireMandatoryFields && normalizedPhone == null) {
            return Optional.of("Phone number is required");
        }
        if (normalizedPhone != null && !PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            return Optional.of("Phone number must be in +91XXXXXXXXXX format and start with 6-9");
        }

        String normalizedNationality = normalizeBlank(nationality);
        if (normalizedNationality != null) {
            if (normalizedNationality.length() > 16) {
                return Optional.of("Nationality must be at most 16 characters");
            }
            if (!NATIONALITY_PATTERN.matcher(normalizedNationality).matches()) {
                return Optional.of("Nationality must contain only letters and spaces");
            }
        }

        String normalizedBloodGroup = normalizeBloodGroup(bloodGroup);
        if (normalizedBloodGroup != null && !BLOOD_GROUP_PATTERN.matcher(normalizedBloodGroup).matches()) {
            return Optional.of("Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, or O-");
        }

        String normalizedPersonalEmail = normalizeEmail(personalEmail);
        if (normalizedPersonalEmail != null && !EMAIL_PATTERN.matcher(normalizedPersonalEmail).matches()) {
            return Optional.of("Personal email must be a valid lowercase email address");
        }

        if (!adminUser) {
            String normalizedDob = normalizeBlank(dob);
            if (requireMandatoryFields && normalizedDob == null) {
                return Optional.of("Date of birth is required");
            }

            if (normalizedDob != null) {
                LocalDate dobDate;
                try {
                    dobDate = LocalDate.parse(normalizedDob);
                } catch (DateTimeParseException ex) {
                    try {
                        dobDate = LocalDate.parse(normalizedDob, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (DateTimeParseException ex2) {
                        try {
                            dobDate = LocalDate.parse(normalizedDob, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                        } catch (DateTimeParseException ex3) {
                            return Optional.of("Date of birth must be in yyyy-MM-dd format");
                        }
                    }
                }

                LocalDate today = LocalDate.now();
                if (!dobDate.isBefore(today)) {
                    return Optional.of("Date of birth cannot be today or in the future");
                }

                int age = today.getYear() - dobDate.getYear();
                if (dobDate.plusYears(age).isAfter(today)) {
                    age -= 1;
                }

                if (age < 18 || age > 60) {
                    return Optional.of("Employee age must be between 18 and 60 years");
                }
            }

            String normalizedAddress = normalizeBlank(address);
            if (requireMandatoryFields && normalizedAddress == null) {
                return Optional.of("Address is required");
            }
            if (normalizedAddress != null && !ADDRESS_PATTERN.matcher(normalizedAddress).matches()) {
                return Optional.of("Address contains unsupported characters");
            }
        }

        return Optional.empty();
    }

    private void createNotification(String recipientUsername, String title, String message, String type) {
        AppNotification notification = new AppNotification();
        notification.setRecipientUsername(recipientUsername);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(Instant.now().toString());
        appNotificationRepository.save(notification);
    }

    private String generateRandomPassword(int length) {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#*!";
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(SECURE_RANDOM.nextInt(alphabet.length())));
        }
        return builder.toString();
    }
}
