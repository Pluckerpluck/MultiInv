package uk.co.tggl.pluckerpluck.multiinv.command;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import Tux2.TuxTwoLib.TuxTwoPlayer;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.api.MIAPIPlayer;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIItemStack;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayerFile;

public class MCExportThread implements Runnable {
	
	CommandSender sender;
	MultiInv plugin;
	String group;
	
	public MCExportThread(CommandSender sender, MultiInv plugin, String group) {
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
					MIAPIPlayer playerinstance = getPlayer(player, group);
					MIItemStack[] inventorycontents = playerinstance.getInventory().getInventoryContents();
					// Iterate and get inventory contents
					ItemStack[] inventory = new ItemStack[inventorycontents.length];
					for(int i = 0; i < inventory.length; i++) {
						if(inventorycontents[i] != null) {
							inventory[i] = inventorycontents[i].getItemStack();
						} else {
							inventory[i] = null;
						}
					}
					MIItemStack[] enderchestcontents = playerinstance.getEnderchest().getInventoryContents();
					ItemStack[] enderchest = new ItemStack[enderchestcontents.length];
					for(int i = 0; i < enderchest.length; i++) {
						if(enderchestcontents[i] != null) {
							enderchest[i] = enderchestcontents[i].getItemStack();
						} else {
							enderchest[i] = null;
						}
					}
					MIItemStack[] armorcontents = playerinstance.getInventory().getArmorContents();
					ItemStack[] armor = new ItemStack[armorcontents.length];
					for(int i = 0; i < armor.length; i++) {
						if(armorcontents[i] != null) {
							armor[i] = armorcontents[i].getItemStack();
						} else {
							armor[i] = null;
						}
					}
					Double health = playerinstance.getHealth();
					Integer hunger = playerinstance.getFoodlevel();
					Float saturation = playerinstance.getSaturation();
					Collection<PotionEffect> effects = playerinstance.getInventory().getPotions();
					player.getInventory().setContents(inventory);
					player.getInventory().setArmorContents(armor);
					Collection<PotionEffect> oldeffects = player.getActivePotionEffects();
					for(PotionEffect oldeffect : oldeffects) {
						player.removePotionEffect(oldeffect.getType());
					}
					for(PotionEffect effect : effects) {
						player.addPotionEffect(effect);
					}
					player.getEnderChest().setContents(enderchest);
					player.setHealth(health);
					player.setFoodLevel(hunger);
					player.setSaturation(saturation);
					player.setExp(playerinstance.getXp());
					player.setLevel(playerinstance.getXpLevel());
					player.saveData();
				
				}
			}catch (Exception e) {
				sender.sendMessage(ChatColor.DARK_RED + "Error exporting survival inventory for player " + player1.getUniqueId());
				e.printStackTrace();
			}
			MultiInv.log.info("Exported " + count++ + " of " + oplayers.length  + " players.");
		}
		sender.sendMessage(ChatColor.DARK_GREEN + "Minecraft inventories exported successfuly to the default world!");
		plugin.setIsImporting(false);
	
	}
	
	private MIAPIPlayer getPlayer(Player player, String group) {
		String inventoryName = "SURVIVAL";
        if(MIYamlFiles.con != null) {
            MIAPIPlayer playerfile = new MIAPIPlayer(player);
            playerfile.setInventory(MIYamlFiles.con.getInventory(player, group, inventoryName));
            playerfile.setEnderchest(MIYamlFiles.con.getEnderchestInventory(player, group, inventoryName));
            playerfile.setFoodlevel(MIYamlFiles.con.getHunger(player, group));
            playerfile.setSaturation(MIYamlFiles.con.getSaturation(player, group));
            playerfile.setHealth(MIYamlFiles.con.getHealth(player, group));
            int totalxp = MIYamlFiles.con.getTotalExperience(player, group);
            int[] xp = plugin.getXP(totalxp);
            playerfile.setXpLevel(xp[0]);
            playerfile.setXp((float) ((float) xp[1] / (float) xp[2]));
            playerfile.setGm(MIYamlFiles.con.getGameMode(player, group));
            return playerfile;
        } else {
            MIPlayerFile config = new MIPlayerFile(player, group);
            MIAPIPlayer playerfile = new MIAPIPlayer(player);
            playerfile.setInventory(config.getInventory(inventoryName));
            playerfile.setEnderchest(config.getEnderchestInventory(inventoryName));
            playerfile.setFoodlevel(config.getHunger());
            playerfile.setSaturation(config.getSaturation());
            playerfile.setHealth(config.getHealth());
            playerfile.setXpLevel(config.getLevel());
            playerfile.setXp(config.getExperience());
            playerfile.setGm(config.getGameMode());
            return playerfile;
        }
	}

}
