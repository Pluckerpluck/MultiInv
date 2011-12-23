package uk.co.tggl.pluckerpluck.multiinv.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import uk.co.tggl.pluckerpluck.multiinv.inventory.MIInventory;

import java.io.File;

/**
 * Class that contains all the configuration file methods
 */
public class MIPlayerFile {
    //final private Configuration playerFile;
    final private YamlConfiguration playerFile;
    final private File file;

    public MIPlayerFile(Player player, String group) {
        // Find and load configuration file for the player
        File dataFolder =  Bukkit.getServer().getPluginManager().getPlugin("MultiInv").getDataFolder();
        File worldsFolder = new File(dataFolder, "Groups");
        file = new File(worldsFolder, group + File.separator + player.getName() + ".yml");

        playerFile = new YamlConfiguration();
        load();
    }

    private void load(){
        if (file.exists()){
            try{
                playerFile.load(file);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
                save();
        }
    }

    private void save(){
        try{
            playerFile.save(file);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Load particular inventory for specified player from specified group
    public MIInventory getInventory(String inventoryName){
        // Get stored string from configuration file
        MIInventory inventory;
        String inventoryString = playerFile.getString(inventoryName, null);
        inventory = new MIInventory(inventoryString);
        return inventory;
    }

    public void saveInventory(MIInventory inventory, String inventoryName){
        String inventoryString = inventory.toString();
        playerFile.set(inventoryName, inventoryString);
        save();
    }

    public int getHealth(){
        int health = playerFile.getInt("health", 20);
        if (health <= 0 || health > 20) {
            health = 20;
        }
        return health;
    }

    public void saveHealth(int health){
        playerFile.set("health", health);
        save();
    }

    public GameMode getGameMode(){
        String gameModeString = playerFile.getString("gameMode", null);
        GameMode gameMode = null;
        if ("CREATIVE".equalsIgnoreCase(gameModeString)){
            gameMode = GameMode.CREATIVE;
        }else if ("SURVIVAL".equalsIgnoreCase(gameModeString)){
            gameMode = GameMode.SURVIVAL;
        }
        return gameMode;
    }

    public void saveGameMode(GameMode gameMode){
        playerFile.set("gameMode", gameMode.toString());
        save();
    }


}
