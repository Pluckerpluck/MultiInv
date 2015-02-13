package uk.co.tggl.pluckerpluck.multiinv.listener;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This Class is a kludge to remove a player after a tick so that the game mode changes don't cause the inventory dupe.
 * 
 * @author joshua
 * 
 */
public class RemovePlayer implements Runnable {
    
    UUID player;
    ConcurrentHashMap<UUID,Boolean> players;
    
    public RemovePlayer(UUID player, ConcurrentHashMap<UUID,Boolean> players) {
        this.player = player;
        this.players = players;
    }
    
    @Override
    public void run() {
        if(players.containsKey(player)) {
            players.remove(player);
        }
    }
    
}
