package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MultiInvHealthRunnable implements Runnable {

    public int health;
    public String player;

    public MultiInvHealthRunnable(String player, int health) {
        this.health = health;
        this.player = player;
    }

    @Override
    public void run() {
        Player playerObj = Bukkit.getServer().getPlayer(this.player);
        playerObj.setHealth(health);
    }
}
