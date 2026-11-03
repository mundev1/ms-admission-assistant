/**
 * api.js
 * API client module for the MS Admissions Matrix Engine.
 * Communicates with the Java Spring Boot backend at http://localhost:8080.
 */

const API_BASE = 'http://localhost:8080';

// ─── Helpers ────────────────────────────────────────────────────────────────

/**
 * Handle non-2xx responses by throwing a descriptive error.
 * @param {Response} response - Fetch API Response object
 * @throws {Error}
 */
async function handleApiError(response) {
  let message = `HTTP ${response.status}: ${response.statusText}`;
  try {
    const body = await response.json();
    if (body.message) message = body.message;
    else if (body.error) message = body.error;
  } catch (_) { /* use default message */ }
  const err = new Error(message);
  err.status = response.status;
  err.isApiError = true;
  throw err;
}

/**
 * Shared fetch wrapper with timeout, error handling and optional callbacks.
 * @param {string} url
 * @param {RequestInit} options
 * @param {{ onLoading?: (v: boolean) => void }} callbacks
 * @returns {Promise<any>}
 */
async function apiFetch(url, options = {}, callbacks = {}) {
  const controller = new AbortController();
  const timeoutId  = setTimeout(() => controller.abort(), 15_000); // 15 s timeout

  callbacks.onLoading?.(true);

  try {
    const response = await fetch(url, {
      headers: { 'Content-Type': 'application/json', ...options.headers },
      signal: controller.signal,
      ...options,
    });

    if (!response.ok) await handleApiError(response);

    // Handle 204 No Content
    if (response.status === 204) return null;

    return await response.json();
  } catch (err) {
    if (err.name === 'AbortError') {
      const timeoutErr = new Error('Request timed out after 15 seconds. Is the backend running?');
      timeoutErr.isTimeout = true;
      throw timeoutErr;
    }
    throw err;
  } finally {
    clearTimeout(timeoutId);
    callbacks.onLoading?.(false);
  }
}

// ─── API Functions ───────────────────────────────────────────────────────────

/**
 * Save an applicant profile to the backend.
 * @param {Object} profileData - Applicant form data
 * @param {{ onLoading?: (v: boolean) => void }} [callbacks]
 * @returns {Promise<Object>} Saved applicant with generated ID
 */
export async function saveApplicant(profileData, callbacks = {}) {
  return apiFetch(
    `${API_BASE}/api/applicants`,
    {
      method: 'POST',
      body: JSON.stringify(profileData),
    },
    callbacks
  );
}

/**
 * Score an applicant against all programs in a specific region.
 * @param {Object} profile - Applicant profile (must include ID if saved)
 * @param {string} region - 'US' | 'EU' | 'JP' | 'ALL'
 * @param {{ onLoading?: (v: boolean) => void }} [callbacks]
 * @returns {Promise<Object[]>} Array of program score results
 */
export async function scoreAgainstRegion(profile, region, callbacks = {}) {
  return apiFetch(
    `${API_BASE}/api/score/region/${encodeURIComponent(region)}`,
    {
      method: 'POST',
      body: JSON.stringify(profile),
    },
    callbacks
  );
}

/**
 * Score an applicant against a single specific program.
 * @param {Object} profile - Applicant profile
 * @param {string|number} programId - Program identifier
 * @param {{ onLoading?: (v: boolean) => void }} [callbacks]
 * @returns {Promise<Object>} Score result for the program
 */
export async function scoreAgainstProgram(profile, programId, callbacks = {}) {
  return apiFetch(
    `${API_BASE}/api/score/program/${encodeURIComponent(programId)}`,
    {
      method: 'POST',
      body: JSON.stringify(profile),
    },
    callbacks
  );
}

/**
 * Retrieve all available programs from the backend.
 * @param {{ region?: string, specialization?: string }} [filters]
 * @param {{ onLoading?: (v: boolean) => void }} [callbacks]
 * @returns {Promise<Object[]>} Array of program objects
 */
