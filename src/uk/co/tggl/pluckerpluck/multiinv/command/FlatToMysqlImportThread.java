package uk.co.tggl.pluckerpluck.multiinv.command;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.books.MIBook;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayerFile;

public class FlatToMysqlImportThread implements Runnable {
	
	CommandSender sender;
	MultiInv plugin;
	
	public FlatToMysqlImportThread(CommandSender sender, MultiInv plugin) {
		this.sender = sender;
		this.plugin = plugin;
	}

	@Override
	public synchronized void run() {
		if(importInventories()) {
			sender.sendMessage(ChatColor.DARK_GREEN + "MultiInv flat files converted to mysql!");
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "I'm sorry, something isn't set up right... Import aborted.");
		}
	}

	private synchronized boolean importInventories() {
		if(MIYamlFiles.con == null) {
			System.out.println("[MultiInv] No sql connection, not converting.");
			return false;
		}
		plugin.setIsImporting(true);
		System.out.println("getting World Inventories Directory");
		String worldinventoriesdir = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder().getAbsolutePath() + File.separator + "UUIDGroups";
		File worldinvdir = new File(worldinventoriesdir);
		if(worldinvdir.exists()) {
			File[] thedirs = worldinvdir.listFiles();
			for(File fdir : thedirs) {
				if(fdir.isDirectory()) {
					String group = fdir.getName();
					System.out.println("In group directory " + group);
					File[] playerfiles = fdir.listFiles();
					for(File pfile : playerfiles) {
						if(pfile.getName().endsWith(".yml") && !pfile.getName().endsWith(".ec.yml")) {
							String suuid = pfile.getName().substring(0, pfile.getName().lastIndexOf("."));
							OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(suuid));
							System.out.println("Importing player " + player1.getName() + " with UUID: " + suuid);
							MIPlayerFile playerfile = new MIPlayerFile(player1, fdir.getName());
							MIYamlFiles.con.saveExperience(player1, group, playerfile.getTotalExperience());
							if(playerfile.getGameMode() != null) {
								MIYamlFiles.con.saveGameMode(player1, group, playerfile.getGameMode());
							}
							MIYamlFiles.con.saveHealth(player1, group, playerfile.getHealth());
							MIYamlFiles.con.saveHunger(player1, group, playerfile.getHunger());
							if(playerfile.getInventory("SURVIVAL") != null) {
								try {
									MIYamlFiles.con.saveInventory(player1, group, playerfile.getInventory("SURVIVAL"), "SURVIVAL");
								} catch(NullPointerException e) {
									// We need to catch this, otherwise it goes wild sometimes... not a pretty sight to see...
								}
							}
							if(playerfile.getInventory("CREATIVE") != null) {
								try {
									MIYamlFiles.con.saveInventory(player1, group, playerfile.getInventory("CREATIVE"), "CREATIVE");
								} catch(NullPointerException e) {
									// We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
								}
							}
							if(playerfile.getInventory("ADVENTURE") != null) {
								try {
									MIYamlFiles.con.saveInventory(player1, group, playerfile.getInventory("ADVENTURE"), "ADVENTURE");
								} catch(NullPointerException e) {
									// We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
								}
							}
							if(playerfile.getEnderchestInventory("SURVIVAL") != null) {
								try {
									MIYamlFiles.con.saveEnderchestInventory(player1, group, playerfile.getEnderchestInventory("SURVIVAL"), "SURVIVAL");
								} catch(NullPointerException e) {
									// We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
								}
							}
							if(playerfile.getEnderchestInventory("CREATIVE") != null) {
								try {
									MIYamlFiles.con.saveEnderchestInventory(player1, group, playerfile.getEnderchestInventory("CREATIVE"), "CREATIVE");
								} catch(NullPointerException e) {
									// We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
								}
							}
							if(playerfile.getEnderchestInventory("ADVENTURE") != null) {
								try {
									MIYamlFiles.con.saveEnderchestInventory(player1, group, playerfile.getEnderchestInventory("ADVENTURE"), "ADVENTURE");
								} catch(NullPointerException e) {
									// We need to catch this for old inventory files, otherwise it goes wild... not a pretty sight to see...
								}
							}
							MIYamlFiles.con.saveSaturation(player1, group, playerfile.getSaturation());
	
						}
					}
				}
			}
			String booksdir = Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder().getAbsolutePath() + File.separator + "books";
			File fbooksdir = new File(booksdir);
			if(fbooksdir.exists()) {
				System.out.println("books directory found, importing books.");
				File[] thebooks = fbooksdir.listFiles();
				for(File fdir : thebooks) {
					if(fdir.isFile() && fdir.getName().endsWith(".yml")) {
						System.out.println("Importing book " + fdir.getName());
						MIBook thebook = new MIBook(fdir);
						MIYamlFiles.con.saveBook(thebook, true);
					}
				}
			}
			plugin.setIsImporting(false);
			return true;
		}
		plugin.setIsImporting(false);
		return false;
	}
}