package uk.co.tggl.Pluckerpluck.MultiInv;

import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldSaveEvent;


public class MultiInvWorldListener extends WorldListener{
    
    public final MultiInv plugin;
    
    public MultiInvWorldListener(MultiInv instance) {
        plugin = instance;
    }

    @Override
    public void onWorldSave(WorldSaveEvent event) {
        for (Player player : plugin.getServer().getOnlinePlayers()){
            plugin.playerInventory.storeCurrentInventory(player, player.getWorld().getName());
        }
    }   
    
}
