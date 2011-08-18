package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitScheduler;

import uk.co.tggl.pluckerpluck.multiinv.MultiInvEnums.MultiInvEvent;

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
        plugin.debugger.debugEvent(MultiInvEvent.PLAYER_LOGOUT, new String[]{playerName});
        plugin.playerInventory.storeCurrentInventory(player, player.getWorld().getName());
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!(event.isCancelled())) {
            String worldTo = event.getTo().getWorld().getName();
            Player player = event.getPlayer();
            String worldFrom = event.getFrom().getWorld().getName();
            if (!(worldTo.equals(worldFrom))) {
                plugin.debugger.debugEvent(MultiInvEvent.WORLD_CHANGE,
                        new String[]{player.getName(), worldFrom, worldTo});
            }
            if (plugin.sharesMap.containsKey(worldTo)) {
                worldTo = plugin.sharesMap.get(worldTo);
            }
            if (plugin.sharesMap.containsKey(worldFrom)) {
                worldFrom = plugin.sharesMap.get(worldFrom);
            }
            if (!(worldTo.equals(worldFrom))) {
                plugin.playerInventory.storeCurrentInventory(player, worldFrom);
                if (!plugin.ignoreList.contains(player.getName())) {
                    plugin.playerInventory.loadWorldInventory(player, worldTo);
                }
            }
        }
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (!(event.isCancelled()) && event.getTo() != null) {
            String worldTo = event.getTo().getWorld().getName();
            Player player = event.getPlayer();
            String worldFrom = event.getFrom().getWorld().getName();
            if (!(worldTo.equals(worldFrom))) {
                plugin.debugger.debugEvent(MultiInvEvent.WORLD_CHANGE,
                        new String[]{player.getName(), worldFrom, worldTo});
            }
            if (plugin.sharesMap.containsKey(worldTo)) {
                worldTo = plugin.sharesMap.get(worldTo);
            }
            if (plugin.sharesMap.containsKey(worldFrom)) {
                worldFrom = plugin.sharesMap.get(worldFrom);
            }
            if (!(worldTo.equals(worldFrom))) {
                plugin.playerInventory.storeCurrentInventory(player, worldFrom);
                if (!plugin.ignoreList.contains(player.getName())) {
                    plugin.playerInventory.loadWorldInventory(player, worldTo);
                }
            }
        }
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        String worldTo = event.getRespawnLocation().getWorld().getName();
        String worldFrom = event.getPlayer().getWorld().getName();
        String player = event.getPlayer().getName();

        if (plugin.sharesMap.containsKey(worldTo)) {
            worldTo = plugin.sharesMap.get(worldTo);
        }
        if (plugin.sharesMap.containsKey(worldFrom)) {
            worldFrom = plugin.sharesMap.get(worldFrom);
        }
        if (!(worldTo.equals(worldFrom))) {
            MultiInvRespawnRunnable respawnWait = new MultiInvRespawnRunnable(worldTo, worldFrom, player, plugin);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, respawnWait, 40);
        }

    }
}
