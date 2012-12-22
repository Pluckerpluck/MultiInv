package uk.co.tggl.pluckerpluck.multiinv;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import Tux2.TuxTwoLib.TuxTwoPlayer;

import uk.co.tggl.pluckerpluck.multiinv.api.MIAPIPlayer;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIItemStack;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayerFile;

public class MultiInvAPI {
    
    MultiInv plugin;
    public static final int CREATIVE = 1;
    public static final int SURVIVAL = 0;
    
    public MultiInvAPI(MultiInv plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Returns the specific player's inventory for a specific world and game mode.
     * 
     * @param player
     *            The player you want to look up.
     * @param world
     *            The world's name (not group name)
     * @param gm
     *            The game mode of the inventory you want to retrieve.
     * @return
     */
    public MIInventory getPlayerInventory(String player, String world, GameMode gm) {
        Player giveplayer = plugin.getServer().getPlayer(player);
        boolean playeronline = true;
        if((giveplayer == null || !giveplayer.isOnline())) {
            // See if the player has data files
            
            // Find the player folder
            File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
            
            // Find player name
            for(File playerfile : playerfolder.listFiles()) {
                String filename = playerfile.getName();
                String playername = filename.substring(0, filename.length() - 4);
                
                if(playername.trim().equalsIgnoreCase(player)) {
                    Player target = TuxTwoPlayer.getOfflinePlayer(playername);
                    if(target != null) {
                        target.loadData();
                        giveplayer = target;
                    }
                }
            }
        }
        if(giveplayer != null) {
            // Let's see if the player is in the same world group.
            if((playeronline && MIPlayerListener.getGroup(giveplayer.getWorld().getName()).equalsIgnoreCase(MIPlayerListener.getGroup(world))) ||
                    (!playeronline && MIYamlFiles.logoutworld.get(giveplayer.getName()).equals(MIPlayerListener.getGroup(world)))) {
                // If they are in the same world, yet are in the wrong game mode, let's get the inventory from the file.
                if(MIYamlFiles.config.getBoolean("separateGamemodeInventories", true) && (giveplayer.getGameMode() != gm)) {
                    String inventoryName = "CREATIVE";
                    if(GameMode.SURVIVAL == gm) {
                        inventoryName = "SURVIVAL";
                    }
                    if(MIYamlFiles.config.getBoolean("useSQL")) {
                        MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventoryName);
                        return inventory;
                    } else {
                        MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                        MIInventory inventory = config.getInventory(inventoryName);
                        return inventory;
                    }
                    // If they are currently using the inventory, let's just grab it...
                } else {
                    return new MIInventory(giveplayer);
                }
                // If we are getting an inventory from another world let's just load it.
            } else {
                String inventoryName = "CREATIVE";
                if(GameMode.SURVIVAL == gm) {
                    inventoryName = "SURVIVAL";
                }
                if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
                    inventoryName = "SURVIVAL";
                }
                if(MIYamlFiles.config.getBoolean("useSQL")) {
                    MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventoryName);
                    return inventory;
                } else {
                    MIPlayerFile config = new MIPlayerFile(player, MIPlayerListener.getGroup(world));
                    MIInventory inventory = config.getInventory(inventoryName);
                    return inventory;
                }
            }
            // The player isn't online. Let's do it in offline mode then...
        }
        return null;
        
    }
    
    /**
     * Sets the player's inventory using a MIInventory
     * 
     * @param player
     *            The player's name
     * @param world
     *            The world name that you want to set the inventory in
     * @param gm
     *            Gamemode
     * @param inventory
     *            The inventory
     * @return True upon success, false upon error.
     */
    public boolean setPlayerInventory(String player, String world, GameMode gm, MIInventory inventory) {
        Player giveplayer = plugin.getServer().getPlayer(player);
        String currentworld = "";
        boolean offlineplayer = false;
        if(giveplayer != null && giveplayer.isOnline()) {
            currentworld = giveplayer.getWorld().getName();
        } else {
            giveplayer = TuxTwoPlayer.getOfflinePlayer(player);
            if(giveplayer == null) {
                return false;
            }
            currentworld = MIYamlFiles.logoutworld.get(player);
            offlineplayer = true;
        }
        if((!offlineplayer && MIPlayerListener.getGroup(currentworld).equalsIgnoreCase(MIPlayerListener.getGroup(world))) ||
                (offlineplayer && currentworld.equalsIgnoreCase(MIPlayerListener.getGroup(world)))) {
            if(MIYamlFiles.config.getBoolean("separateGamemodeInventories", true) && (giveplayer.getGameMode() != gm)) {
                String inventoryName = "CREATIVE";
                if(GameMode.SURVIVAL == gm) {
                    inventoryName = "SURVIVAL";
                }
                if(MIYamlFiles.config.getBoolean("useSQL")) {
                    MIYamlFiles.con.saveInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventory, inventoryName);
                    return true;
                } else {
                    MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                    config.saveInventory(inventory, inventoryName);
                    return true;
                }
                // If they are currently using the inventory, let's set it...
            } else {
                inventory.loadIntoInventory(giveplayer.getInventory());
                if(offlineplayer) {
                    giveplayer.saveData();
                }
                return true;
            }
            // They aren't in the same world, so let's just save the inventory.
        } else {
            String inventoryName = "CREATIVE";
            if(GameMode.SURVIVAL == gm) {
                inventoryName = "SURVIVAL";
            }
            if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
                inventoryName = "SURVIVAL";
            }
            if(MIYamlFiles.config.getBoolean("useSQL")) {
                MIYamlFiles.con.saveInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventory, inventoryName);
                return true;
            } else {
                MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                config.saveInventory(inventory, inventoryName);
                return true;
            }
        }
    }
    
    /**
     * Adds a single item to the player's inventory.
     * 
     * @param player
     *            The player's name
     * @param world
     *            The world to add it to.
     * @param gm
     *            The game mode
     * @param itemstack
     *            The item to add.
     * @return true upon successful adding, false if the inventory was full or player not found.
     */
    public boolean addItemToInventory(String player, String world, GameMode gm, MIItemStack itemstack) {
        Player giveplayer = plugin.getServer().getPlayer(player);
        String currentworld = "";
        boolean offlineplayer = false;
        if(giveplayer != null && giveplayer.isOnline()) {
            currentworld = giveplayer.getWorld().getName();
        } else {
            giveplayer = TuxTwoPlayer.getOfflinePlayer(player);
            if(giveplayer == null) {
                return false;
            }
            currentworld = MIYamlFiles.logoutworld.get(player);
            offlineplayer = true;
        }
        if((!offlineplayer && MIPlayerListener.getGroup(currentworld).equalsIgnoreCase(MIPlayerListener.getGroup(world))) ||
                (offlineplayer && currentworld.equalsIgnoreCase(MIPlayerListener.getGroup(world)))) {
            if(MIYamlFiles.config.getBoolean("separateGamemodeInventories", true) && (giveplayer.getGameMode() != gm)) {
                String inventoryName = "CREATIVE";
                if(GameMode.SURVIVAL == gm) {
                    inventoryName = "SURVIVAL";
                }
                if(MIYamlFiles.config.getBoolean("useSQL")) {
                    MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventoryName);
                    // now let's find an empty slot...
                    boolean noempty = true;
                    MIItemStack[] items = inventory.getInventoryContents();
                    for(int i = 0; i < items.length && noempty; i++) {
                        MIItemStack is = items[i];
                        if(is.getItemStack() == null) {
                            items[i] = itemstack;
                            noempty = false;
                        }
                    }
                    if(noempty) {
                        return false;
                    }
                    MIYamlFiles.con.saveInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventory, inventoryName);
                    return true;
                } else {
                    MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                    MIInventory inventory = config.getInventory(inventoryName);
                    // now let's find an empty slot...
                    boolean noempty = true;
                    MIItemStack[] items = inventory.getInventoryContents();
                    for(int i = 0; i < items.length && noempty; i++) {
                        MIItemStack is = items[i];
                        if(is.getItemStack() == null) {
                            items[i] = itemstack;
                            noempty = false;
                        }
                    }
                    if(noempty) {
                        return false;
                    }
                    config.saveInventory(inventory, inventoryName);
                    return true;
                }
                // If they are currently using the inventory, let's set it...
            } else {
                if(giveplayer.getInventory().firstEmpty() == -1) {
                    return false;
                }
                giveplayer.getInventory().addItem(itemstack.getItemStack());
                if(offlineplayer) {
                    giveplayer.saveData();
                }
                return true;
            }
            // They aren't in the same world, so let's just save the inventory.
        } else {
            String inventoryName = "CREATIVE";
            if(GameMode.SURVIVAL == gm) {
                inventoryName = "SURVIVAL";
            }
            if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
                inventoryName = "SURVIVAL";
            }
            if(MIYamlFiles.config.getBoolean("useSQL")) {
                MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventoryName);
                // now let's find an empty slot...
                boolean noempty = true;
                MIItemStack[] items = inventory.getInventoryContents();
                for(int i = 0; i < items.length && noempty; i++) {
                    MIItemStack is = items[i];
                    if(is.getItemStack() == null) {
                        items[i] = itemstack;
                        noempty = false;
                    }
                }
                if(noempty) {
                    return false;
                }
                MIYamlFiles.con.saveInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventory, inventoryName);
                return true;
            } else {
                MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                MIInventory inventory = config.getInventory(inventoryName);
                // now let's find an empty slot...
                boolean noempty = true;
                MIItemStack[] items = inventory.getInventoryContents();
                for(int i = 0; i < items.length && noempty; i++) {
                    MIItemStack is = items[i];
                    if(is.getItemStack() == null) {
                        items[i] = itemstack;
                        noempty = false;
                    }
                }
                if(noempty) {
                    return false;
                }
                config.saveInventory(inventory, inventoryName);
                return true;
            }
        }
    }
    
    /**
     * Gets a copy of all of a player's vital statistics. Editing stuff in this file does not directly modify the player.
     * 
     * @param player
     *            The player's name.
     * @param world
     *            The world that you want the statistics for.
     * @param gm
     *            What gamemode you want the statistics for.
     * @return A copy of the player's stats.
     */
    public MIAPIPlayer getPlayerInstance(String player, String world, GameMode gm) {
        Player giveplayer = plugin.getServer().getPlayer(player);
        boolean playeronline = true;
        if((giveplayer == null || !giveplayer.isOnline())) {
            // See if the player has data files
            
            // Find the player folder
            File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
            
            // Find player name
            for(File playerfile : playerfolder.listFiles()) {
                String filename = playerfile.getName();
                String playername = filename.substring(0, filename.length() - 4);
                
                if(playername.trim().equalsIgnoreCase(player)) {
                    Player target = TuxTwoPlayer.getOfflinePlayer(playername);
                    if(target != null) {
                        target.loadData();
                        giveplayer = target;
                    }
                }
            }
        }
        if(giveplayer != null) {
            // Let's see if the player is in the same world group.
            if((playeronline && MIPlayerListener.getGroup(giveplayer.getWorld().getName()).equalsIgnoreCase(MIPlayerListener.getGroup(world))) ||
                    (!playeronline && MIYamlFiles.logoutworld.get(giveplayer.getName()).equals(MIPlayerListener.getGroup(world)))) {
                // If they are in the same world, yet are in the wrong game mode, let's get the inventory from the file.
                if(MIYamlFiles.config.getBoolean("separateGamemodeInventories", true) && (giveplayer.getGameMode() != gm)) {
                    String inventoryName = "CREATIVE";
                    if(GameMode.SURVIVAL == gm) {
                        inventoryName = "SURVIVAL";
                    }
                    if(MIYamlFiles.config.getBoolean("useSQL")) {
                        MIAPIPlayer playerfile = new MIAPIPlayer(giveplayer.getName());
                        String group = MIPlayerListener.getGroup(world);
                        String playername = giveplayer.getName();
                        playerfile.setInventory(MIYamlFiles.con.getInventory(playername, group, inventoryName));
                        playerfile.setEnderchest(MIYamlFiles.con.getEnderchestInventory(playername, group, inventoryName));
                        playerfile.setFoodlevel(giveplayer.getFoodLevel());
                        playerfile.setSaturation(giveplayer.getSaturation());
                        playerfile.setHealth(giveplayer.getHealth());
                        playerfile.setXpLevel(giveplayer.getLevel());
                        playerfile.setXp(giveplayer.getExp());
                        playerfile.setGm(gm);
                        return playerfile;
                    } else {
                        MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                        MIAPIPlayer playerfile = new MIAPIPlayer(giveplayer.getName());
                        playerfile.setInventory(config.getInventory(gm.toString()));
                        playerfile.setEnderchest(config.getEnderchestInventory(gm.toString()));
                        playerfile.setFoodlevel(giveplayer.getFoodLevel());
                        playerfile.setSaturation(giveplayer.getSaturation());
                        playerfile.setHealth(giveplayer.getHealth());
                        playerfile.setXpLevel(giveplayer.getLevel());
                        playerfile.setXp(giveplayer.getExp());
                        playerfile.setGm(gm);
                        return playerfile;
                    }
                    // If they are currently using the inventory, let's just grab it...
                } else {
                    MIAPIPlayer playerfile = new MIAPIPlayer(giveplayer.getName());
                    playerfile.setInventory(playerfile.getInventory());
                    playerfile.setEnderchest(playerfile.getEnderchest());
                    playerfile.setFoodlevel(giveplayer.getFoodLevel());
                    playerfile.setSaturation(giveplayer.getSaturation());
                    playerfile.setHealth(giveplayer.getHealth());
                    playerfile.setXpLevel(giveplayer.getLevel());
                    playerfile.setXp(giveplayer.getExp());
                    playerfile.setGm(gm);
                    return playerfile;
                }
                // If we are getting an inventory from another world let's just load it.
            } else {
                String inventoryName = "CREATIVE";
                if(GameMode.SURVIVAL == gm) {
                    inventoryName = "SURVIVAL";
                }
                if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
                    inventoryName = "SURVIVAL";
                }
                if(MIYamlFiles.config.getBoolean("useSQL")) {
                    MIAPIPlayer playerfile = new MIAPIPlayer(giveplayer.getName());
                    String group = MIPlayerListener.getGroup(world);
                    String playername = giveplayer.getName();
                    playerfile.setInventory(MIYamlFiles.con.getInventory(playername, group, inventoryName));
                    playerfile.setEnderchest(MIYamlFiles.con.getEnderchestInventory(playername, group, inventoryName));
                    playerfile.setFoodlevel(MIYamlFiles.con.getHunger(playername, group));
                    playerfile.setSaturation(MIYamlFiles.con.getSaturation(playername, group));
                    playerfile.setHealth(MIYamlFiles.con.getHealth(playername, group));
                    int totalxp = MIYamlFiles.con.getTotalExperience(playername, group);
                    int[] xp = plugin.getXP(totalxp);
                    playerfile.setXpLevel(xp[0]);
                    playerfile.setXp((float) ((float) xp[1] / (float) xp[2]));
                    playerfile.setGm(gm);
                    return playerfile;
                } else {
                    MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                    MIAPIPlayer playerfile = new MIAPIPlayer(giveplayer.getName());
                    playerfile.setInventory(config.getInventory(gm.toString()));
                    playerfile.setEnderchest(config.getEnderchestInventory(gm.toString()));
                    playerfile.setFoodlevel(config.getHunger());
                    playerfile.setSaturation(config.getSaturation());
                    playerfile.setHealth(config.getHealth());
                    playerfile.setXpLevel(config.getLevel());
                    playerfile.setXp(config.getExperience());
                    playerfile.setGm(gm);
                    return playerfile;
                }
            }
        }
        return null;
        
    }
    
    /**
     * Takes a player instance and saves it.
     * 
     * @param player
     *            The Player instance.
     * @param world
     *            The world the instance is for.
     * @return True on success, false on failure.
     */
    public boolean savePlayerInstance(MIAPIPlayer player, String world) {
        Player giveplayer = plugin.getServer().getPlayer(player.getPlayername());
        boolean playeronline = true;
        if((giveplayer == null || !giveplayer.isOnline())) {
            // See if the player has data files
            
            // Find the player folder
            File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
            
            // Find player name
            for(File playerfile : playerfolder.listFiles()) {
                String filename = playerfile.getName();
                String playername = filename.substring(0, filename.length() - 4);
                
                if(playername.trim().equalsIgnoreCase(player.getPlayername())) {
                    Player target = TuxTwoPlayer.getOfflinePlayer(playername);
                    if(target != null) {
                        target.loadData();
                        giveplayer = target;
                    }
                }
            }
        }
        if(giveplayer != null) {
            // Let's see if the player is in the same world group.
            if((playeronline && MIPlayerListener.getGroup(giveplayer.getWorld().getName()).equalsIgnoreCase(MIPlayerListener.getGroup(world))) ||
                    (!playeronline && MIYamlFiles.logoutworld.get(giveplayer.getName()).equals(MIPlayerListener.getGroup(world)))) {
                // If they are in the same world, yet are in the wrong game mode, let's save the player to a file.
                if(MIYamlFiles.config.getBoolean("separateGamemodeInventories", true) && (giveplayer.getGameMode() != player.getGm())) {
                    String inventoryName = player.getGm().toString();
                    if(MIYamlFiles.config.getBoolean("useSQL")) {
                        String group = MIPlayerListener.getGroup(world);
                        String playername = giveplayer.getName();
                        MIYamlFiles.con.saveInventory(playername, group, player.getInventory(), inventoryName);
                        MIYamlFiles.con.saveEnderchestInventory(playername, group, player.getEnderchest(), inventoryName);
                    } else {
                        MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                        config.saveInventory(player.getInventory(), player.getGm().toString());
                        config.saveEnderchestInventory(player.getEnderchest(), player.getGm().toString());
                    }
                    // If they are currently using the inventory, let's just set it...
                } else {
                    player.getInventory().loadIntoInventory(giveplayer.getInventory());
                    player.getEnderchest().loadIntoInventory(giveplayer.getEnderChest());
                }
                giveplayer.setFoodLevel(player.getFoodlevel());
                giveplayer.setSaturation(player.getSaturation());
                giveplayer.setHealth(player.getHealth());
                giveplayer.setLevel(player.getXpLevel());
                giveplayer.setExp((float) player.getXp());
                if(!playeronline) {
                    giveplayer.saveData();
                }
                return true;
                // If we are getting an inventory from another world let's just save it.
            } else {
                String inventoryName = player.getGm().toString();
                if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
                    inventoryName = "SURVIVAL";
                }
                if(MIYamlFiles.config.getBoolean("useSQL")) {
                    String group = MIPlayerListener.getGroup(world);
                    String playername = giveplayer.getName();
                    MIYamlFiles.con.saveInventory(playername, group, player.getInventory(), inventoryName);
                    MIYamlFiles.con.saveEnderchestInventory(playername, group, player.getEnderchest(), inventoryName);
                    MIYamlFiles.con.saveHunger(playername, group, player.getFoodlevel());
                    MIYamlFiles.con.saveSaturation(playername, group, player.getSaturation());
                    MIYamlFiles.con.saveHealth(playername, group, player.getHealth());
                    int xp = plugin.getTotalXP(player.getXpLevel(), player.getXp());
                    MIYamlFiles.con.saveExperience(playername, group, xp);
                    return true;
                } else {
                    MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                    config.saveInventory(player.getInventory(), inventoryName);
                    config.saveEnderchestInventory(player.getEnderchest(), inventoryName);
                    config.saveHunger(player.getFoodlevel());
                    config.saveSaturation(player.getSaturation());
                    config.saveHealth(player.getHealth());
                    config.saveExperience(plugin.getTotalXP(player.getXpLevel(), player.getXp()), player.getXpLevel(), player.getXp());
                    return true;
                }
            }
        }
        return false;
        
    }
    
    /**
     * Returns a hashmap of worlds and what group they are in. Format: ( world, group).
     * 
     * @return All worlds and their assigned groups.
     */
    public HashMap<String,String> getGroups() {
        return MIYamlFiles.getGroups();
    }
    
}
