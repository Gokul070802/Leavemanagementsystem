package org.kumaran.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "leave_application", indexes = {
        @Index(name = "idx_leave_username", columnList = "username"),
        @Index(name = "idx_leave_employee_id", columnList = "employeeId"),
        @Index(name = "idx_leave_status", columnList = "status")
})
@Schema(description = "Leave application request and review state")
public class LeaveApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Leave request identifier", example = "101")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Employee ID of applicant", example = "LP-007")
    private String employeeId;

    @Column(nullable = false)
    @Schema(description = "Username of applicant", example = "john.doe@leavepal.com")
    private String username;

    @Column(nullable = false)
    @Schema(description = "Official email of applicant", example = "john.doe@leavepal.com")
    private String emailId;

    @Schema(description = "Reporting manager employee ID", example = "LP-002")
    private String reportingManagerId;
    @Schema(description = "Reporting manager username", example = "manager.ravi@leavepal.com")
    private String reportingManagerUsername;
    @Schema(description = "Reporting manager email", example = "manager.ravi@leavepal.com")
    private String reportingManagerEmail;
    @Schema(description = "Reporting manager display name", example = "Ravi Kumar")
    private String reportingManagerName;

    @Column(nullable = false)
    @Schema(description = "Applicant full name", example = "John Doe")
    private String employeeName;

    @Column(nullable = false)
    @Schema(description = "Type of leave", example = "casual", allowableValues = { "sick", "casual", "lop" })
    private String leaveType;

    @Column(nullable = false)
    @Schema(description = "Leave start date", example = "2026-04-20")
    private String fromDate;

    @Column(nullable = false)
    @Schema(description = "Leave end date", example = "2026-04-21")
    private String toDate;

    @Column(nullable = false, columnDefinition = "NUMERIC(4,1)")
    @Schema(description = "Requested days (supports 0.5 for half day)", example = "1.0")
    private Double duration;

    @Schema(description = "Reason entered by requester", example = "Family function")
    private String reason;

    @Schema(description = "Uploaded proof name (sick leave)", example = "medical-certificate.pdf")
    private String sickAttachmentName;

    @Column(columnDefinition = "TEXT")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "Base64 attachment payload (write only)", example = "JVBERi0xLjQKJcTl8uXrp...==")
    private String sickAttachmentData;

    @Column(nullable = false)
    @Schema(description = "Current workflow status", example = "PENDING", allowableValues = { "PENDING", "APPROVED",
            "REJECTED" })
    private String status = "PENDING";

    @Column(nullable = false)
    @Schema(description = "Date when leave was applied", example = "2026-04-16")
    private String appliedDate;

    @Schema(description = "Review timestamp", example = "2026-04-17T12:11:08Z")
    private String reviewedAt;
    @Schema(description = "Reviewer username", example = "manager.ravi@leavepal.com")
    private String reviewedBy;
    @Schema(description = "Reason for rejection when status is REJECTED", example = "Critical release timeline")
    private String rejectionReason;
    @Schema(description = "Manager/admin comment", example = "Approved. Ensure handover before leave.")
    private String managerComment;

    @Column(nullable = false, updatable = false)
    @Schema(description = "Creation timestamp in epoch millis", example = "1776115200000")
    private Long createdAt = System.currentTimeMillis();

    @Column(nullable = false)
    @Schema(description = "Last update timestamp in epoch millis", example = "1776201600000")
    private Long updatedAt = System.currentTimeMillis();

    @PreUpdate
    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getReportingManagerId() {
        return reportingManagerId;
    }

    public void setReportingManagerId(String reportingManagerId) {
        this.reportingManagerId = reportingManagerId;
    }

    public String getReportingManagerUsername() {
        return reportingManagerUsername;
    }

    public void setReportingManagerUsername(String reportingManagerUsername) {
        this.reportingManagerUsername = reportingManagerUsername;
    }

    public String getReportingManagerEmail() {
        return reportingManagerEmail;
    }

    public void setReportingManagerEmail(String reportingManagerEmail) {
        this.reportingManagerEmail = reportingManagerEmail;
    }

    public String getReportingManagerName() {
        return reportingManagerName;
    }

    public void setReportingManagerName(String reportingManagerName) {
        this.reportingManagerName = reportingManagerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSickAttachmentName() {
        return sickAttachmentName;
    }

    public void setSickAttachmentName(String sickAttachmentName) {
        this.sickAttachmentName = sickAttachmentName;
    }

    public String getSickAttachmentData() {
        return sickAttachmentData;
    }

    public void setSickAttachmentData(String sickAttachmentData) {
        this.sickAttachmentData = sickAttachmentData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getManagerComment() {
        return managerComment;
    }

    public void setManagerComment(String managerComment) {
        this.managerComment = managerComment;
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
}
