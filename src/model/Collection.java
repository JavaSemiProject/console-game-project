package model;

import java.sql.Timestamp;

public class Collection {
    private int id;
    private String collectionType;  // CARD / NPC / ITEM / ENDING / ACHIEVEMENT
    private int contentId;          // 참조 대상 ID
    private boolean isUnlocked;
    private Timestamp unlockedAt;
    private int firstTry;

    public Collection(int id, int playerId, String collectionType, int contentId,
                      boolean isUnlocked, Timestamp unlockedAt, int firstTry) {
        this.id = id;
        this.collectionType = collectionType;
        this.contentId = contentId;
        this.isUnlocked = isUnlocked;
        this.unlockedAt = unlockedAt;
        this.firstTry = firstTry;
    }

    public void unlock(int currentTry) {
        this.isUnlocked = true;
        this.firstTry = currentTry;
        this.unlockedAt = new Timestamp(System.currentTimeMillis());
    }

    // --- Getters ---

    public int getId() { return id; }
    public String getCollectionType() { return collectionType; }
    public int getContentId() { return contentId; }
    public boolean isUnlocked() { return isUnlocked; }
    public Timestamp getUnlockedAt() { return unlockedAt; }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setCollectionType(String collectionType) { this.collectionType = collectionType; }
    public void setContentId(int contentId) { this.contentId = contentId; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
    public void setUnlockedAt(Timestamp unlockedAt) { this.unlockedAt = unlockedAt; }

    @Override
    public String toString() {
        return "[COLLECTION:" + id + "] " + collectionType + " #" + contentId +
                (isUnlocked ? " [해금됨]" : " [미해금]");
    }
}
