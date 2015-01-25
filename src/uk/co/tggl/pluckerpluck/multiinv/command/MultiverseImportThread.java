package uk.co.tggl.pluckerpluck.multiinv.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayerFile;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.ProfileTypes;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;

public class MultiverseImportThread implements Runnable {
	
	CommandSender sender;
	MultiInv plugin;
	
	public MultiverseImportThread(CommandSender sender, MultiInv plugin) {
		this.sender = sender;
		this.plugin = plugin;
	}

	@Override
	public synchronized void run() {
		Plugin p = plugin.getServer().getPluginManager().getPlugin("Multiverse-Inventories");
		if(p == null) {
			sender.sendMessage(ChatColor.DARK_RED + "I'm sorry, Multiverse-Inventories isn't loaded... Import aborted.");
		} else {
			MultiverseInventories mvinventories;
			try {
				mvinventories = (MultiverseInventories) p;
			} catch(Exception e) {
				MultiInv.log.severe("Unable to import inventories from Multiverse-Inventories.");
				sender.sendMessage(ChatColor.DARK_RED + "I'm sorry, something funky happened... Import aborted.");
				return;
			}
			plugin.setIsImporting(true);
			List<WorldGroupProfile> mvgroups = mvinventories.getGroupManager().getGroups();
			YamlConfiguration groups = new YamlConfiguration();
			MIYamlFiles.getGroups().clear();
			OfflinePlayer[] oplayers = Bukkit.getServer().getOfflinePlayers();
			for(WorldGroupProfile mvgroup : mvgroups) {
				Set<String> mvworlds = mvgroup.getWorlds();
				ArrayList<String> exampleGroup = new ArrayList<String>();
				for(String world : mvworlds) {
					exampleGroup.add(world);
					MIYamlFiles.getGroups().put(world, mvgroup.getName());
				}
				String group = mvgroup.getName();
				groups.set(group, exampleGroup);
				MIYamlFiles.saveYamlFile(groups, "groups.yml");
				int count = 1;
				for(OfflinePlayer player1 : oplayers) {
					try {
						PlayerProfile playerdata = mvgroup.getPlayerData(ProfileTypes.SURVIVAL, player1);
						if(playerdata != null && playerdata.get(Sharables.INVENTORY) != null) {
							ItemStack[] inventory = playerdata.get(Sharables.INVENTORY);
							ItemStack[] armor = playerdata.get(Sharables.ARMOR);
							Double health = playerdata.get(Sharables.HEALTH);
							Integer hunger = playerdata.get(Sharables.FOOD_LEVEL);
							Float saturation = playerdata.get(Sharables.SATURATION);
							Integer totalexp = playerdata.get(Sharables.TOTAL_EXPERIENCE);
							PotionEffect[] potioneffects = playerdata.get(Sharables.POTIONS);
							LinkedList<PotionEffect> effects = new LinkedList<PotionEffect>();
							if(potioneffects != null) {
								for(int i = 0; i < potioneffects.length; i++) {
									effects.add(potioneffects[i]);
								}
							}
							if(MIYamlFiles.usesql) {
								MIYamlFiles.con.saveInventory(player1, group, new MIInventory(inventory, armor, effects), "SURVIVAL");
								MIYamlFiles.con.saveHealth(player1, group, health);
								MIYamlFiles.con.saveHunger(player1, group, hunger);
								MIYamlFiles.con.saveSaturation(player1, group, saturation);
								MIYamlFiles.con.saveExperience(player1, group, totalexp);
							} else {
								MIPlayerFile config = new MIPlayerFile(player1, group);
								config.saveInventory(new MIInventory(inventory, armor, effects), "SURVIVAL");
								config.saveHealth(health);
								config.saveHunger(hunger);
								config.saveSaturation(saturation);
								int[] levels = plugin.getXP(totalexp);
								config.saveExperience(totalexp, levels[0], (float) ((float) levels[1] / (float) levels[2]));
							}
						}
					}catch (Exception e) {
						sender.sendMessage(ChatColor.DARK_RED + "Error importing survival inventory for player " + player1.getName());
					}
					try {
						PlayerProfile adventureplayerdata = mvgroup.getPlayerData(ProfileTypes.ADVENTURE, player1);
						if(adventureplayerdata != null && adventureplayerdata.get(Sharables.INVENTORY) != null) {
							ItemStack[] inventory = adventureplayerdata.get(Sharables.INVENTORY);
							ItemStack[] armor = adventureplayerdata.get(Sharables.ARMOR);
							PotionEffect[] potioneffects = adventureplayerdata.get(Sharables.POTIONS);
							LinkedList<PotionEffect> effects = new LinkedList<PotionEffect>();
							if(potioneffects != null) {
								for(int i = 0; i < potioneffects.length; i++) {
									effects.add(potioneffects[i]);
								}
							}
							if(MIYamlFiles.usesql) {
								MIYamlFiles.con.saveInventory(player1, group, new MIInventory(inventory, armor, effects), "ADVENTURE");
							} else {
								MIPlayerFile config = new MIPlayerFile(player1, group);
								config.saveInventory(new MIInventory(inventory, armor, effects), "ADVENTURE");
							}
						}
					}catch (Exception e) {
						sender.sendMessage(ChatColor.DARK_RED + "Error importing adventure inventory for player " + player1.getName());
					}
					MultiInv.log.info("Imported " + count++ + " of " + oplayers.length  + " players in group " + mvgroup.getName());
				}
			}
			for(World world : Bukkit.getWorlds()) {
				String worldName = world.getName();
				if(!MIYamlFiles.getGroups().containsKey(worldName)) {
					WorldProfile worldprofile = mvinventories.getWorldManager().getWorldProfile(worldName);
					int count = 1;
					for(OfflinePlayer player1 : oplayers) {
						PlayerProfile playerdata = worldprofile.getPlayerData(ProfileTypes.SURVIVAL, player1);
						if(playerdata != null && playerdata.get(Sharables.INVENTORY) != null) {
							ItemStack[] inventory = playerdata.get(Sharables.INVENTORY);
							ItemStack[] armor = playerdata.get(Sharables.ARMOR);
							Double health = playerdata.get(Sharables.HEALTH);
							Integer hunger = playerdata.get(Sharables.FOOD_LEVEL);
							Float saturation = playerdata.get(Sharables.SATURATION);
							Integer totalexp = playerdata.get(Sharables.TOTAL_EXPERIENCE);
							PotionEffect[] potioneffects = playerdata.get(Sharables.POTIONS);
							LinkedList<PotionEffect> effects = new LinkedList<PotionEffect>();
							if(potioneffects != null) {
								for(int i = 0; i < potioneffects.length; i++) {
									effects.add(potioneffects[i]);
								}
							}
							if(MIYamlFiles.usesql) {
								MIYamlFiles.con.saveInventory(player1, worldName, new MIInventory(inventory, armor, effects), "SURVIVAL");
								MIYamlFiles.con.saveHealth(player1, worldName, health);
								MIYamlFiles.con.saveHunger(player1, worldName, hunger);
								MIYamlFiles.con.saveSaturation(player1, worldName, saturation);
								MIYamlFiles.con.saveExperience(player1, worldName, totalexp);
							} else {
								MIPlayerFile config = new MIPlayerFile(player1, worldName);
								config.saveInventory(new MIInventory(inventory, armor, effects), "SURVIVAL");
								config.saveHealth(health);
								config.saveHunger(hunger);
								config.saveSaturation(saturation);
								int[] levels = plugin.getXP(totalexp);
								config.saveExperience(totalexp, levels[0], (float) ((float) levels[1] / (float) levels[2]));
							}
						}
						PlayerProfile adventureplayerdata = worldprofile.getPlayerData(ProfileTypes.ADVENTURE, player1);
						if(adventureplayerdata != null && adventureplayerdata.get(Sharables.INVENTORY) != null) {
							ItemStack[] inventory = adventureplayerdata.get(Sharables.INVENTORY);
							ItemStack[] armor = adventureplayerdata.get(Sharables.ARMOR);
							PotionEffect[] potioneffects = adventureplayerdata.get(Sharables.POTIONS);
							LinkedList<PotionEffect> effects = new LinkedList<PotionEffect>();
							if(potioneffects != null) {
								for(int i = 0; i < potioneffects.length; i++) {
									effects.add(potioneffects[i]);
								}
							}
							if(MIYamlFiles.usesql) {
								MIYamlFiles.con.saveInventory(player1, worldName, new MIInventory(inventory, armor, effects), "ADVENTURE");
							} else {
								MIPlayerFile config = new MIPlayerFile(player1, worldName);
								config.saveInventory(new MIInventory(inventory, armor, effects), "ADVENTURE");
							}
						}
						MultiInv.log.info("Imported " + count++ + " of " + oplayers.length  + " players in group " + worldName);
					}
				}
			}
			// Once the groups are loaded, let's load them into MultiInv
			MIYamlFiles.parseGroups(groups);
			// Once the import is done let's disable MultiVerse-Inventories.
			Bukkit.getPluginManager().disablePlugin(mvinventories);
			sender.sendMessage(ChatColor.DARK_GREEN + "Multiverse-Inventories inventories imported successfuly!");
			sender.sendMessage(ChatColor.DARK_GREEN + "Please disable/delete Multiverse-Inventories now.");
			plugin.setIsImporting(false);
		}
	}

}
