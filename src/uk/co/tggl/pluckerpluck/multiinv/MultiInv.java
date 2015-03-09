package uk.co.tggl.pluckerpluck.multiinv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.tux2mc.debugreport.DebugReport;

import uk.co.tggl.pluckerpluck.multiinv.command.MICommand;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;
import uk.co.tggl.pluckerpluck.multiinv.logger.MILogger;
import uk.co.tggl.pluckerpluck.multiinv.util.UUIDFetcher;

/**
 * Created by IntelliJ IDEA. User: Pluckerpluck Date: 17/12/11 Time: 11:58 To change this template use File | Settings | File Templates.
 */
public class MultiInv extends JavaPlugin {
    
    // Initialize logger (auto implements enable/disable messages to console)
    public static MILogger log;
    public int xpversion = 0;
    private MultiInvAPI api;
    
    boolean importing = false;
    
    public DebugReport dreport = null;
    
    private ArrayList<String> grouplist = new ArrayList<String>();
    
    static MultiInv instance;
    
    // Listeners
    MIPlayerListener playerListener;
    
    @Override
    public void onDisable() {
        MIYamlFiles.saveLogoutWorlds();

        //If we save on quit we also want to save on disable!
		if(MIYamlFiles.saveonquit) {
			for(Player player : getServer().getOnlinePlayers()) {
				String currentworld = MIPlayerListener.getGroup(player.getLocation().getWorld());
	        	if(!player.hasPermission("multiinv.enderchestexempt")) {
	                // Load the enderchest inventory for this world from file.
	                playerListener.saveEnderchestState(player, currentworld);
	            }
	            if(!player.hasPermission("multiinv.exempt")) {
	                // Load the inventory for this world from file.
	                playerListener.savePlayerState(player, currentworld);
	            }
			}
			//Let's make sure all the files get saved before shutdown!
			if(MIYamlFiles.usesql && MIYamlFiles.con != null) {
				MIYamlFiles.con.run();
			}
		}
	
    }
    
