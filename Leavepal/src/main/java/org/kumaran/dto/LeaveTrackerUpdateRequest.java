package org.kumaran.dto;

public class LeaveTrackerUpdateRequest {
    private Double sickLeaveAvailable;
    private Double casualLeaveAvailable;
    private Double lossOfPayAvailable;
    private Double sickLeaveBooked;
    private Double casualLeaveBooked;
    private Double lossOfPayBooked;

    public Double getSickLeaveAvailable() {
        return sickLeaveAvailable;
    }

    public void setSickLeaveAvailable(Double sickLeaveAvailable) {
        this.sickLeaveAvailable = sickLeaveAvailable;
    }

    public Double getCasualLeaveAvailable() {
        return casualLeaveAvailable;
    }

    public void setCasualLeaveAvailable(Double casualLeaveAvailable) {
        this.casualLeaveAvailable = casualLeaveAvailable;
    }

    public Double getLossOfPayAvailable() {
        return lossOfPayAvailable;
    }

    public void setLossOfPayAvailable(Double lossOfPayAvailable) {
        this.lossOfPayAvailable = lossOfPayAvailable;
    }

    public Double getSickLeaveBooked() {
        return sickLeaveBooked;
    }

    public void setSickLeaveBooked(Double sickLeaveBooked) {
        this.sickLeaveBooked = sickLeaveBooked;
    }

    public Double getCasualLeaveBooked() {
        return casualLeaveBooked;
    }

    public void setCasualLeaveBooked(Double casualLeaveBooked) {
        this.casualLeaveBooked = casualLeaveBooked;
    }

    public Double getLossOfPayBooked() {
        return lossOfPayBooked;
    }

    public void setLossOfPayBooked(Double lossOfPayBooked) {
        this.lossOfPayBooked = lossOfPayBooked;
    }
}
