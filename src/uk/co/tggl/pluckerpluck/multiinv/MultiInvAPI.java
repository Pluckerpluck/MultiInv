package uk.co.tggl.pluckerpluck.multiinv;

import java.io.File;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;
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
								plugin.log.warning(playername + " not found!");
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
				plugin.log.warning("Error while retrieving offline player data for player " + player + "!");
				return null;
			}
		}
		return null;
		
	}

}
