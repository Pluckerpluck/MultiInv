package uk.co.tggl.pluckerpluck.multiinv.api;



import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class ChangeInventoryEvent extends Event {
	
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private World worldTo,worldFrom;
    
    public ChangeInventoryEvent(World worldTo, World worldFrom, Player player) {
		this.player=player;
		this.worldTo = worldTo;
		this.worldFrom = worldFrom;
	}

    public Player getPlayer(){
    	return this.player;
    }
    
    public World getWorldTo(){
    	return this.worldTo;
    }
    
    public World getWorldFrom(){
    	return this.worldFrom;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
