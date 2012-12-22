package uk.co.tggl.pluckerpluck.multiinv.logger;

/**
 * Created with IntelliJ IDEA. User: Pluckerpluck Date: 29/04/12
 */
public interface Handler {
    
    void info(String message);
    
    void warning(String message);
    
    void severe(String message);
    
    void debug(String message);
    
}
