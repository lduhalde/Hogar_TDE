package epcs;

//import javax.servlet.jsp.PageContext;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.rpc.ServiceFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Sagen.Principal.ConsumidorSagen;

import cl.entel.ServiceContract.principal.ServiceContract;
import cl.entel.asset.principal.Asset;
import cl.entel.assetproductoffer.principal.AssetProductOffer;
import cl.entel.availablemsisdn.principal.AvailableMSISDN;
import cl.entel.availableproductoffer.principal.AvailableProductOffer;
import cl.entel.availableproductofferv2.principal.AvailableProductOfferV2;
import cl.entel.contract.principal.Contract;
import cl.entel.customeraccount.principal.CustomerAccount;
import cl.entel.customeraccountasset.principal.CustomerAccountAsset;
import cl.entel.customeraccountbalance.principal.CustomerAccountBalance;
import cl.entel.customeraccountbalanceandcharge.principal.CustomerAccountBalanceAndCharge;
import cl.entel.customeraccountbalanceandchargev2.principal.CustomerAccountBalanceAndChargeV2;
import cl.entel.customeraccountbalancev2.principal.CustomerAccountBalanceV2;
import cl.entel.frecuentnumber.principal.FrecuentNumber;
import cl.entel.loyaltybalance.principal.LoyaltyBalance;
import cl.entel.loyaltyburn.principal.LoyaltyBurn;
import cl.entel.loyaltyproduct.principal.LoyaltyProduct;
import cl.entel.migrationinfo.principal.MigrationInfo;
import cl.entel.notification.principal.Notification;
import cl.entel.oneclicktoorder.principal.OneClickToOrder;
import cl.entel.productorder.principal.ProductOrder;
import cl.entel.productorderitem.principal.ProductOrderItem;
import cl.entel.refilloffer.principal.RefillOffer;
//import cl.entel.productorderpaymentmethod.principal.ProductOrderPaymentMethod;
import cl.entel.requestpayment.principal.RequestPayment;
import cl.entel.requestpaymentloyaltyburn.principal.RequestPaymentLoyaltyBurn;
import cl.entel.requestproductorder.principal.RequestProductOrder;
import cl.entel.servicerequestactivity.principal.ServiceRequestActivity;
import cl.entel.servicerequestv1.principal.ServiceRequest;
import cl.entel.transfercredit.principal.TransferCredit;
import cl.entel.usagethresholdcounter.principal.UsageThresholdCounter;
import eContact.FunctionsGVP;
import eContact.OracleDBAccess;
import eContact.Parameters;


public class FunctionsEPCS extends FunctionsGVP
{
	public ArrayList PromptList = new ArrayList(1);



	/*
	 * Variables Generales para Web Services
	 * */        

	static javax.xml.rpc.Service service = null;
	static java.net.URL url = null;
	private static ServiceFactory serviceFactory;
	public String debugXML = "FALSE";


	/*
	 * Variables Generales para Base de Datos de Ripley
	 * */

	public Parameters parametrosBD = new Parameters();
	private OracleDBAccess conexionDB = new OracleDBAccess();

	//	private static  Gson            gson    = new Gson();
	//    private static  JsonParser      parser  = new JsonParser();
	public String  hostIvrToDB     = "127.0.0.1";
	public int     portIvrToDB     = 50081;
	public int     timeoutSocket  = 3000;


	/*
	 * Variables Generales para MQ
	 * */        
	public String MQhost = "127.0.0.1";
	public int MQport = 50039;

	/** VARIABLES PUBLICAS DE SERVICIOS MQ**/
	public String trxCod = "";
	public String trxMsj = "";

	//    public KVPairList XMLKVPairList = new KVPairList();

	//    public Parameters Params = new Parameters();

	public String ExceptionMessage = "";

	public String InstanceID = "";

	public boolean DVesK = false;


	public FunctionsEPCS(String ParametersFile)
	{
		super(ParametersFile);  			
		inicializar();
	}

	public FunctionsEPCS(String ParametersFile, String id)
	{    	
		super(ParametersFile, id);
		inicializar();
	}

	/*Parametros adicionales a Leer que no lee el FGVP*/
	private void inicializar(){
		MQhost = this.Params.GetValue("IvrToMQhost", "127.0.0.1");
		MQport = Integer.valueOf(this.Params.GetValue("IvrToMQport", "50039"));

		hostIvrToDB = this.Params.GetValue("IvrToDBhost", "127.0.0.1");
		portIvrToDB = Integer.valueOf(this.Params.GetValue("IvrToDBport", "50080"));

		timeoutSocket  = Integer.valueOf(this.Params.GetValue("SocketTimeout", "2000"));    

		debugXML = this.Params.GetValue("DebugXML", "FALSE");
	}


	public void loggerTraza(String Message, String Level){
		//Set Logger options;    	    	
		SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
		format.setTimeZone(TimeZone.getTimeZone(this.Timezone));
		Date curDate = new Date();
		//CREA UN ARCHIVO POR DIA (Y ES BASE PARA OTROS LOGS)
		String DateToStr = format.format(curDate);

		log = Logger.getLogger("GVPTRAZA");
		RollingFileAppender appender = (RollingFileAppender) log.getAppender("gvplogtrazafile");     	

		String archivo = this.DebugFilePath.replace(".log", "");    	
		if (archivo.equals("")){
			archivo = "C:\\logs\\IVR\\TrazaLog";
		}    	
		archivo = archivo + "-" + DateToStr + ".log";

		appender.setFile(archivo);
		appender.activateOptions();

		// DEBUG < INFO < WARN < ERROR < FATAL
		if (Level.equalsIgnoreCase("DEBUG")) {
			log.debug(Message); 
		}else if (Level.equalsIgnoreCase("INFO")) {
			log.info(Message); 
		}else if (Level.equalsIgnoreCase("WARN")) {
			log.warn(Message); 
		}else if (Level.equalsIgnoreCase("ERROR")) {
			log.error(Message); 
		}else if (Level.equalsIgnoreCase("FATAL")) {
			log.fatal(Message); 
		}else { 
			log.debug(Message); 
		}

	}


	public void WriteParameters (String ParametersFile)
	{
		Debug("[FunctionsGVP - WriteParameters] Escribiendo archivo de parametros.", "Detail");

		Params.WriteParametersFile(ParametersFile);
	}



	public boolean Log (String Message, boolean IncludeDateTime)
	{
		String LogMessage = "";

		if( IncludeDateTime )
		{
			// Obtiene fecha  hora actual y la formatea para cabecera
			// del texto que se escribira en el archivo de Log.
			TimeZone tz = TimeZone.getTimeZone(this.Timezone);
			SimpleDateFormat DateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
			DateFormatter.setTimeZone(tz);
			String DateString = DateFormatter.format(new Date());

			// Compone el texto que se escribira en el archivo de Log
			LogMessage += DateString + " ";
		}

		LogMessage += Message + "\n";

		try
		{
			File fLogFile = new File(this.DebugFilePath);

			fLogFile.createNewFile();

			if( fLogFile.canWrite() )
			{
				FileOutputStream osLogFile = new FileOutputStream(fLogFile, true);

				osLogFile.write(LogMessage.getBytes());
				osLogFile.close();
			}
			else
			{
				return false;
			}
		}

		catch( Exception e )
		{
			return false;
		}

		return true;
	}



	public boolean Log (String Message)
	{
		return Log(Message, true);
	}






	public void RegistroControl (Map<String, String> parametros, String sTipoLog, String traza)
	{
		try
		{
			String sArchivoLog = (String) parametros.get("ArchivoLog");
			String sIDLlamada = (String) parametros.get("IDLlamada");
			String sANI = (String) parametros.get("ANI");
			String sDNIS = (String) parametros.get("DNIS");
			String sidLlamada = (String) parametros.get("idLlamada");

			// La fecha y hora debe ser generada al momenmto de generar el registro
			TimeZone tz = TimeZone.getTimeZone(this.Timezone);
			SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yyyy");
			df2.setTimeZone(tz);
			String sFecha = df2.format(new Date());

			SimpleDateFormat df3 = new SimpleDateFormat("HH:mm:ss");
			df3.setTimeZone(tz);
			String sHora = df3.format(new Date());


			StringBuffer bRegistroLog = new StringBuffer(sIDLlamada);
			bRegistroLog.append(";");
			bRegistroLog.append(traza);
			bRegistroLog.append(";");
			bRegistroLog.append(sFecha);
			bRegistroLog.append(";");
			bRegistroLog.append(sHora);
			bRegistroLog.append(";");
			bRegistroLog.append(sANI);
			bRegistroLog.append(";");
			bRegistroLog.append(sDNIS);
			bRegistroLog.append(";");
			bRegistroLog.append(sidLlamada);

			String sRegistroLog = bRegistroLog.toString();

			this.DebugFilePath = sArchivoLog;
			loggerTraza(sRegistroLog, "DEBUG");
			//            Log(sRegistroLog, false);
		}

		catch (Exception e)
		{
			return;
		}

		return;
	}  


	public Parameters leerParametrosWS(){
		Parameters parametros = new Parameters();
		String catalina = System.getProperty("catalina.base");    	
		String archivo = catalina + "//lib//ConfiguracionServiciosWeb.properties";
		//    	String archivo = "D://Composer//workspace_Ripley//ConfiguracionServiciosWeb.properties";
		parametros.ReadParametersFile(archivo);
		return parametros;
	}

	public Parameters leerParametrosMQ(String nombreServicio){
		Parameters parametros = new Parameters();
		String catalina = System.getProperty("catalina.base");    	
		String archivo = catalina + "//lib//ConfiguracionServiciosMQ.properties";
		//    	String archivo = "D://Composer//workspace_Ripley//ConfiguracionServiciosMQ.properties";
		parametros.ReadParametersFile(archivo);
		return parametros;
	}



	public boolean iniciarConexionBDOracle(String nombreBD){
		boolean retorno = false;
		try{
			String catalina = System.getProperty("catalina.base");    	    		    		
			String archivoParametros = catalina + "//lib//ConexionesDB.properties";
			//    		String archivoParametros = "D://Composer//workspace_Ripley//ConexionesDB.properties";
			parametrosBD.ReadParametersFile(archivoParametros);

			String connectionURL = "";
			String user = parametrosBD.GetValue(nombreBD+"_user");
			String pass = parametrosBD.GetValue(nombreBD+"_pass");
			int timeout  = Integer.valueOf(parametrosBD.GetValue(nombreBD+"_timeout"));

			if ((parametrosBD.GetValue(nombreBD+"_url") != null) && (!parametrosBD.GetValue(nombreBD+"_url").equalsIgnoreCase(""))){
				Debug("[ConexionBD "+nombreBD+"] Conexion URL = "+parametrosBD.GetValue(nombreBD+"_url"), "DEBUG");
				connectionURL = parametrosBD.GetValue(nombreBD+"_url");
			}else{
				Debug("[ConexionBD "+nombreBD+"] Conexion a armar", "DEBUG");
				String host = parametrosBD.GetValue(nombreBD+"_host");
				String port = parametrosBD.GetValue(nombreBD+"_port");
				String servicio = parametrosBD.GetValue(nombreBD+"_service");

				String isSID = parametrosBD.GetValue(nombreBD+"_isSID");

				connectionURL = conexionDB.getURLConexion(host, port, servicio, isSID.equalsIgnoreCase("false"));
			}


			if (conexionDB.OpenDataBase(connectionURL, user, pass, timeout)){
				Debug("[ConexionBD "+nombreBD+"] Conexion Exitosa.", "INFO");
				retorno = true;
			}else{
				Debug("[ConexionBD "+nombreBD+"] Conexion Fallida.", "INFO");
				Debug("[ConexionBD "+nombreBD+"] Error "+conexionDB.GetErrorMessage(), "DEBUG");
			}
		}catch(Exception ex){
			trxCod = "Timeout";
			trxMsj = ex.getMessage();
		}finally{
			return retorno;
		}    	
	}

	public void finalizarConexionBDOracle(){       	
		if (this.conexionDB != null)
			conexionDB.CloseDataBase();
		Debug("[ConexionBD] Conexion Finalizada " +conexionDB.getEcErrorMessage(), "INFO");

	}

