package model;

import java.sql.Timestamp;

public class CardDictionary {
    private int id;
    private int cardId;
    private boolean isUnlocked;
    private int firstTry;

    public CardDictionary(int id, int cardId, boolean isUnlocked, int firstTry) {
        this.id = id;
        this.cardId = cardId;
        this.isUnlocked = isUnlocked;
        this.firstTry = firstTry;
    }

    public void unlock(int currentTry) {
        this.isUnlocked = true;
        this.firstTry = currentTry;
    }

    // --- Getters ---

    public int getId() { return id; }
    public int getCardId() { return cardId; }
    public boolean isUnlocked() { return isUnlocked; }
    public int getFirstTry() { return firstTry; }

    // --- Setters ---

    public void setId(int id) { this.id = id; }
    public void setCardId(int cardId) { this.cardId = cardId; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
    public void setFirstTry(int firstTry) { this.firstTry = firstTry; }

    @Override
    public String toString() {
        return "[DICT:" + id + "] Card " + cardId +
                (isUnlocked ? " [해금됨]" : " [미해금]");
    }
}
