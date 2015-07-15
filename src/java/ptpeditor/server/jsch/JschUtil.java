/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server.jsch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import ptpeditor.server.util.PropertiesManager;

import com.jcraft.jsch.JSchException;

/**
 * This class uses JSch to provide a few utility tasks, primarily through testing
 * for and/or setting up an SSH keypair with another computer.
 * @author Mitch
 */
public class JschUtil {
    
    /**
     * Retrieves the location of the SSH keypair files.
     * @return The path to the .ssh directory containing the desired keypair.
     */
    public static String sshDirectory() {
        return PropertiesManager.sshLocation();
    }
    
    /**
     * If you do not have a keypair set up with the given server, this sets it up
     * for you.
     * @param info The information on the server you wish to set up a keypair with. 
     */
    public static void registerWithServer(ServerInfo info) {
        if(!readyForSSH(info)) {
            sendPublicKey(info);
        }
    }
    
    /**
     * Determines whether or not you have established a keypair with a given computer.
     * @param info Information about the server you wish to check for a keypair.
     * @return True if a keypair is already installed, false otherwise. 
     */
    public static boolean readyForSSH(ServerInfo info) {
        JschExec testExec = new JschExec(info);
        try {
            testExec.connectNoPw();
            return true;
        } catch (JSchException e) {
            return false;
        }
    }
    
    /**
     * Sends and installs the public half of your SSH keypair into the given server.
     * @param info Information about the server you wish to setup a keypair with.
     */
    public static void sendPublicKey(ServerInfo info) {
        System.out.println("Sending public key!");
        String publicKeyFile = sshDirectory() + "/id_dsa.pub";
        String publicKey = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(publicKeyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
		publicKey += line + "/n";
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e.toString());
        } 
        
        JschExec writeKeyExec = new JschExec(info);
        try {
            writeKeyExec.connect().execute("cat >> .ssh/authorized_keys", true);
            writeKeyExec.enterTextAndClose(publicKey);
            writeKeyExec.stop().disconnect();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.toString());
        }
    }
}
