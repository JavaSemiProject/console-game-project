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



