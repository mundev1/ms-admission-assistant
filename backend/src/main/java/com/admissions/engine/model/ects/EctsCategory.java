package com.admissions.engine.model.ects;

/**
 * Standard subject domains used by German public university admission committees
 * to evaluate undergraduate curriculum equivalence for Master's programs.
 */
public enum EctsCategory {
    THEORETICAL_CS(
        "Theoretical Computer Science",
        "🧠",
        "Automata theory, formal languages, computability, complexity theory, algorithms & data structures, graph theory."
    ),
    MATHEMATICS(
        "Higher Mathematics & Discrete Structures",
        "📐",
        "Linear algebra, calculus/analysis, discrete mathematics, numerical analysis, probability & statistics."
    ),
    PRACTICAL_SYSTEMS_CS(
        "Systems & Practical Computer Science",
        "💻",
        "Operating systems, computer architecture, database management systems, computer networks, distributed systems."
    ),
    SOFTWARE_ENGINEERING(
        "Software Engineering & Programming",
        "⚙️",
        "Object-oriented programming, software engineering, compiler design, design patterns, testing & QA."
    ),
    ELECTIVES_OTHER(
        "Advanced Electives & Applied CS",
        "🌐",
        "Artificial intelligence, machine learning, cloud computing, cyber security, computer graphics, final thesis/capstone."
    );

    private final String displayName;
    private final String emoji;
    private final String description;

    EctsCategory(String displayName, String emoji, String description) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getDescription() {
        return description;
    }
}
