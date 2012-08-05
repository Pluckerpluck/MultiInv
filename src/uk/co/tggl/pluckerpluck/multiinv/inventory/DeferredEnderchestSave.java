package uk.co.tggl.pluckerpluck.multiinv.inventory;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

public class DeferredEnderchestSave implements Runnable {
	
	Inventory inventory;
	HumanEntity player;
	String inventoryName;
	String group;
	
	public DeferredEnderchestSave(Inventory inventory, HumanEntity player, String group, String inventoryName) {
		this.inventory = inventory;
		this.player = player;
		this.inventoryName = inventoryName;
		this.group = group;
	}

	@Override
	public void run() {
		MIEnderchestInventory miinventory = new MIEnderchestInventory(inventory);
		if(MIYamlFiles.config.getBoolean("useSQL")) {
			MIYamlFiles.con.saveEnderchestInventory(player.getName(), group, miinventory, inventoryName);
		}else {
			// Find and load configuration file for the player's enderchest
	        File dataFolder =  Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
	        File worldsFolder = new File(dataFolder, "Groups");
	        File file = new File(worldsFolder, group + File.separator + player.getName() + ".ec.yml");
	        String playername = player.getName();
	        YamlConfiguration playerFile = new YamlConfiguration();
	        if (file.exists()){
	            try{
	                playerFile.load(file);
	            }catch (Exception e){
	                e.printStackTrace();
	                return;
	            }
	        }
	        String inventoryString = new MIEnderchestInventory(inventory).toString();
            playerFile.set(inventoryName, inventoryString);
            String folder = file.getParentFile().getName();
            MultiInv.log.debug("Saving " + playername + "'s " + inventoryName + " Enderchest inventory to " + folder);
            try {
				playerFile.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
