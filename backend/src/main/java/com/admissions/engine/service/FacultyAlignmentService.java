package com.admissions.engine.service;

import com.admissions.engine.model.alignment.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service that analyzes semantic alignment between an applicant's Statement of Purpose (SOP)
 * or research proposal and university faculty laboratories, matching against arXiv/IEEE/ACM publications.
 *
 * <p>Specialized for:</p>
 * <ul>
 *   <li>Japanese graduate programs requiring informal supervisor consent (内諾, Naidaku)</li>
 *   <li>US and European research MSc and thesis tracks</li>
 *   <li>Actionable literature gap critique and automated cold outreach email drafting</li>
 * </ul>
 */
@Service
public class FacultyAlignmentService {

    private final List<FacultyMember> facultyDatabase = new ArrayList<>();

    // Domain concept clusters for CS/AI soft semantic matching
    private static final Map<String, List<String>> CONCEPT_CLUSTERS = Map.of(
        "COMPUTER_VISION", List.of("vision", "image", "visual", "diffusion", "segmentation", "object detection",
                "nerf", "radiance", "3d", "reconstruction", "slam", "camera", "multimodal", "generative", "video", "cvpr"),
        "NLP", List.of("nlp", "language", "llm", "text", "translation", "transformer", "semantic", "dialogue",
                "speech", "conversational", "prompt", "reasoning", "retrieval", "rag", "summarization", "acl", "emnlp"),
        "ROBOTICS", List.of("robot", "robotics", "manipulation", "locomotion", "reinforcement", "policy", "control",
                "embodied", "planning", "autonomous", "sensors", "kinematics", "icra", "iros"),
        "MACHINE_LEARNING", List.of("deep learning", "neural", "bayesian", "representation", "optimization", "graph",
                "gnn", "causal", "foundation", "generalization", "unsupervised", "contrastive", "neurips", "icml")
    );

    public FacultyAlignmentService() {
        initFacultyDatabase();
    }

    /**
     * Returns all faculty members in the system, optionally filtered by university or country.
     */
    public List<FacultyMember> getAllFaculty(String universityFilter, String countryFilter) {
        return facultyDatabase.stream()
                .filter(f -> {
                    if (universityFilter != null && !universityFilter.isBlank()) {
                        return f.getUniversityName().toLowerCase().contains(universityFilter.trim().toLowerCase());
                    }
                    return true;
                })
                .filter(f -> {
                    if (countryFilter != null && !countryFilter.isBlank()) {
                        return f.getCountry().toLowerCase().contains(countryFilter.trim().toLowerCase());
                    }
                    return true;
                })
                .toList();
    }

    /**
     * Finds a single faculty member by their unique ID or fuzzy name identifier.
     */
    public Optional<FacultyMember> getFacultyById(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        String clean = id.trim().toLowerCase();
        String noHyphen = clean.replace("-", "").replace("_", "");
        return facultyDatabase.stream()
                .filter(f -> f.getId().equalsIgnoreCase(clean) ||
                             f.getId().toLowerCase().replace("-", "").contains(noHyphen) ||
                             f.getName().toLowerCase().contains(clean))
                .findFirst();
    }

    /**
     * Performs deep semantic alignment critique on the candidate's SOP or research proposal.
     */
    public AlignmentAnalysisResult analyzeAlignment(AlignmentAnalysisRequest request) {
        String sop = request.getSopText() != null ? request.getSopText().trim() : "";
        if (sop.isBlank()) {
            throw new IllegalArgumentException("Statement of Purpose or research proposal text is required.");
        }

        int wordCount = sop.split("\\s+").length;
        String candidateName = (request.getCandidateName() != null && !request.getCandidateName().isBlank())
                ? request.getCandidateName().trim() : "Prospective Candidate";
        String candidateMajor = (request.getUndergradMajor() != null && !request.getUndergradMajor().isBlank())
                ? request.getUndergradMajor().trim() : "B.Tech in Computer Science & Engineering";

        // Candidate detected keywords and themes
        Set<String> candidateTokens = extractSalientTerms(sop);
        List<String> detectedThemes = detectThemes(candidateTokens);

        // Filter candidate faculty pools
        List<FacultyMember> pool = facultyDatabase.stream()
                .filter(f -> {
                    if (request.getTargetUniversityName() != null && !request.getTargetUniversityName().isBlank()) {
                        String target = request.getTargetUniversityName().trim().toLowerCase();
                        if (!f.getUniversityName().toLowerCase().contains(target)) return false;
                    }
                    if (request.getTargetCountry() != null && !request.getTargetCountry().isBlank()) {
                        String c = request.getTargetCountry().trim().toLowerCase();
                        if (c.contains("japan") && !f.getCountry().equalsIgnoreCase("Japan")) return false;
                        if ((c.contains("us") || c.contains("usa")) && !f.getCountry().equalsIgnoreCase("USA")) return false;
                        if ((c.contains("germany") || c.contains("europe")) &&
                            !(f.getCountry().equalsIgnoreCase("Germany") || f.getCountry().equalsIgnoreCase("Switzerland"))) return false;
                    }
                    return true;
                })
                .toList();

        if (pool.isEmpty()) {
            pool = facultyDatabase; // Fallback to entire global pool if filter yielded no exact hit
        }

        List<FacultyMatch> matches = new ArrayList<>();
        Set<String> allOverlappingKeywords = new LinkedHashSet<>();
        Set<String> allMissingKeywords = new LinkedHashSet<>();

        for (FacultyMember faculty : pool) {
            FacultyMatch match = scoreFacultyAlignment(sop, candidateTokens, faculty);
            matches.add(match);
            allOverlappingKeywords.addAll(match.getMatchingKeywords());
        }

        // Rank by match score descending
        matches.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        List<FacultyMatch> topMatches = matches.stream().limit(4).toList();

        // Calculate aggregate overall alignment score based on best matched laboratory
        double overallScore = !topMatches.isEmpty() ? topMatches.get(0).getMatchScore() : 40.0;

        String tier;
        if (overallScore >= 80.0) tier = "STRONG_ALIGNMENT";
        else if (overallScore >= 65.0) tier = "MODERATE_ALIGNMENT";
        else if (overallScore >= 45.0) tier = "POTENTIAL_ALIGNMENT";
        else tier = "WEAK_ALIGNMENT";

        // Aggregate missing high-value lab keywords
        if (!topMatches.isEmpty()) {
            FacultyMember topProf = topMatches.get(0).getFaculty();
            for (String theme : topProf.getResearchThemes()) {
                if (!candidateTokens.contains(theme.toLowerCase())) {
                    allMissingKeywords.add(theme);
                }
            }
            for (ResearchPaper p : topProf.getRecentPublications()) {
                for (String kw : p.getKeywords()) {
                    if (!candidateTokens.contains(kw.toLowerCase())) {
                        allMissingKeywords.add(kw);
                    }
                }
            }
        }

        // Generate customized critique
        List<String> critique = generateCritique(sop, wordCount, overallScore, topMatches, allMissingKeywords);

        // Japanese Naidaku Guidance
        String naidakuGuidance = generateNaidakuGuidance(topMatches);

        // Pre-generate cold outreach email for the top faculty
        ColdEmailDraft coldEmail = null;
        if (!topMatches.isEmpty()) {
            FacultyMatch top = topMatches.get(0);
            coldEmail = generateColdEmail(top.getFaculty(), candidateName, candidateMajor, sop, top.getRelevantPapers());
        }

        String targetUni = request.getTargetUniversityName() != null && !request.getTargetUniversityName().isBlank()
                ? request.getTargetUniversityName() : (!topMatches.isEmpty() ? topMatches.get(0).getFaculty().getUniversityName() : "Target University");

        return new AlignmentAnalysisResult(
            overallScore,
            tier,
            targetUni,
            wordCount,
            detectedThemes,
            topMatches,
            new ArrayList<>(allOverlappingKeywords).stream().limit(8).toList(),
            new ArrayList<>(allMissingKeywords).stream().limit(8).toList(),
            critique,
            naidakuGuidance,
            coldEmail
        );
    }

