package uk.co.tggl.pluckerpluck.multiinv2;

import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MultiInvDebugger {

    public final MultiInv plugin;
    private boolean debugging = false;
    private final ArrayList<String> debuggers = new ArrayList<String>();
    private final ArrayList<String> logHistory = new ArrayList<String>();
    private final boolean debugLogging = false;

    public MultiInvDebugger(MultiInv instance) {
        plugin = instance;
    }

    public void addDebugger(Player player) {
        if (!(debuggers.contains(player.getName()))) {
            debuggers.add(player.getName());
            debugging = true;
        }
    }

    public void removeDebugger(Player player) {
        if (debuggers.contains(player.getName())) {
            debuggers.remove(player.getName());
        }
    }

    public void startDebugging() {
        debugging = true;
    }

    public void stopDebugging() {
        saveDebugLog();
        debuggers.clear();
        debugging = false;
    }

    public void saveDebugLog() {
        if (debugLogging && debugging) {
            logToFile();
            logHistory.clear();
        }
    }

    public void debugEvent(MultiInvEnums.MultiInvEvent event, String[] args) {
        String message;
        String message2;
        String dividerStart = "#-----";
        String dividerEnd = "-----#";
        if (debugging) {
            switch (event) {
                case WORLD_CHANGE:
                    message = dividerStart + args[0] + " changed world" + dividerEnd;
                    int shareNumber = shareCheck(args[1], args[2]);
                    switch (shareNumber) {
                        case 0:
                            message2 = "Moved from " + args[1] + " to " + args[2];
                            break;
                        case 1:
                            message2 = "Moved from " + args[1] + "* to " + args[2];
                            break;
                        case 2:
                            message2 = "Moved from " + args[1] + " to " + args[2] + "*";
                            break;
                        case 3:
                            message2 = "Moved from " + args[1] + "* to " + args[2] + "*";
                            break;
                        case 4:
                            message2 = "Moved from " + args[1] + " to " + args[2] + " (Shared)";
                            break;
                        default:
                            message2 = "Error with WORLD_CHANGE debug event";
                            break;
                    }
                    sendDebuggersMessage(message);
                    sendDebuggersMessage(message2);
                    break;
                case INVENTORY_SAVE:
                    message = "Saved '" + args[0] + "'";
                    sendDebuggersMessage(message);
                    break;
                case INVENTORY_LOAD:
                    message = "Loaded '" + args[0] + "'";
                    sendDebuggersMessage(message);
                    break;
                case INVENTORY_NEW:
                    message = "Creating new inventory for '" + args[0] + "'";
                    sendDebuggersMessage(message);
                    break;
                case FILE_SAVE:
                    message = "Saved inventories to file";
                    sendDebuggersMessage(message);
                    break;
                case FILE_LOAD:
                    message = "Loaded inventories from file";
                    sendDebuggersMessage(message);
                    break;
                case PLAYERS_UPDATE:
                    message = "Players list updated";
                    sendDebuggersMessage(message);
                    break;
                case INVENTORY_DELETE:
                    message = "'" + args[0] + "' has been deleted";
                    sendDebuggersMessage(message);
                    break;
                case INVENTORY_ADDED:
                    message = "'" + args[0] + "' has been added";
                    sendDebuggersMessage(message);
                    break;
                case PLAYER_LOGIN:
                    message = dividerStart + args[0] + " logged in" + dividerEnd;
                    sendDebuggersMessage(message);
                    break;
                case PLAYER_LOGOUT:
                    message = dividerStart + args[0] + " logged out" + dividerEnd;
                    sendDebuggersMessage(message);
                    break;
                case INVENTORY_LOAD_NULL:
                    message = "Loaded empty inventory for '" + args[0] + "'";
                    sendDebuggersMessage(message);
                    break;
                case INVENTORY_LOAD_NEW:
                    message = "Loaded new inventory for '" + args[0] + "'";
                    sendDebuggersMessage(message);
                    break;
                case INVENTORY_DELETE_UNUSED:
                    message = "'" + args[0] + "' has been deleted as it is unused";
                    sendDebuggersMessage(message);
                    break;
                default:
                    message = "Error with " + event.toString() + " debug event";
                    sendDebuggersMessage(message);
                    break;

            }
        }
    }

    private void sendDebuggersMessage(String message) {
        if (!(debuggers.isEmpty())) {
            for (String player : debuggers) {
                Player player2 = plugin.getServer().getPlayer(player);
                if (player2 != null) {
                    player2.sendMessage(message);
                }
            }
        }
        if (debugLogging) {
            logHistory.add(message);
        }
    }

    private int shareCheck(String world1, String world2) {
        if (MultiInv.sharesMap.containsKey(world1)) {
            if (MultiInv.sharesMap.containsKey(world2)) {
                return 3;
            }
            return 1;
        }
        if (MultiInv.sharesMap.containsKey(world2)) {
            return 2;
        }
        return 0;
    }

    private void logToFile() {
        File file = new File("plugins" + File.separator + "MultiInv" + File.separator + "logs" + File.separator + "debugLog.txt");
        String parent = file.getParent();
        File dir = new File(parent);
        if (!dir.exists()) {
            dir.mkdir();
        }
        int i = 1;
        while (file.exists()) {
            file = new File("plugins" + File.separator + "MultiInv" + File.separator + "logs" + File.separator + "debugLog" + i + ".txt");
            i++;
        }
        if (!(logHistory.isEmpty())) {
            //Print to file
            try {
                // Create file 
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);
                for (String line : logHistory) {
                    if (line != null) {
                        out.write(line + System.getProperty("line.separator"));
                    }
                }
                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("MultiInv Error: " + e.getMessage());
            }
        }
    }
}
