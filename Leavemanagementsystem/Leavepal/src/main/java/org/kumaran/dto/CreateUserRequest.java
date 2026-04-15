package org.kumaran.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating a new user")
public class CreateUserRequest {
    @NotBlank(message = "Username cannot be blank")
    @Schema(description = "Username for login (email format for employees)", example = "john.doe@company.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 20, message = "Password must be 8-20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()-_+=]).{8,20}$", message = "Password must have uppercase, lowercase, number, and special character")
    @Schema(description = "User password (8-20 chars, must include uppercase, lowercase, number, special char)", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Role cannot be blank")
    @Schema(description = "User role", example = "employee", allowableValues = { "admin", "manager",
            "employee" }, requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;

    @Schema(description = "Employee ID (auto-generated if not provided for employees)", example = "LP-001")
    private String employeeId;

    @NotBlank(message = "Email ID cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@leavepal\\.com$", message = "Email must be in format user@leavepal.com")
    @Schema(description = "Official email address (must be @leavepal.com)", example = "john.doe@leavepal.com")
    private String emailId;

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 8, max = 20, message = "First name must be 8-20 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "First name must contain only alphabets")
    @Schema(description = "First name (8-20 chars, alphabets only)", example = "John")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 8, max = 20, message = "Last name must be 8-20 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Last name must contain only alphabets")
    @Schema(description = "Last name (8-20 chars, alphabets only)", example = "Doe")
    private String lastName;

    @Schema(description = "Department name", example = "Engineering")
    private String department;

    @NotBlank(message = "Designation cannot be blank")
    @Size(min = 5, max = 20, message = "Designation must be 5-20 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Designation must contain only alphabets")
    @Schema(description = "Job designation (5-20 chars, alphabets only)", example = "Software Engineer")
    private String designation;

    @Schema(description = "Reporting authority employee ID", example = "LP-001")
    private String reportingEmployeeId;

    @NotBlank(message = "Location cannot be blank")
    @Size(min = 3, max = 20, message = "Location must be 3-20 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Location must contain only alphabets")
    @Schema(description = "Office location (3-20 chars, alphabets only)", example = "New York")
    private String location;

    @Schema(description = "Joining date (auto-set to current date if not provided)", example = "2024-01-15")
    private String joining;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\+91\\d{10}$", message = "Phone number must be exactly 10 digits prefixed with +91")
    @Schema(description = "Phone number (exactly 10 digits with +91 prefix)", example = "+919876543210")
    private String phoneNumber;

    @NotBlank(message = "Nationality cannot be blank")
    @Size(max = 15, message = "Nationality must be maximum 15 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Nationality must contain only alphabets")
    @Schema(description = "Nationality (alphabets only, max 15 chars)", example = "Indian")
    private String nationality;

    @NotBlank(message = "Blood group cannot be blank")
    @Pattern(regexp = "^(O\\+|O-|A\\+|A-|B\\+|B-|AB\\+|AB-)$", message = "Blood group must be one of: O+, O-, A+, A-, B+, B-, AB+, AB-")
    @Schema(description = "Blood group (must be valid)", example = "O+", allowableValues = { "O+", "O-", "A+", "A-",
            "B+", "B-", "AB+", "AB-" })
    private String bloodGroup;

    @Schema(description = "Marital status", example = "Single", allowableValues = { "Single", "Married", "Divorced",
            "Widowed" })
    private String maritalStatus;

    @NotBlank(message = "Date of birth cannot be blank")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in YYYY-MM-DD format")
    @Schema(description = "Date of birth (YYYY-MM-DD, age must be 18-60)", example = "1990-05-15")
    private String dob;

    @NotBlank(message = "Personal email cannot be blank")
    @Email(message = "Personal email must be valid")
    @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$", message = "Personal email must be in valid format")
    @Schema(description = "Personal email address (valid email format)", example = "john.doe@gmail.com")
    private String personalEmail;

    @Schema(description = "Gender", example = "Male", allowableValues = { "Male", "Female", "Other" })
    private String gender;

    @NotBlank(message = "Address cannot be blank")
    @Size(min = 10, max = 40, message = "Address must be 10-40 characters")
    @Schema(description = "Residential address (10-40 chars)", example = "123 Main Street, New York")
    private String address;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getReportingEmployeeId() {
        return reportingEmployeeId;
    }

    public void setReportingEmployeeId(String reportingEmployeeId) {
        this.reportingEmployeeId = reportingEmployeeId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJoining() {
        return joining;
    }

    public void setJoining(String joining) {
        this.joining = joining;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPersonalEmail() {
        return personalEmail;
    }

    public void setPersonalEmail(String personalEmail) {
        this.personalEmail = personalEmail;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
