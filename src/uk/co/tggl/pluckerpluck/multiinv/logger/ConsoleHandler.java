package uk.co.tggl.pluckerpluck.multiinv.logger;

import org.bukkit.Bukkit;
import uk.co.tggl.pluckerpluck.multiinv.DefaultVals;

import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 29/04/12
 */
public class ConsoleHandler implements Handler {
    private Logger log = Bukkit.getServer().getPluginManager().getPlugin(DefaultVals.pluginName).getLogger();
    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void warning(String message) {
        log.warning(message);
    }

    @Override
    public void severe(String message) {
        log.severe(message);
    }

    @Override
    public void debug(String message) {
        log.info("[DEBUG] " + message);
    }
}
