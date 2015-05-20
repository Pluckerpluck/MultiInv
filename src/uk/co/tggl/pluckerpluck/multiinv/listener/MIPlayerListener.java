package uk.co.tggl.pluckerpluck.multiinv.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.PlayerLogoutRemover;
import uk.co.tggl.pluckerpluck.multiinv.api.ChangeInventoryEvent;
import uk.co.tggl.pluckerpluck.multiinv.player.DeferredWorldCheck;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayer;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayerGiveCache;
import uk.co.tggl.pluckerpluck.multiinv.workarounds.SetXP;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA. User: Pluckerpluck Date: 18/12/11 Time: 23:32 To change this template use File | Settings | File Templates.
 */
public class MIPlayerListener implements Listener {

	private static ConcurrentHashMap<UUID,MIPlayer> players = new ConcurrentHashMap<UUID,MIPlayer>();
	ConcurrentHashMap<UUID,Boolean> playerchangeworlds = new ConcurrentHashMap<UUID,Boolean>();
	static public MultiInv plugin;

	static ConcurrentHashMap<UUID, BukkitTask> playerremoval = new ConcurrentHashMap<UUID, BukkitTask>();
	static ConcurrentHashMap<UUID, MIPlayerGiveCache> playerrestrict = new ConcurrentHashMap<UUID, MIPlayerGiveCache>();
	public ConcurrentHashMap<UUID, MIPlayerGiveCache> playerworldrestrict = new ConcurrentHashMap<UUID, MIPlayerGiveCache>();
	
	private ConcurrentHashMap<UUID, Boolean> additems = new ConcurrentHashMap<UUID, Boolean>();


	public MIPlayerListener(MultiInv plugin) {
		MIPlayerListener.plugin = plugin;
		reloadPlayersMap();
	}
	
	public void addingItemsToPlayer(UUID uuid) {
		additems.put(uuid, true);
	}
	
	public void stoppedAddingItemsToPlayer(UUID uuid) {
		additems.remove(uuid);
	}
	
	public boolean isAddingItemsToPlayer(UUID uuid) {
		Boolean result = additems.get(uuid);
		return (result != null && result.booleanValue());
	}

	public static MIPlayer getMIPlayer(Player player) {
		MIPlayer mplayer = players.get(player.getUniqueId());
		if(mplayer == null) {
			mplayer = addMIPlayer(player, 0);
		}
		return mplayer;
	}
	
	public static MIPlayer addMIPlayer(Player player, int cachedelay) {
		MIPlayer mplayer = new MIPlayer(player, plugin, cachedelay);
		players.put(player.getUniqueId(), mplayer);
		return mplayer;
	}
	
