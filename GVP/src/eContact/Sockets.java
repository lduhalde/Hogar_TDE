package eContact;

import java.io.*;
import java.net.*;

public class Sockets 
{
	private Socket				SocketServer;
	
	private DataOutputStream    SocketServerOutputStream;
	private DataInputStream    	SocketServerInputStream;
	
	private PrintWriter 		oSocketServerOutputStream;      
	private	BufferedReader 		oSocketServerInputStream;
	
	private boolean 			SocketConnected;
	private boolean				SocketError;
	private String				SocketErrorMessage;

	public Sockets()
	{
		this.SocketServer 				= null;
		this.SocketServerInputStream	= null;
		this.SocketServerOutputStream 	= null;
		this.SocketError				= false;
		this.SocketConnected			= false;
		this.SocketErrorMessage			= "";
	}
	
	public String GetErrorMessage()	
	{
		return this.SocketErrorMessage;	
	}
	
	public boolean IsSocketWithError()
	{
		return this.SocketError;
	}
	
	public boolean OpenConnection(String RemoteHost, int RemotePort, int ConnectionTimeOut, int ReadWriteTimeOut)
	{
		this.SocketError 		= false;
		this.SocketErrorMessage	= "";
		
		try
		{
			this.SocketServer				= new Socket();
			InetSocketAddress SocketAddr 	= new InetSocketAddress( RemoteHost, RemotePort );
			
			this.SocketServer.connect( SocketAddr, ConnectionTimeOut );
			this.SocketServer.setKeepAlive( true );
			this.SocketServer.setSoTimeout( ReadWriteTimeOut );							
            
			this.SocketServerOutputStream 	= new DataOutputStream( this.SocketServer.getOutputStream() );          
            this.SocketServerInputStream  	= new DataInputStream( this.SocketServer.getInputStream() );          
            
			this.SocketConnected 			= true;
			this.SocketErrorMessage 		= "";
		}
		catch(UnknownHostException uhe)
		{
			this.SocketError		= true;
			this.SocketConnected	= false;
			this.SocketErrorMessage = uhe.getMessage();
		}
		catch(IOException ioe){
			this.SocketError		= true;
			this.SocketConnected 	= false;
			this.SocketErrorMessage = ioe.getMessage();
		}	    		
		return this.SocketConnected;		
	}
	
	public String SendReceiveMessage(String RequestMessageToSend)
	{
		String InPutMessage  = RequestMessageToSend;
		String OutPutMessage = "";
		
		this.SocketError 		= false;
		this.SocketErrorMessage	= "";
		
		if( this.SocketServer != null && this.SocketServerInputStream != null && this.SocketServerOutputStream != null )
		{
			try{
				int  	BufferLeido 	= 0;
				byte 	DatosBytes[]	= new byte[512];			
				
				this.SocketServerOutputStream.write( InPutMessage.getBytes() );
				
				while ( ( BufferLeido = SocketServerInputStream.read( DatosBytes,0,DatosBytes.length ) ) != 0)  
				{
					if ( BufferLeido > 0 )
						OutPutMessage += new String( DatosBytes,0,( BufferLeido ) );
				}
			}
			catch(Exception e)
			{
				this.SocketError		= true;
				this.SocketErrorMessage = e.getMessage();
			}
		}		
		return OutPutMessage;		
	}
	
	public String SendReceiveMessageIO(String RequestMessageToSend)
	{        
		String InPutMessage		= RequestMessageToSend;
		String OutPutMessage 	= "";
		
		this.SocketError 		= false;
		this.SocketErrorMessage	= "";
		
		if( this.SocketServer != null && this.SocketServerInputStream != null && this.SocketServerOutputStream != null )
		{
			try
			{
				oSocketServerInputStream = new BufferedReader(new InputStreamReader(SocketServer.getInputStream()));
                oSocketServerOutputStream = new PrintWriter( new OutputStreamWriter(SocketServer.getOutputStream()),true);           
            
                if (SocketServer != null && oSocketServerOutputStream != null && oSocketServerInputStream != null)
                {              
                    oSocketServerOutputStream.println(InPutMessage);
                    OutPutMessage = oSocketServerInputStream.readLine();				
                }
			}
			catch(Exception e)
			{
				System.out.print("Error: " + e.getMessage());
				this.SocketError		= true;
				this.SocketErrorMessage = e.getMessage();				
			}			
		}		
		return OutPutMessage;		
	}	
	
	public void CloseConnection()
	{
		try
	    {
            if( this.SocketServer !=null ) SocketServer.close();
            
            if( this.SocketServerInputStream != null ) SocketServerInputStream.close();
            if( this.SocketServerOutputStream != null ) SocketServerOutputStream.close();
            
            if( this.oSocketServerInputStream != null ) oSocketServerInputStream.close();
            if( this.oSocketServerOutputStream != null ) oSocketServerOutputStream.close();  
            
            this.SocketError		= false;
            this.SocketConnected 	= false;
            this.SocketErrorMessage	= "";
        }
        catch(IOException ioe)
        {
        	this.SocketError		= true;
        	this.SocketErrorMessage = ioe.getMessage();
        }
	}	
}
