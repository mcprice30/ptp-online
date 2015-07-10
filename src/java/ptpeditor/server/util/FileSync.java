/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import ptpeditor.server.WorkspaceResource;

/**
 * This class is used specifically for syncing files from the server to the
 * deployment supercomputer.
 * @author Mitch
 */
public class FileSync {
    
    /**
     * Delegates file synching to a different helper methods depending on the OS.
     * @param ip The ip address of the supercomputer to sync with.
     * @param username The username to login as.
     * @param password The password corresponding to the given username. 
     * @param directory The directory on the supercomputer to write to.
     * @param projectName The name of the project to sync.
     * @param userId The id assigned to the user.
     * Currently, this field is unused.
     * @return A message pertaining to the outcome of the action.
     */
    public static String syncFiles(String ip, String username, String password, String directory, String projectName, String userId) {
        if(ip == null || username == null) {
            return "[ERROR]: Server information not entered!";
        }
        
        ServerUtility.OS serverOS = ServerUtility.getOS();
        if(serverOS == ServerUtility.OS.WINDOWS) {
            return syncFilesWindows(ip, username, password, directory, projectName, userId);
        } else if (serverOS == ServerUtility.OS.UNIX) {
            return syncFilesUnix(ip, username, password, directory, projectName, userId);
        } else  {
            return "[ERROR]: Operating system not supported!";
        }
    }
    
    /**
     * Syncs files from a windows server.
     * @param ip The ip address of the supercomputer to sync with.
     * @param username The username to login as.
     * @param password The password corresponding to the given username.
     * @param directory The directory on the supercomputer to sync to.
     * @param projectName The name of the project to sync.
     * @param userId The id assigned to the user.
     * Currently, this field is unused.
     * @return A message pertaining to the outcome of the action.
     */
    public static String syncFilesWindows(String ip, String username, String password, String directory, String projectName, String userId){
        try {
            String command = "C:\\cygwin64\\bin\\rsync -avz --delete -e C:\\cygwin64\\bin\\ssh " +
                    WorkspaceResource.getResourceBaseCygwin(userId) + "/" + projectName + " "           
                    + username + "@" + ip + ":" + directory;                  

            System.out.println(command);
            Process p = Runtime.getRuntime().exec(command);
            
            
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader pError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            
            String line;
            String output = "";
            while((line = in.readLine()) != null) {
                //line = (char) in.read();
                output += line + "\n";
                System.out.println(line);
            }            
            while((line = pError.readLine()) != null) {
                output += line + "\n";
                System.out.println(line);
            }
            return output;
        } catch (IOException e) {
            System.out.println(e.toString());
            return "COULD NOT SYNC!";
        }
    }
    
    /**
     * Syncs files from a unix style server.
     * @param ip The ip address of the supercomputer to sync with.
     * @param username The username to login as.
     * @param password The password corresponding to the given username.
     * @param directory The directory to sync to on the supercomputer.
     * @param projectName The name of the project to sync.
     * @param userId The id assigned to the user.
     * Currently, this field is unused.
     * @return A message pertaining to the outcome of the action.
     */
    public static String syncFilesUnix(String ip, String username, String password, String directory, String projectName, String userId) {
        try {
            String command = "rsync -avz --delete -e ssh " +
                    WorkspaceResource.getResourceBaseCygwin(userId) + "/" + projectName + " "           
                    + username + "@" + ip + ":" + directory;                  

            System.out.println(command);
            Process p = Runtime.getRuntime().exec(command);
            
            
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader pError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            
            String line;
            String output = "";
            while((line = in.readLine()) != null) {
                //line = (char) in.read();
                output += line + "\n";
                System.out.println(line);
            }            
            while((line = pError.readLine()) != null) {
                output += line + "\n";
                System.out.println(line);
            }
            return output;
        } catch (IOException e) {
            System.out.println(e.toString());
            return "COULD NOT SYNC!";
        }
    }
}