	public static void reloadPlayersMap() {
		players.clear();
		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
			addMIPlayer(player, 0);
		}
	}

	public static void removePlayer(UUID playername) {
		players.remove(playername);
		playerremoval.remove(playername);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onWorldLoaded(WorldLoadEvent event) {
		World world = event.getWorld();
		plugin.addWorld(world);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerPrelogin(AsyncPlayerPreLoginEvent event) {
		//Only handle the event if something else isn't already handling it.
		if(event.getLoginResult() != Result.ALLOWED) {
			return;
		}
		if(plugin.isImporting()) {
			event.setLoginResult(Result.KICK_OTHER);
			event.setKickMessage("This server is undergoing maintenance please check back later.");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();

		if(player.hasMetadata("NPC")) {
			//It's an NPC, we can safely exit...
			return;
		}
		
		if(playerremoval.containsKey(player.getUniqueId())) {
			BukkitTask task = playerremoval.get(player.getUniqueId());
			task.cancel();
		}
		if(MIYamlFiles.saveonquit) {
			addMIPlayer(player, 38);
		}else if(!players.containsKey(player.getUniqueId())) {
			addMIPlayer(player, 0);
		}else {
			setCachedPlayer(player);
		}
		if(!player.hasPermission("multiinv.exempt") || !player.hasPermission("multiinv.enderchestexempt")) {
			// Let's set a task to run once they get switched to the proper world by bukkit.
			
			if(MIYamlFiles.saveonquit) {
				//We need to do a few things to stop exploits.
				playerrestrict.put(player.getUniqueId(), new MIPlayerGiveCache(System.currentTimeMillis() + (50*40), player.getUniqueId()));
				//Usually we want to wait 2 seconds, which players won't notice as they will still be logging in anyways.
				//This is also to give Bungeecord servers time to switch the player before polling the inventory.
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DeferredWorldCheck(player, this), 40);
			}else {
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DeferredWorldCheck(player, this), 1);
			}
		}
		if(player.hasPermission("multiinv.exempt") && player.hasPermission("multiinv.enderchestexempt")) {
			player.sendMessage(ChatColor.GOLD + "[MultiInv] You have the multiinv.exempt and multiinv.enderchestexempt permission nodes.");
			player.sendMessage(ChatColor.GOLD + "Your inventory and enderchest contents will not change between worlds.");
		}else if(player.hasPermission("multiinv.exempt")) {
			player.sendMessage(ChatColor.GOLD + "[MultiInv] You have the multiinv.exempt permission node.");
			player.sendMessage(ChatColor.GOLD + "Your inventory contents will not change between worlds.");
		}else if(player.hasPermission("multiinv.enderchestexempt")) {
			player.sendMessage(ChatColor.GOLD + "[MultiInv] You have the multiinv.enderchestexempt permission node.");
			player.sendMessage(ChatColor.GOLD + "Your enderchest contents will not change between worlds.");
		}
	}
	
	public MIPlayerGiveCache getPlayerGiveCache(UUID uuid) {
		MIPlayerGiveCache cache = playerrestrict.get(uuid);
		if(cache == null) {
			cache = playerworldrestrict.get(uuid);
		}
		return cache;
	}
	
	public MIPlayerGiveCache removePlayerGiveCache(UUID uuid) {
		return playerrestrict.remove(uuid);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		MIPlayerGiveCache time = playerrestrict.get(event.getPlayer().getUniqueId());
		if(time != null) {
			if(time.getTime() > System.currentTimeMillis()) {
				event.setCancelled(true);
			}else {
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		MIPlayerGiveCache time = playerrestrict.get(event.getPlayer().getUniqueId());
		if(time != null) {
			if(time.getTime() > System.currentTimeMillis()) {
				event.setCancelled(true);
			}else {
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInventoryClick(InventoryClickEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			MIPlayerGiveCache time = playerrestrict.get(player.getUniqueId());
			if(time != null) {
				if(time.getTime() > System.currentTimeMillis()) {
					event.setCancelled(true);
				}else {
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInventoryDrag(InventoryDragEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			MIPlayerGiveCache time = playerrestrict.get(player.getUniqueId());
			if(time != null) {
				if(time.getTime() > System.currentTimeMillis()) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInventoryMoveItem(InventoryMoveItemEvent event) {
		InventoryHolder holder = event.getInitiator().getHolder();
		if(holder != null && holder instanceof Player) {
			Player player = (Player) holder;
			MIPlayerGiveCache time = playerrestrict.get(player.getUniqueId());
			if(time != null) {
				if(time.getTime() > System.currentTimeMillis()) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	//Let's make sure they can't place blocks or interact with anything while they are locked.
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerIteract(PlayerInteractEvent event) {
		Player player = (Player) event.getPlayer();
		MIPlayerGiveCache time = playerrestrict.get(player.getUniqueId());
		if(time != null) {
			if(time.getTime() > System.currentTimeMillis()) {
				if(event.getItem() != null && event.getItem().getType() != Material.AIR) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event) {
		if(event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();

		if(player.hasMetadata("NPC")) {
			//It's an NPC, we can safely exit...
			return;
		}
		
		String currentworld = getGroup(player.getLocation().getWorld());
		if(MIYamlFiles.saveonquit) {
        	if(!player.hasPermission("multiinv.enderchestexempt")) {
                // Load the enderchest inventory for this world from file.
                saveEnderchestState(player, currentworld);
            }
            if(!player.hasPermission("multiinv.exempt")) {
                // Load the inventory for this world from file.
                savePlayerState(player, currentworld);
            }
            //Remove the player from the list immediately if saveonquit is true.
            //Prevents a bug where stuff is saved to file within the 60 seconds time out.
			MIPlayerListener.removePlayer(player.getUniqueId());
        }else {
    		BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, new PlayerLogoutRemover(event.getPlayer().getUniqueId()), 20*60);
    		playerremoval.put(event.getPlayer().getUniqueId(), task);
			removeCachedPlayer(player);
        }
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if(player.hasMetadata("NPC")) {
			//It's an NPC, we can safely exit...
			return;
		}
		
		String currentworld = getGroup(player.getLocation().getWorld());
		if(MIYamlFiles.saveonquit) {
        	if(!player.hasPermission("multiinv.enderchestexempt")) {
                // Load the enderchest inventory for this world from file.
                saveEnderchestState(player, currentworld);
            }
            if(!player.hasPermission("multiinv.exempt")) {
                // Load the inventory for this world from file.
                savePlayerState(player, currentworld);
            }
            //Remove the player from the list immediately if saveonquit is true.
            //Prevents a bug where stuff is saved to file within the 60 seconds time out.
			MIPlayerListener.removePlayer(player.getUniqueId());
        }else {
    		BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, new PlayerLogoutRemover(event.getPlayer().getUniqueId()), 20*60);
    		playerremoval.put(event.getPlayer().getUniqueId(), task);
			removeCachedPlayer(player);
        }
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		// No need to run this twice!
		if(MIYamlFiles.compatibilitymode) {
			return;
		}
		// Get player objects
		Player player = event.getPlayer();

		if(player.hasMetadata("NPC")) {
			//It's an NPC, we can safely exit...
			return;
		}
		// Get world objects
		World worldTo = player.getWorld();
		World worldFrom = event.getFrom();

		// Get corresponding groups
		String groupTo = getGroup(worldTo);
		String groupFrom = getGroup(worldFrom);

		MultiInv.log.debug(player.getName() + " moved from " + groupFrom + " to " + groupTo);

		if(!groupTo.equals(groupFrom)) {
			// Let's put this player in the pool of players that switched worlds, that way we don't dupe the inventory.
			playerchangeworlds.put(player.getUniqueId(), new Boolean(true));
			// Let's schedule it so that we take the player out soon afterwards.
			player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemovePlayer(player.getUniqueId(), playerchangeworlds), 2);

			MIPlayerGiveCache cache = getPlayerGiveCache(player.getUniqueId());
			
			if(!player.hasPermission("multiinv.enderchestexempt")) {
				if(cache == null) {
					saveEnderchestState(player, groupFrom);
				}
				loadEnderchestState(player, groupTo);
			}
			if(!player.hasPermission("multiinv.exempt")) {
				if(cache == null) {
					savePlayerState(player, groupFrom);
				}
				loadPlayerState(player, groupTo);
			}
			//Let's add them to the restricted part to bypass a couple of exploits
			playerworldrestrict.put(player.getUniqueId(), new MIPlayerGiveCache(System.currentTimeMillis() + (50*15), player.getUniqueId()));
			// Save the player's current world
			MIYamlFiles.savePlayerLogoutWorld(player.getUniqueId(), groupTo);
			ChangeInventoryEvent eventcall = new ChangeInventoryEvent(worldTo,worldFrom,player);
			Bukkit.getServer().getPluginManager().callEvent(eventcall);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.isCancelled()) {
			return;
		}

		if(event.getPlayer().hasMetadata("NPC")) {
			//It's an NPC, we can safely exit...
			return;
		}
		
		if(playerchangeworlds.containsKey(event.getPlayer().getUniqueId())) {
			if(!event.getFrom().getWorld().getName().equals(event.getTo().getWorld().getName())) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.DARK_RED + "You're teleporting too fast, slow down!");
				return;
			}
		}
		// Only do this if they have problem plugins.
		if(MIYamlFiles.compatibilitymode) {
			if(event.getFrom().getWorld() != event.getTo().getWorld()) {
				// Get player objects
				Player player = event.getPlayer();

				// Get world objects
				World worldTo = event.getTo().getWorld();
				World worldFrom = event.getFrom().getWorld();

				// Get corresponding groups
				String groupTo = getGroup(worldTo);
				String groupFrom = getGroup(worldFrom);

				MultiInv.log.debug(player.getName() + " moved from " + groupFrom + " to " + groupTo);

				if(!groupTo.equals(groupFrom)) {
					// Let's put this player in the pool of players that switched worlds, that way we don't dupe the inventory.
					playerchangeworlds.put(player.getUniqueId(), new Boolean(true));
					// Let's schedule it so that we take the player out soon afterwards.
					player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemovePlayer(player.getUniqueId(), playerchangeworlds), 2);

					MIPlayerGiveCache cache = getPlayerGiveCache(player.getUniqueId());
					
					if(!player.hasPermission("multiinv.enderchestexempt")) {
						if(cache == null) {
							saveEnderchestState(player, groupFrom);
						}
						loadEnderchestState(player, groupTo);
					}
					if(!player.hasPermission("multiinv.exempt")) {
						if(cache == null) {
							savePlayerState(player, groupFrom);
						}
						loadPlayerState(player, groupTo);
					}
					//Let's add them to the restricted part to bypass a couple of exploits
					playerworldrestrict.put(player.getUniqueId(), new MIPlayerGiveCache(System.currentTimeMillis() + (50*15), player.getUniqueId()));
					// Save the player's current world
					MIYamlFiles.savePlayerLogoutWorld(player.getUniqueId(), groupTo);
					ChangeInventoryEvent eventcall = new ChangeInventoryEvent(worldTo,worldFrom,player);
					Bukkit.getServer().getPluginManager().callEvent(eventcall);

				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerRespawnEvent event) {
		if(!MIYamlFiles.compatibilitymode) {
			return;
		}
		Location respawn = event.getRespawnLocation();
		Player player = event.getPlayer();
		if(player.hasMetadata("NPC")) {
			//It's an NPC, we can safely exit...
			return;
		}
		if(respawn.getWorld() != player.getWorld()) {
			String groupTo = getGroup(respawn.getWorld());
			String groupFrom = getGroup(player.getWorld());
			MIPlayer miPlayer = getMIPlayer(player);
			miPlayer.saveInventory(groupFrom, player.getGameMode().toString());
			miPlayer.saveFakeHealth(groupFrom, 20);
			miPlayer.saveFakeHunger(groupFrom, 20, 5);
			miPlayer.saveGameMode(groupFrom);
			miPlayer.saveExperience(groupFrom);
			loadPlayerState(player, groupTo);
			// Save the player's current world
			MIYamlFiles.savePlayerLogoutWorld(player.getUniqueId(), groupTo);
			ChangeInventoryEvent eventcall = new ChangeInventoryEvent(respawn.getWorld(),player.getWorld(),player);
			Bukkit.getServer().getPluginManager().callEvent(eventcall);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if(!event.isCancelled() && MIYamlFiles.separategamemodeinventories) {
			Player player = event.getPlayer();
			if(player.hasMetadata("NPC")) {
				//It's an NPC, we can safely exit...
				return;
			}
			MIPlayer miPlayer = getMIPlayer(player);

			// Find correct group
			World world = player.getWorld();
			String group = getGroup(world);

			MultiInv.log.debug(player.getName() + " changed from " + player.getGameMode().toString() + " to " + event.getNewGameMode().toString());

			// We only want to save the old inventory if we didn't switch worlds in the same tick. Inventory problems otherwise.
			if(!playerchangeworlds.containsKey(player.getUniqueId())) {
				if(!player.hasPermission("multiinv.enderchestexempt")) {
					miPlayer.saveEnderchestInventory(group, player.getGameMode().toString());
				}
				if(!player.hasPermission("multiinv.exempt")) {
					miPlayer.saveInventory(group, player.getGameMode().toString());
				}
			}
			if(!player.hasPermission("multiinv.enderchestexempt")) {
				addingItemsToPlayer(player.getUniqueId());
				miPlayer.loadEnderchestInventory(group, event.getNewGameMode().toString());
				stoppedAddingItemsToPlayer(player.getUniqueId());
			}
			if(!player.hasPermission("multiinv.exempt")) {
				addingItemsToPlayer(player.getUniqueId());
				miPlayer.loadInventory(group, event.getNewGameMode().toString());
				stoppedAddingItemsToPlayer(player.getUniqueId());
			}
			miPlayer.gameModeChanged(event.getNewGameMode());
		}
	}

	public void savePlayerState(Player player, String group) {
		// TODO: Check config for each save method
		MIPlayer miPlayer = getMIPlayer(player);
		//miPlayer.saveInventory(group, player.getGameMode().toString());
		MultiInv.log.debug("Saving player state for " + player.getName() + " with UUID: " + player.getUniqueId().toString() + " for world group: " + group);
		miPlayer.saveAll(group, player.getGameMode().toString());
	}

	public void removeCachedPlayer(Player player) {
		MIPlayer miplayer = players.get(player.getUniqueId());
		if(miplayer != null) {
			miplayer.removePlayer();
		}
	}

	public void setCachedPlayer(Player player) {
		MIPlayer miplayer = players.get(player.getUniqueId());
		if(miplayer != null) {
			miplayer.setPlayer(player);
		}
	}

	public void loadPlayerState(Player player, String group) {
		// TODO: Check config for each save method
		MultiInv.log.debug("Loading player state for " + player.getName() + " with UUID: " + player.getUniqueId().toString() + " for world group: " + group);
		MIPlayer miPlayer = getMIPlayer(player);
		if(MIYamlFiles.controlgamemode) {
			// If this is a creative world and we control the game modes let's always switch it.
			if(MIYamlFiles.creativegroups.containsKey(group)) {
				player.setGameMode(GameMode.CREATIVE);
				// Otherwise default to the mode that they were in.
			} else {
				miPlayer.loadGameMode(group);
			}
		}
		addingItemsToPlayer(player.getUniqueId());
		miPlayer.loadInventory(group, player.getGameMode().toString());
		stoppedAddingItemsToPlayer(player.getUniqueId());

		// Due to a dupe exploit this has to come after loading inventory
		miPlayer.loadHealth(group);
		miPlayer.loadHunger(group);
		// If we have the xp bug, let's "set" the xp several times. Seems like the client doesn't update the
		// xp properly if it's still loading the world... so let's just send it the current xp several times.
		miPlayer.loadExperience(group);
		if(MIYamlFiles.xpfix) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 5);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 15);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 25);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 35);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 75);
			// Last one sets those really laggy clients 30 seconds after world change. If you have any more
			// lag than this, you have problems!
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 600);
		}
	}

	public void saveEnderchestState(Player player, String group) {
		MIPlayer miPlayer = getMIPlayer(player);
		MultiInv.log.debug("Saving enderchest inventory for " + player.getName() + " with UUID: " + player.getUniqueId().toString() + " for world group: " + group);
		//This should never be null, but sometimes it is...
		if(player.getGameMode() != null) {
			miPlayer.saveEnderchestInventory(group, player.getGameMode().toString());
		}else {
			MultiInv.log.severe("The player's game mode was null while trying to save the enderchest inventory for " + player.getName() + " with UUID: " + player.getUniqueId().toString() + " for world group: " + group + ". Enderchest inventory save skipped!");
		}
	}

	public void loadEnderchestState(Player player, String group) {
		MIPlayer miPlayer = getMIPlayer(player);
		MultiInv.log.debug("Loading enderchest inventory for " + player.getName() + " with UUID: " + player.getUniqueId().toString() + " for world group: " + group);
		miPlayer.loadEnderchestInventory(group, player.getGameMode().toString());
	}

	public void loadPlayerXP(Player player, String group) {
		MIPlayer miPlayer = getMIPlayer(player);
		MultiInv.log.debug("Loading player XP for " + player.getName() + " with UUID: " + player.getUniqueId().toString() + " for world group: " + group);
		miPlayer.loadExperience(group);
	}

	public static String getGroup(String world) {
		String group = world;
		if(MIYamlFiles.getGroups().containsKey(group)) {
			group = MIYamlFiles.getGroups().get(group);
		}
		return group;
	}

	public static String getGroup(World world) {
		return getGroup(world.getName());
	}
}
