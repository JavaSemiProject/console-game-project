package model;

public interface Entity {
    public void takeDamage(int damage);
    public void boostPower(int amount);
    public void heal(int amount);
    public void increaseMaxHealth(int amount);

    public int getHealth();
    public int getCurrentHealth();
    public int getPowerMin();
    public int getPowerMax();
    public int getPp();

    public void setHealth(int health);
    public void setCurrentHealth(int currentHealth);
    public void setPowerMin(int powerMin);
    public void setPowerMax(int powerMax);
    public void setPp(int pp);
    public void setCId(String cId);
}
