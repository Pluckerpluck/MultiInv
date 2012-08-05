package uk.co.tggl.pluckerpluck.multiinv.listener;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

public class MIEnderChest implements Listener {
	
	MultiInv plugin;
	
	public MIEnderChest(MultiInv plugin) {
	    MIPlayerListener.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void blockPlaced(BlockPlaceEvent event) {
		if(event.getBlockPlaced().getTypeId() == 130 && !MIYamlFiles.config.getBoolean("allowEnderChestPlacement", true)) {
		event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "I'm sorry, EnderChest placement disabled.");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void enderchestOpen(InventoryOpenEvent event) {
		if(event.isCancelled()) {
			return;
		}
		MultiInv.log.debug("An inventory was opened, and it was a " + event.getInventory().getType() + " inventory.");
		if(event.getInventory().getType() == InventoryType.ENDER_CHEST) {
			HumanEntity player = event.getPlayer();
			String group = MIPlayerListener.getGroup(player.getWorld());
			MIEnderchestInventory inventorystring = null;
			String inventoryName = "";
			if(player.getGameMode() == GameMode.SURVIVAL) {
				inventoryName = "SURVIVAL";
			}else if(player.getGameMode() == GameMode.CREATIVE) {
				inventoryName = "CREATIVE";
			}else if(player.getGameMode() == GameMode.ADVENTURE) {
				inventoryName = "ADVENTURE";
			}
			if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
	    		inventoryName = "SURVIVAL";
	    	}
			Inventory inventory = event.getInventory();
			if(MIYamlFiles.config.getBoolean("useSQL")) {
				inventorystring = MIYamlFiles.con.getEnderchestInventory(player.getName(), group, inventoryName);
			}else {
				// Find and load configuration file for the player's enderchest
		        File dataFolder =  Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
		        File worldsFolder = new File(dataFolder, "Groups");
		        File file = new File(worldsFolder, group + File.separator + player.getName() + ".ec.yml");
		        String playername = player.getName();
		        YamlConfiguration playerFile = new YamlConfiguration();
		        if (file.exists()){
		            try{
		                playerFile.load(file);
		                String sinventory = playerFile.getString(inventoryName, null);
		                inventorystring = new MIEnderchestInventory(sinventory);
		            }catch (Exception e){
		                e.printStackTrace();
		                //To avoid problems where inventories inadvertently cross on error, wipe chest contents.
		                event.getInventory().clear();
		                return;
		            }
		        }else {
		        	//If a file doesn't exist, let's clear the inventory of the enderchest.
		        	inventory.clear();
		        	return;
		        }
			}
			if(inventorystring == null) {
            	inventory.clear();
            	return;
            }
			ItemStack[] is = new ItemStack[inventorystring.getInventoryContents().length];
			for(int i = 0; i < inventorystring.getInventoryContents().length; i++) {
				is[i] = inventorystring.getInventoryContents()[i].getItemStack();
			}
			inventory.setContents(is);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void enderchestInteraction(InventoryClickEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(event.getInventory().getType() == InventoryType.ENDER_CHEST /*&& event.getRawSlot() < 27*/) {
			HumanEntity player = event.getWhoClicked();
			String group = MIPlayerListener.getGroup(player.getWorld());
			MIEnderchestInventory inventorystring = null;
			String inventoryName = "";
			if(player.getGameMode() == GameMode.SURVIVAL) {
				inventoryName = "SURVIVAL";
			}else if(player.getGameMode() == GameMode.CREATIVE) {
				inventoryName = "CREATIVE";
			}else if(player.getGameMode() == GameMode.ADVENTURE) {
				inventoryName = "ADVENTURE";
			}
			if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
	    		inventoryName = "SURVIVAL";
	    	}
			Inventory inventory = event.getInventory();
			MIEnderchestInventory miinventory = new MIEnderchestInventory(inventory);
			if(MIYamlFiles.config.getBoolean("useSQL")) {
				MIYamlFiles.con.saveEnderchestInventory(player.getName(), group, miinventory, inventoryName);
			}else {
				// Find and load configuration file for the player's enderchest
		        File dataFolder =  Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
		        File worldsFolder = new File(dataFolder, "Groups");
		        File file = new File(worldsFolder, group + File.separator + player.getName() + ".ec.yml");
		        String playername = player.getName();
		        YamlConfiguration playerFile = new YamlConfiguration();
		        if (file.exists()){
		            try{
		                playerFile.load(file);
		            }catch (Exception e){
		                e.printStackTrace();
		                //To avoid problems where inventories inadvertently cross on error, wipe chest contents.
		                event.getInventory().clear();
		                return;
		            }
		        }
		        String inventoryString = inventory.toString();
                playerFile.set(inventoryName, inventoryString);
                String folder = file.getParentFile().getName();
                MultiInv.log.debug("Saving " + playername + "'s " + inventoryName + " Enderchest inventory to " + folder);
                try {
					playerFile.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void enderchestClosed(InventoryCloseEvent event) {
		if(event.getInventory().getType() == InventoryType.ENDER_CHEST) {
			HumanEntity player = event.getPlayer();
			String group = MIPlayerListener.getGroup(player.getWorld());
			MIEnderchestInventory inventorystring = null;
			String inventoryName = "";
			if(player.getGameMode() == GameMode.SURVIVAL) {
				inventoryName = "SURVIVAL";
			}else if(player.getGameMode() == GameMode.CREATIVE) {
				inventoryName = "CREATIVE";
			}else if(player.getGameMode() == GameMode.ADVENTURE) {
				inventoryName = "ADVENTURE";
			}
			if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
	    		inventoryName = "SURVIVAL";
	    	}
			Inventory inventory = event.getInventory();
			MIEnderchestInventory miinventory = new MIEnderchestInventory(inventory);
			if(MIYamlFiles.config.getBoolean("useSQL")) {
				MIYamlFiles.con.saveEnderchestInventory(player.getName(), group, miinventory, inventoryName);
			}else {
				// Find and load configuration file for the player's enderchest
		        File dataFolder =  Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
		        File worldsFolder = new File(dataFolder, "Groups");
		        File file = new File(worldsFolder, group + File.separator + player.getName() + ".ec.yml");
		        String playername = player.getName();
		        YamlConfiguration playerFile = new YamlConfiguration();
		        if (file.exists()){
		            try{
		                playerFile.load(file);
		            }catch (Exception e){
		                e.printStackTrace();
		                //To avoid problems where inventories inadvertently cross on error, wipe chest contents.
		                event.getInventory().clear();
		                return;
		            }
		        }
		        String inventoryString = inventory.toString();
                playerFile.set(inventoryName, inventoryString);
                String folder = file.getParentFile().getName();
                MultiInv.log.debug("Saving " + playername + "'s " + inventoryName + " Enderchest inventory to " + folder);
                try {
					playerFile.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