export async function getAllPrograms(filters = {}, callbacks = {}) {
  const params = new URLSearchParams();
  if (filters.region)         params.set('region', filters.region);
  if (filters.specialization) params.set('specialization', filters.specialization);
  const qs = params.toString() ? `?${params}` : '';
  return apiFetch(`${API_BASE}/api/programs${qs}`, { method: 'GET' }, callbacks);
}

/**
 * Fetch a single program by ID.
 * @param {string|number} programId
 * @returns {Promise<Object>}
 */
export async function getProgramById(programId) {
  return apiFetch(`${API_BASE}/api/programs/${encodeURIComponent(programId)}`, { method: 'GET' });
}

/**
 * Get score history for a saved applicant.
 * @param {string|number} applicantId
 * @returns {Promise<Object[]>}
 */
export async function getApplicantHistory(applicantId) {
  return apiFetch(`${API_BASE}/api/applicants/${encodeURIComponent(applicantId)}/history`, { method: 'GET' });
}

// ─── Convenience: full scoring pipeline ─────────────────────────────────────

/**
 * Run the full pipeline: save applicant → score against region(s).
 * Falls back gracefully if the backend is unavailable.
 * @param {Object} profileData
 * @param {string} region
 * @param {{ onLoading?: (v: boolean) => void, onError?: (err: Error) => void }} callbacks
 * @returns {Promise<{ applicant: Object|null, results: Object[] }>}
 */
export async function runScoringPipeline(profileData, region, callbacks = {}) {
  let applicant = null;

  try {
    applicant = await saveApplicant(profileData, {
      onLoading: callbacks.onLoading,
    });
  } catch (err) {
    console.warn('[API] saveApplicant failed, continuing with local profile:', err.message);
    callbacks.onError?.(err);
    // Use local profile as fallback
    applicant = { ...profileData, id: `local_${Date.now()}` };
  }

  const profileToScore = applicant ?? profileData;
  const targetRegion = (!region || region === 'ALL') ? 'ALL' : region;

  let allResults = [];

  try {
    const results = await scoreAgainstRegion(profileToScore, targetRegion, {
      onLoading: callbacks.onLoading,
    });
    allResults = results || [];
  } catch (err) {
    console.warn(`[API] scoreAgainstRegion(${targetRegion}) failed:`, err.message);
    callbacks.onError?.(err);
    // Caller will handle missing results (e.g., show mock data)
  }

  return { applicant, results: allResults };
}

// ─── Timeline & Visa Tracker Functions ──────────────────────────────────────

/**
 * Fetch supported university intake cycles.
 * @param {{ onLoading?: (v: boolean) => void }} [callbacks]
 * @returns {Promise<Object[]>}
 */
export async function getIntakes(callbacks = {}) {
  return apiFetch(`${API_BASE}/api/timeline/intakes`, { method: 'GET' }, callbacks);
}

/**
 * Calculate backwards-scheduled deadlines and visa milestones.
 * @param {Object} timelineRequest
 * @param {{ onLoading?: (v: boolean) => void }} [callbacks]
 * @returns {Promise<Object>} TimelineSchedule
 */
export async function planTimeline(timelineRequest, callbacks = {}) {
  return apiFetch(
    `${API_BASE}/api/timeline/plan`,
    {
      method: 'POST',
      body: JSON.stringify(timelineRequest),
    },
    callbacks
  );
}

/**
 * Trigger browser download of the .ics calendar file.
 * @param {Object} timelineRequest
 * @param {string} [filename]
 */
export async function downloadTimelineIcs(timelineRequest, filename = 'ms-admissions-timeline.ics') {
  const resp = await fetch(`${API_BASE}/api/timeline/export/ics`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(timelineRequest),
  });
  if (!resp.ok) {
    throw new Error(`Failed to export ICS: HTTP ${resp.status}`);
  }
  const blob = await resp.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  setTimeout(() => {
    window.URL.revokeObjectURL(url);
    a.remove();
  }, 1000);
}

// Export handleApiError so callers can re-use it
export { handleApiError };
