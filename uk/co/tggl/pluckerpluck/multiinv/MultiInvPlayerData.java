package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MultiInvPlayerData {

    public static ArrayList<String> existingPlayers = new ArrayList<String>();
    private static boolean isHealthSplit;
    private static boolean isHungerSplit;
    private static boolean isExpSplit;
    static boolean restoreGameModes;


    static void storeConfig(HashMap<String, Boolean> config) {
        isHealthSplit = config.get("isHealthSplit");
        isHungerSplit = config.get("isHungerSplit");
        isExpSplit = config.get("isExpSplit");
        restoreGameModes = config.get("restoreGameModes");
    }

    private static void loadNewInventory(Player player, String group) {
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        //plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_NEW, new String[]{player.getName()});
        // TODO: Add Debugging
        String inventoryName;
        if (MultiInv.creativeGroups.contains(group)){
            inventoryName = "creative";
        }else{
            inventoryName = "survival";
        }
        storeManualInventory(player, inventoryName, group);
    }

    public static void storeCurrentInventory(Player player, String group) {

        // Load correct config file
        File dataFile = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
        File file = new File(dataFile, "Worlds" + File.separator + group + File.separator + player.getName() + ".yml");
        Configuration playerFile = new Configuration(file);
        playerFile.load();
        String inventoryName = "survival";
        if (MultiInv.currentInventories.containsKey(player.getName())) {
            inventoryName = MultiInv.currentInventories.get(player.getName())[0];
        }
        MultiInvInventory inventory = new MultiInvInventory(player, inventoryName, MultiInv.pluginName);
        playerFile.setProperty(inventoryName, inventory.toString());

        saveStateToFile(playerFile, player);

        // Save gameMode
        int creativeGroup = 0;
        if (MultiInv.creativeGroups.contains(group)){
            creativeGroup = 1;
        }
        playerFile.setProperty("gameMode", playerFile.getInt("gameMode", creativeGroup));

        playerFile.save();
        // TODO: Add Debugging
        // plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_SAVE, new String[]{group});
    }

    public static void storeManualInventory(Player player, String inventoryName, String group) {
        MultiInvInventory inventory = new MultiInvInventory(player, inventoryName, MultiInv.pluginName);
        String[] array = new String[2];
        array[0] = inventoryName;
        array[1] = "{other}";

        // Load correct config file
        File dataFile = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
        File file = new File(dataFile, "Worlds" + File.separator + "other" + File.separator + player.getName() + ".yml");
        if (group != null) {
            file = new File(dataFile, "Worlds" + File.separator + group + File.separator + player.getName() + ".yml");
            array[1] = group;
        }
        Configuration playerFile = new Configuration(file);

        // Store current inventory info into the array
        MultiInv.currentInventories.put(player.getName(), array);

        // Store inventory into file
        playerFile.setProperty(inventoryName, inventory.toString());
    }
    
    public static void loadWorldInventory(Player player, String group, boolean loadExtras) {
        if (MultiInv.sharesMap.containsKey(group)) {
            group = MultiInv.sharesMap.get(group);
        }

        // Load correct config file
        File dataFile = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
        File file = new File(dataFile, "Worlds" + File.separator + group + File.separator + player.getName() + ".yml");
        Configuration playerFile = new Configuration(file);
        playerFile.load();

        // Get gameMode
        int creativeGroup = 0;
        if (MultiInv.creativeGroups.contains(group)){
            creativeGroup = 1;
        }
        player.setGameMode(GameMode.getByValue(playerFile.getInt("gameMode", creativeGroup)));

        if (loadExtras) {
             loadStateFromFile(playerFile, player);
        }

        String inventoryName;
        if (player.getGameMode() == GameMode.CREATIVE){
            inventoryName = "creative";
        }else{
            inventoryName = "survival";
        }

        // Load the inventory into the Player
        String tmpInventory = playerFile.getString(inventoryName, null);
        if (tmpInventory != null) {
            MultiInvInventory inventory = new MultiInvInventory();
            inventory.fromString(tmpInventory); // converts properties string to MultiInvInventory
            inventory.getInventory(player); //sets players inventory
            String[] array = new String[2];
            array[0] = inventoryName;
            array[1] = group;
            MultiInv.currentInventories.put(player.getName(), array);
            // TODO: Add Debugging
            // plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_LOAD, new String[]{group});
        }else{
            loadNewInventory(player, group); //calls if no inventory is found
            // TODO: Add Debugging
            //plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_LOAD_NEW, new String[]{player.getName()});
        }
        playerFile.save();
    }

    public static void saveStateToFile(Configuration playerFile, Player player) {
        if (isHealthSplit) {
        	int health = player.getHealth();
            if (health <= 0 || health > 20) {
            	health = 20;
            }
            playerFile.setProperty("health", health);
        }
        if(isHungerSplit){
            playerFile.setProperty("hungerSaturation", player.getSaturation());
            playerFile.setProperty("hungerLevel", player.getFoodLevel());
            playerFile.setProperty("exhaustion", player.getExhaustion());
        }
        if (isExpSplit) {
            playerFile.setProperty("totalExp", player.getTotalExperience());
        }

        playerFile.save();
    }

    public static HashMap<String, String> loadStateFromFile(Configuration playerFile, Player player) {
        HashMap<String, String> data = new HashMap<String, String>();

        //Store health
        data.put("health", playerFile.getString("health", "20"));

        //Store hunger levels
        data.put("hungerSaturation", playerFile.getString("hungerSaturation", "20"));
        data.put("hungerLevel", playerFile.getString("hungerLevel", "20"));
        data.put("exhaustion", playerFile.getString("exhaustion", "0"));

        //Store exp
        data.put("totalExp", playerFile.getString("totalExp", "0"));

        //Load info into Player
        if (player != null) {
            if (isHealthSplit) {
                int health = Integer.parseInt(data.get("health"));
                if (health <= 0 || health > 20) {
                	health = 20;
                }
                player.setHealth(health);
            }
            if(isHungerSplit){
                float saturation = Float.parseFloat(data.get("hungerSaturation"));
                player.setSaturation(saturation);

                float exhaustion = Float.parseFloat(data.get("exhaustion"));
                player.setExhaustion(exhaustion);

                int hunger = Integer.parseInt(data.get("hungerLevel"));
                player.setFoodLevel(hunger);
            }
            if (isExpSplit) {
                int totalExp = Integer.parseInt(data.get("totalExp"));
                player.setTotalExperience(totalExp);
            }
        }
        return data;
    }

}
