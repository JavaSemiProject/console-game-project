package model;

public class Ending {
    private int endingId;
    private String endingName;
    private String endingType;  // NORMAL / HIDDEN / TRUE
    private String description;

    public Ending(int endingId, String endingName, String endingType, String description) {
        this.endingId = endingId;
        this.endingName = endingName;
        this.endingType = endingType;
        this.description = description;
    }

    // --- Getters ---

    public int getEndingId() { return endingId; }
    public String getEndingName() { return endingName; }
    public String getEndingType() { return endingType; }
    public String getDescription() { return description; }

    // --- Setters ---

    public void setEndingId(int endingId) { this.endingId = endingId; }
    public void setEndingName(String endingName) { this.endingName = endingName; }
    public void setEndingType(String endingType) { this.endingType = endingType; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "[ENDING:" + endingId + "] " + endingName + " (" + endingType + ")";
    }
}
