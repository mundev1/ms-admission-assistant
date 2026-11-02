/**
 * normalizer.js
 * Client-side GPA & score normalization utilities.
 * Mirrors the Java ScaleNormalizer logic.
 * ES6 module exports.
 */

// ─── GPA Conversions ───────────────────────────────────────────────────────

/**
 * Convert a 10-point CGPA to US 4.0 scale (simple linear).
 * @param {number} cgpa10 - CGPA on a 0–10 scale
 * @returns {number} GPA on 0–4.0 scale
 */
export function cgpaToUS4pt(cgpa10) {
  const clamped = Math.min(10, Math.max(0, cgpa10));
  return parseFloat(((clamped / 10.0) * 4.0).toFixed(2));
}

/**
 * Convert 10-point CGPA to Japanese 4.0 scale using piecewise mapping.
 * Japanese universities use a stricter conversion.
 * @param {number} cgpa10
 * @returns {number}
 */
export function cgpaToJapanese4pt(cgpa10) {
  const c = Math.min(10, Math.max(0, cgpa10));
  let result;
  if (c >= 9.0)       result = 4.0;
  else if (c >= 8.0)  result = 3.5 + ((c - 8.0) / 2.0);
  else if (c >= 7.0)  result = 3.0 + ((c - 7.0) / 2.0);
  else if (c >= 6.0)  result = 2.5 + ((c - 6.0) / 2.0);
  else if (c >= 5.0)  result = 2.0 + ((c - 5.0) / 2.0);
  else if (c >= 4.0)  result = 1.0 + ((c - 4.0));
  else                result = (c / 4.0);
  return parseFloat(Math.min(4.0, result).toFixed(2));
}

/**
 * Convert 10-point CGPA to European ECTS letter grade band.
 * @param {number} cgpa10
 * @returns {{ letter: string, score: number, label: string }}
 */
export function cgpaToEctsGrade(cgpa10) {
  const c = Math.min(10, Math.max(0, cgpa10));
  const bands = [
    { min: 9.0, letter: 'A', score: 95, label: 'Excellent' },
    { min: 8.0, letter: 'B', score: 82, label: 'Very Good' },
    { min: 7.0, letter: 'C', score: 70, label: 'Good' },
    { min: 6.0, letter: 'D', score: 58, label: 'Satisfactory' },
    { min: 5.0, letter: 'E', score: 48, label: 'Sufficient' },
    { min: 0,   letter: 'F', score: 20, label: 'Fail' },
  ];
  const band = bands.find(b => c >= b.min);
  // Interpolate within the band for a finer score
  const nextBandMin = bands[bands.indexOf(band) - 1]?.min ?? 10;
  const rangeSize   = nextBandMin - band.min;
  const nextScore   = bands[bands.indexOf(band) - 1]?.score ?? 100;
  const fraction    = rangeSize > 0 ? (c - band.min) / rangeSize : 0;
  const fineScore   = Math.round(band.score + fraction * (nextScore - band.score));
  return { letter: band.letter, score: fineScore, label: band.label };
}

// ─── GRE Percentile Estimation ─────────────────────────────────────────────

/**
 * Lookup tables for GRE percentile estimation (ETS 2023 data).
 * Key = raw score, value = approximate percentile rank.
 */
const GRE_V_PERCENTILES = {
  170: 99, 169: 99, 168: 98, 167: 98, 166: 97, 165: 96, 164: 95, 163: 93, 162: 91,
  161: 88, 160: 86, 159: 83, 158: 79, 157: 75, 156: 71, 155: 67, 154: 63, 153: 58,
  152: 54, 151: 49, 150: 45, 149: 40, 148: 36, 147: 32, 146: 28, 145: 25, 144: 21,
  143: 18, 142: 15, 141: 13, 140: 10, 139: 8, 138: 6, 137: 5, 136: 4, 135: 3,
  134: 2, 133: 2, 132: 1, 131: 1, 130: 1,
};

