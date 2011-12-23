package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.util.config.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MultiInvReader {

    public final MultiInv plugin;
    public JarFile jar;
    final HashMap<String, Boolean> config = new HashMap<String, Boolean>();

    public MultiInvReader(MultiInv instance, File file) {
        plugin = instance;
        try {
            jar = new JarFile(file);
        } catch (Exception e) {
            System.out.println("[" + MultiInv.pluginName + "] Failed miserably (Location 'Error 001')");
            System.out.println("[" + MultiInv.pluginName + "] Please contact Pluckerpluck with the error location");
        }
    }

    public File loadFileFromJar(String string) {
        // load file, creating it first if it doesn't exist
        File file = new File(plugin.getDataFolder(), string);
        if (!file.canRead()) {
            try {
                file.getParentFile().mkdirs();
                JarEntry entry = jar.getJarEntry(string);
                InputStream is = jar.getInputStream(entry);
                FileOutputStream os = new FileOutputStream(file);
                byte[] buf = new byte[(int) entry.getSize()];
                is.read(buf, 0, (int) entry.getSize());
                os.write(buf);
                os.close();
            } catch (Exception e) {
                MultiInv.log.info("[" + MultiInv.pluginName + "] Could not create/load configuration file");
                return null;
            }
        }
        return file;

    }

    public boolean parseShares() {
        boolean success = false;
        File sharesFile = loadFileFromJar("shares.yml");
        if (sharesFile != null) {
            Configuration ymlFile = new Configuration(sharesFile);
            ymlFile.load();
            for (String key : ymlFile.getKeys()) {
                final List<String> stringList = ymlFile.getStringList(key, null);
                if ("creativeGroups".equals(key) && plugin.fileReader.config.get("worldTypes")){
                    for (String group : stringList){
                        MultiInv.creativeGroups.add(group);
                    }  
                }else if (stringList != null) {
                    for (String group : stringList){
                        MultiInv.sharesMap.put(group, key);
                    }
                }
            }
            success = true;
        }
        return success;
    }
    
   

    public void loadConfig() {
        loadFileFromJar("config.yml");
        plugin.getConfiguration().load();
        Configuration cfg = plugin.getConfiguration();
        config.put("worldTypes", cfg.getBoolean("worldTypes", true));
        config.put("isHealthSplit", cfg.getBoolean("isHealthSplit", true));
        config.put("isHungerSplit", cfg.getBoolean("isHungerSplit", true));
        config.put("invertList", cfg.getBoolean("invertList", false));
        config.put("restoreGameModes", cfg.getBoolean("restoreGameModes", true));
        //config.put("isExpSplit", cfg.getBoolean("isExpSplit", false));
        config.put("isExpSplit", false);
        List<String> ignoreList = cfg.getStringList("ignore", null);
        for (String name : ignoreList) {
            MultiInv.ignoreList.add(name.toLowerCase());
        }
    }
}
