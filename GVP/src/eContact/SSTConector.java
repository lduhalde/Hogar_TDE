package eContact;

import java.io.*;
import java.net.*;

public class SSTConector {
	private String 				ecTransactionID;
	private String 				ecUserName;
	private String 				ecPassword;
	private String 				ecVersion;
	private String 				ecCurrentID;
	private String 				ecProvider;
	private String 				ecService;
	private String				ecRawType;
	private String				ecMaxRowCount;
	private String  			ecUserData;
	private String  			ecResponse;
	private String  			ecResultMessage;
	private String				ecMessageToSend;
	
	private Socket				SocketServer;
	private DataOutputStream    SocketServerOutputStream;
	private DataInputStream    	SocketServerInputStream;
	private int     			ecResultCode;
	private int    				ecSequence;
	private int    				ecTimeOut;
	
	private boolean 			ecPersistent;
	private boolean 			SocketConnected;
	
	public SSTConector()
	{
		ecTransactionID = "SSTT";
		ecUserName      = "";
		ecPassword      = "";
		ecVersion       = "6";
		ecCurrentID     = "";
		ecProvider      = "";
		ecService       = "";
		ecUserData      = "";
		ecResponse      = "";
		ecResultMessage = "";
		ecRawType       = "2";
		ecMaxRowCount   = "0";
		ecMessageToSend = "";
				
		SocketServer    			= null;
		SocketServerOutputStream 	= null;
		SocketServerInputStream  	= null;
		SocketConnected				= false;
		
		ecSequence      = new Double( Math.random() * 999).intValue();
		ecResultCode    = 0;
		ecTimeOut       = 60;
		ecPersistent    = false;
	}
	
	public String ResponseMessage(){ 
		return this.ecResponse; 
	}
	
	public String ResultMessage(){ 
		return this.ecResultMessage; 
	}
	
	public int ResultCode(){ 
		return this.ecResultCode; 
	}
	
	public String RequestMessage(){
		return this.ecMessageToSend;
	}

	/**
	 * 
	 */
	private void RemoveErrores(){
		this.ecResultCode 		= 0;
		this.ecResultMessage 	= "";
	}
	
	/**
	*   
	* @param InputValue
	* @param AddChar
	* @param MaxLen
	* @param FromLeftSide
	* @return
	**/
	private static String Mask(String InputValue, String AddChar, int MaxLen, Boolean FromLeftSide){
		int 	auxI = 0;
		int		LenDiff = 0;
		String 	OutputValue = "";
		
		if( InputValue.length() == MaxLen )
		{
			return InputValue;
		}
			
		if( InputValue.length() > MaxLen )
		{
			return InputValue.substring(1, MaxLen);
		}
			
		LenDiff = MaxLen - InputValue.length() ;
		
		if( !FromLeftSide )
		{
			for( auxI = 0; auxI < LenDiff; auxI++ )
			{
				InputValue = InputValue + AddChar; 
			}				
			OutputValue = InputValue;
		}
		else
		{
			for( auxI = 0; auxI < LenDiff; auxI++ )
			{
				InputValue = AddChar + InputValue; 
			}				
			OutputValue = InputValue;
		}			
		return OutputValue;		
	}
	
	/**
	 * 
	 * @param newValue
	 * @return
	 */
	private String RemoveHTMLEscapeCharacters(String newValue){
		String OutputMessage = "";
		
		if( newValue.compareTo("") != 0 )
		{
			OutputMessage = newValue;
		
			OutputMessage = OutputMessage.replace("&lt;", "<");
	        OutputMessage = OutputMessage.replace("&gt;", ">");
	        OutputMessage = OutputMessage.replace("&quot;", "'\'");
	        OutputMessage = OutputMessage.replace("&amp;", "&");
	        OutputMessage = OutputMessage.replace("&nbsp;", " ");
	        OutputMessage = OutputMessage.replace("''", "\"");
	        OutputMessage = OutputMessage.replace(" ?", "?");
		}
		return OutputMessage;
	}
	
	/**
	 * 
	 * @param OutPutMessage
	 * @return
	 */
	public String RemoveNoXMLInfo(String OutPutMessage)
	{
		String mvarTransactionId	= "";
		int    mvarVersion       	= 0;
		int    mvarLength			= 0;
		
		String ResponseXMLMessage 	= OutPutMessage.trim();

		if( ResponseXMLMessage.compareTo("") == 0 )
			return ResponseXMLMessage;
		
		try{
			mvarTransactionId 	= ResponseXMLMessage.substring(0, 4);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(4);
			
			mvarVersion         = Integer.parseInt(ResponseXMLMessage.substring(0,3));
			ResponseXMLMessage 	= ResponseXMLMessage.substring(3);

			ResponseXMLMessage  = ResponseXMLMessage.substring(8);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(12);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(8);

			mvarLength			= Integer.parseInt(ResponseXMLMessage.substring(0,8));
			ResponseXMLMessage 	= ResponseXMLMessage.substring(8);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(mvarLength);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(60);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(60);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(1);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(11);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(3);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(5);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(14);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(8);

			mvarLength			= Integer.parseInt(ResponseXMLMessage.substring(0,5));
			ResponseXMLMessage 	= ResponseXMLMessage.substring(5);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(mvarLength);
			ResponseXMLMessage 	= ResponseXMLMessage.substring(4);
			 
			mvarLength			= Integer.parseInt(ResponseXMLMessage.substring(0,8));
			ResponseXMLMessage 	= ResponseXMLMessage.substring(8);										
		}
		catch(IndexOutOfBoundsException e){
			this.ecResultCode    = -1;
			this.ecResultMessage = e.getMessage();
			return this.ecResultMessage;		
		}
		
		if( mvarTransactionId.compareToIgnoreCase("SSTT") != 0 )
		{
			this.ecResultCode    = 101;
			this.ecResultMessage = "Error recibiendo respuesta desde ECC.";
			return this.ecResultMessage;					
		}		
				
		if( mvarVersion != 6 )
		{
			this.ecResultCode    = 102;
			this.ecResultMessage = "Error recibiendo respuesta: Numero de version no valido.";
			return this.ecResultMessage;			
		}
		return ResponseXMLMessage;
	}
		
