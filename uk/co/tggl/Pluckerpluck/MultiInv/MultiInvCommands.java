package uk.co.tggl.Pluckerpluck.MultiInv;

import java.io.File;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MultiInvCommands {

    public final MultiInv plugin;

    public MultiInvCommands(MultiInv instance) {
        plugin = instance;
    }

    /*
     * Different return integers represent different errors
     * 0 -> Success
     * 1 -> Failed permissions
     * 2 -> Lack of inputs
     * 3 -> No command performed
     */
    int playerCommand(CommandSender sender, String[] split, String permission) {
        Player player = (Player) sender;
        String Str = split[0];
        if (!plugin.permissionCheck((Player) sender, permission)) {
            sender.sendMessage("You do not have permissions to use " + Str);
            return 1;
        }
        if (Str.equalsIgnoreCase("delete")) {
            if (split.length > 1) {
                deleteCommand(sender, split);
            } else {
                sender.sendMessage("Please supply player name to delete the inventories");
            }
            return 0;
        } else if (Str.equalsIgnoreCase("debug")) {
            if (split.length >= 2) {
                debugCommand(sender, split);
                return 0;
            }
            return 2;
        } else if (Str.equalsIgnoreCase("ignore")) {
            Player ignored = player;
            if (split.length >= 2) {
                ignored = plugin.getServer().getPlayer(split[1]);
                if (!ignored.getName().equalsIgnoreCase(split[1])) {
                    sender.sendMessage("Player cannot be found. He must be online");
                    return 0;
                }
            }
            ignoreCommand(sender, ignored);
        } else if (Str.equalsIgnoreCase("unignore")) {
            String ignored = player.getName();
            if (split.length >= 2) {
                ignored = split[1];
            }
            unignoreCommand(sender, ignored);
            return 0;
        } else if (Str.equalsIgnoreCase("addShare")) {
            if (split.length >= 3) {
                String minorWorld = split[1];
                String majorWorld = split[2];
                shareWorlds(minorWorld, majorWorld);
                return 0;
            }
            sender.sendMessage("/MultiInv addShare <minorWorld> <majorWorld>");
            return 2;
        } else if (Str.equalsIgnoreCase("removeShare")) {
            if (split.length >= 2) {
                String minorWorld = split[1];
                removeShareWorld(minorWorld);
            }
            sender.sendMessage("/MultiInv removeShare <minorWorld>");
            return 2;
        }
        return 3;
    }

    /* Below here are the actual commands called that perform the required action */
    private void deleteCommand(CommandSender sender, String[] split) {
        int invs = plugin.deletePlayerInventories(split[1]);
        if (invs != 0) {
            if (invs == 1) {
                sender.sendMessage("Deleted 1 invetory for player " + split[1]);
            } else {
                sender.sendMessage("Deleted " + invs + " invetories for player " + split[1]);
            }
        } else {
            sender.sendMessage("Player " + split[1] + " does not exist");
        }
    }

    private void debugCommand(CommandSender sender, String[] split) {
        if (split[1].equalsIgnoreCase("start")) {
            if (split.length >= 3 && split[2].equalsIgnoreCase("show") && sender instanceof Player) {
                plugin.debugger.addDebugger((Player) sender);
                sender.sendMessage("Debugging started (shown)");
            } else {
                plugin.debugger.startDebugging();
                sender.sendMessage("Debugging started (hidden)");
            }
        } else if (split[1].equalsIgnoreCase("stop")) {
            plugin.debugger.stopDebugging();
            sender.sendMessage("Debugging stopped");
        } else if (split[1].equalsIgnoreCase("save")) {
            plugin.debugger.saveDebugLog();
            sender.sendMessage("Debugging saved");
        }
        return;
    }

    private void ignoreCommand(CommandSender sender, Player player) {
        String playerName = player.getName();
        if (plugin.ignoreList.contains(playerName)) {
            sender.sendMessage("Player is already being ignored");
            return;
        }
        plugin.ignoreList.add(playerName);
        sender.sendMessage(playerName + " is now being ignored");
        return;
    }

    private void unignoreCommand(CommandSender sender, String playerName) {
        if (plugin.ignoreList.contains(playerName)) {
            plugin.ignoreList.remove(playerName);
            sender.sendMessage(playerName + " is no longer ignored");
            return;
        }
        sender.sendMessage(playerName + " was not being ignored");
        return;
    }

    private void shareWorlds(String minorWorld, String majorWorld) {
        String file = plugin.getDataFolder() + File.separator + "shares.properties";
        MultiInvProperties.saveToProperties(file, minorWorld, majorWorld);
        plugin.sharesMap.put(minorWorld, majorWorld);
    }

    private void removeShareWorld(String minorWorld) {
        String file = plugin.getDataFolder() + File.separator + "shares.properties";
        MultiInvProperties.removeProperty(file, minorWorld, null);
        plugin.sharesMap.remove(minorWorld);
    }
}
