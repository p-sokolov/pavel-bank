(function () {
  function byId(id){ return document.getElementById(id); }

  // Toggle deposit end date field for DEPOSIT accounts on dashboard
  var typeSelect = byId("createAccountType");
  var box = byId("depositEndDateBox");
  function syncDepositBox(){
    if(!typeSelect || !box) return;
    var v = (typeSelect.value || "").toUpperCase();
    if(v === "DEPOSIT") box.classList.remove("hidden");
    else box.classList.add("hidden");
  }
  if(typeSelect){
    typeSelect.addEventListener("change", syncDepositBox);
    syncDepositBox();
  }

  // Simple confirmation helper
  document.addEventListener("click", function(e){
    var t = e.target;
    if(!t) return;
    var confirmText = t.getAttribute && t.getAttribute("data-confirm");
    if(confirmText){
      if(!window.confirm(confirmText)){
        e.preventDefault();
        e.stopPropagation();
      }
    }
  }, true);
})();
