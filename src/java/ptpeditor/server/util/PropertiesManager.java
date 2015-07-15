/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
/**
 * This class is used to access the properties within the Glassfish install folder's
 * properties.txt folder.
 * @author Mitch
 */
public class PropertiesManager {
    private static String workspaceLocation = null;
    private static String sshLocation = null;
    private static boolean propsRead = false;
    
    private static void readPropertiesFile() {
        try {
            String propertiesLoc = (String) System.getProperties().get("com.sun.aas.instanceRoot");
            BufferedReader br = new BufferedReader(new FileReader(propertiesLoc + "/properties.txt" ));
            String line;
            int lineNo = 0;
            while((line = br.readLine()) != null) {
                lineNo++;
                switch(lineNo) {
                    case 1:
                        workspaceLocation = getEntryFromLine(line);
                        break;
                    case 2:
                        sshLocation = getEntryFromLine(line);
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e.toString());
        }
        propsRead = true;
    }
    
    private static String getEntryFromLine(String lineText) {
        return lineText.substring(lineText.indexOf(" ") + 1, lineText.length());
    }
    
    /**
     * Retrieves the location of the workspace on this computer.
     * @return The location of the workpace.
     */
    public static String workspaceLocation() {
        if(!propsRead) {
            readPropertiesFile();
        }
        return workspaceLocation;
    }
    
    /**
     * Retrieves the directory containing the SSH keypair to be used.
     * @return The .ssh directory's location.
     */
    public static String sshLocation() {
        if(!propsRead) {
            readPropertiesFile();
        }
        return sshLocation;
    }
}
