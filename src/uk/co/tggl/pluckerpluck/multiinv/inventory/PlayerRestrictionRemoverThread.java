package uk.co.tggl.pluckerpluck.multiinv.inventory;

import java.util.Enumeration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayerGiveCache;

public class PlayerRestrictionRemoverThread implements Runnable {

	MIPlayerListener listener;
	
	public PlayerRestrictionRemoverThread(MIPlayerListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void run() {
		long time = System.currentTimeMillis();
		Enumeration<MIPlayerGiveCache> pwr = listener.playerworldrestrict.elements();
		while(pwr.hasMoreElements()) {
			MIPlayerGiveCache cache = pwr.nextElement();
			if(cache.getTime() >= time) {
				listener.playerworldrestrict.remove(cache.getPlayerID());
				Player player = Bukkit.getPlayer(cache.getPlayerID());
				if(player != null) {
					//Let's do a final restore of inventory from file, bypassing the
					//little switch worlds really quickly trick.
					String world = player.getLocation().getWorld().getName();
					String groupTo = MIPlayerListener.getGroup(world);
					if(!player.hasPermission("multiinv.enderchestexempt")) {
						listener.loadEnderchestState(player, groupTo);
					}
					if(!player.hasPermission("multiinv.exempt")) {
						listener.loadPlayerState(player, groupTo);
					}
					//Let's do any stored inventory change events.
					cache.ExecuteStoredInventoryChanges();
					// Save the player's current world
					MIYamlFiles.savePlayerLogoutWorld(player.getUniqueId(), groupTo);
				}
			}
		}
	}

}
