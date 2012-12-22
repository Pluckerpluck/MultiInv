package uk.co.tggl.pluckerpluck.multiinv.workarounds;

import org.bukkit.entity.Player;

import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;

public class SetXP implements Runnable {
    
    Player player;
    MIPlayerListener listener;
    
    public SetXP(Player player, MIPlayerListener listener) {
        this.player = player;
        this.listener = listener;
    }
    
    @Override
    public void run() {
        // Seems banned players generate an exception... make sure they are actually logged in...
        // this just fake "sets" the xp so that it gets resent to the client.
        if(player != null && player.isOnline()) {
            player.setLevel(player.getLevel());
            player.setExp(player.getExp());
        }
    }
    
}
