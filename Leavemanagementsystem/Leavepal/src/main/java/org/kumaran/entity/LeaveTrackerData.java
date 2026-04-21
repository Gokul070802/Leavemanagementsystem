package org.kumaran.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "leave_tracker")
@Schema(description = "Leave tracker data for employees")
public class LeaveTrackerData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Employee ID", example = "LP-001")
    private String employeeId;

    @Column(nullable = false)
    @Schema(description = "Employee full name", example = "John Doe")
    private String employeeName;

    @Schema(description = "Job role/designation", example = "Software Engineer")
    private String role;

    @Schema(description = "Department", example = "Engineering")
    private String department;

    @Schema(description = "Date of joining", example = "2024-12-01")
    private String joiningDate;

    @Column(nullable = false, columnDefinition = "NUMERIC(4,1)")
    @Schema(description = "Sick leave available in current cycle", example = "4.0")
    private Double sickLeaveAvailable = 0.0;

    @Column(nullable = false, columnDefinition = "NUMERIC(4,1)")
    @Schema(description = "Casual leave available in current cycle", example = "4.0")
    private Double casualLeaveAvailable = 0.0;

    @Column(nullable = false, columnDefinition = "NUMERIC(4,1)")
    @Schema(description = "Loss of pay available", example = "0.0")
    private Double lossOfPayAvailable = 0.0;

    @Column(nullable = false, columnDefinition = "NUMERIC(4,1)")
    @Schema(description = "Sick leave booked", example = "0.0")
    private Double sickLeaveBooked = 0.0;

    @Column(nullable = false, columnDefinition = "NUMERIC(4,1)")
    @Schema(description = "Casual leave booked", example = "0.0")
    private Double casualLeaveBooked = 0.0;

    @Column(nullable = false, columnDefinition = "NUMERIC(4,1)")
    @Schema(description = "Loss of pay booked", example = "0.0")
    private Double lossOfPayBooked = 0.0;

    @Schema(description = "Leave cycle label, e.g. Apr 2026 – Mar 2027", example = "Apr 2026 – Mar 2027")
    private String cycleLabel;

    @Column(nullable = false, updatable = false)
    @Schema(description = "Created timestamp")
    private Long createdAt = System.currentTimeMillis();

    @Column(nullable = false)
    @Schema(description = "Updated timestamp")
    private Long updatedAt = System.currentTimeMillis();

    // Constructors
    public LeaveTrackerData() {
    }

    public LeaveTrackerData(String employeeId, String employeeName, String role, String department,
            String joiningDate) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.role = role;
        this.department = department;
        this.joiningDate = joiningDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public Double getSickLeaveAvailable() {
        return sickLeaveAvailable;
    }

    public void setSickLeaveAvailable(Double sickLeaveAvailable) {
        this.sickLeaveAvailable = sickLeaveAvailable;
        this.updatedAt = System.currentTimeMillis();
    }

    public Double getCasualLeaveAvailable() {
        return casualLeaveAvailable;
    }

    public void setCasualLeaveAvailable(Double casualLeaveAvailable) {
        this.casualLeaveAvailable = casualLeaveAvailable;
        this.updatedAt = System.currentTimeMillis();
    }

    public Double getLossOfPayAvailable() {
        return lossOfPayAvailable;
    }

    public void setLossOfPayAvailable(Double lossOfPayAvailable) {
        this.lossOfPayAvailable = lossOfPayAvailable;
        this.updatedAt = System.currentTimeMillis();
    }

    public Double getSickLeaveBooked() {
        return sickLeaveBooked;
    }

    public void setSickLeaveBooked(Double sickLeaveBooked) {
        this.sickLeaveBooked = sickLeaveBooked;
        this.updatedAt = System.currentTimeMillis();
    }

    public Double getCasualLeaveBooked() {
        return casualLeaveBooked;
    }

    public void setCasualLeaveBooked(Double casualLeaveBooked) {
        this.casualLeaveBooked = casualLeaveBooked;
        this.updatedAt = System.currentTimeMillis();
    }

    public Double getLossOfPayBooked() {
        return lossOfPayBooked;
    }

    public void setLossOfPayBooked(Double lossOfPayBooked) {
        this.lossOfPayBooked = lossOfPayBooked;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCycleLabel() {
        return cycleLabel;
    }

    public void setCycleLabel(String cycleLabel) {
        this.cycleLabel = cycleLabel;
        this.updatedAt = System.currentTimeMillis();
    }
}
