// util.js (global helpers)

// ---- Role helpers ----
function setRole(role) {
  localStorage.setItem("userRole", role);
}

function getRole() {
  return localStorage.getItem("userRole");
}

function clearRole() {
  localStorage.removeItem("userRole");
}

// ---- Token helpers ----
function setToken(token) {
  localStorage.setItem("token", token);
}

function getToken() {
  return localStorage.getItem("token");
}

function clearToken() {
  localStorage.removeItem("token");
}

function clearSession() {
  clearToken();
  clearRole();
}

// ---- Simple DOM helpers ----
function $(id) {
  return document.getElementById(id);
}