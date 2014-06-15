package uk.co.tggl.pluckerpluck.multiinv.player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;

import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

public class MIPlayerCache {
    
    String playername;
    UUID uuid;
    ConcurrentHashMap<String, MIInventory> inventory = new ConcurrentHashMap<String, MIInventory>();
    ConcurrentHashMap<String, MIEnderchestInventory> enderchest = new ConcurrentHashMap<String, MIEnderchestInventory>();
    GameMode gm;
    int xpLevel = 0;
    float xp = 0;
    int totalxp = 0;
    double health = 20;
    int foodlevel = 20;
    float saturation = 5;
    MIPlayerFile file = null;
    
    public MIPlayerCache(OfflinePlayer player) {
        this.playername = player.getName();
        uuid = player.getUniqueId();
    }
    
    public void setFile(MIPlayerFile file) {
    	this.file = file;
    }
    
    public MIPlayerFile getFile() {
    	return file;
    }
    
    public MIInventory getInventory(String gamemode) {
        return inventory.get(gamemode.toUpperCase());
    }
    
    public void setInventory(MIInventory inventory, String gamemode) {
        this.inventory.put(gamemode.toUpperCase(), inventory);
    }
    
    public MIEnderchestInventory getEnderchest(String gamemode) {
        return enderchest.get(gamemode.toUpperCase());
    }
    
    public void setEnderchest(MIEnderchestInventory enderchest, String gamemode) {
        this.enderchest.put(gamemode.toUpperCase(), enderchest);
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
    
    public int getTotalXp() {
    	return totalxp;
    }
    
    public void setTotalXp(int total) {
    	totalxp = total;
    }
    
    public double getHealth() {
        return health;
    }
    
    public void setHealth(double health) {
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
    
    public UUID getUniqueId() {
    	return uuid;
    }
    
}
