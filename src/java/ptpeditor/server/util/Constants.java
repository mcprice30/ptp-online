/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server.util;

/**
 * This class contains the various constants that may be accessed from the server.
 * @author Mitch
 */
public class Constants {
   
        //      --REQUEST KEYS--
    
    /** Prefaces requests to save a file. */
    public static final String SAVE_KEY = "SAVE";
    /** Prefaces requests to load a file. */
    public static final String LOAD_KEY = "LOAD";
    /** Prefaces requests to build or compile a file. */
    public static final String BUILD_KEY = "BUILD";
    /** Prefaces requests to create a new file. */
    public static final String NEW_KEY = "NEW";
    /** Prefaces requests to delete a file. */
    public static final String DELETE_KEY = "DELETE";
    /** Prefaces requests to delete a project. */
    public static final String DELETE_PROJECT_KEY = "DELETE_PROJECT";
    /** Prefaces requests to write settings to the server. */
    public static final String SETTINGS_KEY = "SETTINGS";
    /** Prefaces requests for the server to read the settings from a file and reply. */
    public static final String READ_SETTINGS_KEY = "READ_SETTINGS";
    /** Prefaces requests to synchronize the server with the target supercomputer */
    public static final String SYNC_KEY = "SYNC";
    /** Prefaces requests to generate a security key with a given password. */
    public static final String USE_PASSWORD_KEY = "AUTH";
    
        //      --RESPONSE KEYS--
    
    /** Prefaces responses to requests to save a file. */
    public static final String SAVE_RESPONSE = "S";
    /** Prefaces responses to requests to load a file. */
    public static final String LOAD_RESPONSE = "L";
    /** Prefaces responses to requests to build a file. */
    public static final String BUILD_RESPONSE = "B";
    /** Prefaces responses to requests to create a new file. */
    public static final String NEW_RESPONSE = "N";
    /** Prefaces responses to requests to delete a file. */
    public static final String DELETE_RESPONSE = "D";
    /** Prefaces responses to requests affecting the project. */
    public static final String PROJECT_RESPONSE = "P";
    /** Prefaces responses to requests to write settings to the server. */
    public static final String SETTINGS_RESPONSE = "G";
    /** Prefaces responses that contain an error message. */
    public static final String ERROR_RESPONSE = "E";
    /** Prefaces responses to requests to read saved settings. */
    public static final String READ_SETTINGS_RESPONSE = "R";
    /** Prefaces responses to rsync requests. */
    public static final String SYNC_RESPONSE = "Y";
    /** Prefaces responses prompting for a password. */
    public static final String PASSWORD_PROMPT_RESPONSE = "Q";
    /** Prefaces responses pertaining to keypair generation */
    public static final String KEYPAIR_GENERATION_RESPONSE = "K";
        
        //      --OTHER CONSTANTS--
    
    /** This text indicates that there is no makefile specified.*/
    public static final String DEFAULT_MAKE_FILE = "NO_MAKEFILE_FOUND";
}