	/* 
	 * METODO GENERICO PARA CONEXION A IvrToMQ
	 * 
	 * */
	private String conexionIvrToMQ(String queueName, String mensaje){
		String respuesta = "";

		Socket socket = null;

		try{
			Debug("[Conexion_MQ] *** INICIO CONEXION A MQ ***", "DEBUG");
			//        	Debug("[Conexion_MQ] *** HOST : "+MQhost, "DEBUG");
			//        	Debug("[Conexion_MQ] *** PORT : "+MQport, "DEBUG");

			socket = new Socket(MQhost, MQport);	

			int timeout = Integer.valueOf(this.Params.GetValue("SocketTimeout", "2000"));//Integer.valueOf(ObtenerParametroProperties("WS_BeneficiariTimeout", "ConfiguracionServiciosMQ.properties")).intValue();
			socket.setSoTimeout(timeout);
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			//			BufferedReader userInputBR = new BufferedReader(new InputStreamReader(System.in));
			//			String userInput = userInputBR.readLine();				

			//Escribiendo Mensaje al IvrToMQ
			String mensajeToMQ = "IvrToMQ:"+queueName+":"+mensaje;						
			Debug("[Conexion_MQ] *** REQ  : "+mensaje, "DEBUG");
			out.println(mensajeToMQ);

			//Leyendo Respuesta del IvrToMQ
			respuesta = br.readLine();
			Debug("[Conexion_MQ] *** RESP : "+respuesta, "DEBUG");

		}catch (UnknownHostException e) {
			Debug("[Conexion_MQ] - UnknownHostException "+ e.getMessage(), "DEBUG");
			respuesta = "TimeOut";
			trxCod = "TimeOut";
			trxMsj = e.getMessage();
		}catch (IOException e) {
			Debug("[Conexion_MQ] - IOException "+ e.getMessage(), "DEBUG");
			respuesta = "TimeOut";
			trxCod = "TimeOut";
			trxMsj = e.getMessage();
		}catch (Exception e) {
			Debug("[Conexion_MQ] - Exception "+ e.getMessage(), "DEBUG");
			respuesta = "Error";
			trxCod = "Error";
			trxMsj = e.getMessage();
		}finally{
			if (socket != null){
				try {
					socket.close();
				} catch (IOException e1) {

					e1.printStackTrace();
				}				
			}
			Debug("[Conexion_MQ] *** FIN CONEXION A MQ ***", "DEBUG");
		}

		return respuesta;
	}


	/*
	 * CREDITOS CONSUMO E HIPOTECARIO
	 * OBTENER CUOTAS PAGADAS
	 * 
	 * Tipo MQ
	 * Nombre Servicio SFISERB850C
	 * Queue Name REQ --> SFISERB850C.REQ
	 * Queue Name RESP --> SFISERB850C.RESP
	 * 
	 * */
	public boolean TestMQ(Properties datosEntrada){
		boolean retorno = false;
		String respuesta = "";
		String requerimiento = "";
		String nombreQueue = "SFISERB850C";

		SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		String fechaHoy = formatoFecha.format(new Date());

		try{
			Debug("["+nombreQueue+"] *** INICIO Ejecucion de la transaccion ***", "INFO");
			Debug("["+nombreQueue+"] RUT : " + datosEntrada.getProperty("RUT"), "INFO");

			//Parameters parametros = leerParametrosMQ(nombreQueue);

			/*INICIO XML*/
			requerimiento += "<?xml version=\"1.0\" encoding=\"UTF-8\"?><MWAS><Hdr><Servicio>"+nombreQueue+"</Servicio></Hdr><Datos><![CDATA[";

			/*MENSAJE*/
			//DATOS HEADER
			requerimiento += "0000000000";			// INV-COD-RETORNO         	    	    	
			requerimiento += this.Rellena(""+nombreQueue+"", " ", 50, 1);			// INV-NOMBRE-SERVICIO
			requerimiento += "000003290";			// INV-LARGO-MENSAJE

			//DATOS ENCABEZADO
			requerimiento += this.Rellena("15", "0", 6, 0);		// L850C-CODIGO-CANAL
			requerimiento += this.Rellena("", " ", 8, 1);		// L850C-CODIGO-USUARIO
			//        	requerimiento += parametros.GetValue(nombreQueue+"-COD-OFICINA", "0001");				// L850C-COD-OFICINA
			//        	requerimiento += parametros.GetValue(nombreQueue+"-TERM-FISICO", "0001");				// L850C-TERM-FISICO
			//        	requerimiento += this.Rellena(datosEntrada.getProperty("CODENT"), " ", 4, 1);		// L850C-COD-ENT 810
			requerimiento += "CL";													// L850C-CODIGO-PAIS

			//DATOS PAGINACION
			requerimiento += "N";								// L850C-IND-PAGINACION
			requerimiento += this.Rellena("", " ", 200, 1);		// L850C-CLAVE-INICIO
			requerimiento += this.Rellena("", " ", 200, 1);		// L850C-CLAVE-FIN
			requerimiento += "000";								// L850C-PANTALLA-PAG
			requerimiento += "N";								// L850C-IND-MAS-DATOS
			requerimiento += this.Rellena("", " ", 90, 1);		// L850C-MAS-DATOS

			//DATOS ENTRADA
			requerimiento += "    ";							// L850C-SUCURSAL
			requerimiento += "    ";							// L850C-DEPARTAMENTO
			requerimiento += this.Rellena(datosEntrada.getProperty("RUT"), "0", 10, 0);	// L850C-RUT
			//        	requerimiento += datosEntrada.getProperty("COD-FAMILIA");	// L850C-COD-FAMILIA --> 6=CREDITOS CONSUMO; 7 CREDITOS HIPOTECARIO
			requerimiento += "C";								// L850C-TIPOIDENTIFICACION


			requerimiento += "]]></Datos></MWAS>";
			/*FIN XML*/

			respuesta = conexionIvrToMQ(nombreQueue, requerimiento);

			if (!respuesta.equals("TimeOut") && !respuesta.equals("Error")){        		

			}
		}catch (Exception e){

		}


		return retorno;
	}


	public String WSSocket_SendRecv (String Server, String Port, String Transaccion, String Message2Send, String Charset) {
		int iLength = 0;
		String sReturn = "";
		char[] tmp = new char[10240];

		try {
			Socket SocketServer = null;
			DataOutputStream SocketServerOutputStream = null;
			BufferedReader SocketServerBufferedReader = null;

			Debug("[FunctionsGVP - WSSocket_SendRecv] Se ejecutará la transacción " + Transaccion + ".", "Detail");

			try {
				SocketServer = new Socket(Server, Integer.parseInt(Port));
				SocketServer.setSoTimeout(timeoutSocket);
				SocketServerOutputStream = new DataOutputStream(SocketServer.getOutputStream());
				if(Charset == null)
					SocketServerBufferedReader = new BufferedReader(new InputStreamReader(SocketServer.getInputStream()));
				else
					SocketServerBufferedReader = new BufferedReader(new InputStreamReader(SocketServer.getInputStream(), Charset));
			}
			catch (IOException e) {
				Debug("[FunctionsGVP - WSSocket_SendRecv]     Couldn't get I/O for the socket connection", "Trace");
				System.out.println("Error en WSSocket_SendRecv: " + e.getMessage());
				e.printStackTrace();
			}

			if (SocketServer != null && SocketServerOutputStream != null && SocketServerBufferedReader != null) {
				try {
					Debug("[FunctionsGVP - WSSocket_SendRecv]     Mensaje: \n" + Message2Send, "Detail");
					SocketServerOutputStream.writeBytes(Message2Send);
					// Se leen todas las lineas hasta leer una linea en blanco
					// Durante el proceso, se recupera el largo del cuerpo de la respuesta
					for(; ; ) {
						String sTemp = SocketServerBufferedReader.readLine();
						if(sTemp.length() == 0) break;
						if(sTemp.startsWith("Content-Length")) iLength = Integer.parseInt(sTemp.substring(16));
					}
					// Despues de la línea en blanco viene el cuerpo de la respuesta
					SocketServerBufferedReader.read(tmp, 0, iLength);
					sReturn = new String(tmp);
					Debug("[FunctionsGVP - WSSocket_SendRecv]     Respuesta: " + sReturn, "Detail");
				}
				catch (IOException e) {
					Debug("[FunctionsGVP - WSSocket_SendRecv]     I/O failed on the socket connection", "Trace");
				}
			}
			Debug("[FunctionsGVP - WSSocket_SendRecv] Luego de validar los objetos.", "Detail");

			try {
				SocketServerOutputStream.close();
				SocketServerBufferedReader.close();
				SocketServer.close();
			}
			catch (Exception e) {
			}
		}

		catch (Exception e) {
			Debug("[FunctionsGVP - WSSocket_SendRecv]     Servicio: " + Transaccion + ". Problemas al enviar mensaje: " + e.getMessage() + ".", "Trace");
		}

		return sReturn;
	}



	public String WSSocket_SendRecv (String Server, String Port, String Transaccion, String Message2Send) {
		return WSSocket_SendRecv(Server, Port, Transaccion, Message2Send, null);
	}



	public boolean WSSocket_Send (String Server, String Port, String Transaccion, String Message2Send) {
		int iLength = 0;
		boolean bReturn = true;
		char[] tmp = new char[10240];

		try {
			Socket SocketServer = null;
			DataOutputStream SocketServerOutputStream = null;

			Debug("[FunctionsGVP - WSSocket_Send] Se ejecutará la transacción " + Transaccion + ".", "Detail");

			try {
				SocketServer = new Socket(Server, Integer.parseInt(Port));
				SocketServerOutputStream = new DataOutputStream(SocketServer.getOutputStream());
			}
			catch (IOException e) {
				bReturn = false;
				Debug("[FunctionsGVP - WSSocket_Send]     Couldn't get I/O for the socket connection", "Trace");
				System.out.println("Error en WSSocket_Send: " + e.getMessage());
				e.printStackTrace();
			}

			if (SocketServer != null && SocketServerOutputStream != null) {
				try {
					Debug("[FunctionsGVP - WSSocket_Send]     Mensaje: \n" + Message2Send, "Detail");
					SocketServerOutputStream.writeBytes(Message2Send);
				}
				catch (IOException e) {
					bReturn = false;
					Debug("[FunctionsGVP - WSSocket_Send]     I/O failed on the socket connection", "Trace");
				}
			}
			Debug("[FunctionsGVP - WSSocket_Send] Luego de enviar el mensaje.", "Detail");

			try {
				SocketServerOutputStream.close();
				SocketServer.close();
			}
			catch (Exception e) {
			}
		}

		catch (Exception e) {
			bReturn = false;
			Debug("[FunctionsGVP - WSSocket_Send]     Servicio: " + Transaccion + ". Problemas al enviar mensaje: " + e.getMessage() + ".", "Trace");
		}

		return bReturn;
	}


