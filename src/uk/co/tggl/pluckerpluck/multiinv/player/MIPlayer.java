package uk.co.tggl.pluckerpluck.multiinv.player;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIEnderchestInventory;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

/**
 * Player class designed to store all information for each saved world
 */
public class MIPlayer implements Runnable {
    
    // Initialize final variables that define the MIPlayer
    private Player player;
    private PlayerInventory inventory;
    private Inventory enderchest;
    MultiInv plugin;
    
    ConcurrentHashMap<String, MIPlayerCache> cache = new ConcurrentHashMap<String, MIPlayerCache>();
    
    // Initialize (and assign) variables containing the initial state of an
    // MIPlayer
    
    public MIPlayer(Player player, MultiInv plugin, int precache) {
        this.player = player;
        this.plugin = plugin;
        inventory = player.getInventory();
        enderchest = player.getEnderChest();
        //Load world data asynchronously.
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this, precache);
    }
    
    public void removePlayer() {
    	player = null;
    }
    
    public void setPlayer(Player player) {
    	this.player = player;
        inventory = player.getInventory();
        enderchest = player.getEnderChest();
    }
    
    /*
     * -------------------- PlayerInventory methods --------------------
     */
    
    // Load methods that will load data into the game
    public void loadInventory(String group, String inventoryName) {
        if(!MIYamlFiles.separategamemodeinventories) {
            inventoryName = "SURVIVAL";
        }
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        MIInventory inventory = pcache.getInventory(inventoryName);
        inventory.loadIntoInventory(this.inventory);
        inventory.setPotionEffects(player);
    }
    
    public void saveInventory(String group, String inventoryName) {
        if(!MIYamlFiles.separategamemodeinventories) {
            inventoryName = "SURVIVAL";
        }
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        MIInventory inventory = new MIInventory(player);
        pcache.setInventory(inventory, inventoryName);
        if(MIYamlFiles.usesql) {
            MIYamlFiles.con.refreshConnection();
            MIYamlFiles.con.saveInventory(player, group, inventory,
                    inventoryName);
        } else {
            MIPlayerFile config = pcache.getFile();
            config.saveInventory(inventory, inventoryName);
        }
    }
    
    public void saveAll(String group, String inventoryName) {
        if(!MIYamlFiles.separategamemodeinventories) {
            inventoryName = "SURVIVAL";
        }
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        MIInventory inventory = new MIInventory(player);
        pcache.setInventory(inventory, inventoryName);
        pcache.setFoodlevel(player.getFoodLevel());
        pcache.setGm(player.getGameMode());
        pcache.setHealth(player.getHealth());
        pcache.setSaturation(player.getSaturation());
        pcache.setXp(player.getExp());
        pcache.setXpLevel(player.getLevel());
    	if(MIYamlFiles.usesql) {
            MIYamlFiles.con.refreshConnection();
            int totalxp = plugin.getTotalXP(player.getLevel(), player.getExp());
            MIYamlFiles.con.saveAll(player, group, inventory, inventoryName, totalxp,
            		player.getGameMode(), player.getHealth(), player.getFoodLevel(), player.getSaturation());
        } else {
            MIPlayerFile config = pcache.getFile();
            config.saveAll(inventory, inventoryName, player.getTotalExperience(),
                    player.getLevel(), player.getExp(), player.getGameMode(), player.getHealth(),
                    player.getFoodLevel(), player.getSaturation());
        }
    }
    
    /*
     * -------------------- PlayerEnderchestInventory methods --------------------
     */
    
    // Load methods that will load data into the game
    public void loadEnderchestInventory(String group, String inventoryName) {
        if(!MIYamlFiles.separategamemodeinventories) {
            inventoryName = "SURVIVAL";
        }
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        MIEnderchestInventory inventory = pcache.getEnderchest(inventoryName);
        inventory.loadIntoInventory(enderchest);
    }
    
    public void saveEnderchestInventory(String group, String inventoryName) {
        if(!MIYamlFiles.separategamemodeinventories) {
            inventoryName = "SURVIVAL";
        }
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        MIEnderchestInventory inventory = new MIEnderchestInventory(player);
        pcache.setEnderchest(inventory, inventoryName);
        if(MIYamlFiles.usesql) {
            MIYamlFiles.con.refreshConnection();
            MIYamlFiles.con.saveEnderchestInventory(player, group,
                    inventory, inventoryName);
        } else {
            MIPlayerFile config = pcache.getFile();
            config.saveEnderchestInventory(inventory, inventoryName);
        }
    }
    
    /*
     * -------------------- Other methods --------------------
     */
    
    public void loadHealth(String group) {
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        player.setHealth(pcache.getHealth());
    }
    
    /*
     * This is needed because of the new compatibility mode. The death teleport event has the health set to -980 or so making it throw an
     * error.
     */
    public void saveFakeHealth(String group, int value) {
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        pcache.setHealth(value);
        if(MIYamlFiles.usesql) {
            if(value < 0) {
                MIYamlFiles.con.saveHealth(player, group, 0);
            } else {
                MIYamlFiles.con.saveHealth(player, group, value);
            }
        } else {
            MIPlayerFile config = pcache.getFile();
            config.saveHealth(value);
        }
    }

    @Deprecated
    public void saveHealth(String group) {
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        pcache.setHealth(player.getHealth());
        if(MIYamlFiles.usesql) {
            if(player.getHealth() < 0) {
                MIYamlFiles.con.saveHealth(player, group, 0);
            } else {
                MIYamlFiles.con.saveHealth(player, group,
                        player.getHealth());
            }
        } else {
            MIPlayerFile config = new MIPlayerFile(player, group);
            config.saveHealth(player.getHealth());
        }
    }
    
    public void loadGameMode(String group) {
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        GameMode gm = pcache.getGm();
        if(gm != null) {
            player.setGameMode(gm);
        }
    }
    
    public void saveGameMode(String group) {
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        pcache.setGm(player.getGameMode());
        if(MIYamlFiles.usesql) {
            MIYamlFiles.con.saveGameMode(player, group,
                    player.getGameMode());
        } else {
            MIPlayerFile config = pcache.getFile();
            config.saveGameMode(player.getGameMode());
        }
    }
    
    public void loadHunger(String group) {
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        player.setFoodLevel(pcache.getFoodlevel());
        player.setSaturation(pcache.getSaturation());
    }
    
    public void saveFakeHunger(String group, int hunger, float saturation) {
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        pcache.setFoodlevel(hunger);
        pcache.setSaturation(saturation);
        if(MIYamlFiles.usesql) {
            MIYamlFiles.con.saveHunger(player, group, hunger);
            MIYamlFiles.con.saveSaturation(player, group, saturation);
        } else {
            MIPlayerFile config = pcache.getFile();
            config.saveHunger(hunger);
            config.saveSaturation(saturation);
        }
    }

    @Deprecated
    public void saveHunger(String group) {
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        pcache.setFoodlevel(player.getFoodLevel());
        pcache.setSaturation(player.getSaturation());
        if(MIYamlFiles.usesql) {
            MIYamlFiles.con.saveHunger(player, group,
                    player.getFoodLevel());
            MIYamlFiles.con.saveSaturation(player, group,
                    player.getSaturation());
        } else {
            MIPlayerFile config = pcache.getFile();
            config.saveHunger(player.getFoodLevel());
            config.saveSaturation(player.getSaturation());
        }
    }
    
    public void loadExperience(String group) {
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        player.setLevel(pcache.getXpLevel());
        player.setTotalExperience(pcache.getTotalXp());
        player.setExp(pcache.getXp());
    }
    
    @Deprecated
    public void saveExperience(String group) {
        MIPlayerCache pcache = cache.get(group);
        if(pcache == null) {
        
        	pcache = loadGroup(group);
        	cache.put(group, pcache);
        }
        pcache.setXpLevel(player.getLevel());
        pcache.setTotalXp(player.getTotalExperience());
        pcache.setXp(player.getExp());
        if(MIYamlFiles.usesql) {
            int totalxp = plugin.getTotalXP(player.getLevel(), player.getExp());
            MIYamlFiles.con.saveExperience(player, group, totalxp);
        } else {
            MIPlayerFile config = pcache.getFile();
            config.saveExperience(player.getTotalExperience(),
                    player.getLevel(), player.getExp());
        }
    }
    
    public MIPlayerCache loadGroup(String group) {
        MIPlayerCache pcache = new MIPlayerCache(player);
        if(!MIYamlFiles.separategamemodeinventories) {
            String inventoryName = "SURVIVAL";
	        if(MIYamlFiles.usesql) {
	            MIInventory inventory = MIYamlFiles.con.getInventory(
	                    player, group, inventoryName);
	            pcache.setInventory(inventory, inventoryName);

	            pcache.setInventory(MIYamlFiles.con.getInventory(player, group, inventoryName), inventoryName);
	            pcache.setEnderchest(MIYamlFiles.con.getEnderchestInventory(player, group, inventoryName), inventoryName);
	            pcache.setFoodlevel(MIYamlFiles.con.getHunger(player, group));
	            pcache.setSaturation(MIYamlFiles.con.getSaturation(player, group));
	            pcache.setHealth(MIYamlFiles.con.getHealth(player, group));
                int totalxp = MIYamlFiles.con.getTotalExperience(player, group);
                int[] xp = plugin.getXP(totalxp);
                pcache.setTotalXp(totalxp);
                pcache.setXpLevel(xp[0]);
                pcache.setXp((float) ((float) xp[1] / (float) xp[2]));
                pcache.setGm(MIYamlFiles.con.getGameMode(player, group));
	        } else {
	            MIPlayerFile config = new MIPlayerFile(player, group);
	            MIInventory inventory = config.getInventory(inventoryName);
	            pcache.setInventory(inventory, inventoryName);
	            pcache.setEnderchest(config.getEnderchestInventory(inventoryName), inventoryName);
	            pcache.setFoodlevel(config.getHunger());
	            pcache.setGm(config.getGameMode());
	            pcache.setHealth(config.getHealth());
	            pcache.setSaturation(config.getSaturation());
	            pcache.setXp(config.getExperience());
	            pcache.setXpLevel(config.getLevel());
	            pcache.setTotalXp(config.getTotalExperience());
	            pcache.setFile(config);
	        }
        }else {
        	if(MIYamlFiles.usesql) {
        		for(GameMode gm : GameMode.values()) {
        			String inventoryName = gm.toString();
    	            MIInventory inventory = MIYamlFiles.con.getInventory(
    	                    player, group, inventoryName);
    	            pcache.setInventory(inventory, inventoryName);

    	            pcache.setInventory(MIYamlFiles.con.getInventory(player, group, inventoryName), inventoryName);
    	            pcache.setEnderchest(MIYamlFiles.con.getEnderchestInventory(player, group, inventoryName), inventoryName);
        		}
	            pcache.setFoodlevel(MIYamlFiles.con.getHunger(player, group));
	            pcache.setSaturation(MIYamlFiles.con.getSaturation(player, group));
	            pcache.setHealth(MIYamlFiles.con.getHealth(player, group));
                int totalxp = MIYamlFiles.con.getTotalExperience(player, group);
                int[] xp = plugin.getXP(totalxp);
                pcache.setXpLevel(xp[0]);
                pcache.setXp((float) ((float) xp[1] / (float) xp[2]));
                pcache.setGm(MIYamlFiles.con.getGameMode(player, group));
	        } else {
	            MIPlayerFile config = new MIPlayerFile(player, group);
	            for(GameMode gm : GameMode.values()) {
	            	String inventoryName = gm.toString();
		            MIInventory inventory = config.getInventory(inventoryName);
		            pcache.setInventory(inventory, inventoryName);
		            pcache.setEnderchest(config.getEnderchestInventory(inventoryName), inventoryName);
	            }
	            pcache.setFoodlevel(config.getHunger());
	            pcache.setGm(config.getGameMode());
	            pcache.setHealth(config.getHealth());
	            pcache.setSaturation(config.getSaturation());
	            pcache.setXp(config.getExperience());
	            pcache.setXpLevel(config.getLevel());
	            pcache.setFile(config);
	        }
        }
        return pcache;
    }

	@Override
	public void run() {
		ArrayList<String> groups = plugin.getAllGroups();
		for(String group : groups) {
			MIPlayerCache pcache = loadGroup(group);
			cache.put(group, pcache);
		}
	}
}
