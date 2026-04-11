package model;

interface CardEffect {
    int execute(Entity user, Entity target);
    String getDescription();  // 효과 설명 (UI 출력용)
}

class AttackEffect implements CardEffect {
    private int power;
    private Card card;
    
    public AttackEffect(int power, Card card) {
        this.power = power;
        this.card = card;
    }
    
    public int execute(Entity user, Entity target) {
        int min = user.getPowerMin();
        int max = user.getPowerMax();
        int base = (max > min) ? min + (int)(Math.random() * (max - min + 1)) : min;
        int total = base + card.getCPower();
        target.takeDamage(total);
        return total;
    }
    
    public String getDescription() {
        return String.format("%s 대상에게 %d의 대미지를 입힌다.", card.getCDesc(), power);
    }
}

// 나머지 카드 효과는 보류
//class HealEffect implements CardEffect {
//    private int healAmount;
//    private Card card;
//
//    public HealEffect(int healAmount, Card card) {
//        this.healAmount = healAmount;
//        this.card = card;
//    }
//
//    public int execute(Entity user, Entity target) {
//        int newHp = Math.min(user.getCurrentHealth() + healAmount, 100);
//        user.setCurrentHealth(newHp);
//        System.out.println(">> HP를 " + healAmount + "만큼 회복했다!");
//        return healAmount;
//    }
//
//    public String getDescription() {
//        return String.format("%s %d만큼 체력을 회복한다.", card.getCDesc(), healAmount);
//    }
//}
//
//class maxHpIncreaseEffect implements CardEffect {
//    private int increaseAmount;
//    private Card card;
//
//    public maxHpIncreaseEffect(int increaseAmount, Card card) {
//        this.increaseAmount = increaseAmount;
//        this.card = card;
//    }
//
//    public int execute(Entity user, Entity target) {
//        int newHp = Math.min(user.getCurrentHealth() + increaseAmount, 100);
//        user.setCurrentHealth(newHp);
//        System.out.println(">> 최대 체력이 " + increaseAmount + "만큼 증가했다!");
//        return increaseAmount;
//    }
//
//    public String getDescription() {
//        return String.format("%s 최대 체력이 %d만큼 증가한다.", card.getCDesc(), increaseAmount);
//    }
//}

public class Card {
    private String cId;
    private int pp;          // 조사 키 (particle)
    private String cName;    // 카드 이름 (메서드 이름)
    private String method;   // 발동 메시지
    private int cPower;      // 공격력
    private String cDesc;    // 카드 설명
    private String cUseMsg;  // 카드 사용 시 메시지 (nullable)
    private String cImg;     // 카드 이미지 (nullable)
    private int tryNum;         // 최초 획득 트라이
    private CardEffect effect;
    private boolean combatUsable = true; // 전투 중 사용 가능 여부

    private Card(Builder builder) {
        this.cId = builder.cId;
        this.pp = builder.pp;
        this.cName = builder.cName;
        this.method = builder.method;
        this.cPower = builder.cPower;
        this.cDesc = builder.cDesc;
        this.cUseMsg = builder.cUseMsg;
        this.cImg = builder.cImg;
        this.tryNum = builder.tryNum;
        this.effect = builder.effect;
        this.combatUsable = true;
    }

    public static class Builder {
        private String cId;
        private int pp;
        private String cName;
        private String method;
        private int cPower;
        private String cDesc;
        private String cUseMsg;
        private String cImg;
        private int tryNum;
        private CardEffect effect;

        public Builder cId(String cId) { this.cId = cId; return this; }
        public Builder pp(int pp) { this.pp = pp; return this; }
        public Builder cName(String cName) { this.cName = cName; return this; }
        public Builder method(String method) { this.method = method; return this; }
        public Builder cPower(int cPower) { this.cPower = cPower; return this; }
        public Builder cDesc(String cDesc) { this.cDesc = cDesc; return this; }
        public Builder cUseMsg(String cUseMsg) { this.cUseMsg = cUseMsg; return this; }
        public Builder cImg(String cImg) { this.cImg = cImg; return this; }
        public Builder tryNum(int tryNum) { this.tryNum = tryNum; return this; }
        public Builder effect(CardEffect effect) { this.effect = effect; return this; }

        public Card build() {
            return new Card(this);
        }
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
    public void setEffect(CardEffect effect) { this.effect = effect; }
    public boolean isCombatUsable() { return combatUsable; }
    public void setCombatUsable(boolean combatUsable) { this.combatUsable = combatUsable; }

    @Override
    public String toString() {
        return "[CARD:" + cId + "] " + cName + " (ATK:" + cPower + ") - " + cDesc;
    }

    public int use(Entity user, Entity target) {
        return effect.execute(user, target);
    }
    public static Card createAttackCard(String cId, int pp, String cName, String method,
                                        int power, String desc, String cUseMsg, String cImg, int tryNum) {
        Card card = new Card.Builder()
            .cId(cId)
            .pp(pp)
            .cName(cName)
            .method(method)
            .cPower(power)
            .cDesc(desc)
            .cUseMsg(cUseMsg)
            .cImg(cImg)
            .tryNum(tryNum)
            .build();
        card.setEffect(new AttackEffect(power, card)); // 같은 패키지라 접근 가능
        return card;
    }

}
