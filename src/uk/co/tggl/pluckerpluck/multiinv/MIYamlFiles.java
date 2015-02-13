package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import uk.co.tggl.pluckerpluck.multiinv.mysql.SqlConnector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA. User: Pluckerpluck Date: 19/12/11 Time: 22:11 To change this template use File | Settings | File Templates.
 */
public class MIYamlFiles {
    
    protected static YamlConfiguration config;
    public static YamlConfiguration playerlogoutmap;
    private static HashMap<String,String> worldgroups = new HashMap<String,String>();
    public static HashMap<String,String> creativegroups = new HashMap<String,String>();
    public static ConcurrentHashMap<UUID,String> logoutworld = new ConcurrentHashMap<UUID,String>();
    
    public static boolean separategamemodeinventories = true;
    public static boolean usesql = false;
    public static boolean compatibilitymode = false;
    public static boolean controlgamemode = true;
    public static boolean xpfix = false;
    public static boolean saveonquit = false;
    
    static boolean logoutdirty = false;
    
    public static SqlConnector con;
    
    public static void loadConfig() {
        config = loadYamlFile("config.yml");
        if(config == null) {
            config = new YamlConfiguration();
            // setConfigDefaults(config);
            config.set("useSQL", false);
            config.set("splitHealth", true);
            config.set("splitHunger", true);
            config.set("controlGamemode", true);
            config.set("separateGamemodeInventories", true);
            config.set("creativeGroups", new String[]{"creative"});
            config.set("sql.host", "localhost");
            config.set("sql.port", "3306");
            config.set("sql.username", "username");
            config.set("sql.password", "password");
            config.set("sql.database", "database");
            config.set("sql.prefix", "multiinv_");
            creativegroups.clear();
            creativegroups.put("creative", "creative");
            saveYamlFile(config, "config.yml");
        } else {
            String worldtypes = config.getString("splitHealth");
            String squit = config.getString("SaveInventoryOnQuit");
            if(worldtypes == null || worldtypes.equals("")) {
                config.set("useSQL", false);
                config.set("splitHealth", true);
                config.set("splitHunger", true);
                config.set("controlGamemode", true);
                config.set("separateGamemodeInventories", true);
                config.set("creativeGroups", new String[]{"creative"});
                config.set("sql.host", "localhost");
                config.set("sql.port", "3306");
                config.set("sql.username", "username");
                config.set("sql.password", "password");
                config.set("sql.database", "database");
                config.set("sql.prefix", "multiinv_");
                creativegroups.clear();
                creativegroups.put("creative", "creative");
                saveYamlFile(config, "config.yml");
            } else if(squit == null || squit.equals("")) {
                config.set("SaveInventoryOnQuit", false);
                saveYamlFile(config, "config.yml");
            } else {
                creativegroups.clear();
                List<String> worlds = config.getStringList("creativeGroups");
                for(String world : worlds) {
                    creativegroups.put(world, "creative");
                }
                String compatibilitymode = config.getString("compatibilityMode");
                if(compatibilitymode == null || compatibilitymode.equals("")) {
                    config.set("compatibilityMode", false);
                    saveYamlFile(config, "config.yml");
                }
                String xpfix = config.getString("xpfix");
                if(xpfix == null || xpfix.equals("")) {
                    config.set("xpfix", false);
                    saveYamlFile(config, "config.yml");
                }
                separategamemodeinventories = MIYamlFiles.config.getBoolean("separateGamemodeInventories", true);
                MIYamlFiles.compatibilitymode = MIYamlFiles.config.getBoolean("compatibilityMode");
                controlgamemode = config.getBoolean("controlGamemode", true);
                MIYamlFiles.xpfix = config.getBoolean("xpfix", true);
                saveonquit = config.getBoolean("SaveInventoryOnQuit", false);
                if(config.getBoolean("useSQL", false)) {
                	usesql = true;
                    try {
                        String username = config.getString("sql.username", "username");
                        String password = config.getString("sql.password", "password");
                        String url = "jdbc:mysql://" + config.getString("sql.host", "localhost") + ":" + config.getString("sql.port", "3306") + "/"
                                + config.getString("sql.database", "database") + "?autoReconnect=true";
                        Connection connect = DriverManager.getConnection(url, config.getString("sql.username", "username"),
                                config.getString("sql.password", "password"));
                        con = new SqlConnector(connect, config.getString("sql.prefix", "multiinv_"), url, username, password, MultiInv.getPlugin());
                        
                    } catch(SQLException ex) {
                        MultiInv.log.warning("Could not establish connection to the database! User inventories won't be saved!");
                    }
                }
                
            }
        }
    }
    
    public static HashMap<String,String> getGroups() {
        return worldgroups;
    }
    
    public static void loadPlayerLogoutWorlds() {
        playerlogoutmap = loadYamlFile("logoutworld.yml");
        if(playerlogoutmap == null) {
            playerlogoutmap = new YamlConfiguration();
            saveYamlFile(playerlogoutmap, "logoutworld.yml");
        } else {
            Map<String,Object> playermap = playerlogoutmap.getValues(false);
            Set<String> players = playermap.keySet();
            for(String player : players) {
            	try {
            		UUID uuid = UUID.fromString(player);
                    logoutworld.put(uuid, playermap.get(player).toString());
            	}catch (Exception ex) {
            		//Logout world database reset
            	}
            }
        }
    }
    
    public static void savePlayerLogoutWorld(UUID uuid, String world) {
		MultiInv.log.debug("Saving player logout world for " + uuid + " World: " + world);
        logoutworld.put(uuid, world);
        playerlogoutmap.set(uuid.toString(), world);
        logoutdirty = true;
    }
    
    public static void saveLogoutWorlds() {
    	if(logoutdirty) {
            saveYamlFile(playerlogoutmap, "logoutworld.yml");
            logoutdirty = false;
    	}
    }
    
    public static void loadGroups() {
        YamlConfiguration groups = loadYamlFile("groups.yml");
        if(groups == null) {
            MultiInv.log.info("No groups.yml found. Creating example file...");
            groups = new YamlConfiguration();
            
            ArrayList<String> exampleGroup = new ArrayList<String>();
            exampleGroup.add("world");
            exampleGroup.add("world_nether");
            exampleGroup.add("world_the_end");
            groups.set("exampleGroup", exampleGroup);
            saveYamlFile(groups, "groups.yml");
        }
        parseGroups(groups);
        
    }
    
    public static void parseGroups(Configuration config) {
        worldgroups.clear();
        Set<String> keys = config.getKeys(false);
        for(String group : keys) {
            List<String> worlds = config.getStringList(group);
            for(String world : worlds) {
                worldgroups.put(world, group);
            }
        }
    }
    
    public static void saveYamlFile(YamlConfiguration yamlFile, String fileLocation) {
        File dataFolder = Bukkit.getServer().getPluginManager().getPlugin(DefaultVals.pluginName).getDataFolder();
        File file = new File(dataFolder, fileLocation);
        
        try {
            yamlFile.save(file);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static YamlConfiguration loadYamlFile(String file) {
        File dataFolder = Bukkit.getServer().getPluginManager().getPlugin(DefaultVals.pluginName).getDataFolder();
        File yamlFile = new File(dataFolder, file);
        
        YamlConfiguration config = null;
        if(yamlFile.exists()) {
            try {
                config = new YamlConfiguration();
                config.load(yamlFile);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return config;
    }
}
