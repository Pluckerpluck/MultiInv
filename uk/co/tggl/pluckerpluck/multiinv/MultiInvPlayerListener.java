package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitScheduler;
import uk.co.tggl.pluckerpluck.multiinv.MultiInvEnums.MultiInvEvent;

import java.sql.SQLOutput;

public class MultiInvPlayerListener extends PlayerListener {

    public final MultiInv plugin;
    public BukkitScheduler tasks;

    public MultiInvPlayerListener(MultiInv instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.shares == 0) {
            Boolean shares = plugin.fileReader.parseShares();
            if (shares) {
                MultiInv.log.info("[" + MultiInv.pluginName + "] Shared worlds loaded with no errors");
                plugin.shares = 1;
            }
            plugin.shares = 2;
        }
        Player player = event.getPlayer();
        String playerName = player.getName();
        //String world = player.getWorld().getName();

        plugin.loadPermissions(player);

        plugin.debugger.debugEvent(MultiInvEvent.PLAYER_LOGIN, new String[]{playerName});
        //plugin.playerInventory.loadWorldInventory(player, world);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        // TODO: Add Debugging
        // plugin.debugger.debugEvent(MultiInvEvent.PLAYER_LOGOUT, new String[]{playerName});
        MultiInvPlayerData.storeCurrentInventory(player, player.getWorld().getName());
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!(event.isCancelled())) {
            String groupTo = event.getTo().getWorld().getName();
            Player player = event.getPlayer();
            String groupFrom = event.getFrom().getWorld().getName();
            if (!(groupTo.equals(groupFrom))) {
                plugin.debugger.debugEvent(MultiInvEvent.WORLD_CHANGE,
                        new String[]{player.getName(), groupFrom, groupTo});
            }
            if (MultiInv.sharesMap.containsKey(groupTo)) {
                groupTo = MultiInv.sharesMap.get(groupTo);
            }
            if (MultiInv.sharesMap.containsKey(groupFrom)) {
                groupFrom = MultiInv.sharesMap.get(groupFrom);
            }
            if (!(groupTo.equals(groupFrom))) {
                if (!MultiInv.ignoreList.contains(player.getName().toLowerCase())) {
                    MultiInvPlayerData.storeCurrentInventory(player, groupFrom);

                    //set GameMode and load inventory
                    setGameMode(player, groupTo);
                    MultiInvPlayerData.loadWorldInventory(player, groupTo, true);
                }
            }
        }
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (!(event.isCancelled()) && event.getTo() != null) {
            String groupTo = event.getTo().getWorld().getName();
            Player player = event.getPlayer();
            String groupFrom = event.getFrom().getWorld().getName();
            if (!(groupTo.equals(groupFrom))) {
                plugin.debugger.debugEvent(MultiInvEvent.WORLD_CHANGE,
                        new String[]{player.getName(), groupFrom, groupTo});
            }
            if (MultiInv.sharesMap.containsKey(groupTo)) {
                groupTo = MultiInv.sharesMap.get(groupTo);
            }
            if (MultiInv.sharesMap.containsKey(groupFrom)) {
                groupFrom = MultiInv.sharesMap.get(groupFrom);
            }
            if (!(groupTo.equals(groupFrom))) {
                if (!MultiInv.ignoreList.contains(player.getName().toLowerCase())) {
                    MultiInvPlayerData.storeCurrentInventory(player, groupFrom);

                    // Set gameMode and load inventory
                    setGameMode(player, groupTo);
                    MultiInvPlayerData.loadWorldInventory(player, groupTo, true);
                }
            }
        }
    }

    @Override
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        MultiInvPlayerData.storeCurrentInventory(player, player.getWorld().getName());
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        String groupTo = event.getRespawnLocation().getWorld().getName();
        String groupFrom = event.getPlayer().getWorld().getName();
        Player player = event.getPlayer();
        String name = player.getName();

        if (MultiInv.sharesMap.containsKey(groupTo)) {
            groupTo = MultiInv.sharesMap.get(groupTo);
        }
        if (MultiInv.sharesMap.containsKey(groupFrom)) {
            groupFrom = MultiInv.sharesMap.get(groupFrom);
        }
        if (!(groupTo.equals(groupFrom))) {
            if (!MultiInv.ignoreList.contains(name.toLowerCase())) {
                MultiInvPlayerData.storeCurrentInventory(player, groupFrom);

                //set GameMode and load start runnable for respawn
                setGameMode(player, groupTo);
                MultiInvRespawnRunnable respawnWait = new MultiInvRespawnRunnable(groupTo, groupFrom, name, plugin);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, respawnWait, 40);
            }
        }

    }

    private void setGameMode(Player player, String group) {
        if (MultiInv.creativeGroups.contains(group)) {
            player.setGameMode(GameMode.CREATIVE);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }
}
