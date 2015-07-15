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

import static ptpeditor.server.util.Constants.*;

import ptpeditor.server.util.FileSync;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.File;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.PathParam;

import ptpeditor.server.jsch.ServerInfo;
import ptpeditor.server.jsch.JschUtil;
import ptpeditor.server.jsch.JschExec;

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
@ServerEndpoint("/load/{userId}/{projectName}")
public class LoadServer {
    
    private static String ip = null;
    private static String username = null;
    private static String password = null;
    private static String directory = null;
    private static String makefile = null;
    
    /**
     *  The onOpen message sends a message back to the client side once
     *  this endpoint of the Websocket is established.
     * @param session
     * @param userId The id assigned to the user.
     * @param projectName 
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId")String userId, @PathParam("projectName")String projectName) {
        System.out.println(session.getId() + " has opened a connection"); 
        //System.out.println("Will save file: " + fileName + "." + fileType);
        try {
            session.getBasicRemote().sendText(PROJECT_RESPONSE + "Ready to load from: " + projectName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String initialSettings = performReadSettings(projectName, userId);
        initialSettings = initialSettings.substring(1, initialSettings.length());
        char delimiter = 187;
        if(!initialSettings.equals("ip" + delimiter + "username" + delimiter + "password" + delimiter + "workspace" + delimiter + "makefile")) {
            performUpdateSettings(initialSettings, projectName, userId, session);
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
     * @param userId
     * @param projectName 
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId")String userId, @PathParam("projectName")String projectName) {
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
            session.getBasicRemote().sendText(handleMessage(actionType, payload, text, userId, projectName, session));
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }
    
    /**
     * When the websocket is closed, log the event and session ID to the server.
     * @param session
     * @param userId
     * @param projectName 
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId")String userId, @PathParam("projectName")String projectName) {
        System.out.println("Session " + session.getId() + " has ended");
        System.out.println("Project " + projectName + "for user: " + userId + "is closed.");
    }
    
    /**
     * Deals with the message by farming it out to a variety of subtasks
     * depending on the header's content.
     * @param header The type of action to perform.
     * @param payload An action file, settings, etc.
     * @param text If saving a file, the text to save.
     * @param userId The name of the user.
     * @param projectName The name of the project.
     * @param session The ongoing websocket session.
     * @return The server's response.
     */
    public static String handleMessage(String header, String payload, String text, String userId, String projectName, Session session) {
        switch (header) {
            case LOAD_KEY:
                return performLoad(payload);
            case SAVE_KEY:
                return performSave(payload, text, projectName, userId);
            case BUILD_KEY:
                return performBuild(projectName);
            case NEW_KEY:
                return performCreate(payload);
            case DELETE_KEY:
                return performDelete(payload);
            case DELETE_PROJECT_KEY:
                return performDeleteProject(projectName, userId);
            case SETTINGS_KEY:
                return performUpdateSettings(payload, projectName, userId, session);
            case READ_SETTINGS_KEY:
                return performReadSettings(projectName, userId);
            case SYNC_KEY:
                return syncFiles(projectName, userId);
            case USE_PASSWORD_KEY:
                return registerKeyPair(payload, projectName);
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
            return ERROR_RESPONSE + "Unable to find file!";
        } catch (IOException e) {
            return ERROR_RESPONSE + "Error loading file!";
        }
    }
    
    /**
     * Takes a file to read the input into and the text to write and saves the
     * text to the file.
     * @param actionFile The absolute path of the file to save to.
     * @param text The text to save.
     * @param projectName The name of the project.
     * @param userId The id assigned to the user.
     * @return A string to be logged to the console.
     */
    public static String performSave(String actionFile, String text, String projectName, String userId) {
        try {
            PrintWriter out = new PrintWriter(actionFile);
            out.print(text);
            out.close();
            return (SAVE_RESPONSE + "Saved!");
        } catch (IOException e) {
            return SAVE_RESPONSE + "ERROR SAVING FILE";
        }
    }
    
