/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server.jsch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import ptpeditor.server.util.PropertiesManager;

/**
 *
 * @author Mitch
 */
public class JschUtil {
    
    public static String sshDirectory() {
        return PropertiesManager.sshLocation();
        // return "C:\\cygwin64\\home\\Mitch\\.ssh";
    }
    
    public static void registerWithServer(ServerInfo info) {
        if(!readyForSSH(info)) {
            sendPublicKey(info);
        }
    }
    
    public static boolean readyForSSH(ServerInfo info) {
        JschExec testExec = new JschExec(info);
        try {
            testExec.connectNoPw();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
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
