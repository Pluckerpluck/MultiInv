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
import java.util.concurrent.ConcurrentHashMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 19/12/11
 * Time: 22:11
 * To change this template use File | Settings | File Templates.
 */
public class MIYamlFiles {
    public static YamlConfiguration config;
    public static YamlConfiguration playerlogoutmap;
    public static HashMap<String, String> groups = new HashMap<String, String>();
    public static HashMap<String, String> creativegroups = new HashMap<String, String>();
    public static ConcurrentHashMap<String, String> logoutworld = new ConcurrentHashMap<String, String>();
    
    public static SqlConnector con;

    public static void loadConfig(){
        config = loadYamlFile("config.yml");
        if (config == null){
            config = new YamlConfiguration();
            //setConfigDefaults(config);
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
        }else{
        	String worldtypes = config.getString("splitHealth");
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
        	}else {
        		creativegroups.clear();
        		List<String> worlds = config.getStringList("creativeGroups");
                for (String world : worlds){
                    creativegroups.put(world, "creative");
                }
                String allowenderchests = config.getString("allowEnderChestPlacement");
                if(allowenderchests == null || allowenderchests.equals("")) {
                	config.set("allowEnderChestPlacement", false);
                	saveYamlFile(config, "config.yml");
                }
                if(config.getBoolean("useSQL", false)) {
                    try {
                    	String url = "jdbc:mysql://" + config.getString("sql.host", "localhost") + ":" + config.getString("sql.port", "3306") + "/" + config.getString("sql.database", "database") + "?autoReconnect=true";
                        Connection connect = DriverManager.getConnection(url, config.getString("sql.username", "username"), config.getString("sql.password", "password"));
                        con = new SqlConnector(connect, config.getString("sql.prefix", "multiinv_"));

                    } catch (SQLException ex) {
                        MultiInv.log.warning("Could not establish connection to the database! User inventories won't be saved!");
                    }
                }
                
        	}
        }
    }
    
    public static void loadPlayerLogoutWorlds() {
    	playerlogoutmap = loadYamlFile("logoutworld.yml");
        if (playerlogoutmap == null){
        	playerlogoutmap = new YamlConfiguration();
            saveYamlFile(playerlogoutmap, "logoutworld.yml");
        }else{
        	Map<String, Object> playermap = playerlogoutmap.getValues(false);
        	Set<String> players = playermap.keySet();
        	for(String player : players) {
        		logoutworld.put(player, playermap.get(player).toString());
        	}
        }
    }
    
    public static void savePlayerLogoutWorld(String player, String world) {
    	logoutworld.put(player, world);
    	playerlogoutmap.set(player, world);
        saveYamlFile(playerlogoutmap, "logoutworld.yml");
    }

    public static void loadGroups(){
        YamlConfiguration groups = loadYamlFile("groups.yml");
        if (groups == null) {
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

    private static void parseGroups(Configuration config){
    	groups.clear();
        Set<String> keys = config.getKeys(false);
        for (String group : keys){
            List<String> worlds = config.getStringList(group);
            for (String world : worlds){
                groups.put(world, group);
            }
        }
    }

    private static void saveYamlFile(YamlConfiguration yamlFile, String fileLocation){
        File dataFolder =  Bukkit.getServer().getPluginManager().getPlugin(DefaultVals.pluginName).getDataFolder();
        File file = new File(dataFolder, fileLocation);

        try{
            yamlFile.save(file);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static YamlConfiguration loadYamlFile(String file){
        File dataFolder =  Bukkit.getServer().getPluginManager().getPlugin(DefaultVals.pluginName).getDataFolder();
        File yamlFile = new File(dataFolder, file);

        YamlConfiguration config = null;
        if (yamlFile.exists()){
            try{
                config = new YamlConfiguration();
                config.load(yamlFile);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return config;
    }

    /*private static void setConfigDefaults(Configuration config){
        config.addDefault("useSQL", false);
        config.addDefault("splitHealth", true);
        config.addDefault("splitHunger", true);
    }*/

}
