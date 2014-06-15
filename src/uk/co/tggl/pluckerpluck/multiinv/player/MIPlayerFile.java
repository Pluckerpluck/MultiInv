package uk.co.tggl.pluckerpluck.multiinv.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventoryOld;

import java.io.File;

/**
 * Class that contains all the configuration file methods
 */
public class MIPlayerFile {
    
    // final private Configuration playerFile;
    final private YamlConfiguration playerFile;
    final private YamlConfiguration enderchestFile;
    final private File file;
    final private File enderfile;
    final private String playername;
    final private String uuid;
    
    public MIPlayerFile(Player player, String group) {
        // Find and load configuration file for the player
        File dataFolder = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
        File worldsFolder = new File(dataFolder, "UUIDGroups");
        playername = player.getName();
        uuid = player.getUniqueId().toString();
        file = new File(worldsFolder, group + File.separator + uuid + ".yml");
        enderfile = new File(worldsFolder, group + File.separator + uuid + ".ec.yml");
        playerFile = new YamlConfiguration();
        enderchestFile = new YamlConfiguration();
        load();
    }
    
    public MIPlayerFile(OfflinePlayer player, String group) {
        // Find and load configuration file for the player
        File dataFolder = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
        File worldsFolder = new File(dataFolder, "UUIDGroups");
        playername = player.getName();
        uuid = player.getUniqueId().toString();
        file = new File(worldsFolder, group + File.separator + uuid + ".yml");
        enderfile = new File(worldsFolder, group + File.separator + uuid + ".ec.yml");
        playerFile = new YamlConfiguration();
        enderchestFile = new YamlConfiguration();
        load();
    }
    
    public MIPlayerFile(OfflinePlayer player, String group, boolean convert) {
        // Find and load configuration file for the player
        File dataFolder = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
        File worldsFolder = new File(dataFolder, "UUIDGroups");
        playername = player.getName();
        uuid = player.getUniqueId().toString();
        file = new File(worldsFolder, group + File.separator + uuid + ".yml");
        enderfile = new File(worldsFolder, group + File.separator + uuid + ".ec.yml");
        playerFile = new YamlConfiguration();
        enderchestFile = new YamlConfiguration();
        if(convert) {
            File oldWorldsFolder = new File(dataFolder, "Groups");
            File file1 = new File(oldWorldsFolder, group + File.separator + uuid + ".yml");
            File enderfile1 = new File(oldWorldsFolder, group + File.separator + uuid + ".ec.yml");
            if(file1.exists()) {
                try {
                    playerFile.load(file1);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if(enderfile1.exists()) {
                try {
                    enderchestFile.load(enderfile1);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            save();
        }else {
            load();
        }
    }
    
    private void load() {
        if(file.exists()) {
            try {
                playerFile.load(file);
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
        if(enderfile.exists()) {
            try {
                enderchestFile.load(enderfile);
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
    }
    
    private void save() {
        try {
            playerFile.save(file);
            enderchestFile.save(enderfile);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveInventory() {
        try {
            playerFile.save(file);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveEnder() {
        try {
            enderchestFile.save(enderfile);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    // Load particular inventory for specified player from specified group
    public MIInventory getInventory(String inventoryName) {
        // Get stored string from configuration file
        MIInventory inventory;
        String inventoryString = playerFile.getString(inventoryName, null);
        
        String folder = file.getParentFile().getName();
        MultiInv.log.debug("Loading " + playername + "'s " + inventoryName + " inventory from " + folder);
        
        // Check for old inventory save
        if(inventoryString == null || inventoryString.contains(";-;")) {
            MultiInv.log.debug("First time or old inventory file detected for " + playername + " in " + folder);
            if(inventoryString == null) {
                // seems the older versions have the inventory name in lower case...
                inventoryString = playerFile.getString(inventoryName.toLowerCase());
            }
            inventory = new MIInventoryOld(inventoryString);
        } else {
            inventory = new MIInventory(inventoryString);
        }
        return inventory;
    }
    
    public void saveInventory(MIInventory inventory, String inventoryName) {
        String inventoryString = inventory.toString();
        playerFile.set(inventoryName, inventoryString);
        
        String folder = file.getParentFile().getName();
        MultiInv.log.debug("Saving " + playername + "'s " + inventoryName + " inventory to " + folder);

        saveInventory();
    }
    
    // Load particular enderchest inventory for specified player from specified group
    public MIEnderchestInventory getEnderchestInventory(String inventoryName) {
        // Get stored string from configuration file
        MIEnderchestInventory inventory;
        String inventoryString = enderchestFile.getString(inventoryName, null);
        
        String folder = file.getParentFile().getName();
        MultiInv.log.debug("Loading " + playername + "'s " + inventoryName + " inventory from " + folder);
        inventory = new MIEnderchestInventory(inventoryString);
        return inventory;
    }
    
    public void saveEnderchestInventory(MIEnderchestInventory inventory, String inventoryName) {
        String inventoryString = inventory.toString();
        enderchestFile.set(inventoryName, inventoryString);
        saveEnder();
    }
    
    public double getHealth() {
        double health = playerFile.getDouble("health", 20);
        if(health > 20) {
            health = 20;
        }
        return health;
    }

    @Deprecated
    public void saveHealth(double health) {
        playerFile.set("health", health);
        saveInventory();
    }
    
    public GameMode getGameMode() {
        String gameModeString = playerFile.getString("gameMode", null);
        GameMode gameMode = null;
        if("CREATIVE".equalsIgnoreCase(gameModeString)) {
            gameMode = GameMode.CREATIVE;
        } else if("SURVIVAL".equalsIgnoreCase(gameModeString)) {
            gameMode = GameMode.SURVIVAL;
        }
        return gameMode;
    }
    
    public void saveGameMode(GameMode gameMode) {
        playerFile.set("gameMode", gameMode.toString());
        saveInventory();
    }
    
    public int getHunger() {
        int hunger = playerFile.getInt("hunger", 20);
        if(hunger > 20) {
            hunger = 20;
        }
        return hunger;
    }
    
    public float getSaturation() {
        double saturationDouble = playerFile.getDouble("saturation", 5);
        float saturation = (float) saturationDouble;
        return saturation;
    }

    @Deprecated
    public void saveSaturation(float saturation) {
        playerFile.set("saturation", saturation);
        saveInventory();
    }
    
    public int getTotalExperience() {
        return playerFile.getInt("experience", 0);
    }
    
    public int getLevel() {
        return playerFile.getInt("level", 0);
    }
    
    public float getExperience() {
        double expDouble = playerFile.getDouble("exp", 0);
        float exp = (float) expDouble;
        return exp;
    }

    @Deprecated
    public void saveExperience(int experience, int level, float exp) {
        playerFile.set("experience", experience);
        playerFile.set("level", level);
        playerFile.set("exp", exp);
        saveInventory();
    }
    
    @Deprecated
    public void saveHunger(int hunger) {
        playerFile.set("hunger", hunger);
        saveInventory();
    }
    
    public void saveAll(MIInventory inventory, String inventoryName,
    		int experience, int level, float exp, GameMode gameMode, double health, int hunger, float saturation) {

        String inventoryString = inventory.toString();
        playerFile.set(inventoryName, inventoryString);
        playerFile.set("experience", experience);
        playerFile.set("level", level);
        playerFile.set("exp", exp);
        playerFile.set("gameMode", gameMode.toString());
        playerFile.set("health", health);
        playerFile.set("hunger", hunger);
        playerFile.set("saturation", saturation);
        saveInventory();
    }
}
