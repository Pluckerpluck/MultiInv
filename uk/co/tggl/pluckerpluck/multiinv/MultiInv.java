package uk.co.tggl.pluckerpluck.multiinv;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.tggl.pluckerpluck.multiinv.command.MICommand;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 17/12/11
 * Time: 11:58
 * To change this template use File | Settings | File Templates.
 */
public class MultiInv extends JavaPlugin {

    public static final Logger log = Logger.getLogger("Minecraft");

    // Listeners
    MIPlayerListener playerListener;

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {
        // Get the description file containing plugin information
        PluginDescriptionFile pdfFile = this.getDescription();

        // Load yaml files
        MIYamlFiles.loadConfig();
        MIYamlFiles.loadGroups();

        // Initialize listeners
        playerListener = new MIPlayerListener();

        // Register required events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_CHANGED_WORLD, playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_GAME_MODE_CHANGE, playerListener, Event.Priority.Highest, this);
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        MICommand.command(args);
        return true;
    }

}
