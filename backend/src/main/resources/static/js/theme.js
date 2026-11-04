/**
 * Global Theme Switcher (Dark / Light Mode)
 * MS Admissions Matrix Engine
 *
 * Persists theme preference across sessions and seamlessly toggles between
 * modern matte black (zinc) and crisp clean white (slate) palettes.
 */

(function () {
  'use strict';

  const STORAGE_KEY = 'ms-admissions-theme';

  function getSavedTheme() {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved === 'light' || saved === 'dark') return saved;
    } catch (e) {
      console.warn('[Theme] localStorage access denied', e);
    }
    // Default to dark mode (modern matte black)
    return 'dark';
  }

  function applyTheme(theme) {
    const root = document.documentElement;
    root.setAttribute('data-theme', theme);

    try {
      localStorage.setItem(STORAGE_KEY, theme);
    } catch (e) {}

    // Update toggle buttons across the DOM
    updateToggleButtons(theme);

    // Notify components that might need re-render (charts, canvas)
    window.dispatchEvent(new CustomEvent('themechange', { detail: { theme } }));
  }

  function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') || 'dark';
    const next = current === 'dark' ? 'light' : 'dark';
    applyTheme(next);
  }

  function updateToggleButtons(theme) {
    const btns = document.querySelectorAll('.theme-toggle-btn, #themeToggleBtn');
    btns.forEach(btn => {
      const icon = btn.querySelector('.theme-toggle-icon');
      const label = btn.querySelector('.theme-toggle-label');

      if (theme === 'dark') {
        if (icon) icon.textContent = '☀️';
        if (label) label.textContent = 'Light';
        btn.setAttribute('title', 'Switch to Clean White (Light Theme)');
        btn.setAttribute('aria-label', 'Switch to Clean White theme');
      } else {
        if (icon) icon.textContent = '🌙';
        if (label) label.textContent = 'Dark';
        btn.setAttribute('title', 'Switch to Matte Black (Dark Theme)');
        btn.setAttribute('aria-label', 'Switch to Matte Black theme');
      }
    });
  }

  // Ensure button exists or attach listeners
  function initTheme() {
    const theme = getSavedTheme();
    applyTheme(theme);

    // Delegated click handler for theme toggle buttons
    document.addEventListener('click', (e) => {
      const btn = e.target.closest('.theme-toggle-btn, #themeToggleBtn');
      if (btn) {
        e.preventDefault();
        toggleTheme();
      }
    });
  }

  window.MS_Theme = {
    get: () => document.documentElement.getAttribute('data-theme') || 'dark',
    set: applyTheme,
    toggle: toggleTheme
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initTheme);
  } else {
    initTheme();
  }
})();
