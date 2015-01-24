package uk.co.tggl.pluckerpluck.multiinv.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import me.drayshak.WorldInventories.Group;
import me.drayshak.WorldInventories.InventoryLoadType;
import me.drayshak.WorldInventories.InventoryStoredType;
import me.drayshak.WorldInventories.PlayerStats;
import me.drayshak.WorldInventories.WorldInventories;

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

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.ProfileTypes;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.books.MIBook;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayerFile;

/**
 * Created by IntelliJ IDEA. User: Pluckerpluck Date: 19/12/11 Time: 22:58 To change this template use File | Settings | File Templates.
 */
public class MICommand {

	MultiInv plugin;

	public MICommand(MultiInv plugin) {
		this.plugin = plugin;
	}

	public static void command(String[] strings, CommandSender sender, MultiInv plugin) {
		Player player = null;
		if(sender instanceof Player) {
			player = (Player) sender;
		}
		if(strings.length > 0) {
			String command = strings[0];

			// Check to see if the player has the permission to run this command.
			if(player != null && !player.hasPermission("multiinv." + command.toLowerCase())) {
				return;
			}

			// Populate a new args array
			String[] args = new String[strings.length - 1];
			for(int i = 1; i < strings.length; i++) {
				args[i - 1] = strings[i];
			}
			if(command.equalsIgnoreCase("report")) {
				if(plugin.dreport != null) {
					LinkedList<String> customdata = new LinkedList<String>();
					customdata.add("MultiInv Custom Data");
					customdata.add("================================");
					customdata.add("-----------config.yml-----------");
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new FileReader(plugin.getDataFolder() + File.separator + "config.yml"));
						String line = null;
						while ((line = reader.readLine()) != null) {
							if(line.startsWith("  password:")) {
								line = "  password: NOTSHOWN";
							}
							customdata.add(line);
						}
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}finally {
						if(reader != null) {
							try {
								reader.close();
							} catch (IOException e) {
							}
							reader = null;
						}
					}
					customdata.add("-----------groups.yml-----------");
					try {
						reader = new BufferedReader(new FileReader(plugin.getDataFolder() + File.separator + "groups.yml"));
						String line = null;
						while ((line = reader.readLine()) != null) {
							customdata.add(line);
						}
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}finally {
						if(reader != null) {
							try {
								reader.close();
							} catch (IOException e) {
							}
						}
					}
					plugin.dreport.createReport(sender, customdata);
				}else {
					sender.sendMessage(ChatColor.RED + "In order to generate a debug report you need the plugin DebugReport!");
				}
			}else if(command.equalsIgnoreCase("reload")) {
				MIYamlFiles.loadConfig();
				MIYamlFiles.loadGroups();
				sender.sendMessage(ChatColor.DARK_GREEN + "MultiInv configs reloaded!");
			} else if(command.equalsIgnoreCase("import")) {
				sender.sendMessage(ChatColor.GOLD + "Please wait as we import all the player files.");
				FlatToMysqlImportThread ithread = new FlatToMysqlImportThread(sender, plugin);
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, ithread);
			} else if(command.equalsIgnoreCase("mvimport")) {
				sender.sendMessage(ChatColor.GOLD + "Please wait as we import all the player files from Multiverse-Inventories.");
				MultiverseImportThread ithread = new MultiverseImportThread(sender, plugin);
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, ithread);
			} else if(command.equalsIgnoreCase("miimport")) {
				Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldInventories");
				if(p == null) {
					sender.sendMessage(ChatColor.DARK_RED + "I'm sorry, WorldInventories isn't loaded... Import aborted.");
				} else {
					WorldInventories mvinventories;
					try {
						mvinventories = (WorldInventories) p;
					} catch(Exception e) {
						MultiInv.log.severe("Unable to import inventories from WorldInventories.");
						sender.sendMessage(ChatColor.DARK_RED + "I'm sorry, something funky happened... Import aborted.");
						return;
					}
					ArrayList<Group> mvgroups = WorldInventories.groups;
					YamlConfiguration groups = new YamlConfiguration();
					MIYamlFiles.getGroups().clear();
					for(Group mvgroup : mvgroups) {
						List<String> mvworlds = mvgroup.getWorlds();
						ArrayList<String> exampleGroup = new ArrayList<String>();
						for(String world : mvworlds) {
							exampleGroup.add(world);
							MIYamlFiles.getGroups().put(world, mvgroup.getName());
						}
						String group = mvgroup.getName();
						groups.set(group, exampleGroup);
						MIYamlFiles.saveYamlFile(groups, "groups.yml");
						for(OfflinePlayer player1 : Bukkit.getServer().getOfflinePlayers()) {
							plugin.getLogger().info("Importing player " + player1.getName() + "'s inventory from group " + mvgroup.getName());
							HashMap<Integer, ItemStack[]> pinventory = mvinventories.loadPlayerInventory(player1.getName(), mvgroup, InventoryLoadType.INVENTORY);
							HashMap<Integer, ItemStack[]> playerenderchest = mvinventories.loadPlayerInventory(player1.getName(), mvgroup, InventoryLoadType.ENDERCHEST);
							PlayerStats playerstats = mvinventories.loadPlayerStats(player1.getName(), mvgroup);
							if(pinventory != null) {
								ItemStack[] inventory = pinventory.get(InventoryStoredType.INVENTORY);
								ItemStack[] armor = pinventory.get(InventoryStoredType.ARMOUR);
								double health = 20;
								int hunger = 20;
								float saturation = 5;
								int totalexp = 0;
								int level = 0;
								float exp = 0;
								if(playerstats != null) {
									health = playerstats.getHealth();
									hunger = playerstats.getFoodLevel();
									saturation = playerstats.getSaturation();
									level = playerstats.getLevel();
									exp = playerstats.getExp();
									totalexp = plugin.getTotalXP(level, exp);
								}
								if(MIYamlFiles.usesql) {
									if(playerenderchest != null) {
										ItemStack[] enderchestinventory = playerenderchest.get(InventoryStoredType.ARMOUR);
										MIYamlFiles.con.saveEnderchestInventory(player, group, new MIEnderchestInventory(enderchestinventory),
												"SURVIVAL");
									}
									MIYamlFiles.con.saveInventory(player, group, new MIInventory(inventory, armor, new LinkedList<PotionEffect>()),
											"SURVIVAL");
									MIYamlFiles.con.saveHealth(player, group, health);
									MIYamlFiles.con.saveHunger(player, group, hunger);
									MIYamlFiles.con.saveSaturation(player, group, saturation);
									MIYamlFiles.con.saveExperience(player, group, totalexp);
								} else {
									MIPlayerFile config = new MIPlayerFile(player1, group);
									config.saveInventory(new MIInventory(inventory, armor, new LinkedList<PotionEffect>()), "SURVIVAL");
									if(playerenderchest != null) {
										ItemStack[] enderchestinventory = playerenderchest.get(InventoryStoredType.ARMOUR);
										config.saveEnderchestInventory(new MIEnderchestInventory(enderchestinventory), "SURVIVAL");
									}
									config.saveHealth(health);
									config.saveHunger(hunger);
									config.saveSaturation(saturation);
									config.saveExperience(totalexp, level, exp);
								}
							}
						}
					}

					// Now to emulate WorldInventories behavior with worlds not in a group.
					ArrayList<String> exampleGroup = new ArrayList<String>();
					for(World world : Bukkit.getWorlds()) {
						String worldName = world.getName();
						if(!MIYamlFiles.getGroups().containsKey(worldName)) {
							exampleGroup.add(worldName);
							MIYamlFiles.getGroups().put(worldName, "default");
						}
					}
					// Once the groups are loaded, let's load them into MultiInv
					MIYamlFiles.parseGroups(groups);
					// Once the import is done let's disable WorldInventories.
					Bukkit.getPluginManager().disablePlugin(mvinventories);
					sender.sendMessage(ChatColor.DARK_GREEN + "WorldInventories inventories imported successfuly!");
					sender.sendMessage(ChatColor.DARK_GREEN + "Please disable/delete WorldInventories now.");
				}
			}
		}else {
			if(sender.hasPermission("multiinv.import")) {
				sender.sendMessage(ChatColor.GOLD + "Import Commands:");
				sender.sendMessage(ChatColor.GOLD + "/multiinv import" + ChatColor.AQUA + " - Import from fat file to mySQL");
				sender.sendMessage(ChatColor.GOLD + "/multiinv mvimport" + ChatColor.AQUA + " - Imports Multiverse-Inventories into MultiInv");
				sender.sendMessage(ChatColor.GOLD + "/multiinv miimport" + ChatColor.AQUA + " - Imports WorldInventories into MultiInv");
			}
			if(sender.hasPermission("multiinv.reload")) {
				sender.sendMessage(ChatColor.GOLD + "/multiinv reload" + ChatColor.AQUA + " - Reloads config files.");
			}
		}
	}
}