const GRE_Q_PERCENTILES = {
  170: 96, 169: 93, 168: 91, 167: 88, 166: 86, 165: 84, 164: 82, 163: 79, 162: 76,
  161: 73, 160: 70, 159: 66, 158: 62, 157: 58, 156: 54, 155: 50, 154: 46, 153: 42,
  152: 38, 151: 34, 150: 30, 149: 26, 148: 22, 147: 19, 146: 16, 145: 13, 144: 11,
  143: 9, 142: 7, 141: 5, 140: 4, 139: 3, 138: 2, 137: 1, 136: 1, 135: 1,
  134: 1, 133: 1, 132: 1, 131: 1, 130: 1,
};

const GRE_AW_PERCENTILES = {
  6.0: 99, 5.5: 98, 5.0: 92, 4.5: 80, 4.0: 60, 3.5: 42, 3.0: 25, 2.5: 11,
  2.0: 5, 1.5: 2, 1.0: 1, 0.5: 1, 0: 1,
};

/**
 * Estimate GRE percentile from raw score.
 * @param {number} rawScore - integer (130-170 for V/Q) or float (0-6 for AW)
 * @param {'V'|'Q'|'AW'} type - score type
 * @returns {{ percentile: number, label: string }}
 */
export function greToPercentile(rawScore, type) {
  let table;
  if (type === 'V')       table = GRE_V_PERCENTILES;
  else if (type === 'Q')  table = GRE_Q_PERCENTILES;
  else if (type === 'AW') table = GRE_AW_PERCENTILES;
  else return { percentile: 0, label: 'Unknown' };

  const score = type === 'AW' ? parseFloat(rawScore) : parseInt(rawScore, 10);
  let percentile = table[score];

  if (percentile === undefined) {
    // Interpolate between nearest keys
    const keys = Object.keys(table).map(Number).sort((a, b) => b - a);
    const higher = keys.find(k => k <= score);
    const lower  = keys.find(k => k >= score);
    if (higher !== undefined) percentile = table[higher];
    else if (lower !== undefined) percentile = table[lower];
    else percentile = 1;
  }

  let label;
  if (percentile >= 90)      label = 'Top 10%';
  else if (percentile >= 75) label = 'Top 25%';
  else if (percentile >= 50) label = 'Top 50%';
  else if (percentile >= 25) label = 'Bottom 50%';
  else                       label = 'Low';

  return { percentile, label };
}

// ─── JLPT Score ────────────────────────────────────────────────────────────

/**
 * Convert JLPT level string to a normalized score 0–1.
 * @param {string} level - 'N1'|'N2'|'N3'|'N4'|'N5'|'None'
 * @returns {number}
 */
export function jlptToScore(level) {
  const map = { N1: 1.0, N2: 0.8, N3: 0.6, N4: 0.4, N5: 0.2, None: 0.0 };
  return map[level] ?? 0.0;
}

/**
 * Convert JLPT level to a descriptive label.
 * @param {string} level
 * @returns {string}
 */
export function jlptToLabel(level) {
  const map = {
    N1: 'Native-level proficiency (N1)',
    N2: 'Business proficiency (N2)',
    N3: 'Intermediate (N3)',
    N4: 'Basic reading (N4)',
    N5: 'Beginner (N5)',
    None: 'No JLPT certification',
  };
  return map[level] ?? 'Unknown';
}

// ─── TOEFL / IELTS helpers ─────────────────────────────────────────────────

/**
 * Normalize TOEFL score (0–120) to 0–1.
 * @param {number} toefl
 * @returns {number}
 */
export function toeflToNorm(toefl) {
  return Math.min(1, Math.max(0, toefl / 120));
}

/**
 * Normalize IELTS score (0–9) to 0–1.
 * @param {number} ielts
 * @returns {number}
 */
export function ieltsToNorm(ielts) {
  return Math.min(1, Math.max(0, ielts / 9));
}

/**
 * Return the better of TOEFL/IELTS normalized scores.
 * @param {number} toefl
 * @param {number} ielts
 * @returns {number}
 */
