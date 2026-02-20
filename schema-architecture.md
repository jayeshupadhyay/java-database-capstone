# Smart Clinic Management System — Schema & Architecture

## Section 1: Architecture summary

This Smart Clinic Management System is built as a Spring Boot application that combines both MVC and REST-based architecture to support different user experiences. Thymeleaf templates are used to render server-side pages for key role-based dashboards (such as Admin and Doctor views), while REST APIs expose core resources (Doctors, Patients, Appointments, Prescriptions) for frontend consumption and integration. The system enforces role-based access control (Admin, Doctor, Patient) and protects sensitive routes using JWT authentication, ensuring each role can only access authorized data.

The application uses a polyglot persistence approach with two databases. MySQL stores structured relational data including admins, doctors, patients, and appointments, modeled as JPA entities with validation and relationships. MongoDB stores flexible prescription documents that may evolve over time (medicine lists, dosage instructions, notes), modeled using Mongo document classes. All controllers route requests through a shared service layer where business rules and validations are applied. The service layer delegates persistence operations to repositories: Spring Data JPA for MySQL and Spring Data MongoDB for MongoDB.

## Section 2: Numbered flow of data and control

1. A user (Admin/Doctor/Patient) accesses the application through a browser and navigates to a dashboard page (Thymeleaf MVC) or triggers an action that calls a REST endpoint (e.g., search doctors, book appointment).
2. The request is received by Spring Boot and routed through the security filter chain, where JWT authentication and role-based access rules are validated for protected routes.
3. If the request is a page-based action, it is handled by an MVC controller that returns a Thymeleaf template; if it is a data/action request, it is handled by a REST controller that returns JSON.
4. The controller forwards the request to the service layer, passing any input data (DTOs/form fields) and identifiers (userId, doctorId, appointmentId).
5. The service layer applies business validations and rules (e.g., appointment must be in the future, doctor/patient must exist, status changes must follow allowed transitions, role restrictions are enforced).
6. The service layer calls the appropriate repository:
    - Spring Data JPA repositories interact with MySQL for Admin/Doctor/Patient/Appointment entities.
    - Spring Data MongoDB repositories interact with MongoDB for Prescription documents linked to appointments/patients/doctors.
7. The repository returns results to the service layer, which prepares the final response (DTO/view model). The controller then returns either a rendered Thymeleaf page or a JSON response back to the user.

## MySQL Database Design

Structured, validated, and interrelated operational data is stored in MySQL. The core tables are **patients**, **doctors**, **appointments**, and **admins**, with additional tables to support **doctor availability** and prevent schedule conflicts.

> Notes on design choices:
> - Appointments should be retained for audit/history, so deleting users should be restricted or handled via *soft delete*.
> - Prevent overlapping appointments by enforcing time-slot rules in application logic (and optionally a unique constraint per doctor per time range if using discrete slots).
> - Availability is modeled separately so doctors can block off time and patients can only book valid hours.

---

### Table: admins
- id: INT, Primary Key, Auto Increment
- username: VARCHAR(50), Not Null, Unique
- password_hash: VARCHAR(255), Not Null
- email: VARCHAR(120), Not Null, Unique
- created_at: DATETIME, Not Null
- updated_at: DATETIME, Not Null

**Constraints / Notes:**
- `username` and `email` are `UNIQUE`.
- Password is stored hashed (BCrypt) via application code.

---

### Table: patients
- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(60), Not Null
- last_name: VARCHAR(60), Not Null
- email: VARCHAR(120), Not Null, Unique
- phone: VARCHAR(20), Null
- password_hash: VARCHAR(255), Not Null
- date_of_birth: DATE, Null
- is_active: BOOLEAN, Not Null, Default TRUE
- created_at: DATETIME, Not Null
- updated_at: DATETIME, Not Null

**Constraints / Notes:**
- `email` is `UNIQUE`.
- `is_active` supports soft-deactivation instead of deleting the patient (keeps appointment history).

