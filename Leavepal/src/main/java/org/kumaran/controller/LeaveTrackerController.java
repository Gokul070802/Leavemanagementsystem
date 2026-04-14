package org.kumaran.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kumaran.dto.LeaveTrackerUpdateRequest;
import org.kumaran.entity.LeaveTrackerData;
import org.kumaran.entity.UserAccount;
import org.kumaran.repository.LeaveTrackerRepository;
import org.kumaran.repository.UserAccountRepository;
import org.kumaran.security.JwtRequestHelper;
import org.kumaran.service.LeaveTrackerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/leave-tracker")
@Tag(name = "Leave Tracker Management", description = "APIs for managing leave tracker data and leave balances")
public class LeaveTrackerController {
    private final LeaveTrackerRepository leaveTrackerRepository;
    private final UserAccountRepository userRepository;
    private final LeaveTrackerService leaveTrackerService;
    private final JwtRequestHelper jwtHelper;

    public LeaveTrackerController(LeaveTrackerRepository leaveTrackerRepository, UserAccountRepository userRepository, LeaveTrackerService leaveTrackerService, JwtRequestHelper jwtHelper) {
        this.leaveTrackerRepository = leaveTrackerRepository;
        this.userRepository = userRepository;
        this.leaveTrackerService = leaveTrackerService;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping("/sync-all")
    @Operation(
        summary = "Sync All Employees Leave Tracker",
        description = "Initialize or refresh leave tracker data for all employees in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All leave trackers synced successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> syncAllLeaveTrackers(HttpServletRequest request) {
        if (!jwtHelper.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        try {
            leaveTrackerService.syncAllEmployeeLeaveTrackers();
            return ResponseEntity.ok("All employee leave trackers synced successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error syncing leave trackers: " + e.getMessage());
        }
    }

    @PostMapping("/sync")
    @Operation(
        summary = "Sync Leave Tracker Data",
        description = "Create or update leave tracker data for an employee. Automatically called when employee is created or leave is applied."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave tracker data synced successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveTrackerData.class))),
        @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> syncLeaveTrackerData(@RequestBody LeaveTrackerData request,
                                                  HttpServletRequest httpRequest) {
        if (!jwtHelper.isAdmin(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        if (request == null || request.getEmployeeId() == null || request.getEmployeeId().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Employee ID is required");
        }

        Optional<UserAccount> employeeOpt = userRepository.findByEmployeeId(request.getEmployeeId());
        if (employeeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
        }

        UserAccount employee = employeeOpt.get();
        LeaveTrackerData tracker = leaveTrackerService.recalculateLeaveTrackerForEmployee(employee);
        return ResponseEntity.ok(tracker);
    }

    @GetMapping("/{employeeId}")
    @Operation(
        summary = "Get Leave Tracker by Employee ID",
        description = "Retrieve leave tracker data for a specific employee"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave tracker retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveTrackerData.class))),
        @ApiResponse(responseCode = "404", description = "Leave tracker not found for employee",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getLeaveTrackerByEmployeeId(
        @Parameter(description = "Employee ID", required = true, example = "LP-001")
        @PathVariable String employeeId,
        HttpServletRequest request) {
        Optional<UserAccount> employee = userRepository.findByEmployeeId(employeeId);
        if (employee.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave tracker not found for employee: " + employeeId);
        }

        if (!jwtHelper.isAdmin(request)) {
            String empUsername = employee.get().getUsername();
            String callerUsername = jwtHelper.extractUsername(request);
            if (empUsername == null || !empUsername.equalsIgnoreCase(callerUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
        }

        LeaveTrackerData tracker = leaveTrackerService.getLeaveTrackerForEmployee(employeeId);
        if (tracker == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave tracker not found for employee: " + employeeId);
        }

        return ResponseEntity.ok(tracker);
    }

    @GetMapping("/department/{department}")
    @Operation(
        summary = "Get Leave Tracker by Department",
        description = "Retrieve leave tracker data for all employees in a specific department"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave trackers retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveTrackerData.class))),
        @ApiResponse(responseCode = "404", description = "No employees found in department",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> getLeaveTrackerByDepartment(
        @Parameter(description = "Department name", required = true, example = "Engineering")
        @PathVariable String department,
        HttpServletRequest request) {
        if (!jwtHelper.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        List<LeaveTrackerData> trackers = leaveTrackerRepository.findByDepartment(department);
        if (trackers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No leave trackers found for department: " + department);
        }
        return ResponseEntity.ok(trackers);
    }

    @GetMapping
    @Operation(
        summary = "Get All Leave Trackers",
        description = "Retrieve leave tracker data for all employees sorted by employee ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave trackers retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveTrackerData.class)))
    })
    public ResponseEntity<List<LeaveTrackerData>> getAllLeaveTrackers(HttpServletRequest request) {
        if (!jwtHelper.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<LeaveTrackerData> trackers = leaveTrackerRepository.findAllByOrderByEmployeeIdAsc();
        return ResponseEntity.ok(trackers);
    }

    @PutMapping("/{employeeId}")
    @Operation(
        summary = "Update Leave Tracker Data",
        description = "Update leave tracker information for a specific employee"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leave tracker updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LeaveTrackerData.class))),
        @ApiResponse(responseCode = "404", description = "Leave tracker not found",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> updateLeaveTracker(
        @Parameter(description = "Employee ID", required = true, example = "LP-001")
        @PathVariable String employeeId,
        @RequestBody LeaveTrackerUpdateRequest request,
        HttpServletRequest httpRequest) {
        if (!jwtHelper.isAdmin(httpRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        Optional<String> validationError = validateTrackerValues(
                request.getSickLeaveAvailable(),
                request.getCasualLeaveAvailable(),
                request.getLossOfPayAvailable(),
                request.getSickLeaveBooked(),
                request.getCasualLeaveBooked(),
                request.getLossOfPayBooked()
        );
        if (validationError.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError.get());
        }

        Optional<LeaveTrackerData> existing = leaveTrackerRepository.findByEmployeeId(employeeId);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave tracker not found for employee: " + employeeId);
        }

        LeaveTrackerData tracker = Objects.requireNonNull(existing.get());
        if (request.getSickLeaveAvailable() != null) {
            tracker.setSickLeaveAvailable(request.getSickLeaveAvailable());
        }
        if (request.getCasualLeaveAvailable() != null) {
            tracker.setCasualLeaveAvailable(request.getCasualLeaveAvailable());
        }
        if (request.getLossOfPayAvailable() != null) {
            tracker.setLossOfPayAvailable(request.getLossOfPayAvailable());
        }
        if (request.getSickLeaveBooked() != null) {
            tracker.setSickLeaveBooked(request.getSickLeaveBooked());
        }
        if (request.getCasualLeaveBooked() != null) {
            tracker.setCasualLeaveBooked(request.getCasualLeaveBooked());
        }
        if (request.getLossOfPayBooked() != null) {
            tracker.setLossOfPayBooked(request.getLossOfPayBooked());
        }

        leaveTrackerRepository.save(tracker);
        return ResponseEntity.ok(tracker);
    }

    @DeleteMapping("/{employeeId}")
    @Operation(
        summary = "Delete Leave Tracker Data",
        description = "Delete leave tracker data for a specific employee"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Leave tracker deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Leave tracker not found",
            content = @Content(mediaType = "text/plain"))
    })
    public ResponseEntity<?> deleteLeaveTracker(
        @Parameter(description = "Employee ID", required = true, example = "LP-001")
        @PathVariable String employeeId,
        HttpServletRequest request) {
        if (!jwtHelper.isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Optional<LeaveTrackerData> existing = leaveTrackerRepository.findByEmployeeId(employeeId);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Leave tracker not found for employee: " + employeeId);
        }

        LeaveTrackerData trackerToDelete = Objects.requireNonNull(existing.get());
        leaveTrackerRepository.delete(trackerToDelete);
        return ResponseEntity.noContent().build();
    }

    private Optional<String> validateTrackerValues(Double... values) {
        for (Double value : values) {
            if (value == null) {
                continue;
            }
            if (value < 0) {
                return Optional.of("Leave tracker values cannot be negative");
            }
            if (!isHalfDayIncrement(value)) {
                return Optional.of("Leave tracker values must be whole-day or half-day increments");
            }
        }
        return Optional.empty();
    }

    private boolean isHalfDayIncrement(Double value) {
        if (value == null) {
            return true;
        }
        return Math.abs((value * 2) - Math.rint(value * 2)) < 0.000001;
    }
}


