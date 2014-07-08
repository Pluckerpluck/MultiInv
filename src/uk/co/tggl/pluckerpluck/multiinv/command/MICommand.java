package uk.co.tggl.pluckerpluck.multiinv.command;

import java.io.File;
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
            if(command.equalsIgnoreCase("reload")) {
                MIYamlFiles.loadConfig();
                MIYamlFiles.loadGroups();
                sender.sendMessage(ChatColor.DARK_GREEN + "MultiInv configs reloaded!");
            } else if(command.equalsIgnoreCase("import")) {
                if(importInventories()) {
                    sender.sendMessage(ChatColor.DARK_GREEN + "MultiInv flat files converted to mysql!");
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "I'm sorry, something isn't set up right... Import aborted.");
                }
                
            } else if(command.equalsIgnoreCase("mvimport")) {
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
                    List<WorldGroupProfile> mvgroups = mvinventories.getGroupManager().getGroups();
                    MultiInv.log.info("No groups.yml found. Creating example file...");
                    YamlConfiguration groups = new YamlConfiguration();
                    MIYamlFiles.getGroups().clear();
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
                        for(OfflinePlayer player1 : Bukkit.getServer().getOfflinePlayers()) {
                            PlayerProfile playerdata = mvgroup.getPlayerData(ProfileTypes.SURVIVAL, player1);
                            if(playerdata != null && playerdata.get(Sharables.INVENTORY) != null) {
                                ItemStack[] inventory = playerdata.get(Sharables.INVENTORY);
                                ItemStack[] armor = playerdata.get(Sharables.ARMOR);
                                Integer health = playerdata.get(Sharables.HEALTH);
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
                                    MIYamlFiles.con.saveInventory(player, group, new MIInventory(inventory, armor, effects), "SURVIVAL");
                                    MIYamlFiles.con.saveHealth(player, group, health);
                                    MIYamlFiles.con.saveHunger(player, group, hunger);
                                    MIYamlFiles.con.saveSaturation(player, group, saturation);
                                    MIYamlFiles.con.saveExperience(player, group, totalexp);
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
                                    MIYamlFiles.con.saveInventory(player, group, new MIInventory(inventory, armor, effects), "ADVENTURE");
                                } else {
                                    MIPlayerFile config = new MIPlayerFile(player1, group);
                                    config.saveInventory(new MIInventory(inventory, armor, effects), "ADVENTURE");
                                }
                            }
                        }
                    }
                    for(World world : Bukkit.getWorlds()) {
                        String worldName = world.getName();
                        if(!MIYamlFiles.getGroups().containsKey(worldName)) {
                            WorldProfile worldprofile = mvinventories.getWorldManager().getWorldProfile(worldName);
                            for(OfflinePlayer player1 : Bukkit.getServer().getOfflinePlayers()) {
                                PlayerProfile playerdata = worldprofile.getPlayerData(ProfileTypes.SURVIVAL, player1);
                                if(playerdata != null && playerdata.get(Sharables.INVENTORY) != null) {
                                    ItemStack[] inventory = playerdata.get(Sharables.INVENTORY);
                                    ItemStack[] armor = playerdata.get(Sharables.ARMOR);
                                    Integer health = playerdata.get(Sharables.HEALTH);
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
                                        MIYamlFiles.con.saveInventory(player, worldName, new MIInventory(inventory, armor, effects), "SURVIVAL");
                                        MIYamlFiles.con.saveHealth(player, worldName, health);
                                        MIYamlFiles.con.saveHunger(player, worldName, hunger);
                                        MIYamlFiles.con.saveSaturation(player, worldName, saturation);
                                        MIYamlFiles.con.saveExperience(player, worldName, totalexp);
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
                                        MIYamlFiles.con.saveInventory(player, worldName, new MIInventory(inventory, armor, effects), "ADVENTURE");
                                    } else {
                                        MIPlayerFile config = new MIPlayerFile(player1, worldName);
                                        config.saveInventory(new MIInventory(inventory, armor, effects), "ADVENTURE");
                                    }
                                }
                            }
                        }
                    }
                    // Once the groups are loaded, let's load them into MultiInv
                    MIYamlFiles.parseGroups(groups);
                    // Once the import is done let's disable MultiVerse-Inventories.
                    Bukkit.getPluginManager().disablePlugin(mvinventories);
                    sender.sendMessage(ChatColor.DARK_GREEN + "Multiverse-Inventories inventories imported successfuly!");
                    sender.sendMessage(ChatColor.DARK_GREEN + "Please disable/delete Multiverse-Inventories now.");
                }
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
            return true;
        }
        return false;
    }
}
