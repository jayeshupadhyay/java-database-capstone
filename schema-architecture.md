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