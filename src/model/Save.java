package model;

import java.sql.Timestamp;
import java.util.List;

public class Save {
    private String t_id;
    private String s_id;
    private String lId;          // 최종 저장 위치 (stage s_id)
    private int tryNum;          // 몇 번째 시도인지 (auto_increment)
    private Timestamp t_time;
    private List<Card> cards;    // 보유 카드 (c_save)
    private List<Item> items;    // 보유 아이템 (i_save)

    public Save(String t_id, String lId, int tryNum, Timestamp t_time, List<Card> cards, List<Item> items) {
        this.t_id = t_id;
        this.lId = lId;
        this.tryNum = tryNum;
        this.t_time = t_time;
        this.cards = cards;
        this.items = items;
    }

    public String getSaveStatus() {
        if (t_time == null) { return "세이브가 없습니다!"; }
        return "Try #" + tryNum + " | Stage:" + lId + " | " + t_time;
    }

    // --- Getters ---
    public String getS_id() { return s_id; }
    public String getLId() { return lId; }
    public int getTryNum() { return tryNum; }
    public Timestamp getT_time() { return t_time; }
    public List<Card> getCards() { return cards; }
    public List<Item> getItems() { return items; }

    // --- Setters ---
    public void setLId(String lId) { this.lId = lId; }
    public void setTryNum(int tryNum) { this.tryNum = tryNum; }
    public void setTime(Timestamp t_time) { this.t_time = t_time; }
    public void setCards(List<Card> cards) { this.cards = cards; }
    public void setItems(List<Item> items) { this.items = items; }
}