---

### Table: doctors
- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(60), Not Null
- last_name: VARCHAR(60), Not Null
- email: VARCHAR(120), Not Null, Unique
- phone: VARCHAR(20), Null
- specialization: VARCHAR(80), Not Null
- password_hash: VARCHAR(255), Not Null
- is_active: BOOLEAN, Not Null, Default TRUE
- created_at: DATETIME, Not Null
- updated_at: DATETIME, Not Null

**Constraints / Notes:**
- `email` is `UNIQUE`.
- `specialization` can later be restricted in code (enum/allowed list).

---

### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Not Null, Foreign Key → doctors(id)
- patient_id: INT, Not Null, Foreign Key → patients(id)
- start_time: DATETIME, Not Null
- end_time: DATETIME, Not Null
- status: VARCHAR(20), Not Null
   - Suggested values: `SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`
- reason: VARCHAR(255), Null
- created_at: DATETIME, Not Null
- updated_at: DATETIME, Not Null

**Constraints / Notes:**
- `start_time` must be in the future when booking (validated in application).
- Appointments should **not** overlap for the same doctor; enforce in service logic by checking existing appointments in the same window.
- Deleting a doctor/patient should usually be **RESTRICTED** (or handled with soft delete) to preserve history:
   - Suggested FK actions: `ON DELETE RESTRICT` (or no cascade).

---

### Table: doctor_availability
This table stores working hours and blocked times (unavailability) so booking can be validated against doctor schedules.

- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Not Null, Foreign Key → doctors(id)
- day_of_week: TINYINT, Not Null
   - 1=Mon ... 7=Sun
- start_time: TIME, Not Null
- end_time: TIME, Not Null
- is_available: BOOLEAN, Not Null, Default TRUE
   - TRUE = working hours, FALSE = blocked/unavailable slot
- effective_from: DATE, Null
- effective_to: DATE, Null
- created_at: DATETIME, Not Null

**Constraints / Notes:**
- Enables “Doctor marks unavailability” without changing appointment records.
- Application logic uses this table to calculate valid 1-hour time slots.

---

## MongoDB Collection Design

Flexible, evolving data (like prescriptions and doctor notes) is stored in MongoDB. Prescriptions often vary in structure (multiple medicines, instructions, optional notes), making them a strong NoSQL candidate.

> Design choice: Store IDs (patientId/doctorId/appointmentId) rather than embedding full patient/doctor objects to avoid duplication and keep MySQL as the source of truth for user profiles.

### Collection: prescriptions

```json
{
  "_id": "ObjectId('65f12ab34cd56ef789012345')",
  "appointmentId": 133,
  "patientId": 26,
  "doctorId": 7,

  "createdAt": "2025-05-22T10:15:00Z",
  "updatedAt": "2025-05-22T10:20:00Z",
  "status": "ACTIVE",

  "diagnosis": "Seasonal allergies",
  "medications": [
    {
      "name": "Cetirizine",
      "strength": "10mg",
      "form": "tablet",
      "frequency": "Once daily",
      "durationDays": 14,
      "instructions": "Take after dinner"
    },
    {
      "name": "Saline nasal spray",
      "strength": "0.65%",
      "form": "spray",
      "frequency": "Twice daily",
      "durationDays": 7,
      "instructions": "Use morning and evening"
    }
  ],

  "doctorNotes": "Increase water intake. Return if symptoms worsen.",
  "refills": {
    "allowed": 2,
    "used": 0
  },

  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  },

  "tags": ["allergy", "outpatient"],
  "attachments": [
    {
      "type": "PDF",
      "fileName": "lab_results.pdf",
      "url": "https://example.com/files/lab_results.pdf",
      "uploadedAt": "2025-05-22T10:18:00Z"
    }
  ],

  "auditLog": [
    {
      "action": "CREATED",
      "byRole": "DOCTOR",
      "byUserId": 7,
      "at": "2025-05-22T10:15:00Z"
    }
  ]
}