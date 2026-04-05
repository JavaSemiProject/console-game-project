package model;

interface CardEffect {
    int execute(Entity user, Entity target);
    String getDescription();  // 효과 설명 (UI 출력용)
}

class AttackEffect implements CardEffect {
    private int power;
    
    public AttackEffect(int power) {
        this.power = power;
    }
    
    public int execute(Entity user, Entity target) {
        target.takeDamage(power);
        System.out.println(">> " + power + " 데미지를 입혔다!");
        return power;
    }
    
    public String getDescription() {
        return power + " 데미지 공격";
    }
}

class HealEffect implements CardEffect {
    private int healAmount;
    
    public HealEffect(int healAmount) {
        this.healAmount = healAmount;
    }
    
    public int execute(Entity user, Entity target) {
        int newHp = Math.min(user.getCurrentHealth() + healAmount, 100);
        user.setCurrentHealth(newHp);
        System.out.println(">> HP를 " + healAmount + "만큼 회복했다!");
        return healAmount;
    }
    
    public String getDescription() {
        return "HP " + healAmount + " 회복";
    }
}

public class Card {
    private String cId;
    private int pp;          // 조사 키 (particle)
    private String cName;    // 카드 이름 (메서드 이름)
    private String method;   // 발동 메시지
    private int cPower;      // 공격력
    private String cDesc;    // 카드 설명
    private String cUseMsg;  // 카드 사용 시 메시지 (nullable)
    private String cImg;     // 카드 이미지 (nullable)
    private int tryNum;      // 최초 획득 트라이
    private CardEffect effect;

    public Card(String cId, int pp, String cName, String method, int cPower,
                String cDesc, String cUseMsg, String cImg, int tryNum, CardEffect effect) {
        this.cId = cId;
        this.pp = pp;
        this.cName = cName;
        this.method = method;
        this.cPower = cPower;
        this.cDesc = cDesc;
        this.cUseMsg = cUseMsg;
        this.cImg = cImg;
        this.tryNum = tryNum;
        this.effect = effect;
    }

    // --- Getters ---

    public String getCId() { return cId; }
    public int getPp() { return pp; }
    public String getCName() { return cName; }
    public String getMethod() { return method; }
    public int getCPower() { return cPower; }
    public String getCDesc() { return cDesc; }
    public String getCUseMsg() { return cUseMsg; }
    public String getCImg() { return cImg; }
    public int getTryNum() { return tryNum; }

    // --- Setters ---

    public void setCId(String cId) { this.cId = cId; }
    public void setPp(int pp) { this.pp = pp; }
    public void setCName(String cName) { this.cName = cName; }
    public void setMethod(String method) { this.method = method; }
    public void setCPower(int cPower) { this.cPower = cPower; }
    public void setCDesc(String cDesc) { this.cDesc = cDesc; }
    public void setCUseMsg(String cUseMsg) { this.cUseMsg = cUseMsg; }
    public void setCImg(String cImg) { this.cImg = cImg; }
    public void setTryNum(int tryNum) { this.tryNum = tryNum; }

    @Override
    public String toString() {
        return "[CARD:" + cId + "] " + cName + " (ATK:" + cPower + ") - " + cDesc;
    }

    public int use(Entity user, Entity target) {
        return effect.execute(user, target);
    }
}