    /**
     * Generates a cold email outreach draft for a specific professor.
     */
    public ColdEmailDraft generateColdEmail(FacultyMember faculty, String candidateName, String candidateMajor,
                                           String sopText, List<ResearchPaper> relevantPapers) {
        String paperTitle = (!relevantPapers.isEmpty())
                ? relevantPapers.get(0).getTitle()
                : (faculty.getRecentPublications().isEmpty() ? "recent laboratory publications" : faculty.getRecentPublications().get(0).getTitle());

        boolean isJapan = faculty.getCountry().equalsIgnoreCase("Japan");
        String subject;
        StringBuilder body = new StringBuilder();

        if (isJapan) {
            subject = "Prospective Research Student Inquiry (Fall 2027) — " + candidateName + " | Research Alignment with " + faculty.getLabName();

            body.append("Dear Professor ").append(faculty.getName().replace("Prof. ", "")).append(" Sensei,\n\n");
            body.append("I hope this email finds you well. My name is ").append(candidateName).append(", and I am completing my ")
                .append(candidateMajor).append(". I am writing to respectfully express my strong interest in joining your ")
                .append(faculty.getLabName()).append(" at ").append(faculty.getUniversityName())
                .append(" as an international research student (Kenkyusei) / Master's student for the upcoming academic cycle.\n\n");

            body.append("I have been following your laboratory's work with great admiration, particularly your recent publication, \"")
                .append(paperTitle).append("\". Inspired by your approach, my proposed research centers on ");

            // Extract a 1-sentence hook from candidate SOP
            body.append(extractHookSentence(sopText)).append(". I believe your lab's active direction in ")
                .append(String.join(", ", faculty.getResearchThemes().stream().limit(2).toList()))
                .append(" offers the ideal environment to conduct rigorous research in this area.\n\n");

            body.append("I plan to apply through the MEXT Japanese Government Scholarship (University Recommendation track) / self-funded route. ")
                .append("Could you kindly advise if you are accepting international graduate students for this intake?\n\n");

            body.append("I have attached my detailed Research Proposal (Kenkyu-keikakusho), CV, and official transcripts for your review. ")
                .append("I would be immensely grateful for the opportunity to briefly speak with you or present my research ideas at your convenience.\n\n");

            body.append("Thank you very much for your time and consideration.\n\n")
                .append("Respectfully yours,\n")
                .append(candidateName).append("\n")
                .append(candidateMajor).append("\n");

            String protocol = "🇯🇵 Japanese Academic Etiquette: In Japan, professor prior consent (内諾, Naidaku) is essential. "
                    + "Always address the professor respectfully as 'Sensei'. Attach your research proposal, CV, and transcripts in a single PDF. "
                    + "Best time to send: Tuesday to Thursday morning (9:00 AM – 11:00 AM JST). If no response after 10 business days, send a single polite follow-up.";

            return new ColdEmailDraft(faculty.getName(), faculty.getEmail(), subject, body.toString(), paperTitle, protocol);

        } else {
            // US / Europe Academic Style
            subject = "Prospective MS Thesis Student Inquiry — " + candidateName + " (Research Alignment in " + (faculty.getResearchThemes().isEmpty() ? "CS" : faculty.getResearchThemes().get(0)) + ")";

            body.append("Dear Professor ").append(faculty.getName().replace("Prof. ", "")).append(",\n\n");
            body.append("My name is ").append(candidateName).append(", a senior undergraduate in ").append(candidateMajor).append(". ")
                .append("I am applying for the MS program in Computer Science at ").append(faculty.getUniversityName())
                .append(" and am writing to inquire about research assistantship (RA) opportunities in your group, the ")
                .append(faculty.getLabName()).append(".\n\n");

            body.append("I recently read your paper, \"").append(paperTitle).append("\", and was particularly impressed by your methodology. ")
                .append("My previous research experience focuses on ").append(extractHookSentence(sopText))
                .append(", which closely aligns with your laboratory's focus on ")
                .append(String.join(" and ", faculty.getResearchThemes().stream().limit(2).toList())).append(".\n\n");

            body.append("I have attached my CV, code repositories, and research statement. ")
                .append("Do you anticipate having openings for graduate research students in your lab for the upcoming academic year?\n\n");

            body.append("Thank you for your time and guidance.\n\n")
                .append("Best regards,\n")
                .append(candidateName).append("\n")
                .append(candidateMajor).append("\n");

            String protocol = "🇺🇸 US/Europe Academic Etiquette: Keep emails concise (under 250 words). Faculty receive dozens of inquiries weekly. "
                    + "Hook them in the first two sentences by referencing their exact recent paper and linking your GitHub/Google Scholar. Avoid generic flatteries.";

            return new ColdEmailDraft(faculty.getName(), faculty.getEmail(), subject, body.toString(), paperTitle, protocol);
        }
    }

