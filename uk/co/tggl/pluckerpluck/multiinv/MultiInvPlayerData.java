package uk.co.tggl.pluckerpluck.multiinv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

import uk.co.tggl.pluckerpluck.multiinv.MultiInvEnums.MultiInvEvent;

public class MultiInvPlayerData {

    public final MultiInv plugin;
    public ArrayList<String> existingPlayers = new ArrayList<String>();
    private boolean segregateHealth;

    public MultiInvPlayerData(MultiInv instance) {
        this.plugin = instance;

        loadPlayers();
    }

    void storeConfig(HashMap<String, Boolean> config) {
        this.segregateHealth = config.get("health");
    }

    private void loadPlayers() {
        File file = new File("plugins" + File.separator + "MultiInv" + File.separator
                + "Worlds");
        searchFolders(file);
    }

    private void searchFolders(File file) {
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

    private void loadNewInventory(Player player, String group) {
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_NEW, new String[]{player.getName()});
        String inventoryName = "MultiInvInventory";
        storeManualInventory(player, inventoryName, group);
    }

    public void storeCurrentInventory(Player player, String group) {
        String inventoryName = "MultiInvInventory";
        //String inventoryName = "w:" + player.getWorld().getName();
        if (plugin.currentInventories.containsKey(player.getName())) {
            inventoryName = plugin.currentInventories.get(player.getName())[0];
        }
        MultiInvInventory inventory = new MultiInvInventory(player, inventoryName, MultiInv.pluginName);
        saveStateToFile(player, inventory, group);
        plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_SAVE, new String[]{group});
    }

    public void storeManualInventory(Player player, String inventoryName, String group) {
        MultiInvInventory inventory = new MultiInvInventory(player, inventoryName, MultiInv.pluginName);
        String[] array = new String[2];
        array[0] = inventoryName;
        array[1] = "{other}";
        String file = "plugins" + File.separator + "MultiInv" + File.separator
                + "Other" + File.separator + player.getName() + ".data";
        if (group != null) {
            file = "plugins" + File.separator + "MultiInv" + File.separator
                    + "Worlds" + File.separator + group + File.separator + player.getName() + ".data";
            array[1] = "world";
        }
        plugin.currentInventories.put(player.getName(), array);
        MultiInvProperties.saveToProperties(file, inventory.getName(), inventory.toString(), "Stored Inventory");
    }

    public void loadWorldInventory(Player player, String group) {
        if (!existingPlayers.contains(player.getName())) {
            MultiInv.log.info("[" + MultiInv.pluginName + "] New player detected: " + player.getName());
            existingPlayers.add(player.getName());
            return;
        }
        if (plugin.sharesMap.containsKey(group)) {
            group = plugin.sharesMap.get(group);
        }
        if (this.segregateHealth) {
            int health = loadHealthFromFile(player.getName(), group);
            MultiInvHealthRunnable respawnWait = new MultiInvHealthRunnable(player.getName(), health, plugin);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, respawnWait, 40);
            //player.setHealth(health); Waiting for fix by bukkit
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
            plugin.currentInventories.put(player.getName(), array);
            plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_LOAD, new String[]{group});
            return;
        }
        loadNewInventory(player, group); //calls if no inventory is found
        plugin.debugger.debugEvent(MultiInvEvent.INVENTORY_LOAD_NEW, new String[]{player.getName()});
    }

    public void saveStateToFile(Player player, MultiInvInventory inventory, String group) {
        //String world = player.getWorld().getName();
        String file = "plugins" + File.separator + "MultiInv" + File.separator
                + "Worlds" + File.separator + group + File.separator + player.getName() + ".data";
        if (this.segregateHealth) {
            MultiInvProperties.saveToProperties(file, "health:" + group, Integer.toString(player.getHealth()));
        }
        MultiInvProperties.saveToProperties(file, inventory.getName(), inventory.toString(), "Stored Inventory");
    }

    public int loadHealthFromFile(String player, String group) {
        String file = "plugins" + File.separator + "MultiInv" + File.separator
                + "Worlds" + File.separator + group + File.separator + player + ".data";
        String healthString = MultiInvProperties.loadFromProperties(file, "health:" + group, "20");
        return Integer.parseInt(healthString);
    }
}
