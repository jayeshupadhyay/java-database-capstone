function renderHeader() {
  // If on homepage, clear session-ish state
  if (window.location.pathname.endsWith("/")) {
    localStorage.removeItem("userRole");
    localStorage.removeItem("token");
  }

  const headerDiv = document.getElementById("header");
  if (!headerDiv) return;

  const role = localStorage.getItem("userRole");
  const token = localStorage.getItem("token");

  // Invalid session guard
  if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
    localStorage.removeItem("userRole");
    alert("Session expired or invalid login. Please log in again.");
    window.location.href = "/";
    return;
  }

  let headerContent = `
    <header class="header">
      <div class="header-left">
        <img class="logo" src="/assets/images/logo/logo.png" alt="logo" />
        <span class="brand">Hospital CMS</span>
      </div>
      <nav class="header-right">
  `;

  if (role === "admin") {
    headerContent += `
      <button id="addDocBtn" class="adminBtn">Add Doctor</button>
      <a href="#" id="logoutBtn">Logout</a>
    `;
  } else if (role === "doctor") {
    headerContent += `
      <a href="/doctor/dashboard" id="homeBtn">Home</a>
      <a href="#" id="logoutBtn">Logout</a>
    `;
  } else if (role === "patient") {
    headerContent += `
      <a href="#" id="loginBtn">Login</a>
      <a href="#" id="signupBtn">Sign Up</a>
    `;
  } else if (role === "loggedPatient") {
    headerContent += `
      <a href="/pages/loggedPatientDashboard.html" id="homeBtn">Home</a>
      <a href="/pages/patientAppointments.html" id="apptBtn">Appointments</a>
      <a href="#" id="logoutPatientBtn">Logout</a>
    `;
  } else {
    // default (index / unknown)
    headerContent += `
      <a href="/pages/patientDashboard.html">Explore Doctors</a>
    `;
  }

  headerContent += `
      </nav>
    </header>
  `;

  headerDiv.innerHTML = headerContent;
  attachHeaderButtonListeners();
}

function attachHeaderButtonListeners() {
  const addDocBtn = document.getElementById("addDocBtn");
  if (addDocBtn) {
    addDocBtn.addEventListener("click", () => {
      // expects openModal() from util/modals layer
      if (typeof openModal === "function") openModal("addDoctor");
      else alert("openModal() not found yet.");
    });
  }

  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) logoutBtn.addEventListener("click", logout);

  const logoutPatientBtn = document.getElementById("logoutPatientBtn");
  if (logoutPatientBtn) logoutPatientBtn.addEventListener("click", logoutPatient);

  const loginBtn = document.getElementById("loginBtn");
  if (loginBtn) {
    loginBtn.addEventListener("click", () => {
      if (typeof openModal === "function") openModal("login");
      else alert("Login modal not wired yet.");
    });
  }

  const signupBtn = document.getElementById("signupBtn");
  if (signupBtn) {
    signupBtn.addEventListener("click", () => {
      if (typeof openModal === "function") openModal("signup");
      else alert("Signup modal not wired yet.");
    });
  }
}

function logout(e) {
  if (e) e.preventDefault();
  localStorage.removeItem("token");
  localStorage.removeItem("userRole");
  window.location.href = "/";
}

function logoutPatient(e) {
  if (e) e.preventDefault();
  localStorage.removeItem("token");
  localStorage.setItem("userRole", "patient");
  window.location.href = "/pages/patientDashboard.html";
}

renderHeader();