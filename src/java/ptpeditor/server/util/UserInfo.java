/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server.util;

/**
 * The UserInfo class contains various information about a given user. However,
 * this information, while pertaining to a user, is also project-specific.
 * @author Mitch
 */
public class UserInfo {
    private String ip;
    private String username;
    private String password;
    private String directory;
    private String makefile;
    
    /**
     * Constructor for UserInfo.
     * @param ip The ip this user is currently connecting to.
     * @param username The username this user is currently SSHing as.
     * @param password The password this user is currently using to SSH.
     * @param directory The remote directory the user's project is located in.
     * @param makefile The name of the makefile for this user's currently open project.
     */
    public UserInfo(String ip, String username, String password, String directory, String makefile) {
        this.ip = ip;
        this.username = username;
        this.password = password;
        this.directory = directory;
        this.makefile = makefile;
    }
    
    /**
     * Getter method for IP.
     * @return The IP address this user is currently connecting to.
     */
    public String getIP() {
        return this.ip;
    }
    
    /**
     * Setter method for IP.
     * @param ip The new IP address this user is connecting to.
     */
    public void setIP(String ip) {
        this.ip = ip;
    }
    
    /**
     * Getter method for username.
     * @return The username this user is currently SSHing as.
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * Setter method for username.
     * @param username The username this user will try to SSH as.
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Getter method for password.
     * @return The password this user uses to SSH.
     */
    public String getPassword() {
        return this.password;
    }
    
    /**
     * Setter method for password.
     * @param password The password this user will use to SSH.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Getter method for directory.
     * @return The remote directory the user's current project is located in.
     */
    public String getDirectory() {
        return this.directory;
    }
    
    /**
     * Setter method for directory.
     * @param directory The remote directory the user's project will be located in.
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    
    /**
     * Getter method for makefile.
     * @return The name of this user's current project's makefile.
     */
    public String getMakefile() {
        return this.makefile;
    }
    
    /**
     * Setter method for makefile.
     * @param makefile The name of the makefile this user will use.
     */
    public void setMakefile(String makefile) {
        this.makefile = makefile;
    }
}