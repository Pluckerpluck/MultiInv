package uk.co.tggl.pluckerpluck.multiinv.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitTask;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.PlayerLogoutRemover;
import uk.co.tggl.pluckerpluck.multiinv.player.DeferredWorldCheck;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayer;
import uk.co.tggl.pluckerpluck.multiinv.workarounds.SetXP;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA. User: Pluckerpluck Date: 18/12/11 Time: 23:32 To change this template use File | Settings | File Templates.
 */
public class MIPlayerListener implements Listener {

	static ConcurrentHashMap<String,MIPlayer> players = new ConcurrentHashMap<String,MIPlayer>();
	ConcurrentHashMap<String,Boolean> playerchangeworlds = new ConcurrentHashMap<String,Boolean>();
	static public MultiInv plugin;

	static ConcurrentHashMap<String, BukkitTask> playerremoval = new ConcurrentHashMap<String, BukkitTask>();


	public MIPlayerListener(MultiInv plugin) {
		MIPlayerListener.plugin = plugin;
		reloadPlayersMap();
	}

	public static void reloadPlayersMap() {
		players.clear();
		for(Player player : Bukkit.getServer().getOnlinePlayers()) {
			players.put(player.getName(), new MIPlayer(player, plugin));
		}
	}

	public static void removePlayer(String player) {
		players.remove(player);
		playerremoval.remove(player);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onWorldLoaded(WorldLoadEvent event) {
		World world = event.getWorld();
		plugin.addWorld(world);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if(playerremoval.containsKey(event.getPlayer().getName())) {
			BukkitTask task = playerremoval.get(player.getName());
			task.cancel();
		}
		players.put(player.getName(), new MIPlayer(player, plugin));
		if(!player.hasPermission("multiinv.exempt") || !player.hasPermission("multiinv.enderchestexempt")) {
			// Let's set a task to run once they get switched to the proper world by bukkit.
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DeferredWorldCheck(player, this), 1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogout(PlayerQuitEvent event) {
		BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, new PlayerLogoutRemover(event.getPlayer().getName()), 20*60);
		playerremoval.put(event.getPlayer().getName(), task);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		// No need to run this twice!
		if(MIYamlFiles.compatibilitymode) {
			return;
		}
		// Get player objects
		Player player = event.getPlayer();

		// Get world objects
		World worldTo = player.getWorld();
		World worldFrom = event.getFrom();

		// Get corresponding groups
		String groupTo = getGroup(worldTo);
		String groupFrom = getGroup(worldFrom);

		MultiInv.log.debug(player.getName() + " moved from " + groupFrom + " to " + groupTo);

		if(!groupTo.equals(groupFrom)) {
			// Let's put this player in the pool of players that switched worlds, that way we don't dupe the inventory.
			playerchangeworlds.put(player.getName(), new Boolean(true));
			// Let's schedule it so that we take the player out soon afterwards.
			player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemovePlayer(player.getName(), playerchangeworlds), 2);

			if(!player.hasPermission("multiinv.enderchestexempt")) {
				saveEnderchestState(player, groupFrom);
				loadEnderchestState(player, groupTo);
			}
			if(!player.hasPermission("multiinv.exempt")) {
				savePlayerState(player, groupFrom);
				loadPlayerState(player, groupTo);
			}
			// Save the player's current world
			MIYamlFiles.savePlayerLogoutWorld(player.getName(), groupTo);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(playerchangeworlds.containsKey(event.getPlayer().getName())) {
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
					playerchangeworlds.put(player.getName(), new Boolean(true));
					// Let's schedule it so that we take the player out soon afterwards.
					player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemovePlayer(player.getName(), playerchangeworlds), 2);

					if(!player.hasPermission("multiinv.enderchestexempt")) {
						saveEnderchestState(player, groupFrom);
						loadEnderchestState(player, groupTo);
					}
					if(!player.hasPermission("multiinv.exempt")) {
						savePlayerState(player, groupFrom);
						loadPlayerState(player, groupTo);
					}
					// Save the player's current world
					MIYamlFiles.savePlayerLogoutWorld(player.getName(), groupTo);

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
		if(respawn.getWorld() != player.getWorld()) {
			String groupTo = getGroup(respawn.getWorld());
			String groupFrom = getGroup(player.getWorld());
			MIPlayer miPlayer = players.get(player.getName());
			miPlayer.saveInventory(groupFrom, player.getGameMode().toString());
			miPlayer.saveFakeHealth(groupFrom, 20);
			miPlayer.saveFakeHunger(groupFrom, 20, 5);
			miPlayer.saveGameMode(groupFrom);
			miPlayer.saveExperience(groupFrom);
			loadPlayerState(player, groupTo);
			// Save the player's current world
			MIYamlFiles.savePlayerLogoutWorld(player.getName(), groupTo);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
		if(!event.isCancelled() && MIYamlFiles.separategamemodeinventories) {
			Player player = event.getPlayer();
			MIPlayer miPlayer = players.get(player.getName());

			// Find correct group
			World world = player.getWorld();
			String group = getGroup(world);

			MultiInv.log.debug(player.getName() + " changed from " + player.getGameMode().toString() + " to " + event.getNewGameMode().toString());

			// We only want to save the old inventory if we didn't switch worlds in the same tick. Inventory problems otherwise.
			if(!playerchangeworlds.containsKey(player.getName())) {
				if(!player.hasPermission("multiinv.enderchestexempt")) {
					miPlayer.saveEnderchestInventory(group, player.getGameMode().toString());
				}
				if(!player.hasPermission("multiinv.exempt")) {
					miPlayer.saveInventory(group, player.getGameMode().toString());
				}
			}
			if(!player.hasPermission("multiinv.enderchestexempt")) {
				miPlayer.loadEnderchestInventory(group, event.getNewGameMode().toString());
			}
			if(!player.hasPermission("multiinv.exempt")) {
				miPlayer.loadInventory(group, event.getNewGameMode().toString());
			}
		}
	}

	public void savePlayerState(Player player, String group) {
		// TODO: Check config for each save method
		MIPlayer miPlayer = players.get(player.getName());
		//miPlayer.saveInventory(group, player.getGameMode().toString());
		miPlayer.saveAll(group, player.getGameMode().toString());
		//miPlayer.saveHealth(group);
		//miPlayer.saveHunger(group);
		//miPlayer.saveGameMode(group);
		//miPlayer.saveExperience(group);
	}

	public void loadPlayerState(Player player, String group) {
		// TODO: Check config for each save method
		MIPlayer miPlayer = players.get(player.getName());
		if(MIYamlFiles.controlgamemode) {
			// If this is a creative world and we control the game modes let's always switch it.
			if(MIYamlFiles.creativegroups.containsKey(group)) {
				player.setGameMode(GameMode.CREATIVE);
				// Otherwise default to the mode that they were in.
			} else {
				miPlayer.loadGameMode(group);
			}
		}
		miPlayer.loadInventory(group, player.getGameMode().toString());

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
		MIPlayer miPlayer = players.get(player.getName());
		miPlayer.saveEnderchestInventory(group, player.getGameMode().toString());
	}

	public void loadEnderchestState(Player player, String group) {
		MIPlayer miPlayer = players.get(player.getName());
		miPlayer.loadEnderchestInventory(group, player.getGameMode().toString());
	}

	public void loadPlayerXP(Player player, String group) {
		MIPlayer miPlayer = players.get(player.getName());
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
