package uk.co.tggl.pluckerpluck.multiinv.logger;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Pluckerpluck
 * Date: 29/04/12
 */
public class MILogger {
    private Level LogLevel = Level.DEBUG;
    private ArrayList<Handler> handlers = new ArrayList<Handler>();

    public enum Level{
        NONE (0),
        SEVERE (1),
        WARNING (2),
        INFO (3),
        DEBUG (4);

        private int value;
        Level(int value){
            this.value = value;
        }

    }

    public MILogger() {
        handlers.add(new ConsoleHandler());
    }

    public Level getLogLevel() {
        return LogLevel;
    }

    public void setLogLevel(Level logLevel) {
        LogLevel = logLevel;
    }

    public void addHandler(Handler handler) {
        if (find(handler.getClass()) == null){
            handlers.add(handler);
        }
    }

    public void removeHandler(Handler handler){
        Handler h = find(handler.getClass());
        if (h != null){
            handlers.remove(h);
        }
    }

    public void info(String message){
        if (LogLevel.value >= Level.INFO.value){
            for (Handler handler : handlers){
                handler.info(message);
            }
        }
    }

    public void warning(String message){
        if (LogLevel.value >= Level.WARNING.value){
            for (Handler handler : handlers){
                handler.warning(message);
            }
        }
    }

    public void severe(String message){
        if (LogLevel.value >= Level.SEVERE.value){
            for (Handler handler : handlers){
                handler.severe(message);
            }
        }
    }

    public void debug(String message){
        if (LogLevel.value >= Level.DEBUG.value){
            for (Handler handler : handlers){
                handler.debug(message);
            }
        }
    }



    private <T> T find(Class<T> clazz){
        for(Handler o : handlers){
            if (o != null && o.getClass() == clazz)
            {
                return clazz.cast(o);
            }
        }

        return null;
    }

}