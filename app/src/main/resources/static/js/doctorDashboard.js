// doctorDashboard.js
import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

let selectedDate = new Date().toISOString().split("T")[0]; // yyyy-mm-dd
let patientName = "null";
const token = localStorage.getItem("token");

document.addEventListener("DOMContentLoaded", () => {
  const datePicker = document.getElementById("datePicker");
  const todayBtn = document.getElementById("todayBtn");
  const searchBar = document.getElementById("searchBar");

  if (datePicker) datePicker.value = selectedDate;

  if (searchBar) {
    searchBar.addEventListener("input", () => {
      const val = searchBar.value.trim();
      patientName = val.length === 0 ? "null" : val;
      loadAppointments();
    });
  }

  if (todayBtn) {
    todayBtn.addEventListener("click", () => {
      selectedDate = new Date().toISOString().split("T")[0];
      if (datePicker) datePicker.value = selectedDate;
      loadAppointments();
    });
  }

  if (datePicker) {
    datePicker.addEventListener("change", () => {
      selectedDate = datePicker.value;
      loadAppointments();
    });
  }

  loadAppointments();
});

async function loadAppointments() {
  const tbody = document.getElementById("patientTableBody");
  if (!tbody) return;

  tbody.innerHTML = "";

  try {
    const data = await getAllAppointments(selectedDate, patientName, token);
    const appointments = data.appointments || [];

    if (appointments.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="5" style="text-align:center;">No Appointments found for selected date</td>
        </tr>`;
      return;
    }

    appointments.forEach((appt) => {
      // expected backend shape:
      // { id, doctorId, patient: {..} } or { appointmentId, doctorId, patient }
      const appointmentId = appt.id ?? appt.appointmentId;
      const doctorId = appt.doctorId ?? appt.doctor?.id;
      const patient = appt.patient;

      if (patient) tbody.appendChild(createPatientRow(patient, appointmentId, doctorId));
    });
  } catch (err) {
    console.error(err);
    tbody.innerHTML = `
      <tr>
        <td colspan="5" style="text-align:center;">Error loading appointments</td>
      </tr>`;
  }
}