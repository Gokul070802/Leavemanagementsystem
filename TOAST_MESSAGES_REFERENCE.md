# Toast Messages Reference — Leave Management System
> **For automation / QA validation use**
> All toasts are rendered via `notify(message, type)` or `window.showTopRightNotification(message, type)` — both call the same underlying function from `ui-notifications.js`.

---

## Toast Types

| Type | Colour | CSS Class | When Used |
|------|--------|-----------|-----------|
| `success` | Green | `.toast-success` | Action completed successfully |
| `error` | Red | `.toast-error` | API failure, unrecoverable error |
| `warning` | Yellow / Amber | `.toast-warning` | Validation failure, user input error |
| `info` | Blue | `.toast-info` | General info (default type) |

---

## 1. Login / Auth Page (`index.html` + `script.js`)

| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| L1 | `Welcome back, {firstName}! Login successful.` | success | Successful login (firstName may be absent → `Welcome back! Login successful.`) |
| L2 | `Password reset complete. Logging you in...` | success | Temp-password login completes (redirects after toast) |
| L3 | `Login failed. Check your credentials.` | error | API returns error on login |
| L4 | `Please enter a valid email address or username.` | warning | Empty or invalid identifier field |
| L5 | `Password must be at least 6 characters.` | warning | Password field < 6 characters |
| L6 | `Unable to connect to the authentication server.` | error | Network / fetch exception on login |

---

## 2. Forgot Password Flow (`script.js`)

| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| FP1 | `Please enter your email ID.` | warning | Email field empty when requesting reset |
| FP2 | `Password reset request sent. Admin can now see it in notifications and employee records.` | success | API returns success for password-reset request |
| FP3 | `Unable to send password reset request.` *(or server message)* | error | API error on password-reset request |
| FP4 | `Unable to connect to the authentication server.` | error | Network exception on password-reset request |
| FP5 | `Please enter both new and confirm password.` | warning | One or both password fields are empty on reset-submit |
| FP6 | `New password and confirm password do not match.` | error | Passwords do not match |
| FP7 | `Unable to reset password.` *(or server message)* | error | API error on password change |
| FP8 | `Unable to reset password at the moment.` | error | Network exception on password change |

---

## 3. Apply Leave Page (`apply-leave.html`)

### 3a. Attachment Validation (before submit)
| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| AL1 | `Only PDF attachment is allowed for sick leave.` | warning | Non-PDF file selected for sick leave attachment |
| AL2 | `Attachment size must be between 1 byte and 5 MB.` | warning | File is 0 bytes or > 5 MB |
| AL3 | `File "{name}" is valid ({size} KB).` | success | File passes all validation checks |

### 3b. Form Submission Validation
| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| AL4 | `Reason/Comment is mandatory for leave application.` | warning | Reason field empty on submit |
| AL5 | `Please select valid leave dates and type before submitting.` | warning | Dates not selected or leave type missing |
| AL6 | `Your reporting manager is not mapped. Please contact admin to link your manager before applying leave.` | warning | Employee has no manager linked |

### 3c. Per-Leave Submission Validation (inside loop, before API call)
| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| AL7 | `Medical attachment is mandatory for sick leave of 3 or more days.` | warning | Sick leave ≥ 3 days and no attachment |
| AL8 | `Only PDF attachment is allowed for sick leave.` | warning | Non-PDF attachment on sick leave chunk |
| AL9 | `Attachment size must be between 1 byte and 5 MB.` | warning | Attachment out of size bounds |
| AL10 | `Unable to read the attachment file. Try again.` | error | FileReader API fails |

### 3d. After All Submissions Complete
| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| AL11 | `Unable to apply leave right now. Please try again.` | error | All API calls failed (0 successes) |
| AL12 | `Leave submitted as {N} day(s) {Sick/Casual} + {M} day(s) Loss of Pay. Both sent to your manager for approval.` | success | Leave split into primary + LOP overflow, both saved |
| AL13 | `Leave applied successfully! Sent to your manager for review.` | success | Single leave saved successfully |

