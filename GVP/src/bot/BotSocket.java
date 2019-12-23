package bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Stack;

import org.apache.log4j.Logger;

public class BotSocket {
	
	static String SERVER_HOST  = "localhost";
	static int SERVER_PORT = 50080;
	static int SERVER_TIMEOUT = 10000;
	
	private Socket socket;
	
	private OutputStream    os;
    private BufferedReader  in;
    
	
	private Stack<String> messages = new Stack<String>();
	
	private Logger log = Logger.getLogger("GVP");
	
	public BotSocket(){		
		super();
	}

		
	public String messageToBot(String sessionId, String message) throws IOException{        
		String response = "";
		try {
            
            socket = new Socket();
            System.out.println(SERVER_HOST+":"+SERVER_PORT);
            socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), SERVER_TIMEOUT);            
//            socket.setSoTimeout(SERVER_TIMEOUT);
            
            os = socket.getOutputStream();
            in = new BufferedReader(new InputStreamReader( socket.getInputStream()));
            
            os.flush();

            String userInput = message;
            os.write(userInput.getBytes());
            os.flush();
            
            String respLine = "";

            do{
            	respLine = (String) in.readLine();
            	
                System.out.println("server>" + respLine);
            
                if (respLine != null)
                	response += respLine + "\n";  
                else
                	break;
            }while(!respLine.equals("") || !respLine.equals("\n"));

            os.close();
            in.close();
            
        } catch (SocketTimeoutException ex) {
            response = "No se pudo conectar con el host, TimeOut = " + SERVER_TIMEOUT + "\n";
            log.debug("[messageToBot] "+sessionId+" Exception: "+ex.getMessage());
            
        }finally{
        	if (socket != null){
        		socket.close();
        	}
        }
		return response;
   }
	
	
	
	
	
}
