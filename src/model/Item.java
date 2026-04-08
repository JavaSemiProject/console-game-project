package model;

interface ItemEffect {
    int execute(Entity user);
    String getDescription();
}

class BoostPowerEffect implements ItemEffect {
    private int power;
    private Item item;

    public BoostPowerEffect(int power, Item item) {
        this.power = power;
        this.item = item;
    }

    public int execute(Entity user) {
        user.boostPower(power);
        System.out.println(">> 공격력이 " + power + "만큼 증가했다!");
        return power;
    }

    public String getDescription() {
        return String.format("%s 공격력이 %d만큼 증가한다.", item.getIName(), power);
    }
}

class ItemHealEffect implements ItemEffect {
    private int healAmount;
    private Item item;

    public ItemHealEffect(int healAmount, Item item) {
        this.healAmount = healAmount;
        this.item = item;
    }

    public int execute(Entity user) {
        user.heal(healAmount);
        System.out.println(">> HP를 " + healAmount + "만큼 회복했다!");
        return healAmount;
    }

    public String getDescription() {
        return String.format("%s %d만큼 체력을 회복한다.", item.getIName(), healAmount);
    }
}

class ItemMaxHpIncreaseEffect implements ItemEffect {
    private int increaseAmount;
    private Item item;

    public ItemMaxHpIncreaseEffect(int increaseAmount, Item item) {
        this.increaseAmount = increaseAmount;
        this.item = item;
    }

    public int execute(Entity user) {
        user.increaseMaxHealth(increaseAmount);
        System.out.println(">> 최대 체력이 " + increaseAmount + "만큼 증가했다!");
        return increaseAmount;
    }

    public String getDescription() {
        return String.format("%s 최대 체력이 %d만큼 증가한다.", item.getIName(), increaseAmount);
    }
}

public class Item {
    private String iId;
    private String iName;
    private int heal;
    private int power;
    private int hp;              // 최대 체력 증가량
    private String iDesc;
    private String iUseMsg;
    private String iImg;
    private int tryNum;
    private int pp;
    private ItemEffect effect;

    private Item(Builder builder) {
        this.iId = builder.iId;
        this.iName = builder.iName;
        this.heal = builder.heal;
        this.power = builder.power;
        this.hp = builder.hp;
        this.iDesc = builder.iDesc;
        this.iUseMsg = builder.iUseMsg;
        this.iImg = builder.iImg;
        this.tryNum = builder.tryNum;
        this.pp = builder.pp;
        this.effect = builder.effect;
    }

    public static class Builder {
        private String iId;
        private String iName;
        private int heal;
        private int power;
        private int hp;
        private String iDesc;
        private String iUseMsg;
        private String iImg;
        private int tryNum;
        private int pp;
        private ItemEffect effect;

        public Builder iId(String iId) { this.iId = iId; return this; }
        public Builder iName(String iName) { this.iName = iName; return this; }
        public Builder heal(int heal) { this.heal = heal; return this; }
        public Builder power(int power) { this.power = power; return this; }
        public Builder hp(int hp) { this.hp = hp; return this; }
        public Builder iDesc(String iDesc) { this.iDesc = iDesc; return this; }
        public Builder iUseMsg(String iUseMsg) { this.iUseMsg = iUseMsg; return this; }
        public Builder iImg(String iImg) { this.iImg = iImg; return this; }
        public Builder tryNum(int tryNum) { this.tryNum = tryNum; return this; }
        public Builder pp(int pp) { this.pp = pp; return this; }
        public Builder effect(ItemEffect effect) { this.effect = effect; return this; }

        public Item build() {
            return new Item(this);
        }
    }

    // --- Getters ---

    public String getIId() { return iId; }
    public String getIName() { return iName; }
    public int getHeal() { return heal; }
    public int getPower() { return power; }
    public int getHp() { return hp; }
    public String getIDesc() { return iDesc; }
    public String getIUseMsg() { return iUseMsg; }
    public String getIImg() { return iImg; }
    public int getTryNum() { return tryNum; }
    public int getPp() { return pp; }

    // --- Setters ---

    public void setIId(String iId) { this.iId = iId; }
    public void setIName(String iName) { this.iName = iName; }
    public void setHeal(int heal) { this.heal = heal; }
    public void setPower(int power) { this.power = power; }
    public void setHp(int hp) { this.hp = hp; }
    public void setIDesc(String iDesc) { this.iDesc = iDesc; }
    public void setIUseMsg(String iUseMsg) { this.iUseMsg = iUseMsg; }
    public void setIImg(String iImg) { this.iImg = iImg; }
    public void setTryNum(int tryNum) { this.tryNum = tryNum; }
    public void setPp(int pp) { this.pp = pp; }

    @Override
    public String toString() {
        return "[ITEM:" + iId + "] " + iName +
                " (회복:" + heal + " 공격:" + power + " HP:" + hp + ") - " + iDesc;
    }

    public int use(Entity user) {
        return effect.execute(user);
    }
}
