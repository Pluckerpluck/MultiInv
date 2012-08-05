package uk.co.tggl.pluckerpluck.multiinv.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventoryOld;

import java.io.File;

/**
 * Class that contains all the configuration file methods
 */
public class MIPlayerFile {
    //final private Configuration playerFile;
    final private YamlConfiguration playerFile;
    final private File file;
    final private String playername;

    public MIPlayerFile(Player player, String group) {
        // Find and load configuration file for the player
        File dataFolder =  Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
        File worldsFolder = new File(dataFolder, "Groups");
        file = new File(worldsFolder, group + File.separator + player.getName() + ".yml");
        playername = player.getName();
        playerFile = new YamlConfiguration();
        load();
    }
    
    public MIPlayerFile(String player, String group) {
        // Find and load configuration file for the player
        File dataFolder =  Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
        File worldsFolder = new File(dataFolder, "Groups");
        file = new File(worldsFolder, group + File.separator + player + ".yml");
        playername = player;
        playerFile = new YamlConfiguration();
        load();
    }

    private void load(){
        if (file.exists()){
            try{
                playerFile.load(file);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            save();
        }
    }

    private void save(){
        try{
            playerFile.save(file);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Load particular inventory for specified player from specified group
    public MIInventory getInventory(String inventoryName){
        // Get stored string from configuration file
        MIInventory inventory;
        String inventoryString = playerFile.getString(inventoryName, null);

        String folder = file.getParentFile().getName();
        MultiInv.log.debug("Loading " + playername + "'s " + inventoryName + " inventory from " + folder);

        // Check for old inventory save
        if (inventoryString == null || inventoryString.contains(";-;")){
        	MultiInv.log.debug("First time or old inventory file detected for " + playername + " in " + folder);
        	if(inventoryString == null) {
        		//seems the older versions have the inventory name in lower case...
        		inventoryString = playerFile.getString(inventoryName.toLowerCase());
        	}
            inventory = new MIInventoryOld(inventoryString);
        }else{
            inventory = new MIInventory(inventoryString);
        }
        return inventory;
    }

    public void saveInventory(MIInventory inventory, String inventoryName){
        String inventoryString = inventory.toString();
        playerFile.set(inventoryName, inventoryString);

        String folder = file.getParentFile().getName();
        MultiInv.log.debug("Saving " + playername + "'s " + inventoryName + " inventory to " + folder);

        save();
    }

    public int getHealth(){
        int health = playerFile.getInt("health", 20);
        if (health > 20) {
            health = 20;
        }
        return health;
    }

    public void saveHealth(int health){
        playerFile.set("health", health);
        save();
    }

    public GameMode getGameMode(){
        String gameModeString = playerFile.getString("gameMode", null);
        GameMode gameMode = null;
        if ("CREATIVE".equalsIgnoreCase(gameModeString)){
            gameMode = GameMode.CREATIVE;
        }else if ("SURVIVAL".equalsIgnoreCase(gameModeString)){
            gameMode = GameMode.SURVIVAL;
        }
        return gameMode;
    }

    public void saveGameMode(GameMode gameMode){
        playerFile.set("gameMode", gameMode.toString());
        save();
    }

    public int getHunger(){
        int hunger = playerFile.getInt("hunger", 20);
        if (hunger > 20) {
            hunger = 20;
        }
        return hunger;
    }

    public float getSaturation(){
        double saturationDouble = playerFile.getDouble("saturation", 5);
        float saturation = (float)saturationDouble;
        return saturation;
    }

    public void saveSaturation(float saturation){
        playerFile.set("saturation", saturation);
        save();
    }

    public int getTotalExperience(){
        return playerFile.getInt("experience", 0);
    }

    public int getLevel(){
        return playerFile.getInt("level", 0);
    }

    public float getExperience(){
        double expDouble = playerFile.getDouble("exp", 0);
        float exp = (float)expDouble;
        return exp;
    }

    public void saveExperience(int experience, int level, float exp){
        playerFile.set("experience", experience);
        playerFile.set("level", level);
        playerFile.set("exp", exp);
        save();
    }

    public void saveHunger(int hunger){
        playerFile.set("hunger", hunger);
        save();
    }


}
