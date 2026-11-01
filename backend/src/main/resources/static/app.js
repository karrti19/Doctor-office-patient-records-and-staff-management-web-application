(()=>{"use strict";
let csrf="",currentRole="",canReadClinical=false;
const $=id=>document.getElementById(id);
const msg=s=>$("message").textContent=s;
const esc=s=>String(s??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
async function token(){
 const r=await fetch("/api/csrf",{credentials:"same-origin"});
 if(r.ok)csrf=await r.text();
 const cookie=document.cookie.split("; ").find(x=>x.startsWith("XSRF-TOKEN="));
 if(cookie)csrf=decodeURIComponent(cookie.slice(11))||csrf;
 $("logout-csrf").value=csrf;
}
async function api(url,opts={}){
 const headers={"Accept":"application/json",...(opts.body?{"Content-Type":"application/json"}:{}),...(opts.method&&opts.method!=="GET"?{"X-XSRF-TOKEN":csrf}:{}),...(opts.headers||{})};
 const r=await fetch(url,{credentials:"same-origin",...opts,headers});
 if(r.status===401||r.redirected){location.href="/login";throw Error("Please sign in again.");}
 if(!r.ok){let message="Request failed ("+r.status+")";try{const d=await r.json();message=d.detail||d.message||d.error||message;}catch{}throw Error(message);}
 if(r.status===204)return null;
 return r.json();
}
function patientHeaders(){
 $("patient-head").innerHTML="<tr><th>Patient</th><th>Date of birth</th><th>Address</th>"+(canReadClinical?"<th>Clinical notes</th><th>Prescription</th>":"")+"<th>Actions</th></tr>";
}
async function load(){
 try{
  const rows=await api("/api/patients?q="+encodeURIComponent($("query").value));
  $("count").textContent=rows.length;patientHeaders();
  $("patients").innerHTML=rows.map(p=>"<tr><td><b>"+esc(p.fullName)+"</b><br><small>#"+esc(p.id)+"</small></td><td>"+esc(p.dateOfBirth)+"</td><td>"+esc(p.address||"—")+"</td>"+(canReadClinical?"<td>"+esc(p.clinicalNotes||"—")+"</td><td>"+esc(p.prescriptionNotes||"—")+"</td>":"")+"<td><button type=\"button\" class=\"small-button\" data-edit-patient=\""+esc(p.id)+"\">View / edit</button></td></tr>").join("")||'<tr><td colspan="'+(canReadClinical?6:4)+'">No matching records. Add a fictional sample below.</td></tr>';
  msg("Patient records loaded.");
 }catch(e){$("patients").innerHTML="<tr><td>Could not load records. Confirm your account and database configuration.</td></tr>";msg(e.message);}
}
async function editPatient(id){
 try{
  const p=await api("/api/patients/"+encodeURIComponent(id));
  const form=$("edit-patient-form");
  for(const key of ["id","fullName","dateOfBirth","address","phone"])form.elements[key].value=p[key]??"";
  if(canReadClinical){form.elements.clinicalNotes.value=p.clinicalNotes??"";form.elements.prescriptionNotes.value=p.prescriptionNotes??"";}
  $("editor-title").textContent="Edit "+p.fullName+" · #"+p.id;
  $("edit-message").textContent="";$("patient-editor").hidden=false;
  $("patient-editor").scrollIntoView({behavior:"smooth",block:"start"});
 }catch(e){msg(e.message);}
}
$("search").onclick=load;$("refresh").onclick=load;$("query").addEventListener("keydown",e=>{if(e.key==="Enter")load();});
$("patients").onclick=e=>{const b=e.target.closest("[data-edit-patient]");if(b)editPatient(b.dataset.editPatient);};
$("close-editor").onclick=()=>$("patient-editor").hidden=true;
$("patient-form").onsubmit=async e=>{
 e.preventDefault();const body=Object.fromEntries(new FormData(e.currentTarget).entries());
 try{await api("/api/patients",{method:"POST",body:JSON.stringify(body)});e.currentTarget.reset();msg("Patient registered.");await load();}
 catch(err){msg(err.message);}
};
$("edit-patient-form").onsubmit=async e=>{
 e.preventDefault();const f=e.currentTarget;const id=f.elements.id.value;
 const demographics={fullName:f.elements.fullName.value,dateOfBirth:f.elements.dateOfBirth.value,address:f.elements.address.value,phone:f.elements.phone.value};
 try{
  await api("/api/patients/"+encodeURIComponent(id)+"/demographics",{method:"PUT",body:JSON.stringify(demographics)});
  if(canReadClinical){
   const clinical={clinicalNotes:f.elements.clinicalNotes.value,prescriptionNotes:f.elements.prescriptionNotes.value};
   await api("/api/patients/"+encodeURIComponent(id)+"/clinical",{method:"PUT",body:JSON.stringify(clinical)});
  }
  $("edit-message").textContent="Changes saved.";msg("Patient record updated.");await load();
 }catch(err){$("edit-message").textContent=err.message;}
};
$("staff-form").onsubmit=async e=>{
 e.preventDefault();const body=Object.fromEntries(new FormData(e.currentTarget).entries());
 try{await api("/api/admin/staff",{method:"POST",body:JSON.stringify(body)});$("staff-message").textContent="Staff account created. Deliver the temporary password securely.";e.currentTarget.reset();await staffList();}
 catch(err){$("staff-message").textContent=err.message;}
};
async function auditList(){
 try{
  const rows=await api("/api/admin/audit");
  $("audit-list").innerHTML=rows.map(a=>'<div class="audit-row"><div><b>'+esc(a.action)+'</b><small>'+esc(a.actorUsername)+' · '+esc(a.targetType)+(a.targetId?' #'+esc(a.targetId):'')+'</small></div><time>'+esc(String(a.createdAt||"").replace("T"," ").slice(0,19))+'</time></div>').join("")||'<p class="muted">No audit events recorded yet.</p>';
 }catch(e){$("audit-list").textContent=e.message;}
}
async function staffList(){
 try{
  const list=await api("/api/admin/staff");
  $("staff-list").innerHTML=list.map(s=>'<form class="staff-card" data-staff-form="'+esc(s.id)+'"><div class="staff-card-top"><div><b>'+esc(s.displayName)+'</b><small>'+esc(s.username)+' · '+esc(String(s.createdAt||"").slice(0,10))+'</small></div><span class="status '+(s.enabled?"active":"inactive")+'">'+(s.enabled?"Active":"Disabled")+'</span></div><div class="staff-edit-grid"><label>Display name<input name="displayName" required minlength="2" maxlength="100" value="'+esc(s.displayName)+'"></label><label>Role<input value="'+esc(s.role)+'" disabled></label><label class="check-label"><input type="checkbox" name="enabled" '+(s.enabled?"checked":"")+'> Account enabled</label></div><div class="staff-actions"><button type="submit" class="small-button">Save staff changes</button><button type="button" class="secondary small-button" data-reset-staff="'+esc(s.id)+'">Reset password</button></div><div class="reset-box" id="reset-'+esc(s.id)+'" hidden><label>New temporary password<input type="password" minlength="14" maxlength="128" autocomplete="new-password"></label><button type="button" class="small-button" data-confirm-reset="'+esc(s.id)+'">Set temporary password</button></div></form>').join("")||'<p class="muted">No staff accounts found.</p>';
 }catch(e){$("staff-message").textContent=e.message;}
}
$("load-staff").onclick=staffList;
$("load-audit").onclick=auditList;
$("staff-list").addEventListener("submit",async e=>{
 const f=e.target.closest("[data-staff-form]");if(!f)return;e.preventDefault();
 const id=f.dataset.staffForm;const body={displayName:f.elements.displayName.value,enabled:f.elements.enabled.checked};
 try{await api("/api/admin/staff/"+encodeURIComponent(id),{method:"PUT",body:JSON.stringify(body)});$("staff-message").textContent="Staff details updated.";await staffList();}
 catch(err){$("staff-message").textContent=err.message;}
});
$("staff-list").onclick=async e=>{
 const reset=e.target.closest("[data-reset-staff]");
 if(reset){const box=$("reset-"+reset.dataset.resetStaff);box.hidden=!box.hidden;return;}
 const confirmButton=e.target.closest("[data-confirm-reset]");
 if(confirmButton){
  const id=confirmButton.dataset.confirmReset;const input=$("reset-"+id).querySelector("input");const password=input.value;
  if(password.length<14){$("staff-message").textContent="Temporary password must be at least 14 characters.";return;}
  try{await api("/api/admin/staff/"+encodeURIComponent(id)+"/reset-password",{method:"POST",body:JSON.stringify({temporaryPassword:password})});input.value="";$("staff-message").textContent="Temporary password reset. Deliver it securely.";$("reset-"+id).hidden=true;}
  catch(err){$("staff-message").textContent=err.message;}
 }
};
async function start(){
 try{
  await token();
  const session=await api("/api/session");
  currentRole=session.role;canReadClinical=currentRole==="ADMIN"||currentRole==="DOCTOR";
  $("signed-in-user").textContent=session.displayName+" · "+session.role;
  $("current-role").textContent=session.role;
  $("clinical-editor").hidden=!canReadClinical;
  $("staff-panel").hidden=currentRole!=="ADMIN";
  await load();
  if(currentRole==="ADMIN"){await staffList();await auditList();}
 }catch(e){msg(e.message||"Sign in to use CarePoint.");}
}
start();
})();