package uk.co.tggl.pluckerpluck.multiinv.command;

/**
 * Created by IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 19/12/11
 * Time: 22:58
 * To change this template use File | Settings | File Templates.
 */
public class MICommand {

    public static void command(String[] strings){
        String command = strings[0];

        // Populate a new args array
        String[] args = new String[strings.length - 1];
        for (int i = 1; i < strings.length; i++) {
            args[i-1] = strings[i];
        }
    }
}
