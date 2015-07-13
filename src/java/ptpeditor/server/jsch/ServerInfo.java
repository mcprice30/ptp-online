/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server.jsch;

/**
 *
 * @author Mitch
 */
public class ServerInfo {
    private final String userName;
    private final String host;
    private final String password;
    private final String name;
    
    public ServerInfo(String userName, String host, String password, String name) {
        this.userName = userName;
        this.host = host;
        this.password = password;
        this.name = name;
    }
    
    public ServerInfo(String userName, String host, String name) {
        this.userName = userName;
        this.host = host;
        this.password = "";
        this.name = name;
    }
    
    public String userName() {
        return userName;
    }
    
    public String password() {
        return password;
    }
    
    public String host() {
        return host;
    }
    
    public String name() {
        return name;
    }
}
