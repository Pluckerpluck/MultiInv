package uk.co.tggl.pluckerpluck.multiinv.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;

/**
 * Created by IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 19/12/11
 * Time: 22:58
 * To change this template use File | Settings | File Templates.
 */
public class MICommand {

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
            }
    	}
    }
}