export function bestLanguageScore(toefl, ielts) {
  return Math.max(toeflToNorm(toefl), ieltsToNorm(ielts));
}

// ─── GRE composite normalization ───────────────────────────────────────────

/**
 * Normalize GRE Verbal (130–170) to 0–1.
 * @param {number} v
 * @returns {number}
 */
export function greVToNorm(v) {
  return Math.min(1, Math.max(0, (v - 130) / 40));
}

/**
 * Normalize GRE Quant (130–170) to 0–1.
 * @param {number} q
 * @returns {number}
 */
export function greQToNorm(q) {
  return Math.min(1, Math.max(0, (q - 130) / 40));
}

/**
 * Normalize GRE AW (0–6) to 0–1.
 * @param {number} aw
 * @returns {number}
 */
export function greAWToNorm(aw) {
  return Math.min(1, Math.max(0, aw / 6));
}

// ─── Research Score ─────────────────────────────────────────────────────────

/**
 * Compute a composite research score (0–100) from raw inputs.
 * @param {number} researchScore   - self-rated 0–100
 * @param {number} internshipMonths
 * @param {number} publications
 * @returns {number} composite 0–100
 */
export function computeResearchComposite(researchScore, internshipMonths, publications) {
  const pubScore   = Math.min(100, publications * 20);
  const intScore   = Math.min(100, internshipMonths * 8.33);
  const composite  = researchScore * 0.5 + intScore * 0.3 + pubScore * 0.2;
  return Math.round(Math.min(100, composite));
}

// ─── ECTS normalization ──────────────────────────────────────────────────────

/**
 * Normalize ECTS credits in CS (0–300) to 0–1 suitability score.
 * Most EU programs expect 180–240 ECTS.
 * @param {number} ects
 * @returns {number}
 */
export function ectsToNorm(ects) {
  if (ects >= 240) return 1.0;
  if (ects >= 180) return 0.8 + ((ects - 180) / 300);
  if (ects >= 120) return 0.5 + ((ects - 120) / 200);
  return Math.max(0, ects / 240);
}

// ─── Region score summary ────────────────────────────────────────────────────

/**
 * Build a normalized score object for a given applicant profile.
 * Returns components on a 0–100 scale for display.
 * @param {Object} p - applicant profile
 * @param {'US'|'EU'|'JP'} region
 * @returns {Object} score components
 */
export function buildScoreComponents(p, region) {
  const gpa10 = parseFloat(p.cgpa) || 0;

  let gpaScore;
  if (region === 'EU') {
    gpaScore = cgpaToEctsGrade(gpa10).score;
  } else if (region === 'JP') {
    gpaScore = (cgpaToJapanese4pt(gpa10) / 4) * 100;
  } else {
    gpaScore = cgpaToUS4pt(gpa10) / 4 * 100;
  }

  const greQ     = greQToNorm(parseFloat(p.greQuant) || 130) * 100;
  const greV     = greVToNorm(parseFloat(p.greVerbal) || 130) * 100;
  const lang     = bestLanguageScore(parseFloat(p.toefl) || 0, parseFloat(p.ielts) || 0) * 100;
  const research = computeResearchComposite(
    parseFloat(p.researchScore) || 0,
    parseFloat(p.internshipMonths) || 0,
    parseInt(p.publications, 10) || 0
  );
  const ects     = region === 'EU'
    ? ectsToNorm(parseFloat(p.ectsCredits) || 0) * 100
    : null;
  const jlpt     = region === 'JP'
    ? jlptToScore(p.jlptLevel || 'None') * 100
    : null;

  return {
    gpaScore:  Math.round(gpaScore),
    greQ:      Math.round(greQ),
    greV:      Math.round(greV),
    lang:      Math.round(lang),
    research:  Math.round(research),
    ects:      ects !== null ? Math.round(ects) : null,
    jlpt:      jlpt !== null ? Math.round(jlpt) : null,
  };
}