> **Notes for automation:**
> - AL12 is a dynamic string. The partial format is: `Leave submitted as X day(s) Sick + Y day(s) Loss of Pay. Both sent...` or `Leave submitted as X day(s) Casual + Y day(s) Loss of Pay. Both sent...`. The prefix (`X day(s) Sick/Casual + `) is omitted if primaryDuration = 0 (pure LOP case).
> - AL13 appears for all non-overflow single submissions.
> - AL11 appears only when **every** submission attempt returns a non-OK HTTP status.

---

## 4. Add Employee / Manager Page (`add-employee.html`)

### 4a. Form Validation
| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| AE1 | `Please fix the highlighted errors before saving.` | warning | Client-side field validation fails (name, email, phone, etc.) |
| AE2 | `Please select joining date.` | warning | Joining date field is empty |
| AE3 | `Joining date is invalid. Use yyyy-MM-dd.` | warning | Joining date cannot be parsed |
| AE4 | `Joining date cannot be in the future.` | warning | Joining date > today |
| AE5 | `Joining date cannot be older than 7 days from today.` | warning | Joining date < today − 7 days |
| AE6 | `Create at least one manager before creating an employee.` | warning | No managers exist in system when creating an employee |
| AE7 | `Please select a reporting manager.` | warning | Reporting manager dropdown not selected |

### 4b. API Responses
| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| AE8 | `{Role} {Full Name} added successfully!` | success | User created (e.g. `Employee John Smith added successfully!`) |
| AE9 | `Error adding {role}: {error.message}` | error | Network/API error creating user |

---

## 5. Admin Dashboard (`admin-dashboard.html`)

| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| AD1 | `Temporary password generated. Share it securely with the employee.` | success | Temp password generated for a user |
| AD2 | `Unable to generate temporary password.` *(or server message)* | error | API error on temp-password generation |
| AD3 | `Unable to generate temporary password.` | error | Network/fetch exception on temp-password |
| AD4 | `Session expired. Please login again.` | warning | 401 returned from dashboard data load |
| AD5 | `Unable to load admin dashboard data.` *(or server message)* | error | Non-401 API error loading dashboard |
| AD6 | `Unable to load admin dashboard data from server.` | error | Network exception loading dashboard |

---

## 6. Employee Details Page (`employee-details.html`)

| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| ED1 | `Session expired. Please login again.` | warning | 401 on workforce data load |
| ED2 | `Unable to load workforce data.` *(or server message)* | error | Non-401 API error loading workforce list |
| ED3 | `Unable to load employee profile.` | error | API error loading single employee profile |
| ED4 | `Failed to delete user record.` *(or server message)* | error | API error on delete user |
| ED5 | `User deleted successfully.` *(or server message)* | success | User deleted OK (message may come from backend) |
| ED6 | `Unable to delete user record.` | error | Network/fetch exception on delete |
| ED7 | `Unable to generate temporary password.` *(or server message)* | error | API error on temp-password generation |
| ED8 | `Temporary password generated. Share it securely with employee.` | success | Temp password generated for employee |
| ED9 | `Unable to generate temporary password.` | error | Network exception on temp-password |

---

## 7. Manager Leave Requests Page (`manager-leave-requests.html`)

> **Note:** This page uses `window.showTopRightNotification()` (aliased to `notify()` from `ui-notifications.js`).

### 7a. Data Loading
| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| ML1 | `Unable to load leave requests.` *(or server message)* | warning | Non-OK response loading requests (non-fatal) |
| ML2 | `Unable to load leave requests right now.` | error | Network exception loading requests |

### 7b. Approve / Reject Actions
| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| ML3 | `Please provide a reason for rejection.` | warning | Manager clicks Reject without entering a reason |
| ML4 | `Leave request approved successfully.` | success | Approve API call returns OK (`statusLabel = 'approved'`) |
| ML5 | `Leave request rejected successfully.` | success | Reject API call returns OK (`statusLabel = 'rejected'`) |
| ML6 | `Unable to update request status.` *(or server message)* | error | API error on approve/reject |

### 7c. Attachment Actions
| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| ML7 | `Popup blocked. Please allow popups for preview.` | warning | Browser blocked the PDF preview popup |
| ML8 | `Unable to preview attachment.` *(or specific error message)* | error | Exception during attachment preview |
| ML9 | `Unable to download attachment.` *(or specific error message)* | error | Exception during attachment download |

---

## 8. Edit Profile Page (`edit-profile.html`)

