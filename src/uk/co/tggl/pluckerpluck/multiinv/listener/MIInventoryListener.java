package uk.co.tggl.pluckerpluck.multiinv.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayerGiveCache;
import Tux2.TuxTwoLib.InventoryChangeEvent;

public class MIInventoryListener implements Listener {

	MultiInv plugin;
	MIPlayerListener listener;
	
	public MIInventoryListener(MultiInv plugin, MIPlayerListener listener) {
		this.plugin = plugin;
		this.listener = listener;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void inventoryChangeListener(InventoryChangeEvent event) {
		MIPlayerGiveCache cache = listener.getPlayerGiveCache(event.getPlayer().getUniqueId());
		if(cache != null) {
			cache.addInventoryChangeEvent(event);
		}
	}
}
