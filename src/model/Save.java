package model;

import java.sql.Timestamp;
import java.util.List;

public class Save {
    private String lId;          // 최종 저장 위치 (stage s_id)
    private int tryNum;          // 몇 번째 시도인지 (auto_increment)
    private Timestamp time;
    private List<Card> cards;    // 보유 카드 (c_save)
    private List<Item> items;    // 보유 아이템 (i_save)

    public Save(String lId, int tryNum, Timestamp time, List<Card> cards, List<Item> items) {
        this.lId = lId;
        this.tryNum = tryNum;
        this.time = time;
        this.cards = cards;
        this.items = items;
    }

    public String getSaveStatus() {
        if (time == null) { return "세이브가 없습니다!"; }
        return "Try #" + tryNum + " | Stage:" + lId + " | " + time;
    }

    // --- Getters ---

    public String getLId() { return lId; }
    public int getTryNum() { return tryNum; }
    public Timestamp getTime() { return time; }
    public List<Card> getCards() { return cards; }
    public List<Item> getItems() { return items; }

    // --- Setters ---

    public void setLId(String lId) { this.lId = lId; }
    public void setTryNum(int tryNum) { this.tryNum = tryNum; }
    public void setTime(Timestamp time) { this.time = time; }
    public void setCards(List<Card> cards) { this.cards = cards; }
    public void setItems(List<Item> items) { this.items = items; }
}
