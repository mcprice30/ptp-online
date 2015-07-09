/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ptpeditor.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.PathParam;

/**
 *
 * @author Mitch
 */
@ServerEndpoint("/save/{fileType}/{fileName}")
public class SaveServer {
    /**
     * @OnOpen allows us to intercept the creation of a new session.
     * The session class allows us to send data to the user.
     * In the method onOpen, we'll let the user know that the handshake was 
     * successful.
     * @param session The websocket session that has been opened.
     * @param fileType The file extension that is being created, as specified by the path.
     * @param fileName The name of the new file, as specified by the path.
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("fileType") String fileType, @PathParam("fileName") String fileName){
        System.out.println(session.getId() + " has opened a connection"); 
        System.out.println("Will save file: " + fileName + "." + fileType);
        try {
            session.getBasicRemote().sendText("Creating: " + fileName + "." + fileType);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
 
    /**
     * When a user sends a message to the server, this method will intercept the message
     * and allow us to react to it. For now the message is read as a String.
     * @param message The message from the client endpoint.
     * @param session The websocket session that sent the message.
     * @param fileType The file extension that is being created, as specified by the path.
     * @param fileName The name of the new file, as specified by the path.
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("fileType") String fileType, @PathParam("fileName") String fileName){
        System.out.println("Message from " + session.getId() + ": " + message);
        PrintWriter out;
        try {
            String file = fileName + "." + fileType;
            String workspace = "C:\\Users\\Mitch\\Documents\\PTPworkspace\\";
            out = new PrintWriter(workspace + file);
            out.print(message);
            out.close();
            session.getBasicRemote().sendText("Saved!");
        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                session.getBasicRemote().sendText("ERROR!");
            } catch (IOException ex2) {
                ex.printStackTrace();
            }
        }
        
    }
 
    /**
     * The user closes the connection.
     * 
     * Note: you can't send messages to the client from this method
     * @param session The websocket session that was just closed.
     * @param fileType The file extension that is being created, as specified by the path.
     * @param fileName The name of the new file, as specified by the path.
     */
    @OnClose
    public void onClose(Session session, @PathParam("fileType") String fileType, @PathParam("fileName") String fileName){
        System.out.println("Session " + session.getId() + " has ended");
    }
}