| # | Toast Message | Type | Trigger |
|---|---------------|------|---------|
| EP1 | `Please fix the highlighted errors.` | warning | Multiple validation failures (name, email, phone, gender, department, etc.) |
| EP2 | `Please fix the date of birth.` | warning | Date of birth field is invalid |
| EP3 | `Profile updated successfully!` | success | Profile saved via API |
| EP4 | `Unable to save profile.` *(or server message)* | error | API error on profile save |

---

## 9. Toast Format for Dynamic Messages

Some toasts include runtime values. Below are their resolved templates:

| ID | Template | Variables |
|----|----------|-----------|
| L1 | `Welcome back[, {firstName}]! Login successful.` | `firstName` from JWT/session |
| AE8 | `{Role} {fullName} added successfully!` | Role = `Employee` / `Manager` / `Admin`; fullName = first + last name |
| AE9 | `Error adding {role}: {error.message}` | Role lowercase; error from catch block |
| AL3 | `File "{file.name}" is valid ({(file.size/1024).toFixed(2)} KB).` | File object properties |
| AL12 | `Leave submitted as {N} day(s) {Sick/Casual} + {M} day(s) Loss of Pay. Both sent to your manager for approval.` | N = primaryDuration, M = lopDuration |
| ML4/ML5 | `Leave request {approved/rejected} successfully.` | statusLabel from `APPROVED`→`approved`, `REJECTED`→`rejected` |

---

## 10. Summary Count by Page and Type

| Page | success | error | warning | info |
|------|---------|-------|---------|------|
| Login / script.js | 2 | 5 | 3 | 0 |
| Apply Leave | 2 (AL3, AL12/AL13) | 2 | 7 | 0 |
| Add Employee | 1 | 1 | 7 | 0 |
| Admin Dashboard | 1 | 3 | 1 | 0 |
| Employee Details | 2 | 5 | 1 | 0 |
| Manager Requests | 2 | 4 | 2 | 0 |
| Edit Profile | 1 | 1 | 2 | 0 |
| **Total** | **11** | **21** | **23** | **0** |

---

## 11. Automation Tips

1. **Toast selector**: Toasts appear in a container rendered by `ui-notifications.js`. Target `.leavepal-toast` elements (or whatever class your version uses) — check the toast type via the CSS class suffix (`.toast-success`, `.toast-error`, `.toast-warning`).

2. **Timing**: Toasts auto-dismiss after ~3–5 seconds. Poll for presence within the first 2 seconds after the triggering action. Default timeout is configurable via `timeoutMs` option (example: AE8 uses `{ timeoutMs: 2200 }`).

3. **Partial matches for dynamic toasts**: For AL12, assert `contains("Loss of Pay")` and `contains("Both sent to your manager for approval")`.

4. **Server-message fallback pattern**: Many error toasts are `message || 'Default fallback text'`. The server's response body is used when available. Always assert on the *exact* server message or use a `startsWith`/`contains` approach for these.

5. **Session expired (401)**: Toast `Session expired. Please login again.` (warning) appears before redirect to login page.

6. **DB ALTER TABLE required** before full half-day testing:
   ```sql
   ALTER TABLE leave_application ALTER COLUMN duration TYPE NUMERIC(4,1) USING duration::NUMERIC(4,1);
   ALTER TABLE leave_tracker ALTER COLUMN sick_leave_available TYPE NUMERIC(4,1) USING sick_leave_available::NUMERIC(4,1);
   ALTER TABLE leave_tracker ALTER COLUMN casual_leave_available TYPE NUMERIC(4,1) USING casual_leave_available::NUMERIC(4,1);
   ALTER TABLE leave_tracker ALTER COLUMN loss_of_pay_available TYPE NUMERIC(4,1) USING loss_of_pay_available::NUMERIC(4,1);
   ALTER TABLE leave_tracker ALTER COLUMN sick_leave_booked TYPE NUMERIC(4,1) USING sick_leave_booked::NUMERIC(4,1);
   ALTER TABLE leave_tracker ALTER COLUMN casual_leave_booked TYPE NUMERIC(4,1) USING casual_leave_booked::NUMERIC(4,1);
   ALTER TABLE leave_tracker ALTER COLUMN loss_of_pay_booked TYPE NUMERIC(4,1) USING loss_of_pay_booked::NUMERIC(4,1);
   ```
