package eContact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FunctionsGVP {

	protected String InstanceID = "";
	
	/*ARCHIVO PARAMETROS*/
	public Parameters Params = new Parameters();		

	/*INICIO Variables Log*/
	protected Logger log;
	private String loggerName;
	private String loggerNameError;
	
	
	protected String DebugLevel = "";
	protected String DebugFilePath = "";
	protected String ErrorFilePath = "";
	protected String DebugFileMaxSize = "20971520";
	protected String Timezone = "America/Buenos_Aires";
	/*FIN Variables Log*/
	
	/*DB Conectors*/
	public KVPairList			RoutingKVPairs			= new KVPairList();
	public KVPairList 			RecordSetKVPs			= new KVPairList();
		
//	private DBAccess			RoutingDAP				= new DBAccess();
	private OracleDBAccess      DataAccessPointOracle   = new OracleDBAccess();
	
	/*Sockets Conectors*/
	private Sockets				SocketRequest			= new Sockets();
//	private SSTConector			SSTSockets				= new SSTConector();
	public boolean				SocketIsOK				= false;
	
	private URL urlFile = null;
		
	private boolean DVesK = false;
	
	public Date fechaDate = null;
	public String fechaString = "";
	
	
	public FunctionsGVP(String ParametersFile) {
		String catalina = System.getProperty("catalina.base");
    	if(catalina != null)
    		if (ParametersFile.equals("")){    		
    			ParametersFile = catalina + "//lib//FunctionsGVP.properties";    		    		
	    	}else{
	    		ParametersFile = catalina + "//lib//"+ParametersFile;
	    	}
    	
		Inicializar(ParametersFile);
//        this.InstanceID = id;               
    }
	
	public FunctionsGVP(String ParametersFile, String id) {
		String catalina = System.getProperty("catalina.base");
		if(catalina != null)
    		if (ParametersFile.equals("")){    		
    			ParametersFile = catalina + "//lib//FunctionsGVP.properties";    		    		
	    	}else{
	    		ParametersFile = catalina + "//lib//"+ParametersFile;
	    	}		

		Inicializar(ParametersFile);
        this.InstanceID = id;               
    }
	
	
	 /**
     * Inicializa datos para el registro de log
     * @param ParametersFile Archivo de parámetros.
     */
    private void Inicializar(String ParametersFile) {

        InstanceID = (new Long((new Random()).nextLong()).toString());
        
        if( InstanceID.substring(0, 1).compareTo("-") == 0 )
            InstanceID = InstanceID.substring(1);
        
        InstanceID = "00000000000000000000" + InstanceID;
        InstanceID = InstanceID.substring(InstanceID.length() - 20);
        
        ReadParameters(ParametersFile);
        
        loggerName = Params.GetValue("LoggerName", "GVP");
        loggerNameError = Params.GetValue("LoggerNameError", "GVP_ERROR");
        DebugLevel = Params.GetValue("DebugLevel", "None");
        DebugFilePath = Params.GetValue("DebugFilePath");
        DebugFileMaxSize = Params.GetValue("DebugFileMaxSize", "20971520");
        
        ErrorFilePath = Params.GetValue("ErrorFilePath");
        Timezone = Params.GetValue("Timezone");
        
 //       InicializarLogger();
//        Debug("FunctionsGVP.Initialize - " + ParametersFile, "Detail");
        
    }
    
    
    public void InicializarLogger(){
    	log = Logger.getLogger(loggerName);
    }
    
    public void logger(String Message, String Level){    	    	

        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(Timezone));
        Date curDate = new Date();
        //CREA UN ARCHIVO POR DIA (Y ES BASE PARA OTROS LOGS)
        String DateToStr = format.format(curDate);
        SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
        formatHora.setTimeZone(TimeZone.getTimeZone(Timezone));
        String Hora = formatHora.format(curDate);
        log = Logger.getLogger(loggerName);
        Message = DateToStr+ " "+Hora+" "+Rellena(Level, " ", 10, 1)+" ["+this.InstanceID + "] " + Message;       
        if (DebugLevel.equalsIgnoreCase("Detail") || DebugLevel.equalsIgnoreCase("Trace") || DebugLevel.equalsIgnoreCase("DEBUG")){
        	log.debug(Message);
        }else{
            if (Level.equalsIgnoreCase("Standard") || Level.equalsIgnoreCase("INFO")){
        		log.debug(Message);
            }
        }
    }
	
    
    public void loggerError(String Message){    	    	

    	SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
    	format.setTimeZone(TimeZone.getTimeZone(Timezone));
    	Date curDate = new Date();
    	//CREA UN ARCHIVO POR DIA (Y ES BASE PARA OTROS LOGS)
    	String DateToStr = format.format(curDate);
    	
    	SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
    	formatHora.setTimeZone(TimeZone.getTimeZone(Timezone));
        String Hora = formatHora.format(curDate);
   	
    	log = Logger.getLogger(loggerNameError);
  	    	    	
    	Message = DateToStr+ " "+Hora+" "+Rellena("ERROR", " ", 10, 1)+" ["+this.InstanceID + "] " + Message;    	

    	log.error(Message); 
    	
    }
    
    /**
     * Permite registrar mensajes en un archivo de log.
     * Los parámetros para generar el archivo de log están definidos en el constructor utilizado.
     * @param Message Mensaje a registrar en archivo de log.
     * @param Level Nivel de log (None, Standard, Trace, Detail)
     * @return True: Éxito<br>
     * False: Error
     */
    public boolean Debug(String Message) {
    	return Debug(Message, "INFO");
    }
   
    
    /**
     * Permite registrar mensajes en un archivo de log.
     * Los parámetros para generar el archivo de log están definidos en el constructor utilizado.
     * @param Message Mensaje a registrar en archivo de log.
     * @param Level Nivel de log (None, Standard, Trace, Detail)
     * @return True: Éxito<br>
     * False: Error
     */
    public boolean Debug(String Message, String Level) {
        logger(Message, Level);
        return true;
    }
	
    
    /**
     * Permite registrar mensajes de error en un archivo de log dedicado para esta tarea.
     * El archivo de error se generará según lo definido en el parámetro <b>ErrorFilePath</b> del constructor utilizado..
     * @param Message Mensaje de error a registrar en archivo de log.
     */
    public void DebugError(String Message) {    	
    	loggerError(Message);
        return;
    }
    
   	public void VerInfoMap(String mensaje, Map<String, String> mapa) {

    	Set<String> keys = mapa.keySet();
    	for (String key : keys) {
    		logger(mensaje+" DATOS "+key+"="+mapa.get(key), "DEBUG");
		}
    }
    
    public void VerInfoProperties(String mensaje, Properties properties) {

    	Set<Object> keys = properties.keySet();
    	for (Object key : keys) {
    		logger(mensaje+" DATOS "+key.toString()+"="+properties.get(key.toString()).toString(), "DEBUG");
		}
    }
    
    
    /**
     * Lee parámetros desde un archivo de parámetros.<br>
     * Utilizar junto a método <b>GetValue</b> de la clase Parameters.
     * @param ParametersFile Archivo de parámetros.
     */
    public void ReadParameters(String ParametersFile) {
        if (! Params.ReadParametersFile(ParametersFile)){
        	System.out.println("FunctionsGVP.Leyendo archivo de parámetros [" + ParametersFile +"]");
            System.out.println("FunctionsGVP.No se pudo leer archivo [" + ParametersFile + "]");
        }
            
    }
    
    /**
     * Método que busca un archivo dentro su entorno ClassLoader si lo encuentra devuelve true y  almacena el path en urlFile
     * @param fileName Nombre Archivo.
     * @return true o false.<br>
     * <b>urlFile</b> Path del Archivo (variable public).
     */
	private boolean getFile(String fileName){  
        this.urlFile = null;
        try
        {
            this.urlFile = this.getClass().getClassLoader().getResource(fileName);  //Archivo buscado, ej. Fonobank.FunctionsGVP.properties
            return true;
        }
        catch(Exception e)
        {
        	DebugError("FunctionsGVP.getFile.IOException [" + e.getMessage() + "]");
        	return false;
        }
    }
    
    /**
	 * Método utilizado en Fonobank Personas y empresas y crediChile
	 *  el cual devuelve la URL del servicio
	 * ejemplo:
	 *
	 * @param Codigo, nombre del archivo
	 * @return URL
	 */
    public String ObtenerParametroProperties(String Nombre, String Archivo){ 
     String url = "";     
    // Archivo = "/usr/local/tomcat/shared/lib/ConfiguracionServiciosWeb.properties";
     //Archivo = "ConfiguracionServiciosWeb.properties";
     String r = "";
     try{               
        
        Properties pMov = new Properties();
        if (getFile(Archivo))
        {
           pMov.load(this.urlFile.openStream());
           try
           {                              
        	   url = pMov.getProperty(Nombre);
        	   if ( !url.equals("") ){        		   				
            	   r = url;   				   
        	   }        	                 
           }
           catch(Exception e){           	  
        	   DebugError("FunctionsGVP.ObtenerParametroProperties.Exception [" + e.getMessage() + "]");              
           }                      
        }        
      }catch(IOException e){
    	  DebugError("FunctionsGVP.ObtenerParametroProperties.IOException [" + e.getMessage() + "]");          
      }finally{
    	  url = r;          
      }
     	
     return url;
    }
    
    
    
    /**
     * Método para validar digito verificador.
     * @param RUT  Rut
     * @param DV  Digito verifricador
     * @param EquivalenciaK equivalencia "K"
     * @return True o False
     */    
    public boolean ValidaRUT(String RUT, String DV, String EquivalenciaK) {
        try{
            int auxI;
            int auxJ = 2;
            int auxSuma = 0;
            int auxDV;
            boolean auxB = false;
            DVesK = false;
            
            Debug("FunctionsGVP.Validando RUT: " + RUT + "-" + DV + " (EquivalenciaK=" + EquivalenciaK + ").", "Detail");
            
            for( auxI = RUT.length() - 1 ; auxI >= 0 ; auxI-- ) {
                auxSuma += Integer.parseInt(RUT.substring(auxI, auxI + 1)) * auxJ;
                
                if( ++auxJ > 7 )
                    auxJ = 2;
            }
            
            auxDV = 11 - (auxSuma % 11);
            
            if( auxDV < 10 )            // Corresponde a digito calculado
            {
                if( Integer.parseInt(DV) == auxDV )
                    auxB = true;
            } else if( auxDV == 10 )        // Corresponde a letra K
            {
                // if( Integer.parseInt(DV) == Integer.parseInt(EquivalenciaK) ){
                if(DV.equals(EquivalenciaK)){
                    auxB = true;
                    DVesK = true;
                }
            } else                        // Corresponde a digito cero
            {
                if( Integer.parseInt(DV) == 0 )
                    auxB = true;
            }
            
            Debug("FunctionsGVP.    RUT: " + RUT + "-" + DV + " " + (auxB ? "valido" : "invalido") + ".", "Trace");
            
            return auxB;
        }catch(Exception e) { 
        	DebugError("FunctionsGVP.ValidaRUT.Exception [" + e.getMessage() + "]");
        	e.printStackTrace();
        	return false;
        }
    }

    
    /* INICIO Retorno de Variables del FunctionsGVP*/

    public String getInstanceID() {
		return InstanceID;
	}


	public boolean isDVesK() {
		return DVesK;
	}	
	 /*
	  *  FIN Retorno de Variables del FunctionsGVP
	  *  */
	
	
	/*
	 *	INICIO Funciones Genericas para Fechas y Validacion de Horario 	  
	 * */	
	
	public String obtenerHostname(){
		String hostname = "";
		try 
		{
			InetAddress address = InetAddress.getLocalHost();
			hostname = address.getHostName();
		}
		catch (UnknownHostException e) 
		{		
			DebugError("USUARIO : ERROR " + e.getMessage());
			e.printStackTrace();
		}
		return hostname;
	}
	
	public String obtenerHostaddress(){
		String hostaddress = "";
		try 
		{
			InetAddress address = InetAddress.getLocalHost();
			hostaddress = address.getHostAddress();
		}
		catch (UnknownHostException e) 
		{		
			DebugError("USUARIO : ERROR " + e.getMessage());
			e.printStackTrace();
		}
		return hostaddress;
	}
	
	public String obtenerDirectorioTomcatWindows(){
    	return System.getProperty("catalina.base");
    }
	
	public int obtenerTimestampEpoch(){
		return (int) (System.currentTimeMillis() / 1000L);
	}
	
	
	/*
	 * Metodo que lee el Status de los Speech Server
	 * Verifica si el Text To Speech debe estar ACTIVO o INACTIVO
	 * Flujos de IVR cambian sus Input acorde a este valor
	 * */
	public boolean verificarTTS(){
    	String status = ObtenerParametroProperties("TTS_Estatus", "ConfiguracionServiciosWeb.properties");
    	
    	if (status.equals("ACTIVO")){
    		return true;
    	}else{
    		return false;
    	}
    }
	
	/*
	 * Metodo que lee el Status de los Speech Server
	 * Verifica si el Automatic Speech Recoginition debe estar ACTIVO o INACTIVO
	 * Flujos de IVR cambian sus Input acorde a este valor
	 * */
	public boolean verificarASR(){
    	String status = ObtenerParametroProperties("ASR_Estatus", "ConfiguracionServiciosWeb.properties");
    	
    	if (status.equals("ACTIVO")){
    		return true;
    	}else{
    		return false;
    	}
    }
	
	
	/** Metodo para rellenar cadenas
     * Orden = 1:derecha   2:izquierda
     * @params valor String
     * @params caracter String
     * @params largo Int
     * @params orden Int
     * @return valor
     */
 public String Rellena(String valor, String caracter, int largo, int orden)
 {
     int largoV = valor.length();

     if(orden == 1)
     {
         for(int i = largoV; i<largo; i++)
         {
             valor = valor + caracter;
         }
     }
     else
     {
         for(int i = largoV; i<largo; i++)
         {
             valor = caracter + valor;
         }
     }

     return valor;
 }
 
 public static String tiraceros ( String Numero)
 {
  /*
  Elimina los ceros a la izquierda de una cantidad conservando el signo
  */
     boolean bTieneSigno = false;
     String sCero = "0";
     String sSigno = "-";
     String sCantidad = Numero;

     while ( sCantidad.length() > 0 && (sCantidad.substring(0,1).equals(sCero) || sCantidad.substring(0,1).equals(sSigno) )) {

         if (sCantidad.substring(0,1).equals(sCero))
             sCantidad = sCantidad.substring(1,sCantidad.length());
         else if (sCantidad.substring(0,1).equals(sSigno)) {
             bTieneSigno = true;
             sCantidad = sCantidad.substring(1,sCantidad.length());
         }
     }
     if ( sCantidad.length() == 0 )
         sCantidad = sCero + sCero; // los dos decimales ...

     if ( bTieneSigno )
         sCantidad = sSigno + sCantidad;

     return sCantidad;
 }
 
 public String rpad(String data, int length) {
     return rpad(data, length, " ");
 }



 public String rpad(String data, int length, String filler) {
     return Rellena(data, filler, length, 1);
 }



 public String lpad(String data, int length) {
     return lpad(data, length, "0");
 }



 public String lpad(String data, int length, String filler) {
     return Rellena(data, filler, length, 2);
 }

	
	 public String getFechaActual(String formato) throws ParseException{ 
//	    String zonaHoraria = ObtenerParametroProperties("Timezone", "ConfiguracionServiciosWeb.properties");
	    	
	    TimeZone tz = TimeZone.getTimeZone(Timezone); 
	        
	    SimpleDateFormat DateFormatter = new SimpleDateFormat(formato);
	    DateFormatter.setTimeZone(tz);
	    fechaString = DateFormatter.format(new Date());      
	    fechaDate = DateFormatter.parse(fechaString);
	    return fechaString;
	 } 
    
	 public String getFechaAyer(String formato) {
		 	SimpleDateFormat dateFormat = new SimpleDateFormat(formato);
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -1);    
	        return dateFormat.format(cal.getTime());
	}
	 
	// Obtener el día de la semana, dada una Fecha
    public int getDayOfTheWeek(Date d){
    	GregorianCalendar cal = new GregorianCalendar();
    	cal.setTime(d);
    	return cal.get(Calendar.DAY_OF_WEEK);		
    }
    
    /*
	 *	FIN Funciones Genericas para Fechas y Validacion de Horario 	  
	 * */
    
    
    public long calcularDuracion(Date inicio, Date fin){

 		java.util.GregorianCalendar fechaIni = new java.util.GregorianCalendar();
 		fechaIni.setTime(inicio);
 	    
 	    java.util.GregorianCalendar fechaFin = new java.util.GregorianCalendar();
 	    fechaFin.setTime(fin);
 	    
 	    Date primer = fechaIni.getTime();
 	    Date ultimo = fechaFin.getTime();

 		long resta = ultimo.getTime() - primer.getTime();
 	    long minutos = (resta/(1000*60));
 	    long horas = (resta/(1000*60*60));	    
 	    long min = minutos - (horas*60);	   	   	   
 	    
 	    long miliSegIni = fechaIni.getTimeInMillis();
 	    long miliSegFin = fechaFin.getTimeInMillis();
 	    
// 	    long seg = (miliSegFin - miliSegIni)/1000;
// 	    
// 	    long totalSeg = 0;
// 	    if (min > 0){
// 	    	totalSeg = seg - (min*60);
// 	    }else{
// 	    	totalSeg = seg;
// 	    }

// 	    String duracion = ""+totalSeg;
 	    
 		return miliSegFin - miliSegIni;
 	}
    
    /**
     * 
     * @param RemoteHost
     * @param RemotePort
     * @param ConnectionTimeOut
     * @param ReadWriteTimeOut
     * @param Message2Send
     * @return
     */
    public String SendReceiveSocketMessageIO(String RemoteHost, int RemotePort, int ConnectionTimeOut, int ReadWriteTimeOut, String Message2Send)
    {
    	String OutPutMessage = "";
    	
    	Debug("[Functions - SendReceiveSocketMessageIO] (init)", "Standard");
    	Debug("[Functions - SendReceiveSocketMessageIO] Host: " + RemoteHost + ", Port: "+ String.valueOf(RemotePort) + ", TimeOut: "+ String.valueOf(ConnectionTimeOut) + " (ms)", "Trace");	
    	Debug("[Functions - SendReceiveSocketMessageIO] Input Message: " + Message2Send, "Trace");
    	
    	Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - Intentando conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    	
    	if( SocketRequest.OpenConnection(RemoteHost, RemotePort, ConnectionTimeOut, ReadWriteTimeOut) )
    	{
    		Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - conexion exitosa con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    		Debug("[Functions - SendReceiveSocketMessageIO (SendReceiveMessage)] - Enviando mensaje formateado: " + Message2Send, "Standard"); 
    		
    		OutPutMessage = SocketRequest.SendReceiveMessageIO(Message2Send);
    		
    		if( SocketRequest.IsSocketWithError() )
    		{
    			Debug("[Functions - SendReceiveSocketMessageIO (SendReceiveMessage)] - Problemas al enviar/recibir mensaje '[" + SocketRequest.GetErrorMessage() + "'].", "Standard");
    			
    			SocketIsOK = false;
    			OutPutMessage = "";
    		}
    		else
    		{
    			SocketIsOK = true;
    			Debug("[Functions - SendReceiveSocketMessageIO] - Se ha recibido el siguiente mensaje: '" + OutPutMessage + "'", "Standard");
    		}
    		
    		Debug("[Functions - SendReceiveSocketMessageIO (CloseConnection)] - Cerrando conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");		
    		SocketRequest.CloseConnection();
    		
    		if( SocketRequest.IsSocketWithError() )
    			Debug("[Functions - SendReceiveSocketMessageIO (CloseConnection)] - Problemas al cerrar conexi�n '[" + SocketRequest.GetErrorMessage() + "'].", "Standard");   			
    	}
    	else
    	{
    		Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - Problemas al realizar conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    		
    		if( SocketRequest.IsSocketWithError() )
    			Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - " + SocketRequest.GetErrorMessage() + "'.", "Standard");
    		
    		SocketIsOK = false;
    	}
    	
    	Debug("[Functions - SendReceiveSocketMessageIO] (stop)", "Standard");
    	
    	return OutPutMessage;
    }    
    
    public boolean WriteLocalFile(String InputFileName, String InputMessage, boolean AppendMessage)
    {
    	return ( WriteLocalFile(InputFileName, InputMessage, AppendMessage, true) );
    }
    
    public boolean WriteLocalFile(String InputFileName, String InputMessage, boolean AppendMessage, boolean PrintDetails)
    {
    	boolean				IsOK				= false;
    	FileOutputStream 	oFileOutputStream	= null;
    	
    	Debug("[Functions - WriteLocalFile] (init)", "Standard");
    	
        try
        {
            File oLocalFile	= new File(InputFileName);

            oLocalFile.createNewFile();

            if( oLocalFile.canWrite() )
            {
            	if( AppendMessage )
            		oFileOutputStream = new FileOutputStream( oLocalFile, true );
            	else
            		oFileOutputStream = new FileOutputStream( oLocalFile, false );
            	            	
                oFileOutputStream.write( InputMessage.getBytes() );
                oFileOutputStream.close();
                
                IsOK = true;
                
                if (PrintDetails == true )
                	Debug("[Functions - WriteLocalFile] - Se ha registrado la informacion '" + InputMessage + "' en forma exitosa.", "Standard");
            }
            else
            {
            	IsOK = false;
            	Debug("[Functions - WriteLocalFile] - Problemas al escribir archivo local '" + InputFileName + "'.", "Standard");
        	}
        }

        catch( Exception e )
        {
        	IsOK = false;
        	Debug("[Functions - WriteLocalFile] - Se ha detectado el siguiente Error: " + e.getMessage(), "Trace");      	
        }    	
    	
    	Debug("[Functions - WriteLocalFile] (stop)", "Standard");
    	
    	return IsOK;
    }
    
    /**
     * 
     * @param pConnectionURL
     * @param pUserName
     * @param pPassword
     * @param DriverName
     * @param pReturnRecorset
     * @param pSQLQuery
     * @param ConnectionTimeOut
     * @param QueryTimeOut
     * @return
     */
    public boolean ExecuteSQLQuery(String pConnectionURL, String pUserName, String pPassword, String DriverName, boolean pReturnRecorset, String pSQLQuery, int ConnectionTimeOut, int QueryTimeOut)
    {
    	boolean				Retorno				= false;
    	int					oIndex				= 0;
    	int					oRowsAffected		= 0;
    	String				RecordName			= "";
    	String				RecordValue 		= "";
    	DBAccess			LocalDAP			= new DBAccess();
    	ArrayList<String>	RecordFieldName		= new ArrayList<String>(1);    	
    	ResultSet			OutputRecorset		= null;
    	ResultSetMetaData	eResultSetMetaData	= null;

    	Debug("[Functions - ExecuteSQLQuery] (init)", "Standard");
    	Debug("[Functions - ExecuteSQLQuery] *** ConnectionURL:" + pConnectionURL, "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** DriverName   :" + DriverName, "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** UserName     :" + pUserName, "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** Password     :" + pPassword, "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** Out Recordset:" + String.valueOf(pReturnRecorset), "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** Connection TO:" + String.valueOf(ConnectionTimeOut), "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** Query TimeOut:" + String.valueOf(QueryTimeOut), "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** SQL Sentence :" + pSQLQuery, "Trace");
    	
    	RecordSetKVPs.clear();
    	
    	if( LocalDAP.OpenDataBase(pConnectionURL, DriverName, pUserName, pPassword, ConnectionTimeOut) )
    	{
    		Debug("[Functions - ExecuteSQLQuery] - conexion exitosa con la Base de Datos '" + pConnectionURL + "',", "Standard");    	 

    		Debug("[Functions - ExecuteSQLQuery] - Ejecutando Query en la Base de Datos '" + pSQLQuery + "'", "Standard");

    		if( pReturnRecorset == true )
    		{
    			OutputRecorset = LocalDAP.ExecuteQuery(pSQLQuery, QueryTimeOut);
    		}
    		else
    		{
    			oRowsAffected 	= LocalDAP.ExecuteUpdate( pSQLQuery, QueryTimeOut);

    			Retorno 		= true;
    			OutputRecorset 	= null;

    			Debug("[Functions - ExecuteSQLQuery] - Rows Affected: " + String.valueOf(oRowsAffected) + "',", "Standard");
    		}

    		if( OutputRecorset != null )
    		{
    			try {
    				eResultSetMetaData = OutputRecorset.getMetaData();

    				for( oIndex = 1; oIndex <= eResultSetMetaData.getColumnCount(); oIndex++ )
    					RecordFieldName.add( eResultSetMetaData.getColumnName( oIndex ) );

    				while( OutputRecorset.next() )
    				{
    					for( oIndex = 0; oIndex < RecordFieldName.size(); oIndex++ )
    					{
    						RecordName	= RecordFieldName.get(oIndex);																
    						RecordValue	= OutputRecorset.getString( RecordName );
	
    						if( RecordName != null )
    						{
    							if( RecordValue != null )
    								RecordSetKVPs.add( RecordName, RecordValue );
    							else
    								RecordSetKVPs.add( RecordName, "" );
    						}
    					}
    				}
    				Retorno = true;
    			} 
    			catch (SQLException e) 
    			{
    				Retorno = false;
    				Debug("[Functions - ExecuteSQLQuery] - Error: " + e.getMessage(), "Standard");
    			}

    			Debug("[Functions - ExecuteSQLQuery] - Resultado exitoso de la Query en la Base de Datos", "Standard");
    			Debug("[Functions - ExecuteSQLQuery] - Numero Total de KVPairs: " + String.valueOf(RecordSetKVPs.count()), "Standard");

    			if( RecordSetKVPs.count() > 0 )
    			{
    				for ( oIndex = 0; oIndex < RecordSetKVPs.count(); oIndex++ ) 
    				{
    					Debug("[Functions - ExecuteSQLQuery] - KeyName: " + RecordSetKVPs.getKey(oIndex) +", KeyValue: " + RecordSetKVPs.getValue(oIndex), "Standard");	
    				}
    			}
    			LocalDAP.CloseDataBase();
    		}
    		else
    		{
    			if( LocalDAP.GetErrorMessage() != "" )
    			{
    				Debug("[Functions - ExecuteSQLQuery] - Problemas al ejecutar query '" + pSQLQuery + "'", "Standard");
    				Debug("[Functions - ExecuteSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
    			}

    			if( pReturnRecorset )
    			{
    				Retorno			= false;
    				RecordSetKVPs	= null;
    			}

    			LocalDAP.CloseDataBase();
    		}
    	}
    	else
    	{
    		Debug("[Functions - ExecuteSQLQuery] - Problemas al conectar con la Base de Datos '" + pConnectionURL + "'.", "Standard");
    		Debug("[Functions - ExecuteSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
    	}
    	Debug("[Functions - ExecuteSQLQuery] (stop)", "Standard");
    
    	return Retorno;
    }    
    
    
    public boolean ExecutePLSQLQuery(String pConnectionURL, String pUserName, String pPassword, boolean pReturnRecorset, String pSQLQuery, int ConnectionTimeOut, int QueryTimeOut)
    {
    	boolean				Retorno				= false;
    	int					oIndex				= 0;
    	int					oRowsAffected		= 0;
    	String				RecordName			= "";
    	String				RecordValue 		= "";
    	OracleDBAccess		LocalDAP			= new OracleDBAccess();
    	ArrayList<String>	RecordFieldName		= new ArrayList<String>(1);    	
    	ResultSet			OutputRecorset		= null;
    	ResultSetMetaData	eResultSetMetaData	= null;

    	Debug("[Functions - ExecutePLSQLQuery] (init)", "Standard");
    	Debug("[Functions - ExecutePLSQLQuery] *** ConnectionURL:" + pConnectionURL, "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** UserName     :" + pUserName, "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** Password     :" + pPassword, "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** Out Recordset:" + String.valueOf(pReturnRecorset), "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** Connection TO:" + String.valueOf(ConnectionTimeOut), "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** Query TimeOut:" + String.valueOf(QueryTimeOut), "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** SQL Sentence :" + pSQLQuery, "Trace");
    	
    	RecordSetKVPs.clear();
    	
    	if( LocalDAP.OpenDataBase(pConnectionURL, pUserName, pPassword, ConnectionTimeOut) )
    	{
    		Debug("[Functions - ExecutePLSQLQuery] - conexion exitosa con la Base de Datos '" + pConnectionURL + "',", "Standard");    	 

    		Debug("[Functions - ExecutePLSQLQuery] - Ejecutando Query en la Base de Datos '" + pSQLQuery + "'", "Standard");

    		if( pReturnRecorset == true )
    		{
    			OutputRecorset = LocalDAP.ExecuteQuery(pSQLQuery, QueryTimeOut);
    		}
    		else
    		{
    			oRowsAffected 	= LocalDAP.ExecuteUpdate( pSQLQuery, QueryTimeOut);

    			Retorno 		= true;
    			OutputRecorset 	= null;

    			Debug("[Functions - ExecutePLSQLQuery] - Rows Affected: " + String.valueOf(oRowsAffected) + "',", "Standard");
    		}

    		if( OutputRecorset != null )
    		{
    			try {
    				eResultSetMetaData = OutputRecorset.getMetaData();

    				for( oIndex = 1; oIndex <= eResultSetMetaData.getColumnCount(); oIndex++ )
    					RecordFieldName.add( eResultSetMetaData.getColumnName( oIndex ) );

    				while( OutputRecorset.next() )
    				{
    					for( oIndex = 0; oIndex < RecordFieldName.size(); oIndex++ )
    					{
    						RecordName	= RecordFieldName.get(oIndex);																
    						RecordValue	= OutputRecorset.getString( RecordName );
	
    						if( RecordName != null )
    						{
    							if( RecordValue != null )
    								RecordSetKVPs.add( RecordName, RecordValue );
    							else
    								RecordSetKVPs.add( RecordName, "" );
    						}
    					}
    				}
    				Retorno = true;
    			} 
    			catch (SQLException e) 
    			{
    				Retorno = false;
    				Debug("[Functions - ExecutePLSQLQuery] - Error: " + e.getMessage(), "Standard");
    			}

    			Debug("[Functions - ExecutePLSQLQuery] - Resultado exitoso de la Query en la Base de Datos", "Standard");
    			Debug("[Functions - ExecutePLSQLQuery] - Numero Total de KVPairs: " + String.valueOf(RecordSetKVPs.count()), "Standard");

    			if( RecordSetKVPs.count() > 0 )
    			{
    				for ( oIndex = 0; oIndex < RecordSetKVPs.count(); oIndex++ ) 
    				{
    					Debug("[Functions - ExecutePLSQLQuery] - KeyName: " + RecordSetKVPs.getKey(oIndex) +", KeyValue: " + RecordSetKVPs.getValue(oIndex), "Standard");	
    				}
    			}
    			LocalDAP.CloseDataBase();
    		}
    		else
    		{
    			if( LocalDAP.GetErrorMessage() != "" )
    			{
    				Debug("[Functions - ExecutePLSQLQuery] - Problemas al ejecutar query '" + pSQLQuery + "'", "Standard");
    				Debug("[Functions - ExecutePLSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
    			}

    			if( pReturnRecorset )
    			{
    				Retorno			= false;
    				RecordSetKVPs	= null;
    			}

    			LocalDAP.CloseDataBase();
    		}
    	}
    	else
    	{
    		Debug("[Functions - ExecutePLSQLQuery] - Problemas al conectar con la Base de Datos '" + pConnectionURL + "'.", "Standard");
    		Debug("[Functions - ExecutePLSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
    	}
    	Debug("[Functions - ExecutePLSQLQuery] (stop)", "Standard");
    
    	return Retorno;
    }    
    
    public String ExecOracleStoreProcedure(String pConnectionURL, String pUserName, String pPassword, int pNumberOutParameters, String pDelimiterOutParameters, String SQLQuery, int ConnectionTimeOut, int QueryTimeOut)
    {
    	String OutputValue = "";
    	
       	Debug("[Functions - ExecOracleStoreProcedure] (init)", "Standard");
    	Debug("[Functions - ExecOracleStoreProcedure] *** ConnectionURL :" + pConnectionURL, "Trace");
    	Debug("[Functions - ExecOracleStoreProcedure] *** UserName      :" + pUserName, "Trace");
    	Debug("[Functions - ExecOracleStoreProcedure] *** Password      :" + pPassword, "Trace");
    	Debug("[Functions - ExecOracleStoreProcedure] *** Out Parameters:" + String.valueOf(pNumberOutParameters), "Trace");
    	Debug("[Functions - ExecOracleStoreProcedure] *** Procedure     :" + SQLQuery, "Trace");
    	
    	if( DataAccessPointOracle.OpenDataBase(pConnectionURL, pUserName, pPassword, ConnectionTimeOut) )
    	{
    		Debug("[Functions - ExecOracleStoreProcedure] - conexion exitosa con la Base de Datos '" + pConnectionURL + "',", "Standard");
    		
    		Debug("[Functions - ExecOracleStoreProcedure] - Ejecutando Store Procedure en la Base de Datos '" + SQLQuery + "'", "Standard");
    		
    		if( DataAccessPointOracle.ExecuteCallableStatement(SQLQuery, pNumberOutParameters, pDelimiterOutParameters, QueryTimeOut) )
    		{
    			OutputValue = DataAccessPointOracle.GetResultadoSP();
    			
    			Debug("[Functions - ExecOracleStoreProcedure] - Se ha ejecutado en forma correcta store procedure '" + SQLQuery + "'", "Standard");
				Debug("[Functions - ExecOracleStoreProcedure] - Parametros obtenidos de store procedure '" + OutputValue + "'", "Trace");
    		}
    		else
    		{
				Debug("[Functions - ExecOracleStoreProcedure] - Problemas al ejecutar store procedure '" + SQLQuery + "'", "Standard");
				Debug("[Functions - ExecOracleStoreProcedure] - Error: " + DataAccessPointOracle.GetErrorMessage(), "Trace");
    		}
    		
    		DataAccessPointOracle.CloseDataBase();
    	}
    	else
    	{
    		Debug("[Functions - ExecOracleStoreProcedure] - Problemas al conectar con la Base de Datos '" + pConnectionURL + "'.", "Standard");
			Debug("[Functions - ExecOracleStoreProcedure] - Error: " + DataAccessPointOracle.GetErrorMessage(), "Trace");
    	}
    	
    	Debug("[Functions - ExecOracleStoreProcedure] (stop)", "Standard");
    	
    	return OutputValue;
    }
    
    /**
     * Metodo utilizado para Leer un Archivo INI
     * 
	 * 
	 *
	 * @param directory : Directorio en la carpeta donde se ubica el archivo (Raiz: %catalina_home%\lib\
	 * @param fileName  : Nombre del archivo a leer
	 * @return JSONArray - Cada Fila es un JSONObject
     * */
    public JSONArray ReadIniFile(String directory, String fileName, boolean hasHeaders){
    	List<String> keys = new ArrayList<String>();
		JSONArray jsonArray = new JSONArray();
		
		String catalina = System.getProperty("catalina.base");
		String iniFile = "";
		if(catalina != null)
			iniFile = catalina+"\\lib\\"+directory+"\\"+fileName;
		else
			iniFile = directory+"\\"+fileName;
		
		try {
			FileInputStream fis = new FileInputStream(iniFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		 
			String line = null;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (hasHeaders && (i == 0)){
					String[] auxkeys = line.split(";");
					for (String key : auxkeys) {
						keys.add(key.trim());
					}
				}else{
					String[] values = line.split(";");
					int cont = 0;
					JSONObject json = new JSONObject();
					if (hasHeaders){
						for (String value : values) {						
							json.put(keys.get(cont), value.trim());
							cont++;						
						}
					}else{
						for (String value : values) {						
							json.put("Key_"+cont, value.trim());
							cont++;						
						}
					}
					jsonArray.put(json);
				}				
				i++;
			}		
			
			br.close();			
		} catch (IOException | JSONException e) {
			DebugError("Error en ReadIniFile "+e.getMessage());
			e.printStackTrace();			
		}
		return jsonArray;
    }
    
    
    public String getParametro(String key) {
		return Params.GetValue(key);
	}

    
    
    public long getCurrentEpoch(){
    	return System.currentTimeMillis()/1000; //tiempo en segundos
    }
    
}
