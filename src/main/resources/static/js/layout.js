document.addEventListener('DOMContentLoaded', function () {
        const params = new URLSearchParams(globalThis.location.search);

        function showToast(id, delay = 4000) {
            const el = document.getElementById(id);
            if (!el) return;
            new bootstrap.Toast(el, { delay }).show();
        }

        function showModal(id) {
            const el = document.getElementById(id);
            if (!el) return;
            bootstrap.Modal.getOrCreateInstance(el).show();
        }

        if (params.has('loginError'))    { showModal('loginModal');    showToast('toastLoginError'); }
        if (params.has('openLogin'))     { showModal('loginModal'); }
        if (params.has('registerError')) { showModal('registerModal'); showToast('toastRegisterError'); }
        if (params.has('openRegister'))  { showModal('registerModal'); }
        if (params.has('registered'))    { showModal('loginModal');    showToast('toastRegistered'); }

        if (params.toString()) {
            globalThis.history.replaceState(null, '', globalThis.location.pathname);
        }
    });