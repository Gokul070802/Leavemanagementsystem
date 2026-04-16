# LeavePal API Swagger + Postman Guide

## Swagger URLs
- Swagger UI: http://localhost:8081/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

## Authentication
1. Login using POST /api/auth/login.
2. Copy token from response body field: token.
3. In Postman, set Authorization type to Bearer Token and paste token.

## Base URL
http://localhost:8081

## Authentication and User Management Endpoints

### POST /api/auth/login
Description: Authenticate user and return profile with JWT token.

Request body example:
{
  "username": "john.doe@leavepal.com",
  "password": "Password@123",
  "role": "workforce"
}

Success response example (200):
{
  "username": "john.doe@leavepal.com",
  "token": "<jwt-token>",
  "redirectUrl": "/dashboard.html",
  "role": "employee",
  "employeeId": "LP-007",
  "emailId": "john.doe@leavepal.com",
  "firstName": "John",
  "lastName": "Doe"
}

### POST /api/auth/forgot-password-request
Description: Create password reset request for employee or manager.

Request body example:
{
  "usernameOrEmail": "john.doe@leavepal.com"
}

Success response example (200):
Password reset request submitted to admin notification queue.

### POST /api/users/{username}/generate-temporary-password
Description: Admin only. Generate temporary password for employee or manager.

Success response example (200):
{
  "temporaryPassword": "Qm9@4vXk2P",
  "username": "john.doe@leavepal.com",
  "message": "Temporary password generated. Share this password securely with the employee."
}

### POST /api/auth/reset-temporary-password
Description: Change temporary password on first login.

Request body example:
{
  "username": "john.doe@leavepal.com",
  "currentPassword": "Qm9@4vXk2P",
  "newPassword": "Password@123",
  "confirmPassword": "Password@123"
}

Success response example (200):
Password updated successfully. You can now login.

### GET /api/users/{username}
Description: Get profile by username (self or admin).

Success response example (200):
{
  "username": "john.doe@leavepal.com",
  "role": "employee",
  "employeeId": "LP-007",
  "emailId": "john.doe@leavepal.com",
  "firstName": "John",
  "lastName": "Doe",
  "department": "Engineering",
  "designation": "Developer"
}

### PUT /api/users/{username}
Description: Update mutable profile fields.

Request body example:
{
  "phoneNumber": "+919876543210",
  "nationality": "Indian",
  "bloodGroup": "O+",
  "maritalStatus": "Single",
  "dob": "1996-06-24",
  "personalEmail": "john.personal@gmail.com",
  "gender": "Male",
  "address": "123 Main Street, Chennai"
}

Success response example (200):
{
  "username": "john.doe@leavepal.com",
  "role": "employee",
  "phoneNumber": "+919876543210",
  "nationality": "Indian",
  "message": "Profile updated successfully"
}

### POST /api/users
Description: Admin only. Create admin, manager, or employee.

Request body example:
{
  "username": "new.employee@leavepal.com",
  "password": "Password@123",
  "role": "employee",
  "emailId": "new.employee@leavepal.com",
  "firstName": "Newname",
  "lastName": "Employe",
  "department": "Engineering",
  "designation": "Developer",
  "reportingEmployeeId": "LP-002",
  "location": "Chennai",
  "joining": "2026-04-16",
  "phoneNumber": "+919999888777",
  "nationality": "Indian",
  "bloodGroup": "B+",
  "maritalStatus": "Single",
  "dob": "1995-03-11",
  "personalEmail": "new.employee@gmail.com",
  "gender": "Female",
  "address": "No 10, ABC Street, Chennai"
}

Success response example (201):
{
  "username": "new.employee@leavepal.com",
  "role": "employee",
  "employeeId": "LP-042",
  "emailId": "new.employee@leavepal.com"
}

### GET /api/users
Description: Admin only. Get all users.

### GET /api/users/next-employee-id
Description: Admin only. Get next generated employee ID.

Success response example (200):
{
  "nextEmployeeId": "LP-042"
}

### GET /api/users/subordinates
Description: Get subordinates or peers based on caller role.

### DELETE /api/users/{username}
Description: Admin only. Delete workforce user.

Success response example (200):
User deleted successfully

## Leave Applications Endpoints

### POST /api/leave-applications
Description: Create leave request.

Request body example:
{
  "leaveType": "casual",
  "fromDate": "2026-04-20",
  "toDate": "2026-04-21",
  "duration": 2,
  "reason": "Family function"
}

Success response example (201):
{
  "id": 101,
  "employeeId": "LP-007",
  "username": "john.doe@leavepal.com",
  "leaveType": "casual",
  "fromDate": "2026-04-20",
  "toDate": "2026-04-21",
  "duration": 2,
  "status": "PENDING"
}

### POST /api/leave-applications/auto-lop
Description: Auto-create LOP request for excess leave.

Request body example:
{
  "fromDate": "2026-04-20",
  "toDate": "2026-04-21",
  "duration": 2,
  "reason": "Insufficient casual leave balance"
}

### GET /api/leave-applications/my
Description: Get my leave requests.

### GET /api/leave-applications/all
Description: Admin only. Get all leave requests.

### GET /api/leave-applications/manager
Description: Manager queue for subordinates.

### PATCH /api/leave-applications/{id}/status
Description: Approve or reject leave request.

Approve request example:
{
  "status": "APPROVED",
  "managerComment": "Approved for planned leave"
}

Reject request example:
{
  "status": "REJECTED",
  "rejectionReason": "Critical release timeline"
}

### GET /api/leave-applications/{id}/attachment
Description: Download/view sick leave medical attachment.

## Leave Tracker Endpoints

### POST /api/leave-tracker/sync-all
Description: Admin only. Sync tracker records for all users.

Success response example (200):
All employee leave trackers synced successfully

### POST /api/leave-tracker/sync
Description: Create or update one employee tracker.

Request body example:
{
  "employeeId": "LP-007",
  "sickLeaveAvailable": 4,
  "casualLeaveAvailable": 4,
  "lossOfPayAvailable": 0,
  "sickLeaveBooked": 1,
  "casualLeaveBooked": 0,
  "lossOfPayBooked": 0
}

### GET /api/leave-tracker/{employeeId}
Description: Get tracker by employee ID.

### GET /api/leave-tracker/department/{department}
Description: Admin only. Get tracker data for one department.

### GET /api/leave-tracker
Description: Admin only. Get all tracker data.

### PUT /api/leave-tracker/{employeeId}
Description: Admin only. Update tracker balances.

### DELETE /api/leave-tracker/{employeeId}
Description: Admin only. Delete tracker.

## Notification Endpoints

### GET /api/notifications/my
Description: Get my notifications.

Success response example (200):
[
  {
    "id": 120,
    "recipientUsername": "manager.ravi@leavepal.com",
    "title": "New Leave Request",
    "message": "John Doe submitted a Casual leave request (2 days).",
    "type": "leave-request-submitted",
    "read": false,
    "createdAt": "2026-04-16T09:42:15Z",
    "readAt": null
  }
]

### POST /api/notifications/mark-all-read
Description: Mark all current user notifications as read.

Success response example (200):
{
  "updated": 5
}
