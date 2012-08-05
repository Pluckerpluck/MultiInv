package uk.co.tggl.pluckerpluck.multiinv.player;

import org.bukkit.entity.Player;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;

public class DeferredInvSwitch implements Runnable {
	
	Player player;
	MIPlayerListener listener;

	public DeferredInvSwitch(Player player, MIPlayerListener listener) {
		this.player = player;
		this.listener = listener;
	}
	@Override
	public void run() {
		//Seems banned players generate an exception... make sure they are actually logged in...
		if(player != null && player.isOnline()) {
			//If they aren't in the same world they logged out of let's save their current inventory
	    	listener.savePlayerState(player, MIYamlFiles.logoutworld.get(player.getName()));
	    	//and switch them to the correct inventory for this world.
	    	listener.loadPlayerState(player, MIPlayerListener.getGroup(player.getWorld()));
	    	MIYamlFiles.savePlayerLogoutWorld(player.getName(), MIPlayerListener.getGroup(player.getWorld()));
		}
	}
}
