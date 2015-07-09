/* 
 *  PTP Online Editor
 *   Developed by Mitch Price under supervision of Dr. Jeff Overbey
 *   Auburn University, 2015.
 *   
 *   TODO: find an appropriate license and insert it here.
 *
 *   This project would not be possible without the following open source projects:
 *   CodeMirror Online Editor, by Marijn Haverbeke and others    (codemirror.net)   
 *   jquery-console by chrisdone.    (github.com/chrisdone/jquery-console)
 *   jquery File Tree by Cory LaViska    (abeautifulsite.net)
 */
package ptpeditor.server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.DataOutputStream;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.PathParam;

/**
 * The LoadServer will eventually take over entirely for the SaveServer
 * as the primary server websocket endpoint. Its purpose is to add save/load
 * functionality to the editor. The websocket lifecycle is designed to be tied
 * opening/creating a project to open the websocket and closing/deleting a 
 * project to close the websocket. Therefore, each websocket can be considered
 * to be project-specific, rather than user-specific. (A user may only have
 * one project open at a time).
 * @author Mitch
 */
@ServerEndpoint("/load/{projectName}")
public class LoadServer {
    
    private static final String SAVE_KEY = "SAVE";
    private static final String LOAD_KEY = "LOAD";
    private static final String BUILD_KEY = "BUILD";
    private static final String NEW_KEY = "NEW";
    private static final String DELETE_KEY = "DELETE";
    private static final String DELETE_PROJECT_KEY = "DELETE_PROJECT";
    private static final String SETTINGS_KEY = "SETTINGS";
    
    private static final String SAVE_RESPONSE = "S";
    private static final String LOAD_RESPONSE = "L";
    private static final String BUILD_RESPONSE = "B";
    private static final String NEW_RESPONSE = "N";
    private static final String DELETE_RESPONSE = "D";
    private static final String PROJECT_RESPONSE = "P";
    private static final String SETTINGS_RESPONSE = "G";
    private static final String ERROR_RESPONSE = "E";
    
    private static String ip = null;
    private static String username = null;
    private static String password = null;
    
    /**
     *  The onOpen message sends a message back to the client side once
     *  this endpoint of the Websocket is established.
     * @param session
     * @param projectName 
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("projectName")String projectName) {
        System.out.println(session.getId() + " has opened a connection"); 
        //System.out.println("Will save file: " + fileName + "." + fileType);
        try {
            session.getBasicRemote().sendText(PROJECT_RESPONSE + "Ready to load from: " + projectName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * The message from the client side of the websocket can tell the server
     * to do a variety of things. The action type that the client side wants the
     * server to perform is specified at the beginning of the message, and the file
     * to perform the action on is specified immediately afterwards. The server
     * response will begin with a single letter indicating the response type.
     * @param message
     * @param session
     * @param projectName 
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("projectName")String projectName) {
        //Breaking up the message into the various components: an action type,
        //a file to perform the action on, and (if applicable), a text payload.
        String[] messageComponents = message.split(" ");
        String actionType = messageComponents[0]; 
        String payload = "";
        String text = "";
        
        if(messageComponents.length > 1) {
            payload = messageComponents[1];
            if(messageComponents.length > 2) {
                int firstSpace = message.indexOf(' ');
                int secondSpace = message.indexOf(' ', firstSpace + 1);
                text = message.substring(secondSpace + 1, message.length());
            }
        }
        
        try {
            session.getBasicRemote().sendText(handleMessage(actionType, payload, text, projectName));
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }
    
    /**
     * When the websocket is closed, log the event and session ID to the server.
     * @param session
     * @param projectName 
     */
    @OnClose
    public void onClose(Session session, @PathParam("projectName")String projectName) {
        System.out.println("Session " + session.getId() + " has ended");
    }
    
    /**
     * Deals with the message by farming it out to a variety of subtasks
     * depending on the header's content.
     * @param header The type of action to perform.
     * @param payload An action file, settings, etc.
     * @param text If saving a file, the text to save.
     * @param projectName The name of the project.
     * @return The server's response.
     */
    public static String handleMessage(String header, String payload, String text, String projectName) {
        switch (header) {
            case LOAD_KEY:
                return performLoad(payload);
            case SAVE_KEY:
                return performSave(payload, text, projectName);
            case BUILD_KEY:
                return performBuild(payload);
            case NEW_KEY:
                return performCreate(payload);
            case DELETE_KEY:
                return performDelete(payload);
            case DELETE_PROJECT_KEY:
                return performDeleteProject(projectName);
            case SETTINGS_KEY:
                return performUpdateSettings(payload);
            default:
                return ERROR_RESPONSE + "Unknown Action!";
        }
    }
    
    /**
     * Opens a specified file and reads in the contents of the file.
     * @param actionFile The absolute path of the file to load.
     * @return The string contents of the file loaded.
     */
    public static String performLoad(String actionFile) {
        String response = "";    //The response. (Begins with response key).
        String line;                        //Reads in a single line at a time.
        try {
            BufferedReader in = new BufferedReader(new FileReader(actionFile));
            while( (line = in.readLine()) != null) {
                response += line + "\n";
            }
            in.close();
            return LOAD_RESPONSE + response;
        } catch (FileNotFoundException e) {
            return SAVE_RESPONSE + "Unable to find file!";
        } catch (IOException e) {
            return SAVE_RESPONSE + "Error loading file!";
        }
    }
    