    @Override
    public void onEnable() {
    	instance = this;
        // Initialize Logger
        log = new MILogger();
        
        // Load yaml files
        MIYamlFiles.loadConfig();
        MIYamlFiles.loadGroups();
        MIYamlFiles.loadPlayerLogoutWorlds();
        
        // Adding in metrics
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch(IOException e) {
            // Failed to submit the stats :-(
        }
        
        // An easy way to set the default logging levels
        if(MIYamlFiles.config.contains("loglevel")) {
            try {
                log.setLogLevel(MILogger.Level.valueOf(MIYamlFiles.config.getString("loglevel").toUpperCase()));
            } catch(Exception e) {
                log.warning("Log level value invalid! Valid values are: NONE, SEVERE, WARNING, INFO and DEBUG.");
                log.warning("Setting log level to INFO.");
                log.setLogLevel(MILogger.Level.INFO);
            }
        } else {
            // Set a sane level for logging
            log.setLogLevel(MILogger.Level.INFO);
        }
        
        // Initialize listeners
        playerListener = new MIPlayerListener(this);
        
        // Register required events
        
        //Is it GlowStone or Spigot?
        boolean isglowstone = false;
		File glowstoneproperties = new File("config/glowstone.yml");
		if(glowstoneproperties.exists()) {
			isglowstone = true;
		}
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
        
		Matcher mcmatch;
		if(isglowstone) {
			Pattern pmcversion = Pattern.compile("(\\d+)\\.(\\d+)\\.?(\\d*)");
			mcmatch = pmcversion.matcher(getServer().getVersion());
		}else {
			String[] cbversionstring = getServer().getVersion().split(":");
			Pattern pmcversion = Pattern.compile("(\\d+)\\.(\\d+)\\.?(\\d*)");
			mcmatch = pmcversion.matcher(cbversionstring[1]);
		}
		
        if(mcmatch.find()) {
        try {
			int majorversion = Integer.parseInt(mcmatch.group(1));
			int minorversion = Integer.parseInt(mcmatch.group(2));
            if(majorversion == 1) {
                if(minorversion > 2) {
                    xpversion = 1;
                    log.info("MC 1.3 or above found, enabling version 2 XP handling.");
                } else {
                    log.info("MC 1.2 or below found, enabling version 1 XP handling.");
                }
            } else if(majorversion > 1) {
                xpversion = 1;
                log.info("MC 1.3 or above found, enabling version 2 XP handling.");
            }
        } catch(Exception e) {
            log.severe("Unable to get server version! Inaccurate XP handling may occurr!");
            log.severe("Server Version String: " + getServer().getVersion());
        }
        }else {
            log.severe("Unable to get server version! Inaccurate XP handling may occurr!");
            log.severe("Server Version String: " + getServer().getVersion());
        }
        
        api = new MultiInvAPI(this);
        if(!MIYamlFiles.usesql) {
            File groupsfolder = new File(getDataFolder(), "Groups");
            if(groupsfolder.exists()) {
            	//Let's convert!
            	log.info("Older data folder detected. Converting users to UUID in the background, please wait... Players will not be able to log in until conversion is complete.");
            	getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
        			
        			@Override
        			public synchronized void run() {
                    	convertToUUID();
        			}
        		});
            }
        }else if(MIYamlFiles.con != null) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, MIYamlFiles.con, 20, 20);
        }
        
        getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			
			@Override
			public void run() {
				MIYamlFiles.saveLogoutWorlds();
			}
		}, 60, 20);
        scanWorlds();
        loadReportPlugin();
    }
    
    private void loadReportPlugin() {
    	if(Bukkit.getPluginManager().isPluginEnabled("DebugReport")) {
    		dreport = DebugReport.getInstance();
    	}
    }
    
    private synchronized void convertToUUID() {
    	setIsImporting(true);
    	ConcurrentHashMap<String, UUID> cacheduuids = new ConcurrentHashMap<String, UUID>();
        File groupsfolder = new File(getDataFolder(), "Groups");
        File uuidgroupsfolder = new File(getDataFolder(), "UUIDGroups");
        groupsfolder.renameTo(uuidgroupsfolder);
        if(uuidgroupsfolder.exists() && uuidgroupsfolder.isDirectory()) {
        	File[] groups = uuidgroupsfolder.listFiles();
        	for(File gfolder : groups) {
        		if(gfolder.isDirectory()) {
        			File[] users = gfolder.listFiles();
        			LinkedList<String> uncachedplayers = new LinkedList<String>();
        			for(int i = 0; i < users.length; i++) {
        				File user = users[i];
        				if(user.isFile()) {
        					String filename = user.getName();
        					if(filename.endsWith(".yml")) {
        						String username = filename.substring(0, filename.lastIndexOf("."));
        						if(!cacheduuids.containsKey(username)) {
        							uncachedplayers.add(username);
        						}
        					}
        				}
        			}
        			boolean first = true;
        			while(uncachedplayers.size() > 0) {
        				LinkedList<String> playerlist = new LinkedList<String>();
        				for(int i = 0; i < uncachedplayers.size() && i < 100; i++) {
        					playerlist.add(uncachedplayers.remove());
        				}
        				UUIDFetcher fetcher = new UUIDFetcher(playerlist);

        				try {
							Map<String, UUID> result = fetcher.call();
        					cacheduuids.putAll(result);
        					if(first) {
        						first = false;
        					}else {
        						try {
        							wait(100);
        						} catch (InterruptedException e1) {
        							
        						}
        					}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        			}
        			for(File user : users) {
        				if(user.isFile()) {
        					String filename = user.getName();
        					if(filename.endsWith(".ec.yml")) {
        						String username = filename.substring(0, filename.indexOf("."));
        						log.debug("Converting " + username + "'s enderchest file.");
        						UUID uuid = cacheduuids.get(username);
        						if(uuid != null) {
            						File newname = new File(user.getParent(), uuid.toString() + ".ec.yml");
            						user.renameTo(newname);
        						}else {
        							log.warning(username + " doesn't have a UUID! Skipping player's enderchest file.");
        						}
        					}else if(filename.endsWith(".yml")) {
        						String username = filename.substring(0, filename.lastIndexOf("."));
        						log.debug("Converting " + username + "'s inventory file.");
        						UUID uuid = cacheduuids.get(username);
        						if(uuid != null) {
            						File newname = new File(user.getParent(), uuid.toString() + ".yml");
            						user.renameTo(newname);
        						}else {
        							log.warning(username + " doesn't have a UUID! Skipping player's inventory file.");
        						}
        					}
        				}
        			}
        		}
        	}
        }
        setIsImporting(false);
    	log.info("Conversion complete!");
    }
    
    public MultiInvAPI getAPI() {
        return api;
    }
    
    public int[] getXP(int totalxp) {
        int level = 0;
        int leftoverexp = totalxp;
        int xpneededforlevel = 0;
        if(xpversion == 1) {
            xpneededforlevel = 17;
            while(leftoverexp >= xpneededforlevel) {
                level++;
                leftoverexp -= xpneededforlevel;
                if(level >= 16) {
                    xpneededforlevel += 3;
                }
            }
            // We only have 2 versions at the moment
        } else {
            xpneededforlevel = 7;
            boolean odd = true;
            while(leftoverexp >= xpneededforlevel) {
                level++;
                leftoverexp -= xpneededforlevel;
                if(odd) {
                    xpneededforlevel += 3;
                    odd = false;
                } else {
                    xpneededforlevel += 4;
                    odd = true;
                }
            }
        }
        return new int[]{level, leftoverexp, xpneededforlevel};
    }
    
    public int getTotalXP(int level, float xp) {
        int atlevel = 0;
        int totalxp = 0;
        int xpneededforlevel = 0;
        if(xpversion == 1) {
            xpneededforlevel = 17;
            while(atlevel < level) {
                atlevel++;
                totalxp += xpneededforlevel;
                if(atlevel >= 16) {
                    xpneededforlevel += 3;
                }
            }
            // We only have 2 versions at the moment
        } else {
            xpneededforlevel = 7;
            boolean odd = true;
            while(atlevel < level) {
                atlevel++;
                totalxp += xpneededforlevel;
                if(odd) {
                    xpneededforlevel += 3;
                    odd = false;
                } else {
                    xpneededforlevel += 4;
                    odd = true;
                }
            }
        }
        totalxp = (int) (totalxp + (xp * xpneededforlevel));
        return totalxp;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        MICommand.command(args, sender, this);
        return true;
    }
    
    public void scanWorlds() {
    	grouplist.clear();
    	List<World> worlds = Bukkit.getServer().getWorlds();
    	for(World world : worlds) {
    		String group = playerListener.getGroup(world);
    		if(!grouplist.contains(group)) {
    			grouplist.add(group);
    		}
    	}
    }
    
    public void addWorld(World world) {
    	String group = playerListener.getGroup(world);
		if(!grouplist.contains(group)) {
			grouplist.add(group);
		}
    }
    
    public ArrayList<String> getAllGroups() {
    	return grouplist;
    }
    
    public synchronized boolean isImporting() {
    	return importing;
    }
    
    public synchronized void setIsImporting(boolean iimport) {
    	importing = iimport;
    }
    
    public static MultiInv getPlugin() {
    	return instance;
    }
}
