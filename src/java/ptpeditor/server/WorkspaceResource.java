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
     * Creates a new instance of WorkspaceResource
     */
    public WorkspaceResource() {
    
    }

    public static String getResourceBase() {
        return "C:\\Users/Mitch/Documents/PTPworkspace";
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
