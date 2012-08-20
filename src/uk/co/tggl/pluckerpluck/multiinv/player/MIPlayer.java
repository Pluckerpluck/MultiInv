package uk.co.tggl.pluckerpluck.multiinv.player;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

/**
 * Player class designed to store all information for each saved world
 */
public class MIPlayer{

    // Initialize final variables that define the MIPlayer
    final Player player;
    final PlayerInventory inventory;
    MultiInv plugin;

    // Initialize (and assign) variables containing the initial state of an MIPlayer
    private boolean ignored = false;

    public MIPlayer(Player player, MultiInv plugin) {
        this.player = player;
        this.plugin = plugin;
        inventory = player.getInventory();
    }

    // Getters and Setters
    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    /* --------------------
     * PlayerInventory methods
     * --------------------
     */

    // Load methods that will load data into the game
    public void loadInventory(String group, String inventoryName){
    	if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
    		inventoryName = "SURVIVAL";
    	}
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	MIInventory inventory = MIYamlFiles.con.getInventory(player.getName(), group, inventoryName);
        	inventory.loadIntoInventory(this.inventory);
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            MIInventory inventory = config.getInventory(inventoryName);
            inventory.loadIntoInventory(this.inventory);
        }
    }

    public void saveInventory(String group, String inventoryName){
    	if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
    		inventoryName = "SURVIVAL";
    	}
        MIInventory inventory = new MIInventory(this.inventory);
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	MIYamlFiles.con.saveInventory(player.getName(), group, inventory, inventoryName);
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            config.saveInventory(inventory, inventoryName);
        }
    }

    /* --------------------
     * Other methods
     * --------------------
     */

    public void loadHealth(String group){
        if (MIYamlFiles.config.getBoolean("useSQL")){
            player.setHealth(MIYamlFiles.con.getHealth(player.getName(), group));
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            int health = config.getHealth();
            if(health < 0) {
            	health = 0;
            }
            player.setHealth(health);
        }
    }
    
    /* This is needed because of the new compatibility mode.
     * The death teleport event has the health set to -980 or
     * so making it throw an error.
     */
    public void saveFakeHealth(String group, int value) {
    	if (MIYamlFiles.config.getBoolean("useSQL")){
        	if(value < 0) {
        		MIYamlFiles.con.saveHealth(player.getName(), group, 0);
        	}else {
            	MIYamlFiles.con.saveHealth(player.getName(), group, value);
        	}
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            config.saveHealth(value);
        }
    }

    public void saveHealth(String group){
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	if(player.getHealth() < 0) {
        		MIYamlFiles.con.saveHealth(player.getName(), group, 0);
        	}else {
            	MIYamlFiles.con.saveHealth(player.getName(), group, player.getHealth());
        	}
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            config.saveHealth(player.getHealth());
        }
    }

    public void loadGameMode(String group){
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	GameMode gameMode = MIYamlFiles.con.getGameMode(player.getName(), group);
            if (gameMode != null) {
                player.setGameMode(gameMode);
            }
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            GameMode gameMode = config.getGameMode();
            if (gameMode != null) {
                player.setGameMode(gameMode);
            }
        }
    }

    public void saveGameMode(String group){
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	MIYamlFiles.con.saveGameMode(player.getName(), group, player.getGameMode());
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            config.saveGameMode(player.getGameMode());
        }
    }

    public void loadHunger(String group){
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	player.setFoodLevel(MIYamlFiles.con.getHunger(player.getName(), group));
            player.setSaturation(MIYamlFiles.con.getSaturation(player.getName(), group));
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            player.setFoodLevel(config.getHunger());
            player.setSaturation(config.getSaturation());

        }
    }

    public void saveFakeHunger(String group, int hunger, float saturation){
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	MIYamlFiles.con.saveHunger(player.getName(), group, hunger);
        	MIYamlFiles.con.saveSaturation(player.getName(), group, saturation);
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            config.saveHunger(hunger);
            config.saveSaturation(saturation);
        }
    }
    
    public void saveHunger(String group){
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	MIYamlFiles.con.saveHunger(player.getName(), group, player.getFoodLevel());
        	MIYamlFiles.con.saveSaturation(player.getName(), group, player.getSaturation());
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            config.saveHunger(player.getFoodLevel());
            config.saveSaturation(player.getSaturation());
        }
    }

    public void loadExperience(String group){
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	//player.setLevel(MIYamlFiles.con.getLevel(player.getName(), group));
        	//clear the levels
        	//since the set total experience has a bug, let's just do it this way...
        	int[] levels = plugin.getXP(MIYamlFiles.con.getTotalExperience(player.getName(), group));
            MultiInv.log.debug("Setting player level to: " + levels[0]);
        	player.setLevel(levels[0]);
        	player.setTotalExperience(MIYamlFiles.con.getTotalExperience(player.getName(), group));
            MultiInv.log.debug("Setting player xp to: " + levels[1]);
        	player.setExp((float)((float)levels[1]/(float)levels[2]));
            //player.setTotalExperience(MIYamlFiles.con.getTotalExperience(player.getName(), group));
            //player.setExp(MIYamlFiles.con.getExperience(player.getName(), group));
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            player.setLevel(config.getLevel());
            player.setTotalExperience(config.getTotalExperience());
            player.setExp(config.getExperience());
        }
    }

    public void saveExperience(String group){
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	int totalxp = plugin.getTotalXP(player.getLevel(), player.getExp());
        	MultiInv.log.debug("XP Level and xp is: " + player.getLevel() + " " + player.getExp() + " for player " + player.getName());
        	MultiInv.log.debug("Total xp is: " + totalxp);
        	MIYamlFiles.con.saveExperience(player.getName(), group, totalxp);
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            config.saveExperience(player.getTotalExperience(), player.getLevel(), player.getExp());
        }
    }
}
