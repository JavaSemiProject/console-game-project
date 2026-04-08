package model;

import java.sql.Timestamp;
import java.util.List;

public class Save {
    private int saveId;
    private int currentStageId;
    private int currentHp;
    private String gameStatus;   // PLAYING / CLEAR / GAMEOVER
    private Timestamp savedAt;
    private List<Card> cards;
    private List<Item> items;

    public Save(Builder builder) {
        saveId = builder.saveId;
        currentStageId = builder.currentStageId;
        currentHp = builder.currentHp;
        gameStatus = builder.gameStatus;
        savedAt = builder.savedAt;
        cards = builder.cards;
        items = builder.items;
    }

    class Builder {
        private int saveId;
        private int currentStageId;
        private int currentHp;
        private String gameStatus;   // PLAYING / CLEAR / GAMEOVER
        private Timestamp savedAt;
        private List<Card> cards;
        private List<Item> items;

        public Builder saveId(int saveId) { this.saveId = saveId; return this; }
        public Builder currentStageId(int currentStageId) { this.currentStageId = currentStageId; return this; }
        public Builder currentHp(int currentHp) { this.currentHp = currentHp; return this; }
        public Builder gameStatus(String gameStatus) { this.gameStatus = gameStatus ; return this; }
        public Builder savedAt(Timestamp savedAt) { this.savedAt = savedAt; return this; }
        public Builder cards(List<Card> cards) { this.cards = cards; return this; }
        public Builder items(List<Item> items) { this.items = items; return this; }

        public Save build() { return new Save(this); }
    }

    public String getSaveStatus() {
        if (savedAt == null) { return "세이브가 없습니다!"; }
        return "Stage " + currentStageId + " | HP:" + currentHp + " | " + savedAt;
    }

    // --- Getters ---

    public int getSaveId() { return saveId; }
    public int getCurrentStageId() { return currentStageId; }
    public int getCurrentHp() { return currentHp; }
    public String getGameStatus() { return gameStatus; }
    public Timestamp getSavedAt() { return savedAt; }
    public List<Card> getCards() { return cards; }

    // --- Setters ---

    public void setSaveId(int saveId) { this.saveId = saveId; }
    public void setCurrentStageId(int currentStageId) { this.currentStageId = currentStageId; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public void setGameStatus(String gameStatus) { this.gameStatus = gameStatus; }
    public void setSavedAt(Timestamp savedAt) { this.savedAt = savedAt; }
    public void setCards(List<Card> cards) { this.cards = cards; }
}