    // ── Internal Scoring & NLP Logic ─────────────────────────────────────────

    private FacultyMatch scoreFacultyAlignment(String sop, Set<String> candidateTokens, FacultyMember faculty) {
        Set<String> labTokens = new HashSet<>();
        for (String theme : faculty.getResearchThemes()) {
            labTokens.addAll(extractSalientTerms(theme));
        }
        for (ResearchPaper p : faculty.getRecentPublications()) {
            labTokens.addAll(extractSalientTerms(p.getTitle()));
            labTokens.addAll(extractSalientTerms(p.getAbstractSnippet()));
            for (String kw : p.getKeywords()) {
                labTokens.addAll(extractSalientTerms(kw));
            }
        }

        // 1. Direct Term Overlap
        Set<String> overlap = new HashSet<>(candidateTokens);
        overlap.retainAll(labTokens);

        // Filter trivial words
        List<String> cleanOverlap = overlap.stream()
                .filter(t -> t.length() > 3)
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();

        // 2. Compute Cosine Vector Similarity
        double cosine = computeCosineSimilarity(candidateTokens, labTokens);

        // 3. Cluster Soft-Matching Bonus
        double clusterBonus = computeClusterBonus(candidateTokens, labTokens);

        // 4. Paper Title Direct Hit Bonus
        List<ResearchPaper> relevantPapers = new ArrayList<>();
        double paperBonus = 0.0;
        String sopLower = sop.toLowerCase();

        for (ResearchPaper p : faculty.getRecentPublications()) {
            double pMatch = 0.0;
            for (String kw : p.getKeywords()) {
                if (sopLower.contains(kw.toLowerCase())) pMatch += 1.0;
            }
            if (pMatch > 0 || sopLower.contains(p.getTitle().toLowerCase().split(" ")[0])) {
                relevantPapers.add(p);
                paperBonus += 4.0;
            }
        }
        if (relevantPapers.isEmpty() && !faculty.getRecentPublications().isEmpty()) {
            relevantPapers.add(faculty.getRecentPublications().get(0));
        }

        // Calibrate raw score to 0-100 range
        double rawScore = 20.0 + (cosine * 70.0) + (cleanOverlap.size() * 3.5) + (clusterBonus * 15.0) + Math.min(paperBonus, 15.0);
        double finalScore = Math.min(96.0, Math.max(20.0, Math.round(rawScore * 10.0) / 10.0));

        String rationale = generateRationale(faculty, cleanOverlap, finalScore, relevantPapers);

        return new FacultyMatch(faculty, finalScore, cleanOverlap, relevantPapers, rationale);
    }

    private double computeCosineSimilarity(Set<String> v1, Set<String> v2) {
        if (v1.isEmpty() || v2.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(v1);
        intersection.retainAll(v2);
        return (double) intersection.size() / Math.sqrt((double) v1.size() * v2.size());
    }

    private double computeClusterBonus(Set<String> v1, Set<String> v2) {
        double bonus = 0.0;
        for (List<String> cluster : CONCEPT_CLUSTERS.values()) {
            boolean v1Has = cluster.stream().anyMatch(v1::contains);
            boolean v2Has = cluster.stream().anyMatch(v2::contains);
            if (v1Has && v2Has) {
                bonus += 0.25;
            }
        }
        return Math.min(1.0, bonus);
    }

    private Set<String> extractSalientTerms(String text) {
        if (text == null) return Collections.emptySet();
        Set<String> terms = new HashSet<>();
        Pattern p = Pattern.compile("[a-zA-Z]{3,}");
        Matcher m = p.matcher(text.toLowerCase());
        while (m.find()) {
            String word = m.group();
            if (!isStopWord(word)) {
                terms.add(word);
            }
        }
        return terms;
    }

    private boolean isStopWord(String w) {
        return Set.of(
            "the", "and", "for", "with", "that", "this", "from", "have", "been", "will", "would",
            "which", "their", "about", "into", "through", "after", "before", "research", "study",
            "work", "project", "program", "university", "student", "applicant", "interest", "interested"
        ).contains(w);
    }

    private List<String> detectThemes(Set<String> tokens) {
        List<String> themes = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : CONCEPT_CLUSTERS.entrySet()) {
            long count = entry.getValue().stream().filter(tokens::contains).count();
            if (count >= 2) {
                themes.add(formatThemeName(entry.getKey()));
            }
        }
        if (themes.isEmpty()) themes.add("Artificial Intelligence & Systems");
        return themes;
    }

