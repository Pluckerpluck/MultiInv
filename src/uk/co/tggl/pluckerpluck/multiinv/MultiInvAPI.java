package uk.co.tggl.pluckerpluck.multiinv;

import java.io.File;
import java.util.HashMap;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;

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
	 * @param player The player you want to look up.
	 * @param world The world's name (not group name)
	 * @param gm The game mode of the inventory you want to retrieve.
	 * @return
	 */
	public MIInventory getPlayerInventory(String player, String world, GameMode gm) {
		Player giveplayer = plugin.getServer().getPlayer(player);
		//If the player is online it's very easy.
		if(giveplayer != null && giveplayer.isOnline()) {
			//Let's see if the player is in the same world group.
			if(MIPlayerListener.getGroup(giveplayer.getWorld().getName()).equalsIgnoreCase(MIPlayerListener.getGroup(world))) {
				//If they are in the same world, yet are in the wrong game mode, let's get the inventory from the file.
				if(MIYamlFiles.config.getBoolean("separateGamemodeInventories", true) && (giveplayer.getGameMode() != gm)) {
					String inventoryName = "CREATIVE";
					if(GameMode.SURVIVAL == gm) {
			    		inventoryName = "SURVIVAL";
			    	}
			        if (MIYamlFiles.config.getBoolean("useSQL")){
			        	MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventoryName);
			        	return inventory;
			        }else{
			            MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
			            MIInventory inventory = config.getInventory(inventoryName);
			            return inventory;
			        }
			        //If they are currently using the inventory, let's just grab it...
				}else {
					return new MIInventory(giveplayer.getInventory());
				}
				//If we are getting an inventory from another world let's just load it.
			}else {
				String inventoryName = "CREATIVE";
				if(GameMode.SURVIVAL == gm) {
		    		inventoryName = "SURVIVAL";
		    	}
				if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
		    		inventoryName = "SURVIVAL";
		    	}
		        if (MIYamlFiles.config.getBoolean("useSQL")){
		        	MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventoryName);
		        	return inventory;
		        }else{
		            MIPlayerFile config = new MIPlayerFile(player, MIPlayerListener.getGroup(world));
		            MIInventory inventory = config.getInventory(inventoryName);
		            return inventory;
		        }
			}
			//The player isn't online. Let's do it in offline mode then...
		}else {
			//Offline inv here...
			try {
				//See if the player has data files

				// Find the player folder
				File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");

				// Find player name
				for (File playerfile : playerfolder.listFiles()) {
					String filename = playerfile.getName();
					String playername = filename.substring(0, filename.length() - 4);

					if(playername.trim().equalsIgnoreCase(player)) {
						//This player plays on the server!
						
						//If the player logged out in the world group this world is in we need the
						//actual player file.
						if(MIYamlFiles.logoutworld.get(playername).equals(MIPlayerListener.getGroup(world))) {
							//Create an entity to load the player data
							MinecraftServer server = ((CraftServer)this.plugin.getServer()).getServer();
							EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), playername, new ItemInWorldManager(server.getWorldServer(0)));
							Player target = (entity == null) ? null : (Player) entity.getBukkitEntity();
							if(target != null) {
								target.loadData();
								//Now let's check the gamemode!
								if(MIYamlFiles.config.getBoolean("separateGamemodeInventories", true) && (target.getGameMode() != gm)) {
									String inventoryName = "CREATIVE";
									if(GameMode.SURVIVAL == gm) {
							    		inventoryName = "SURVIVAL";
							    	}
							        if (MIYamlFiles.config.getBoolean("useSQL")){
							        	MIInventory inventory = MIYamlFiles.con.getInventory(target.getName(), MIPlayerListener.getGroup(world), inventoryName);
							        	return inventory;
							        }else{
							            MIPlayerFile config = new MIPlayerFile(target, MIPlayerListener.getGroup(world));
							            MIInventory inventory = config.getInventory(inventoryName);
							            return inventory;
							        }
							        //If they are currently using the inventory, let's just grab it...
								}else {
									return new MIInventory(target.getInventory());
								}
							}else {
								MultiInv.log.warning(playername + " not found!");
								return null;
							}
							//They aren't in the same world group, so let's just grab it from the file.
						}else {
							String inventoryName = "CREATIVE";
							if(GameMode.SURVIVAL == gm) {
					    		inventoryName = "SURVIVAL";
					    	}
							if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
					    		inventoryName = "SURVIVAL";
					    	}
					        if (MIYamlFiles.config.getBoolean("useSQL")){
					        	MIInventory inventory = MIYamlFiles.con.getInventory(playername, MIPlayerListener.getGroup(world), inventoryName);
					        	return inventory;
					        }else{
					            MIPlayerFile config = new MIPlayerFile(playername, MIPlayerListener.getGroup(world));
					            MIInventory inventory = config.getInventory(inventoryName);
					            return inventory;
					        }
						}
					}
				}
			}
			catch(Exception e) {
				MultiInv.log.warning("Error while retrieving offline player data for player " + player + "!");
				return null;
			}
		}
		return null;
		
	}
	
	/**
	 * Sets the player's inventory using a MIInventory
	 * @param player The player's name
	 * @param world The world name that you want to set the inventory in
	 * @param gm Gamemode
	 * @param inventory The inventory
	 * @return True upon success, false upon error.
	 */
	public boolean setPlayerInventory(String player, String world, GameMode gm, MIInventory inventory) {
		Player giveplayer = plugin.getServer().getPlayer(player);
		String currentworld = "";
		boolean offlineplayer = false;
		if(giveplayer != null && giveplayer.isOnline()) {
			currentworld = giveplayer.getWorld().getName();
		}else {
			giveplayer = getOfflinePlayer(player);
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
		        if (MIYamlFiles.config.getBoolean("useSQL")){
		        	MIYamlFiles.con.saveInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventory, inventoryName);
		        	return true;
		        }else{
		            MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
		            config.saveInventory(inventory, inventoryName);
		            return true;
		        }
		        //If they are currently using the inventory, let's set it...
			}else {
				inventory.loadIntoInventory(giveplayer.getInventory());
				if(offlineplayer) {
					giveplayer.saveData();
				}
				return true;
			}
			//They aren't in the same world, so let's just save the inventory.
		}else {
			String inventoryName = "CREATIVE";
			if(GameMode.SURVIVAL == gm) {
	    		inventoryName = "SURVIVAL";
	    	}
			if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
	    		inventoryName = "SURVIVAL";
	    	}
	        if (MIYamlFiles.config.getBoolean("useSQL")){
	        	MIYamlFiles.con.saveInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventory, inventoryName);
	        	return true;
	        }else{
	        	MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
	            config.saveInventory(inventory, inventoryName);
	            return true;
	        }
		}
	}
	
	/**
	 * Adds a single item to the player's inventory.
	 * @param player The player's name
	 * @param world The world to add it to.
	 * @param gm The game mode
	 * @param itemstack The item to add.
	 * @return true upon successful adding, false if the inventory was full or player not found.
	 */
	public boolean addItemToInventory(String player, String world, GameMode gm, MIItemStack itemstack) {
		Player giveplayer = plugin.getServer().getPlayer(player);
		String currentworld = "";
		boolean offlineplayer = false;
		if(giveplayer != null && giveplayer.isOnline()) {
			currentworld = giveplayer.getWorld().getName();
		}else {
			giveplayer = getOfflinePlayer(player);
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
		        if (MIYamlFiles.config.getBoolean("useSQL")){
		        	MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventoryName);
		        	//now let's find an empty slot...
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
		        }else{
		            MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
		            MIInventory inventory = config.getInventory(inventoryName);
		            //now let's find an empty slot...
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
		        //If they are currently using the inventory, let's set it...
			}else {
				if(giveplayer.getInventory().firstEmpty() == -1) {
					return false;
				}
				giveplayer.getInventory().addItem(itemstack.getItemStack());
				if(offlineplayer) {
					giveplayer.saveData();
				}
				return true;
			}
			//They aren't in the same world, so let's just save the inventory.
		}else {
			String inventoryName = "CREATIVE";
			if(GameMode.SURVIVAL == gm) {
	    		inventoryName = "SURVIVAL";
	    	}
			if(!MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)) {
	    		inventoryName = "SURVIVAL";
	    	}
	        if (MIYamlFiles.config.getBoolean("useSQL")){
	        	MIInventory inventory = MIYamlFiles.con.getInventory(giveplayer.getName(), MIPlayerListener.getGroup(world), inventoryName);
	        	//now let's find an empty slot...
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
	        }else{
	        	MIPlayerFile config = new MIPlayerFile(giveplayer, MIPlayerListener.getGroup(world));
	            MIInventory inventory = config.getInventory(inventoryName);
	            //now let's find an empty slot...
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
	 * Returns a hashmap of worlds and what group they are in. Format: ( world, group).
	 * @return All worlds and their assigned groups.
	 */
	public HashMap<String, String> getGroups() {
		return MIYamlFiles.getGroups();
	}
	
	private Player getOfflinePlayer(String player) {
		Player pplayer = null;
		try {
			//See if the player has data files

			// Find the player folder
			File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");

			// Find player name
			for (File playerfile : playerfolder.listFiles()) {
				String filename = playerfile.getName();
				String playername = filename.substring(0, filename.length() - 4);

				if(playername.trim().equalsIgnoreCase(player)) {
					//This player plays on the server!
					MinecraftServer server = ((CraftServer)this.plugin.getServer()).getServer();
					EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), playername, new ItemInWorldManager(server.getWorldServer(0)));
					Player target = (entity == null) ? null : (Player) entity.getBukkitEntity();
					if(target != null) {
						target.loadData();
						return target;
					}
				}
			}
		}
		catch(Exception e) {
			MultiInv.log.warning("Error while retrieving offline player data for player " + player + "!");
			return null;
		}
		return pplayer;
	}

}