    /**
     * Takes a file to read the input into and the text to write and saves the
     * text to the file.
     * @param actionFile The absolute path of the file to save to.
     * @param text The text to save.
     * @param projectName The name of the project.
     * @return A string to be logged to the console.
     */
    public static String performSave(String actionFile, String text, String projectName) {
        try {
            PrintWriter out = new PrintWriter(actionFile);
            out.print(text);
            out.close();
            return (SAVE_RESPONSE + "Saved!" + "\n" + syncFiles(projectName));
        } catch (IOException e) {
            return SAVE_RESPONSE + "ERROR SAVING FILE";
        }
    }
    
    /**
     * Calls javac on all java files this method is called on. 
     * @param actionFile The absolute path of the file to compile.
     * @return Any compilation errors, or new of a successful build.
     */ 
    public static String performBuild(String actionFile) {
        String fileType = actionFile.substring(actionFile.lastIndexOf('.') + 1, actionFile.length());
        if(fileType.equals("java")) {
            try {
                Process p = Runtime.getRuntime().exec("javac " + actionFile);
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String output = "", line;
                while((line = in.readLine()) != null) {
                    System.out.println(line);
                    output += line + "\n";
                }
                System.out.println("END OF LOOP");
                if(output.equals("")) {
                    return (BUILD_RESPONSE + "BUILD SUCCESS!");
                } else {
                    return BUILD_RESPONSE + output;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return BUILD_RESPONSE + "An error occurred!";
            }
        } else {
            return BUILD_RESPONSE + "File Type currently unsupported.";
        }
    }
    
    /**
     * Creates a new file and returns a response message.
     * @param actionFile The absolute path of the file to create.
     * @return A message indicating the success of the action.
     */
    public static String performCreate(String actionFile) {
        boolean dirSucceed = true;
        
        int lastDelimiterIndex = Math.max(actionFile.lastIndexOf('/'), actionFile.lastIndexOf('\\'));
        String actionPath = actionFile.substring(0, lastDelimiterIndex);
        
        try {
           File f = new File(actionFile);
           File folder = new File(actionPath);
           if(f.exists()) {
               return NEW_RESPONSE + "File already exists!";
           } else {
                dirSucceed = folder.mkdirs();
                PrintWriter out = new PrintWriter(actionFile);
                out.print("");
                out.close();
                return NEW_RESPONSE + "File created!";
           }
        } catch (IOException e) {
            String output = NEW_RESPONSE + "Error creating file! " + e.toString();
            e.printStackTrace();
            if(!dirSucceed) {
                output += " Could not create necessary directories!";
            }
            return output;
        }
    }
    
    /**
     * Deletes the given file.
     * @param actionFile The absolute path of the file to delete.
     * @return A statement indicating the success of the action.
     */
    public static String performDelete(String actionFile) {

            String command = "";
            actionFile = actionFile.replace('/', '\\');
            System.out.println(actionFile);
            File f = new File(actionFile);
            if(f.isDirectory()) {
                command = "RD /S /Q " + actionFile;
            } else {
                f.delete();
            }
            
            //Process p = Runtime.getRuntime().exec(command);
            //BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            
            String output = DELETE_RESPONSE + actionFile + " deleted.";
            //String line;
            //while((line = in.readLine()) != null) {
            //    output += line + "/n";
            //}
            return output;

    }
    
    /**
     * Given the name of a project to delete, removes the project directory,
     * and all of its files / subdirectories from the workspace. If the project
     * is on a supercomputer, this does not occur.
     * @param projectName  The name of the project to delete.
     * @return A statement indicating the success of the action.
     */
    public static String performDeleteProject(String projectName) {
        String output = PROJECT_RESPONSE + projectName + " deleted.\n";
        if (deleteDir(new File(WorkspaceResource.getResourceBase() + "/" + projectName))) {
            return output;
        } else {
            return PROJECT_RESPONSE + "Could not delete project!";
        }
        
    }
    
    /**
     * Deletes a given directory. This method was found on StackOverflow, written
     * by Sidharth Panwar.
     * @param dir The directory to delete.
     * @return true if successful, false otherwise.
     */
    public static boolean deleteDir(File dir) { 
        if (dir.isDirectory()) { 
        String[] children = dir.list(); 
        
            for(int i=0; i<children.length; i++) { 
                boolean success = deleteDir(new File(dir, children[i])); 
                if (!success) {  
                    return false; 
                }       
            } 
        }  
        // The directory is now empty or this is a file so delete it 
         return dir.delete(); 
    }
  
    /**
     * Given a payload (consisting of the IP, Username, and Password), delimited
     * by a special character (187), sets the private fields equal to the given
     * data.
     * @param payload The settings to update, separated by a special delimiter.
     * @return A message indicating the success of the action.
     */
    public static String performUpdateSettings(String payload) {
        char delimiter = 187;
        String[] settingsList = payload.split((delimiter + ""));
        try {
            ip = settingsList[0];
            username = settingsList[1];
            password = settingsList[2];
            
            String output = SETTINGS_RESPONSE + "Settings updated successfully!";
            //  output += "\nIP: " + ip;
            // output += "\nUsername: " + username;
            // output += "\nPassword: " + password;
            return output;
        } catch (ArrayIndexOutOfBoundsException e) {
            return SETTINGS_RESPONSE + "Could not resolve message from client!";    
        }
    }
    
    
    /**
     * Invokes rsync to synchronize files from the server to the target
     * supercomputer, as determined by the IP and username provided.
     * @param projectName The name of the project.
     * @return 
     */
    public static String syncFiles(String projectName) {
        return FileSync.syncFiles(ip, username, password, projectName);
    }
}
