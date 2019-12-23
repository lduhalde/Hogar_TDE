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

public class connIvrToDB {
    private Socket          echoSocket;
    private OutputStream    os;
    private BufferedReader  in;
    private String          response;
    
    public connIvrToDB() {}
    
    public void SendMessage(String msjRequest)
    {
        try
        {  
            os = echoSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader( echoSocket.getInputStream()));
            os.write(msjRequest.getBytes());
            
            response = "";
            String respLine = "";
            
            while (!(respLine = in.readLine()).isEmpty()) {
                 response += respLine + "\n";
             }
            
        }
        catch (Exception e)
        {
//        	System.err.println(" < Ocurrio un error al enviar el mensaje al IvrToDB: " + e.getMessage());
        }
    }

   public boolean connect(String host, int port, int timeout) throws IOException
   {
        
        try {
            
            echoSocket = new Socket();
            echoSocket.connect(new InetSocketAddress(host, port), timeout);
            
            //echoSocket.setSoTimeout(30000);
            os = echoSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader( echoSocket.getInputStream()));
            
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
}
