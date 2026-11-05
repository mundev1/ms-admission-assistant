package com.admissions.engine.config;

import com.admissions.engine.model.CountryFramework;
import com.admissions.engine.model.ScoringWeights;
import com.admissions.engine.model.UniversityProgram;
import com.admissions.engine.repository.CountryFrameworkRepository;
import com.admissions.engine.repository.ScoringWeightsRepository;
import com.admissions.engine.repository.UniversityProgramRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

/**
 * Initializes default regional frameworks, scoring weights, and university programs
 * on startup if the database is unpopulated.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOG = Logger.getLogger(DataInitializer.class.getName());

    private final CountryFrameworkRepository  frameworkRepo;
    private final ScoringWeightsRepository     weightsRepo;
    private final UniversityProgramRepository  programRepo;

    public DataInitializer(CountryFrameworkRepository frameworkRepo,
                           ScoringWeightsRepository weightsRepo,
                           UniversityProgramRepository programRepo) {
        this.frameworkRepo = frameworkRepo;
        this.weightsRepo   = weightsRepo;
        this.programRepo   = programRepo;
    }

    @Override
    public void run(String... args) {
        if (frameworkRepo.count() > 0 && programRepo.count() >= 55) {
            LOG.info("Database already seeded with " + programRepo.count() + " programs. Skipping initialization.");
            return;
        }

        if (programRepo.count() > 0 && programRepo.count() < 55) {
            LOG.info("Refreshing database with expanded global university programs (Spain, Portugal, Italy, France, Ireland, Australia, etc.)...");
            programRepo.deleteAll();
        }

        LOG.info("Seeding admissions frameworks, regional weights, and university programs...");

        // 1. Frameworks
        CountryFramework usFramework = CountryFramework.builder()
                .frameworkId(1).region("US_CANADA").gpaScale(4.00).gpaLabel("US 4.0 GPA")
                .primaryTest("GRE").languageReq("TOEFL/IELTS").ectsRequired(false).researchProposal(false).build();

        CountryFramework euFramework = CountryFramework.builder()
                .frameworkId(2).region("EUROPE").gpaScale(4.00).gpaLabel("ECTS Grade")
                .primaryTest(null).languageReq("IELTS").ectsRequired(true).researchProposal(false).build();

        CountryFramework jpFramework = CountryFramework.builder()
                .frameworkId(3).region("JAPAN").gpaScale(4.00).gpaLabel("Japanese 4.0")
                .primaryTest("JLPT").languageReq("JLPT/TOEFL").ectsRequired(false).researchProposal(true).build();

        frameworkRepo.saveAll(List.of(usFramework, euFramework, jpFramework));

        // 2. Scoring Weights
        ScoringWeights usWeights = ScoringWeights.builder()
                .weightId(1).frameworkId(1).weightVersion("1.0")
                .weightGpa(0.30).weightGreVerbal(0.15).weightGreQuant(0.25).weightGreAw(0.05)
                .weightLanguage(0.10).weightEcts(0.00).weightResearch(0.00).weightCoursework(0.10).weightThesis(0.05)
                .isActive(true).notes("US/Canada default").build();

        ScoringWeights euWeights = ScoringWeights.builder()
                .weightId(2).frameworkId(2).weightVersion("1.0")
                .weightGpa(0.25).weightGreVerbal(0.00).weightGreQuant(0.00).weightGreAw(0.00)
                .weightLanguage(0.15).weightEcts(0.30).weightResearch(0.00).weightCoursework(0.10).weightThesis(0.20)
                .isActive(true).notes("Europe ECTS default").build();

        ScoringWeights jpWeights = ScoringWeights.builder()
                .weightId(3).frameworkId(3).weightVersion("1.0")
                .weightGpa(0.20).weightGreVerbal(0.00).weightGreQuant(0.00).weightGreAw(0.00)
                .weightLanguage(0.35).weightEcts(0.00).weightResearch(0.30).weightCoursework(0.10).weightThesis(0.05)
                .isActive(true).notes("Japan MEXT default").build();

        weightsRepo.saveAll(List.of(usWeights, euWeights, jpWeights));

        // 3. 19 University Programs
        List<UniversityProgram> programs = List.of(
            // US / Canada
            UniversityProgram.builder().universityName("MIT").programName("MS Electrical Engineering & CS")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS/AI")
                .thresholdGpa(3.8).thresholdGreV(163).thresholdGreQ(167).thresholdGreAw(5.0)
                .thresholdToefl(100).thresholdIelts(7.5).acceptanceRate(0.045).avgAcceptedGpa(3.90)
                .tuitionAnnualUsd(60000.0).livingCostAnnualUsd(25000.0).blockedAccountRequirementUsd(85000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("Fulbright, Graduate TA/RA, MIT Presidential Fellowship").build(),

            UniversityProgram.builder().universityName("Stanford University").programName("MS Computer Science")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS/AI")
                .thresholdGpa(3.7).thresholdGreV(160).thresholdGreQ(165).thresholdGreAw(4.5)
                .thresholdToefl(100).thresholdIelts(7.0).acceptanceRate(0.06).avgAcceptedGpa(3.85)
                .tuitionAnnualUsd(62000.0).livingCostAnnualUsd(26000.0).blockedAccountRequirementUsd(88000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("Fulbright, Graduate Assistantship, Knight-Hennessy Scholar").build(),

            UniversityProgram.builder().universityName("CMU").programName("MS Machine Learning")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("AI/ML")
                .thresholdGpa(3.75).thresholdGreV(162).thresholdGreQ(166).thresholdGreAw(4.5)
                .thresholdToefl(105).thresholdIelts(7.5).acceptanceRate(0.05).avgAcceptedGpa(3.88)
                .tuitionAnnualUsd(59000.0).livingCostAnnualUsd(22000.0).blockedAccountRequirementUsd(81000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("Fulbright, Departmental TA/RA Appointments").build(),

            UniversityProgram.builder().universityName("UC Berkeley").programName("MEng EECS")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS/AI")
                .thresholdGpa(3.6).thresholdGreV(158).thresholdGreQ(163).thresholdGreAw(4.0)
                .thresholdToefl(90).thresholdIelts(7.0).acceptanceRate(0.12).avgAcceptedGpa(3.75)
                .tuitionAnnualUsd(58000.0).livingCostAnnualUsd(25000.0).blockedAccountRequirementUsd(83000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("Fulbright, Fung Institute Excellence Fellowship").build(),

            UniversityProgram.builder().universityName("Cornell Tech").programName("MS Computer Science")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS")
                .thresholdGpa(3.5).thresholdGreV(155).thresholdGreQ(160).thresholdGreAw(4.0)
                .thresholdToefl(90).thresholdIelts(6.5).acceptanceRate(0.15).avgAcceptedGpa(3.70)
                .tuitionAnnualUsd(64000.0).livingCostAnnualUsd(27000.0).blockedAccountRequirementUsd(91000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("Fulbright, Cornell Tech Merit Scholarship").build(),

            UniversityProgram.builder().universityName("Georgia Tech").programName("MS Computer Science")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS")
                .thresholdGpa(3.0).thresholdGreV(153).thresholdGreQ(155).thresholdGreAw(3.5)
                .thresholdToefl(79).thresholdIelts(6.5).acceptanceRate(0.18).avgAcceptedGpa(3.55)
                .tuitionAnnualUsd(32000.0).livingCostAnnualUsd(18000.0).blockedAccountRequirementUsd(50000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("Fulbright, Graduate Teaching/Research Assistantship (GRA/GTA)").build(),

            UniversityProgram.builder().universityName("University of Toronto").programName("MScAC")
                .country("Canada").frameworkId(1).region("US_CANADA").specialization("CS/AI")
                .thresholdGpa(3.5).thresholdGreV(155).thresholdGreQ(160).thresholdGreAw(4.0)
                .thresholdToefl(93).thresholdIelts(6.5).acceptanceRate(0.12).avgAcceptedGpa(3.65)
                .tuitionAnnualUsd(35000.0).livingCostAnnualUsd(18000.0).blockedAccountRequirementUsd(15500.0)
                .postStudyVisaMonths(36).visaProgramName("Canada PGWP (36 Months Open Work Permit)")
                .scholarshipTypes("Vector Institute AI Scholarship, Ontario Graduate Scholarship (OGS)").build(),

            UniversityProgram.builder().universityName("University of Waterloo").programName("MASc CS")
                .country("Canada").frameworkId(1).region("US_CANADA").specialization("CS")
                .thresholdGpa(3.3).thresholdGreV(150).thresholdGreQ(155).thresholdGreAw(3.5)
                .thresholdToefl(90).thresholdIelts(6.5).acceptanceRate(0.20).avgAcceptedGpa(3.50)
                .tuitionAnnualUsd(30000.0).livingCostAnnualUsd(16000.0).blockedAccountRequirementUsd(15500.0)
                .postStudyVisaMonths(36).visaProgramName("Canada PGWP (36 Months Open Work Permit)")
                .scholarshipTypes("International Master's Award of Excellence (IMAE)").build(),

            // Australia
            UniversityProgram.builder().universityName("University of Melbourne").programName("Master of Computer Science")
                .country("Australia").frameworkId(1).region("US_CANADA").specialization("CS")
                .thresholdGpa(3.3).thresholdGreV(152).thresholdGreQ(158).thresholdGreAw(3.5)
                .thresholdToefl(85).thresholdIelts(6.5).acceptanceRate(0.24).avgAcceptedGpa(3.45)
                .tuitionAnnualUsd(32000.0).livingCostAnnualUsd(18000.0).blockedAccountRequirementUsd(16000.0)
                .postStudyVisaMonths(36).visaProgramName("Australia Subclass 485 Temporary Graduate Visa (3 Years)")
                .scholarshipTypes("Melbourne International Graduate Scholarship, Australia Awards").build(),

            // Europe - Germany
            UniversityProgram.builder().universityName("TU Munich").programName("MSc Informatics (AI)")
                .country("Germany").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(3.3).thresholdIelts(6.5).thresholdEctsCs(90).thesisRequired(true)
                .acceptanceRate(0.15).avgAcceptedGpa(3.40)
                .tuitionAnnualUsd(6500.0).livingCostAnnualUsd(13000.0).blockedAccountRequirementUsd(13000.0)
                .postStudyVisaMonths(18).visaProgramName("Germany 18-Month Jobseeker Visa / EU Blue Card Pathway")
                .scholarshipTypes("DAAD Master Studies Scholarship, Deutschlandstipendium").build(),

            UniversityProgram.builder().universityName("RWTH Aachen University").programName("MSc Computer Science")
                .country("Germany").frameworkId(2).region("EUROPE").specialization("CS")
                .thresholdGpa(3.2).thresholdIelts(6.5).thresholdEctsCs(85).thesisRequired(true)
                .acceptanceRate(0.19).avgAcceptedGpa(3.30)
                .tuitionAnnualUsd(0.0).livingCostAnnualUsd(11500.0).blockedAccountRequirementUsd(13000.0)
                .postStudyVisaMonths(18).visaProgramName("Germany 18-Month Jobseeker Visa / EU Blue Card Pathway")
                .scholarshipTypes("DAAD Master Studies Scholarship, Deutschlandstipendium, RWTH Bildungsfonds").build(),

            UniversityProgram.builder().universityName("TU Berlin").programName("MSc Computer Science")
                .country("Germany").frameworkId(2).region("EUROPE").specialization("CS")
                .thresholdGpa(3.0).thresholdIelts(6.5).thresholdEctsCs(80).thesisRequired(true)
                .acceptanceRate(0.22).avgAcceptedGpa(3.25)
                .tuitionAnnualUsd(0.0).livingCostAnnualUsd(12500.0).blockedAccountRequirementUsd(13000.0)
                .postStudyVisaMonths(18).visaProgramName("Germany 18-Month Jobseeker Visa / EU Blue Card Pathway")
                .scholarshipTypes("DAAD Master Studies Scholarship, Deutschlandstipendium").build(),

            // Europe - Switzerland
            UniversityProgram.builder().universityName("ETH Zurich").programName("MSc Computer Science")
                .country("Switzerland").frameworkId(2).region("EUROPE").specialization("CS")
                .thresholdGpa(3.7).thresholdIelts(7.0).thresholdEctsCs(120).thesisRequired(true)
                .acceptanceRate(0.08).avgAcceptedGpa(3.75)
                .tuitionAnnualUsd(1600.0).livingCostAnnualUsd(24000.0).blockedAccountRequirementUsd(24000.0)
                .postStudyVisaMonths(6).visaProgramName("Switzerland 6-Month Jobseeker Visa")
                .scholarshipTypes("ETH Excellence Scholarship & Opportunity Programme (ESOP)").build(),

            UniversityProgram.builder().universityName("EPFL").programName("MSc Computer Science")
                .country("Switzerland").frameworkId(2).region("EUROPE").specialization("CS/AI")
                .thresholdGpa(3.6).thresholdIelts(7.0).thresholdEctsCs(110).thesisRequired(true)
                .acceptanceRate(0.10).avgAcceptedGpa(3.70)
                .tuitionAnnualUsd(1600.0).livingCostAnnualUsd(22000.0).blockedAccountRequirementUsd(24000.0)
                .postStudyVisaMonths(6).visaProgramName("Switzerland 6-Month Jobseeker Visa")
                .scholarshipTypes("EPFL Master Excellence Fellowship").build(),

            // Europe - Spain (Top Technical Universities)
            UniversityProgram.builder().universityName("UPC BarcelonaTech").programName("MSc in Artificial Intelligence")
                .country("Spain").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(3.0).thresholdIelts(6.5).thresholdEctsCs(80).thesisRequired(false)
                .acceptanceRate(0.26).avgAcceptedGpa(3.20)
                .tuitionAnnualUsd(4200.0).livingCostAnnualUsd(11000.0).blockedAccountRequirementUsd(7500.0)
                .postStudyVisaMonths(12).visaProgramName("Spain 12-Month Job Search Visa (Búsqueda de Empleo) / EU Blue Card")
                .scholarshipTypes("UPC Erasmus+ Mundus Grant, Catalonia Beatriu de Pinós, Spanish Ministry Grants").build(),

            UniversityProgram.builder().universityName("Universidad Politécnica de Madrid").programName("MSc in Artificial Intelligence")
                .country("Spain").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(2.9).thresholdIelts(6.0).thresholdEctsCs(75).thesisRequired(false)
                .acceptanceRate(0.30).avgAcceptedGpa(3.10)
                .tuitionAnnualUsd(4800.0).livingCostAnnualUsd(11500.0).blockedAccountRequirementUsd(7500.0)
                .postStudyVisaMonths(12).visaProgramName("Spain 12-Month Job Search Visa / EU Blue Card")
                .scholarshipTypes("UPM International Mobility Grants, Spanish MAEC AECID Scholarships").build(),

            UniversityProgram.builder().universityName("Carlos III University of Madrid").programName("MSc Computer Science & Technology")
                .country("Spain").frameworkId(2).region("EUROPE").specialization("CS")
                .thresholdGpa(3.0).thresholdIelts(6.5).thresholdEctsCs(80).thesisRequired(false)
                .acceptanceRate(0.28).avgAcceptedGpa(3.15)
                .tuitionAnnualUsd(6000.0).livingCostAnnualUsd(11000.0).blockedAccountRequirementUsd(7500.0)
                .postStudyVisaMonths(12).visaProgramName("Spain 12-Month Job Search Visa / EU Blue Card")
                .scholarshipTypes("UC3M Master Tuition Grants, Ibercaja Academic Excellence").build(),

            // Europe - Portugal (Leading Engineering Hubs)
            UniversityProgram.builder().universityName("Instituto Superior Técnico").programName("MSc Computer Science and Engineering")
                .country("Portugal").frameworkId(2).region("EUROPE").specialization("CS/AI")
                .thresholdGpa(3.0).thresholdIelts(6.5).thresholdEctsCs(80).thesisRequired(false)
                .acceptanceRate(0.28).avgAcceptedGpa(3.20)
                .tuitionAnnualUsd(4000.0).livingCostAnnualUsd(9500.0).blockedAccountRequirementUsd(10000.0)
                .postStudyVisaMonths(12).visaProgramName("Portugal 12-Month Post-Study Job Seeker Visa / D4 Residence Permit")
                .scholarshipTypes("FCT Master Fellowships (€990/mo), University of Lisbon International Merit Grant").build(),

            UniversityProgram.builder().universityName("University of Porto").programName("MSc in Artificial Intelligence")
                .country("Portugal").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(2.8).thresholdIelts(6.0).thresholdEctsCs(75).thesisRequired(false)
                .acceptanceRate(0.32).avgAcceptedGpa(3.05)
                .tuitionAnnualUsd(4500.0).livingCostAnnualUsd(8500.0).blockedAccountRequirementUsd(10000.0)
                .postStudyVisaMonths(12).visaProgramName("Portugal 12-Month Post-Study Job Seeker Visa / D4 Residence Permit")
                .scholarshipTypes("FEUP Merit Scholarship, Santander International Mobility Grants").build(),

            // Europe - Italy & France
            UniversityProgram.builder().universityName("Politecnico di Milano").programName("MSc Computer Science and Engineering")
                .country("Italy").frameworkId(2).region("EUROPE").specialization("CS")
                .thresholdGpa(3.2).thresholdIelts(6.5).thresholdEctsCs(85).thesisRequired(false)
                .acceptanceRate(0.20).avgAcceptedGpa(3.35)
                .tuitionAnnualUsd(3900.0).livingCostAnnualUsd(12500.0).blockedAccountRequirementUsd(6500.0)
                .postStudyVisaMonths(12).visaProgramName("Italy 12-Month Permesso di Soggiorno per Ricerca Lavoro / EU Blue Card")
                .scholarshipTypes("DSU Regional Scholarship (Full Tuition Waiver + €7,200/yr), PoliMi Platinum/Gold Merit Award").build(),

            UniversityProgram.builder().universityName("IP Paris").programName("MSc Computer Science - Data & AI")
                .country("France").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(3.4).thresholdIelts(6.5).thresholdEctsCs(90).thesisRequired(true)
                .acceptanceRate(0.16).avgAcceptedGpa(3.50)
                .tuitionAnnualUsd(8000.0).livingCostAnnualUsd(14000.0).blockedAccountRequirementUsd(7500.0)
                .postStudyVisaMonths(24).visaProgramName("France 24-Month RECE / APS Visa + 2-Yr Fast-Track Naturalization")
                .scholarshipTypes("Eiffel Excellence Scholarship (€1,181/mo), IP Paris Master Fellowships").build(),

            // Europe - Ireland
            UniversityProgram.builder().universityName("Trinity College Dublin").programName("MSc Computer Science (Intelligent Systems)")
                .country("Ireland").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(3.3).thresholdIelts(6.5).thresholdEctsCs(80).thesisRequired(false)
                .acceptanceRate(0.22).avgAcceptedGpa(3.40)
                .tuitionAnnualUsd(26000.0).livingCostAnnualUsd(16000.0).blockedAccountRequirementUsd(11000.0)
                .postStudyVisaMonths(24).visaProgramName("Ireland 24-Month Stamp 1G Third Level Graduate Scheme (Dublin Tech Hub)")
                .scholarshipTypes("Government of Ireland International Education Scholarship (GOI-IES), Trinity Global Excellence Award").build(),

            // Europe - Netherlands, Sweden, UK
            UniversityProgram.builder().universityName("KTH Stockholm").programName("MSc Machine Learning")
                .country("Sweden").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(3.2).thresholdIelts(6.5).thresholdEctsCs(90).thesisRequired(false)
                .acceptanceRate(0.20).avgAcceptedGpa(3.30)
                .tuitionAnnualUsd(16500.0).livingCostAnnualUsd(12500.0).blockedAccountRequirementUsd(11500.0)
                .postStudyVisaMonths(12).visaProgramName("Sweden 12-Month Job Seeking Residence Permit")
                .scholarshipTypes("KTH Scholarship, Swedish Institute (SI) Scholarships").build(),

            UniversityProgram.builder().universityName("TU Delft").programName("MSc Computer Science")
                .country("Netherlands").frameworkId(2).region("EUROPE").specialization("CS")
                .thresholdGpa(3.0).thresholdIelts(6.0).thresholdEctsCs(80).thesisRequired(false)
                .acceptanceRate(0.22).avgAcceptedGpa(3.20)
                .tuitionAnnualUsd(22000.0).livingCostAnnualUsd(14000.0).blockedAccountRequirementUsd(14000.0)
                .postStudyVisaMonths(12).visaProgramName("Netherlands Zoekjaar / 1-Year Orientation Year")
                .scholarshipTypes("Justus & Louise van Effen Excellence Scholarship").build(),

            UniversityProgram.builder().universityName("University of Amsterdam").programName("MSc AI")
                .country("Netherlands").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(3.2).thresholdIelts(6.5).thresholdEctsCs(90).thesisRequired(false)
                .acceptanceRate(0.18).avgAcceptedGpa(3.35)
                .tuitionAnnualUsd(23000.0).livingCostAnnualUsd(15000.0).blockedAccountRequirementUsd(14000.0)
                .postStudyVisaMonths(12).visaProgramName("Netherlands Zoekjaar / 1-Year Orientation Year")
                .scholarshipTypes("Amsterdam Merit Scholarship (AMS)").build(),

            UniversityProgram.builder().universityName("University of Edinburgh").programName("MSc AI")
                .country("UK").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(3.3).thresholdIelts(6.5).thresholdEctsCs(80).thesisRequired(false)
                .acceptanceRate(0.15).avgAcceptedGpa(3.40)
                .tuitionAnnualUsd(42000.0).livingCostAnnualUsd(18000.0).blockedAccountRequirementUsd(16000.0)
                .postStudyVisaMonths(24).visaProgramName("UK Graduate Route (2-Year Unsponsored Work Visa)")
                .scholarshipTypes("Edinburgh Global Research Scholarship, Chevening Scholarship").build(),

            UniversityProgram.builder().universityName("Imperial College London").programName("MSc Advanced Computing")
                .country("UK").frameworkId(2).region("EUROPE").specialization("CS/AI")
                .thresholdGpa(3.7).thresholdIelts(7.0).thresholdEctsCs(100).thesisRequired(true)
                .acceptanceRate(0.11).avgAcceptedGpa(3.80)
                .tuitionAnnualUsd(48000.0).livingCostAnnualUsd(22000.0).blockedAccountRequirementUsd(18000.0)
                .postStudyVisaMonths(24).visaProgramName("UK Graduate Route (2-Year Unsponsored Work Visa)")
                .scholarshipTypes("Imperial President's Scholarship, Chevening Scholarship").build(),

            // Japan
            UniversityProgram.builder().universityName("University of Tokyo").programName("Graduate School of Information Science")
                .country("Japan").frameworkId(3).region("JAPAN").specialization("CS/AI")
                .thresholdGpa(3.2).thresholdJlpt("N2").thresholdToefl(80).thesisRequired(true)
                .acceptanceRate(0.25).avgAcceptedGpa(3.30)
                .tuitionAnnualUsd(3600.0).livingCostAnnualUsd(12000.0).blockedAccountRequirementUsd(14000.0)
                .postStudyVisaMonths(24).visaProgramName("Japan J-Find / Designated Activities (Up to 24 Months) + HSP Fast-Track")
                .scholarshipTypes("MEXT Japanese Government Scholarship, JASSO Honors, Todai Fellowship").build(),

            UniversityProgram.builder().universityName("Kyoto University").programName("Graduate School of Informatics")
                .country("Japan").frameworkId(3).region("JAPAN").specialization("CS/AI")
                .thresholdGpa(3.0).thresholdJlpt("N2").thresholdToefl(79).thesisRequired(true)
                .acceptanceRate(0.28).avgAcceptedGpa(3.10)
                .tuitionAnnualUsd(3600.0).livingCostAnnualUsd(11000.0).blockedAccountRequirementUsd(14000.0)
                .postStudyVisaMonths(24).visaProgramName("Japan J-Find / Designated Activities + HSP Points (PR in 1-3 Yrs)")
                .scholarshipTypes("MEXT Japanese Government Scholarship, JASSO Honors, Kyoto iUP").build(),

            UniversityProgram.builder().universityName("Osaka University").programName("Graduate School IS")
                .country("Japan").frameworkId(3).region("JAPAN").specialization("CS")
                .thresholdGpa(2.8).thresholdJlpt("N3").thresholdToefl(70).thesisRequired(true)
                .acceptanceRate(0.30).avgAcceptedGpa(3.00)
                .tuitionAnnualUsd(3600.0).livingCostAnnualUsd(10500.0).blockedAccountRequirementUsd(14000.0)
                .postStudyVisaMonths(24).visaProgramName("Japan Designated Activities (Job Hunting) + HSP Points")
                .scholarshipTypes("MEXT Japanese Government Scholarship, JASSO Honors").build(),

            UniversityProgram.builder().universityName("Tohoku University").programName("Graduate School IS")
                .country("Japan").frameworkId(3).region("JAPAN").specialization("CS")
                .thresholdGpa(2.8).thresholdJlpt("N3").thresholdToefl(70).thesisRequired(true)
                .acceptanceRate(0.32).avgAcceptedGpa(2.90)
                .tuitionAnnualUsd(3600.0).livingCostAnnualUsd(9500.0).blockedAccountRequirementUsd(14000.0)
                .postStudyVisaMonths(24).visaProgramName("Japan Designated Activities (Job Hunting) + HSP Points")
                .scholarshipTypes("MEXT Japanese Government Scholarship, JASSO Honors, President Fellowship").build(),

            UniversityProgram.builder().universityName("NAIST").programName("MS Information Science")
                .country("Japan").frameworkId(3).region("JAPAN").specialization("CS/AI")
                .thresholdGpa(2.5).thresholdJlpt("N3").thresholdToefl(65).thesisRequired(true)
                .acceptanceRate(0.35).avgAcceptedGpa(2.80)
                .tuitionAnnualUsd(3600.0).livingCostAnnualUsd(9000.0).blockedAccountRequirementUsd(14000.0)
                .postStudyVisaMonths(24).visaProgramName("Japan Designated Activities (Job Hunting) + HSP Points")
                .scholarshipTypes("MEXT Japanese Government Scholarship, JASSO Honors, NAIST Foundation").build(),

            UniversityProgram.builder().universityName("Tokyo Institute of Technology").programName("Graduate Major in CS & AI")
                .country("Japan").frameworkId(3).region("JAPAN").specialization("CS/AI")
                .thresholdGpa(3.1).thresholdJlpt("N2").thresholdToefl(75).thesisRequired(true)
                .acceptanceRate(0.26).avgAcceptedGpa(3.25)
                .tuitionAnnualUsd(3600.0).livingCostAnnualUsd(11500.0).blockedAccountRequirementUsd(14000.0)
                .postStudyVisaMonths(24).visaProgramName("Japan J-Find / Designated Activities + HSP Points")
                .scholarshipTypes("MEXT Japanese Government Scholarship, Tokyo Tech Tsubame Scholarship").build(),

            // ── Newly Added Iconic World Universities ──────────────────────────────────
            // USA Top Tech & Ivy League
            UniversityProgram.builder().universityName("Harvard University").programName("MS Computational Science & Engineering")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS/AI")
                .thresholdGpa(3.8).thresholdGreV(162).thresholdGreQ(167).thresholdGreAw(4.5)
                .thresholdToefl(100).thresholdIelts(7.5).acceptanceRate(0.05).avgAcceptedGpa(3.90)
                .tuitionAnnualUsd(61000.0).livingCostAnnualUsd(26000.0).blockedAccountRequirementUsd(87000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("Fulbright, Harvard SEAS Fellowship, Graduate Research Assistantships (GRA)").build(),

            UniversityProgram.builder().universityName("Princeton University").programName("MSE in Computer Science")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS")
                .thresholdGpa(3.8).thresholdGreV(163).thresholdGreQ(168).thresholdGreAw(4.5)
                .thresholdToefl(105).thresholdIelts(8.0).acceptanceRate(0.04).avgAcceptedGpa(3.92)
                .tuitionAnnualUsd(59000.0).livingCostAnnualUsd(24000.0).blockedAccountRequirementUsd(83000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("Princeton Graduate Fellowship, Full Tuition & Assistantship Stipend").build(),

            UniversityProgram.builder().universityName("Columbia University").programName("MS Computer Science (ML Track)")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("AI/ML")
                .thresholdGpa(3.65).thresholdGreV(158).thresholdGreQ(165).thresholdGreAw(4.0)
                .thresholdToefl(101).thresholdIelts(7.0).acceptanceRate(0.11).avgAcceptedGpa(3.78)
                .tuitionAnnualUsd(64000.0).livingCostAnnualUsd(28000.0).blockedAccountRequirementUsd(92000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("Columbia SEAS Master's Fellowship, Departmental Course Assistantships").build(),

            UniversityProgram.builder().universityName("University of Washington").programName("MS Computer Science & Engineering")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS/AI")
                .thresholdGpa(3.7).thresholdGreV(158).thresholdGreQ(166).thresholdGreAw(4.0)
                .thresholdToefl(100).thresholdIelts(7.0).acceptanceRate(0.07).avgAcceptedGpa(3.85)
                .tuitionAnnualUsd(42000.0).livingCostAnnualUsd(22000.0).blockedAccountRequirementUsd(64000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("Paul G. Allen Fellowship, Graduate Teaching/Research Assistantships").build(),

            UniversityProgram.builder().universityName("UT Austin").programName("MS Computer Science")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS")
                .thresholdGpa(3.5).thresholdGreV(156).thresholdGreQ(164).thresholdGreAw(4.0)
                .thresholdToefl(95).thresholdIelts(7.0).acceptanceRate(0.09).avgAcceptedGpa(3.75)
                .tuitionAnnualUsd(28000.0).livingCostAnnualUsd(18000.0).blockedAccountRequirementUsd(46000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("UT Austin MCD Fellowship, Graduate TA/RA").build(),

            UniversityProgram.builder().universityName("UIUC").programName("MS Computer Science")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS/AI")
                .thresholdGpa(3.6).thresholdGreV(156).thresholdGreQ(165).thresholdGreAw(4.0)
                .thresholdToefl(102).thresholdIelts(7.5).acceptanceRate(0.08).avgAcceptedGpa(3.82)
                .tuitionAnnualUsd(38000.0).livingCostAnnualUsd(18000.0).blockedAccountRequirementUsd(56000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("UIUC Research Assistantships (Full Tuition Waiver + $2,400/mo stipend)").build(),

            UniversityProgram.builder().universityName("UCLA").programName("MS Computer Science")
                .country("USA").frameworkId(1).region("US_CANADA").specialization("CS")
                .thresholdGpa(3.6).thresholdGreV(155).thresholdGreQ(165).thresholdGreAw(4.0)
                .thresholdToefl(95).thresholdIelts(7.0).acceptanceRate(0.10).avgAcceptedGpa(3.76)
                .tuitionAnnualUsd(36000.0).livingCostAnnualUsd(24000.0).blockedAccountRequirementUsd(60000.0)
                .postStudyVisaMonths(36).visaProgramName("USA F-1 STEM OPT (36 Months)")
                .scholarshipTypes("UCLA Graduate Division Award, Departmental TA Fee Remissions").build(),

            // United Kingdom - Oxbridge & UCL
            UniversityProgram.builder().universityName("University of Oxford").programName("MSc in Advanced Computer Science")
                .country("UK").frameworkId(2).region("EUROPE").specialization("CS/AI")
                .thresholdGpa(3.75).thresholdIelts(7.5).thresholdToefl(110).thresholdEctsCs(100).thesisRequired(true)
                .acceptanceRate(0.08).avgAcceptedGpa(3.85)
                .tuitionAnnualUsd(49000.0).livingCostAnnualUsd(20000.0).blockedAccountRequirementUsd(18000.0)
                .postStudyVisaMonths(24).visaProgramName("UK Graduate Route (2-Year Unsponsored Work Visa)")
                .scholarshipTypes("Clarendon Fund Scholarship, Oxford-DeepMind Scholarship, Chevening Scholarship").build(),

            UniversityProgram.builder().universityName("University of Cambridge").programName("MPhil in Advanced Computer Science")
                .country("UK").frameworkId(2).region("EUROPE").specialization("CS/AI")
                .thresholdGpa(3.8).thresholdIelts(7.5).thresholdToefl(110).thresholdEctsCs(100).thesisRequired(true)
                .acceptanceRate(0.07).avgAcceptedGpa(3.88)
                .tuitionAnnualUsd(48000.0).livingCostAnnualUsd(20000.0).blockedAccountRequirementUsd(18000.0)
                .postStudyVisaMonths(24).visaProgramName("UK Graduate Route (2-Year Unsponsored Work Visa)")
                .scholarshipTypes("Gates Cambridge Scholarship, Cambridge Trust Awards, Chevening Scholarship").build(),

            UniversityProgram.builder().universityName("University College London").programName("MSc Computer Science")
                .country("UK").frameworkId(2).region("EUROPE").specialization("CS")
                .thresholdGpa(3.4).thresholdIelts(7.0).thresholdToefl(100).thresholdEctsCs(85).thesisRequired(false)
                .acceptanceRate(0.14).avgAcceptedGpa(3.55)
                .tuitionAnnualUsd(45000.0).livingCostAnnualUsd(22000.0).blockedAccountRequirementUsd(18000.0)
                .postStudyVisaMonths(24).visaProgramName("UK Graduate Route (2-Year Unsponsored Work Visa)")
                .scholarshipTypes("UCL Global Masters Scholarship (£15,000), Chevening Scholarship").build(),

            // Canada
            UniversityProgram.builder().universityName("McGill University").programName("MSc Computer Science")
                .country("Canada").frameworkId(1).region("US_CANADA").specialization("CS/AI")
                .thresholdGpa(3.4).thresholdGreV(152).thresholdGreQ(160).thresholdGreAw(3.5)
                .thresholdToefl(90).thresholdIelts(6.5).acceptanceRate(0.14).avgAcceptedGpa(3.60)
                .tuitionAnnualUsd(28000.0).livingCostAnnualUsd(16000.0).blockedAccountRequirementUsd(15500.0)
                .postStudyVisaMonths(36).visaProgramName("Canada PGWP (36 Months Open Work Permit)")
                .scholarshipTypes("Mila Quebec AI Institute Fellowship, McGill Graduate Excellence Award").build(),

            UniversityProgram.builder().universityName("University of British Columbia").programName("MSc in Computer Science")
                .country("Canada").frameworkId(1).region("US_CANADA").specialization("CS")
                .thresholdGpa(3.5).thresholdGreV(153).thresholdGreQ(162).thresholdGreAw(4.0)
                .thresholdToefl(100).thresholdIelts(7.0).acceptanceRate(0.11).avgAcceptedGpa(3.70)
                .tuitionAnnualUsd(24000.0).livingCostAnnualUsd(18000.0).blockedAccountRequirementUsd(15500.0)
                .postStudyVisaMonths(36).visaProgramName("Canada PGWP (36 Months Open Work Permit)")
                .scholarshipTypes("UBC International Tuition Award, Graduate Support Initiative (GSI)").build(),

            // Singapore & Asia
            UniversityProgram.builder().universityName("National University of Singapore").programName("Master of Computing (AI/CS)")
                .country("Singapore").frameworkId(3).region("JAPAN").specialization("CS/AI")
                .thresholdGpa(3.5).thresholdToefl(95).thresholdIelts(7.0).thesisRequired(false)
                .acceptanceRate(0.15).avgAcceptedGpa(3.65)
                .tuitionAnnualUsd(38000.0).livingCostAnnualUsd(16000.0).blockedAccountRequirementUsd(20000.0)
                .postStudyVisaMonths(12).visaProgramName("Singapore Long-Term Visit Pass (LTVP - 1 Year) / Tech.Pass")
                .scholarshipTypes("NUS Graduate School Scholarship, Asian Development Bank (ADB-JSP)").build(),

            UniversityProgram.builder().universityName("Nanyang Technological University").programName("MSc Artificial Intelligence")
                .country("Singapore").frameworkId(3).region("JAPAN").specialization("AI/ML")
                .thresholdGpa(3.4).thresholdToefl(92).thresholdIelts(6.5).thesisRequired(false)
                .acceptanceRate(0.17).avgAcceptedGpa(3.55)
                .tuitionAnnualUsd(36000.0).livingCostAnnualUsd(15000.0).blockedAccountRequirementUsd(20000.0)
                .postStudyVisaMonths(12).visaProgramName("Singapore LTVP / Employment Pass Pathway")
                .scholarshipTypes("NTU Research Scholarship, Singapore Digital (SG:D) Scholarship").build(),

            // South Korea
            UniversityProgram.builder().universityName("KAIST").programName("MS in School of Computing")
                .country("South Korea").frameworkId(3).region("JAPAN").specialization("CS/AI")
                .thresholdGpa(3.2).thresholdToefl(83).thresholdIelts(6.5).thesisRequired(true)
                .acceptanceRate(0.22).avgAcceptedGpa(3.40)
                .tuitionAnnualUsd(8000.0).livingCostAnnualUsd(9000.0).blockedAccountRequirementUsd(12000.0)
                .postStudyVisaMonths(24).visaProgramName("South Korea D-10 Job Seeking Visa (24 Months) / F-2-7 Points")
                .scholarshipTypes("KAIST International Student Scholarship (Full Tuition + 350,000 KRW/mo)").build(),

            UniversityProgram.builder().universityName("Seoul National University").programName("MS in Computer Science & Engineering")
                .country("South Korea").frameworkId(3).region("JAPAN").specialization("CS")
                .thresholdGpa(3.3).thresholdToefl(85).thresholdIelts(6.5).thesisRequired(true)
                .acceptanceRate(0.19).avgAcceptedGpa(3.50)
                .tuitionAnnualUsd(6500.0).livingCostAnnualUsd(10000.0).blockedAccountRequirementUsd(12000.0)
                .postStudyVisaMonths(24).visaProgramName("South Korea D-10 Job Seeking Visa / F-2-7 Points Residence")
                .scholarshipTypes("Global Korea Scholarship (GKS Full Ride), SNU President Fellowship").build(),

            // Hong Kong
            UniversityProgram.builder().universityName("University of Hong Kong").programName("MSc in Computer Science (Financial AI)")
                .country("Hong Kong").frameworkId(3).region("JAPAN").specialization("CS/AI")
                .thresholdGpa(3.2).thresholdToefl(85).thresholdIelts(6.5).thesisRequired(false)
                .acceptanceRate(0.21).avgAcceptedGpa(3.45)
                .tuitionAnnualUsd(32000.0).livingCostAnnualUsd(16000.0).blockedAccountRequirementUsd(15000.0)
                .postStudyVisaMonths(24).visaProgramName("Hong Kong IANG Visa (24 Months Unconditional Work Permit)")
                .scholarshipTypes("HKU Foundation Postgraduate Fellowship, Targeted Taught Postgraduate Schemes").build(),

            // Australia
            UniversityProgram.builder().universityName("University of Sydney").programName("Master of Computer Science")
                .country("Australia").frameworkId(1).region("US_CANADA").specialization("CS")
                .thresholdGpa(3.2).thresholdToefl(85).thresholdIelts(6.5).acceptanceRate(0.26).avgAcceptedGpa(3.35)
                .tuitionAnnualUsd(34000.0).livingCostAnnualUsd(19000.0).blockedAccountRequirementUsd(16000.0)
                .postStudyVisaMonths(36).visaProgramName("Australia Subclass 485 Temporary Graduate Visa (3-4 Years)")
                .scholarshipTypes("Sydney International Student Award (20% reduction), Vice-Chancellor’s Award").build(),

            UniversityProgram.builder().universityName("UNSW Sydney").programName("Master of Information Technology (AI)")
                .country("Australia").frameworkId(1).region("US_CANADA").specialization("AI/ML")
                .thresholdGpa(3.1).thresholdToefl(85).thresholdIelts(6.5).acceptanceRate(0.28).avgAcceptedGpa(3.30)
                .tuitionAnnualUsd(33000.0).livingCostAnnualUsd(19000.0).blockedAccountRequirementUsd(16000.0)
                .postStudyVisaMonths(36).visaProgramName("Australia Subclass 485 Temporary Graduate Visa (3-4 Years)")
                .scholarshipTypes("UNSW International Scientia Scholarship, Future of Change Award").build(),

            // Europe - Germany (Additional Famous Tech Hubs)
            UniversityProgram.builder().universityName("LMU Munich").programName("MSc Data Science / Informatics")
                .country("Germany").frameworkId(2).region("EUROPE").specialization("Data Science")
                .thresholdGpa(3.3).thresholdIelts(7.0).thresholdToefl(95).thresholdEctsCs(85).thesisRequired(true)
                .acceptanceRate(0.16).avgAcceptedGpa(3.45)
                .tuitionAnnualUsd(0.0).livingCostAnnualUsd(13500.0).blockedAccountRequirementUsd(13000.0)
                .postStudyVisaMonths(18).visaProgramName("Germany 18-Month Jobseeker Visa / EU Blue Card Pathway")
                .scholarshipTypes("DAAD Master Studies Scholarship, Deutschlandstipendium, Bavarian Elite Network").build(),

            UniversityProgram.builder().universityName("Karlsruhe Institute of Technology").programName("MSc Informatics")
                .country("Germany").frameworkId(2).region("EUROPE").specialization("CS")
                .thresholdGpa(3.2).thresholdIelts(6.5).thresholdToefl(88).thresholdEctsCs(80).thesisRequired(true)
                .acceptanceRate(0.20).avgAcceptedGpa(3.35)
                .tuitionAnnualUsd(3200.0).livingCostAnnualUsd(11500.0).blockedAccountRequirementUsd(13000.0)
                .postStudyVisaMonths(18).visaProgramName("Germany 18-Month Jobseeker Visa / EU Blue Card Pathway")
                .scholarshipTypes("DAAD Scholarship, Baden-Württemberg Fellowship, Deutschlandstipendium").build(),

            UniversityProgram.builder().universityName("University of Stuttgart").programName("MSc Computer Science")
                .country("Germany").frameworkId(2).region("EUROPE").specialization("CS/AI")
                .thresholdGpa(3.0).thresholdIelts(6.5).thresholdToefl(85).thresholdEctsCs(80).thesisRequired(false)
                .acceptanceRate(0.24).avgAcceptedGpa(3.20)
                .tuitionAnnualUsd(3200.0).livingCostAnnualUsd(11500.0).blockedAccountRequirementUsd(13000.0)
                .postStudyVisaMonths(18).visaProgramName("Germany 18-Month Jobseeker Visa / EU Blue Card Pathway")
                .scholarshipTypes("DAAD Master Studies Scholarship, Baden-Württemberg State Merit Award").build(),

            // Europe - Belgium & Finland
            UniversityProgram.builder().universityName("KU Leuven").programName("Master of Artificial Intelligence")
                .country("Belgium").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(3.3).thresholdIelts(7.0).thresholdToefl(94).thresholdEctsCs(85).thesisRequired(false)
                .acceptanceRate(0.18).avgAcceptedGpa(3.40)
                .tuitionAnnualUsd(7500.0).livingCostAnnualUsd(12000.0).blockedAccountRequirementUsd(11000.0)
                .postStudyVisaMonths(12).visaProgramName("Belgium 12-Month Search Year Visa / EU Blue Card")
                .scholarshipTypes("Master Mind Scholarship (Flanders €9,600/yr), KU Leuven Benefactors Grant").build(),

            UniversityProgram.builder().universityName("Aalto University").programName("MSc Computer Science (ML & Data)")
                .country("Finland").frameworkId(2).region("EUROPE").specialization("AI/ML")
                .thresholdGpa(3.4).thresholdIelts(6.5).thresholdToefl(92).thresholdEctsCs(90).thesisRequired(true)
                .acceptanceRate(0.13).avgAcceptedGpa(3.55)
                .tuitionAnnualUsd(16000.0).livingCostAnnualUsd(11000.0).blockedAccountRequirementUsd(10000.0)
                .postStudyVisaMonths(24).visaProgramName("Finland 2-Year Post-Study Job Search Permit / Fast-Track Specialist")
                .scholarshipTypes("Aalto University Scholarship (100% Tuition Waiver), Finland Scholarship").build()

        );

        programRepo.saveAll(programs);
        LOG.info("Seeding complete: " + programs.size() + " programs across US, Europe, and Japan.");
    }
}
