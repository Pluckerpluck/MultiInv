package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * MultiInv for Bukkit
 *
 * @author Pluckerpluck
 */
public class MultiInv extends JavaPlugin {

    MultiInvPlayerListener playerListener;
    MultiInvWorldListener worldListener = new MultiInvWorldListener(this);
    MultiInvConverter versionCheck = new MultiInvConverter(this);
    MultiInvDebugger debugger = new MultiInvDebugger(this);
    MultiInvReader fileReader;
    MultiInvCommands commands = new MultiInvCommands(this);
    
    static final ConcurrentHashMap<String, String[]> currentInventories = new ConcurrentHashMap<String, String[]>();
    static final ConcurrentHashMap<String, String> sharesMap = new ConcurrentHashMap<String, String>();
    static final HashSet<String> creativeGroups = new HashSet<String>();
    static final HashSet<String> ignoreList = new HashSet<String>();
    static final Logger log = Logger.getLogger("Minecraft");
    static String pluginName;
    // 0 = unloaded, 1 = loaded successfully, 2 = loaded with errors
    int shares = 0;

    @Override
    public void onDisable() {
        for (Player player : this.getServer().getOnlinePlayers()) {
            MultiInvPlayerData.storeCurrentInventory(player, player.getWorld().getName());
        }

        debugger.saveDebugLog();
        log.info("[" + pluginName + "] Plugin disabled.");
    }

    @Override
    public void onEnable() {
        //File reader must be initialized here due to limitation with getFile()
        fileReader = new MultiInvReader(this, this.getFile());

        PluginDescriptionFile pdfFile = this.getDescription();
        pluginName = pdfFile.getName();
        if (!versionCheck.convertFromOld()){
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Loads config and gives it to the player listener
        fileReader.loadConfig();
        MultiInvPlayerData.storeConfig(fileReader.config);

        
        if (getServer().getOnlinePlayers().length > 0) {
            Boolean localShares = fileReader.parseShares();
            if (localShares) {
                log.info("[" + pluginName + "] Shared worlds loaded with no errors");
                this.shares = 1;
            }
            this.shares = 2;
        }else{
            fileReader.loadFileFromJar("shares.yml");
        }

        log.info("[" + pluginName + "] version " + pdfFile.getVersion() + " is enabled!");

        //Initialize classes
        playerListener = new MultiInvPlayerListener(this);


        // Event registration
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.WORLD_SAVE, worldListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_GAME_MODE_CHANGE, playerListener, Priority.Highest, this);

        //Permissions setup
        loadPermissions();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();
        return "multiinv".equals(commandName) && performCheck(sender, args);
    }

    boolean permissionCheck(CommandSender sender, String node) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return permissionCheck(player, node);
        }
        return true;
    }

    boolean permissionCheck(Player player, String node) {
        return permissionCheck(player, node, true);
    }

    boolean permissionCheck(Player player, String node, Boolean loud) {
        if (!player.hasPermission(node)) {
        	if (loud) {
        		player.sendMessage("You do not have permission to use this command");
        	}
        	return false;
        }
        return true;

    }

    private boolean performCheck(CommandSender sender, String[] split) {
        if (split.length == 0) {
            sender.sendMessage("Type a command to utilize MultiInv");
            return true;
        }
        if (sender instanceof Player) {
            /*
             * HashMap the maps command with the equivalent permission node
             */
            ConcurrentHashMap<String, String> commandPermissions = new ConcurrentHashMap<String, String>();
            // Basic commands (User power - None atm)

            // Ignore commands (Mod power)
            commandPermissions.put("ignore", "MultiInv.mod.ignore");
            commandPermissions.put("unignore", "MultiInv.mod.ignore");

            // Ignore commands (Admin power)
            commandPermissions.put("delete", "MultiInv.admin.delete");
            commandPermissions.put("addshare", "MultiInv.admin.shares");
            commandPermissions.put("removeshare", "MultiInv.admin.shares");
            commandPermissions.put("debug", "MultiInv.admin.debug");

            if (commandPermissions.containsKey(split[0])) {
                String permission = commandPermissions.get(split[0].toLowerCase());
                commands.playerCommand(sender, split, permission);
            }
        } else {
            sender.sendMessage("Console commands not yet avaliable");
        }
        return true;
    }

    public int deletePlayerInventories(String name) {
        int count;
        File file = new File(getDataFolder() + File.separator + "Worlds");
        count = searchFolders(file, name + ".data");
        return count;
    }

    private int searchFolders(File file, String search) {
        int count = 0;
        if (file.isDirectory()) {
            String internalNames[] = file.list();
            for (String name : internalNames) {
                count = count + searchFolders(new File(file.getAbsolutePath() + File.separator + name), search);
            }
        } else {
            if (file.getName().equals(search)) {
                file.delete();
                return count + 1;
            }
        }
        return count;
    }

    void loadPermissions() {
        Player[] online = getServer().getOnlinePlayers();
        if (online.length > 0) {
            for (Player player : online) {
                loadPermissions(player);
            }
        }
    }

    void loadPermissions(Player player) {
        if (ignoreList.contains(player.getName().toLowerCase())) {
            player.sendMessage("MultiInv is ignoring you");
        }

        if (!MultiInvPlayerData.existingPlayers.contains(player.getName())) {
            MultiInv.log.info("[" + MultiInv.pluginName + "] New player detected: " + player.getName());
            MultiInvPlayerData.existingPlayers.add(player.getName());
        }
        /* 
        if(permissionCheck(player, "MultiInv.state.ignore", true)){
        
        }
         * */

    }

    public void backDoor() {
        /* There's not actually a hidden back door
         * this was added to see if anyone reads the source.
         * For those that do post:
         * "Open source is great" 
         * or something along those lines in my thread
         * and if you do I'll treasure you in my heart more
         * than all the users who don't.
         */
    }
}
