package uk.co.tggl.pluckerpluck.multiinv.api;

import org.bukkit.GameMode;

import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

public class MIAPIPlayer {
    
    String playername;
    MIInventory inventory;
    MIEnderchestInventory enderchest;
    GameMode gm;
    int xpLevel = 0;
    float xp = 0;
    int health = 0;
    int foodlevel = 0;
    float saturation = 5;
    
    public MIAPIPlayer(String playername) {
        this.playername = playername;
    }
    
    public MIInventory getInventory() {
        return inventory;
    }
    
    public void setInventory(MIInventory inventory) {
        this.inventory = inventory;
    }
    
    public MIEnderchestInventory getEnderchest() {
        return enderchest;
    }
    
    public void setEnderchest(MIEnderchestInventory enderchest) {
        this.enderchest = enderchest;
    }
    
    public GameMode getGm() {
        return gm;
    }
    
    public void setGm(GameMode gm) {
        this.gm = gm;
    }
    
    public int getXpLevel() {
        return xpLevel;
    }
    
    public void setXpLevel(int xpLevel) {
        this.xpLevel = xpLevel;
    }
    
    public float getXp() {
        return xp;
    }
    
    public void setXp(float xp) {
        this.xp = xp;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public int getFoodlevel() {
        return foodlevel;
    }
    
    public void setFoodlevel(int foodlevel) {
        this.foodlevel = foodlevel;
    }
    
    public float getSaturation() {
        return saturation;
    }
    
    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }
    
    public String getPlayername() {
        return playername;
    }
    
}
