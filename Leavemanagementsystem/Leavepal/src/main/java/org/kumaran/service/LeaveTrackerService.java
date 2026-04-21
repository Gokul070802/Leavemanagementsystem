package org.kumaran.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.kumaran.entity.LeaveApplication;
import org.kumaran.entity.LeaveTrackerData;
import org.kumaran.entity.UserAccount;
import org.kumaran.repository.LeaveApplicationRepository;
import org.kumaran.repository.LeaveTrackerRepository;
import org.kumaran.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class LeaveTrackerService {
    private static final List<String> TRACKED_LEAVE_STATUSES = List.of("approved", "pending");

    private final LeaveTrackerRepository leaveTrackerRepository;
    private final UserAccountRepository userRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;

    public LeaveTrackerService(LeaveTrackerRepository leaveTrackerRepository,
            UserAccountRepository userRepository,
            LeaveApplicationRepository leaveApplicationRepository) {
        this.leaveTrackerRepository = leaveTrackerRepository;
        this.userRepository = userRepository;
        this.leaveApplicationRepository = leaveApplicationRepository;
    }

    /**
     * Calculate leave entitlement in the current April-March cycle.
     *
     * Rules:
     * - Maximum 12 sick + 12 casual per cycle.
     * - First cycle is prorated from joining month.
     */
    public int calculateCycleAccrual(String joiningDateString) {
        if (joiningDateString == null || joiningDateString.isEmpty()) {
            return 0;
        }

        try {
            LocalDate joinDate = parseFlexibleDate(joiningDateString);
            if (joinDate == null) {
                return 0;
            }

            int currentCycleStartYear = getCycleStartYear(LocalDate.now());
            return calculateEntitlementForCycle(joinDate, currentCycleStartYear);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Total entitlement accumulated from joining cycle to current cycle.
     *
     * Unused leave carry-forward is naturally represented by summing all cycle
     * entitlements and subtracting approved usage.
     */
    public int calculateTotalEntitlement(String joiningDateString) {
        if (joiningDateString == null || joiningDateString.isEmpty()) {
            return 0;
        }

        LocalDate joinDate = parseFlexibleDate(joiningDateString);
        if (joinDate == null) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        int currentCycleStartYear = getCycleStartYear(today);
        int firstCycleStartYear = getCycleStartYear(joinDate);

        int total = 0;
        for (int cycleStartYear = firstCycleStartYear; cycleStartYear <= currentCycleStartYear; cycleStartYear++) {
            total += calculateEntitlementForCycle(joinDate, cycleStartYear);
        }
        return total;
    }

    /**
     * Parse date in flexible formats
     */
    private LocalDate parseFlexibleDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (Exception e1) {
            try {
                String[] parts = dateString.split("-");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[2]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[0]);
                    return LocalDate.of(year, month, day);
                }
            } catch (Exception e2) {
                // Ignore
            }
            return null;
        }
    }

    /**
     * Count months inclusively between two dates
     */
    private int monthsInclusive(LocalDate start, LocalDate end) {
        YearMonth startMonth = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);
        return (int) startMonth.until(endMonth, java.time.temporal.ChronoUnit.MONTHS) + 1;
    }

    private int getCycleStartYear(LocalDate date) {
        return date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1;
    }

    /**
     * Calculates entitlement for a specific April-March cycle.
     */
    private int calculateEntitlementForCycle(LocalDate joinDate, int cycleStartYear) {
        LocalDate cycleStart = LocalDate.of(cycleStartYear, 4, 1);
        LocalDate cycleEnd = LocalDate.of(cycleStartYear + 1, 3, 31);

        if (joinDate.isAfter(cycleEnd)) {
            return 0;
        }

        LocalDate effectiveStart = joinDate.isAfter(cycleStart) ? joinDate : cycleStart;
        int entitlement = monthsInclusive(effectiveStart, cycleEnd);
        return Math.min(12, Math.max(0, entitlement));
    }

    /**
     * Create or update leave tracker for an employee
     */
    public LeaveTrackerData syncLeaveTrackerForEmployee(UserAccount employee, double sickLeaveBooked,
            double casualLeaveBooked, double lopBooked) {
        double totalEntitlement = calculateTotalEntitlement(employee.getJoining());

        sickLeaveBooked = normalizeLeaveUnits(sickLeaveBooked);
        casualLeaveBooked = normalizeLeaveUnits(casualLeaveBooked);
        lopBooked = normalizeLeaveUnits(lopBooked);

        Optional<LeaveTrackerData> existing = leaveTrackerRepository.findByEmployeeId(employee.getEmployeeId());
        LeaveTrackerData tracker;

        if (existing.isPresent()) {
            tracker = existing.get();
        } else {
            String employeeName = ((employee.getFirstName() != null ? employee.getFirstName() : "") + " " +
                    (employee.getLastName() != null ? employee.getLastName() : "")).trim();
            if (employeeName.isEmpty()) {
                employeeName = "Unknown Employee";
            }
            tracker = new LeaveTrackerData(
                    employee.getEmployeeId(),
                    employeeName,
                    employee.getRole(),
                    employee.getDepartment(),
                    employee.getJoining());
        }

        tracker.setSickLeaveAvailable(normalizeLeaveUnits(Math.max(0.0, totalEntitlement - sickLeaveBooked)));
        tracker.setCasualLeaveAvailable(normalizeLeaveUnits(Math.max(0.0, totalEntitlement - casualLeaveBooked)));
        tracker.setLossOfPayAvailable(0.0);
        tracker.setSickLeaveBooked(sickLeaveBooked);
        tracker.setCasualLeaveBooked(casualLeaveBooked);
        tracker.setLossOfPayBooked(lopBooked);

        return leaveTrackerRepository.save(tracker);
    }

    /**
     * Sync all workforce leave tracker data (employees and managers)
     */
    public void syncAllEmployeeLeaveTrackers() {
        List<UserAccount> employees = userRepository.findByRoleIgnoreCaseIn(List.of("employee", "manager"));

        for (UserAccount employee : employees) {
            if (employee.getEmployeeId() != null) {
                recalculateLeaveTrackerForEmployee(employee);
            }
        }
    }

    /**
     * Get leave tracker for specific employee
     */
    public LeaveTrackerData getLeaveTrackerForEmployee(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            return null;
        }

        Optional<LeaveTrackerData> existing = leaveTrackerRepository.findByEmployeeId(employeeId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Optional<UserAccount> employee = userRepository.findByEmployeeId(employeeId);
        if (employee.isEmpty()) {
            return null;
        }

        // Build tracker only once when record does not exist.
        return recalculateLeaveTrackerForEmployee(employee.get());
    }

    public LeaveTrackerData recalculateLeaveTrackerForEmployee(UserAccount employee) {
        if (employee == null || employee.getEmployeeId() == null) {
            return null;
        }

        double totalEntitlement = calculateTotalEntitlement(employee.getJoining());
        double sickBooked = 0.0;
        double casualBooked = 0.0;
        double lopBooked = 0.0;

        List<LeaveApplication> applications = leaveApplicationRepository
                .findByIdentityAndStatusesOrderByCreatedAtAsc(
                        employee.getEmployeeId(),
                        employee.getUsername(),
                        employee.getEmailId(),
                        TRACKED_LEAVE_STATUSES);

        for (LeaveApplication app : applications) {
            double duration = normalizeLeaveUnits(app.getDuration() == null ? 0.0 : app.getDuration());
            String leaveType = app.getLeaveType() == null ? "" : app.getLeaveType().trim().toLowerCase(Locale.ROOT);

            if (leaveType.equals("lop")) {
                lopBooked = normalizeLeaveUnits(lopBooked + duration);
            } else if (leaveType.equals("sick")) {
                double available = normalizeLeaveUnits(Math.max(0.0, totalEntitlement - sickBooked));
                double used = normalizeLeaveUnits(Math.min(duration, available));
                sickBooked = normalizeLeaveUnits(sickBooked + used);
                lopBooked = normalizeLeaveUnits(lopBooked + (duration - used));
            } else if (leaveType.equals("casual")) {
                double available = normalizeLeaveUnits(Math.max(0.0, totalEntitlement - casualBooked));
                double used = normalizeLeaveUnits(Math.min(duration, available));
                casualBooked = normalizeLeaveUnits(casualBooked + used);
                lopBooked = normalizeLeaveUnits(lopBooked + (duration - used));
            }
        }

        return syncLeaveTrackerForEmployee(employee, sickBooked, casualBooked, lopBooked);
    }

    public LeaveTrackerData updateLeaveTrackerBookingOnApproval(UserAccount employee) {
        if (employee == null || employee.getEmployeeId() == null) {
            return null;
        }
        return recalculateLeaveTrackerForEmployee(employee);
    }

    private double normalizeLeaveUnits(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
