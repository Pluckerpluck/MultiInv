package uk.co.tggl.pluckerpluck.multiinv.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.player.DeferredWorldCheck;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayer;
import uk.co.tggl.pluckerpluck.multiinv.workarounds.SetXP;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 18/12/11
 * Time: 23:32
 * To change this template use File | Settings | File Templates.
 */
public class MIPlayerListener implements Listener{

    static HashMap<String, MIPlayer> players = new HashMap<String, MIPlayer>();
    ConcurrentHashMap<String, Boolean> playerchangeworlds = new ConcurrentHashMap<String, Boolean>();
    static public MultiInv plugin;

    public MIPlayerListener(MultiInv plugin) {
    	MIPlayerListener.plugin = plugin;
        reloadPlayersMap();
    }

    public static void reloadPlayersMap(){
        players.clear();
        for (Player player:Bukkit.getServer().getOnlinePlayers()){
            players.put(player.getName(), new MIPlayer(player, plugin));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event){
        Player player = event.getPlayer();
        players.put(player.getName(), new MIPlayer(player, plugin));
        if(player.hasPermission("multiinv.exempt")) {
        	return;
        }
        
        //Let's set a task to run once they get switched to the proper world by bukkit.
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DeferredWorldCheck(player, this), 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event){
    	//No need to run this twice!
    	if(MIYamlFiles.config.getBoolean("compatibilityMode")) {
    		return;
    	}
        // Get player objects
        Player player = event.getPlayer();
        if(player.hasPermission("multiinv.exempt")) {
        	return;
        }
        MIPlayer miPlayer = players.get(player.getName());

        // Get world objects
        World worldTo = player.getWorld();
        World worldFrom = event.getFrom();

        // Get corresponding groups
        String groupTo = getGroup(worldTo);
        String groupFrom = getGroup(worldFrom);

        MultiInv.log.debug(player.getName() + " moved from " + groupFrom + " to " + groupTo);

        if (!groupTo.equals(groupFrom) && !miPlayer.isIgnored()){
        	//Let's put this player in the pool of players that switched worlds, that way we don't dupe the inventory.
        	playerchangeworlds.put(player.getName(), new Boolean(true));
        	//Let's schedule it so that we take the player out soon afterwards.
        	player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemovePlayer(player.getName(), playerchangeworlds), 1);
            savePlayerState(player, groupFrom);
            loadPlayerState(player, groupTo);
            //Save the player's current world
            MIYamlFiles.savePlayerLogoutWorld(player.getName(), groupTo);
        }else if(MIYamlFiles.config.getBoolean("xpfix", true)) {
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 5);
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 15);
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 25);
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 35);
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 75);
        	//Last one sets those really laggy clients 30 seconds after world change. If you have any more
        	//lag than this, you have problems!
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 600);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
    	if(event.isCancelled()) {
    		return;
    	}
    	//Only do this if they have problem plugins.
    	if(MIYamlFiles.config.getBoolean("compatibilityMode")) {
    		if(event.getFrom().getWorld() != event.getTo().getWorld()) {
    			// Get player objects
    	        Player player = event.getPlayer();
    	        if(player.hasPermission("multiinv.exempt")) {
    	        	return;
    	        }
    	        MIPlayer miPlayer = players.get(player.getName());

    	        // Get world objects
    	        World worldTo = event.getTo().getWorld();
    	        World worldFrom = event.getFrom().getWorld();

    	        // Get corresponding groups
    	        String groupTo = getGroup(worldTo);
    	        String groupFrom = getGroup(worldFrom);

    	        MultiInv.log.debug(player.getName() + " moved from " + groupFrom + " to " + groupTo);

    	        if (!groupTo.equals(groupFrom) && !miPlayer.isIgnored()){
    	        	//Let's put this player in the pool of players that switched worlds, that way we don't dupe the inventory.
    	        	playerchangeworlds.put(player.getName(), new Boolean(true));
    	        	//Let's schedule it so that we take the player out soon afterwards.
    	        	player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemovePlayer(player.getName(), playerchangeworlds), 1);
    	            savePlayerState(player, groupFrom);
    	            loadPlayerState(player, groupTo);
    	            //Save the player's current world
    	            MIYamlFiles.savePlayerLogoutWorld(player.getName(), groupTo);
    	            
    	        }else if(MIYamlFiles.config.getBoolean("xpfix", true)) {
    	        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 5);
    	        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 15);
    	        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 25);
    	        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 35);
    	        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 75);
    	        	//Last one sets those really laggy clients 30 seconds after world change. If you have any more
    	        	//lag than this, you have problems!
    	        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 600);
    	        }
    		}
    	}
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerRespawnEvent event) {
    	if(!MIYamlFiles.config.getBoolean("compatibilityMode")) {
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
            //Save the player's current world
            MIYamlFiles.savePlayerLogoutWorld(player.getName(), groupTo);
    	}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event){
        if (!event.isCancelled() && MIYamlFiles.config.getBoolean("separateGamemodeInventories", true)){
            Player player = event.getPlayer();
            if(player.hasPermission("multiinv.exempt")) {
            	return;
            }
            MIPlayer miPlayer = players.get(player.getName());

            // Find correct group
            World world = player.getWorld();
            String group = getGroup(world);

            MultiInv.log.debug(player.getName() + " changed from " + player.getGameMode().toString() + " to " + event.getNewGameMode().toString());

            //We only want to save the old inventory if we didn't switch worlds in the same tick. Inventory problems otherwise.
            if(!playerchangeworlds.containsKey(player.getName())) {
                miPlayer.saveInventory(group, player.getGameMode().toString());
            }
            miPlayer.loadInventory(group, event.getNewGameMode().toString());
        }
    }

    public void savePlayerState(Player player, String group){
        // TODO: Check config for each save method
        MIPlayer miPlayer = players.get(player.getName());
        miPlayer.saveInventory(group, player.getGameMode().toString());
        miPlayer.saveHealth(group);
        miPlayer.saveHunger(group);
        miPlayer.saveGameMode(group);
        miPlayer.saveExperience(group);
    }

    public void loadPlayerState(Player player, String group){
        //  TODO: Check config for each save method
        MIPlayer miPlayer = players.get(player.getName());
        if(MIYamlFiles.config.getBoolean("controlGamemode", true)) {
        	//If this is a creative world and we control the game modes let's always switch it.
        	if(MIYamlFiles.creativegroups.containsKey(group)) {
        		player.setGameMode(GameMode.CREATIVE);
        		//Otherwise default to the mode that they were in.
        	}else {
                miPlayer.loadGameMode(group);
        	}
        }
        miPlayer.loadInventory(group, player.getGameMode().toString());
        
        //Due to a dupe exploit this has to come after loading inventory
        miPlayer.loadHealth(group);
        miPlayer.loadHunger(group);
      //If we have the xp bug, let's "set" the xp several times. Seems like the client doesn't update the
      //xp properly if it's still loading the world... so let's just send it the current xp several times.
        miPlayer.loadExperience(group);
        if(MIYamlFiles.config.getBoolean("xpfix", true)) {
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 5);
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 15);
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 25);
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 35);
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 75);
        	//Last one sets those really laggy clients 30 seconds after world change. If you have any more
        	//lag than this, you have problems!
        	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SetXP(player, this), 600);
        }
    }

    public void loadPlayerXP(Player player, String group){
        MIPlayer miPlayer = players.get(player.getName());
        miPlayer.loadExperience(group);
    }
    
    public static String getGroup(String world) {
    	String group = world;
    	if (MIYamlFiles.getGroups().containsKey(group)){
            group =  MIYamlFiles.getGroups().get(group);
        }
        return group;
    }

    public static String getGroup(World world){
        return getGroup(world.getName());
    }
}
