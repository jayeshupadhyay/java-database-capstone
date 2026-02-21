// services/doctorServices.js
import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = API_BASE_URL + "/doctor";

// GET all doctors
export async function getDoctors() {
  try {
    const response = await fetch(DOCTOR_API);
    if (!response.ok) return [];
    const data = await response.json();

    // Accept both: {doctors:[...]} OR [...]
    if (Array.isArray(data)) return data;
    return data.doctors ?? [];
  } catch (err) {
    console.error("getDoctors error:", err);
    return [];
  }
}

// DELETE doctor by id (admin token required)
export async function deleteDoctor(id, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/${id}/${token}`, {
      method: "DELETE",
    });
    const data = await response.json().catch(() => ({}));
    return { success: response.ok, message: data.message || "" };
  } catch (err) {
    console.error("deleteDoctor error:", err);
    return { success: false, message: "Network error" };
  }
}

// POST create doctor (admin token required)
export async function saveDoctor(doctor, token) {
  try {
    const response = await fetch(`${DOCTOR_API}/${token}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(doctor),
    });

    const data = await response.json().catch(() => ({}));
    return { success: response.ok, message: data.message || "Saved" };
  } catch (err) {
    console.error("saveDoctor error:", err);
    return { success: false, message: "Network error" };
  }
}

// GET filter doctors
// If your backend expects route params:
// /doctor/filter/{name}/{time}/{specialty}
// this matches your patientDashboard.js usage (response.doctors)
export async function filterDoctors(name, time, specialty) {
  const safe = (v) => (v === null || v === undefined || v === "" ? "null" : encodeURIComponent(v));

  const url = `${DOCTOR_API}/filter/${safe(name)}/${safe(time)}/${safe(specialty)}`;

  try {
    const response = await fetch(url);
    if (!response.ok) return { doctors: [] };
    const data = await response.json();
    return data; // expected: { doctors: [...] }
  } catch (err) {
    console.error("filterDoctors error:", err);
    alert("Something went wrong!");
    return { doctors: [] };
  }
}