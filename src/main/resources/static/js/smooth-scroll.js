// Smooth scroll for navbar links with scroll spy
(function () {
  'use strict';

  const navLinks = document.querySelectorAll('.nav-link');

  // Smooth scroll handler
  navLinks.forEach((link) => {
    link.addEventListener('click', function (e) {
      const href = this.getAttribute('href');

      // Only handle anchor links (starts with #)
      if (href && href.startsWith('#')) {
        e.preventDefault();
        const targetId = href.substring(1);

        // Handle href="#" (Beranda) - scroll to top
        if (targetId === '') {
          window.scrollTo({
            top: 0,
            behavior: 'smooth',
          });
          return;
        }

        const target = document.getElementById(targetId);

        if (target) {
          const headerOffset = 80;
          const elementPosition = target.getBoundingClientRect().top;
          const offsetPosition =
            elementPosition + window.pageYOffset - headerOffset;

          window.scrollTo({
            top: offsetPosition,
            behavior: 'smooth',
          });
        }
      }
    });
  });

  // Scroll spy - update active link based on scroll position
  function updateActiveLink() {
    const sections = document.querySelectorAll('section[id]');
    const scrollPos = window.scrollY + 100;

    let activeFound = false;

    sections.forEach((section) => {
      const sectionTop = section.offsetTop;
      const sectionHeight = section.offsetHeight;
      const sectionId = section.getAttribute('id');

      if (
        !activeFound &&
        scrollPos >= sectionTop &&
        scrollPos < sectionTop + sectionHeight
      ) {
        navLinks.forEach((link) => {
          link.classList.remove('active');
          const linkHref = link.getAttribute('href');
          if (linkHref === '#' + sectionId || linkHref === '#' + sectionId) {
            link.classList.add('active');
            activeFound = true;
          }
        });
      }
    });

    // If at top of page, set first link as active
    if (scrollPos < 200) {
      navLinks.forEach((link) => link.classList.remove('active'));
      const firstLink = document.querySelector('.nav-link[href="#"]');
      if (firstLink) firstLink.classList.add('active');
    }
  }

  // Listen for scroll events
  let ticking = false;
  window.addEventListener('scroll', function () {
    if (!ticking) {
      window.requestAnimationFrame(function () {
        updateActiveLink();
        ticking = false;
      });
      ticking = true;
    }
  });

  // Initialize on page load
  window.addEventListener('load', updateActiveLink);
})();
