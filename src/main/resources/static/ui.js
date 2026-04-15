(function () {
  function byId(id) { return document.getElementById(id); }
  function all(selector, root) { return Array.from((root || document).querySelectorAll(selector)); }

  function ensureToastRoot() {
    var root = byId('toastRoot');
    if (root) return root;

    root = document.createElement('div');
    root.id = 'toastRoot';
    root.className = 'toast-stack';
    document.body.appendChild(root);
    return root;
  }

  function showToast(message, variant, timeoutMs) {
    if (!message) return;
    var root = ensureToastRoot();
    var toast = document.createElement('div');
    toast.className = 'toast' + (variant ? ' toast--' + variant : '');
    toast.setAttribute('role', 'status');
    toast.setAttribute('aria-live', 'polite');
    toast.textContent = message;
    root.appendChild(toast);

    window.setTimeout(function () {
      toast.classList.add('toast--visible');
    }, 10);

    var delay = typeof timeoutMs === 'number' ? timeoutMs : 2600;
    window.setTimeout(function () {
      toast.classList.remove('toast--visible');
      window.setTimeout(function () {
        if (toast.parentNode) toast.parentNode.removeChild(toast);
      }, 220);
    }, delay);
  }

  async function copyText(text) {
    var value = (text || '').trim();
    if (!value) throw new Error('Nothing to copy');

    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(value);
      return;
    }

    var textarea = document.createElement('textarea');
    textarea.value = value;
    textarea.setAttribute('readonly', 'readonly');
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    textarea.style.left = '-9999px';
    document.body.appendChild(textarea);
    textarea.select();
    textarea.setSelectionRange(0, value.length);
    var ok = false;
    try {
      ok = document.execCommand('copy');
    } finally {
      document.body.removeChild(textarea);
    }
    if (!ok) {
      throw new Error('Copy command was rejected');
    }
  }

  async function submitAsyncForm(form) {
    if (!form || form.dataset.asyncBound === 'true') return;
    form.dataset.asyncBound = 'true';

    form.addEventListener('submit', async function (e) {
      e.preventDefault();

      if (form.dataset.submitting === 'true') return;
      form.dataset.submitting = 'true';

      var submitter = form.querySelector('button[type="submit"], input[type="submit"]');
      var originalLabel = submitter && 'value' in submitter ? submitter.value : (submitter ? submitter.textContent : '');
      var isInput = submitter && submitter.tagName === 'INPUT';

      if (submitter) {
        submitter.disabled = true;
        if (isInput) submitter.value = 'Saving…';
        else submitter.textContent = 'Saving…';
      }

      try {
        var response = await fetch(form.action || window.location.href, {
          method: (form.method || 'GET').toUpperCase(),
          body: new FormData(form),
          credentials: 'same-origin',
          headers: {
            'X-Requested-With': 'XMLHttpRequest',
            'Accept': 'text/html,application/json'
          }
        });

        var contentType = response.headers.get('content-type') || '';
        var isJson = contentType.indexOf('application/json') !== -1;

        if (response.status === 429) {
          var rateMessage = 'Too many requests. Please wait and try again.';
          if (isJson) {
            try {
              var rateData = await response.json();
              if (rateData && rateData.message) rateMessage = rateData.message;
            } catch (ignored) {}
          }
          showToast(rateMessage, 'error', 5000);
          return;
        }

        if (!response.ok) {
          var errorMessage = 'Request failed.';
          if (isJson) {
            try {
              var errorData = await response.json();
              if (errorData && errorData.message) errorMessage = errorData.message;
            } catch (ignored2) {}
          } else {
            try {
              var text = await response.text();
              if (text) errorMessage = text.replace(/\s+/g, ' ').trim().slice(0, 180);
            } catch (ignored3) {}
          }
          showToast(errorMessage, 'error', 4500);
          return;
        }

        if (response.redirected) {
          window.location.href = response.url;
          return;
        }

        if (isJson) {
          var data = null;
          try {
            data = await response.json();
          } catch (ignored4) {}
          if (data && data.redirectUrl) {
            window.location.href = data.redirectUrl;
            return;
          }
        }

        window.location.reload();
      } catch (err) {
        showToast('Network error. Please try again.', 'error', 4500);
      } finally {
        form.dataset.submitting = 'false';
        if (submitter) {
          submitter.disabled = false;
          if (isInput) submitter.value = originalLabel;
          else submitter.textContent = originalLabel;
        }
      }
    });
  }

  function handleCopyClick(target) {
    var node = target && target.closest ? target.closest('[data-copy-text], .copyable') : null;
    if (!node) return false;

    var text = node.getAttribute('data-copy-text') || node.textContent || '';
    text = text.replace(/\s+/g, ' ').trim();
    if (!text) return false;

    copyText(text)
      .then(function () {
        showToast('Copied to clipboard', 'success');
      })
      .catch(function () {
        showToast('Copy failed', 'error', 3500);
      });

    return true;
  }

  function handleConfirmClick(target) {
    var node = target && target.closest ? target.closest('[data-confirm]') : null;
    if (!node) return false;

    var confirmText = node.getAttribute('data-confirm');
    if (confirmText && !window.confirm(confirmText)) {
      return true;
    }
    return false;
  }

  function syncDepositBox() {
    var typeSelect = byId('createAccountType');
    var box = byId('depositEndDateBox');
    if (!typeSelect || !box) return;

    var value = (typeSelect.value || '').toUpperCase();
    if (value === 'DEPOSIT') box.classList.remove('hidden');
    else box.classList.add('hidden');
  }

  function showServerNoticeFromUrl() {
    var params = new URLSearchParams(window.location.search);
    var notice = params.get('notice') || params.get('error') || params.get('status');

    if (!notice) return;

    var normalized = String(notice).toLowerCase();
    if (normalized === '429' || normalized === 'rate-limit' || normalized === 'rate_limited' || normalized === 'too_many_requests') {
      showToast('Too many requests. Please wait a moment and try again.', 'error', 5000);
    }
  }

  function init() {
    syncDepositBox();
    showServerNoticeFromUrl();

    var typeSelect = byId('createAccountType');
    if (typeSelect) {
      typeSelect.addEventListener('change', syncDepositBox);
    }

    all('form[data-async-form]').forEach(function (form) {
      submitAsyncForm(form);
    });
  }

  document.addEventListener('click', function (e) {
    if (!e.target) return;

    if (handleCopyClick(e.target)) {
      e.preventDefault();
      e.stopPropagation();
      return;
    }

    if (handleConfirmClick(e.target)) {
      e.preventDefault();
      e.stopPropagation();
    }
  }, true);

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  window.PavelBankUI = window.PavelBankUI || {};
  window.PavelBankUI.toast = showToast;
  window.PavelBankUI.copy = copyText;
})();