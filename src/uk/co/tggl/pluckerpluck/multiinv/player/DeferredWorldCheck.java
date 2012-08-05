package uk.co.tggl.pluckerpluck.multiinv.player;

import org.bukkit.entity.Player;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;

public class DeferredWorldCheck implements Runnable  {
	
	Player player;
	MIPlayerListener listener;

	public DeferredWorldCheck(Player player, MIPlayerListener listener) {
		this.player = player;
		this.listener = listener;
	}
	@Override
	public void run() {
		//Seems banned players generate an exception... make sure they are actually logged in...
		if(player != null && player.isOnline()) {
			//Let's see if the player is in a world that doesn't exist anymore...
	        if(MIYamlFiles.logoutworld.containsKey(player.getName())) {
	        	 MultiInv.log.debug(player.getName() + " has logged in in world " + player.getWorld().getName() + ". Logout world was: " + MIYamlFiles.logoutworld.get(player.getName()));
	            if(MIPlayerListener.getGroup(player.getWorld()) != MIYamlFiles.logoutworld.get(player.getName())) {
	            	//If they aren't in the same world they logged out of let's save their current inventory
	    	    	listener.savePlayerState(player, MIYamlFiles.logoutworld.get(player.getName()));
	    	    	//and switch them to the correct inventory for this world.
	    	    	listener.loadPlayerState(player, MIPlayerListener.getGroup(player.getWorld()));
	    	    	MIYamlFiles.savePlayerLogoutWorld(player.getName(), MIPlayerListener.getGroup(player.getWorld()));
	            }
	        }
		}
	}
}
