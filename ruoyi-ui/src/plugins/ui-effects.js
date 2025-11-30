export default {
  install(Vue) {
    // Entry animations: fade in up (global), one by one
    const observer = new IntersectionObserver(entries => {
      entries.forEach((entry, idx) => {
        if (!entry.isIntersecting) return;
        const el = entry.target;
        const animation = 'animate-fade-in-up';
        el.style.animationDelay = `${idx * 120}ms`;
        el.classList.add(animation);
        observer.unobserve(el);
      });
    }, { threshold: 0.1 });

    Vue.directive('animate', {
      bind(el, binding) {
        el.dataset.animation = binding.value || 'animate-fade-in-up';
        observer.observe(el);
      }
    });

    // Card spotlight (flashlight)
    Vue.directive('spotlight', {
      bind(el) {
        el.classList.add('spotlight-card');
        el.addEventListener('mousemove', e => {
          const rect = el.getBoundingClientRect();
          const x = e.clientX - rect.left;
          const y = e.clientY - rect.top;
          el.style.setProperty('--mouse-x', `${x}px`);
          el.style.setProperty('--mouse-y', `${y}px`);
        });
      }
    });
  }
}