	/**
	 * 
	 * @param RemoteHost
	 * @param RemotePort
	 * @param ConnectionTimeOut
	 * @param ReadWriteTimeOut
	 * @return
	 */
	public boolean OpenConnection(String RemoteHost, int RemotePort, int ConnectionTimeOut, int ReadWriteTimeOut)
	{
		try{
			RemoveErrores();
			
			this.SocketServer 			 = new Socket();
			InetSocketAddress SocketAddr = new InetSocketAddress( RemoteHost, RemotePort );
			
			this.SocketServer.connect( SocketAddr, ConnectionTimeOut );
			this.SocketServer.setKeepAlive( true );
			this.SocketServer.setSoTimeout( ReadWriteTimeOut );							
            
			this.SocketServerOutputStream 	= new DataOutputStream( this.SocketServer.getOutputStream() );          
            this.SocketServerInputStream  	= new DataInputStream( this.SocketServer.getInputStream() );	            
			this.SocketConnected 			= true;
		}
		catch(UnknownHostException uhe){
			this.SocketConnected = false;
			this.ecResultCode    = -1;
			this.ecResultMessage = uhe.getMessage();
		}
		catch(IOException ioe){
			this.SocketConnected = false;
			this.ecResultCode    = -1;
			this.ecResultMessage = ioe.getMessage();
		}	    
		
		return this.SocketConnected;
	}
	
	/**
	 * 
	 * @param oAsteriskID
	 * @param oUser
	 * @param oPassword
	 * @param oService
	 * @param oProvider
	 * @param oUserData
	 * @param oIsPersistent
	 * @param oMessageFromIVR
	 */
	public void SendReceiveMessage(String oAsteriskID, String oUser, String oPassword, String oService, String oProvider, String oUserData, boolean oIsPersistent, String oMessageFromIVR)
	{		
		String  FormatedMessage = "";
		String	OutputMessage   = "";
		
		RemoveErrores();
		
		this.ecCurrentID  = oAsteriskID;
		this.ecUserName   = oUser;
		this.ecPassword   = oPassword;
		this.ecService    = oService;
		this.ecProvider   = oProvider;
		this.ecUserData   = oUserData;
		this.ecPersistent = oIsPersistent;			
		
		FormatedMessage += Mask(this.ecTransactionID," ",4,false);
		FormatedMessage += Mask(this.ecVersion,"0",3,true);
		FormatedMessage += Mask(this.ecUserName," ",8,false);
		FormatedMessage += Mask(this.ecPassword," ",8,false);
		FormatedMessage += Mask(String.valueOf(this.ecSequence),"0",12,true);
		FormatedMessage += Mask(String.valueOf(this.ecTimeOut),"0",8,true);
		FormatedMessage += Mask(String.valueOf(this.ecCurrentID.length()),"0",8,true);
		FormatedMessage += this.ecCurrentID;
		FormatedMessage += Mask(this.ecProvider," ",60,false);
		FormatedMessage += Mask(this.ecService," ",60,false);
	
		if( this.ecPersistent )
			FormatedMessage += "S";
		else
			FormatedMessage += "N";
	
		FormatedMessage += Mask(this.ecUserData," ",11,false);
		FormatedMessage += Mask(this.ecMaxRowCount,"0",9,true);
		FormatedMessage += Mask(this.ecRawType,"0",4,true);
		FormatedMessage += Mask(String.valueOf(oMessageFromIVR.length()),"0",8,true);
		FormatedMessage += oMessageFromIVR;
		
		this.ecMessageToSend = FormatedMessage;
		
		if( this.SocketServer != null && this.SocketServerInputStream != null && this.SocketServerOutputStream != null )
		{
			try{
				int  	BufferLeido 	= 0;
				byte 	DatosBytes[]	= new byte[512];			
				
				this.SocketServerOutputStream.write( FormatedMessage.getBytes() );
				
				while ( ( BufferLeido = SocketServerInputStream.read( DatosBytes,0,DatosBytes.length ) ) != 0)  
				{
					if ( BufferLeido > 0 )
						OutputMessage += new String( DatosBytes,0,( BufferLeido ) );
				}
			}
			catch(Exception e){
				this.ecResultCode = -1;
				this.ecResultMessage = e.getMessage();
			}
		}
		
		this.ecResultCode	= 0;
		this.ecResponse 	= RemoveHTMLEscapeCharacters( OutputMessage );
	}
	
	/**
	 * 
	 */
	public void CloseConnection(){
        try
        {
        	RemoveErrores();
        	
            if( this.SocketServer !=null ) SocketServer.close();
            if( this.SocketServerInputStream != null ) SocketServerInputStream.close();
            if( this.SocketServerOutputStream != null ) SocketServerOutputStream.close();
            
            this.SocketConnected = false;
        }
        catch(IOException ioe){
        	this.ecResultCode    = -1;
        	this.ecResultMessage = ioe.getMessage();
        }
	}
}
