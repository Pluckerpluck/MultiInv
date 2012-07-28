package uk.co.tggl.pluckerpluck.multiinv.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

public class MIEnderChest implements Listener {
	
	MultiInv plugin;
	
	public MIEnderChest(MultiInv plugin) {
	    MIPlayerListener.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void blockPlaced(BlockPlaceEvent event) {
		if(event.getBlockPlaced().getTypeId() == 130 && !MIYamlFiles.config.getBoolean("allowEnderChestPlacement", true)) {
		event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.DARK_RED + "I'm sorry, EnderChest placement disabled.");
		}
	}
	
}
