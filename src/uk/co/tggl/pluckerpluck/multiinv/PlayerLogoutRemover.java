package uk.co.tggl.pluckerpluck.multiinv;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;

public class PlayerLogoutRemover implements Runnable {
	
	UUID playername;
	
	public PlayerLogoutRemover(UUID uuid) {
		playername = uuid;
	}

	@Override
	public void run() {
		Player player = Bukkit.getPlayer(playername);
		if(player == null || !player.isOnline()) {
			MIPlayerListener.removePlayer(playername);
		}
	}

}
