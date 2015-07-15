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
 * The JschExec class is a means of sending commands to the remote server. It
 * allows you to connect/disconnect, execute a command, and stop command execution,
 * as well as write text to an ongoing command.
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
    
    /**
     * Constructor for JschExec.
     * @param info The server you are connecting to's information.
     */
    public JschExec (ServerInfo info) {
        this.userName = info.userName();
        this.host = info.host();
        this.password = info.password();
        this.name = info.name();
        this.jsch = new JSch();
    }
    
    /**
     * Connects to the remote server, using either a keypair or a password.
     * @return A JschExec instance that is connected to the remote computer.
     * @throws JSchException Thrown if error connecting. 
     */
    public JschExec connect() throws JSchException {
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
     * Connects to the remote server using only an SSH keypair. No password is
     * used.
     * @return a JschExec instance that has connected to the remote computer.
     * @throws JSchException Thrown if error connecting. 
     */
    public JschExec connectNoPw() throws JSchException {
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
     * @param willWrite True if this process will be written to later, false otherwise.
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
    
    /**
     * Takes a message and writes it to the channel's execution process.
     * @param message The text to write to the file.
     * @throws IOException Thrown in the event of being unable to locate or write to
     * the necessary stream.
     */
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
    
    /**
     * Halts execution for this JschExec instance.
     * @return An instance of JschExec with halted execution.
     */
    public JschExec stop(){
        this.flag = false;
        System.out.println("execution stopped");
        return this;
    }

    /**
     * Disconnects this JschExec's channel.
     * @return An instance of JschExec that has been disconnected.
     * @throws JSchException
     */
    public JschExec disconnect() throws JSchException {
        int exitCode = channelExec.getExitStatus();
        System.out.println("Ending with exit code " + exitCode);
        if (channelExec != null) channelExec.disconnect();
        if (session != null) session.disconnect();
        System.out.println("disconnected from " + name);
        return this;
    }
    
    /**
     * Gets the name of this Jsch Instance.
     * @return The name of the server info passed in.
     */
    public String name() {
        return this.name;
    }
}