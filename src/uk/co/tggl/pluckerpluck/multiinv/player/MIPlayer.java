package uk.co.tggl.pluckerpluck.multiinv.player;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

/**
 * Player class designed to store all information for each saved world
 */
public class MIPlayer{

    // Initialize final variables that define the MIPlayer
    final Player player;
    final PlayerInventory inventory;

    // Initialize (and assign) variables containing the initial state of an MIPlayer
    private boolean ignored = false;

    public MIPlayer(Player player) {
        this.player = player;
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
            player.setHealth(config.getHealth());
        }
    }

    public void saveHealth(String group){
        if (MIYamlFiles.config.getBoolean("useSQL")){
        	MIYamlFiles.con.saveHealth(player.getName(), group, player.getHealth());
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
            player.setTotalExperience(MIYamlFiles.con.getTotalExperience(player.getName(), group));
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
        	MIYamlFiles.con.saveExperience(player.getName(), group, player.getTotalExperience());
        }else{
            MIPlayerFile config = new MIPlayerFile(player, group);
            config.saveExperience(player.getTotalExperience(), player.getLevel(), player.getExp());
        }
    }
}
