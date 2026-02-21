// render.js

function selectRole(role) {
  setRole(role);

  const token = localStorage.getItem("token");

  if (role === "admin") {
    if (token) {
      window.location.href = `/adminDashboard/${token}`;
    } else {
      // If token not present, user will login via modal
      // Stay on home / role page
      window.location.href = "/";
    }
    return;
  }

  if (role === "doctor") {
    if (token) {
      window.location.href = `/doctorDashboard/${token}`;
    } else {
      window.location.href = "/";
    }
    return;
  }

  if (role === "patient") {
    window.location.href = "/pages/patientDashboard.html";
    return;
  }

  if (role === "loggedPatient") {
    window.location.href = "/pages/loggedPatientDashboard.html";
    return;
  }

  // fallback
  window.location.href = "/";
}

function renderContent() {
  const role = getRole();

  // If userRole is not set, allow homepage (role selection)
  if (!role && window.location.pathname.endsWith("/")) return;

  // If userRole is missing on internal pages, send to homepage
  if (!role) {
    window.location.href = "/";
  }
}