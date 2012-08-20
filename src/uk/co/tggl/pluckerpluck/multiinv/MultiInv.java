package uk.co.tggl.pluckerpluck.multiinv;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.tggl.pluckerpluck.multiinv.command.MICommand;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIEnderChest;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;
import uk.co.tggl.pluckerpluck.multiinv.logger.MILogger;

/**
 * Created by IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 17/12/11
 * Time: 11:58
 * To change this template use File | Settings | File Templates.
 */
public class MultiInv extends JavaPlugin {

    // Initialize logger (auto implements enable/disable messages to console)
    public static MILogger log;
    public int xpversion = 0;
    private MultiInvAPI api;

    // Listeners
    MIPlayerListener playerListener;

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {
        // Initialize Logger
        log = new MILogger();

        // Load yaml files
        MIYamlFiles.loadConfig();
        MIYamlFiles.loadGroups();
        MIYamlFiles.loadPlayerLogoutWorlds();
        
        //Adding in metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        
        //An easy way to set the default logging levels
        if(MIYamlFiles.config.contains("loglevel")) {
        	try {
            	log.setLogLevel(MILogger.Level.valueOf(MIYamlFiles.config.getString("loglevel").toUpperCase()));
        	}catch(Exception e) {
        		log.warning("Log level value invalid! Valid values are: NONE, SEVERE, WARNING, INFO and DEBUG.");
        		log.warning("Setting log level to INFO.");
            	log.setLogLevel(MILogger.Level.INFO);
        	}
        }else {
        	//Set a sane level for logging
        	log.setLogLevel(MILogger.Level.INFO);
        }

        // Initialize listeners
        playerListener = new MIPlayerListener(this);

        // Register required events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new MIEnderChest(this), this);
        pm.registerEvents(playerListener, this);
        String[] cbversionstring = getServer().getVersion().split(":");
        String[] versionstring = cbversionstring[1].split("\\.");
        try{
        	int majorversion = Integer.parseInt(versionstring[0].trim());
        	int minorversion = Integer.parseInt(versionstring[1].trim());
        	if(majorversion == 1) {
        		if(minorversion > 2) {
        			xpversion = 1;
        			log.info("MC 1.3 or above found, enabling version 2 XP handling.");
        		}else {
        			log.info("MC 1.2 or below found, enabling version 1 XP handling.");
        		}
        	}else if(majorversion > 1) {
        		xpversion = 1;
    			log.info("MC 1.3 or above found, enabling version 2 XP handling.");
        	}
        }catch (Exception e) {
        	log.severe("Unable to get server version! Inaccurate XP handling may occurr!");
        	log.severe("Server Version String: " + getServer().getVersion());
        }

        api = new MultiInvAPI(this);
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
    		//We only have 2 versions at the moment
    	}else {
    		xpneededforlevel = 7;
        	boolean odd = true;
        	while(leftoverexp >= xpneededforlevel) {
        		level++;
        		leftoverexp -= xpneededforlevel;
        		if(odd) {
        			xpneededforlevel += 3;
        			odd = false;
        		}else {
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
    		//We only have 2 versions at the moment
    	}else {
    		xpneededforlevel = 7;
        	boolean odd = true;
        	while(atlevel < level) {
        		atlevel++;
        		totalxp += xpneededforlevel;
        		if(odd) {
        			xpneededforlevel += 3;
        			odd = false;
        		}else {
        			xpneededforlevel += 4;
        			odd = true;
        		}
        	}
    	}
    	totalxp = (int) (totalxp + (xp*xpneededforlevel));
    	return totalxp;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        MICommand.command(args, sender, this);
        return true;
    }

}
