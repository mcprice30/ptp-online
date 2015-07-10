/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server;

import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * REST Web Service
 *
 * @author Mitch
 */
@Path("workspace/{userId}")
public class WorkspaceResource {


    /**
     * Returns the absolute path to the workspace.
     * @param userId The user's id.
     * @return The path.
     */
    public static String getResourceBase(String userId) {
        return "C:\\Users/Mitch/Documents/PTPworkspace/" + userId;
    }
    
    /**
     * Returns the absolute path to the workspace, specially formatted to 
     * account for Cygwin's directory link (if applicable).
     * @return The absolute path.
     */
    public static String getResourceBaseCygwin(String userId) {
        if(ServerUtility.getOS() == ServerUtility.OS.WINDOWS) {
            String base = getResourceBase(userId);
            //replacing "C:\\" with "/cygdrive/c/"
            return "/cygdrive/c/" + base.substring(3, base.length());
        } else {
            return getResourceBase(userId);
        }
    }
    
    /**
     * Retrieves representation of an instance of ptpeditor.server.WorkspaceResource
     * @param userId The ID of the user.
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("text/plain")
    public String getText(@PathParam("userId")String userId) {
        return getResourceBase(userId);
    }
}