    private String formatThemeName(String key) {
        return switch (key) {
            case "COMPUTER_VISION" -> "Computer Vision & Generative AI";
            case "NLP" -> "Natural Language Processing & Large Language Models";
            case "ROBOTICS" -> "Embodied AI & Robotics";
            case "MACHINE_LEARNING" -> "Deep Learning & Representation Theory";
            default -> key;
        };
    }

    private String generateRationale(FacultyMember faculty, List<String> overlap, double score, List<ResearchPaper> papers) {
        StringBuilder sb = new StringBuilder();
        if (score >= 80.0) {
            sb.append("Exceptional topical alignment. ");
        } else if (score >= 65.0) {
            sb.append("Strong technical overlap. ");
        } else {
            sb.append("Moderate foundational overlap. ");
        }

        if (!overlap.isEmpty()) {
            sb.append("Your proposal shares key concepts: ").append(String.join(", ", overlap.stream().limit(4).toList())).append(". ");
        }

        if (!papers.isEmpty()) {
            sb.append("Directly connects with lab publication \"").append(papers.get(0).getTitle()).append("\".");
        }
        return sb.toString();
    }

    private List<String> generateCritique(String sop, int wordCount, double score, List<FacultyMatch> matches, Set<String> missingKeywords) {
        List<String> crit = new ArrayList<>();

        if (wordCount < 250) {
            crit.add("Statement Length Warning: Your proposal is relatively brief (" + wordCount + " words). Top labs expect a detailed 500–1000 word proposal covering background, problem formulation, methodology, and expected results.");
        } else if (wordCount > 1500) {
            crit.add("Conciseness Suggestion: At " + wordCount + " words, your document may be too long for initial professor scanning. Aim to trim the introduction and frontload your specific research questions.");
        }

        if (!missingKeywords.isEmpty()) {
            crit.add("Literature Gap: Target faculty labs are actively publishing in [" + String.join(", ", missingKeywords.stream().limit(4).toList()) + "]. Weaving these topics or citing recent 2023–2024 lab publications will significantly raise your profile's relevance.");
        }

        if (!sop.toLowerCase().contains("evaluat") && !sop.toLowerCase().contains("benchmark") && !sop.toLowerCase().contains("dataset")) {
            crit.add("Methodology Gap: Your text discusses high-level conceptual ideas but lacks concrete experimental validation plans. Specify target datasets, benchmark suites, or simulation environments (e.g. ImageNet, HuggingFace, Isaac Sim).");
        }

        if (!sop.toLowerCase().contains("limitat") && !sop.toLowerCase().contains("challeng")) {
            crit.add("Analytical Depth: Acknowledge current limitations in existing state-of-the-art approaches to demonstrate mature critical thinking.");
        }

        if (matches.size() >= 2) {
            FacultyMember f1 = matches.get(0).getFaculty();
            crit.add("Personalization Strategy: Tailor your opening paragraph specifically to " + f1.getName() + "'s " + f1.getLabName() + " to avoid looking like a mass-copied template email.");
        }

        return crit;
    }

    private String generateNaidakuGuidance(List<FacultyMatch> matches) {
        boolean hasJapan = matches.stream().anyMatch(m -> m.getFaculty().getCountry().equalsIgnoreCase("Japan"));
        if (!hasJapan) {
            return "For US and European programs, informal prior consent is not strictly required before portal submission, but contacting professors with strong alignment 4–6 weeks ahead can secure valuable Graduate Research Assistantships (RA).";
        }
        return "🎌 Japanese Laboratory Naidaku Protocol: In Japan, formal university acceptance requires securing prior informal supervisor approval (内諾, Naidaku). "
                + "Professors receive hundreds of emails; follow strict protocol: 1) Address the professor as Sensei; 2) Attach a 2–3 page research proposal (Kenkyu-keikakusho); "
                + "3) State whether you are applying via MEXT Scholarship or self-funded; 4) Send your initial inquiry 8–10 months before the April or October intake.";
    }

    private String extractHookSentence(String sop) {
        String[] sentences = sop.split("[.\\n]+");
        for (String s : sentences) {
            String clean = s.trim();
            if (clean.length() > 30 && clean.length() < 140 && (clean.toLowerCase().contains("aim") || clean.toLowerCase().contains("propose") || clean.toLowerCase().contains("focus") || clean.toLowerCase().contains("develop"))) {
                return clean;
            }
        }
        if (sentences.length > 0 && sentences[0].trim().length() > 20) {
            return sentences[0].trim();
        }
        return "advancing scalable and efficient machine intelligence systems";
    }

    // ── Seed Faculty & Lab Database ──────────────────────────────────────────

