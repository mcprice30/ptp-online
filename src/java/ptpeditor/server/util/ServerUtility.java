/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server.util;

/**
 * The ServerUtility class is designed to hold various helper methods for the
 * server. These methods are of various types, but are generally simply things
 * that don't fall within their own class.
 * @author Mitch
 */
public class ServerUtility {

    private static OS osType = null;
    /**
     * Returns the OS enum corresponding to the server's current operating
     * system.
     * @return The operating system type this server is running.
     */
    public static OS getOS() {
        
        String osName = System.getProperty("os.name");
        
        if(osType != null) {
            return osType;
        }
        
        if(osName.startsWith("Windows")) {
            osType = OS.WINDOWS;
            return OS.WINDOWS;
        }
        
        if(osName.startsWith("Mac") || osName.startsWith("Linux")) {
            osType = OS.UNIX;
            return OS.UNIX;
        }
        
        osType = OS.OTHER;
        return OS.OTHER;
    }
    
    /**
     *  This enum contains the various OS types that the server is capable of
     *  handling. This matters in terms of executing various commands.
     */
    public enum OS {
        WINDOWS, UNIX, OTHER;
    }
}