    /**
     * Calls javac on all java files this method is called on. 
     * @param projectName The name of the project that is being built.
     * @return Any compilation errors, or new of a successful build.
     */ 
    public static String performBuild(String projectName) {
        String command;
        String cFlag = "-C " + directory + "/" + projectName;

        String makeFile = makefile;
        if(makeFile.indexOf("/") == 0) {
            makeFile = makeFile.substring(1, makeFile.length());
        }
        String fFlag = " -f " + makeFile;
        if(makeFile.equals(DEFAULT_MAKE_FILE)) {
            command = "make " + cFlag;
        } else {
            command = "make " + cFlag + fFlag;   
        }
        
        ServerInfo info = new ServerInfo(username, ip, username + "'s server for " + projectName);
        JschExec writeKeyExec = new JschExec(info);
        try {
            String output = BUILD_RESPONSE + writeKeyExec.connect().execute(command, false);
            writeKeyExec.stop().disconnect();
            return output;
        } catch (Exception e) {
            return BUILD_RESPONSE + "ERROR: " + e.toString();
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
            System.out.println(e.toString());
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
     * @param userId The id assigned to the user.
     * @return A statement indicating the success of the action.
     */
    public static String performDeleteProject(String projectName, String userId) {
        String output = PROJECT_RESPONSE + projectName + " deleted.\n";
        if (deleteDir(new File(WorkspaceResource.getResourceBase(userId) + "/" + projectName))) {
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
     * @param projectName The name of the project being worked on.
     * @param userId The id assigned to the user.
     * @return A message indicating the success of the action.
     */
    public static String performUpdateSettings(String payload, String projectName, String userId, Session session) {
        System.out.println(payload);
        String propertiesFile = WorkspaceResource.getResourceBase(userId) + "/" + projectName + "/.settings.prop";
        char delimiter = 187;
        String[] settingsList = payload.split((delimiter + ""));
        try {
            ip = settingsList[0];
            username = settingsList[1];
            password = settingsList[2];
            directory = settingsList[3];
            makefile = settingsList[4];
            
            ServerInfo info = new ServerInfo(username, ip, password, userId + "'s server for " + projectName);
            if(!JschUtil.readyForSSH(info)) {
                try {
                    session.getBasicRemote().sendText(PASSWORD_PROMPT_RESPONSE);
                } catch (IOException e) {
                    System.out.println("Unable to message client!");
                }
            }
            //JschUtil.registerWithServer(info);
            //String output = SETTINGS_RESPONSE + "Settings updated successfully!";
            //  output += "\nIP: " + ip;
            // output += "\nUsername: " + username;
            // output += "\nPassword: " + password;
        } catch (ArrayIndexOutOfBoundsException e) {
            return SETTINGS_RESPONSE + "Could not resolve message from client!";    
        }
        
        try {
            PrintWriter out = new PrintWriter(propertiesFile);
            out.print(payload);
            out.close();
            return (SETTINGS_RESPONSE + "Settings updated successfully!");
        } catch (IOException e) {
            return SETTINGS_RESPONSE + "Could not save settings!";
        }
        
    }
    
    /**
     * Upon being given a project name, reads the .settings.prop folder for the project
     * and returns the contents.
     * @param projectName The name of the active project.
     * @param userId The id assigned to the user.
     * @return The contents of the project's .settings.prop folder.
     */
    public static String performReadSettings(String projectName, String userId) {
        char delimiter = 187;
        String defaultResponse = "ip" + delimiter + "username" + delimiter + "password" + delimiter + "workspace";
        String response;    //The response. (Begins with response key).
        String propertiesFile = WorkspaceResource.getResourceBase(userId) + "/" + projectName + "/.settings.prop";
        try {
            BufferedReader in = new BufferedReader(new FileReader(propertiesFile));
            response = in.readLine();
            in.close();
            return READ_SETTINGS_RESPONSE + response;
        } catch (FileNotFoundException e) {
            System.out.println("Settings file not found!");
            return READ_SETTINGS_RESPONSE + defaultResponse;
        } catch (IOException e) {
            System.out.println("Error reading settings file!");
            return READ_SETTINGS_RESPONSE + defaultResponse;
        }
    }
    
    /**
     * Invokes rsync to synchronize files from the server to the target
     * supercomputer, as determined by the IP and username provided.
     * @param projectName The name of the project.
     * @param userId The id assigned to the user.
     * @return 
     */
    public static String syncFiles(String projectName, String userId) {
        return SYNC_RESPONSE + FileSync.syncFiles(ip, username, password, directory, projectName, userId);
    }
    
    /**
     * 
     * @param payload
     * @return 
     */
    public static String registerKeyPair(String payload, String projectName) {
        ServerInfo info = new ServerInfo(username, ip, payload, username + "'s server for " + projectName);
        JschUtil.registerWithServer(info);
        return KEYPAIR_GENERATION_RESPONSE + (JschUtil.readyForSSH(info) ? "Login successful!" : "Login failure!");
    }
}
