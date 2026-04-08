package model;

public class Ending {
    private String eId;
    private String eName;
    private String eDesc;
    private String eImg;
    private int tryNum;      // 엔딩 해금 트라이 (0 = 미해금)
    private int eCount;      // 엔딩 본 횟수

    public Ending(String eId, String eName, String eDesc, String eImg, int tryNum, int eCount) {
        this.eId = eId;
        this.eName = eName;
        this.eDesc = eDesc;
        this.eImg = eImg;
        this.tryNum = tryNum;
        this.eCount = eCount;
    }

    // --- Getters ---

    public String getEId() { return eId; }
    public String getEName() { return eName; }
    public String getEDesc() { return eDesc; }
    public String getEImg() { return eImg; }
    public int getTryNum() { return tryNum; }
    public int getECount() { return eCount; }

    // --- Setters ---

    public void setEId(String eId) { this.eId = eId; }
    public void setEName(String eName) { this.eName = eName; }
    public void setEDesc(String eDesc) { this.eDesc = eDesc; }
    public void setEImg(String eImg) { this.eImg = eImg; }
    public void setTryNum(int tryNum) { this.tryNum = tryNum; }
    public void setECount(int eCount) { this.eCount = eCount; }

    @Override
    public String toString() {
        return "[ENDING:" + eId + "] " + eName +
                (tryNum > 0 ? " [해금 - " + tryNum + "트라이]" : " [미해금]") +
                " (열람 " + eCount + "회)";
    }
}