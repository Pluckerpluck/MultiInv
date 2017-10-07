package uk.co.tggl.pluckerpluck.multiinv;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import Tux2.TuxTwoLib.TuxTwoPlayer;
import uk.co.tggl.pluckerpluck.multiinv.api.MIAPIPlayer;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
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
    public MIInventory getPlayerInventory(OfflinePlayer player, String world, GameMode gm) {
        Player giveplayer = null;
    	if(player instanceof Player) {
    		giveplayer = (Player) player;
    	}else {
    		giveplayer = plugin.getServer().getPlayer(player.getName());
    	}
        boolean playeronline = true;
        if((giveplayer == null || !giveplayer.isOnline())) {
            Player target = TuxTwoPlayer.getOfflinePlayer(player);
            if(target != null) {
                target.loadData();
                giveplayer = target;
            }
        }
        if(giveplayer != null) {
            // Let's see if the player is in the same world group.
            if((playeronline && MIPlayerListener.getGroup(giveplayer.getWorld().getName()).equalsIgnoreCase(MIPlayerListener.getGroup(world))) ||
                    (!playeronline && MIYamlFiles.logoutworld.get(giveplayer.getUniqueId()).equals(MIPlayerListener.getGroup(world)))) {
                // If they are in the same world, yet are in the wrong game mode, let's get the inventory from the file.
                if(MIYamlFiles.config.getBoolean("separateGamemodeInventories", true) && (giveplayer.getGameMode() != gm)) {
                    String inventoryName = "CREATIVE";
                    if(GameMode.SURVIVAL == gm) {
                        inventoryName = "SURVIVAL";
                    }
                    if(MIYamlFiles.config.getBoolean("useSQL")) {
                        MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer, MIPlayerListener.getGroup(world), inventoryName);
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
                    MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer, MIPlayerListener.getGroup(world), inventoryName);
                    return inventory;
                } else {
                    MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
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
    public boolean setPlayerInventory(OfflinePlayer player, String world, GameMode gm, MIInventory inventory) {
        Player giveplayer;
    	if(player instanceof Player) {
    		giveplayer = (Player) player;
    	}else {
    		giveplayer = plugin.getServer().getPlayer(player.getName());
    	}
        String currentworld = "";
        boolean offlineplayer = false;
        if(giveplayer != null && giveplayer.isOnline()) {
            currentworld = giveplayer.getWorld().getName();
        } else {
            giveplayer = TuxTwoPlayer.getOfflinePlayer(player);
            if(giveplayer == null) {
                return false;
            }
            currentworld = MIYamlFiles.logoutworld.get(giveplayer.getUniqueId());
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
                    MIYamlFiles.con.saveInventory(giveplayer, MIPlayerListener.getGroup(world), inventory, inventoryName);
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
                MIYamlFiles.con.saveInventory(giveplayer, MIPlayerListener.getGroup(world), inventory, inventoryName);
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
    public boolean addItemToInventory(OfflinePlayer player, String world, GameMode gm, MIItemStack itemstack) {
        Player giveplayer;
    	if(player instanceof Player) {
    		giveplayer = (Player) player;
    	}else {
    		giveplayer = plugin.getServer().getPlayer(player.getName());
    	}
        String currentworld = "";
        boolean offlineplayer = false;
        if(giveplayer != null && giveplayer.isOnline()) {
            currentworld = giveplayer.getWorld().getName();
        } else {
            giveplayer = TuxTwoPlayer.getOfflinePlayer(player);
            if(giveplayer == null) {
                return false;
            }
            currentworld = MIYamlFiles.logoutworld.get(giveplayer.getUniqueId());
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
                    MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer, MIPlayerListener.getGroup(world), inventoryName);
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
                    MIYamlFiles.con.saveInventory(giveplayer, MIPlayerListener.getGroup(world), inventory, inventoryName);
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
                MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer, MIPlayerListener.getGroup(world), inventoryName);
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
                MIYamlFiles.con.saveInventory(giveplayer, MIPlayerListener.getGroup(world), inventory, inventoryName);
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
    public MIAPIPlayer getPlayerInstance(OfflinePlayer player, String world, GameMode gm) {
        Player giveplayer;
    	if(player instanceof Player) {
    		giveplayer = (Player) player;
    	}else {
    		giveplayer = plugin.getServer().getPlayer(player.getName());
    	}
        boolean playeronline = true;
        if((giveplayer == null || !giveplayer.isOnline())) {
            Player target = TuxTwoPlayer.getOfflinePlayer(player);
            if(target != null) {
                target.loadData();
                giveplayer = target;
            }
        }
        if(giveplayer != null) {
            // Let's see if the player is in the same world group.
            if((playeronline && MIPlayerListener.getGroup(giveplayer.getWorld().getName()).equalsIgnoreCase(MIPlayerListener.getGroup(world))) ||
                    (!playeronline && MIYamlFiles.logoutworld.get(giveplayer.getUniqueId()).equals(MIPlayerListener.getGroup(world)))) {
                // If they are in the same world, yet are in the wrong game mode, let's get the inventory from the file.
                if(MIYamlFiles.config.getBoolean("separateGamemodeInventories", true) && (giveplayer.getGameMode() != gm)) {
                    String inventoryName = "CREATIVE";
                    if(GameMode.SURVIVAL == gm) {
                        inventoryName = "SURVIVAL";
                    }
                    if(MIYamlFiles.config.getBoolean("useSQL")) {
                        MIAPIPlayer playerfile = new MIAPIPlayer(giveplayer);
                        String group = MIPlayerListener.getGroup(world);
                        playerfile.setInventory(MIYamlFiles.con.getInventory(giveplayer, group, inventoryName));
                        playerfile.setEnderchest(MIYamlFiles.con.getEnderchestInventory(giveplayer, group, inventoryName));
                        playerfile.setFoodlevel(giveplayer.getFoodLevel());
                        playerfile.setSaturation(giveplayer.getSaturation());
                        playerfile.setHealth(giveplayer.getHealth());
                        playerfile.setXpLevel(giveplayer.getLevel());
                        playerfile.setXp(giveplayer.getExp());
                        playerfile.setGm(gm);
                        return playerfile;
                    } else {
                        MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                        MIAPIPlayer playerfile = new MIAPIPlayer(giveplayer);
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
                    MIAPIPlayer playerfile = new MIAPIPlayer(giveplayer);
                    playerfile.setInventory(new MIInventory(giveplayer));
                    playerfile.setEnderchest(new MIEnderchestInventory(giveplayer));
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
                    MIAPIPlayer playerfile = new MIAPIPlayer(giveplayer);
                    String group = MIPlayerListener.getGroup(world);
                    playerfile.setInventory(MIYamlFiles.con.getInventory(giveplayer, group, inventoryName));
                    playerfile.setEnderchest(MIYamlFiles.con.getEnderchestInventory(giveplayer, group, inventoryName));
                    playerfile.setFoodlevel(MIYamlFiles.con.getHunger(giveplayer, group));
                    playerfile.setSaturation(MIYamlFiles.con.getSaturation(giveplayer, group));
                    playerfile.setHealth(MIYamlFiles.con.getHealth(giveplayer, group));
                    int totalxp = MIYamlFiles.con.getTotalExperience(giveplayer, group);
                    int[] xp = plugin.getXP(totalxp);
                    playerfile.setXpLevel(xp[0]);
                    playerfile.setXp((float) ((float) xp[1] / (float) xp[2]));
                    playerfile.setGm(gm);
                    return playerfile;
                } else {
                    MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                    MIAPIPlayer playerfile = new MIAPIPlayer(giveplayer);
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
        	OfflinePlayer oplayer = Bukkit.getOfflinePlayer(player.getPlayername());
            Player target = TuxTwoPlayer.getOfflinePlayer(oplayer);
            if(target != null) {
                target.loadData();
                giveplayer = target;
            }
        }
        if(giveplayer != null) {
            // Let's see if the player is in the same world group.
            if((playeronline && MIPlayerListener.getGroup(giveplayer.getWorld().getName()).equalsIgnoreCase(MIPlayerListener.getGroup(world))) ||
                    (!playeronline && MIYamlFiles.logoutworld.get(giveplayer.getUniqueId()).equals(MIPlayerListener.getGroup(world)))) {
                // If they are in the same world, yet are in the wrong game mode, let's save the player to a file.
                if(MIYamlFiles.config.getBoolean("separateGamemodeInventories", true) && (giveplayer.getGameMode() != player.getGm())) {
                    String inventoryName = player.getGm().toString();
                    if(MIYamlFiles.config.getBoolean("useSQL")) {
                        String group = MIPlayerListener.getGroup(world);
                        MIYamlFiles.con.saveInventory(giveplayer, group, player.getInventory(), inventoryName);
                        MIYamlFiles.con.saveEnderchestInventory(giveplayer, group, player.getEnderchest(), inventoryName);
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
                    int xp = plugin.getTotalXP(player.getXpLevel(), player.getXp());
                    MIYamlFiles.con.saveAll(giveplayer, group, player.getInventory(), inventoryName, xp, player.getGm(), 
                    		player.getHealth(), player.getFoodlevel(), player.getSaturation());
                    //MIYamlFiles.con.saveInventory(giveplayer, group, player.getInventory(), inventoryName);
                    MIYamlFiles.con.saveEnderchestInventory(giveplayer, group, player.getEnderchest(), inventoryName);
                    //MIYamlFiles.con.saveHunger(giveplayer, group, player.getFoodlevel());
                    //MIYamlFiles.con.saveSaturation(giveplayer, group, player.getSaturation());
                    //MIYamlFiles.con.saveHealth(giveplayer, group, player.getHealth());
                    //MIYamlFiles.con.saveExperience(giveplayer, group, xp);
                    return true;
                } else {
                    MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
                    //config.saveInventory(player.getInventory(), inventoryName);
                    config.saveAll(player.getInventory(), inventoryName, plugin.getTotalXP(player.getXpLevel(), player.getXp()), player.getXpLevel(), player.getXp(),
                    		player.getGm(), player.getHealth(), player.getFoodlevel(), player.getSaturation());
                    config.saveEnderchestInventory(player.getEnderchest(), inventoryName);
                    //config.saveHunger(player.getFoodlevel());
                    //config.saveSaturation(player.getSaturation());
                    //config.saveHealth(player.getHealth());
                    //config.saveExperience(plugin.getTotalXP(player.getXpLevel(), player.getXp()), player.getXpLevel(), player.getXp());
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
    
    /**
     * Forces the plugin to do an immediate save of inventory for a player.
     * @param player the player to force a save on.
     */
    public void forcePlayerSave(Player player) {
		String groupFrom = MIPlayerListener.getGroup(player.getWorld());
		plugin.playerListener.saveEnderchestState(player, groupFrom);
		plugin.playerListener.savePlayerState(player, groupFrom);
    	
    }
}
