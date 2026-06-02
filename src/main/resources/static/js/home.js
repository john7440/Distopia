  function scrollSlider(sliderId, direction) {
            const slider = document.getElementById(sliderId);

            if (!slider) {
                return;
            }

            const scrollAmount = slider.clientWidth * 0.85;

            slider.scrollBy({
                left: direction * scrollAmount,
                behavior: 'smooth'
            });
        }