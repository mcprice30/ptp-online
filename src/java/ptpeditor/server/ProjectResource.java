/*
 *  PTP Online Editor
 *  Developed by Mitch Price under supervision of Dr. Jeff Overbey
 *  Auburn University, 2015.
 *
 *  This project would not be possible without the following open source projects:
 *  CodeMirror Online Editor, by Marijn Haverbeke and others    (codemirror.net)   
 *  jquery-console by chrisdone.    (github.com/chrisdone/jquery-console)
 *  jquery File Tree by Cory LaViska    (abeautifulsite.net)
 */
package ptpeditor.server;

import java.nio.file.Files;
import java.nio.file.Paths;
//import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * REST Web Service
 *
 * @author Mitch
 */
@Path("project/{userId}")
public class ProjectResource {


    private String activeProject;
    
    /**
     * Creates a new instance of NewProjectResource
     */
    public ProjectResource() {
        activeProject = "";
    }

    /**
     * Checks whether the project name is taken or not. If it is already taken,
     * it responds saying so to the user. If the project name is not taken yet,
     * the method attempts to create a directory for the project and responds
     * to the user with a response message.
     * @return an instance of java.lang.String
     * @param userId The id assigned to the user.
     * @param projectID The id assigned to the project.
     * @param projectName The name of the project, and it's directory.
     */
    @GET
    @Path("create/{projectID}/{projectName}")
    @Produces("text/plain")
    public String createProject(@PathParam("userId")String userId, @PathParam("projectID")String projectID, @PathParam("projectName")String projectName) {
        
        String workspaceLocation = WorkspaceResource.getResourceBase(userId); 
        String projectLocation = workspaceLocation + "/" + projectName;
        
        
        java.nio.file.Path p = Paths.get(projectLocation);
        
        if(Files.exists(p)) {
            return "Project could not be created!\nA project with the same name already exists.";
        } else {
            File dir = new File(projectLocation);
            boolean success = dir.mkdirs();
            if(success) {
                return "Project Created!";
            } else {
                return "Project not Created!";
            }
            /*
            try {
               Process ps = Runtime.getRuntime().exec("mkdir " + "\"" +  projectLocation + "\"");
               BufferedReader in = new BufferedReader(new InputStreamReader(ps.getInputStream()));
               String line;
               while((line = in.readLine()) != null) {
                   System.out.println(line);
               }
               in = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
               while((line = in.readLine()) != null) {
                   System.out.println(line);
               } 
            } catch (IOException e) {
                return "Error making project!";
            }  
            */
        }  
    }

    /**
     * HTTP GET request server side component.
     * Lists every project in the directory. Projects are separated by new lines.
     * @param userId The id of the user.
     * @return The list of projects in the workspace.
     */
    @GET
    @Path("list")
    @Produces("text/plain")
    public String getProjectList(@PathParam("userId")String userId) {
        String output = "";
        try {
            String commandLocation = WorkspaceResource.getResourceBase(userId) + "/*/";
            Process p = Runtime.getRuntime().exec("ls -d " + commandLocation);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = in.readLine()) != null) {
                line = line.trim();
                line = line.substring(0, line.length() - 1);
                line = line.substring(line.lastIndexOf('/') + 1, line.length());
                output += line + "\n";
            }
        } catch (IOException e) {
            return "Error finding projects!";
        }
        
        return output;
    }
    
    /**
     *  DEPRECATED.
     *  HTTP POST request server side component.
     *  Sets the active project. This will return a string describing the 
     *  success of the action.
     *  @param projectName The name of the project to set active.
     */
    @POST
    @Path("setactive/{projectName}")
    public void getActiveProject(@PathParam("projectName")String projectName) {
        activeProject = projectName;
    }
}
