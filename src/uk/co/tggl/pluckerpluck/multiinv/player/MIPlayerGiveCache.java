package uk.co.tggl.pluckerpluck.multiinv.player;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import Tux2.TuxTwoLib.InventoryChangeEvent;

public class MIPlayerGiveCache {
	
	long restrictlimit = 0;
	UUID playerID;
	ArrayList<InventoryChangeEvent> inventorychanges = new ArrayList<InventoryChangeEvent>();
	
	public MIPlayerGiveCache(long timeout, UUID playerID) {
		restrictlimit = timeout;
		this.playerID = playerID;
	}
	
	public long getTime() {
		return restrictlimit;
	}
	
	public UUID getPlayerID() {
		return playerID;
	}
	
	public void addInventoryChangeEvent(InventoryChangeEvent event) {
		inventorychanges.add(event);
	}
	
	public void ExecuteStoredInventoryChanges() {
		Player player = Bukkit.getPlayer(playerID);
		if(player != null && player.isOnline()) {
			for(InventoryChangeEvent event : inventorychanges) {
				if(event.getItems() != null) {
					if(event.isArmor()) {
						if(event.getItems().length > 1) {
							player.getInventory().setArmorContents(event.getItems());
						}else {
							switch(event.getSlot()) {
							case 0:
								player.getInventory().setBoots(event.getItems()[0]);
								break;
							case 1:
								player.getInventory().setLeggings(event.getItems()[0]);
								break;
							case 2:
								player.getInventory().setChestplate(event.getItems()[0]);
								break;
							case 3:
								player.getInventory().setHelmet(event.getItems()[0]);
								break;
							}
						}
					}else {
						if(event.getSlot() < 0 || event.getItems().length > 1) {
							for(ItemStack is : event.getItems()) {
								//Dont add in null items
								if(is != null) {
									player.getInventory().addItem(is);
								}
							}
						}else {
							player.getInventory().setItem(event.getSlot(), event.getItems()[0]);
						}
					}
				}
			}
		}
	}

}
