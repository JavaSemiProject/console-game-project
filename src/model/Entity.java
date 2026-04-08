package model;

public interface Entity {
    // --- Getters ---

    public int getHealth();
    public int getCurrentHealth();
    public int getPowerMin();
    public int getPowerMax();
    public int getPp();
    public String getCId();

    // --- Setters ---

    public void setHealth(int health);
    public void setCurrentHealth(int currentHealth);
    public void setPowerMin(int powerMin);
    public void setPowerMax(int powerMax);
    public void setPp(int pp);
    public void setCId(String cId);


    public void takeDamage(int power);
}

class Hero implements Entity {
    private String hId;
    private int health;
    private int currentHealth;
    private int powerMin;
    private int powerMax;
    private int pp;       // 조사 키 (particle)
    private String cId;   // 초기 카드 ID (nullable)

    public Hero(String hId, int health, int powerMin, int powerMax, int pp, String cId) {
        this.hId = hId;
        this.health = health;
        this.currentHealth = health;
        this.powerMin = powerMin;
        this.powerMax = powerMax;
        this.pp = pp;
        this.cId = cId;
    }

    // 공격 시 powerMin~powerMax 범위의 랜덤 데미지 반환
    public int getAttackDamage() {
        return (int) (Math.random() * (powerMax - powerMin + 1)) + powerMin;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public void takeDamage(int damage) {
        currentHealth = Math.max(0, currentHealth - damage);
    }

    public void heal(int amount) {
        currentHealth = Math.min(health, currentHealth + amount);
    }

    // 아이템으로 최대 체력 증가
    public void increaseMaxHealth(int amount) {
        health += amount;
        currentHealth += amount;
    }

    // 아이템으로 공격력 버프
    public void boostPower(int amount) {
        powerMin += amount;
        powerMax += amount;
    }

    // --- Getters ---

    public String getHId() { return hId; }
    public int getHealth() { return health; }
    public int getCurrentHealth() { return currentHealth; }
    public int getPowerMin() { return powerMin; }
    public int getPowerMax() { return powerMax; }
    public int getPp() { return pp; }
    public String getCId() { return cId; }

    // --- Setters ---

    public void setHId(String hId) { this.hId = hId; }
    public void setHealth(int health) { this.health = health; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }
    public void setPowerMin(int powerMin) { this.powerMin = powerMin; }
    public void setPowerMax(int powerMax) { this.powerMax = powerMax; }
    public void setPp(int pp) { this.pp = pp; }
    public void setCId(String cId) { this.cId = cId; }

    @Override
    public String toString() {
        return "[HERO:" + hId + "] HP:" + currentHealth + "/" + health +
                " ATK:" + powerMin + "~" + powerMax;
    }
}

class NPC implements Entity {
    private String nId;
    private String nName;
    private String nDesc;
    private int health;
    private int currentHealth;
    private boolean isBoss;
    private int powerMin;
    private int powerMax;
    private int pp;       // 조사 키 (particle)
    private String cId;   // 보유 카드 ID (nullable)

    public NPC(String nId, String nName, String nDesc, int health,
               boolean isBoss, int powerMin, int powerMax, int pp, String cId) {
        this.nId = nId;
        this.nName = nName;
        this.nDesc = nDesc;
        this.health = health;
        this.currentHealth = health;
        this.isBoss = isBoss;
        this.powerMin = powerMin;
        this.powerMax = powerMax;
        this.pp = pp;
        this.cId = cId;
    }

    // 공격 시 powerMin~powerMax 범위의 랜덤 데미지 반환
    public int getAttackDamage() {
        return (int) (Math.random() * (powerMax - powerMin + 1)) + powerMin;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public void takeDamage(int damage) {
        currentHealth = Math.max(0, currentHealth - damage);
    }

    // --- Getters ---

    public String getNId() { return nId; }
    public String getNName() { return nName; }
    public String getNDesc() { return nDesc; }
    public int getHealth() { return health; }
    public int getCurrentHealth() { return currentHealth; }
    public boolean isBoss() { return isBoss; }
    public int getPowerMin() { return powerMin; }
    public int getPowerMax() { return powerMax; }
    public int getPp() { return pp; }
    public String getCId() { return cId; }

    // --- Setters ---

    public void setNId(String nId) { this.nId = nId; }
    public void setNName(String nName) { this.nName = nName; }
    public void setNDesc(String nDesc) { this.nDesc = nDesc; }
    public void setHealth(int health) { this.health = health; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }
    public void setBoss(boolean boss) { isBoss = boss; }
    public void setPowerMin(int powerMin) { this.powerMin = powerMin; }
    public void setPowerMax(int powerMax) { this.powerMax = powerMax; }
    public void setPp(int pp) { this.pp = pp; }
    public void setCId(String cId) { this.cId = cId; }

    @Override
    public String toString() {
        return "[" + (isBoss ? "BOSS" : "NPC") + "] " + nName +
                " HP:" + currentHealth + "/" + health +
                " ATK:" + powerMin + "~" + powerMax;
    }
}
