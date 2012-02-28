package uk.co.tggl.pluckerpluck.multiinv.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.player.DeferredInvSwitch;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayer;

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

    static HashMap<Player, MIPlayer> players = new HashMap<Player, MIPlayer>();
    ConcurrentHashMap<String, Boolean> playerchangeworlds = new ConcurrentHashMap<String, Boolean>();
    static MultiInv plugin;

    public MIPlayerListener(MultiInv plugin) {
    	MIPlayerListener.plugin = plugin;
        reloadPlayersMap();
    }

    public static void reloadPlayersMap(){
        players.clear();
        for (Player player:Bukkit.getServer().getOnlinePlayers()){
            players.put(player, new MIPlayer(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event){
        Player player = event.getPlayer();
        if(player.hasPermission("multiinv.exempt")) {
        	return;
        }
        players.put(player, new MIPlayer(player));
        //Let's see if the player is in a world that doesn't exist anymore...
        if(MIYamlFiles.logoutworld.containsKey(player.getName())) {
            if(getGroup(player.getWorld()) != MIYamlFiles.logoutworld.get(player.getName())) {
            	//If they aren't in the same world they logged out of let's save their current inventory
            	//and switch them to the correct inventory for this world.
            	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DeferredInvSwitch(player, this), 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event){
        // Get player objects
        Player player = event.getPlayer();
        if(player.hasPermission("multiinv.exempt")) {
        	return;
        }
        MIPlayer miPlayer = players.get(player);

        // Get world objects
        World worldTo = player.getWorld();
        World worldFrom = event.getFrom();

        // Get corresponding groups
        String groupTo = getGroup(worldTo);
        String groupFrom = getGroup(worldFrom);

        if (!groupTo.equals(groupFrom) && !miPlayer.isIgnored()){
        	//Let's put this player in the pool of players that switched worlds, that way we don't dupe the inventory.
        	playerchangeworlds.put(player.getName(), new Boolean(true));
        	//Let's schedule it so that we take the player out soon afterwards.
        	player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemovePlayer(player.getName(), playerchangeworlds), 1);
            savePlayerState(player, groupFrom);
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
            MIPlayer miPlayer = players.get(player);

            // Find correct group
            World world = player.getWorld();
            String group = getGroup(world);

            //We only want to save the old inventory if we didn't switch worlds in the same tick. Inventory problems otherwise.
            if(!playerchangeworlds.containsKey(player.getName())) {
                miPlayer.saveInventory(group, player.getGameMode().toString());
            }
            miPlayer.loadInventory(group, event.getNewGameMode().toString());
        }
    }

    public void savePlayerState(Player player, String group){
        // TODO: Check config for each save method
        MIPlayer miPlayer = players.get(player);
        miPlayer.saveInventory(group, player.getGameMode().toString());
        miPlayer.saveHealth(group);
        miPlayer.saveHunger(group);
        miPlayer.saveGameMode(group);
        miPlayer.saveExperience(group);
    }

    public void loadPlayerState(Player player, String group){
        //  TODO: Check config for each save method
        MIPlayer miPlayer = players.get(player);
        if(MIYamlFiles.config.getBoolean("controlGamemode", true)) {
        	//If this is a creative world and we control the game modes let's always switch it.
        	if(MIYamlFiles.creativegroups.containsKey(group)) {
        		player.setGameMode(GameMode.CREATIVE);
        		//Otherwise default to the mode that they were in.
        	}else {
                miPlayer.loadGameMode(group);
        	}
        }
        miPlayer.loadHealth(group);
        miPlayer.loadHunger(group);
        miPlayer.loadExperience(group);
        miPlayer.loadInventory(group, player.getGameMode().toString());
    }

    public String getGroup(World world){
        String group = world.getName();
        if (MIYamlFiles.groups.containsKey(group)){
            group =  MIYamlFiles.groups.get(group);
        }
        return group;
    }
}
