// services/adminDashboard.js
import { openModal } from "../components/modals.js";
import { getDoctors, filterDoctors, saveDoctor } from "./doctorServices.js";
import { createDoctorCard } from "../components/doctorCard.js";

document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();

  // Add Doctor button exists only for admin header
  const addBtn = document.getElementById("addDocBtn");
  if (addBtn) addBtn.addEventListener("click", () => openModal("addDoctor"));

  const searchBar = document.getElementById("searchBar");
  const filterTime = document.getElementById("filterTime");
  const filterSpecialty = document.getElementById("filterSpecialty");

  if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
  if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
  if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
});

async function loadDoctorCards() {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  contentDiv.innerHTML = "";

  const doctors = await getDoctors();
  renderDoctorCards(doctors);
}

function renderDoctorCards(doctors) {
  const contentDiv = document.getElementById("content");
  if (!contentDiv) return;

  contentDiv.innerHTML = "";

  if (!doctors || doctors.length === 0) {
    contentDiv.innerHTML = "<p>No doctors found</p>";
    return;
  }

  doctors.forEach((doctor) => {
    contentDiv.appendChild(createDoctorCard(doctor));
  });
}

async function filterDoctorsOnChange() {
  const name = document.getElementById("searchBar")?.value?.trim() || null;
  const time = document.getElementById("filterTime")?.value || null;
  const specialty = document.getElementById("filterSpecialty")?.value || null;

  const response = await filterDoctors(name, time, specialty);
  renderDoctorCards(response.doctors || []);
}

// Called by modals.js when clicking Save in Add Doctor modal
window.adminAddDoctor = async function adminAddDoctor() {
  const token = localStorage.getItem("token");
  if (!token) {
    alert("Admin session missing. Please login again.");
    window.location.href = "/";
    return;
  }

  const name = document.getElementById("doctorName")?.value?.trim();
  const specialty = document.getElementById("specialization")?.value?.trim();
  const email = document.getElementById("doctorEmail")?.value?.trim();
  const password = document.getElementById("doctorPassword")?.value?.trim();
  const phone = document.getElementById("doctorPhone")?.value?.trim();

  const availability = Array.from(
    document.querySelectorAll("input[name='availability']:checked")
  ).map((cb) => cb.value);

  if (!name || !specialty || !email || !password || !phone) {
    alert("Please fill all required fields.");
    return;
  }

  const doctor = {
    name,
    specialty, // backend field might be "specialty" or "specialization"
    email,
    password,
    phone,
    availableTimes: availability,
  };

  const { success, message } = await saveDoctor(doctor, token);
  alert(message);

  if (success) {
    document.getElementById("modal").style.display = "none";
    loadDoctorCards();
  }
};