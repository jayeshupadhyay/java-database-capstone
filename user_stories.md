# Smart Clinic Management System — User Stories

## Admin User Stories

### 1) Admin Login
**Title:**  
_As an Admin, I want to log into the portal with my username and password, so that I can manage the platform securely._

**Acceptance Criteria:**
1. Admin can enter username and password and submit a login form.
2. Valid credentials authenticate the admin and redirect to the Admin dashboard.
3. Invalid credentials show a clear error message without logging in.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- Login should return a JWT or create a secure session (based on implementation choice).

---

### 2) Admin Logout
**Title:**  
_As an Admin, I want to log out of the portal, so that I can protect system access._

**Acceptance Criteria:**
1. Admin can click “Logout” from any authenticated Admin page.
2. The system invalidates the active session/token on the client side and redirects to the login page.
3. Accessing protected Admin routes after logout requires logging in again.

**Priority:** High  
**Story Points:** 2  
**Notes:**
- For JWT, logout typically clears the token in the browser storage/cookie.

---

### 3) Add Doctor to Portal
**Title:**  
_As an Admin, I want to add doctors to the portal, so that patients can book appointments with available providers._

**Acceptance Criteria:**
1. Admin can create a doctor profile with name, email, specialization, and availability.
2. The system validates required fields and prevents duplicate doctor emails.
3. Newly added doctors appear in the doctor listing/search results.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Store doctor data in MySQL with JPA validations.

---

### 4) Delete Doctor Profile
**Title:**  
_As an Admin, I want to delete a doctor’s profile from the portal, so that outdated or invalid accounts are removed._

**Acceptance Criteria:**
1. Admin can select a doctor profile and choose delete (or deactivate).
2. The system requests confirmation before removal.
3. The doctor no longer appears in public listings after deletion/deactivation.

**Priority:** Medium  
**Story Points:** 3  
**Notes:**
- Consider “soft delete” (active=false) to preserve appointment history.

---

### 5) Run Monthly Appointment Count Stored Procedure
**Title:**  
_As an Admin, I want to run a stored procedure in MySQL CLI to get the number of appointments per month, so that I can track usage statistics._

**Acceptance Criteria:**
1. A MySQL stored procedure exists that returns appointment counts grouped by month.
2. Admin can execute the procedure in the MySQL CLI successfully.
3. The procedure output matches the appointments stored in the database.

**Priority:** Medium  
**Story Points:** 2  
**Notes:**
- Example output: month + count (e.g., 2025-05 → 25).

---

## Patient User Stories

### 6) View Doctors Without Logging In
**Title:**  
_As a Patient, I want to view a list of doctors without logging in, so that I can explore options before registering._

**Acceptance Criteria:**
1. An unauthenticated user can access the doctors listing page/endpoint.
2. The list displays doctor name, specialization, contact (optional), and available slots (if enabled).
3. The system does not expose sensitive doctor-only/admin-only details.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- Public endpoint example: GET /api/doctors.

---

### 7) Patient Sign Up
**Title:**  
_As a Patient, I want to sign up using my email and password, so that I can book appointments._

**Acceptance Criteria:**
1. Patient can register with email and password (and optionally name/phone).
2. The system validates email format and password rules.
3. Duplicate emails are blocked with a clear error message.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Password should be hashed (e.g., BCrypt).

---

### 8) Patient Login
**Title:**  
_As a Patient, I want to log into the portal, so that I can manage my bookings._

**Acceptance Criteria:**
1. Patient can log in with registered email and password.
2. Successful login grants access to patient features (book, view appointments).
3. Failed login shows an error without revealing which field was incorrect.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- JWT should include role=PATIENT.

---

### 9) Patient Logout
**Title:**  
_As a Patient, I want to log out of the portal, so that I can secure my account._

**Acceptance Criteria:**
1. Patient can log out using a logout button/link.
2. The client clears authentication token/session and returns to home/login page.
3. Protected patient pages require re-authentication after logout.

**Priority:** High  
**Story Points:** 2  
**Notes:**
- For JWT, remove token from storage/cookie.

---

### 10) Book an Hour-long Appointment (Authenticated)
**Title:**  
_As a Patient, I want to log in and book an hour-long appointment to consult with a doctor, so that I can receive medical care._

**Acceptance Criteria:**
1. Patient can select a doctor, date, and an available 1-hour time slot.
2. The system prevents booking in the past and prevents double-booking the same slot.
3. On success, the appointment is saved and visible in the patient’s appointment list.

**Priority:** High  
**Story Points:** 8  
**Notes:**
- Appointment duration is fixed at 60 minutes.
- Validate doctorId + patientId exist in MySQL.

---

### 11) View Upcoming Appointments
**Title:**  
_As a Patient, I want to view my upcoming appointments, so that I can prepare accordingly._

**Acceptance Criteria:**
1. Patient can view a list of future appointments sorted by date/time.
2. Each appointment shows doctor name, specialization, date, time, and status.
3. Only the logged-in patient’s appointments are visible.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- Endpoint example: GET /api/appointments?mine=true&upcoming=true.

---

## Doctor User Stories

### 12) Doctor Login
**Title:**  
_As a Doctor, I want to log into the portal, so that I can manage my appointments._

**Acceptance Criteria:**
1. Doctor can log in using assigned credentials.
2. Successful login redirects to Doctor dashboard or calendar view.
3. Invalid login attempts show a safe error message.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- JWT should include role=DOCTOR and doctorId.

---

### 13) Doctor Logout
**Title:**  
_As a Doctor, I want to log out of the portal, so that I can protect my data._

**Acceptance Criteria:**
1. Doctor can log out from any doctor page.
2. Token/session is cleared and user is redirected to login/home.
3. Protected doctor routes require login again.

**Priority:** High  
**Story Points:** 2  
**Notes:**
- Same logout mechanics as other roles.

---

### 14) View Appointment Calendar
**Title:**  
_As a Doctor, I want to view my appointment calendar, so that I can stay organized._

**Acceptance Criteria:**
1. Doctor can view appointments by day/week.
2. Appointments display patient name (or ID), date/time, and status.
3. Doctor only sees appointments assigned to them.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Calendar view can be a table/list in early versions.

---

### 15) Mark Unavailability
**Title:**  
_As a Doctor, I want to mark my unavailability, so that patients are shown only available slots._

**Acceptance Criteria:**
1. Doctor can block off time slots/dates as unavailable.
2. Unavailable slots cannot be booked by patients.
3. Updates reflect immediately in doctor availability and booking screens.

**Priority:** Medium  
**Story Points:** 8  
**Notes:**
- Can be implemented as an “Availability/TimeSlot” table or a simple rule-based schedule.

---

### 16) Update Doctor Profile
**Title:**  
_As a Doctor, I want to update my profile with specialization and contact information, so that patients have up-to-date information._

**Acceptance Criteria:**
1. Doctor can edit profile fields (specialization, phone/email if allowed).
2. The system validates required fields and email format.
3. Updated profile appears in the public doctor listing.

**Priority:** Medium  
**Story Points:** 3  
**Notes:**
- Decide which fields are editable by doctors vs. admins.

---

### 17) View Patient Details for Upcoming Appointments
**Title:**  
_As a Doctor, I want to view the patient details for upcoming appointments, so that I can be prepared._

**Acceptance Criteria:**
1. Doctor can open an appointment and view patient details required for care (name, age/contact if allowed).
2. Doctor can only view details for patients with appointments assigned to them.
3. Access is blocked for unrelated patient records.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Enforce access control in the service layer (doctorId must match appointment’s doctorId).