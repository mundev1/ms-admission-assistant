/**
 * Global Navigation Sidebar Drawer (3-Lines Hamburger Menu)
 * MS Admissions Matrix Engine
 *
 * Provides a clean, modern, slide-out navigation menu housing all core features,
 * tools, active applicant profile summary, and documentation links.
 */

(function () {
  'use strict';

  const PAGES = [
    {
      id: 'index',
      href: 'index.html',
      title: 'Profile Intake & Evaluator',
      desc: 'Indian college selector, GPA normalizer & test scores',
      icon: '📝',
      badge: 'Step 1'
    },
    {
      id: 'dashboard',
      href: 'dashboard.html',
      title: 'Program Matches Dashboard',
      desc: 'Admissions matrix, multi-criteria ranking & decision cards',
      icon: '📊',
      badge: 'Core'
    },
    {
      id: 'scholarships',
      href: 'scholarships.html',
      title: 'Global Scholarship Radar',
      desc: 'DAAD, MEXT, JASSO, Fulbright-Nehru & Erasmus Mundus',
      icon: '💰',
      badge: 'Radar'
    },
    {
      id: 'faculty-critic',
      href: 'faculty-critic.html',
      title: 'Faculty & Lab Critic',
      desc: 'TF-IDF NLP statement & lab publication alignment',
      icon: '🔬',
      badge: 'Critic'
    },
    {
      id: 'timeline',
      href: 'timeline.html',
      title: 'Visa & Deadline Timeline',
      desc: 'Backward-scheduled milestones, APS & VFS backlogs',
      icon: '📅',
      badge: 'Visa'
    },
    {
      id: 'ects-screener',
      href: 'ects-screener.html',
      title: 'German ECTS Pre-Screener',
      desc: 'AICTE credit hours to ECTS & TUM/RWTH prerequisite gap audits',
      icon: '📐',
      badge: 'German'
    }
  ];

  function getCurrentPageId() {
    const path = window.location.pathname.split('/').pop().toLowerCase() || 'index.html';
    for (const page of PAGES) {
      if (path === page.href.toLowerCase() || (page.id === 'index' && (path === '' || path === '/'))) {
        return page.id;
      }
    }
    return '';
  }

  function getStoredApplicant() {
    try {
      const keys = ['applicantData', 'applicantProfile', 'scoringResults'];
      for (const key of keys) {
        const item = sessionStorage.getItem(key) || localStorage.getItem(key);
        if (item) {
          const parsed = JSON.parse(item);
          if (parsed && (parsed.applicant || parsed.fullName || parsed.institution)) {
            return parsed.applicant || parsed;
          }
        }
      }
    } catch (e) {
      // ignore
    }
    return null;
  }

  function buildDrawerMarkup() {
    const currentId = getCurrentPageId();
    const applicant = getStoredApplicant();

    let profileHtml = '';
    if (applicant && (applicant.fullName || applicant.institution || applicant.collegeName)) {
      const name = applicant.fullName || 'Student Applicant';
      const college = applicant.institution || applicant.collegeName || 'Indian University';
      const gpa = applicant.gpa ? `${applicant.gpa} / ${applicant.gpaScale || '10.0'}` : (applicant.normalizedGpa ? `${applicant.normalizedGpa.toFixed(1)}/4.0` : 'Assessed');
      const branch = applicant.specialization || applicant.major || 'Computer Science';

      profileHtml = `
        <div class="drawer-profile-card">
          <div class="drawer-profile-header">
            <span class="drawer-profile-dot active"></span>
            <span class="drawer-profile-label">Active Profile Loaded</span>
          </div>
          <div class="drawer-profile-name">${escapeHtml(name)}</div>
          <div class="drawer-profile-details">${escapeHtml(college)} • ${escapeHtml(branch)} • GPA: <strong>${escapeHtml(gpa)}</strong></div>
        </div>
      `;
    } else {
      profileHtml = `
        <div class="drawer-profile-card">
          <div class="drawer-profile-header">
            <span class="drawer-profile-dot"></span>
            <span class="drawer-profile-label">Applicant Profile</span>
          </div>
          <div class="drawer-profile-name">Guest Profile</div>
          <div class="drawer-profile-details">Complete intake form to unlock personalized match scores. <a href="index.html" class="drawer-profile-link">Start Evaluation →</a></div>
        </div>
      `;
    }

    const navItemsHtml = PAGES.map(p => {
      const isActive = p.id === currentId;
      return `
        <a href="${p.href}" class="drawer-nav-item ${isActive ? 'active' : ''}" ${isActive ? 'aria-current="page"' : ''}>
          <div class="drawer-item-icon">${p.icon}</div>
          <div class="drawer-item-content">
            <div class="drawer-item-title-row">
              <span class="drawer-item-title">${p.title}</span>
              ${p.badge ? `<span class="drawer-badge ${p.badge.toLowerCase()}">${p.badge}</span>` : ''}
            </div>
            <div class="drawer-item-desc">${p.desc}</div>
          </div>
          <span class="drawer-item-arrow" aria-hidden="true">${isActive ? '●' : '→'}</span>
        </a>
      `;
    }).join('');

    return `
      <div class="app-nav-backdrop" id="appNavBackdrop" aria-hidden="true"></div>
      <aside class="app-nav-drawer" id="appNavDrawer" aria-label="Main Navigation Menu" aria-hidden="true">
        <!-- Drawer Header -->
        <div class="app-nav-drawer-header">
          <div class="drawer-brand">
            <span class="drawer-brand-icon">🎓</span>
            <div class="drawer-brand-text">
              <div class="drawer-brand-title">MS Admissions Engine</div>
              <div class="drawer-brand-subtitle">Decision Intelligence Suite</div>
            </div>
          </div>
          <button type="button" class="drawer-close-btn" id="closeNavDrawerBtn" aria-label="Close navigation menu" title="Close Menu (Esc)">
            ✕
          </button>
        </div>

        <!-- Drawer Body -->
        <div class="app-nav-drawer-body">
          ${profileHtml}

          <div class="drawer-nav-group">
            <div class="drawer-group-title">Applications &amp; Tool Suite</div>
            <nav class="drawer-nav-list" aria-label="Feature navigation">
              ${navItemsHtml}
            </nav>
          </div>

          <div class="drawer-nav-group">
            <div class="drawer-group-title">Quick Actions &amp; Controls</div>
            <div class="drawer-shortcuts-grid">
              <button type="button" class="drawer-shortcut-btn" id="drawerSpotlightBtn" title="Open Global Search (⌘K)">
                <span class="drawer-shortcut-icon">🔍</span>
                <div class="drawer-shortcut-text">
                  <span class="drawer-shortcut-name">Quick Search</span>
                  <kbd class="drawer-kbd">⌘K</kbd>
                </div>
              </button>
              <button type="button" class="drawer-shortcut-btn theme-toggle-btn" id="drawerThemeBtn" title="Toggle Theme (Dark / Light)">
                <span class="drawer-shortcut-icon theme-toggle-icon">☀️</span>
                <div class="drawer-shortcut-text">
                  <span class="drawer-shortcut-name">Theme</span>
                  <span class="drawer-kbd theme-toggle-label">Light</span>
                </div>
              </button>
            </div>
          </div>

          <div class="drawer-nav-group">
            <div class="drawer-group-title">Documentation &amp; Source</div>
            <div class="drawer-links-list">
              <a href="https://app.notion.com/p/40de904a35e681bca291c532dfa1ce59?pvs=204" target="_blank" rel="noopener noreferrer" class="drawer-link-row">
                <span class="drawer-link-text">📖 Notion Architecture Spec</span>
                <span class="drawer-ext-icon">↗</span>
              </a>
              <a href="https://github.com/mundev1/ms-admission-assistant" target="_blank" rel="noopener noreferrer" class="drawer-link-row">
                <span class="drawer-link-text">🐙 GitHub Repository</span>
                <span class="drawer-ext-icon">↗</span>
              </a>
            </div>
          </div>
        </div>

        <!-- Drawer Footer -->
        <div class="app-nav-drawer-footer">
          <div class="drawer-status-pill">
            <span class="status-pulse-dot"></span>
            <span>All Systems Operational • v2.5</span>
          </div>
        </div>
      </aside>
    `;
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  let isInitialized = false;

  function ensureDrawerInDOM() {
    let drawer = document.getElementById('appNavDrawer');
    let backdrop = document.getElementById('appNavBackdrop');

    if (!drawer || !backdrop) {
      const container = document.createElement('div');
      container.id = 'appNavWrapper';
      container.innerHTML = buildDrawerMarkup();
      document.body.appendChild(container);
    }
  }

  function openDrawer() {
    ensureDrawerInDOM();
    const drawer = document.getElementById('appNavDrawer');
    const backdrop = document.getElementById('appNavBackdrop');
    const toggleBtns = document.querySelectorAll('.nav-menu-btn, #navMenuBtn');

    if (!drawer || !backdrop) return;

    drawer.classList.add('open');
    backdrop.classList.add('visible');
    drawer.setAttribute('aria-hidden', 'false');
    backdrop.setAttribute('aria-hidden', 'false');

    toggleBtns.forEach(btn => {
      btn.classList.add('active');
      btn.setAttribute('aria-expanded', 'true');
    });

    document.body.classList.add('nav-drawer-open');

    // Focus close button or first link for keyboard accessibility
    setTimeout(() => {
      const closeBtn = document.getElementById('closeNavDrawerBtn');
      if (closeBtn) closeBtn.focus();
    }, 50);
  }

  function closeDrawer() {
    const drawer = document.getElementById('appNavDrawer');
    const backdrop = document.getElementById('appNavBackdrop');
    const toggleBtns = document.querySelectorAll('.nav-menu-btn, #navMenuBtn');

    if (drawer) {
      drawer.classList.remove('open');
      drawer.setAttribute('aria-hidden', 'true');
    }
    if (backdrop) {
      backdrop.classList.remove('visible');
      backdrop.setAttribute('aria-hidden', 'true');
    }

    toggleBtns.forEach(btn => {
      btn.classList.remove('active');
      btn.setAttribute('aria-expanded', 'false');
    });

    document.body.classList.remove('nav-drawer-open');
  }

  function toggleDrawer() {
    const drawer = document.getElementById('appNavDrawer');
    if (drawer && drawer.classList.contains('open')) {
      closeDrawer();
    } else {
      openDrawer();
    }
  }

  function setupEventListeners() {
    if (isInitialized) return;
    isInitialized = true;

    // Delegated click for 3-lines menu button
    document.addEventListener('click', (e) => {
      const menuBtn = e.target.closest('.nav-menu-btn, #navMenuBtn');
      if (menuBtn) {
        e.preventDefault();
        e.stopPropagation();
        toggleDrawer();
        return;
      }

      const closeBtn = e.target.closest('.drawer-close-btn, #closeNavDrawerBtn');
      if (closeBtn) {
        e.preventDefault();
        closeDrawer();
        return;
      }

      const backdrop = e.target.closest('.app-nav-backdrop, #appNavBackdrop');
      if (backdrop) {
        e.preventDefault();
        closeDrawer();
        return;
      }

      // Drawer Spotlight trigger
      const spotlightBtn = e.target.closest('#drawerSpotlightBtn');
      if (spotlightBtn) {
        e.preventDefault();
        closeDrawer();
        setTimeout(() => {
          if (window.MS_Spotlight) {
            window.MS_Spotlight.open();
          } else {
            const staticBtn = document.getElementById('spotlightTriggerBtn');
            if (staticBtn) staticBtn.click();
          }
        }, 150);
        return;
      }
    });

    // Keyboard support: Escape closes drawer, Alt+M toggles
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        const drawer = document.getElementById('appNavDrawer');
        if (drawer && drawer.classList.contains('open')) {
          e.preventDefault();
          closeDrawer();
          const firstMenuBtn = document.querySelector('.nav-menu-btn, #navMenuBtn');
          if (firstMenuBtn) firstMenuBtn.focus();
        }
      } else if ((e.altKey && (e.key === 'm' || e.key === 'M')) || (e.ctrlKey && e.key === 'm')) {
        e.preventDefault();
        toggleDrawer();
      }
    });
  }

  function init() {
    ensureDrawerInDOM();
    setupEventListeners();
  }

  window.MS_NavDrawer = {
    open: openDrawer,
    close: closeDrawer,
    toggle: toggleDrawer,
    isOpen: () => {
      const drawer = document.getElementById('appNavDrawer');
      return !!(drawer && drawer.classList.contains('open'));
    }
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
