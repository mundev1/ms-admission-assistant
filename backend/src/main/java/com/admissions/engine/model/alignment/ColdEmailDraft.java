package com.admissions.engine.model.alignment;

/**
 * Auto-generated personalized cold outreach email tailored for academic PIs or Japanese Naidaku consent.
 */
public class ColdEmailDraft {

    private String recipientName;
    private String recipientEmail;
    private String subjectLine;
    private String bodyText;
    private String referencedPaperTitle;
    private String culturalProtocolNotes;

    public ColdEmailDraft() {}

    public ColdEmailDraft(String recipientName, String recipientEmail, String subjectLine,
                          String bodyText, String referencedPaperTitle, String culturalProtocolNotes) {
        this.recipientName = recipientName;
        this.recipientEmail = recipientEmail;
        this.subjectLine = subjectLine;
        this.bodyText = bodyText;
        this.referencedPaperTitle = referencedPaperTitle;
        this.culturalProtocolNotes = culturalProtocolNotes;
    }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientProfessorName() { return recipientName; }
    public void setRecipientProfessorName(String name) { this.recipientName = name; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getSubjectLine() { return subjectLine; }
    public void setSubjectLine(String subjectLine) { this.subjectLine = subjectLine; }

    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }

    public String getReferencedPaperTitle() { return referencedPaperTitle; }
    public void setReferencedPaperTitle(String referencedPaperTitle) { this.referencedPaperTitle = referencedPaperTitle; }
    public String getPaperReferenced() { return referencedPaperTitle; }
    public void setPaperReferenced(String title) { this.referencedPaperTitle = title; }

    public String getCulturalProtocolNotes() { return culturalProtocolNotes; }
    public void setCulturalProtocolNotes(String culturalProtocolNotes) { this.culturalProtocolNotes = culturalProtocolNotes; }
}
