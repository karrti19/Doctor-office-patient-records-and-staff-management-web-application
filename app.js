(() => {
  "use strict";
  const storageKey = "carepoint_demo_patients_v1";
  const initialPatients = [
    {id:"CP-1001",name:"Alex Morgan",dob:"1988-04-12",address:"12 Sample Lane, Example Town",visit:"2026-10-12",notes:"Fictional sample note: patient reported a mild headache.",prescription:"Fictional sample: no medication specified.",status:"Active"},
    {id:"CP-1002",name:"Jamie Taylor",dob:"1995-09-23",address:"8 Placeholder Road, Example Town",visit:"2026-10-10",notes:"Fictional sample note: routine follow-up visit.",prescription:"Fictional sample: refer to clinician.",status:"Active"},
    {id:"CP-1003",name:"Riley Jordan",dob:"1979-01-06",address:"4 Demo Crescent, Example Town",visit:"2026-10-08",notes:"Fictional sample note: seasonal allergy discussion.",prescription:"Fictional sample: no medication specified.",status:"Active"}
  ];
  let patients = loadPatients();
  const $ = (id) => document.getElementById(id);
  const escapeHTML = (value) => String(value ?? "").replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
  const prettyDate = (value) => {
    if (!value) return "—";
    const date = new Date(value + "T12:00:00");
    return Number.isNaN(date.getTime()) ? "—" : new Intl.DateTimeFormat("en-JM",{day:"numeric",month:"short",year:"numeric"}).format(date);
  };
  const initials = (name) => String(name || "?").trim().split(/\s+/).slice(0,2).map(s => s[0] || "").join("").toUpperCase();
  function loadPatients() {
    try {
      const saved = localStorage.getItem(storageKey);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed)) return parsed;
      }
    } catch (_) {}
    return initialPatients.map(p => ({...p}));
  }
  function savePatients() {
    try {
      localStorage.setItem(storageKey, JSON.stringify(patients));
      return true;
    } catch (_) {
      showToast("Browser storage is unavailable. Changes may not persist.");
      return false;
    }
  }
  function patientRow(p, directory = false) {
    return `<tr>
      <td><div class="patient-cell"><div class="patient-avatar">${escapeHTML(initials(p.name))}</div><div><strong>${escapeHTML(p.name)}</strong><small>${escapeHTML(p.id)}</small></div></div></td>
      ${directory ? `<td>${escapeHTML(prettyDate(p.dob))}</td>` : ""}
      <td>${escapeHTML(prettyDate(p.visit))}</td>
      ${directory ? "" : ""}
      <td><span class="status-label">Demo record</span></td>
      <td><button class="row-action" data-detail="${escapeHTML(p.id)}">View record →</button></td>
    </tr>`;
  }
  function render() {
    $("patient-count").textContent = patients.length;
    $("visit-count").textContent = patients.filter(p => p.visit).length;
    $("recent-patients").innerHTML = patients.slice(0,4).map(p => patientRow(p,false)).join("") || '<tr><td colspan="4">No demo records yet.</td></tr>';
    renderDirectory();
  }
  function renderDirectory() {
    const query = $("patient-search").value.trim().toLowerCase();
    const filtered = patients.filter(p => [p.name,p.id].some(v => String(v || "").toLowerCase().includes(query)));
    $("patient-table").innerHTML = filtered.map(p => patientRow(p,true)).join("") || '<tr><td colspan="5">No matching records found.</td></tr>';
    $("result-count").textContent = `${filtered.length} record${filtered.length === 1 ? "" : "s"}`;
  }
  function showToast(message) {
    const toast = $("toast");
    toast.textContent = message;
    toast.classList.add("show");
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => toast.classList.remove("show"), 2800);
  }
  function go(view) {
    const names = {dashboard:"Overview",patients:"Patient records",staff:"Staff & access"};
    document.querySelectorAll(".view").forEach(el => el.classList.add("hidden"));
    const target = $(view + "-view");
    if (target) target.classList.remove("hidden");
    document.querySelectorAll(".nav-item").forEach(el => el.classList.toggle("active",el.dataset.view === view));
    $("breadcrumb-current").textContent = names[view] || "Overview";
    if (view === "patients") renderDirectory();
    window.scrollTo({top:0,behavior:"smooth"});
  }
  function openPatient(id) {
    const p = patients.find(item => item.id === id);
    if (!p) return;
    $("patient-detail").innerHTML = `
      <div class="dialog-heading"><div><p class="eyebrow">PATIENT ID · ${escapeHTML(p.id)}</p><h2>${escapeHTML(p.name)}</h2></div><button type="button" class="close-button" id="detail-x" aria-label="Close">×</button></div>
      <p class="dialog-warning">Fictional demo record. Not for clinical use.</p>
      <div class="detail-grid">
        <div class="detail-item"><span>Date of birth</span><strong>${escapeHTML(prettyDate(p.dob))}</strong></div>
        <div class="detail-item"><span>Last visit</span><strong>${escapeHTML(prettyDate(p.visit))}</strong></div>
        <div class="detail-item" style="grid-column:1/-1"><span>Address</span><strong>${escapeHTML(p.address || "—")}</strong></div>
      </div>
      <div class="detail-note"><h3>Visit note / illness description</h3><p>${escapeHTML(p.notes || "No demo note recorded.")}</p></div>
      <div class="detail-note"><h3>Prescription note</h3><p>${escapeHTML(p.prescription || "No demo prescription recorded.")}</p></div>`;
    $("detail-dialog").showModal();
    $("detail-x").addEventListener("click",() => $("detail-dialog").close());
  }
  function openAddDialog() {
    $("patient-form").reset();
    $("patient-dialog").showModal();
  }
  document.querySelectorAll(".nav-item").forEach(btn => btn.addEventListener("click",() => go(btn.dataset.view)));
  document.querySelectorAll("[data-go]").forEach(btn => btn.addEventListener("click",() => go(btn.dataset.go)));
  $("add-patient").addEventListener("click",openAddDialog);
  $("add-patient-top").addEventListener("click",openAddDialog);
  $("close-dialog").addEventListener("click",() => $("patient-dialog").close());
  $("cancel-dialog").addEventListener("click",() => $("patient-dialog").close());
  $("close-detail").addEventListener("click",() => $("detail-dialog").close());
  $("patient-search").addEventListener("input",renderDirectory);
  document.addEventListener("click",event => {
    const button = event.target.closest("[data-detail]");
    if (button) openPatient(button.dataset.detail);
  });
  $("patient-form").addEventListener("submit",event => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const name = String(form.get("name") || "").trim();
    const dob = String(form.get("dob") || "");
    if (!name || !dob) return;
    const record = {
      id:"CP-" + String(1001 + Math.max(0,...patients.map(p => Number(String(p.id).replace("CP-","")) - 1000))).padStart(4,"0"),
      name,
      dob,
      visit:String(form.get("visit") || ""),
      address:String(form.get("address") || "").trim(),
      notes:String(form.get("notes") || "").trim(),
      prescription:String(form.get("prescription") || "").trim(),
      status:"Active"
    };
    patients.unshift(record);
    savePatients();
    render();
    $("patient-dialog").close();
    showToast("Demo record added in this browser.");
  });
  render();
})();