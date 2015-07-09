/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server;

import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * REST Web Service
 *
 * @author Mitch
 */
@Path("workspace")
public class WorkspaceResource {


    /**
     * Returns the absolute path to the workspace.
     * @return The path.
     */
    public static String getResourceBase() {
        return "C:\\Users/Mitch/Documents/PTPworkspace";
    }
    
    /**
     * Returns the absolute path to the workspace, specially formatted to 
     * account for Cygwin's directory link (if applicable).
     * @return The absolute path.
     */
    public static String getResourceBaseCygwin() {
        if(ServerUtility.getOS() == ServerUtility.OS.WINDOWS) {
            String base = getResourceBase();
            //replacing "C:\\" with "/cygdrive/c/"
            return "/cygdrive/c/" + base.substring(3, base.length());
        } else {
            return getResourceBase();
        }
    }
    
    /**
     * Retrieves representation of an instance of ptpeditor.server.WorkspaceResource
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("text/plain")
    public String getText() {
        return getResourceBase();
    }
}
