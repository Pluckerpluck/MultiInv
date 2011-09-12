package uk.co.tggl.pluckerpluck.multiinv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MultiInvPlayerData {

    public static final ArrayList<String> existingPlayers = new ArrayList<String>();
    private static boolean segregateHealth;

    static void storeConfig(HashMap<String, Boolean> config) {
        segregateHealth = config.get("health");
    }

    static void loadPlayers() {
        File file = new File("plugins" + File.separator + "MultiInv" + File.separator
                + "Worlds");
        searchFolders(file);
    }

    private static void searchFolders(File file) {
        if (file.isDirectory()) {
            String internalNames[] = file.list();
            for (String name : internalNames) {
                searchFolders(new File(file.getAbsolutePath() + File.separator + name));
            }
        } else {
            String fileName = file.getName().split("\\.")[0];
            if (!existingPlayers.contains(fileName)) {
                existingPlayers.add(fileName);
            }
        }
    }

    private static void loadNewInventory(Player player, String group) {
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        //plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_NEW, new String[]{player.getName()});
        // TODO: Add Debugging
        String inventoryName = "MultiInvInventory";
        storeManualInventory(player, inventoryName, group);
    }

    public static void storeCurrentInventory(Player player, String group) {
        String inventoryName = "MultiInvInventory";
        if (MultiInv.currentInventories.containsKey(player.getName())) {
            inventoryName = MultiInv.currentInventories.get(player.getName())[0];
        }
        MultiInvInventory inventory = new MultiInvInventory(player, inventoryName, MultiInv.pluginName);
        saveStateToFile(player, inventory, group);
        // TODO: Add Debugging
        // plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_SAVE, new String[]{group});
    }

    public static void storeManualInventory(Player player, String inventoryName, String group) {
        MultiInvInventory inventory = new MultiInvInventory(player, inventoryName, MultiInv.pluginName);
        String[] array = new String[2];
        array[0] = inventoryName;
        array[1] = "{other}";
        String file = "plugins" + File.separator + "MultiInv" + File.separator
                + "Other" + File.separator + player.getName() + ".data";
        if (group != null) {
            file = "plugins" + File.separator + "MultiInv" + File.separator
                    + "Worlds" + File.separator + group + File.separator + player.getName() + ".data";
            array[1] = group;
        }
        MultiInv.currentInventories.put(player.getName(), array);
        MultiInvProperties.saveToProperties(file, inventory.getName(), inventory.toString(), "Stored Inventory");
    }
    
    public static void loadWorldInventory(Player player, String group) {
        loadWorldInventory(player, group, true);
    }
    
    public static void loadWorldInventory(Player player, String group, boolean loadHealth) {
        if (!existingPlayers.contains(player.getName())) {
            MultiInv.log.info("[" + MultiInv.pluginName + "] New player detected: " + player.getName());
            existingPlayers.add(player.getName());
            return;
        }
        if (MultiInv.sharesMap.containsKey(group)) {
            group = MultiInv.sharesMap.get(group);
        }
        if (segregateHealth && loadHealth) {
            int health = loadHealthFromFile(player.getName(), group);
            MultiInvHealthRunnable respawnWait = new MultiInvHealthRunnable(player.getName(), health);
            
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MultiInv");
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, respawnWait, 40);
            //player.setHealth(health); TODO: Health (Waiting for fix by bukkit)
        }

        String inventoryName = "MultiInvInventory";
        String file = "plugins" + File.separator + "MultiInv" + File.separator
                + "Worlds" + File.separator + group + File.separator + player.getName() + ".data";
        String tmpInventory = MultiInvProperties.loadFromProperties(file, inventoryName);
        if (tmpInventory != null) {
            MultiInvInventory inventory = new MultiInvInventory();
            inventory.fromString(tmpInventory); // converts properties string to MultiInvInventory
            inventory.getInventory(player); //sets players inventory
            String[] array = new String[2];
            array[0] = inventoryName;
            array[1] = group;
            MultiInv.currentInventories.put(player.getName(), array);
            // TODO: Add Debugging
            //plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_LOAD, new String[]{group});
            return;
        }
        loadNewInventory(player, group); //calls if no inventory is found
        // TODO: Add Debugging
        //plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_LOAD_NEW, new String[]{player.getName()});
    }

    public static void saveStateToFile(Player player, MultiInvInventory inventory, String group) {
        //String world = player.getWorld().getName();
        String file = "plugins" + File.separator + "MultiInv" + File.separator
                + "Worlds" + File.separator + group + File.separator + player.getName() + ".data";
        if (segregateHealth) {
            MultiInvProperties.saveToProperties(file, "health:" + group, Integer.toString(player.getHealth()));
        }
        MultiInvProperties.saveToProperties(file, inventory.getName(), inventory.toString(), "Stored Inventory");
    }

    public static int loadHealthFromFile(String player, String group) {
        String file = "plugins" + File.separator + "MultiInv" + File.separator
                + "Worlds" + File.separator + group + File.separator + player + ".data";
        String healthString = MultiInvProperties.loadFromProperties(file, "health:" + group, "20");
        return Integer.parseInt(healthString);
    }
}