	/*
	 * Base de Datos
	 * Tipo SQ
	 * Nombre PLSQL sfdatmae_prc_cos_dml_par     
	 * 
	 * */
	public String RegistraMarcaNavegacion(JSONObject datosEntrada){
		JSONObject jsonRetorno = new JSONObject();
		String retorno="{\"CODMSG\":\"NULL\",\"MSG\":\"\"}";
		try {         	
			String uniqueid = datosEntrada.getString("UniqueID");
			String idLlamada = datosEntrada.getString("idLlamada");
			String secuencia = datosEntrada.getString("Secuencia");
			String appname = datosEntrada.getString("IVR");
			String opcion = datosEntrada.getString("Opcion");
			if(opcion.length()>30){
				opcion = opcion.substring(0, 30);
			}
			String irc = datosEntrada.getString("Resultado");
			String duracion = datosEntrada.getString("Duracion");
			String idata = datosEntrada.getString("IData");
			String msg = datosEntrada.getString("Msg");

			long epoch = System.currentTimeMillis()/1000; //tiempo en segundos
			String STARTTIME = String.valueOf(epoch);


			String servicio 	= Params.GetValue("nav_servicio","Navegacion");
			String queryIvrToDB = Params.GetValue("nav_query","SP_REGISTRA_NAVEGACION");

			String paramsIvrToDB = uniqueid+"|"+idLlamada+"|"+secuencia+"|"+STARTTIME+"|"+appname+"|"+opcion+"|"+irc+"|"+duracion+"|"+idata+"|"+msg;
			//    		String outParamsIvrToDB = "clob|NumMensaje|Mensaje|cursor";
			//    		String dataTypesIvrToDB = "float|string|float|float|float|float|string|float|clob|float|string|cursor";

			Debug("[RegistraMarcaNavegacion] INICIO "+servicio+" - "+queryIvrToDB, "INFO");

			String mensaje = "{\"servicio\":\""+servicio+"\", \"select\":\"2\", \"query\":\"" + queryIvrToDB + "\", \"parameters\":\"" + paramsIvrToDB + "\"}\n";
			Debug("[RegistraMarcaNavegacion] ENTRADA "+mensaje, "DEBUG");

			/*connIvrToDB ivrtodb = new connIvrToDB();
			ivrtodb.connect(hostIvrToDB, portIvrToDB, timeoutSocket);
			ivrtodb.SendMessage(mensaje);
			String respuestaDB = ivrtodb.getResponse();
			Debug("[RegistraMarcaNavegacion] SALIDA "+ivrtodb.getResponse(), "DEBUG");*/

			String respuestaDB = Socket_SendRecvHA("REGISTRA_NAVEGACION",mensaje,"NAV");

			if(respuestaDB.indexOf("errorMessage")==-1){
				if(!respuestaDB.equals("[]")){	
					JSONArray jArrayRespuesta = new JSONArray(respuestaDB);
					JSONObject jObjRespuesta = jArrayRespuesta.getJSONObject(0);
					jsonRetorno.put("CODMSG", "OK");
					if(respuestaDB.indexOf("RowsAffected")>-1){
						jsonRetorno.put("MSG", "RowsAffected "+jObjRespuesta.getString("RowsAffected"));
					}else if(respuestaDB.indexOf("taskID")>-1){
						jsonRetorno.put("MSG", jObjRespuesta.getString("MSG"));
					}else{
						jsonRetorno.put("MSG", "");
					}
				}else{
					jsonRetorno.put("CODMSG", "NULL");
					jsonRetorno.put("MSG", "Respuesta vacia");
				}

			}else{
				JSONObject jObjRespuesta = new JSONObject(respuestaDB);
				//JSONObject jObjRespuesta = jArrayRespuesta.getJSONObject(0);
				jsonRetorno.put("CODMSG", "NOK");
				if(jObjRespuesta.getString("errorMessage").equalsIgnoreCase("ResultSet is from UPDATE. No Data.")){
					jsonRetorno.put("CODMSG", "IGNORE");
				}
				jsonRetorno.put("MSG", "["+jObjRespuesta.getString("errorCode")+"] "+jObjRespuesta.getString("errorMessage"));
			}
			retorno = jsonRetorno.toString();

			/*} catch (IOException ex) {
			Debug("[RegistraMarcaNavegacion] IOException "+ex.getMessage(), "DEBUG");
			retorno="{\"CODMSG\":\"TimeOut\",\"MSG\":"+ex.getMessage()+"}";*/
		} catch (Exception ex) {
			Debug("[RegistraMarcaNavegacion] Exception "+ex.getMessage(), "DEBUG");
			retorno="{\"CODMSG\":\"Error\",\"MSG\":"+ex.getMessage()+"}";
			ex.printStackTrace();
		}finally{
			Debug("[RegistraMarcaNavegacion] FIN", "INFO");

		}
		return retorno;
	}
	public JSONObject startNavegacion(JSONObject state, String traza){
		JSONObject parametros_marcas_navegacion = new JSONObject();
		try {
			//Formateo marca de navegación 
			traza = traza.toUpperCase();
			traza = traza.replace('-', '_');
			traza = traza.replace(' ', '_');

			parametros_marcas_navegacion = state.getJSONObject("parametros_marcas_navegacion");
			parametros_marcas_navegacion.put("TRAZA",traza);
			//parametros_marcas_navegacion.put("TRAZA_INICIO",getCurrentEpoch());
			parametros_marcas_navegacion.put("TRAZA_INICIO", System.currentTimeMillis());

			Debug("[FunctionsEPCS.startNavegacion] parametros_marcas_navegacion "+parametros_marcas_navegacion, "DEBUG");

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return parametros_marcas_navegacion;
	}

	public JSONObject stopNavegacion(JSONObject state){
		JSONObject resultado = new JSONObject();
		JSONObject parametros_marcas_navegacion = new JSONObject();
		long duracion=0;
		try {
			parametros_marcas_navegacion = state.getJSONObject("parametros_marcas_navegacion");
			Debug("[FunctionsEPCS.stopNavegacion] parametros_marcas_navegacion "+parametros_marcas_navegacion, "DEBUG");

			JSONObject parametros = new JSONObject();

			//Map<String, String> parametros = new HashMap<String, String>();
			long inicio = 0;
			if (parametros_marcas_navegacion.has("TRAZA_INICIO") ){
				inicio = parametros_marcas_navegacion.getLong("TRAZA_INICIO");
			}

			if (inicio>0){
				//long fin = getCurrentEpoch();
				long fin = System.currentTimeMillis();
				duracion = fin - inicio;
			}

			int contadorTrazas = (parametros_marcas_navegacion.has("TRAZA_CONTADOR")) ? (parametros_marcas_navegacion.getInt("TRAZA_CONTADOR") + 1) : 0;

			parametros.put("UniqueID", (parametros_marcas_navegacion.has("AsteriskID")) ? parametros_marcas_navegacion.getString("AsteriskID") : state.getString("CallUUID"));
			parametros.put("idLlamada", state.getString("idLlamada"));
			parametros.put("Secuencia", contadorTrazas+"");
			parametros.put("IVR", parametros_marcas_navegacion.has("appName") ? parametros_marcas_navegacion.getString("appName") : (state.has("appName")) ? state.getString("appName") : "IVR_EPCS");
			parametros.put("Opcion", parametros_marcas_navegacion.getString("TRAZA"));
			parametros.put("Resultado", (parametros_marcas_navegacion.has("RC")) ? parametros_marcas_navegacion.getInt("RC") : 0);
			parametros.put("Duracion", duracion+"");
			parametros.put("IData", (parametros_marcas_navegacion.has("DATA")) ? parametros_marcas_navegacion.getString("DATA") : " ");
			parametros.put("Msg", (parametros_marcas_navegacion.has("MSG")) ? parametros_marcas_navegacion.getString("MSG") : " ");

			resultado = new JSONObject(RegistraMarcaNavegacion(parametros));

			Debug("[FunctionsEPCS.stopNavegacion] resultado "+resultado, "DEBUG");

			if(resultado.getString("CODMSG").equalsIgnoreCase("OK") || resultado.getString("CODMSG").equalsIgnoreCase("IGNORE")){
				parametros_marcas_navegacion.put("TRAZA_CONTADOR", contadorTrazas);
				parametros_marcas_navegacion.remove("TRAZA_INICIO");
				parametros_marcas_navegacion.remove("TRAZA");
				parametros_marcas_navegacion.remove("RC");
				parametros_marcas_navegacion.remove("DATA");
				parametros_marcas_navegacion.remove("MSG");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return parametros_marcas_navegacion;
	}

	public String createEventID(String operationCode, String idLlamada){
		String eventID="0078";
		eventID+=operationCode;
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		eventID+=f.format(new Date());
		eventID+=idLlamada.substring(16);
		eventID+="000";

		return eventID;
	}


	public String createCustomerAccount(String accountType, String customerType, String legalEntityType, String number, String type, String sourceID, String processCode, String idLlamada){
		int maxTimeout = Integer.parseInt(Params.GetValue("TOCustomerAccount", "1000000"));
		String url = Params.GetValue("WSCustomerAccount", "http://10.49.4.78:7011/ESC/CustomerAccount/v1");
		String wsdlLocation=Params.GetValue("WLCustomerAccount", "file:WSDL/Services/CustomerAccount_v1/CustomerAccount_v1_ESC.wsdl");
		CustomerAccount cliente = new CustomerAccount(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccount\":{\"accountType\":\""+accountType+"\",\"customerType\":\""+customerType+"\",\"legalEntityType\":\""+legalEntityType+"\",\"IndividualIdentification\":{\"number\":\""+number+"\",\"type\":\""+type+"\"}}}}";
		Debug("[FunctionsEPCS.createCustomerAccount] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.create(request, createEventID("OPER_00069",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String createNotification(String externalID, String senderAlias, String originSystem, String receiver, String subject, String textMessage, String sourceID, String processCode, String idLlamada){
		Random rng = new Random();

		long timeStamp=System.currentTimeMillis()/1000;
		long dig = rng.nextInt(900)+99;
		String requestID = "0078"+timeStamp+dig;

		int maxTimeout = Integer.parseInt(Params.GetValue("TONotification", "1000000"));
		String url = Params.GetValue("WSNotification", "http://10.51.12.28:7010/ESC/Notification/v1");
		String wsdlLocation=Params.GetValue("WLNotification", "file:WSDL/Services/Notification_v1/Notification_v1_ESC.wsdl");
		Notification cliente = new Notification(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{"
				+ "			\"Consumer\":{"
				+ "				\"sysCode\":\"CHL-IVR\","
				+ "				\"enterpriseCode\":\"ENTEL-CHL\","
				+ "				\"countryCode\":\"CHL\""
				+ "			}"
				+ "		  },"
				+ "		  \"Body\":{"
				+ "			\"Notification\":{"
				+ "				\"externalID\":\""+externalID+"\","
				+ "				\"originSystem\":\""+originSystem+"\","
				+ "				\"Message\":[{"
				+ "					\"deliveryMethod\":\"CellPhone\","
				+ "					\"senderAlias\":\""+senderAlias+"\","
				+ "					\"receiver\":\""+receiver+"\","
				+ "					\"subject\":\""+subject+"\","
				+ "					\"textMessage\":\""+textMessage+"\""
				+ "				}],"
				+ "				\"Parameter\":[{"
				+ "					\"name\":\"requestId\","
				+ "					\"value\":\""+requestID+"\""
				+ "				}]"
				+ "			}"
				+ "		 }"
				+ "		}";

		Debug("[FunctionsEPCS.createNotification] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.create(request, createEventID("OPER_00032",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String createOneClickToOrder(String MSISDN, String planActual,String area,String biType, String subArea,String operationType,String productOffering, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSOneClickToOrder", "http://10.49.4.78:7011/ESC/ProductOrder/v2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOOneClickToOrder", "1000000"));
		String wsdlLocation=Params.GetValue("WLOneClickToOrder", "file:WSDL/Service/OneClickToOrder_v1/OneClickToOrder_v1_ESC.wsdl");
		OneClickToOrder cliente = new OneClickToOrder(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		Random rng = new Random();
		long timeStamp=System.currentTimeMillis()/1000;
		long dig = rng.nextInt(900)+99;
		String requestID = "0078"+timeStamp+dig;

		String request =
				"{\"RequestHeader\":{"
						+ "	\"Consumer\":{"
						+ "		\"sysCode\":\"CHL-IVR\","
						+ "		\"enterpriseCode\":\"ENTEL-CHL\","
						+ "		\"countryCode\":\"CHL\""
						+ "	}"
						+ "},"
						+ "  \"Body\":{"
						+ "  	\"Asset\":{"
						+ "			\"ID\":\""+planActual+"\","
						+ "			\"MSISDN\":{\"SN\":\""+MSISDN+"\"}"
						+ "		},"
						+ "  	\"CustomerOrder\":{"
						+ "			\"area\":\""+area+"\","
						+ "			\"biType\":\""+biType+"\","
						+ "			\"channel\": \"IVR\","
						+ "			\"mode\": \"NON_INTERACTIVE\","
						+ "			\"orderType\": \"Orden\","
						+ "			\"requestID\": \""+requestID+"\","
						+ "			\"subArea\": \""+subArea+"\","
						+ "			\"operationType\":\""+operationType+"\","
						+ "			\"SalesChannel\":{"
						+ "				\"createBy\": \"AutomaticoEntel\","
						+ "				\"orderCommercialChannel\": \"IVR\""
						+ "			}"
						+ "		},"
						+ "  	\"CustomerOrderItem\":[{"
						+ "			\"action\":\"Add\","
						+ "			\"biType\":\"ProductOfferingOrder\","
						+ "			\"requestID\": \""+requestID+"\","
						+ 			productOffering
						+ "		}]"
						+ "	}"
						+ "}"; 


		Debug("[FunctionsEPCS.createOneClickToOrder] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.create(request, createEventID("OPER_00349",idLlamada),sourceID,processCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String createOneClickToOrder(String MSISDN, String planActual,String area,String biType, String subArea,String operationType,String remainingPoints,String[] productOffering, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSOneClickToOrder", "http://10.49.4.78:7011/ESC/ProductOrder/v2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOOneClickToOrder", "1000000"));
		String wsdlLocation=Params.GetValue("WLOneClickToOrder", "file:WSDL/Service/OneClickToOrder_v1/OneClickToOrder_v1_ESC.wsdl");
		OneClickToOrder cliente = new OneClickToOrder(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		Random rng = new Random();
		long timeStamp=System.currentTimeMillis()/1000;
		long dig = rng.nextInt(900)+99;
		String requestID = "0078"+timeStamp+dig;

		String request =
				"{\"RequestHeader\":{"
						+ "	\"Consumer\":{"
						+ "		\"sysCode\":\"CHL-IVR\","
						+ "		\"enterpriseCode\":\"ENTEL-CHL\","
						+ "		\"countryCode\":\"CHL\""
						+ "	}"
						+ "},"
						+ "  \"Body\":{"
						+ "  	\"Asset\":{"
						+ "			\"ID\":\""+planActual+"\","
						+ "			\"MSISDN\":{\"SN\":\""+MSISDN+"\"}"
						+ "		},"
						+ "  	\"CustomerOrder\":{"
						+ "			\"area\":\""+area+"\","
						+ "			\"biType\":\""+biType+"\","
						+ "			\"channel\": \"IVR\","
						+ "			\"mode\": \"NON_INTERACTIVE\","
						+ "			\"orderType\": \"Orden\","
						+ "			\"requestID\": \""+requestID+"\","
						+ "			\"subArea\": \""+subArea+"\","
						+ "			\"operationType\":\""+operationType+"\","
						+ "			\"SalesChannel\":{"
						+ "				\"createBy\": \"AutomaticoEntel\","
						+ "				\"orderCommercialChannel\": \"IVR\""
						+ "			},"
						+ "			\"RelatedParty\":[{"
						+ "				\"CustomerAccount\":{"
						+ "					\"LoyaltyAccount\":{"
						+ "						\"LoyaltyBalance\":{"
						+ "							\"remainingPoints\":{"
						+ "								\"amount\":\""+remainingPoints+"\""
						+ "							}"
						+ "						}"
						+ "					}"
						+ "				}"
						+ "			}]"
						+ "		},"
						+ "  	\"CustomerOrderItem\":[{"
						+ "			\"action\":\"Add\","
						+ "			\"biType\":\"ProductOfferingOrder\","
						+ "			\"requestID\": \""+requestID+"\","
						+ 			productOffering[0]
								+ "		},{"
								+ "			\"action\":\"Add\","
								+ "			\"biType\":\"ProductOfferingOrder\","
								+ "			\"requestID\": \""+requestID+"\","
								+ 			productOffering[1]
										+ "		}]"
										+ "	}"
										+ "}"; 


		Debug("[FunctionsEPCS.createOneClickToOrder] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.create(request, createEventID("OPER_00349",idLlamada),sourceID,processCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String createProductOrderActivacion(String area, String biType, String CustomerAccountID, String mode, String orderType, String state, String subArea, String BillingAccount, String IndividualIdentification,String ServiceRequestID, String sourceID, String processCode, String idLlamada){
		GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
		XMLGregorianCalendar xgcal = null;
		try {
			xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
		} catch (DatatypeConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Random rng = new Random();

		long timeStamp=System.currentTimeMillis()/1000;
		long dig = rng.nextInt(900)+99;
		String requestID = "0078"+timeStamp+dig;

		int maxTimeout = Integer.parseInt(Params.GetValue("TOProductOrder", "1000000"));
		String url = Params.GetValue("WSProductOrder", "http://10.49.4.78:7011/ESC/ProductOrder/v1");
		String wsdlLocation=Params.GetValue("WLProductOrder", "file:WSDL/Services/ProductOrder_v1/ProductOrder_v1_ESC.wsdl");
		ProductOrder cliente = new ProductOrder(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{"
				+ "	\"Consumer\":{"
				+ "		\"sysCode\":\"CHL-IVR\","
				+ "		\"enterpriseCode\":\"ENTEL-CHL\","
				+ "		\"countryCode\":\"CHL\""
				+ "	}"
				+ "},"
				+ "  \"Body\":{"
				+ "  	\"CustomerOrder\":{"
				+ "			\"area\":\""+area+"\","
				+ "			\"biType\":\""+biType+"\","
				+ "			\"channel\": \"IVR\","
				+ "			\"createdBy\": \""+CustomerAccountID+"\","
				+ "			\"createdDate\": \""+xgcal+"\","
				+ "			\"mode\": \""+mode+"\","
				+ "			\"orderType\": \""+orderType+"\","
				+ "			\"owner\": \""+CustomerAccountID+"\","
				+ "			\"requestID\": \""+requestID+"\","
				+ "			\"requester\": \""+CustomerAccountID+"\","
				+ "			\"state\": \""+state+"\","
				+ "			\"subArea\": \""+subArea+"\","
				+ "			\"RelatedEntity\":[{"
				+ "				\"Account\":[{"
				+ "					\"externalID\": \""+CustomerAccountID+"\","
				+ "					\"type\": \"billing\","
				+ "					\"BillingAccount\": "+BillingAccount
				+ "				}],"
				+ "				\"SalesChannel\":{"
				+ "					\"createBy\": \"AutomaticoEntel\","
				+ "					\"orderCommercialChannel\": \"IVR\""
				+ "				}"
				+ "			}],"
				+ "			\"RelatedParty\": [{"
				+ "				\"CustomerAccount\": {"
				+ "					\"ID\": \""+CustomerAccountID+"\""
				+ "				},"
				+ "				\"IndividualIdentification\":"+IndividualIdentification
				+ "			}],"
				+ "			\"ServiceRequest\" :{"
				+ "				\"ID\" : \""+ServiceRequestID+"\""
				+ "			}"
				+ "		}"
				+ "	}"
				+ "}";

		Debug("[FunctionsEPCS.createProductOrder] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.create(request, createEventID("OPER_00042",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	public String createProductOrder(String area, String biType, String CustomerAccountID, String mode, String orderType, String state, String subArea, String MSISDN, String BillingAccount, String IndividualIdentification, String ServiceRequestID, String sourceID, String processCode, String idLlamada){
		GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
		XMLGregorianCalendar xgcal = null;
		try {
			xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
		} catch (DatatypeConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Random rng = new Random();

		long timeStamp=System.currentTimeMillis()/1000;
		long dig = rng.nextInt(900)+99;
		String requestID = "0078"+timeStamp+dig;

		int maxTimeout = Integer.parseInt(Params.GetValue("TOProductOrder", "1000000"));
		String url = Params.GetValue("WSProductOrder", "http://10.49.4.78:7011/ESC/ProductOrder/v1");
		String wsdlLocation=Params.GetValue("WLProductOrder", "file:WSDL/Services/ProductOrder_v1/ProductOrder_v1_ESC.wsdl");
		ProductOrder cliente = new ProductOrder(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{"
				+ "	\"Consumer\":{"
				+ "		\"sysCode\":\"CHL-IVR\","
				+ "		\"enterpriseCode\":\"ENTEL-CHL\","
				+ "		\"countryCode\":\"CHL\""
				+ "	}"
				+ "},"
				+ "  \"Body\":{"
				+ "  	\"CustomerOrder\":{"
				+ "			\"area\":\""+area+"\","
				+ "			\"biType\":\""+biType+"\","
				+ "			\"channel\": \"IVR\","
				+ "			\"createdBy\": \""+CustomerAccountID+"\","
				+ "			\"createdDate\": \""+xgcal+"\","
				+ "			\"mode\": \""+mode+"\","
				+ "			\"orderType\": \""+orderType+"\","
				+ "			\"owner\": \""+CustomerAccountID+"\","
				+ "			\"requestID\": \""+requestID+"\","
				+ "			\"requester\": \""+CustomerAccountID+"\","
				+ "			\"state\": \""+state+"\","
				+ "			\"subArea\": \""+subArea+"\","
				+ "			\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}},"
				+ "			\"RelatedEntity\":[{"
				+ "				\"Account\":[{"
				+ "					\"externalID\": \""+CustomerAccountID+"\","
				+ "					\"type\": \"billing\","
				+ "					\"BillingAccount\": "+BillingAccount
				+ "				}],"
				+ "				\"SalesChannel\":{"
				+ "					\"createBy\": \"AutomaticoEntel\","
				+ "					\"orderCommercialChannel\": \"IVR\""
				+ "				}"
				+ "			}],"
				+ "			\"RelatedParty\": [{"
				+ "				\"CustomerAccount\": {"
				+ "					\"ID\": \""+CustomerAccountID+"\""
				+ "				},"
				+ "				\"IndividualIdentification\":"+IndividualIdentification
				+ "			}],"
				+ "			\"ServiceRequest\" :{"
				+ "				\"ID\" : \""+ServiceRequestID+"\""
				+ "			}"
				+ "		}"
				+ "	}"
				+ "}";

		Debug("[FunctionsEPCS.createProductOrder] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.create(request, createEventID("OPER_00042",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String createProductOrderItem(String action, String biType, String shoppingCartID, String PO_ID, String amount, String CA_ID, String RelatedItem, String sourceID, String processCode, String idLlamada){
		Random rng = new Random();

		long timeStamp=System.currentTimeMillis()/1000;
		long dig = rng.nextInt(900)+99;
		String requestID = "0078"+timeStamp+dig;
		int maxTimeout = Integer.parseInt(Params.GetValue("TOProductOrderItem", "1000000"));
		String url = Params.GetValue("WSProductOrderItem", "http://10.49.4.78:7011/ESC/ProductOrderItem/v1");
		String wsdlLocation=Params.GetValue("WLProductOrderItem", "file:WSDL/Services/ProductOrderItem_v1/ProductOrderItem_v1_ESC.wsdl");
		ProductOrderItem cliente = new ProductOrderItem(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerOrderItem\":{\"requestID\":\""+requestID+"\",\"action\":\""+action+"\",\"biType\":\""+biType+"\",\"CustomerOrder\":{\"shoppingCartID\":\""+shoppingCartID+"\"},\"ProductOffering\":{\"ID\":\""+PO_ID+"\",\"quantity\":{\"amount\": "+amount+"}},\"RelatedParty\":[{\"CustomerAccount\":{\"ID\": \""+CA_ID+"\"}}]"
				+ "					,\"RelatedItem\":[{"
				+ RelatedItem
				+ "					}]}}}";
		Debug("[FunctionsEPCS.createProductOrderItem] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.create(request, createEventID("OPER_00085",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String createRequestPayment(String MSISDN, String amount,String distributor,String integrator, String sourceID, String processCode, String idLlamada)throws DatatypeConfigurationException{
		GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
		XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);

		String url = Params.GetValue("WSRequestPayment", "http://10.49.4.78:7011/CVL/SOAP12/RequestPayment/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TORequestPayment", "1000000"));
		String wsdlLocation=Params.GetValue("WLRequestPayment", "file:WSDL/CVL_RequestPayment_v1/Resources/WSDL/SOAP12/RequestPayment_v1_ESC.wsdl");
		RequestPayment cliente = new RequestPayment(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		//String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\", \"countryCode\":\"CHL\"}},\"Body\":{\"CustomerPayment\":{\"paymentDate\":\""+xgcal+"\",\"requestDate\":\""+xgcal+"\",\"trace\":\"1\",\"CustomerAccount\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}}},\"PaymentMethod\":{\"paymentMethodType\":\"PRESTA LUKA\"},\"ThirdPartyPayeeAgency\":{\"platform\":\"IVR\"},\"amount\":{\"amount\":\""+amount+"\",\"units\":\"CLP\"}}}}";

		String request = 
				"{"
						+ "		\"RequestHeader\":{"
						+ "			\"Consumer\":{"
						+ "				\"sysCode\":\"CHL-IVR\","
						+ "				\"enterpriseCode\":\"ENTEL-CHL\", "
						+ "				\"countryCode\":\"CHL\""
						+ "			}"
						+ "		},"
						+ "		\"Body\":{"
						+ "			\"CustomerPayment\":{"
						+ "				\"paymentDate\":\""+xgcal+"\","
						+ "		\"requestDate\":\""+xgcal+"\","
						+ "\"trace\":\"1\","
						+ "\"CustomerAccount\":{"
						+ "		\"Asset\":{"
						+ "			\"MSISDN\":{"
						+ "				\"SN\":\""+MSISDN+"\""
						+ "			}"
						+ "		}"
						+ "},"
						+ "\"PaymentMethod\":{"
						+ "		\"paymentMethodType\":\"PRESTA LUKA\""
						+ "},"
						+ "\"ThirdPartyPayeeAgency\":{"
						+ "		\"platform\":\"IVR\","
						+ "		\"distributor\":\""+distributor+"\","
						+ "		\"integrator\":\""+integrator+"\""
						+ "},"
						+ "\"amount\":{"
						+ "		\"amount\":\""+amount+"\","
						+ "		\"units\":\"CLP\""
						+ "}"
						+ "}"
						+ "}"
						+ "}";

		Debug("[FunctionsEPCS.createRequestPayment] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.create(request, createEventID("OPER_00006",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String createRequestPaymentLoyaltyBurn(String transactionType, String transactionSubType, String originMSISDN, String productID, String amount, String quantity, String sourceID, String processCode, String idLlamada) throws DatatypeConfigurationException{

		String url = Params.GetValue("WSRequestPaymentLoyaltyBurn", "http://10.49.4.78:7011/ESC/RequestPaymentLoyaltyBurn/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TORequestPaymentLoyaltyBurn", "1000000"));
		String wsdlLocation=Params.GetValue("WLRequestPaymentLoyaltyBurn", "file:WSDL/Services/RequestPaymentLoyaltyBurn_v1/RequestPaymentLoyaltyBurn_v1_ESC.wsdl");
		RequestPaymentLoyaltyBurn cliente = new RequestPaymentLoyaltyBurn(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
		XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},"
				+ "\"Body\":{"
				+ "		\"LoyaltyTransaction\":{"
				+ "			\"exchangeType\":\"A\","
				+ "			\"transactionSubType\":\""+transactionSubType+"\","
				+ "			\"transactionType\":\""+transactionType+"\","
				+ "			\"originMSISDN\":{"
				+ "				\"SN\":\""+originMSISDN+"\"},"
				+ "			\"SalesChannel\":{"
				+ "				\"description\":\"IVR\"},"
				+ "			\"CustomerPayment\":{"
				+ "				\"paymentDate\": \""+xgcal+"\","
				+ "				\"amount\":{\"amount\":"+amount+"}},"
				+ "			\"LoyaltyBalance\":{"
				+ "				\"LoyaltyAccount\":{"
				+ "					\"LoyaltyProgramProduct\":{"
				+ "						\"ID\":\""+productID+"\"}}},"
				+ "			\"quantity\":{"
				+ "				\"amount\":"+quantity+"}"	
				+ "		}}}";

		Debug("[FunctionsEPCS.createRequestPaymentLoyaltyBurn] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.create(request, createEventID("OPER_00134",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	/**
	public String createServiceRequest(String area, String state, String stateReason, String subArea, String type, String number, String typeIdentification){
		String url = Params.GetValue("WSServiceRequest", "http://10.49.4.78:7011/ESC/ServiceRequest/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOServiceRequest", "1000000"));
		String wsdlLocation=Params.GetValue("WLServiceRequest", "file:WSDL/Services/ServiceRequest_v1/ServiceRequest_v1_ESC.wsdl");
		ServiceRequest cliente = new ServiceRequest(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}}"
				+ "		  ,\"Body\":{"
				+ "				\"ServiceRequest\":{"
				+ "					\"area\":\""+area+"\","
				+ "					\"state\":\""+state+"\","
				+ "					\"stateReason\":\""+stateReason+"\","
				+ "					\"subArea\":\""+subArea+"\","
				+ "					\"type\":\""+type+"\","
				+ "					\"IndividualIdentification\":{"
				+ "						\"number\":\""+number+"\","
				+ "						\"type\":\""+typeIdentification+"\""
				+ "					}"
				+ "				}"
				+ "			}"
				+ "}";
		Debug("[FunctionsEPCS.createServiceRequest] request "+request, "DEBUG");
		String resp = null;

		try {

			resp = cliente.create(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}**/

	public String createTransferCredit(String amount, String destinyMSISDN, String originMSISDN, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSTransferCredit", "http://10.49.4.78:7011/CVL/SOAP12/TransferCredit/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOTransferCredit", "1000000"));
		String wsdlLocation=Params.GetValue("WLTransferCredit", "file:WSDL/Services/TransferCredit_v1/TransferCredit_v1_ESC.wsdl");
		TransferCredit cliente = new TransferCredit(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"AppliedTransferCredit\":{\"SalesChannel\":{\"ID\":\"IVR\"},\"amount\":{\"amount\":"+amount+"},\"destinyMSISDN\":{\"SN\":\""+destinyMSISDN+"\"},\"originMSISDN\":{\"SN\":\""+originMSISDN+"\"}}}}";
		Debug("[FunctionsEPCS.createTransferCredit] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.create(request, createEventID("OPER_00125",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getAssetProductOffer(String MSISDN, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSAssetProductOffer", "http://10.49.4.78:7011/ESC/AssetProductOffer/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOAssetProductOffer", "1000000"));
		String wsdlLocation=Params.GetValue("WLAssetProductOffer", "file:WSDL/Services/AssetProductOffer_v1/AssetProductOffer_v1_ESC.wsdl");
		AssetProductOffer cliente = new AssetProductOffer(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"Product\":{\"ProductAccount\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}}}}}";
		Debug("[FunctionsEPCS.getAssetProductOffer] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00135",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getAvailableMSISDN(String searchLimit, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSAvailableMSISDN", "http://10.49.4.78:7011/ESC/AvailableMSISDN/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOAvailableMSISDN", "1000000"));
		String wsdlLocation=Params.GetValue("WLAvailableMSISDN", "file:WSDL/Services/AvailableMSISDN_v1/AvailableMSISDN_v1_ESC.wsdl");
		AvailableMSISDN cliente = new AvailableMSISDN(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"Asset\":{\"status\":\"r\",\"Filter\":{\"searchLimit\":"+searchLimit+"},\"operator\":{\"plmnCode\":\"CHLMV\",\"submarketID\":\"GSM\"}}}}";
		Debug("[FunctionsEPCS.getAvailableMSISDN] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00136",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	public String getAvailableProductOffer(String plan, String specificationSubtype, String specificationType, String subFamily, String MSISDN, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSAvailableProductOffer", "http://10.49.4.78:7011/ESC/AvailableProductOffer/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOAvailableProductOffer", "1000000"));
		String wsdlLocation=Params.GetValue("WLAvailableProductOffer", "file:WSDL/Services/AvailableProductOffer_v1/AvailableProductOffer_v1_ESC.wsdl");
		AvailableProductOffer cliente = new AvailableProductOffer(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);	
		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},"
				+ "\"Body\":{"
				+ "		\"ProductOffering\":{";
		if(!plan.isEmpty()){
			request+= "			\"ID\":\""+plan+"\",";
		}
		request+="\"specificationSubtype\":\""+specificationSubtype+"\","
				+ "			\"specificationType\":\""+specificationType+"\","
				+ "			\"subFamily\":\""+subFamily+"\","
				+ "			\"CustomerAccount\":{"
				+ "				\"Asset\":{"
				+ "					\"MSISDN\":{"
				+ "						\"SN\":\""+MSISDN+"\"}}},"
				+ "			\"SalesChannel\":{\"ID\":\"IVR\"}"
				+ "}}}";

		Debug("[FunctionsEPCS.getAvailableProductOffer] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00066",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getAvailableProductOffer_v2(String plan, String specificationSubtype, String specificationType, String subFamily, String MSISDN,  String segment, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSAvailableProductOffer_v2", "http://10.49.4.78:7011/ESC/AvailableProductOffer/v2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOAvailableProductOffer_v2", "1000000"));
		String wsdlLocation=Params.GetValue("WLAvailableProductOffer_v2", "file:WSDL/Services/AvailableProductOffer_v1/AvailableProductOffer_v2_ESC.wsdl");
		AvailableProductOfferV2 cliente = new AvailableProductOfferV2(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);	
		String request = "{"
				+ "		\"RequestHeader\":{"
				+ "			\"Consumer\":{"
				+ "				\"sysCode\":\"CHL-IVR\","
				+ "				\"enterpriseCode\":\"ENTEL-CHL\","
				+ "				\"countryCode\":\"CHL\""
				+ "			}"
				+ "		},"
				+ "		\"Body\":{"
				+ "			\"ProductOffering\":{";
		if(!plan.isEmpty()){
			request+= "		\"ID\":\""+plan+"\",";
		}
		if(!specificationSubtype.isEmpty()){
			request += "	\"specificationSubtype\":\""+specificationSubtype+"\",";
		}
		if(!specificationType.isEmpty()){
			request += "				\"specificationType\":\""+specificationType+"\",";
		}
		request += "			\"subFamily\":\""+subFamily+"\","
				+ "				\"CustomerAccount\":{";
		if(!MSISDN.isEmpty()){
			request += "	\"Asset\":{"
					+ "						\"MSISDN\":{"
					+ "							\"SN\":\""+MSISDN+"\""
					+ "						}"
					+ "					}";
		}
		request += (!MSISDN.isEmpty() && !segment.isEmpty()) ? ",": "" ;
		if(!segment.isEmpty()){				
			request += "	\"revenueClass\":\""+segment+"\"";
		}
		request += "	},"
				+ "				\"SalesChannel\":{"
				+ "					\"ID\":\"IVR\""
				+ "				}"
				+ "			}"
				+ "		}"
				+ "}";

		Debug("[FunctionsEPCS.getAvailableProductOffer_v2] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00066",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String createRequestActivity(String area, String channel, String state, String subArea, String subState, String type, String iNumber, String iType, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSRequestActivity", "http://10.49.15.149:7010/ESC/ServiceRequestActivity/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TORequestActivity", "1000000"));
		String wsdlLocation=Params.GetValue("WLRequestActivity", "http://10.49.15.149:7010/ESC/ServiceRequestActivity/v1?wsdl");
		Debug("[FunctionsEPCS.createRequestActivity] url "+url, "DEBUG");
		Debug("[FunctionsEPCS.createRequestActivity] maxTimeout "+maxTimeout, "DEBUG");
		ServiceRequestActivity cliente = new ServiceRequestActivity(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		Debug("[FunctionsEPCS.createRequestActivity] Constructor creado ", "DEBUG");
		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},"
						+"\"Body\":{"
						+	"\"ServiceRequest\":{"
						+		"\"area\": \""+area+"\","
						+		"\"chanelIn\": \""+channel+"\","
						+		"\"state\": \""+state+"\","
						+		"\"subArea\": \""+subArea+"\","
						+		"\"subState\": \""+subState+"\","
						+		"\"type\": \""+type+"\","
						+		"\"IndividualIdentification\":{"
						+			"\"number\":\""+iNumber+"\","
						+			"\"type\":\""+iType+"\"}}}}";
		
		Debug("[FunctionsEPCS.createRequestActivity] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.create(request, createEventID("OPER_00110",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	


	public String getContract(String MSISDN, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSContract", "http://10.49.4.78:7011/ESC/Contract/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOContract", "1000000"));
		String wsdlLocation=Params.GetValue("WLContract", "file:WSDL/Services/Contract_v1/Contract_v1_ESC.wsdl");
		Contract cliente = new Contract(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\",\"Session\":{\"key\":\"BU_ID\",\"value\":\"2\"}}}}";
		Debug("[FunctionsEPCS.getContract] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00110",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getServiceContract(String ID, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSServiceContract", "http://10.49.4.86:7011/ESC/ServicesContract/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOServiceContract", "1000000"));
		String wsdlLocation=Params.GetValue("WLServiceContract", "file:WSDL/Services/ServicesContract_v1/ServicesContract_v1_ESC.wsdl");
		ServiceContract cliente = new ServiceContract(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccount\":{\"Asset\":{\"Contract\":{\"ID\":\""+ID+"\"}},\"Session\":{\"key\":\"BU_ID\",\"value\":\"2\"}}}}";
		Debug("[FunctionsEPCS.getServiceContract] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00068",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getMSISDN(String MSISDN, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSMsisdn", "http://10.49.4.86:7011/ESC/MSISDN/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOMsisdn", "1000000"));
		String wsdlLocation=Params.GetValue("WLMsisdn", "file:WSDL/Services/MSISDN_v1/MSISDN_v1_ESC.wsdl");
		Contract cliente = new Contract(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"},\"Operator\":{\"network\":\"E.164\",\"plmnCode\":\"CHLMV\",\"submarketID\":\"GSM\"},\"Session\":{\"key\":\"BU_ID\",\"value\":\"2\"}}}}";
		Debug("[FunctionsEPCS.getMSISDN] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00107",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getCustomerAccount(String MSISDN, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSCustomerAccount", "http://10.49.4.78:7011/ESC/CustomerAccount/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOCustomerAccount", "1000000"));
		String wsdlLocation=Params.GetValue("WLCustomerAccount", "file:WSDL/Services/CustomerAccount_v1/CustomerAccount_v1_ESC.wsdl");
		CustomerAccount cliente = new CustomerAccount(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		//String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\",\"Session\":{\"key\":\"BU_ID\",\"value\":\"2\"}}}}";
		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccountAsset\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}}}}}";
		Debug("[FunctionsEPCS.getCustomerAccount] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00005",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String oneClickToOrder(String MSISDN, String planActual,String area,String biType, String subArea,String type,String action,String PO, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSOneClickToOrder", "http://10.49.4.78:7011/ESC/ProductOrder/v2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOOneClickToOrder", "1000000"));
		String wsdlLocation=Params.GetValue("WLOneClickToOrder", "file:WSDL/Service/OneClickToOrder_v1/OneClickToOrder_v1_ESC.wsdl");
		OneClickToOrder cliente = new OneClickToOrder(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		Random rng = new Random();
		long timeStamp=System.currentTimeMillis()/1000;
		long dig = rng.nextInt(900)+99;
		String requestID = "0078"+timeStamp+dig;

		String request = "{\"RequestHeader\":{"
				+ "	\"Consumer\":{"
				+ "		\"sysCode\":\"CHL-IVR\","
				+ "		\"enterpriseCode\":\"ENTEL-CHL\","
				+ "		\"countryCode\":\"CHL\""
				+ "	}"
				+ "},"
				+ "  \"Body\":{"
				+ "  	\"Asset\":{"
				+ "			\"ID\":\""+planActual+"\","
				+ "			\"MSISDN\":{\"SN\":\""+MSISDN+"\"}"
				+ "		},"
				+ "  	\"CustomerOrder\":{"
				+ "			\"area\":\""+area+"\","
				+ "			\"biType\":\""+biType+"\","
				+ "			\"channel\": \"IVR\","
				+ "			\"mode\": \"NON_INTERACTIVE\","
				+ "			\"orderType\": \"Orden\","
				+ "			\"requestID\": \""+requestID+"\","
				+ "			\"subArea\": \""+subArea+"\","
				+ "			\"operationType\":\""+type+"\","
				+ "			\"SalesChannel\":{"
				+ "				\"createBy\": \"AutomaticoEntel\","
				+ "				\"orderCommercialChannel\": \"IVR\""
				+ "			}"
				+ "		},"
				+ "  	\"CustomerOrderItem\":{"
				+ "			\"action\":\""+action+"\","
				+ "			\"biType\":\"ProductOfferingOrder\","
				+ "			\"requestID\": \""+requestID+"\","
				+ "			\"subArea\": \""+subArea+"\","
				+ "			\"operationType\":\""+type+"\","
				+ "			\"ProductOffering\":{"
				+ "				\"ID\": \""+PO+"\","
				+ "				\"quantity\": {\"amount\":1}"
				+ "			}"
				+ "		}"
				+ "	}"
				+ "}";
		Debug("[FunctionsEPCS.OneClickToOrder] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.create(request, createEventID("OPER_00349",idLlamada),sourceID,processCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	/*	
	public String createOneClickToOrderActivacion(String number,String area,String biType, String subArea,String operationType,String action,String PO, String IndividualIdentification, String resource, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSOneClickToOrder", "http://10.49.4.78:7011/ESC/ProductOrder/v2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOOneClickToOrder", "1000000"));
		String wsdlLocation=Params.GetValue("WLOneClickToOrder", "file:WSDL/Service/OneClickToOrder_v1/OneClickToOrder_v1_ESC.wsdl");
		OneClickToOrder cliente = new OneClickToOrder(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		Random rng = new Random();
		long timeStamp=System.currentTimeMillis()/1000;
	    long dig = rng.nextInt(900)+99;
	    String requestID = "0078"+timeStamp+dig;

		String request = "{\"RequestHeader\":{"
				+ "	\"Consumer\":{"
				+ "		\"sysCode\":\"CHL-IVR\","
				+ "		\"enterpriseCode\":\"ENTEL-CHL\","
				+ "		\"countryCode\":\"CHL\""
				+ "	}"
				+ "},"
				+ "  \"Body\":{"
				+ "  	\"CustomerOrder\":{"
				+ "			\"area\":\""+area+"\","
				+ "			\"biType\":\""+biType+"\","
				+ "			\"channel\": \"IVR\","
				+ "			\"mode\": \"NON_INTERACTIVE\","
				+ "			\"orderType\": \"Orden\","
				+ "			\"requestID\": \""+requestID+"\","
				+ "			\"subArea\": \""+subArea+"\","
				+ "			\"operationType\":\""+operationType+"\","
				+ "			\"RelatedParty\":{"
				+ IndividualIdentification
				+ "			},"
				+ "			\"SalesChannel\":{"
				+ "				\"createBy\": \"AutomaticoEntel\","
				+ "				\"orderCommercialChannel\": \"IVR\""
				+ "			},"
				+ "		},"
				+ "  	\"CustomerOrderItem\":{"
				+ "			\"action\":\""+action+"\","
				+ "			\"biType\":\"ProductOfferingOrder\","
				+ "			\"requestID\": \""+requestID+"\","
				+ "			\"ProductOffering\":{"
				+ "				\"ID\": \""+PO+"\","
				+ "				\"quantity\": {\"amount\":1}"
				+ "			},"
				+ resource
				+ "		}"
				+ "	}"
				+ "}";
		Debug("[FunctionsEPCS.OneClickToOrder] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.create(request, createEventID("OPER_00349",idLlamada),sourceID,processCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	 */

	public String getAsset_v2(String MSISDN, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSAsset_v2", "http://10.49.4.78:7011/ESC/Asset/v2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOAsset_v2", "1000000"));
		String wsdlLocation=Params.GetValue("WLAsset_v2", "file:WSDL/Services/Asset_v2/Asset_v2_ESC.wsdl");
		Asset cliente = new Asset(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		String request = "{\"RequestHeader\":{"
				+ "			\"Consumer\":{"
				+ "				\"sysCode\":\"CHL-IVR\","
				+ "				\"enterpriseCode\":\"ENTEL-CHL\","
				+ "				\"countryCode\":\"CHL\""
				+ "			}"
				+ "		},"
				+ "			\"Body\":{"
				+ "				\"Product\":{"
				+ "					\"Filter\":{"
				+ "						\"name\":\"Offers\""
				+ "					},"
				+ "					\"ProductAccount\":{"
				+ "						\"MSISDN\":{"
				+ "							\"SN\":\""+MSISDN+"\""
				+ "						}"
				+ "					}"
				+ "				}"
				+ "			}"
				+ "}";
		Debug("[FunctionsEPCS.getAsset] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.get(request, createEventID("OPER_0056",idLlamada),sourceID,processCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getCustomerAccountAsset(String MSISDN, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSCustomerAccountAsset", "http://10.49.4.78:7011/ESC/CustomerAccountAsset/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOCustomerAccountAsset", "1000000"));
		String wsdlLocation=Params.GetValue("WLCustomerAccountAsset", "file:WSDL/Services/CustomerAccountAsset_v1/CustomerAccountAsset_v1_ESC.wsdl");
		CustomerAccountAsset cliente = new CustomerAccountAsset(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccountAsset\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}}}}}";

		Debug("[FunctionsEPCS.getCustomerAccountAsset] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00137",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getCustomerAccountBalance(String MSISDN, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSCustomerAccountBalance", "http://10.49.4.78:7011/ESC/CustomerAccountBalance/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOCustomerAccountBalance", "1000000"));
		String wsdlLocation=Params.GetValue("WLCustomerAccountBalance", "file:WSDL//Services/CustomerAccountBalance_v1/CustomerAccountBalance_v1_ESC.wsdl");
		CustomerAccountBalance cliente = new CustomerAccountBalance(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccount\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}},\"BusinessInteraction\":{\"interactionExternalID\":\"IVR\"}}}}";
		Debug("[FunctionsEPCS.getCustomerAccountBalance] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00057",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getCustomerAccountBalance_v2(String MSISDN,String Filter, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSCustomerAccountBalance_v2", "http://10.49.4.78:7011/ESC/CustomerAccountBalance/v2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOCustomerAccountBalance_v2", "1000000"));
		String wsdlLocation=Params.GetValue("WLCustomerAccountBalance_v2", "file:WSDL/Services/CustomerAccountBalance_v2/CustomerAccountBalance_v2_ESC.wsdl");
		CustomerAccountBalanceV2 cliente = new CustomerAccountBalanceV2(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccount\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}},\"BusinessInteraction\":{\"interactionExternalID\":\"IVR\"},\"Filter\":{\"name\":\""+Filter+"\"}}}}";

		Debug("[FunctionsEPCS.getCustomerAccountBalance_v2] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00057",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getCustomerAccountBalanceAndCharge(String MSISDN, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSCustomerAccountBalanceAndCharge", "http://10.49.4.78:7011/ESC/CustomerAccountBalanceAndCharge/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOCustomerAccountBalanceAndCharge", "1000000"));
		String wsdlLocation=Params.GetValue("WLCustomerAccountBalanceAndCharge", "file:WSDL/Services/CustomerAccountBalanceAndCharge_v1/CustomerAccountBalanceAndCharge_v1_ESC.wsdl");
		CustomerAccountBalanceAndCharge cliente = new CustomerAccountBalanceAndCharge(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccount\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}},\"BusinessInteraction\":{\"interactionExternalID\":\"IVR\"}}}}";
		Debug("[FunctionsEPCS.getCustomerAccountBalanceAndCharge] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00285",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getCustomerAccountBalanceAndCharge_v2(String MSISDN,String Filter, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSCustomerAccountBalanceAndCharge_v2", "http://10.49.4.78:7011/ESC/CustomerAccountBalanceAndCharge/v2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOCustomerAccountBalanceAndCharge_v2", "1000000"));
		String wsdlLocation=Params.GetValue("WLCustomerAccountBalanceAndCharge_v2", "file:WSDL/Services/CustomerAccountBalanceAndCharge_v2/CustomerAccountBalanceAndCharge_v2_ESC.wsdl");
		CustomerAccountBalanceAndChargeV2 cliente = new CustomerAccountBalanceAndChargeV2(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);
		String request = "{\"RequestHeader\":{"
				+ "			\"Consumer\":{"
				+ "				\"sysCode\":\"CHL-IVR\","
				+ "				\"enterpriseCode\":\"ENTEL-CHL\","
				+ "				\"countryCode\":\"CHL\""
				+ "			}"
				+ "		  },"
				+ "		  \"Body\":{"
				+ "			\"CustomerAccount\":{"
				+ "				\"Asset\":{"
				+ "					\"MSISDN\":{"
				+ "						\"SN\":\""+MSISDN+"\""
				+ "					}"
				+ "				},"
				+ "				\"BusinessInteraction\":{"
				+ "					\"interactionExternalID\":\"IVR\""
				+ "				},"
				+ "				\"Filter\":{"
				+ "					\"name\":\""+Filter+"\""
				+ "				}"
				+ "			}"
				+ "		  }"
				+ "		}";
		Debug("[FunctionsEPCS.getCustomerAccountBalanceAndCharge_v2] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00285",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}


	public String getFrequentNumber(String MSISDN, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSFrequentNumber", "http://10.49.4.78:7011/ESC/FrequentNumber/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOFrecuentNumber", "1000000"));
		String wsdlLocation=Params.GetValue("WLFrequentNumber", "file:WSDL/Services/FrequentNumber_v1/FrequentNumber_v1_ESC.wsdl");
		FrecuentNumber cliente = new FrecuentNumber(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccount\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}},\"BusinessInteraction\":{\"interactionExternalID\":\"0078\"}}}}";
		Debug("[FunctionsEPCS.getFrequentNumber] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.get(request, createEventID("OPER_00138",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getLoyaltyBalance(String RUT, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSLoyaltyBalance", "http://10.49.4.78:7011/ESC/LoyaltyBalance/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOLoyaltyBalance", "1000000"));
		String wsdlLocation=Params.GetValue("WLLoyaltyBalance", "file:WSDL/Services/LoyaltyBalance_v1/LoyaltyBalance_v1_ESC.wsdl");
		LoyaltyBalance cliente = new LoyaltyBalance(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccount\":{\"LoyaltyProgramMember\":{\"memberNumber\":\""+RUT+"\"}}}}";
		Debug("[FunctionsEPCS.getLoyaltyBalance] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.get(request, createEventID("OPER_00026",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getLoyaltyProduct(String RUT, String PC_ID, String type, String typeCode, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSLoyaltyProduct", "http://10.49.4.78:7011/ESC/LoyaltyProduct/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOLoyaltyProduct", "1000000"));
		String wsdlLocation=Params.GetValue("WLLoyaltyProduct", "file:WSDL/Services/LoyaltyProduct_v1/LoyaltyProduct_v1_ESC.wsdl");
		LoyaltyProduct cliente = new LoyaltyProduct(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"LoyaltyProgramProduct\":[{\"LoyaltyTransaction\":[{\"SalesChannel\":{\"orderCommercialChannel\": \"303\"},\"LoyaltyProgramMember\":{\"ID\" : \""+RUT+"\"}}],\"Product\":{\"ProductCategory\":{\"id\":\""+PC_ID+"\"},\"type\":\""+type+"\",\"typeCode\":\""+typeCode+"\"},\"activeFlag\":true}]}}";
		Debug("[FunctionsEPCS.getLoyaltyProduct] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.get(request, createEventID("OPER_00139",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getMigrationInfo(String MSISDN, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSMigrationInfo", "http://10.49.4.78:7011/ESC/MigrationInfo/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOMigrationInfo", "1000000"));
		String wsdlLocation=Params.GetValue("WLMigrationInfo", "file:WSDL/Services/MigrationInfo_v1/MigrationInfo_v1_ESC.wsdl");
		MigrationInfo cliente = new MigrationInfo(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccount\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}}}}}";
		Debug("[FunctionsEPCS.getMigrationInfo] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.get(request, createEventID("OPER_00018",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getProductOrder(String shoppingCartID, String sourceID, String processCode, String idLlamada){
		int maxTimeout = Integer.parseInt(Params.GetValue("TOProductOrder", "1000000"));
		String url = Params.GetValue("WSProductOrder", "http://10.49.4.78:7011/ESC/ProductOrder/v1");
		String wsdlLocation=Params.GetValue("WLProductOrder", "file:WSDL/Services/ProductOrder_v1/ProductOrder_v1_ESC.wsdl");
		ProductOrder cliente = new ProductOrder(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\", \"countryCode\":\"CHL\"}},\"Body\":{\"CustomerOrder\":{\"shoppingCartID\":\""+shoppingCartID+"\"}}}";
		Debug("[FunctionsEPCS.getProductOrder] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.get(request, createEventID("OPER_00077",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getRefillOffer(String MSISDN, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSRefillOffer", "http://10.49.4.78:7011/CVL/SOAP12/RefillOffer/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TORefillOffer", "1000000"));
		String wsdlLocation=Params.GetValue("WLRefillOffer", "file:WSDL/CVL_RefillOffer_v1/Resources/WSDL/SOAP12/RefillOffer_v1_ESC.wsdl");
		RefillOffer cliente = new RefillOffer(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request="{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"},\"ThirdPartyPayeeAgency\":{\"platform\":\"IVR\"}}}}";

		Debug("[FunctionsEPCS.getRefillOffer] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.get(request, createEventID("OPER_00251",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getUsageThresholdCounter(String MSISDN, String sourceID, String processCode, String idLlamada ){
		String url = Params.GetValue("WSUsageThresholdCounter", "http://10.49.4.78:7011/ESC/UsageThresholdsCounter/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOUsageThresholdCounter", "1000000"));
		String wsdlLocation=Params.GetValue("WLUsageThresholdCounter", "file:WSDL/Services/UsageThresholdCounter_v1/UsageThresholdCounter_v1_ESC.wsdl");
		UsageThresholdCounter cliente = new UsageThresholdCounter(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerAccount\":{\"Asset\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"}},\"BusinessInteraction\":{\"interactionExternalID\":\"IVR\"}}	}}";
		Debug("[FunctionsEPCS.getUsageThresholdCounter] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.get(request, createEventID("OPER_00258",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public boolean sendSMS(String sMovil, String sMensajeSMS){
		boolean estado = false;
		String tmpMovil = sMovil;

		try{
			boolean debugEnabled = Params.GetValue("DEBUG_SMS_ENABLED", "NO").equalsIgnoreCase("SI");
			if (debugEnabled)
				tmpMovil = Params.GetValue("DEBUG_SMS_PCS");


			String DataEnvioSMS = "IvrToMQ:MQPUTSMS.REQ:Ivan|Ivan|" + tmpMovil + "|" + sMensajeSMS + "|" ; 
			Debug("[FunctionsEPCS.sendSMS] Data de entrada = ["+ DataEnvioSMS + "]","Detail");

			String respuestaMQPUT  = Socket_SendRecvHA("MQPUTSMS", DataEnvioSMS);
			Debug("[FunctionsEPCS.sendSMS] Data de salida = ["+ respuestaMQPUT + "]","Detail");

			String[] res_array = respuestaMQPUT.split("\\|");

			if (res_array[0].equals("0000")){
				estado = true;
				Debug("[FunctionsEPCS.sendSMS] SMS enviado exitosamente.","Trace");
			}else
				Debug("[FunctionsEPCS.sendSMS] No se pudo enviar el SMS.","Standard");
		}catch (Exception e) {
			// TODO: handle exception
			Debug("[FunctionsEPCS.sendSMS] Se produjo una excepcion = " + e.toString(),"Standard");
		}

		return estado;
	}

	public String Socket_SendRecv (String Server, int Port, int Timeout, String Transaccion, String Message2Send)throws Exception
	{
		String sReturn = "";
		Socket SocketServer = null;
		//DataOutputStream SocketServerOutputStream = null;
		PrintWriter SocketServerOutputStream = null;
		// DataInputStream SocketServerInputStream = null;
		BufferedReader SocketServerInputStream = null;

		try
		{

			//Date startTime;
			//Date stopTime;

			Debug("[FunctionsEPCS.Socket_SendRecv] - Se ejecutar la transacción : " + Transaccion + " - " + Server + ":" + Port + " - TimeOut:" + String.valueOf(Timeout), "Detail");
			Debug("[FunctionsEPCS.Socket_SendRecv] -  > Data [" + Message2Send + "]", "Detail");

			//startTime = new Date();

			try
			{
				SocketServer = new Socket(Server, Port);
				SocketServer.setKeepAlive(true);
				SocketServer.setSoTimeout(Timeout);

				// SocketServerInputStream = new DataInputStream(SocketServer.getInputStream());
				SocketServerInputStream = new BufferedReader(new InputStreamReader(SocketServer.getInputStream()));
				//SocketServerOutputStream = new DataOutputStream(SocketServer.getOutputStream());
				SocketServerOutputStream = new PrintWriter( new OutputStreamWriter(SocketServer.getOutputStream()),true);
			}
			catch (IOException e)
			{
				Debug("[FunctionsEPCS.Socket_SendRecv] - IOException openning stream buffers: [" + e.toString() + "]", "Standard");
				throw ((Exception) e);
			}

			if (SocketServer != null && SocketServerOutputStream != null && SocketServerInputStream != null)
			{
				try
				{
					//SocketServerOutputStream.writeBytes(Message2Send);
					SocketServerOutputStream.println(Message2Send);
					sReturn = SocketServerInputStream.readLine();
					Debug("[FunctionsEPCS.Socket_SendRecv] - Respuesta [" + sReturn + "]", "Detail");
				}
				catch (Exception e)
				{
					Debug("[FunctionsEPCS.Socket_SendRecv] - Exception Writing Message/Getting Answer: [" + e.toString() + "]", "Standard");
					throw (e);
				}
			}
			//stopTime = new Date ();

			//Registrar(Message2Send, startTime, sReturn, stopTime);

			return sReturn;
		}
		catch (Exception e)
		{
			Debug("[FunctionsEPCS.Socket_SendRecv] - Exception sending message: [" + e.toString() + "]", "Standard");
			throw (e);
		}
		finally {
			SocketServerOutputStream.close();
			SocketServerInputStream.close();
			SocketServer.close();
		}
	}

	public String Socket_SendRecvHA (String Transaccion, String Message2Send)
	{
		String sReturn = "";
		String ServerPRI = Params.GetValue("SocketServerHostPRI", "200.13.15.121");
		String ServerBKP = Params.GetValue("SocketServerHostBKP", "200.13.15.121");
		int PortPRI = Integer.parseInt(Params.GetValue("SocketServerPortPRI", "50020").trim());
		int PortBKP = Integer.parseInt(Params.GetValue("SocketServerPortBKP", "50020").trim());
		int TimeoutPRI = Integer.parseInt(Params.GetValue("SocketServerTimeoutPRI", "9000").trim());
		int TimeoutBKP = Integer.parseInt(Params.GetValue("SocketServerTimeoutBKP", "9000").trim());

		Date startTime;
		Date stopTime;

		Debug("[FunctionsEPCS.Socket_SendRecvHA] Conectando con SocketServer Primario.", "Detail");

		startTime = new Date();

		try{			
			sReturn = Socket_SendRecv(ServerPRI, PortPRI, TimeoutPRI, Transaccion, Message2Send);
		}catch (Exception e)
		{
			Debug("[FunctionsEPCS.Socket_SendRecvHA] Conexion SocketServer Primario fallida, intentando con SocketServer Backup.", "Standard");
			try {
				sReturn = Socket_SendRecv(ServerBKP, PortBKP, TimeoutBKP, Transaccion, Message2Send);
			} catch (Exception e1)
			{
				Debug("[FunctionsEPCS.Socket_SendRecvHA] Intentos superados.", "Standard");
			}
		}

		stopTime = new Date ();

		// Registrar(Message2Send, startTime, sReturn, stopTime);

		return sReturn;
	}

	public String Socket_SendRecvHA (String Transaccion, String Message2Send, String tipo)
	{
		String sReturn = "";
		String ServerPRI = Params.GetValue("SocketServerHostPRI_"+tipo, "200.13.15.121");
		String ServerBKP = Params.GetValue("SocketServerHostBKP_"+tipo, "200.13.15.121");
		int PortPRI = Integer.parseInt(Params.GetValue("SocketServerPortPRI_"+tipo, "50020").trim());
		int PortBKP = Integer.parseInt(Params.GetValue("SocketServerPortBKP_"+tipo, "50020").trim());
		int TimeoutPRI = Integer.parseInt(Params.GetValue("SocketServerTimeoutPRI_"+tipo, "9000").trim());
		int TimeoutBKP = Integer.parseInt(Params.GetValue("SocketServerTimeoutBKP_"+tipo, "9000").trim());

		Date startTime;
		Date stopTime;

		Debug("[FunctionsEPCS.Socket_SendRecvHA] Conectando con SocketServer Primario.", "Detail");

		startTime = new Date();

		try{			
			sReturn = Socket_SendRecv(ServerPRI, PortPRI, TimeoutPRI, Transaccion, Message2Send);
		}catch (Exception e)
		{
			Debug("[FunctionsEPCS.Socket_SendRecvHA] Conexion SocketServer Primario fallida, intentando con SocketServer Backup.", "Standard");
			try {
				sReturn = Socket_SendRecv(ServerBKP, PortBKP, TimeoutBKP, Transaccion, Message2Send);
			} catch (Exception e1)
			{
				Debug("[FunctionsEPCS.Socket_SendRecvHA] Intentos superados.", "Standard");
			}
		}

		stopTime = new Date ();

		//Registrar(Message2Send, startTime, sReturn, stopTime);

		return sReturn;
	}

	public String getFechaHora(String sFormatofecha){
		TimeZone tz = TimeZone.getTimeZone(Params.GetValue("Timezone","Chile/Continental"));
		SimpleDateFormat fmt = new SimpleDateFormat(sFormatofecha);
		fmt.setTimeZone(tz);
		String sFechaHora=fmt.format(new java.util.Date());
		return sFechaHora;
	}

	public String submitProductOrder(String shoppingCartID, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSProductOrder", "http://10.49.4.78:7011/ESC/ProductOrder/v1");
		String wsdlLocation=Params.GetValue("WLProductOrder", "file:WSDL/Services/ProductOrder_v1/ProductOrder_v1_ESC.wsdl");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOProductOrder", "1000000"));
		ProductOrder cliente = new ProductOrder(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\", \"countryCode\":\"CHL\"}},\"Body\":{\"CustomerOrder\":{\"shoppingCartID\":\""+shoppingCartID+"\"}}}";
		Debug("[FunctionsEPCS.submitProductOrder] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.submit(request, createEventID("OPER_00080",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String updateProductOrder(String shoppingCartID, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSProductOrder", "http://10.49.4.78:7011/ESC/ProductOrder/v1");
		String wsdlLocation=Params.GetValue("WLProductOrder", "file:WSDL/Services/ProductOrder_v1/ProductOrder_v1_ESC.wsdl");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOProductOrder", "1000000"));
		ProductOrder cliente = new ProductOrder(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\", \"countryCode\":\"CHL\"}},\"Body\":{\"CustomerOrder\":{\"shoppingCartID\":\""+shoppingCartID+"\"}}}";
		Debug("[FunctionsEPCS.updateProductOrder] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.update(request, createEventID("OPER_00050",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String updateProductOrderItem(String COI_ID, String shoppingCartID, String resource, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSProductOrderItem", "http://10.49.4.78:7011/ESC/ProductOrderItem/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOProductOrderItem", "1000000"));
		String wsdlLocation=Params.GetValue("WLProductOrderItem", "file:WSDL/Services/ProductOrderItem_v1/ProductOrderItem_v1_ESC.wsdl");
		ProductOrderItem cliente = new ProductOrderItem(wsdlLocation, url, maxTimeout, maxTimeout, debugXML);

		String request = "{"
				+ "				\"RequestHeader\":{"
				+ "				\"Consumer\":{"
				+ "					\"sysCode\":\"CHL-IVR\","
				+ "					\"enterpriseCode\":\"ENTEL-CHL\","
				+ "					\"countryCode\":\"CHL\""
				+ "				}"
				+ "			},"
				+ "			\"Body\":{"
				+ "				\"CustomerOrderItem\":{"
				+ "					\"ID\":\""+COI_ID+"\","
				+ "					\"modiefiedBy\":\"\","
				+ "					\"CustomerOrder\":{"
				+ "						\"shoppingCartID\":\""+shoppingCartID+"\""
				+ "					},"
				+ "					\"RelatedEntity\":{},"
				+ resource
				+ "				}"
				+ "			}"
				+ "		}";
		Debug("[FunctionsEPCS.updateProductOrderItem] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.update(request, createEventID("OPER_00087",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	/*    public String updateProductOrderPaymentMethod(String shoppingCartID, String paymentMethodType){
    	String url = Params.GetValue("WSProductOrderPaymentMethod", "http://10.49.4.78:7011/ES_ProductOrderPaymentMethod_v1/MPL/PIF/ProductOrderPaymentMethod");
    	ProductOrderPaymentMethod cliente = new ProductOrderPaymentMethod(url, 8000);
    	String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"CustomerOrder\":{\"shoppingCartID\":\""+shoppingCartID+"\"},\"RelatedEntity\":{\"CustomerPayment\":{\"PaymentMethod\":{ \"paymentMethodType\":\""+paymentMethodType+"\"}}}}}";
		Debug("[FunctionsEPCS.updateProductOrderPaymentMethod] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.update(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
    	return resp;
    }*/

	public String validateRequestProductOrder(String MSISDN, String ICCID, String IMSI, String area, String orderType, String subArea, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSRequestProductOrder", "http://10.49.4.78:7011/ESC/RequestProductOrder/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TORequestProductOrder", "1000000"));
		String wsdlLocation=Params.GetValue("WLRequestProductOrder", "file:WSDL/RequestProductOrder/Services/RequestProductOrder_v1/RequestProductOrder_v1_ESC.wsdl");
		RequestProductOrder cliente = new RequestProductOrder(wsdlLocation,url, maxTimeout, maxTimeout, debugXML);

		String request = "{\"RequestHeader\":{\"Consumer\":{\"sysCode\":\"CHL-IVR\",\"enterpriseCode\":\"ENTEL-CHL\",\"countryCode\":\"CHL\"}},\"Body\":{\"Product\":{\"ProductAccount\":{\"MSISDN\":{\"SN\":\""+MSISDN+"\"},\"ICCID\":{\"ICCID\":\""+ICCID+"\"},\"IMSI\":{\"SN\":\""+IMSI+"\"}},\"CustomerAccount\":{\"CustomerOrder\":{\"area\":\""+area+"\",\"orderType\":\""+orderType+"\",\"subArea\":\""+subArea+"\"}}}}}";
		Debug("[FunctionsEPCS.validateRequestProductOrder] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.validate(request,createEventID("OPER_00142",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String validateLoyaltyBurn(String aniOrigen, String aniDestino, String rut, String amount, String type, String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSLoyaltyBurn", "http://10.49.4.86:7011/ES/Validate_LoyaltyBurn/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOLoyaltyBurn", "1000000"));
		String wsdlLocation=Params.GetValue("WLLoyaltyBurn", "file:WSDL/Services/LoyaltyProduct_v1/Validate_LoyaltyBurn_v1_ESC.wsdl");
		LoyaltyBurn cliente = new LoyaltyBurn(wsdlLocation,url, maxTimeout, maxTimeout, debugXML);

		String request  ="{" + 
				"   \"RequestHeader\":{" + 
				"      \"Consumer\":{" + 
				"         \"sysCode\":\"CHL-IVR\"," + 
				"         \"enterpriseCode\":\"ENTEL-CHL\"," + 
				"         \"countryCode\":\"CHL\"" + 
				"      }" + 
				"   }," + 
				"   \"Body\":{" + 
				"      \"LoyaltyTransaction\":{" +
				"			\"transactionType\": \""+type+"\"," + 
				"         	\"amount\": {" + 
				"            \"amount\": \""+amount+"\"" + 
				"         	},"+
				"			\"originMSISDN\":{" +
				"				\"SN\":\""+aniOrigen+"\""	+	
				"			}," + 
				"			\"destinyMSISDN\":{" +
				"				\"SN\":\""+aniDestino+"\""	+	
				"			}," + 
				"         	\"LoyaltyProgramMember\":{" + 
				"            \"memberNumber\":\""+rut+"\"" + 
				"         	}," + 
				"			\"Channel\":{"+
				"				\"description\":\"IVR\""+
				"			}"+	
				"      }" + 
				"   }" + 
				"}";

		Debug("[FunctionsEPCS.validateLoyaltyBurn] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.validate(request,createEventID("OPER_20001",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String validateLoyaltyBurn(String aniOrigen, String aniDestino, String rut, String amount, String type, String sourceID, String processCode, String idLlamada, String channel){
		String url = Params.GetValue("WSLoyaltyBurn", "http://10.49.4.86:7011/ES/Validate_LoyaltyBurn/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOLoyaltyBurn", "1000000"));
		String wsdlLocation=Params.GetValue("WLLoyaltyBurn", "file:WSDL/Services/LoyaltyProduct_v1/Validate_LoyaltyBurn_v1_ESC.wsdl");
		LoyaltyBurn cliente = new LoyaltyBurn(wsdlLocation,url, maxTimeout, maxTimeout, debugXML);

		String request  ="{" + 
				"   \"RequestHeader\":{" + 
				"      \"Consumer\":{" + 
				"         \"sysCode\":\"CHL-IVR\"," + 
				"         \"enterpriseCode\":\"ENTEL-CHL\"," + 
				"         \"countryCode\":\"CHL\"" + 
				"      }" + 
				"   }," + 
				"   \"Body\":{" + 
				"      \"LoyaltyTransaction\":{" +
				"			\"transactionType\": \""+type+"\"," + 
				"         	\"amount\": {" + 
				"            \"amount\": \""+amount+"\"" + 
				"         	},"+
				"			\"originMSISDN\":{" +
				"				\"SN\":\""+aniOrigen+"\""	+	
				"			}," + 
				"			\"destinyMSISDN\":{" +
				"				\"SN\":\""+aniDestino+"\""	+	
				"			}," + 
				"         	\"LoyaltyProgramMember\":{" + 
				"            \"memberNumber\":\""+rut+"\"" + 
				"         	}," + 
				"			\"Channel\":{"+
				"				\"description\":\""+channel+"\""+
				"			}"+	
				"      }" + 
				"   }" + 
				"}";

		Debug("[FunctionsEPCS.validateLoyaltyBurn] request "+request, "DEBUG");

		String resp = null;

		try {

			resp = cliente.validate(request,createEventID("OPER_20001",idLlamada),sourceID,processCode);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	public String getServiceRequest(String IndividualIdentification,String sourceID, String processCode, String idLlamada){
		String url = Params.GetValue("WSServiceRequest", "http://10.49.4.86:7011/ES/ServiceRequest/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TOServiceRequest", "1000000"));
		String wsdlLocation=Params.GetValue("WLServiceRequest", "file:WSDL/Services/ServiceRequest_v1/ServiceRequest_v1_ESC.wsdl");
		ServiceRequest cliente = new ServiceRequest(wsdlLocation,url, maxTimeout, maxTimeout, debugXML);

		String request ="{" + 
				"   \"RequestHeader\":{" + 
				"      \"Consumer\":{ " + 
				"         \"sysCode\":\"CHL-SIE\"," + 
				"         \"enterpriseCode\":\"ENTEL-CHL\"," + 
				"         \"countryCode\":\"CHL\"" + 
				"      }" + 
				"   }," + 
				"   \"Body\":{" + 
				"      \"ServiceRequest\":{" +
				"			\"IndividualIdentification\":{" +IndividualIdentification+"}" +
				"      }" +
				"   }" + 
				"}";

		Debug("[FunctionsEPCS.getServiceRequest] request "+request, "DEBUG");

		String resp = null;

		try {
			resp = cliente.get(request,createEventID("OPER_20001",idLlamada),sourceID,processCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}


	public String consultaVasSagen(String movil) {
		String resp = "";
		String url = Params.GetValue("urlConsultaVasSagen","http://172.18.155.50:7003/Sagen/WSSagenService");
		int timeout = Integer.parseInt(Params.GetValue("timeoutConsultaVasSagen","5000"));
		String vasID = Params.GetValue("vasIDConsultaVasSagen","PROVIDERS");
		String providerID = Params.GetValue("providerIDConsultaVasSagen","Mapfre");
		String suscriptionID = Params.GetValue("suscriptionIDConsultaVasSagen","sus_contents01");
		String la = Params.GetValue("laConsultaVasSagen","7270"); 
		ConsumidorSagen cs = new ConsumidorSagen(url,timeout); 
		try {
			resp = cs.searchSuscription(movil, vasID, providerID, suscriptionID, la);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
	public JSONObject crearHeader(String servicename,String IDllamada, String processID, String sourceID) {
		JSONObject RequestHeader= new JSONObject();
		JSONObject Consumer = new JSONObject();
		JSONObject Trace = new JSONObject();
		JSONObject Channel = new JSONObject();
		JSONObject Service = new JSONObject();
		try {
			String service_code = Params.GetValue("SERVICE_CODE_"+servicename, "");
			String service_name = Params.GetValue("SERVICE_NAME_"+servicename, "");
			String service_operation = Params.GetValue("SERVICE_OPERATION_"+servicename, "");  

			String sysCode = Params.GetValue("Prepago_sysCode", "IVR");
			String enterpriseCode = Params.GetValue("Prepago_enterpriseCode","ENTEL-CHL");
			String countryCode = Params.GetValue("Prepago_countryCode","CHL");
			String channelName = Params.GetValue("Prepago_channelName", "IVR");
			String channelMode = Params.GetValue("Prepago_channelMode","NO PRESENCIAL"); 
			String eventID = Params.GetValue("Prepago_eventID","0078"); 
			long timeStamp=System.currentTimeMillis()/1000;

			Consumer.put("sysCode",sysCode);
			Consumer.put("enterpriseCode",enterpriseCode);
			Consumer.put("countryCode",countryCode);

			Service.put("code", service_code);
			Service.put("name", service_name);
			Service.put("operation", service_operation);

			GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
			XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);

			Trace.put("clientReqTimestamp",xgcal);
			Trace.put("reqTimestamp",xgcal);
			Trace.put("processID", processID);
			Trace.put("eventID",eventID+service_code+timeStamp+IDllamada+"000");
			Trace.put("sourceID",sourceID);

			Channel.put("name",channelName);
			Channel.put("mode",channelMode);
			RequestHeader.put("Consumer", Consumer);
			Trace.put("Service",Service);
			RequestHeader.put("Trace", Trace);
			RequestHeader.put("Channel", Channel);

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS.CrearHeader] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally{
			Consumer = null;
			Trace = null;
			Channel = null;
			Service = null; 	
		}

		return RequestHeader; 
	}

	public String ejecutarRest(String url, JSONObject request, long timeout) {
		String respuesta = "";
		try {
			WebClient client = WebClient.create(url);
			client = client.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
			HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
			conduit.getClient().setConnectionTimeout(timeout);
			conduit.getClient().setReceiveTimeout(timeout);
			Response r = client.post(request.toString());
			if(r.getStatus() != 500) {
				respuesta = r.readEntity(String.class);
			}else {
				respuesta = "{\"codigoError\":\"EXCEPTION\",\"descripcion\":\"Internal server error 500\"}";
			}
			client.close();
		}catch(Exception e) {
			e.printStackTrace();
			respuesta = "{\"codigoError\":\"EXCEPTION\",\"descripcion\":"+e.getMessage()+"}";
		}
		return respuesta;
	}
	public String createProductOrderREST(String area, String biType, String CustomerAccountID, String mode, String orderType, String state, String subArea, String BillingAccount, String IndividualIdentification,String ServiceRequestID, String IDllamada, String processID, String SourceID){
		String metodo = "createProductOrderREST";
		String url = Params.GetValue("URL_CreateProductOrder", "http://10.49.4.86:7011/ES/JSON/CreateProductOrder/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_CreateProductOrder", "1000000"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject body = new JSONObject();
		JSONObject CustomerOrder = new JSONObject(); 
		JSONArray arrayRelatedEntity = new JSONArray();
		JSONArray arrayAccount = new JSONArray();
		JSONObject RelatedEntity = new JSONObject();
		JSONObject Account = new JSONObject();
		JSONObject SalesChannel = new JSONObject();
		JSONArray arrayRelatedParty = new JSONArray();
		JSONObject RelatedParty = new JSONObject();
		JSONObject CustomerAccount = new JSONObject();
		JSONObject ServiceRequest = new JSONObject();
		JSONObject objBillingAccount = null;
		JSONObject objIndividualIdentification = null;
		
		GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
		XMLGregorianCalendar xgcal = null;
		try {
			xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
		} catch (DatatypeConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			objBillingAccount = new JSONObject(BillingAccount);
			objIndividualIdentification = new JSONObject(IndividualIdentification);
			
			Random rng = new Random();

			long timeStamp=System.currentTimeMillis()/1000;
			long dig = rng.nextInt(900)+99;
			String requestID = "0078"+timeStamp+dig;

			CustomerOrder.put("area", area);
			CustomerOrder.put("biType", biType);
			CustomerOrder.put("createdBy", CustomerAccountID);
			CustomerOrder.put("createdDate", xgcal);
			CustomerOrder.put("mode", mode);
			CustomerOrder.put("orderType", orderType);
			CustomerOrder.put("owner", CustomerAccountID);
			CustomerOrder.put("requestID", requestID);
			CustomerOrder.put("requester", CustomerAccountID);
			CustomerOrder.put("state", state);
			CustomerOrder.put("subArea", subArea);
			CustomerOrder.put("channel", "IVR");

			Account.put("externalID", CustomerAccountID);
			Account.put("type", "billing");
			Account.put("BillingAccount", objBillingAccount);
			arrayAccount.put(Account);
			SalesChannel.put("createBy", "AutomaticoEntel");
			SalesChannel.put("orderCommercialChannel", "IVR");
			RelatedEntity.put("Account", arrayAccount);
			RelatedEntity.put("SalesChannel", SalesChannel);
			arrayRelatedEntity.put(RelatedEntity);
			CustomerOrder.put("RelatedEntity", arrayRelatedEntity);

			CustomerAccount.put("ID", CustomerAccountID);
			RelatedParty.put("CustomerAccount", CustomerAccount);
			RelatedParty.put("IndividualIdentification", objIndividualIdentification);
			arrayRelatedParty.put(RelatedParty);
			CustomerOrder.put("RelatedParty", arrayRelatedParty);

			ServiceRequest.put("ID", ServiceRequestID);
			CustomerOrder.put("ServiceRequest", ServiceRequest);
			body.put("CustomerOrder", CustomerOrder);

			JSONObject header = crearHeader("CreateProductOrder", IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+metodo+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout);				
			Debug("[FunctionsEPCS."+metodo+"] Response "+resp, "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+metodo+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			CustomerOrder = null; 
			arrayRelatedEntity = null;
			arrayAccount = null;
			RelatedEntity = null;
			Account = null;
			SalesChannel = null;
			objBillingAccount = null;
			arrayRelatedParty = null;
			RelatedParty = null;
			CustomerAccount = null;
			ServiceRequest = null;

		}
		return resp;
	}
	public String getUsageThresholdCounterREST(String MSISDN, String ProductID, String IDllamada, String processID, String SourceID){
		String metodo="GetUsageThresholdCounterREST";
		String url = Params.GetValue("URL_GetUsageThresholdCounter", "http://10.49.4.86:7011/ES/JSON/GetUsageThresholdCounter/v2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_GetUsageThresholdCounter", "1000000"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject body = new JSONObject(); 
		JSONObject CustomerAccount = new JSONObject();
		JSONObject Asset = new JSONObject(); 
		JSONObject objMSISDN = new JSONObject(); 
		JSONObject Product = new JSONObject();

		try {	 
			objMSISDN.put("SN", MSISDN);
			Asset.put("MSISDN", objMSISDN);
			CustomerAccount.put("Asset", Asset);
			body.put("CustomerAccount", CustomerAccount);

			if(!ProductID.equals("")){
				Product.put("ID", ProductID);
				body.put("Product", Product);
			}


			JSONObject header = crearHeader("GetUsageThresholdCounter", IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+metodo+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout);				
			Debug("[FunctionsEPCS."+metodo+"] Response "+resp, "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+metodo+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			CustomerAccount = null;
			Asset = null; 
			objMSISDN = null; 
			Product=null;

		}
		return resp;
	}
	public String updateUsageThresholdCounterREST(String MSISDN, String ProductID, String amountProductUsage, String IDllamada, String processID, String SourceID){
		String metodo="UpdateUsageThresholdCounterREST";
		String url = Params.GetValue("URL_UpdateUsageThresholdCounter", "http://10.49.4.86:7011/ES/JSON/UpdateUsageThresholdCounter/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_UpdateUsageThresholdCounter", "1000000"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject body = new JSONObject(); 
		JSONObject CustomerAccount = new JSONObject();
		JSONObject Asset = new JSONObject(); 
		JSONObject objMSISDN = new JSONObject(); 
		JSONObject Product = new JSONObject();

		try {	 
			objMSISDN.put("SN", MSISDN);
			Asset.put("MSISDN", objMSISDN);
			CustomerAccount.put("Asset", Asset);
			body.put("CustomerAccount", CustomerAccount);

			if(!ProductID.equals("")){
				JSONObject ProductUsage = new JSONObject();
				ProductUsage.put("amount", amountProductUsage);
				Product.put("ID", ProductID);
				Product.put("ProductUsage", ProductUsage);
				body.put("Product", Product);
			}


			JSONObject header = crearHeader("UpdateUsageThresholdCounter", IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+metodo+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout);				
			Debug("[FunctionsEPCS."+metodo+"] Response "+resp, "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+metodo+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			CustomerAccount = null;
			Asset = null; 
			objMSISDN = null; 
			Product=null;

		}
		return resp;
	}
}