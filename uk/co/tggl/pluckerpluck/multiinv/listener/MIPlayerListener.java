package uk.co.tggl.pluckerpluck.multiinv.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayer;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 18/12/11
 * Time: 23:32
 * To change this template use File | Settings | File Templates.
 */
public class MIPlayerListener implements Listener{

    static HashMap<Player, MIPlayer> players = new HashMap<Player, MIPlayer>();

    public MIPlayerListener() {
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
        players.put(player, new MIPlayer(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event){
        // Get player objects
        Player player = event.getPlayer();
        MIPlayer miPlayer = players.get(player);

        // Get world objects
        World worldTo = player.getWorld();
        World worldFrom = event.getFrom();

        // Get corresponding groups
        String groupTo = getGroup(worldTo);
        String groupFrom = getGroup(worldFrom);

        if (!groupTo.equals(groupFrom) && !miPlayer.isIgnored()){
            savePlayerState(player, groupFrom);
            loadPlayerState(player, groupTo);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event){
        if (!event.isCancelled()){
            Player player = event.getPlayer();
            MIPlayer miPlayer = players.get(player);

            // Find correct group
            World world = player.getWorld();
            String group = getGroup(world);

            miPlayer.saveInventory(group, player.getGameMode().toString());
            miPlayer.loadInventory(group, event.getNewGameMode().toString());
        }
    }

    private void savePlayerState(Player player, String group){
        // TODO: Check config for each save method
        MIPlayer miPlayer = players.get(player);
        miPlayer.saveInventory(group, player.getGameMode().toString());
        miPlayer.saveHealth(group);
        miPlayer.saveHunger(group);
        miPlayer.saveGameMode(group);
        miPlayer.saveExperience(group);
    }

    private void loadPlayerState(Player player, String group){
        //  TODO: Check config for each save method
        MIPlayer miPlayer = players.get(player);
        if(MIYamlFiles.config.getBoolean("controlGamemode", true)) {
            miPlayer.loadGameMode(group);
        }
        miPlayer.loadHealth(group);
        miPlayer.loadHunger(group);
        miPlayer.loadExperience(group);
        miPlayer.loadInventory(group, player.getGameMode().toString());
    }

    private String getGroup(World world){
        String group = world.getName();
        if (MIYamlFiles.groups.containsKey(group)){
            group =  MIYamlFiles.groups.get(group);
        }
        return group;
    }
}
