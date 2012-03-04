package uk.co.tggl.pluckerpluck.multiinv.command;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayerFile;

/**
 * Created by IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 19/12/11
 * Time: 22:58
 * To change this template use File | Settings | File Templates.
 */
public class MICommand {
	
	MultiInv plugin;
	
	public MICommand(MultiInv plugin) {
		this.plugin = plugin;
	}

    public static void command(String[] strings, CommandSender sender){
    	Player player = null;
    	if(sender instanceof Player) {
    		player = (Player)sender;
    	}
    	if(strings.length > 0) {
            String command = strings[0];
            
            //Check to see if the player has the permission to run this command.
            if(player != null && !player.hasPermission("multiinv." + command.toLowerCase())) {
            	return;
            }
            
            // Populate a new args array
            String[] args = new String[strings.length - 1];
            for (int i = 1; i < strings.length; i++) {
                args[i-1] = strings[i];
            }
            if(command.equalsIgnoreCase("reload")) {
            	MIYamlFiles.loadConfig();
            	MIYamlFiles.loadGroups();
            	sender.sendMessage(ChatColor.DARK_GREEN + "MultiInv configs reloaded!");
            }else if(command.equalsIgnoreCase("import")) {
            	if(importInventories()) {
            		sender.sendMessage(ChatColor.DARK_GREEN + "MultiInv flat files converted to mysql!");
            	}else {
            		sender.sendMessage(ChatColor.DARK_RED + "I'm sorry, something isn't set up right... Import aborted.");
            	}
            	
            }
    	}
    }

	private static boolean importInventories() {
		if(MIYamlFiles.con == null) {
			System.out.println("[MultiInv] No sql connection, not converting.");
			return false;
		}
	    System.out.println("getting World Inventories Directory");
		String worldinventoriesdir = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder().getAbsolutePath() + File.separator + "Groups";
		File worldinvdir = new File(worldinventoriesdir);
		if(worldinvdir.exists()) {
	    	File[] thedirs = worldinvdir.listFiles();
	    	for(File fdir : thedirs) {
	    		if(fdir.isDirectory()) {
	    			String group = fdir.getName();
	                System.out.println("In group directory " + group);
	    			File[] playerfiles = fdir.listFiles();
	    			for(File pfile : playerfiles) {
	    				if(pfile.getName().endsWith(".yml")) {
	                        String playername = pfile.getName().substring(0, pfile.getName().lastIndexOf("."));
	                        System.out.println("Importing player " + playername);
	                        MIPlayerFile playerfile = new MIPlayerFile(playername, fdir.getName());
	                        MIYamlFiles.con.saveExperience(playername, group, playerfile.getTotalExperience());
	                        if(playerfile.getGameMode() != null) {
		                        MIYamlFiles.con.saveGameMode(playername, group, playerfile.getGameMode());
	                        }
	                        MIYamlFiles.con.saveHealth(playername, group, playerfile.getHealth());
	                        MIYamlFiles.con.saveHunger(playername, group, playerfile.getHunger());
	                        if(playerfile.getInventory("SURVIVAL") != null) {
	                        	try {
			                        MIYamlFiles.con.saveInventory(playername, group, playerfile.getInventory("SURVIVAL"), "SURVIVAL");
	                        	}catch (NullPointerException e) {
	                        		//We need to catch this, otherwise it goes wild sometimes... not a pretty sight to see...
	                        	}
	                        }
	                        if(playerfile.getInventory("CREATIVE") != null) {
	                        	try {
			                        MIYamlFiles.con.saveInventory(playername, group, playerfile.getInventory("CREATIVE"), "CREATIVE");
	                        	}catch (NullPointerException e) {
	                        		//We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
	                        	}
	                        }
	                        MIYamlFiles.con.saveSaturation(playername, group, playerfile.getSaturation());
	                        
	    				}
	    			}
	    		}
	    	}
	    	return true;
		}
		return false;
	}
}
