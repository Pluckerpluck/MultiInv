/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.World;
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

    public MultiInvConverter(MultiInv plugin) {
        this.plugin = plugin;
    }
    
    public boolean convertFromOld(){
        boolean success = true;
        if(!parseOldShares()){
            success = false;
        }
        return success;
    }
    
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
}
