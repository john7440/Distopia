document.addEventListener('DOMContentLoaded', function () {
            const selectAll = document.getElementById('selectAllSeances');
            const checkboxes = document.querySelectorAll('.seance-checkbox');

            if (!selectAll) return;

            selectAll.addEventListener('change', function () {
                checkboxes.forEach(checkbox => {
                    checkbox.checked = selectAll.checked;
                });
            });

            checkboxes.forEach(checkbox => {
                checkbox.addEventListener('change', function () {
                    const checkedCount = document.querySelectorAll('.seance-checkbox:checked').length;
                    selectAll.checked = checkedCount === checkboxes.length && checkboxes.length > 0;
                });
            });
        });