// doctorCard.js
import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";

// showBookingOverlay() typically lives in modals.js (or patientDashboard.js)
import { showBookingOverlay } from "./modals.js";

export function createDoctorCard(doctor) {
  const card = document.createElement("div");
  card.classList.add("doctor-card");

  const role = localStorage.getItem("userRole");

  const infoDiv = document.createElement("div");
  infoDiv.classList.add("doctor-info");

  const name = document.createElement("h3");
  name.textContent = doctor.name;

  const specialization = document.createElement("p");
  specialization.textContent = `Specialization: ${doctor.specialty ?? doctor.specialization ?? ""}`;

  const email = document.createElement("p");
  email.textContent = `Email: ${doctor.email ?? ""}`;

  const availability = document.createElement("p");
  const times = Array.isArray(doctor.availableTimes) ? doctor.availableTimes.join(", ") : "";
  availability.textContent = `Available: ${times}`;

  infoDiv.appendChild(name);
  infoDiv.appendChild(specialization);
  infoDiv.appendChild(email);
  infoDiv.appendChild(availability);

  const actionsDiv = document.createElement("div");
  actionsDiv.classList.add("card-actions");

  if (role === "admin") {
    const removeBtn = document.createElement("button");
    removeBtn.textContent = "Delete";
    removeBtn.classList.add("dashboard-btn");

    removeBtn.addEventListener("click", async () => {
      const ok = confirm(`Delete doctor: ${doctor.name}?`);
      if (!ok) return;

      const token = localStorage.getItem("token");
      try {
        await deleteDoctor(doctor.id, token);
        card.remove();
      } catch (err) {
        console.error(err);
        alert("Failed to delete doctor.");
      }
    });

    actionsDiv.appendChild(removeBtn);
  } else if (role === "patient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.classList.add("dashboard-btn");

    bookNow.addEventListener("click", () => {
      alert("Patient needs to login first.");
    });

    actionsDiv.appendChild(bookNow);
  } else if (role === "loggedPatient") {
    const bookNow = document.createElement("button");
    bookNow.textContent = "Book Now";
    bookNow.classList.add("dashboard-btn");

    bookNow.addEventListener("click", async (e) => {
      const token = localStorage.getItem("token");
      try {
        const patientData = await getPatientData(token);
        showBookingOverlay(e, doctor, patientData);
      } catch (err) {
        console.error(err);
        alert("Unable to load patient profile. Please login again.");
      }
    });

    actionsDiv.appendChild(bookNow);
  }

  card.appendChild(infoDiv);
  card.appendChild(actionsDiv);

  return card;
}