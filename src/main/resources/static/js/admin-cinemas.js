document.addEventListener('DOMContentLoaded', function () {
    const modal = document.getElementById('modalAddCinema');
    const preview = document.getElementById('imgPreview');
    const imageInput = document.querySelector('input[name="imageUrl"]');
    const title = document.getElementById('cinemaModalTitle');
    const icon = document.getElementById('cinemaModalIcon');
    const submitLabel = document.getElementById('cinemaSubmitLabel');

    if (!modal) {
        return;
    }

    function resetToAddMode() {
        const form = modal.querySelector('form');

        if (form) {
            form.reset();

            form.querySelectorAll('input:not([type="hidden"]), textarea').forEach(input => {
                input.value = '';
            });

            form.querySelectorAll('select').forEach(select => {
                select.selectedIndex = 0;
            });
        }

        const hiddenId = modal.querySelector('input[name="id"]');

        if (hiddenId) {
            hiddenId.remove();
        }

        if (preview) {
            preview.classList.add('d-none');
            preview.src = '';
        }

        if (title) {
            title.textContent = 'Ajouter un cinéma';
        }

        if (submitLabel) {
            submitLabel.textContent = 'Enregistrer';
        }

        if (icon) {
            icon.className = 'bi bi-plus-circle me-1';
        }
    }

    if (preview) {
        preview.addEventListener('error', function () {
            this.classList.add('d-none');
        });
    }

    if (imageInput && preview) {
        imageInput.addEventListener('input', function () {
            const value = this.value.trim();

            if (!value) {
                preview.classList.add('d-none');
                preview.src = '';
                return;
            }

            preview.src = value.startsWith('http') ? value : '/' + value;
            preview.classList.remove('d-none');
        });
    }

    modal.addEventListener('show.bs.modal', function (event) {
        const trigger = event.relatedTarget;

        if (trigger && trigger.dataset.mode === 'add') {
            resetToAddMode();
        }
    });

    if (modal.dataset.editMode === 'true') {
        bootstrap.Modal.getOrCreateInstance(modal).show();
    }
});