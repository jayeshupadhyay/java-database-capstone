// modals.js
import { bookAppointment } from "../services/appointmentRecordService.js";

export function openModal(type) {
  let modalContent = "";

  if (type === "addDoctor") {
    modalContent = `
      <h2>Add Doctor</h2>
      <input type="text" id="doctorName" placeholder="Doctor Name" class="input-field">
      <select id="specialization" class="input-field select-dropdown">
        <option value="">Specialization</option>
        <option value="cardiologist">Cardiologist</option>
        <option value="dermatologist">Dermatologist</option>
        <option value="neurologist">Neurologist</option>
        <option value="pediatrician">Pediatrician</option>
        <option value="orthopedic">Orthopedic</option>
        <option value="gynecologist">Gynecologist</option>
        <option value="psychiatrist">Psychiatrist</option>
        <option value="dentist">Dentist</option>
        <option value="ophthalmologist">Ophthalmologist</option>
        <option value="ent">ENT Specialist</option>
        <option value="urologist">Urologist</option>
        <option value="oncologist">Oncologist</option>
        <option value="gastroenterologist">Gastroenterologist</option>
        <option value="general">General Physician</option>
      </select>
      <input type="email" id="doctorEmail" placeholder="Email" class="input-field">
      <input type="password" id="doctorPassword" placeholder="Password" class="input-field">
      <input type="text" id="doctorPhone" placeholder="Mobile No." class="input-field">

      <div class="availability-container">
        <label class="availabilityLabel">Select Availability:</label>
        <div class="checkbox-group">
          <label><input type="checkbox" name="availability" value="09:00-10:00"> 9:00 AM - 10:00 AM</label>
          <label><input type="checkbox" name="availability" value="10:00-11:00"> 10:00 AM - 11:00 AM</label>
          <label><input type="checkbox" name="availability" value="11:00-12:00"> 11:00 AM - 12:00 PM</label>
          <label><input type="checkbox" name="availability" value="12:00-13:00"> 12:00 PM - 1:00 PM</label>
        </div>
      </div>

      <button class="dashboard-btn" id="saveDoctorBtn">Save</button>
    `;
  } else if (type === "patientLogin") {
    modalContent = `
      <h2>Patient Login</h2>
      <input type="text" id="email" placeholder="Email" class="input-field">
      <input type="password" id="password" placeholder="Password" class="input-field">
      <button class="dashboard-btn" id="loginBtn">Login</button>
    `;
  } else if (type === "patientSignup") {
    modalContent = `
      <h2>Patient Signup</h2>
      <input type="text" id="name" placeholder="Name" class="input-field">
      <input type="email" id="email" placeholder="Email" class="input-field">
      <input type="password" id="password" placeholder="Password" class="input-field">
      <input type="text" id="phone" placeholder="Phone" class="input-field">
      <input type="text" id="address" placeholder="Address" class="input-field">
      <button class="dashboard-btn" id="signupBtn">Signup</button>
    `;
  } else if (type === "adminLogin") {
    modalContent = `
      <h2>Admin Login</h2>
      <input type="text" id="username" name="username" placeholder="Username" class="input-field">
      <input type="password" id="password" name="password" placeholder="Password" class="input-field">
      <button class="dashboard-btn" id="adminLoginBtn">Login</button>
    `;
  } else if (type === "doctorLogin") {
    modalContent = `
      <h2>Doctor Login</h2>
      <input type="text" id="email" placeholder="Email" class="input-field">
      <input type="password" id="password" placeholder="Password" class="input-field">
      <button class="dashboard-btn" id="doctorLoginBtn">Login</button>
    `;
  }

  document.getElementById("modal-body").innerHTML = modalContent;
  document.getElementById("modal").style.display = "block";

  const closeBtn = document.getElementById("closeModal");
  if (closeBtn) closeBtn.onclick = closeModal;

  // Hook up modal buttons to existing global handlers
  if (type === "patientSignup") {
    document.getElementById("signupBtn")?.addEventListener("click", window.signupPatient);
  }
  if (type === "patientLogin") {
    document.getElementById("loginBtn")?.addEventListener("click", window.loginPatient);
  }
  if (type === "addDoctor") {
    document.getElementById("saveDoctorBtn")?.addEventListener("click", window.adminAddDoctor);
  }
  if (type === "adminLogin") {
    document.getElementById("adminLoginBtn")?.addEventListener("click", window.adminLoginHandler);
  }
  if (type === "doctorLogin") {
    document.getElementById("doctorLoginBtn")?.addEventListener("click", window.doctorLoginHandler);
  }
}

export function closeModal() {
  const modal = document.getElementById("modal");
  if (modal) modal.style.display = "none";
}

/**
 * Minimal booking overlay for logged-in patient.
 * Called from doctorCard.js -> showBookingOverlay(e, doctor, patientData)
 */
export async function showBookingOverlay(e, doctor, patientData) {
  e?.preventDefault?.();

  const token = localStorage.getItem("token");
  if (!token) {
    alert("Session expired. Please login again.");
    localStorage.setItem("userRole", "patient");
    window.location.href = "/pages/patientDashboard.html";
    return;
  }

  const times = Array.isArray(doctor.availableTimes) ? doctor.availableTimes : [];
  if (times.length === 0) {
    alert("No available times for this doctor.");
    return;
  }

  // Simple booking form: pick date + timeslot
  const options = times.map((t) => `<option value="${t}">${t}</option>`).join("");

  document.getElementById("modal-body").innerHTML = `
    <h2>Book Appointment</h2>
    <p><b>Doctor:</b> ${doctor.name}</p>
    <p><b>Patient:</b> ${patientData?.name ?? "Unknown"}</p>

    <input type="date" id="apptDate" class="input-field" />
    <select id="apptSlot" class="input-field select-dropdown">
      ${options}
    </select>

    <button class="dashboard-btn" id="confirmBookingBtn">Confirm Booking</button>
  `;

  document.getElementById("modal").style.display = "block";
  document.getElementById("closeModal").onclick = closeModal;

  document.getElementById("confirmBookingBtn").addEventListener("click", async () => {
    const date = document.getElementById("apptDate").value;
    const slot = document.getElementById("apptSlot").value;

    if (!date) {
      alert("Please select a date.");
      return;
    }

    // Convert "09:00-10:00" => start time
    const start = slot.split("-")[0]; // "09:00"
    const appointmentTime = `${date}T${start}:00`;

    const appointment = {
      doctorId: doctor.id,
      patientId: patientData.id,
      appointmentTime,
      status: 0,
    };

    const result = await bookAppointment(appointment, token);
    alert(result.message);

    if (result.success) closeModal();
  });
}