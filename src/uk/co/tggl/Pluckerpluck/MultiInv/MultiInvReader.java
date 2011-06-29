package uk.co.tggl.Pluckerpluck.MultiInv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.World;
import org.bukkit.util.config.Configuration;

public class MultiInvReader {
    public final MultiInv plugin;
    public JarFile jar;
    HashMap<String, Boolean> config = new HashMap<String, Boolean>();

    public MultiInvReader(MultiInv instance, File file) {
        plugin = instance;
        try{
        	jar = new JarFile(file);
        }catch (Exception e) {
			System.out.println("["+ MultiInv.pluginName + "] Failed miserably (Location 'Error 001')");
			System.out.println("["+ MultiInv.pluginName + "] Please contact Pluckerpluck with the error location");
		}
    }
    
    public File loadFileFromJar(String string) {
		// load file, creating it first if it doesn't exist
		File file = new File(plugin.getDataFolder(), string);
		if (!file.canRead()) try {
			file.getParentFile().mkdirs();
			JarEntry entry = jar.getJarEntry(string);
			InputStream is = jar.getInputStream(entry);
			FileOutputStream os = new FileOutputStream(file);
			byte[] buf = new byte[(int)entry.getSize()];
			is.read(buf, 0, (int)entry.getSize());
			os.write(buf);
			os.close();
		} catch (Exception e) {
			MultiInv.log.info("["+ MultiInv.pluginName + "] Could not create/load configuration file");
			return null;
		}
		return file;

	}
    
    public boolean parseShares() {
    	File sharesFile = loadFileFromJar("shares.properties");
    	if (sharesFile == null){
    		return false;
    	}
        List<World> worldsO = plugin.getServer().getWorlds();
        String[] serverWorlds = new String[worldsO.size()];
        int i = 0;
        for (World world : worldsO){
        	serverWorlds[i] = world.getName();
        	i++;
        }
        for (String key : MultiInvProperties.getAllKeys(sharesFile)){
        	String value = MultiInvProperties.loadFromProperties(sharesFile, key);
        	if (plugin.getServer().getWorld(key) == null || plugin.getServer().getWorld(value) == null){
        		MultiInv.log.info("["+ MultiInv.pluginName + "] Sharing " + key + " to " + value + " is invalid");
        	}
        	plugin.sharesMap.put(key, value);
        }
        return true;
    }
    
    public void loadConfig(){
    	loadFileFromJar("config.yml");
    	plugin.getConfiguration().load();
    	Configuration cfg = plugin.getConfiguration();
        this.config.put("health", cfg.getBoolean("health", true));
    	this.config.put("inventories", cfg.getBoolean("inventories", true));
        String ignoreList = cfg.getString("ignore");
        if (ignoreList != null && !ignoreList.matches(" *")){
            String[] names = cfg.getString("ignore").split(" *, *");
            for (String name : names){
                plugin.ignoreList.add(name.toLowerCase());
            }
        }
    }
}
