package uk.co.tggl.pluckerpluck.multiinv.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import Tux2.TuxTwoLib.TuxTwoPlayer;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayerFile;

public class MCImportThread implements Runnable {
	
	CommandSender sender;
	MultiInv plugin;
	String group;
	
	public MCImportThread(CommandSender sender, MultiInv plugin, String group) {
		this.sender = sender;
		this.plugin = plugin;
		this.group = group;
	}

	@Override
	public synchronized void run() {
		plugin.setIsImporting(true);
		OfflinePlayer[] oplayers = Bukkit.getServer().getOfflinePlayers();
		int count = 1;
		for(OfflinePlayer player1 : oplayers) {
			try {
				Player player = TuxTwoPlayer.getOfflinePlayer(player1);
				if(player != null) {
					String playergroup = group;
					if(MIYamlFiles.logoutworld.containsKey(player.getUniqueId().toString()) && MIYamlFiles.logoutworld.get(player.getUniqueId().toString()) != null) {
						String logoutworld = MIYamlFiles.logoutworld.get(player.getUniqueId().toString());
		                String currentworld = MIPlayerListener.getGroup(player.getWorld());
		                playergroup = currentworld;
					}
					ItemStack[] inventory = player.getInventory().getContents();
					ItemStack[] enderchest = null;
					try {
						enderchest = player.getEnderChest().getContents();
					}catch (NullPointerException e) {
						sender.sendMessage(ChatColor.DARK_RED + "Error importing enderchest inventory for player " + player1.getUniqueId());
					}
					ItemStack[] armor = player.getInventory().getArmorContents();
					Double health = player.getHealth();
					Integer hunger = player.getFoodLevel();
					Float saturation = player.getSaturation();
		            int totalexp = plugin.getTotalXP(player.getLevel(), player.getExp());
					Collection<PotionEffect> effects = player.getActivePotionEffects();
					if(MIYamlFiles.usesql) {
						MIYamlFiles.con.saveAll(player1, group, new MIInventory(inventory, armor, effects), "SURVIVAL", totalexp, player.getGameMode(), health, hunger, saturation);
						if(enderchest != null) {
							MIYamlFiles.con.saveEnderchestInventory(player1, playergroup, new MIEnderchestInventory(enderchest), "SURVIVAL");		
						}
					} else {
						MIPlayerFile config = new MIPlayerFile(player1, group);
						config.saveInventory(new MIInventory(inventory, armor, effects), "SURVIVAL");
						if(enderchest != null) {
							config.saveEnderchestInventory(new MIEnderchestInventory(enderchest), "SURVIVAL");		
						}
						config.saveHealth(health);
						config.saveHunger(hunger);
						config.saveSaturation(saturation);
						int[] levels = plugin.getXP(totalexp);
						config.saveExperience(totalexp, levels[0], (float) ((float) levels[1] / (float) levels[2]));
					}
				}
			}catch (Exception e) {
				sender.sendMessage(ChatColor.DARK_RED + "Error importing survival inventory for player " + player1.getUniqueId());
			}
			MultiInv.log.info("Imported " + count++ + " of " + oplayers.length  + " players.");
		}
		sender.sendMessage(ChatColor.DARK_GREEN + "Minecraft inventories imported successfuly!");
		plugin.setIsImporting(false);
	
	}

}
