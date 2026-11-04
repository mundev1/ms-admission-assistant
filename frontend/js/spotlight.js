/**
 * Global Spotlight / Command Palette (Cmd + K / Ctrl + K)
 * MS Admissions Matrix Engine
 *
 * Provides instant global keyboard navigation, university search,
 * scholarship discovery, and tool page switching across the entire platform.
 */

(function () {
  'use strict';

  // ── 1. Comprehensive Search Catalog ──────────────────────────────────────────

  const SPOTLIGHT_TOOLS = [
    {
      id: 'tool-dashboard',
      type: 'TOOL',
      shortcut: '1',
      title: 'Program Matches & Holistic Admissions Calculator',
      description: 'Cross-border matching engine blending academic fit, financial viability, and post-study visa outlook.',
      icon: '📊',
      badge: 'Tool Page',
      url: 'dashboard.html',
      keywords: ['dashboard', 'programs', 'calculator', 'matches', 'admissions', 'cgpa', 'scores']
    },
    {
      id: 'tool-scholarships',
      type: 'TOOL',
      shortcut: '2',
      title: 'Global Scholarship & Assistantship Radar',
      description: 'Auto-match DAAD, MEXT, Fulbright, Erasmus Mundus, and university TA/RA fee remissions.',
      icon: '💰',
      badge: 'Tool Page',
      url: 'scholarships.html',
      keywords: ['scholarships', 'funding', 'daad', 'mext', 'fulbright', 'grants', 'assistantships', 'epos', 'radar']
    },
    {
      id: 'tool-faculty',
      type: 'TOOL',
      shortcut: '3',
      title: 'Faculty & Lab Alignment Critic (Naidaku & SOP)',
      description: 'Semantic matching between research statement/proposal and professor arXiv/IEEE publications.',
      icon: '🔬',
      badge: 'Tool Page',
      url: 'faculty-critic.html',
      keywords: ['faculty', 'critic', 'lab', 'professor', 'naidaku', 'sop', 'proposal', 'nlp', 'research']
    },
    {
      id: 'tool-timeline',
      type: 'TOOL',
      shortcut: '4',
      title: 'Visa Appointment & Application Deadline Timeline Tracker',
      description: 'Backwards-scheduled milestone generator factoring VFS/consulate slots, APS, and blocked accounts.',
      icon: '📅',
      badge: 'Tool Page',
      url: 'timeline.html',
      keywords: ['timeline', 'visa', 'deadlines', 'vfs', 'aps', 'blocked account', 'tracker', 'milestones']
    },
    {
      id: 'tool-ects',
      type: 'TOOL',
      shortcut: '5',
      title: 'German ECTS Credit Equivalence & Prerequisite Screener',
      description: 'Convert Indian credits to European ECTS and identify curriculum gaps against TUM, RWTH, Stuttgart.',
      icon: '📐',
      badge: 'Tool Page',
      url: 'ects-screener.html',
      keywords: ['ects', 'germany', 'tum', 'rwth', 'curriculum', 'prerequisite', 'screener', 'auflagen', 'aicte']
    },
    {
      id: 'tool-profile',
      type: 'TOOL',
      shortcut: '6',
      title: 'Candidate Profile & AICTE College Questionnaire',
      description: 'Update undergraduate degree, CGPA scale, English proficiency, and regional preferences.',
      icon: '✏️',
      badge: 'Profile',
      url: 'index.html',
      keywords: ['profile', 'candidate', 'edit', 'questionnaire', 'gpa', 'gre', 'toefl', 'coer', 'aicte']
    }
  ];

  const SPOTLIGHT_UNIVERSITIES = [
    {
      id: 'uni-tum',
      type: 'UNIVERSITY',
      country: 'Germany',
      region: 'EU',
      title: 'Technical University of Munich (TUM)',
      subtitle: 'Germany · MSc Informatics / Data Engineering · 18m EU Blue Card',
      flag: '🇩🇪',
      targetParam: 'Technical University of Munich (TUM)',
      ectsId: 'TUM',
      keywords: ['tum', 'munich', 'germany', 'deutschland', 'informatics', 'data engineering', 'bavaria'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=TUM' },
        { label: '📐 ECTS Screener', url: 'ects-screener.html?uni=TUM' },
        { label: '💰 Scholarships', url: 'scholarships.html?country=Germany&uni=TUM' },
        { label: '📅 Timeline', url: 'timeline.html?country=Germany&uni=TUM' }
      ]
    },
    {
      id: 'uni-rwth',
      type: 'UNIVERSITY',
      country: 'Germany',
      region: 'EU',
      title: 'RWTH Aachen University',
      subtitle: 'Germany · MSc Computer Science · Top TU9 Technical University',
      flag: '🇩🇪',
      targetParam: 'RWTH Aachen University',
      ectsId: 'RWTH_AACHEN',
      keywords: ['rwth', 'aachen', 'germany', 'tu9', 'computer science', 'nrw'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=RWTH%20Aachen' },
        { label: '📐 ECTS Screener', url: 'ects-screener.html?uni=RWTH_AACHEN' },
        { label: '💰 Scholarships', url: 'scholarships.html?country=Germany&uni=RWTH%20Aachen' },
        { label: '📅 Timeline', url: 'timeline.html?country=Germany&uni=RWTH%20Aachen' }
      ]
    },
    {
      id: 'uni-stuttgart',
      type: 'UNIVERSITY',
      country: 'Germany',
      region: 'EU',
      title: 'University of Stuttgart',
      subtitle: 'Germany · MSc Computer Science / INFOTECH · Baden-Württemberg Tech Hub',
      flag: '🇩🇪',
      targetParam: 'University of Stuttgart',
      ectsId: 'STUTTGART',
      keywords: ['stuttgart', 'germany', 'infotech', 'tu9', 'baden-wurttemberg'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=University%20of%20Stuttgart' },
        { label: '📐 ECTS Screener', url: 'ects-screener.html?uni=STUTTGART' },
        { label: '📅 Timeline', url: 'timeline.html?country=Germany&uni=University%20of%20Stuttgart' }
      ]
    },
    {
      id: 'uni-kit',
      type: 'UNIVERSITY',
      country: 'Germany',
      region: 'EU',
      title: 'Karlsruhe Institute of Technology (KIT)',
      subtitle: 'Germany · MSc Informatics · Renowned Cyber & Algorithm Research',
      flag: '🇩🇪',
      targetParam: 'Karlsruhe Institute of Technology (KIT)',
      ectsId: 'KIT',
      keywords: ['kit', 'karlsruhe', 'germany', 'informatics', 'cyber'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=Karlsruhe%20Institute%20of%20Technology' },
        { label: '📐 ECTS Screener', url: 'ects-screener.html?uni=KIT' }
      ]
    },
    {
      id: 'uni-tu-berlin',
      type: 'UNIVERSITY',
      country: 'Germany',
      region: 'EU',
      title: 'Technical University of Berlin (TU Berlin)',
      subtitle: 'Germany · MSc Computer Science · Germany Innovation Capital',
      flag: '🇩🇪',
      targetParam: 'Technical University of Berlin',
      ectsId: 'TU_BERLIN',
      keywords: ['tu berlin', 'berlin', 'germany', 'computer science'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=TU%20Berlin' },
        { label: '📐 ECTS Screener', url: 'ects-screener.html?uni=TU_BERLIN' }
      ]
    },
    {
      id: 'uni-stanford',
      type: 'UNIVERSITY',
      country: 'United States',
      region: 'US',
      title: 'Stanford University',
      subtitle: 'USA · MS Computer Science & AI · Silicon Valley Core · 36m STEM OPT',
      flag: '🇺🇸',
      targetParam: 'Stanford University',
      keywords: ['stanford', 'usa', 'united states', 'california', 'silicon valley', 'ms cs', 'ai'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=Stanford%20University' },
        { label: '🔬 Faculty Critic', url: 'faculty-critic.html?country=USA&uni=Stanford%20University' },
        { label: '💰 Scholarships', url: 'scholarships.html?country=USA&uni=Stanford%20University' },
        { label: '📅 Timeline', url: 'timeline.html?country=USA&uni=Stanford%20University' }
      ]
    },
    {
      id: 'uni-cmu',
      type: 'UNIVERSITY',
      country: 'United States',
      region: 'US',
      title: 'Carnegie Mellon University (CMU)',
      subtitle: 'USA · School of Computer Science & Language Technologies Institute',
      flag: '🇺🇸',
      targetParam: 'Carnegie Mellon University',
      keywords: ['cmu', 'carnegie mellon', 'usa', 'pittsburgh', 'scs', 'ai', 'lti'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=Carnegie%20Mellon%20University' },
        { label: '🔬 Faculty Critic', url: 'faculty-critic.html?country=USA&uni=Carnegie%20Mellon%20University' },
        { label: '📅 Timeline', url: 'timeline.html?country=USA&uni=Carnegie%20Mellon%20University' }
      ]
    },
    {
      id: 'uni-mit',
      type: 'UNIVERSITY',
      country: 'United States',
      region: 'US',
      title: 'MIT CSAIL',
      subtitle: 'USA · Electrical Engineering & Computer Science · Premier Global Lab',
      flag: '🇺🇸',
      targetParam: 'MIT CSAIL',
      keywords: ['mit', 'csail', 'cambridge', 'massachusetts', 'eecs', 'usa'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=MIT' },
        { label: '🔬 Faculty Critic', url: 'faculty-critic.html?country=USA&uni=MIT' },
        { label: '📅 Timeline', url: 'timeline.html?country=USA&uni=MIT' }
      ]
    },
    {
      id: 'uni-berkeley',
      type: 'UNIVERSITY',
      country: 'United States',
      region: 'US',
      title: 'UC Berkeley',
      subtitle: 'USA · MS EECS & BAIR Lab (Berkeley AI Research) · Bay Area',
      flag: '🇺🇸',
      targetParam: 'UC Berkeley',
      keywords: ['berkeley', 'uc berkeley', 'bair', 'california', 'usa', 'eecs'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=UC%20Berkeley' },
        { label: '🔬 Faculty Critic', url: 'faculty-critic.html?country=USA&uni=UC%20Berkeley' }
      ]
    },
    {
      id: 'uni-michigan',
      type: 'UNIVERSITY',
      country: 'United States',
      region: 'US',
      title: 'University of Michigan, Ann Arbor',
      subtitle: 'USA · MS CS (Systems & AI) · Generous Graduate Student Assistantships',
      flag: '🇺🇸',
      targetParam: 'University of Michigan',
      keywords: ['michigan', 'umich', 'ann arbor', 'systems', 'usa'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=University%20of%20Michigan' },
        { label: '💰 Scholarships', url: 'scholarships.html?country=USA&uni=University%20of%20Michigan' }
      ]
    },
    {
      id: 'uni-tokyo',
      type: 'UNIVERSITY',
      country: 'Japan',
      region: 'JP',
      title: 'University of Tokyo (Todai)',
      subtitle: 'Japan · Graduate School of Information Science & Tech · Naidaku Required',
      flag: '🇯🇵',
      targetParam: 'University of Tokyo',
      keywords: ['tokyo', 'todai', 'japan', 'mext', 'naidaku', 'informatics'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=University%20of%20Tokyo' },
        { label: '🔬 Faculty Critic (Naidaku)', url: 'faculty-critic.html?country=Japan&uni=University%20of%20Tokyo' },
        { label: '💰 MEXT Scholarships', url: 'scholarships.html?country=Japan&uni=University%20of%20Tokyo' },
        { label: '📅 Timeline', url: 'timeline.html?country=Japan&uni=University%20of%20Tokyo' }
      ]
    },
    {
      id: 'uni-kyoto',
      type: 'UNIVERSITY',
      country: 'Japan',
      region: 'JP',
      title: 'Kyoto University',
      subtitle: 'Japan · Graduate School of Informatics · J-Find 24m Jobseeker Visa',
      flag: '🇯🇵',
      targetParam: 'Kyoto University',
      keywords: ['kyoto', 'japan', 'informatics', 'mext', 'j-find'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=Kyoto%20University' },
        { label: '🔬 Faculty Critic', url: 'faculty-critic.html?country=Japan&uni=Kyoto%20University' },
        { label: '💰 MEXT Scholarships', url: 'scholarships.html?country=Japan&uni=Kyoto%20University' }
      ]
    },
    {
      id: 'uni-osaka',
      type: 'UNIVERSITY',
      country: 'Japan',
      region: 'JP',
      title: 'Osaka University',
      subtitle: 'Japan · Graduate School of Information Science & Robotics Labs',
      flag: '🇯🇵',
      targetParam: 'Osaka University',
      keywords: ['osaka', 'japan', 'robotics', 'jasso', 'mext'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=Osaka%20University' },
        { label: '🔬 Faculty Critic', url: 'faculty-critic.html?country=Japan&uni=Osaka%20University' }
      ]
    },
    {
      id: 'uni-eth',
      type: 'UNIVERSITY',
      country: 'Switzerland',
      region: 'EU',
      title: 'ETH Zürich',
      subtitle: 'Switzerland · MSc Computer Science · Ultra-low $1.6k/yr tuition',
      flag: '🇨🇭',
      targetParam: 'ETH Zürich',
      keywords: ['eth', 'zurich', 'switzerland', 'swiss', 'computer science', 'esop'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=ETH%20Zürich' },
        { label: '💰 ESOP Scholarship', url: 'scholarships.html?country=Switzerland&uni=ETH%20Zürich' },
        { label: '🔬 Faculty Critic', url: 'faculty-critic.html?country=Switzerland&uni=ETH%20Zürich' }
      ]
    },
    {
      id: 'uni-oxford',
      type: 'UNIVERSITY',
      country: 'United Kingdom',
      region: 'EU',
      title: 'University of Oxford',
      subtitle: 'UK · MSc Advanced Computer Science · 2-Year Graduate Route Work Visa',
      flag: '🇬🇧',
      targetParam: 'University of Oxford',
      keywords: ['oxford', 'uk', 'united kingdom', 'oxfordshire', 'computer science'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=University%20of%20Oxford' },
        { label: '💰 Inlaks / Rhodes', url: 'scholarships.html?country=UK&uni=University%20of%20Oxford' }
      ]
    },
    {
      id: 'uni-cambridge',
      type: 'UNIVERSITY',
      country: 'United Kingdom',
      region: 'EU',
      title: 'University of Cambridge',
      subtitle: 'UK · MPhil Advanced Computer Science · Gates Cambridge Scholarships',
      flag: '🇬🇧',
      targetParam: 'University of Cambridge',
      keywords: ['cambridge', 'uk', 'gates cambridge', 'mphil', 'cs'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=University%20of%20Cambridge' },
        { label: '💰 Gates Cambridge', url: 'scholarships.html?country=UK&uni=University%20of%20Cambridge' }
      ]
    },
    {
      id: 'uni-upc-barcelona',
      type: 'UNIVERSITY',
      country: 'Spain',
      region: 'EU',
      title: 'UPC BarcelonaTech (Polytechnic University of Catalonia)',
      subtitle: 'Spain · MSc in Artificial Intelligence · Low €4.2k Tuition · 12m Búsqueda de Empleo',
      flag: '🇪🇸',
      targetParam: 'UPC BarcelonaTech',
      keywords: ['upc', 'barcelona', 'spain', 'catalonia', 'ai', 'erasmus'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=UPC%20BarcelonaTech' },
        { label: '💰 Scholarships', url: 'scholarships.html?country=Spain&uni=UPC%20BarcelonaTech' }
      ]
    },
    {
      id: 'uni-ist-lisbon',
      type: 'UNIVERSITY',
      country: 'Portugal',
      region: 'EU',
      title: 'Instituto Superior Técnico (University of Lisbon)',
      subtitle: 'Portugal · MSc Computer Science · Ultra-low €4k Tuition · Fastest 5-Yr EU Passport',
      flag: '🇵🇹',
      targetParam: 'Instituto Superior Técnico',
      keywords: ['ist', 'lisbon', 'portugal', 'fct', 'd4 visa'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=Instituto%20Superior%20Técnico' },
        { label: '💰 FCT Fellowship', url: 'scholarships.html?country=Portugal&uni=Instituto%20Superior%20Técnico' }
      ]
    },
    {
      id: 'uni-polimi',
      type: 'UNIVERSITY',
      country: 'Italy',
      region: 'EU',
      title: 'Politecnico di Milano',
      subtitle: 'Italy · MSc Computer Science & Engineering · DSU 100% Tuition Waiver & Stipend',
      flag: '🇮🇹',
      targetParam: 'Politecnico di Milano',
      keywords: ['polimi', 'milan', 'italy', 'dsu', 'computer science'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=Politecnico%20di%20Milano' },
        { label: '💰 DSU Scholarship', url: 'scholarships.html?country=Italy&uni=Politecnico%20di%20Milano' }
      ]
    },
    {
      id: 'uni-kth',
      type: 'UNIVERSITY',
      country: 'Sweden',
      region: 'EU',
      title: 'KTH Royal Institute of Technology',
      subtitle: 'Sweden · MSc Machine Learning · 12m Job Seeking Residence Permit',
      flag: '🇸🇪',
      targetParam: 'KTH Stockholm',
      keywords: ['kth', 'stockholm', 'sweden', 'machine learning', 'scandinavia'],
      quickLinks: [
        { label: '📊 Dashboard', url: 'dashboard.html?uni=KTH%20Stockholm' },
        { label: '💰 Scholarships', url: 'scholarships.html?country=Sweden&uni=KTH%20Stockholm' }
      ]
    }
  ];

  const SPOTLIGHT_SCHOLARSHIPS = [
    {
      id: 'sch-daad-epos',
      type: 'SCHOLARSHIP',
      country: 'Germany',
      region: 'GERMANY',
      title: 'DAAD Development-Related Postgraduate Courses (EPOS)',
      subtitle: 'Germany · 100% Full-Ride Tuition + €934/mo Stipend + Travel + Health Coverage',
      flag: '🇩🇪',
      targetParam: 'EPOS',
      filterRegion: 'GERMANY',
      keywords: ['daad', 'epos', 'germany', 'full ride', 'stipend', 'development', 'dac'],
      url: 'scholarships.html?region=GERMANY&q=EPOS'
    },
    {
      id: 'sch-daad-helmut',
      type: 'SCHOLARSHIP',
      country: 'Germany',
      region: 'GERMANY',
      title: 'DAAD Helmut-Schmidt-Programme (Public Policy & Digital Governance)',
      subtitle: 'Germany · Full Tuition Waiver + €934/mo + 6-Month German Prep Course',
      flag: '🇩🇪',
      targetParam: 'Helmut-Schmidt',
      filterRegion: 'GERMANY',
      keywords: ['daad', 'helmut', 'schmidt', 'germany', 'governance', 'policy', 'ethics'],
      url: 'scholarships.html?region=GERMANY&q=Helmut'
    },
    {
      id: 'sch-daad-study',
      type: 'SCHOLARSHIP',
      country: 'Germany',
      region: 'GERMANY',
      title: 'DAAD Master Studies Scholarship for All Academic Disciplines',
      subtitle: 'Germany · €934/mo for 10–24 Months + Airfare + Study Allowance via New Delhi',
      flag: '🇩🇪',
      targetParam: 'DAAD Master Studies',
      filterRegion: 'GERMANY',
      keywords: ['daad', 'all disciplines', 'study scholarship', 'germany', 'delhi'],
      url: 'scholarships.html?region=GERMANY&q=DAAD'
    },
    {
      id: 'sch-deutschlandstipendium',
      type: 'SCHOLARSHIP',
      country: 'Germany',
      region: 'GERMANY',
      title: 'Deutschlandstipendium (German National Merit Scholarship)',
      subtitle: 'Germany · €300/month Tax-Free Cash Stipend · Public-Private Co-Funded (SAP, BMW)',
      flag: '🇩🇪',
      targetParam: 'Deutschlandstipendium',
      filterRegion: 'GERMANY',
      keywords: ['deutschlandstipendium', 'national scholarship', 'germany', 'stipend', 'merit'],
      url: 'scholarships.html?region=GERMANY&q=Deutschlandstipendium'
    },
    {
      id: 'sch-mext-embassy',
      type: 'SCHOLARSHIP',
      country: 'Japan',
      region: 'JAPAN',
      title: 'MEXT Japanese Government Scholarship (Embassy Recommendation)',
      subtitle: 'Japan · Full Tuition Exemption + ¥144k–¥147k/mo + Return Airfare from India',
      flag: '🇯🇵',
      targetParam: 'MEXT Embassy',
      filterRegion: 'JAPAN',
      keywords: ['mext', 'monbukagakusho', 'embassy', 'japan', 'full ride', 'tokyo', 'kyoto'],
      url: 'scholarships.html?region=JAPAN&q=MEXT'
    },
    {
      id: 'sch-mext-uni',
      type: 'SCHOLARSHIP',
      country: 'Japan',
      region: 'JAPAN',
      title: 'MEXT University Recommendation (Special Graduate Track)',
      subtitle: 'Japan · Direct Supervisor Nomination + 100% Tuition Waiver + ¥144k/mo',
      flag: '🇯🇵',
      targetParam: 'MEXT University',
      filterRegion: 'JAPAN',
      keywords: ['mext', 'university recommendation', 'naidaku', 'japan', 'supervisor'],
      url: 'scholarships.html?region=JAPAN&q=University'
    },
    {
      id: 'sch-jasso-honors',
      type: 'SCHOLARSHIP',
      country: 'Japan',
      region: 'JAPAN',
      title: 'JASSO Monbukagakusho Honors Study Grant for Enrolled Students',
      subtitle: 'Japan · ¥48,000/month Study Grant · Accessible 7.0+ CGPA Requirement',
      flag: '🇯🇵',
      targetParam: 'JASSO',
      filterRegion: 'JAPAN',
      keywords: ['jasso', 'honors', 'japan', 'monbukagakusho', 'grant', 'stipend'],
      url: 'scholarships.html?region=JAPAN&q=JASSO'
    },
    {
      id: 'sch-fulbright',
      type: 'SCHOLARSHIP',
      country: 'United States',
      region: 'US_UK',
      title: "Fulbright-Nehru Master's Fellowships (USIEF)",
      subtitle: 'USA · 100% Full-Ride Tuition + Monthly Living Stipend + J-1 Visa + Flights',
      flag: '🇺🇸',
      targetParam: 'Fulbright',
      filterRegion: 'US_UK',
      keywords: ['fulbright', 'nehru', 'usief', 'usa', 'fellowship', 'full ride', 'j1'],
      url: 'scholarships.html?region=US_UK&q=Fulbright'
    },
    {
      id: 'sch-inlaks',
      type: 'SCHOLARSHIP',
      country: 'Global',
      region: 'US_UK',
      title: 'Inlaks Shivdasani Foundation Scholarships',
      subtitle: 'US / UK / Europe · Up to $100,000 for Outstanding Indian Graduates',
      flag: '🌐',
      targetParam: 'Inlaks',
      filterRegion: 'US_UK',
      keywords: ['inlaks', 'shivdasani', 'uk', 'usa', 'europe', 'oxford', 'cambridge', '100k'],
      url: 'scholarships.html?region=US_UK&q=Inlaks'
    },
    {
      id: 'sch-erasmus',
      type: 'SCHOLARSHIP',
      country: 'European Union',
      region: 'ERASMUS',
      title: 'Erasmus Mundus Joint Master Degrees (EMJMD Scholarships)',
      subtitle: 'Europe · 100% Tuition Waiver + €1,400/month Stipend + Multi-Country Travel',
      flag: '🇪🇺',
      targetParam: 'Erasmus',
      filterRegion: 'ERASMUS',
      keywords: ['erasmus', 'mundus', 'emjmd', 'europe', 'consortium', 'full ride', 'spain', 'france'],
      url: 'scholarships.html?region=ERASMUS&q=Erasmus'
    },
    {
      id: 'sch-knight-hennessy',
      type: 'SCHOLARSHIP',
      country: 'United States',
      region: 'US_UK',
      title: 'Knight-Hennessy Scholars Program (Stanford University)',
      subtitle: 'USA · Full Funding for Graduate Study across Stanford Departments',
      flag: '🇺🇸',
      targetParam: 'Knight-Hennessy',
      filterRegion: 'US_UK',
      keywords: ['knight', 'hennessy', 'stanford', 'california', 'usa', 'fellowship'],
      url: 'scholarships.html?region=US_UK&q=Knight'
    },
    {
      id: 'sch-eth-esop',
      type: 'SCHOLARSHIP',
      country: 'Switzerland',
      region: 'ERASMUS',
      title: 'ETH Excellence Scholarship & Opportunity Programme (ESOP)',
      subtitle: 'Switzerland · Full Living Grant (CHF 12k/sem) + Full Tuition Waiver at ETH Zürich',
      flag: '🇨🇭',
      targetParam: 'ESOP',
      filterRegion: 'ERASMUS',
      keywords: ['eth', 'esop', 'zurich', 'switzerland', 'excellence'],
      url: 'scholarships.html?region=ERASMUS&q=ESOP'
    },
    {
      id: 'sch-tara',
      type: 'SCHOLARSHIP',
      country: 'United States',
      region: 'US_UK',
      title: 'Graduate Teaching & Research Assistantships (TA/RA Fee Remission)',
      subtitle: 'US Public & Private Universities · 100% Tuition Waiver + $2,000–$3,500/mo Salary',
      flag: '🇺🇸',
      targetParam: 'Assistantship',
      filterRegion: 'US_UK',
      keywords: ['ta', 'ra', 'assistantship', 'fee remission', 'stipend', 'usa', 'michigan', 'cmu'],
      url: 'scholarships.html?region=US_UK&q=Assistantship'
    }
  ];

  const SPOTLIGHT_ACTIONS = [
    {
      id: 'act-load-aicte',
      type: 'ACTION',
      title: '📋 Load AICTE B.Tech CSE (4-Yr Model Syllabus)',
      subtitle: 'Quickly pre-fill 20 core courses in German ECTS Credit Screener',
      icon: '📋',
      badge: 'ECTS Preset',
      url: 'ects-screener.html?load=aicte',
      keywords: ['aicte', 'syllabus', 'credits', 'ects', 'model', 'btech']
    },
    {
      id: 'act-load-deficit',
      type: 'ACTION',
      title: '📋 Test Common Indian Theoretical CS Deficit',
      subtitle: 'Simulate the #1 curriculum mismatch that triggers German rejections',
      icon: '⚠️',
      badge: 'Deficit Test',
      url: 'ects-screener.html?load=tier2',
      keywords: ['deficit', 'mismatch', 'rejection', 'tier 2', 'tier 3', 'theoretical cs']
    },
    {
      id: 'act-export-formblatt',
      type: 'ACTION',
      title: '📥 Export German Formblatt Markdown Table',
      subtitle: 'Generate formatted prerequisite table for TU Munich / RWTH Aachen',
      icon: '📥',
      badge: 'Export',
      url: 'ects-screener.html#formblatt',
      keywords: ['formblatt', 'markdown', 'export', 'tum', 'table']
    },
    {
      id: 'act-holistic-mode',
      type: 'ACTION',
      title: '✨ Switch Dashboard to Holistic Scoring Index (50/30/20)',
      subtitle: 'Blend Academic fit, Financial feasibility, and Post-Study Visa viability',
      icon: '✨',
      badge: 'Scoring Mode',
      url: 'dashboard.html?mode=holistic',
      keywords: ['holistic', 'scores', 'weights', 'blend', 'visa', 'financial']
    },
    {
      id: 'act-academic-mode',
      type: 'ACTION',
      title: '🎓 Switch Dashboard to Pure Academic Fit Mode',
      subtitle: 'Rank universities strictly by GPA, test percentiles, and research fit',
      icon: '🎓',
      badge: 'Scoring Mode',
      url: 'dashboard.html?mode=academic',
      keywords: ['academic', 'pure', 'gpa', 'scores', 'fit']
    }
  ];

  // Combine full directory
  const ALL_ITEMS = [
    ...SPOTLIGHT_TOOLS,
    ...SPOTLIGHT_UNIVERSITIES,
    ...SPOTLIGHT_SCHOLARSHIPS,
    ...SPOTLIGHT_ACTIONS
  ];

  // ── 2. DOM Injection: Spotlight Modal & Header Trigger ───────────────────────

  function ensureStylesAndDom() {
    // Inject header trigger button if not already present
    injectHeaderTrigger();

    // Inject modal HTML if not already present
    if (!document.getElementById('spotlightModal')) {
      const modalEl = document.createElement('div');
      modalEl.id = 'spotlightModal';
      modalEl.className = 'spotlight-overlay';
      modalEl.setAttribute('role', 'dialog');
      modalEl.setAttribute('aria-modal', 'true');
      modalEl.setAttribute('aria-label', 'Global Command Palette');
      modalEl.innerHTML = `
        <div class="spotlight-dialog">
          <!-- Top Search Header -->
          <div class="spotlight-header">
            <span class="spotlight-search-icon">🔍</span>
            <input type="text" id="spotlightInput" class="spotlight-input"
                   placeholder="Search universities, scholarships (e.g. DAAD, Fulbright), or jump to tools..."
                   autocomplete="off" spellcheck="false" />
            <button type="button" id="spotlightClearBtn" class="spotlight-clear-btn" aria-label="Clear Search" style="display:none">✕</button>
            <kbd class="spotlight-close-kbd" id="spotlightCloseKbd" title="Press Esc to close">ESC</kbd>
          </div>

          <!-- Quick Category Filter Tabs -->
          <div class="spotlight-tabs" id="spotlightTabs">
            <button type="button" class="spotlight-tab active" data-filter="ALL">All</button>
            <button type="button" class="spotlight-tab" data-filter="TOOL">⚡ Tools (1-6)</button>
            <button type="button" class="spotlight-tab" data-filter="UNIVERSITY">🏛 Universities</button>
            <button type="button" class="spotlight-tab" data-filter="SCHOLARSHIP">💰 Scholarships</button>
            <button type="button" class="spotlight-tab" data-filter="ACTION">💡 Actions</button>
          </div>

          <!-- Results Scroll Area -->
          <div class="spotlight-body" id="spotlightBody">
            <div id="spotlightResultsList" class="spotlight-results"></div>
          </div>

          <!-- Footer Legend -->
          <div class="spotlight-footer">
            <div class="spotlight-keys">
              <span class="spotlight-key-item"><kbd class="kbd-sub">↑</kbd><kbd class="kbd-sub">↓</kbd> Navigate</span>
              <span class="spotlight-key-item"><kbd class="kbd-sub">↵</kbd> Select</span>
              <span class="spotlight-key-item"><kbd class="kbd-sub">1-6</kbd> Quick Switch</span>
              <span class="spotlight-key-item"><kbd class="kbd-sub">esc</kbd> Dismiss</span>
            </div>
            <div class="spotlight-hint">
              <strong>${ALL_ITEMS.length}</strong> items indexed
            </div>
          </div>
        </div>
      `;
      document.body.appendChild(modalEl);
    }
  }

  function injectHeaderTrigger() {
    if (document.getElementById('spotlightTriggerBtn')) return;

    const nav = document.querySelector('.app-header .nav-links');
    const headerContainer = document.querySelector('.app-header .container');

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.id = 'spotlightTriggerBtn';
    btn.className = 'spotlight-header-btn';
    btn.setAttribute('aria-label', 'Open Global Search & Command Palette (Cmd + K / Ctrl + K)');
    btn.setAttribute('title', 'Quick Search (⌘K / Ctrl+K)');
    btn.innerHTML = `
      <span class="spotlight-btn-icon">🔍</span>
      <span class="spotlight-btn-label">Quick Search...</span>
      <kbd class="spotlight-btn-kbd">⌘K</kbd>
    `;

    btn.addEventListener('click', (e) => {
      e.preventDefault();
      openSpotlight();
    });

    if (nav) {
      // Place right before "Edit Profile" or as first nav button
      nav.insertBefore(btn, nav.firstChild);
    } else if (headerContainer) {
      headerContainer.appendChild(btn);
    }
  }

  // ── 3. State & Search Engine Logic ──────────────────────────────────────────

  let isOpen = false;
  let activeFilter = 'ALL';
  let activeIndex = 0;
  let currentResults = [];

  function openSpotlight() {
    ensureStylesAndDom();
    const modal = document.getElementById('spotlightModal');
    const input = document.getElementById('spotlightInput');
    if (!modal || !input) return;

    isOpen = true;
    modal.classList.add('visible');
    document.body.style.overflow = 'hidden';

    // Reset or preserve existing state
    input.value = '';
    activeFilter = 'ALL';
    updateFilterTabsUI();
    performSearch('');

    setTimeout(() => {
      input.focus();
      input.select();
    }, 40);
  }

  function closeSpotlight() {
    const modal = document.getElementById('spotlightModal');
    if (!modal) return;

    isOpen = false;
    modal.classList.remove('visible');
    document.body.style.overflow = '';
  }

  function toggleSpotlight() {
    if (isOpen) {
      closeSpotlight();
    } else {
      openSpotlight();
    }
  }

  function updateFilterTabsUI() {
    const tabs = document.querySelectorAll('.spotlight-tab');
    tabs.forEach(t => {
      t.classList.toggle('active', t.dataset.filter === activeFilter);
    });
  }

  function performSearch(query) {
    const q = (query || '').trim().toLowerCase();
    const clearBtn = document.getElementById('spotlightClearBtn');
    if (clearBtn) {
      clearBtn.style.display = q ? 'block' : 'none';
    }

    let items = ALL_ITEMS;

    // 1. Filter by category if not 'ALL'
    if (activeFilter !== 'ALL') {
      items = items.filter(it => it.type === activeFilter);
    }

    // 2. Filter by search query text
    if (q) {
      const tokens = q.split(/\s+/).filter(Boolean);
      items = items.filter(item => {
        const text = [
          item.title,
          item.subtitle || item.description || '',
          item.country || '',
          item.region || '',
          ...(item.keywords || [])
        ].join(' ').toLowerCase();

        return tokens.every(tok => text.includes(tok));
      });

      // Sort by relevance: Title match first, then subtitle, then keywords
      items.sort((a, b) => {
        const aTitleMatch = a.title.toLowerCase().includes(q) ? 1 : 0;
        const bTitleMatch = b.title.toLowerCase().includes(q) ? 1 : 0;
        return bTitleMatch - aTitleMatch;
      });
    }

    currentResults = items;
    activeIndex = 0;
    renderResults(items, q);
  }

  // ── 4. Render Results ───────────────────────────────────────────────────────

  function renderResults(items, query) {
    const container = document.getElementById('spotlightResultsList');
    if (!container) return;

    if (items.length === 0) {
      container.innerHTML = `
        <div class="spotlight-empty">
          <div class="spotlight-empty-icon">🔍</div>
          <div class="spotlight-empty-title">No matching results for "<strong>${escapeHtml(query)}</strong>"</div>
          <div class="spotlight-empty-desc">
            Try searching for universities (e.g. <em>TUM, Stanford, Oxford, Todai</em>),
            scholarships (<em>DAAD, MEXT, Fulbright, Erasmus</em>), or switch tools.
          </div>
          <div class="spotlight-empty-suggestions">
            <button type="button" class="spotlight-suggest-chip" data-q="TUM">🇩🇪 TU Munich</button>
            <button type="button" class="spotlight-suggest-chip" data-q="DAAD">💰 DAAD Scholarships</button>
            <button type="button" class="spotlight-suggest-chip" data-q="MEXT">🇯🇵 MEXT Japan</button>
            <button type="button" class="spotlight-suggest-chip" data-q="Stanford">🇺🇸 Stanford</button>
            <button type="button" class="spotlight-suggest-chip" data-q="ECTS">📐 ECTS Screener</button>
          </div>
        </div>
      `;
      return;
    }

    // If query is empty, render categorized grouped sections
    if (!query) {
      renderGroupedView(items, container);
    } else {
      renderFlatSearchResults(items, container, query);
    }

    updateActiveSelectionVisual();
  }

  function renderGroupedView(items, container) {
    const groups = [
      { key: 'TOOL', label: '⚡ TOOL PAGES (DIRECT SWITCH)', items: items.filter(i => i.type === 'TOOL') },
      { key: 'UNIVERSITY', label: '🏛 FEATURED UNIVERSITIES & ADMISSIONS', items: items.filter(i => i.type === 'UNIVERSITY') },
      { key: 'SCHOLARSHIP', label: '💰 MAJOR SCHOLARSHIP RADAR', items: items.filter(i => i.type === 'SCHOLARSHIP') },
      { key: 'ACTION', label: '💡 QUICK ACTIONS & PRESETS', items: items.filter(i => i.type === 'ACTION') }
    ];

    let html = '';
    let flatIdx = 0;

    groups.forEach(group => {
      if (group.items.length === 0) return;
      html += `<div class="spotlight-group-header">${group.label}</div>`;
      group.items.forEach(item => {
        html += renderItemHtml(item, flatIdx++);
      });
    });

    container.innerHTML = html;
  }

  function renderFlatSearchResults(items, container, query) {
    let html = `<div class="spotlight-group-header">MATCHING RESULTS (${items.length})</div>`;
    items.forEach((item, idx) => {
      html += renderItemHtml(item, idx, query);
    });
    container.innerHTML = html;
  }

  function renderItemHtml(item, idx, query) {
    const isSelected = idx === activeIndex;
    const icon = item.flag || item.icon || '📌';
    const title = highlightMatch(item.title, query);
    const sub = highlightMatch(item.subtitle || item.description || '', query);

    let typeBadgeClass = 'badge-tool';
    if (item.type === 'UNIVERSITY') typeBadgeClass = 'badge-uni';
    else if (item.type === 'SCHOLARSHIP') typeBadgeClass = 'badge-sch';
    else if (item.type === 'ACTION') typeBadgeClass = 'badge-act';

    const shortcutBadge = item.shortcut
      ? `<kbd class="spotlight-item-key" title="Press '${item.shortcut}' to jump">${item.shortcut}</kbd>`
      : '';

    let quickPills = '';
    if (item.quickLinks && item.quickLinks.length > 0) {
      quickPills = `
        <div class="spotlight-quick-pills">
          ${item.quickLinks.map(ql => `
            <a href="${ql.url}" class="spotlight-quick-pill" onclick="event.stopPropagation()">
              ${ql.label}
            </a>
          `).join('')}
        </div>
      `;
    }

    return `
      <div class="spotlight-item ${isSelected ? 'selected' : ''}" data-index="${idx}" data-id="${item.id}" tabindex="-1">
        <div class="spotlight-item-icon">${icon}</div>
        <div class="spotlight-item-content">
          <div class="spotlight-item-title-row">
            <span class="spotlight-item-title">${title}</span>
            <span class="spotlight-type-badge ${typeBadgeClass}">${item.badge || item.type}</span>
          </div>
          <div class="spotlight-item-desc">${sub}</div>
          ${quickPills}
        </div>
        <div class="spotlight-item-right">
          ${shortcutBadge}
          <span class="spotlight-jump-arrow">↵</span>
        </div>
      </div>
    `;
  }

  function highlightMatch(text, query) {
    if (!text) return '';
    if (!query) return escapeHtml(text);
    const escaped = escapeHtml(text);
    const tokens = query.split(/\s+/).filter(Boolean);
    let result = escaped;
    tokens.forEach(tok => {
      const reg = new RegExp(`(${escapeRegex(tok)})`, 'gi');
      result = result.replace(reg, '<mark class="spotlight-mark">$1</mark>');
    });
    return result;
  }

  function updateActiveSelectionVisual() {
    const items = document.querySelectorAll('.spotlight-item');
    items.forEach((el, idx) => {
      const isCur = idx === activeIndex;
      el.classList.toggle('selected', isCur);
      if (isCur) {
        el.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
      }
    });
  }

  // ── 5. Action Execution ─────────────────────────────────────────────────────

  function executeItem(item) {
    if (!item) return;
    closeSpotlight();

    if (item.url) {
      window.location.href = item.url;
      return;
    }

    if (item.type === 'UNIVERSITY') {
      // Default to program matches or ECTS screener if German
      if (item.country === 'Germany') {
        window.location.href = `ects-screener.html?uni=${item.ectsId || encodeURIComponent(item.title)}`;
      } else if (item.country === 'Japan') {
        window.location.href = `faculty-critic.html?country=Japan&uni=${encodeURIComponent(item.title)}`;
      } else {
        window.location.href = `dashboard.html?uni=${encodeURIComponent(item.title)}`;
      }
      return;
    }

    if (item.type === 'SCHOLARSHIP') {
      window.location.href = item.url || `scholarships.html?q=${encodeURIComponent(item.targetParam || item.title)}`;
      return;
    }
  }

  // ── 6. Event Listeners (Keyboard Shortcuts & Interactions) ───────────────────

  function attachEventListeners() {
    // Global Keyboard Shortcut: Cmd+K / Ctrl+K & Slash key
    document.addEventListener('keydown', (e) => {
      const isMac = navigator.platform.toUpperCase().indexOf('MAC') >= 0;
      const isModifier = isMac ? e.metaKey : e.ctrlKey;

      // Cmd+K or Ctrl+K
      if (isModifier && (e.key === 'k' || e.key === 'K')) {
        e.preventDefault();
        toggleSpotlight();
        return;
      }

      // If Spotlight is open, handle palette controls
      if (isOpen) {
        if (e.key === 'Escape') {
          e.preventDefault();
          closeSpotlight();
          return;
        }

        if (e.key === 'ArrowDown') {
          e.preventDefault();
          if (currentResults.length > 0) {
            activeIndex = (activeIndex + 1) % currentResults.length;
            updateActiveSelectionVisual();
          }
          return;
        }

        if (e.key === 'ArrowUp') {
          e.preventDefault();
          if (currentResults.length > 0) {
            activeIndex = (activeIndex - 1 + currentResults.length) % currentResults.length;
            updateActiveSelectionVisual();
          }
          return;
        }

        if (e.key === 'Enter') {
          e.preventDefault();
          if (currentResults[activeIndex]) {
            executeItem(currentResults[activeIndex]);
          }
          return;
        }

        // Direct number keys (1 to 6) when input is empty or when not typing a complex search
        const input = document.getElementById('spotlightInput');
        if (input && input.value.trim() === '' && ['1', '2', '3', '4', '5', '6'].includes(e.key)) {
          e.preventDefault();
          const targetTool = SPOTLIGHT_TOOLS.find(t => t.shortcut === e.key);
          if (targetTool) {
            executeItem(targetTool);
          }
          return;
        }
      } else {
        // Quick '/' key to open when not in input or textarea
        if (e.key === '/' && !['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement?.tagName)) {
          e.preventDefault();
          openSpotlight();
        }
      }
    });

    // Delegate clicks inside Spotlight
    document.addEventListener('click', (e) => {
      // 1. Click overlay backdrop to close
      if (e.target.id === 'spotlightModal') {
        closeSpotlight();
        return;
      }

      // 2. Click close button or ESC kbd
      if (e.target.closest('#spotlightCloseKbd')) {
        closeSpotlight();
        return;
      }

      // 3. Clear button
      if (e.target.closest('#spotlightClearBtn')) {
        const input = document.getElementById('spotlightInput');
        if (input) {
          input.value = '';
          input.focus();
          performSearch('');
        }
        return;
      }

      // 4. Click Category Filter Tab
      const tab = e.target.closest('.spotlight-tab');
      if (tab) {
        activeFilter = tab.dataset.filter || 'ALL';
        updateFilterTabsUI();
        const input = document.getElementById('spotlightInput');
        performSearch(input ? input.value : '');
        return;
      }

      // 5. Click Suggestion Chip in Empty State
      const chip = e.target.closest('.spotlight-suggest-chip');
      if (chip) {
        const q = chip.dataset.q;
        const input = document.getElementById('spotlightInput');
        if (input) {
          input.value = q;
          input.focus();
          performSearch(q);
        }
        return;
      }

      // 6. Click on a result row
      const itemEl = e.target.closest('.spotlight-item');
      if (itemEl) {
        const idx = parseInt(itemEl.dataset.index, 10);
        if (!isNaN(idx) && currentResults[idx]) {
          executeItem(currentResults[idx]);
        }
        return;
      }
    });

    // Input changes
    document.addEventListener('input', (e) => {
      if (e.target.id === 'spotlightInput') {
        performSearch(e.target.value);
      }
    });
  }

  // ── 7. Helpers ──────────────────────────────────────────────────────────────

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function escapeRegex(str) {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  // ── 8. Public API & Bootstrapping ───────────────────────────────────────────

  window.MS_Spotlight = {
    open: openSpotlight,
    close: closeSpotlight,
    toggle: toggleSpotlight,
    search: performSearch,
    tools: SPOTLIGHT_TOOLS,
    universities: SPOTLIGHT_UNIVERSITIES,
    scholarships: SPOTLIGHT_SCHOLARSHIPS
  };

  // Auto-init once DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      ensureStylesAndDom();
      attachEventListeners();
    });
  } else {
    ensureStylesAndDom();
    attachEventListeners();
  }
})();
