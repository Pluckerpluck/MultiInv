package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldSaveEvent;

public class MultiInvWorldListener extends WorldListener {

    public final MultiInv plugin;

    public MultiInvWorldListener(MultiInv instance) {
        plugin = instance;
    }

    @Override
    public void onWorldSave(WorldSaveEvent event) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            MultiInvPlayerData.storeCurrentInventory(player, player.getWorld().getName());
        }
    }
}
