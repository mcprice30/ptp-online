/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server.jsch;

/**
 * ServerInfo describes all information relevant to the computer you are SSHing
 * into.
 * @author Mitch
 */
public class ServerInfo {
    private final String userName;
    private final String host;
    private final String password;
    private final String name;
    
    /**
     * Constructor for ServerInfo.
     * @param userName The username you will be SSHing into.
     * @param host The ip of the host server.
     * @param password The password corresponding to the chosen username.
     * @param name The name to associate with this username/ip.
     */
    public ServerInfo(String userName, String host, String password, String name) {
        this.userName = userName;
        this.host = host;
        this.password = password;
        this.name = name;
    }
    
    /**
     * Passwordless constructor for ServerInfo.
     * @param userName The username you will be SSHing into.
     * @param host The ip of the computer you will be SSHing into.
     * @param name The name to associate with this username/ip.
     */
    public ServerInfo(String userName, String host, String name) {
        this.userName = userName;
        this.host = host;
        this.password = "";
        this.name = name;
    }
    
    /**
     * Getter method for userName.
     * @return The username you are SSHing into.
     */
    public String userName() {
        return userName;
    }
    
    /**
     * Getter method for password.
     * @return The password corresponding to the above username.
     */
    public String password() {
        return password;
    }
    
    /**
     * Getter method for host.
     * @return The IP address of the server you are SSHing into.
     */
    public String host() {
        return host;
    }
    
    /**
     * Getter method for name.
     * @return The name assigned to this username / IP address pair.
     */
    public String name() {
        return name;
    }
}
