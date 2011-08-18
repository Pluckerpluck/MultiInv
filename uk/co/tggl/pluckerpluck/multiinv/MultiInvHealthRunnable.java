package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.entity.Player;

public class MultiInvHealthRunnable implements Runnable {

    public int health;
    public String player;
    public MultiInv plugin;

    public MultiInvHealthRunnable(String player, int health, MultiInv plugin) {
        this.health = health;
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Player playerObj = plugin.getServer().getPlayer(this.player);
        playerObj.setHealth(health);
    }
}
