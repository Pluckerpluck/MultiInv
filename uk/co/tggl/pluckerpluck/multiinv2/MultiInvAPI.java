/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.tggl.pluckerpluck.multiinv2;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * API designed to allow MultiInv to improve compatibility with other plugins
 * @author Pluckerpluck
 */
@SuppressWarnings({"UnusedDeclaration"})
public class MultiInvAPI {
      
    /**
     * Ignores the player by the given name (Name must be exact)
     * Ignored players will not change inventory when teleporting
     * @param playerName The name of the player
     */
    public static void ignorePlayer(String playerName){
        MultiInv.ignoreList.add(playerName.toLowerCase());
    }
    
    /**
     * Un-ignores a previously ignored player
     * Will not un-ignore players ignored by MultiInv unless forced
     * @param playerName The name of the player
     * @param force If true will force an un-ignore of the Player
     */
    public static void unignorePlayer(String playerName, boolean force){
        MultiInv.ignoreList.remove(playerName.toLowerCase());
    }
    
    /**
     * Un-ignores a previously ignored player
     * Will not un-ignore players ignored by MultiInv
     * @param playerName The name of the player
     */
    public static void unignorePlayer(String playerName){
        unignorePlayer(playerName.toLowerCase(), false);
    }
    
    /**
     * Checks whether a player is in the ignore list
     * @param playerName The name of the player
     * @return true if player is ignored
     */
    public static boolean isIgnored(String playerName){
        boolean ignored = false;
        if (MultiInv.ignoreList.contains(playerName.toLowerCase())){
            ignored = true;
        }
        return ignored;
    }
    
    /**
     * Saves the inventory of the chosen player using the given name
     * The current inventory is saved to the group which matches world
     * Note: Unless you have a reason to do otherwise, use saveState
     * @param player Player whose inventory you want to save
     * @param world Name of the world you want to save the inventory to
     * @param inventoryName Name of the inventory (survival/creative are built into MultiInv)
     * @return false if world does not exist
     */
    public static boolean saveManualInventory(Player player, String world, String inventoryName){
        boolean success = false;
        if (player.isOnline() && Bukkit.getServer().getWorld(world) != null){
            MultiInvPlayerData.storeManualInventory(player, inventoryName, world);
            success = true;
        }
        return success;
    }

    /**
     * Saves the inventory (and state) of the player to the chosen world
     * @param player Player whose inventory you want to save
     * @param world Name of the world you want to save the inventory to
     * @return false if world does not exist
     */
    public static boolean saveInventory(Player player, String world){
        boolean success = false;
        if (player.isOnline() && Bukkit.getServer().getWorld(world) != null){
            MultiInvPlayerData.storeCurrentInventory(player, world);
            success = true;
        }
        return success;
    }
    
    /**
     * Loads the inventory from the group containing the chosen world and gives it to the player
     * @param player The player whose inventory you want to set
     * @param world The world's inventory that you wish to load
     * @param state Whether you wish to load extras from the world (health/hunger etc)
     * @return false if world does not exist
     */
    public static boolean loadInventory(Player player, String world, boolean state){
        boolean success = false;
        if (Bukkit.getServer().getWorld(world) != null){
            MultiInvPlayerData.loadWorldInventory(player, world, state);
            success = true;
        }
        return success;
    }

    
}
