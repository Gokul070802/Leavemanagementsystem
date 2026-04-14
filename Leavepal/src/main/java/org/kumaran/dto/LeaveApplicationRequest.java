package org.kumaran.dto;

public class LeaveApplicationRequest {
    private String reportingManagerId;
    private String reportingManagerUsername;
    private String reportingManagerEmail;
    private String reportingManagerName;
    private String leaveType;
    private String fromDate;
    private String toDate;
    private String dayType;
    private Double duration;
    private String reason;
    private String sickAttachmentName;
    private String sickAttachmentData;

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

    public String getDayType() {
        return dayType;
    }

    public void setDayType(String dayType) {
        this.dayType = dayType;
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
}
