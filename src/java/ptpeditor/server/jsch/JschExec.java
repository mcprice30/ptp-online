/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package ptpeditor.server.jsch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSchException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 *
 * @author Mitch
 */
public class JschExec {
    private final JSch jsch;
    
    private ChannelExec channelExec;
    private Session session;
    private boolean flag;
    
    private final String userName;
    private final String host;
    private final String password;
    private final String name;
    
    
    public JschExec (ServerInfo info) {
        this.userName = info.userName();
        this.host = info.host();
        this.password = info.password();
        this.name = info.name();
        this.jsch = new JSch();
    }
    
    /**
     * Connects to the remote server.
     * @return
     * @throws Exception 
     */
    public JschExec connect() throws Exception {
        jsch.addIdentity(JschUtil.sshDirectory() + "/id_dsa");
        System.out.println("connecting to " + name);
        session = jsch.getSession(userName, host);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();
        channelExec = (ChannelExec) session.openChannel("exec");
        System.out.println("connected to " + name);
        return this;
    }
    
    /**
     * Connects to the remote server.
     * @return
     * @throws Exception 
     */
    public JschExec connectNoPw() throws Exception {
        jsch.addIdentity(JschUtil.sshDirectory() + "/id_dsa");
        System.out.println("connecting to " + name);
        session = jsch.getSession(userName, host);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword("");
        session.connect();
        channelExec = (ChannelExec) session.openChannel("exec");
        System.out.println("connected to " + name);
        return this;
    }
    /**
     * Given a single command, executes the command.
     * @param command The command to execute.
     * @throws IOException
     * @throws JSchException Thrown if error connecting.
     * @return The command line's output.
     */
    public String execute(String command, boolean willWrite) throws IOException, JSchException {
        flag = true;
        String output = "", errOutput = "";
        System.out.println("executing: " + command);
        InputStream stream = channelExec.getInputStream();
        channelExec.setCommand(command);
        channelExec.connect();
        System.out.println("Connected!");
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(channelExec.getErrStream()));
        String line;
        System.out.println("Starting Loop!");
        if(reader.ready()) {
            while ((line = reader.readLine()) != null && flag) {
                System.out.println("Line: " + line);
                output += line + "\n";
            }
        }
        if(!willWrite) {
            System.out.println("ERRORS!");
            while ((line = errReader.readLine()) != null && flag) {
                System.out.println(line);
                errOutput += line + "\n";
            }
            if(errOutput.length() > 0) {
                
                output += (output.length() > 0 ? "\n" : "") + "ERROR:\n" + errOutput;
                output += "Command used: " + command;
            }
        }
        System.out.println("execution ending");
        //this.stop().disconnect();
        return output;
    }
    
    public void enterTextAndClose(String message) throws IOException {
        System.out.println("Writing!");
        OutputStream writeStream = channelExec.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(writeStream);
        for(int i = 0; i < message.length(); i++) {
            writer.write(message.charAt(i));
        }
        writer.flush();
        writer.close();
    }
    
    public JschExec stop(){
        this.flag = false;
        System.out.println("execution stopped");
        return this;
    }

    public JschExec disconnect() throws Exception {
        int exitCode = channelExec.getExitStatus();
        System.out.println("Ending with exit code " + exitCode);
        if (channelExec != null) channelExec.disconnect();
        if (session != null) session.disconnect();
        System.out.println("disconnected from " + name);
        return this;
    }
    
    public String name() {
        return this.name;
    }
}