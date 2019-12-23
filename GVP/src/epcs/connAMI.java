/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package epcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jmolina
 */
public class connAMI {
    
    private Socket          echoSocket;
    private OutputStream    os;
    private BufferedReader  in;
    private String          response;
    private boolean         cmdResult;
    
    public connAMI() {
        cmdResult = true;
    }
    
    public void addQueueMember(String queue, String member, boolean paused) {
        
        String command = "";
        
        command += "Action: QueueAdd\r\n";
        command += "Queue: " + queue + "\r\n";
        command += "Interface: " + member + "\r\n";
        command += "Paused: " + paused + "\r\n\r\n";
        
        SendMessageToAMI(command);
       
    }
    
    public void removeQueueMember(String queue, String member) {
        
        String command = "";
        
        command += "Action: QueueRemove\r\n";
        command += "Queue: " + queue + "\r\n";
        command += "Interface: " + member + "\r\n\r\n";
        
        SendMessageToAMI(command);
       
    }
    
    public void execCommand(String command) {
        SendMessageToAMI("Action: Command\r\nCommand: " + command + "\r\n\r\n");
    }
    
    public void disconnectAMI() {
        SendMessageToAMI("Action: Logoff\r\n\r\n");
    }
    public void SendMessageToAMI(String msjRequest)
    {
        try
        {  
            os = echoSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader( echoSocket.getInputStream()));
            os.write(msjRequest.getBytes());
            
            response = "";
            String respLine = "";
            
            if ( (respLine = in.readLine()) != null) {
                 response += respLine + "\n";
             }
            
        }
        catch (Exception e)
        {
            cmdResult = false;
            System.err.println(" < Ocurrio un error al enviar el mensaje al IvrToMQ: " + e.getMessage());
        }
    }

   public boolean connectToAMI(String host, int port, String user, String secret, int timeout) throws IOException
   {
        
        try {
            
            echoSocket = new Socket();
            echoSocket.connect(new InetSocketAddress(host, port), timeout);
            
            //echoSocket.setSoTimeout(30000);
            os = echoSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader( echoSocket.getInputStream()));
            
            String userInput = "Action: Login\r\nUsername: " + user + "\r\nSecret: " + secret + "\r\n\r\n";
            os.write(userInput.getBytes());
            
            response = "";
            String respLine = "";
            
            while (!(respLine = in.readLine()).isEmpty()) {
                 response += respLine + "\n";
             }
            
            return true;
            
        } catch (SocketTimeoutException ex) {
            response = "No se pudo conectar con el host, TimeOut = " + timeout + "\n";
            Logger.getLogger(connAMI.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
   }
   
   public String getResponse() {
       return this.response;
   }
   
   public void setResponse(String value) {
       this.response = value;
   }
   
   public boolean getCmdResult() {
       return this.cmdResult;
   }
   
}