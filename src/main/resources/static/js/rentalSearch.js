
document.addEventListener('DOMContentLoaded', function() {
    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');
    const startTimeInput = document.getElementById('startTime');
    const endTimeInput = document.getElementById('endTime');
    const form = document.querySelector('form');

    // Set min date to today for both start and end date
    const today = new Date().toISOString().split('T')[0];
    startDateInput.setAttribute('min', today);
    endDateInput.setAttribute('min', today);

    form.addEventListener('submit', function(e) {
        const startDate = startDateInput.value;
        const endDate = endDateInput.value;
        const startTime = startTimeInput.value;
        const endTime = endTimeInput.value;
        const now = new Date();

        // 1. Start date and end date must not be in the past
        if (startDate < today) {
            Swal.fire({
                icon: "error",
                title: "SORRY...",
                text: "Start date cannot be in the past."
            });
            e.preventDefault();
            return;
        }
        if (endDate < today) {
            Swal.fire({
                icon: "error",
                title: "SORRY...",
                text: "End date cannot be in the past."
            });
            e.preventDefault();
            return;
        }

        // 2. End date must not be before start date
        if (endDate < startDate) {
            Swal.fire({
                icon: "error",
                title: "SORRY...",
                text: "End date cannot be before start date."
            });
            e.preventDefault();
            return;
        }

        // 3. If start date is today, start time must not be in the past
        if (startDate === today) {
            const nowTime = now.toTimeString().slice(0,5);
            if (startTime < nowTime) {
                Swal.fire({
                    icon: "error",
                    title: "SORRY...",
                    text: "Start time cannot be in the past."
                });
                e.preventDefault();
                return;
            }
        }

        // 4. If start date and end date are the same, end time must be at least 1 hour after start time
        if (startDate === endDate) {
            if (endTime <= startTime) {
                Swal.fire({
                    icon: "error",
                    title: "SORRY...",
                    text: "End time must be after start time."
                });
                e.preventDefault();
                return;
            }
            // Check at least 1 hour difference
            const [sh, sm] = startTime.split(':').map(Number);
            const [eh, em] = endTime.split(':').map(Number);
            const startMinutes = sh * 60 + sm;
            const endMinutes = eh * 60 + em;
            if (endMinutes - startMinutes < 60) {
                Swal.fire({
                    icon: "error",
                    title: "SORRY...",
                    text: "Rental duration must be at least 1 hour."
                });
                e.preventDefault();
                return;
            }
        }
    });
});
