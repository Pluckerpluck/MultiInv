package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.entity.Player;

public class MultiInvRespawnRunnable implements Runnable {

    public final String groupTo;
    public final String groupFrom;
    public final String player;
    public final MultiInv plugin;

    public MultiInvRespawnRunnable(String groupTo, String groupFrom, String player, MultiInv plugin) {
        this.groupTo = groupTo;
        this.groupFrom = groupFrom;
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Player playerObj = plugin.getServer().getPlayer(this.player);

        String inventoryName;
        if (MultiInv.creativeGroups.contains(groupFrom)){
            inventoryName = "creative";
        }else{
            inventoryName = "survival";
        }

        MultiInvPlayerData.storeManualInventory(playerObj, inventoryName, groupFrom);
        if (!MultiInv.ignoreList.contains(player)) {
            MultiInvPlayerData.loadWorldInventory(playerObj, groupTo, true);
        }
    }
}
