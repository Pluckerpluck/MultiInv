package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.entity.Player;

public class MultiInvRespawnRunnable implements Runnable {

    public String groupTo;
    public String groupFrom;
    public String player;
    public MultiInv plugin;

    public MultiInvRespawnRunnable(String groupTo, String groupFrom, String player, MultiInv plugin) {
        this.groupTo = groupTo;
        this.groupFrom = groupFrom;
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Player playerObj = plugin.getServer().getPlayer(this.player);
        plugin.playerInventory.storeManualInventory(playerObj, "MultiInvInventory", groupFrom);
        if (!plugin.ignoreList.contains(player)) {
            plugin.playerInventory.loadWorldInventory(playerObj, groupTo);
        }
    }
}