    private void initFacultyDatabase() {
        // ── 1. University of Tokyo (Japan) ──────────────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-TODAI-01",
            "Prof. Tatsuya Harada",
            "Professor & Lab Director",
            "University of Tokyo",
            "Graduate School of Information Science and Technology",
            "Machine Intelligence & Systems Laboratory (Harada Lab)",
            "https://www.mi.t.u-tokyo.ac.jp/",
            "Japan",
            "harada@mi.t.u-tokyo.ac.jp",
            List.of("Computer Vision", "Multimodal Learning", "Diffusion Models", "Embodied AI"),
            true, true,
            List.of(
                new ResearchPaper("Controllable 3D Generation via Latent Diffusion Models", "CVPR 2024 / arXiv:2403.01892", 2024, "https://arxiv.org/abs/2403.01892", "Presents a novel latent diffusion framework for multi-view consistent 3D asset generation from sparse camera inputs.", List.of("diffusion", "3d", "latent", "generative")),
                new ResearchPaper("Embodied Vision-Language Navigation in Dynamic Environments", "IEEE ICRA 2024", 2024, "https://arxiv.org/abs/2401.09452", "Investigates topological memory representations for mobile robot navigation using foundation multimodal models.", List.of("robotics", "multimodal", "navigation", "embodied"))
            ),
            "Address as 'Harada-Sensei'. Highlight specific background in PyTorch, 3D vision, or robot manipulation. Attach research proposal in English."
        ));

        facultyDatabase.add(new FacultyMember(
            "FAC-TODAI-02",
            "Prof. Issei Sato",
            "Associate Professor",
            "University of Tokyo",
            "Department of Computer Science / RIKEN AIP",
            "Statistical Machine Learning & Optimization Lab",
            "https://www.ms.k.u-tokyo.ac.jp/sato/",
            "Japan",
            "sato@ms.k.u-tokyo.ac.jp",
            List.of("Statistical Machine Learning", "Bayesian Deep Learning", "Causal Inference", "LLM Alignment"),
            true, true,
            List.of(
                new ResearchPaper("Provable Bayesian Optimization with Incomplete Preferences", "ICML 2024 / arXiv:2404.09121", 2024, "https://arxiv.org/abs/2404.09121", "Establishes sublinear regret bounds for preference-based Bayesian optimization in high-dimensional continuous domains.", List.of("bayesian", "optimization", "statistical", "regret")),
                new ResearchPaper("Causal Representation Learning for Robust OOD Generalization", "NeurIPS 2023", 2023, "https://arxiv.org/abs/2310.05432", "Proposes structural equation models to disentangle invariant features from spurious domain shifts.", List.of("causal", "representation", "generalization"))
            ),
            "Mathematical rigor is heavily weighted. Mention strong linear algebra and probability coursework in your statement."
        ));

        // ── 2. Kyoto University (Japan) ─────────────────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-KYOTO-01",
            "Prof. Tatsuya Kawahara",
            "Professor & Dean of Informatics",
            "Kyoto University",
            "Graduate School of Informatics",
            "Speech & Language Processing Laboratory",
            "http://sap.ist.i.kyoto-u.ac.jp/",
            "Japan",
            "kawahara@i.kyoto-u.ac.jp",
            List.of("Speech Recognition", "Conversational AI", "Spoken Dialogue Systems", "Multimodal NLP"),
            true, true,
            List.of(
                new ResearchPaper("Zero-Shot Cross-Lingual Speech-to-Speech Translation with LLMs", "IEEE/ACM TASLP 2024", 2024, "https://arxiv.org/abs/2402.08821", "Integrates discrete acoustic tokens with generative transformers for low-resource Indian and Asian languages.", List.of("speech", "translation", "llm", "acoustic")),
                new ResearchPaper("Affective Conversational Agents with Empathetic Response Generation", "ACL 2023", 2023, "https://arxiv.org/abs/2307.03912", "Models interlocutor prosody and facial cues for nuanced empathetic response ranking.", List.of("conversational", "dialogue", "multimodal", "nlp"))
            ),
            "Laboratory highly active in speech, audio, and language. Excellent opportunity for applicants with multilingual background."
        ));

        // ── 3. Osaka University (Japan) ─────────────────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-OSAKA-01",
            "Prof. Yasushi Yagi",
            "Distinguished Professor",
            "Osaka University",
            "Institute of Scientific and Industrial Research (ISIR)",
            "Computer Vision & Cognitive Robotics Laboratory",
            "http://www.am.sanken.osaka-u.ac.jp/",
            "Japan",
            "yagi@am.sanken.osaka-u.ac.jp",
            List.of("Computer Vision", "Gait Recognition", "Biometrics", "Autonomous Robot Perception"),
            true, true,
            List.of(
                new ResearchPaper("GaitSet-3D: Self-Supervised Gait Representation from Uncalibrated Cameras", "IEEE TPAMI 2024", 2024, "https://arxiv.org/abs/2401.12034", "World-leading biometric person identification benchmark using temporal 3D silhouette representations.", List.of("gait", "biometrics", "vision", "temporal")),
                new ResearchPaper("Omnidirectional Visual Odometry in Complex Crowds", "IEEE ICRA 2024", 2024, "https://arxiv.org/abs/2402.15091", "Robust SLAM algorithm using panoramic fisheye sensors for urban autonomous delivery robots.", List.of("slam", "robotics", "odometry", "autonomous"))
            ),
            "Yagi-sensei pioneered gait recognition globally. Mention familiarity with OpenCV, PyTorch, and geometric computer vision."
        ));

        // ── 4. MIT (USA) ────────────────────────────────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-MIT-01",
            "Prof. Regina Barzilay",
            "School of Engineering Distinguished Professor",
            "MIT",
            "EECS / CSAIL & J-Clinic",
            "AI for Biology & Drug Discovery Lab",
            "https://people.csail.mit.edu/regina/",
            "USA",
            "regina@csail.mit.edu",
            List.of("NLP for Healthcare", "Molecular Diffusion", "Graph Neural Networks", "Drug Discovery"),
            true, false,
            List.of(
                new ResearchPaper("Equivariant Diffusion for Target-Specific Molecular Docking", "NeurIPS 2023 / arXiv:2310.01239", 2023, "https://arxiv.org/abs/2310.01239", "Geometric graph neural networks for pocket-conditioned de novo ligand generation.", List.of("diffusion", "graph", "molecules", "geometric")),
                new ResearchPaper("Cross-Lingual Clinical Fact Verification via Structured LLM Reasoning", "EMNLP 2024", 2024, "https://arxiv.org/abs/2403.09881", "Enhances electronic health record QA accuracy across multilingual clinical datasets.", List.of("clinical", "llm", "reasoning", "nlp"))
            ),
            "Keep outreach under 200 words. Reference specific biomedical or molecular ML projects you have built."
        ));

        facultyDatabase.add(new FacultyMember(
            "FAC-MIT-02",
            "Prof. Daniela Rus",
            "Director of CSAIL & Professor of EECS",
            "MIT",
            "Computer Science and Artificial Intelligence Laboratory (CSAIL)",
            "Distributed Robotics Laboratory",
            "https://danielarus.csail.mit.edu/",
            "USA",
            "rus@csail.mit.edu",
            List.of("Distributed Robotics", "Embodied AI", "Liquid Neural Networks", "Autonomous Vehicles"),
            true, false,
            List.of(
                new ResearchPaper("Liquid State-Space Models for Real-Time End-to-End Autonomous Driving", "Science Robotics 2024", 2024, "https://arxiv.org/abs/2402.04321", "Introduces continuous-time liquid neural architectures that generalize across weather shifts with 10x fewer parameters.", List.of("liquid", "autonomous", "robotics", "embodied")),
                new ResearchPaper("Adaptive Soft Manipulation Using Vision-Tactile Sensor Networks", "IEEE ICRA 2024", 2024, "https://arxiv.org/abs/2404.11029", "Tactile sensing and closed-loop reinforcement learning for delicate food and medical item handling.", List.of("manipulation", "tactile", "reinforcement", "robotics"))
            ),
            "Daniela Rus receives heavy volume. Reference her work on liquid networks or soft manipulation and link your GitHub."
        ));

        // ── 5. Stanford University (USA) ────────────────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-STANFORD-01",
            "Prof. Christopher Manning",
            "Thomas M. Siebel Professor in Machine Learning",
            "Stanford University",
            "Computer Science & Linguistics",
            "Stanford NLP Group",
            "https://nlp.stanford.edu/~manning/",
            "USA",
            "manning@cs.stanford.edu",
            List.of("Natural Language Processing", "Large Language Models", "Compositional Semantics", "Grammar Inductive Bias"),
            true, false,
            List.of(
                new ResearchPaper("Mechanistic Interpretability of Transformer Knowledge Retrieval", "ACL 2024 / arXiv:2403.08761", 2024, "https://arxiv.org/abs/2403.08761", "Maps internal transformer attention heads responsible for associative factual recall across multilingual prompts.", List.of("transformer", "llm", "interpretability", "nlp")),
                new ResearchPaper("Context Compression and State Decay in Long-Sequence Language Models", "ICLR 2024", 2024, "https://arxiv.org/abs/2401.07843", "Theoretical evaluation of attention decay in 1M+ token context windows.", List.of("llm", "compression", "attention", "nlp"))
            ),
            "Stanford NLP prioritizes deep linguistic grounding and rigorous evaluation. Highlight theoretical understanding of attention mechanisms."
        ));

        facultyDatabase.add(new FacultyMember(
            "FAC-STANFORD-02",
            "Prof. Fei-Fei Li",
            "Sequoia Professor & Co-Director Stanford HAI",
            "Stanford University",
            "Computer Science Department",
            "Stanford Vision and Learning Lab (SVL)",
            "https://profiles.stanford.edu/fei-fei-li",
            "USA",
            "feifeili@cs.stanford.edu",
            List.of("Spatial Intelligence", "3D Computer Vision", "Embodied AI", "Physical World Foundation Models"),
            true, false,
            List.of(
                new ResearchPaper("BEHAVIOR-1K: A Benchmark for Embodied AI with 1,000 Everyday Activities", "IEEE TPAMI 2024", 2024, "https://arxiv.org/abs/2403.02981", "Simulation platform with realistic physical dynamics for robotic task learning in human environments.", List.of("embodied", "simulation", "benchmark", "robotics")),
                new ResearchPaper("Spatial Intelligence: Neural Radiance Fields and 3D Scene Graphs for Robot Reasoning", "CVPR 2024", 2024, "https://arxiv.org/abs/2404.14562", "Synthesizes multi-modal visual observations into interactive 3D affordance maps.", List.of("spatial", "3d", "nerf", "vision"))
            ),
            "Focus on 'Spatial Intelligence' and embodied robotics. Clearly articulate how physical reasoning differentiates your proposal."
        ));

        // ── 6. Carnegie Mellon University (USA) ──────────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-CMU-01",
            "Prof. Graham Neubig",
            "Associate Professor",
            "CMU",
            "Language Technologies Institute (LTI)",
            "NeuLab — Natural Language Processing & Code Generation",
            "http://www.phontron.com/",
            "USA",
            "gneubig@cs.cmu.edu",
            List.of("Multilingual NLP", "Code Generation LLMs", "Autonomous Agents", "Interactive Learning"),
            true, false,
            List.of(
                new ResearchPaper("SWE-bench: Can Language Models Resolve Real-World GitHub Issues?", "ICLR 2024 / arXiv:2310.06770", 2024, "https://arxiv.org/abs/2310.06770", "Benchmark evaluating end-to-end repository software engineering capabilities of LLMs.", List.of("code", "benchmark", "agents", "llm")),
                new ResearchPaper("Open-Source Foundation Models for Indic Code Translation", "EMNLP 2024", 2024, "https://arxiv.org/abs/2404.03219", "Fine-tuning methodologies for multilingual code understanding and generation.", List.of("multilingual", "code", "nlp"))
            ),
            "Graham actively welcomes open-source contributors. Linking pull requests to open-source LLM/agent repos is a huge boost."
        ));

        // ── 7. Technical University of Munich (Germany) ──────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-TUM-01",
            "Prof. Daniel Cremers",
            "Chair of Computer Vision and Artificial Intelligence",
            "TU Munich",
            "Department of Informatics & Munich Data Science Institute (MDSI)",
            "Computer Vision & Pattern Recognition Group",
            "https://vision.in.tum.de/members/cremers",
            "Germany",
            "cremers@in.tum.de",
            List.of("3D Reconstruction", "Visual SLAM", "Neural Radiance Fields", "Autonomous Driving"),
            true, false,
            List.of(
                new ResearchPaper("Direct Sparse Mapping with Continuous-Time Trajectories", "IEEE TPAMI 2024", 2024, "https://arxiv.org/abs/2401.07652", "Unified optimization framework for monocular and stereo visual odometry with sub-millimeter precision.", List.of("slam", "odometry", "3d", "vision")),
                new ResearchPaper("Gaussian Splatting in Dynamic Urban Environments", "CVPR 2024", 2024, "https://arxiv.org/abs/2403.18902", "Real-time rendering and dynamic scene decomposition for autonomous vehicles.", List.of("gaussian", "splatting", "rendering", "autonomous"))
            ),
            "Cremers' group is one of Europe's top CV labs. Solid linear algebra, C++, and CUDA experience must be highlighted."
        ));

        facultyDatabase.add(new FacultyMember(
            "FAC-TUM-02",
            "Prof. Stephan Günnemann",
            "Professor & Executive Director MDSI",
            "TU Munich",
            "Department of Informatics",
            "Data Analytics and Machine Learning Group",
            "https://www.daml.in.tum.de/guennemann",
            "Germany",
            "guennemann@in.tum.de",
            List.of("Graph Neural Networks", "Geometric Deep Learning", "Robust Machine Learning", "Molecular AI"),
            true, false,
            List.of(
                new ResearchPaper("Scalable Continuous Normalizing Flows on Riemannian Manifolds", "ICLR 2024 / arXiv:2402.09112", 2024, "https://arxiv.org/abs/2402.09112", "Equivariant generative modeling over non-Euclidean geometric structures.", List.of("geometric", "graph", "gnn", "manifold")),
                new ResearchPaper("Provable Robustness against Adversarial Topology Perturbations in GNNs", "NeurIPS 2023", 2023, "https://arxiv.org/abs/2311.08234", "Certificate bounds for graph representation learning under malicious edge injections.", List.of("graph", "robustness", "gnn", "adversarial"))
            ),
            "Emphasize strong mathematical foundations in graph theory, non-Euclidean geometry, or PyTorch Geometric."
        ));

        // ── 8. ETH Zurich (Switzerland / Europe) ─────────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-ETH-01",
            "Prof. Luc Van Gool",
            "Professor of Computer Vision",
            "ETH Zurich",
            "Department of Information Technology and Electrical Engineering",
            "Computer Vision Laboratory (CVL)",
            "https://ee.ethz.ch/the-department/people-a-z/person-detail.html?persid=39130",
            "Switzerland",
            "vangool@vision.ee.ethz.ch",
            List.of("Computer Vision", "Autonomous Driving", "Generative Visual Models", "3D Perception"),
            true, false,
            List.of(
                new ResearchPaper("Dense Visual Prediction with Foundation Models under Severe Weather", "CVPR 2024", 2024, "https://arxiv.org/abs/2404.09871", "Adversarial domain adaptation for safety-critical autonomous driving vision pipelines.", List.of("vision", "autonomous", "segmentation", "domain")),
                new ResearchPaper("Controllable Human Avatar Generation from Single Monocular Video", "IEEE TPAMI 2024", 2024, "https://arxiv.org/abs/2402.13451", "Photorealistic dynamic avatar rendering using 3D Gaussians and parametric body models.", List.of("avatar", "3d", "rendering", "generative"))
            ),
            "ETH Zurich expects top percentiles and strong technical publications. Highlight systems-level implementation skills."
        ));

        // ── 9. UPC BarcelonaTech (Spain) ────────────────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-UPC-01",
            "Prof. Xavier Giro-i-Nieto",
            "Associate Professor & AI Lab Researcher",
            "UPC BarcelonaTech",
            "Department of Signal Theory and Communications",
            "Image Processing Group (GPI) & Video AI Lab",
            "https://imatge.upc.edu/web/people/xavier-giro",
            "Spain",
            "xavier.giro@upc.edu",
            List.of("Computer Vision", "Generative Video Diffusion", "Multimodal Learning", "Deep Learning"),
            true, false,
            List.of(
                new ResearchPaper("Action-Conditioned Video Diffusion Models with Spatio-Temporal Guidance", "CVPR 2024 / arXiv:2403.04521", 2024, "https://arxiv.org/abs/2403.04521", "Controllable latent video generation using decoupled spatial and temporal transformer blocks.", List.of("diffusion", "video", "generative", "vision")),
                new ResearchPaper("Self-Supervised Representation Learning on Egocentric Multimodal Streams", "ICCV 2023", 2023, "https://arxiv.org/abs/2308.01234", "Cross-modal contrastive learning aligning first-person wearable audio, gaze, and video feeds.", List.of("egocentric", "multimodal", "video", "contrastive"))
            ),
            "UPC BarcelonaTech is a premier Southern European engineering powerhouse. Mention hands-on PyTorch and video generation or vision experience."
        ));

        // ── 10. Universidad Politécnica de Madrid (Spain) ───────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-UPM-01",
            "Prof. Asunción Gómez-Pérez",
            "Professor & Vice-Rector for Research",
            "Universidad Politécnica de Madrid",
            "ETSI Informáticos",
            "Ontology Engineering Group (OEG)",
            "https://oeg.fi.upm.es/",
            "Spain",
            "asuncion.gomez@upm.es",
            List.of("Knowledge Graphs", "Neurosymbolic AI", "Semantic Web", "Natural Language Processing"),
            true, false,
            List.of(
                new ResearchPaper("Neurosymbolic Reasoning via Knowledge Graph Augmented Large Language Models", "ISWC 2024 / arXiv:2404.12098", 2024, "https://arxiv.org/abs/2404.12098", "Unifies vector embeddings and ontological knowledge graphs to suppress hallucination in medical QA.", List.of("knowledge", "graphs", "llm", "semantic")),
                new ResearchPaper("Cross-Lingual Entity Alignment across Distributed Ontological Repositories", "ACM TWEB 2023", 2023, "https://arxiv.org/abs/2309.07654", "Scalable Graph Convolutional Networks for aligning international open linked datasets.", List.of("entity", "graph", "gnn", "ontology"))
            ),
            "UPM's OEG is world-renowned for ontology and knowledge graphs. Highlight experience with Neo4j, RDF, SPARQL, and neurosymbolic methods."
        ));

        // ── 11. Instituto Superior Técnico - IST Lisbon (Portugal) ───────────
        facultyDatabase.add(new FacultyMember(
            "FAC-IST-01",
            "Prof. Mário Figueiredo",
            "Distinguished Professor & Chair of AI",
            "Instituto Superior Técnico",
            "Department of Electrical and Computer Engineering",
            "Pattern and Image Analysis Group (LxDataLab) / IT Lisbon",
            "https://www.it.pt/Members/Index/331",
            "Portugal",
            "mario.figueiredo@tecnico.ulisboa.pt",
            List.of("Statistical Machine Learning", "Optimization", "Confidence Calibration in LLMs", "Inverse Problems"),
            true, false,
            List.of(
                new ResearchPaper("Confidence Calibration and Selective Prediction in Large Language Models", "ACL 2024 / arXiv:2404.09876", 2024, "https://arxiv.org/abs/2404.09876", "Post-hoc uncertainty estimation methods preventing overconfident hallucinations in reasoning chains.", List.of("calibration", "uncertainty", "llm", "optimization")),
                new ResearchPaper("Structured Sparsity and Non-Convex Optimization for Efficient Deep Networks", "NeurIPS 2023", 2023, "https://arxiv.org/abs/2311.08901", "Proximal gradient descent algorithms with proven convergence for network compression.", List.of("sparsity", "optimization", "compression", "theory"))
            ),
            "Prof. Figueiredo is an IEEE & ACM Fellow and Portugal's foremost ML theorist. Solid mathematical grounding in convex optimization is strongly appreciated."
        ));

        // ── 12. University of Porto (Portugal) ──────────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-UPORTO-01",
            "Prof. Alípio Jorge",
            "Full Professor of Computer Science",
            "University of Porto",
            "Department of Computer Science (FEUP)",
            "Laboratory of Artificial Intelligence and Decision Support (LIAAD)",
            "https://liaad.inesctec.pt/",
            "Portugal",
            "alipio.jorge@fe.up.pt",
            List.of("Machine Learning", "Recommender Systems", "Data Streams", "Temporal Graph Mining"),
            true, false,
            List.of(
                new ResearchPaper("Graph Neural Networks for Continual Recommendation on Dynamic User Streams", "ACM SIGKDD 2024", 2024, "https://arxiv.org/abs/2403.11902", "Memory-efficient temporal graph convolutions capturing evolving user preference drift.", List.of("recommender", "graph", "stream", "temporal")),
                new ResearchPaper("Automated Text Simplification with Multi-Objective Reinforcement Learning", "EACL 2023", 2023, "https://arxiv.org/abs/2304.05432", "RLHF reward modeling balancing syntactic readability with semantic information preservation.", List.of("rlhf", "nlp", "reinforcement", "text"))
            ),
            "FEUP Porto has strong industrial ties and INESC TEC research backing. Highlight applied machine learning and data engineering projects."
        ));

        // ── 13. Politecnico di Milano (Italy) ───────────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-POLIMI-01",
            "Prof. Barbara Caputo",
            "Professor of Computer Science & Robotics",
            "Politecnico di Milano",
            "Department of Electronics, Information and Bioengineering",
            "Artificial Intelligence & Robotics Laboratory (AIRLab)",
            "https://www.deib.polimi.it/ita/personale/docenti/dettaglio/273187",
            "Italy",
            "barbara.caputo@polimi.it",
            List.of("Embodied AI", "Continual Learning", "Domain Generalization", "Robot Vision"),
            true, false,
            List.of(
                new ResearchPaper("Continual Learning for Mobile Robot Vision in Unseen Indoor Environments", "IEEE ICRA 2024 / arXiv:2402.11223", 2024, "https://arxiv.org/abs/2402.11223", "Exemplar-free online adaptation preventing catastrophic forgetting during autonomous navigation.", List.of("continual", "robotics", "vision", "embodied")),
                new ResearchPaper("Source-Free Domain Adaptation for 3D Semantic Scene Segmentation", "CVPR 2024", 2024, "https://arxiv.org/abs/2404.01982", "Unsupervised point cloud labeling under sensor degradation without accessing original training samples.", List.of("pointcloud", "3d", "adaptation", "segmentation"))
            ),
            "PoliMi AIRLab is one of Italy's premier robotics and AI centers. Reference robotic perception or vision projects."
        ));

        // ── 14. IP Paris / Télécom Paris (France) ───────────────────────────
        facultyDatabase.add(new FacultyMember(
            "FAC-IPPARIS-01",
            "Prof. Gaël Varoquaux",
            "Research Director at Inria & Professor at IP Paris",
            "IP Paris",
            "Department of Computer Science & Data Science",
            "Parietal / Soda Team (Inria & IP Paris)",
            "https://gael-varoquaux.info/",
            "France",
            "gael.varoquaux@inria.fr",
            List.of("Data Science", "Scikit-Learn Architecture", "Statistical Machine Learning", "Medical AI"),
            true, false,
            List.of(
                new ResearchPaper("Table-Vector Foundations: Pretrained Embeddings for Dirty Tabular Data", "NeurIPS 2023 / arXiv:2307.01123", 2023, "https://arxiv.org/abs/2307.01123", "Self-supervised transformer representation learning over unstandardized relational database tables.", List.of("tabular", "embeddings", "data", "scikit")),
                new ResearchPaper("Validating Causal Biomarkers in Neuroimaging with Population-Scale Deep Models", "Nature Methods 2024", 2024, "https://arxiv.org/abs/2401.08765", "High-dimensional statistical validation pipelines for brain fMRI predictive modeling.", List.of("medical", "causal", "neuroscience", "statistics"))
            ),
            "Gaël Varoquaux is a core creator of scikit-learn. Express deep familiarity with open-source software, clean code, and empirical validation."
        ));
    }
}
