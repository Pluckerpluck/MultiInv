/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Pluckerpluck
 */
public class MultiInvConverter {
    
    final MultiInv plugin;
    private boolean invConversionInProgress;

    public MultiInvConverter(MultiInv plugin) {
        this.plugin = plugin;
        invConversionInProgress = false;
    }
    
    public boolean convertFromOld(){
        boolean success = true;
        if(!parseOldShares()){
            success = false;
        }
        File file = new File(plugin.getDataFolder(), "Worlds");
        searchFolders(file);
        if (invConversionInProgress) {
            MultiInv.log.info("Converting old inventories completed");
            invConversionInProgress = false;
        }
        return success;
    }

    /*
     *  Convert old shares to new shares
     *
     */

    private boolean parseOldShares() {
        boolean success = true;
        File file = new File(plugin.getDataFolder(), "shares.properties");
        if (file.exists()){
            MultiInv.log.info("[" + MultiInv.pluginName + "] Found old shares file. Converting...");
            File newFile = new File(plugin.getDataFolder(), "shares.yml");
            Configuration ymlFile = new Configuration(newFile);
            ymlFile.load();
            List<World> worldsO = plugin.getServer().getWorlds();
            Set<String> keys = MultiInvProperties.getAllKeys(file);
            for (String key : keys) {
                String value = MultiInvProperties.loadFromProperties(file, key);
                if (plugin.getServer().getWorld(key) == null || plugin.getServer().getWorld(value) == null) {
                    MultiInv.log.info("[" + MultiInv.pluginName + "] Sharing " + key + " to " + value + " is invalid");
                    success = false;
                }
                List<String> list = ymlFile.getStringList(value, null);
                if (list.isEmpty()){
                    list = new ArrayList<String>();
                    list.add(value);
                    list.add(key);
                }else{
                    list.add(key);
                }
                ymlFile.setProperty(value, list);
            }

            ymlFile.setProperty("creativeGroups", null);
            ymlFile.save();
            if (success){
                if(!file.delete()){
                    MultiInv.log.warning("[" + MultiInv.pluginName + "] Unable to delete shares.properties. Manual deletion required.");
                }
                MultiInv.log.info("[" + MultiInv.pluginName + "] Conversion completed. Continuing...");
            }else{
                MultiInv.log.severe("[" + MultiInv.pluginName + "] Due to some invalid shares conversion has not completed");
                MultiInv.log.severe("[" + MultiInv.pluginName + "] Please ensure shares.yml is correctly set up and remove the shares.properties if so");
                MultiInv.log.severe("[" + MultiInv.pluginName + "] Plugin will now shut down for safety");
            }           
        }
        return success;
    }

    /*
     *  Convert old .data files to new .yml files
     *
     */

    private void searchFolders(File file) {
        if (file.isDirectory()) {
            String internalNames[] = file.list();
            for (String name : internalNames) {
                searchFolders(new File(file.getAbsolutePath() + File.separator + name));
            }
        } else {
            String[] fileParts = file.getName().split("\\.");
            if ("data".equals(fileParts[1])) {
                if (!invConversionInProgress) {
                    MultiInv.log.info("Starting old inventory conversion...");
                    invConversionInProgress = true;
                }
                File fileDest = new File(file.getParent(), fileParts[0] + ".yml");
                boolean success1 = convertOldInventories(file, fileDest);
                boolean success2 = convertHealth(file, fileDest);
                if (success1 && success2) {
                    file.delete();
                }
            }
        }
    }

    private boolean convertOldInventories(File file, File fileDest){
        String inventoryName = "MultiInvInventory";
        String tmpInventory = MultiInvProperties.loadFromProperties(file, inventoryName);
        if (tmpInventory != null) {
            Configuration ymlFile = new Configuration(fileDest);
            ymlFile.load();
            ymlFile.setProperty("survival", tmpInventory);
            ymlFile.setProperty("creative", tmpInventory);
            return ymlFile.save();
        }
        return false;
    }

    private boolean convertHealth(File file, File fileDest){
        String parent = file.getParent();
        int index = parent.indexOf(File.separator);
        String folder;
        if (index >= 0){
            folder = parent.substring(index);
        }else{
            folder = parent;
        }
        String healthString = MultiInvProperties.loadFromProperties(file, "health:" + folder, "20");
        int health = Integer.parseInt(healthString);
        Configuration ymlFile = new Configuration(fileDest);
        ymlFile.load();
        ymlFile.setProperty("health", health);
        return ymlFile.save();
    }


}
