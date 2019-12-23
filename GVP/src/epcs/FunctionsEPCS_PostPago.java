package epcs;   
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;


import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.experian.sio.principal.EvaluacionCrediticia;

import cl.econtact.principal.Teardown;
import cl.entel.asset.principal.Asset;
import cl.entel.loyaltybalance.principal.LoyaltyBalance;
import eContact.FunctionsGVP;
import eContact.OracleDBAccess;
import eContact.Parameters;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;

public class FunctionsEPCS_PostPago extends FunctionsGVP
{
	public Parameters parametrosBD = new Parameters(); 
	public String  hostIvrToDB     = "127.0.0.1";
	public int     portIvrToDB     = 50081;
	public String InstanceID = "";
	private static              Connection conn     = null;
    private Statement           stmt                = null;
    private CallableStatement   cs                  = null;

	public FunctionsEPCS_PostPago(String ParametersFile)
	{
		super(ParametersFile);  			
		inicializar();
	}

	public FunctionsEPCS_PostPago(String ParametersFile, String id)
	{    	
		super(ParametersFile, id);
		inicializar();
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




	/*Parametros adicionales a Leer que no lee el FGVP*/
	private void inicializar(){
		hostIvrToDB = this.Params.GetValue("IvrToDBhost", "127.0.0.1");
		portIvrToDB = Integer.valueOf(this.Params.GetValue("IvrToDBport", "50080"));
	}


	public String ejecutarRest(String url, JSONObject JSONrequest, long timeout, boolean debug) {
		String respuesta = "";
		try {
			WebClient client = WebClient.create(url); 
			client = client.accept(MediaType.APPLICATION_JSON).type("application/json; charset=utf-8");
			HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
			conduit.getClient().setConnectionTimeout(timeout);
			conduit.getClient().setReceiveTimeout(timeout); 
			String  request = JSONrequest.toString();
			System.out.println("ejecutarRest:"+request);
			if(debug) {
				ClientConfiguration config = WebClient.getConfig(client);
				config.getInInterceptors().add(new LoggingInInterceptor());
				config.getOutInterceptors().add(new LoggingOutInterceptor());
			}
			Response r = client.post(request);

			respuesta = r.readEntity(String.class);
			client.close();
		}catch(Exception e) {
			e.printStackTrace();
			respuesta = "";
		}finally {
			JSONrequest = null;
		}
		return respuesta;
	}

	public XMLGregorianCalendar ObtenerFechaXML() {
		GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
		XMLGregorianCalendar xgcal = null;
		try {
			xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
		} catch (DatatypeConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return xgcal;
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

			String sysCode = Params.GetValue("PostPago_sysCode", "IVR");
			String enterpriseCode = Params.GetValue("PostPago_enterpriseCode","ENTEL-CHL");
			String countryCode = Params.GetValue("PostPago_countryCode","CHL");
			String channelName = Params.GetValue("PostPago_channelName", "IVR");
			String channelMode = Params.GetValue("PostPago_channelMode","NO PRESENCIAL"); 
			String eventID = Params.GetValue("PostPago_eventID","0078"); 
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

	public String ValidateRequestProductOrder(String movil,String ICCID,String idLlamada, String orderType, String area, String subArea,String processID, String SourceID){
		String wsName="ValidateRequestProductOrder";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject(); 
		JSONObject CustomerOrder = new JSONObject();
		JSONObject CustomerAccount = new JSONObject();
		JSONObject Product = new JSONObject();
		JSONObject ProductAccount = new JSONObject();
		JSONObject MSISDN = new JSONObject();
		JSONObject jsICCID = new JSONObject();
		JSONObject body = new JSONObject();
		try {
			jsICCID.put("ICCID", ICCID);
			MSISDN.put("SN", movil);
			ProductAccount.put("ICCID", jsICCID);
			ProductAccount.put("MSISDN", MSISDN);
			CustomerOrder.put("area", area);
			CustomerOrder.put("orderType", orderType);
			CustomerOrder.put("subArea", subArea);

			CustomerAccount.put("CustomerOrder", CustomerOrder);
			Product.put("CustomerAccount", CustomerAccount);
			Product.put("ProductAccount", ProductAccount);

			body.put("Product", Product);

			JSONObject header = crearHeader(wsName, idLlamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally {
			MSISDN= null;
			request = null; 
			CustomerOrder = null;
			CustomerAccount = null;
			Product = null;
			ProductAccount = null; 
		}

		return resp;
	}

	public String GetBillingAccountCoexist(JSONObject individualIdentification,String IDllamada, String processID, String SourceID){
		String wsName="GetBillingAccountCoexist";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = "";
		JSONObject request = new JSONObject();
		try {	 
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", individualIdentification);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout,debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			individualIdentification = null;
		}
		return resp;
	}

	public String GetCustomerAccount(String movil,JSONObject individualIdentification,String IDllamada, String processID, String SourceID){
		String wsName="GetCustomerAccount";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject CustomerAccountAsset = new JSONObject();
		JSONObject Asset = new JSONObject();
		JSONObject MSISDN = new JSONObject();
		JSONObject Filter = new JSONObject();
		JSONObject Body = new JSONObject();

		try {	 
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			if(movil != null) {
				MSISDN.put("SN", movil);
				Asset.put("MSISDN", MSISDN);
				CustomerAccountAsset.put("Asset", Asset); 
				Body.put("CustomerAccount-Asset", CustomerAccountAsset);
			}
			if(individualIdentification!=null) {
				JSONObject custIdentification = new JSONObject();
				JSONObject Individual = new JSONObject();
				Individual.put("IndividualIdentification",individualIdentification);
				custIdentification.put("Individual", Individual);
				Body.put("CustomerAccount-Identification", custIdentification);
			}
			request.put("RequestHeader", header);
			request.put("Body", Body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null; 
			CustomerAccountAsset = null; 
			Asset = null; 
			MSISDN = null; 
			Filter = null; 
			Body = null; 
		}
		return resp;
	}
	//GetCustomerAccountBalance

	public String GetCustomerAccountBalance(String movil, String externalId, String IDllamada, String processID, String SourceID){
		String wsName = "GetCustomerAccountBalance";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject CustomerAccount = new JSONObject();
		JSONObject BusinessInteraction = new JSONObject();

		JSONObject Asset = new JSONObject();
		JSONObject MSISDN = new JSONObject();

		JSONObject Body = new JSONObject();

		try {	 
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);

			MSISDN.put("SN", movil);
			Asset.put("MSISDN", MSISDN);

			BusinessInteraction.put("interactionExternalID", externalId);

			CustomerAccount.put("BusinessInteraction", BusinessInteraction);
			CustomerAccount.put("Asset", Asset);

			Body.put("CustomerAccount", CustomerAccount);

			request.put("RequestHeader", header);
			request.put("Body", Body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null; 
			CustomerAccount = null; 
			Asset = null; 
			MSISDN = null;
			Body = null; 
		}
		return resp;
	}
	public String GetCustomerAccountBalanceAndCharge(String movil, String externalId, String IDllamada, String processID, String SourceID){
		String wsName = "GetCustomerAccountBalanceAndCharge";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject CustomerAccount = new JSONObject();
		JSONObject BusinessInteraction = new JSONObject();

		JSONObject Asset = new JSONObject();
		JSONObject MSISDN = new JSONObject();

		JSONObject Body = new JSONObject();

		try {	 
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);

			MSISDN.put("SN", movil);
			Asset.put("MSISDN", MSISDN);

			BusinessInteraction.put("interactionExternalID", externalId);

			CustomerAccount.put("BusinessInteraction", BusinessInteraction);
			CustomerAccount.put("Asset", Asset);

			Body.put("CustomerAccount", CustomerAccount);

			request.put("RequestHeader", header);
			request.put("Body", Body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null; 
			CustomerAccount = null; 
			Asset = null; 
			MSISDN = null;
			Body = null; 
		}
		return resp;
	}
	public String getUsageThresholdCounterREST(String movil, String ProductID, String IDllamada, String processID, String SourceID){
		String wsName = "GetUsageThresholdCounter";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject CustomerAccount = new JSONObject();

		JSONObject Asset = new JSONObject();
		JSONObject MSISDN = new JSONObject();
		JSONObject Product = new JSONObject();

		JSONObject Body = new JSONObject();

		try {	 
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			
			if(!ProductID.equals("")){
				Product.put("ID", ProductID);
				Body.put("Product", Product);
			}

			MSISDN.put("SN", movil);
			Asset.put("MSISDN", MSISDN);
			CustomerAccount.put("Asset", Asset);
			Body.put("CustomerAccount", CustomerAccount);

			request.put("RequestHeader", header);
			request.put("Body", Body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null; 
			CustomerAccount = null; 
			Asset = null; 
			MSISDN = null;
			Body = null; 
		}
		return resp;
	}

	public String CreateServiceRequest(JSONObject individualIdentification,String IDllamada, String processID, String SourceID, String area, String sbarea, String type, String state, String stateReason,String reason,JSONObject Technical){
		String wsName="CreateServiceRequest";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject Servicerequest = new JSONObject(); 
		JSONObject body = new JSONObject();

		try {	 

			/**"ServiceRequest": { 
		        "area": "Activaci�n de Linea",
		        "chanelIn": "IVR",
		        "subArea": "M�vil Postpago",
		        "type": "Orden",  
		        "IndividualIdentification": {
		          "number": "9371912-3",
		          "type": "RUT"
		        }
	      	}**/
			Servicerequest.put("area", area);
			Servicerequest.put("chanelIn", "IVR");
			Servicerequest.put("subArea", sbarea);
			Servicerequest.put("type", type);  
			Servicerequest.put("state", state);
			if(reason != null) {
				Servicerequest.put("reason", reason);
			}
			Servicerequest.put("stateReason", stateReason);
			Servicerequest.put("IndividualIdentification", individualIdentification);
			if(Technical != null) {
 				Servicerequest.put("Technical", Technical);
			}
			body.put("ServiceRequest", Servicerequest);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			Servicerequest = null;
			individualIdentification = null;
			body = null;
		}
		return resp;
	}
	
	public String CreateActivity(String IDllamada, String processID, String SourceID, String comment, String createdDate, String  description, String result, String status, String type, String externalID){
		String wsName="CreateActivity";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject Servicerequest = new JSONObject(); 
		JSONObject body = new JSONObject();
		JSONObject Activity = new JSONObject();

		try {	 

			/**"Body": {
		      "Activity": {
		         "comment": "O_R_PR_PONR",
		         "createdDate": "2018-05-02T12:17:21-03:00",
		         "description": "O_R_PR_PONR",
		         "result": "Cancelado",
		         "status": "Abierto",
		         "type": "Administración",
		         "ServiceRequest": {
		            "externalID": "250047150"
		         }
		      }
		   }**/
			Activity.put("comment", comment);
			Activity.put("createdDate", createdDate);
			Activity.put("description", description);
			Activity.put("result", result);
			Activity.put("status", status);
			Activity.put("type", type);
			Servicerequest.put("ID", externalID);  
			Activity.put("ServiceRequest", Servicerequest);
			body.put("Activity", Activity);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			Servicerequest = null;
			Activity = null;
			body = null;
		}
		return resp;
	}

	public String GetPartyIdentification(String IDllamada, String processID, String SourceID, String rut, String serialNumber){
		String wsName="GetPartyIdentification";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject IndividualIdentification = new JSONObject(); 
		JSONObject body = new JSONObject(); 
		try {	 
			IndividualIdentification.put("number", rut);
			IndividualIdentification.put("serialNumber", serialNumber);

			body.put("IndividualIdentification", IndividualIdentification);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout,debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			IndividualIdentification = null;
			body = null;
		}
		return resp;
	}

	public String GetAvailableMSISDN(String IDllamada, String processID, String SourceID){
		String wsName="GetAvailableMSISDN";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String searchLimit = Params.GetValue("searchLimit_"+wsName, "3");
		String plmnCode = Params.GetValue("plmnCode_"+wsName, "CHLMV");
		String status = Params.GetValue("status_"+wsName, "r");
		String submarketID = Params.GetValue("status_submarketID", "GSM");


		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject Asset = new JSONObject(); 
		JSONObject Filter = new JSONObject(); 
		JSONObject Operator = new JSONObject();
		JSONObject body = new JSONObject(); 
		try {	 

			Asset.put("status", status);
			Filter.put("searchLimit", searchLimit);
			Operator.put("plmnCode", plmnCode);
			Operator.put("submarketID", submarketID);
			Asset.put("Filter", Filter);
			Asset.put("Operator", Operator);
			body.put("Asset", Asset);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			Asset = null; 
			Filter = null; 
			Operator = null;
			body = null;
		}
		return resp;
	}

	public String SubmitProductOrder(String shoppingCartID,String IDllamada, String processID, String SourceID){
		String wsName="SubmitProductOrder";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject CustomerOrder = new JSONObject();
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject(); 
		try {	 
			CustomerOrder.put("shoppingCartID", shoppingCartID); 
			body.put("CustomerOrder", CustomerOrder);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout,debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;   
			CustomerOrder = null;
			body = null;
		}
		return resp;
	}

	public String CreateCustomerAccount(String accountType,String customerType,String legalEntityType,JSONObject IndividualIdentification, JSONObject GeographicalAddress,JSONObject IndividualName,JSONObject BillingAccount,String IDllamada, String processID, String SourceID){
		String wsName="CreateCustomerAccount";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = ""; 		
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject(); 
		JSONObject CustomerAccount = new JSONObject();

		try {	  
			CustomerAccount.put("accountType", accountType);
			CustomerAccount.put("customerType", customerType);
			CustomerAccount.put("legalEntityType", legalEntityType);
			CustomerAccount.put("BillingAccount", BillingAccount);
			if(IndividualIdentification!= null) {
				CustomerAccount.put("IndividualIdentification", IndividualIdentification);
			}
			if(IndividualName!= null) {
				CustomerAccount.put("IndividualName", IndividualName);
			}
			if(GeographicalAddress!= null) {
				CustomerAccount.put("GeographicalAddress", GeographicalAddress);
			}
			body.put("CustomerAccount", CustomerAccount);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null;
			IndividualName = null;
			GeographicalAddress = null;
			IndividualIdentification = null;
			CustomerAccount = null;
		}
		return resp;
	}

	public String UpdateCustomerAccount(String CustomerAccountID,JSONObject IndividualIdentification, JSONObject GeographicalAddress,JSONObject IndividualName,JSONObject BillingAccount,String accountType, String customerType,String IDllamada, String processID, String SourceID){
		String wsName="UpdateCustomerAccount";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = ""; 
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject(); 
		JSONObject CustomerAccount = new JSONObject();

		try {	 

			CustomerAccount.put("ID",CustomerAccountID);

			//if(accountType != "" && customerType!= "" &&  legalEntityType!= "") {
			CustomerAccount.put("accountType", accountType);
			CustomerAccount.put("customerType", customerType);
			//	CustomerAccount.put("legalEntityType", legalEntityType);
			//}
			if(IndividualIdentification!= null) {
				CustomerAccount.put("IndividualIdentification", IndividualIdentification);
			}
			if(IndividualName!= null) {
				CustomerAccount.put("IndividualName", IndividualName);
			}
			if(GeographicalAddress!= null) {
				CustomerAccount.put("GeographicalAddress", GeographicalAddress);
			}			

			if(BillingAccount != null) {
				CustomerAccount.put("BillingAccount", BillingAccount);
			}

			body.put("CustomerAccount", CustomerAccount);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null;
			IndividualName = null;
			GeographicalAddress = null;
			IndividualIdentification = null;
			CustomerAccount = null;
		}
		return resp;
	}

	public String CreateProductOrder(String movil,JSONObject Account,String CustomerAccountID,JSONObject IndividualIdentification,JSONObject ServiceRequest,String area,String mode,String orderType,String subArea,String requester,JSONObject IndividualName,JSONObject GeographicAddress,String IDllamada,String processID,String sourceID){
		String wsName="CreateProductOrder";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));
		String areaActivacion = Params.GetValue("areaActivacion_"+wsName, "Activación de Línea");
		

		String resp = ""; 
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject(); 
		JSONObject CustomerOrder = new JSONObject();
		JSONObject CustomerAccount = new JSONObject();
		JSONObject SalesChannel = new JSONObject();
		JSONObject RelatedParty = new JSONObject();
		JSONObject RelatedEntity = new JSONObject(); 
		JSONObject Asset = new JSONObject();
		JSONObject MSISDN = new JSONObject();
		try {	
			if(!movil.equals("") && movil!= null) {
				MSISDN.put("SN", movil);
				Asset.put("MSISDN", MSISDN);
				CustomerOrder.put("Asset", Asset);
			}

			if(area.equalsIgnoreCase(areaActivacion)){
				JSONObject CustomerCreditCheck = new JSONObject();
				JSONObject entity = new JSONObject();
				JSONObject equipmentInstallmentEvaluation = new JSONObject();
				JSONObject productEvaluation = new JSONObject();
				
				entity.put("creditCheckDescription", "Credit Check Desc");
				entity.put("creditCheckResultCode", "Pass");
				
				equipmentInstallmentEvaluation.put("maxQuantityEquipmentsWithInstallments", "5");
				equipmentInstallmentEvaluation.put("maxQuantityPaymentsForEquipmentsWithInstallments", "3");
				
				entity.put("equipmentInstallmentEvaluation", equipmentInstallmentEvaluation);
				
				productEvaluation.put("advancedBilling", "0");
				productEvaluation.put("maxAmountInsurancePOs", "0");
				productEvaluation.put("maxFixedRecurringPayment", "40000");
				productEvaluation.put("maxQuantityAdditionalPOs", "0");
				productEvaluation.put("maxQuantityBasicPOs", "10");
				productEvaluation.put("productFamily", "Mobile");
				
				entity.put("productEvaluation", productEvaluation);
				
				CustomerCreditCheck.put("entity", entity);
				
				RelatedEntity.put("CustomerCreditCheck",CustomerCreditCheck);
				

			}
			
			CustomerOrder.put("area", area); 
			CustomerOrder.put("channel", "IVR");
			CustomerOrder.put("createdBy", CustomerAccountID);
			CustomerOrder.put("createdDate", ObtenerFechaXML()); 
			CustomerOrder.put("mode", mode);
			CustomerOrder.put("orderType", orderType);
			CustomerOrder.put("owner", CustomerAccountID); 
			CustomerOrder.put("subArea", subArea);
			CustomerOrder.put("requestID", IDllamada);
			CustomerOrder.put("requester", requester);
			SalesChannel.put("ID", CustomerAccountID);
			SalesChannel.put("createBy", "AutomaticoEntel");
			SalesChannel.put("orderCommercialChannel", "IVR");
			CustomerAccount.put("ID", CustomerAccountID);
			if(IndividualName != null) {
				JSONObject Individual = new JSONObject();
				Individual.put("IndividualName", IndividualName);
				CustomerAccount.put("Individual", Individual);
			}
			
			RelatedParty.put("CustomerAccount", CustomerAccount);
			RelatedParty.put("IndividualIdentification", IndividualIdentification); 
			RelatedEntity.put("Account", Account);
			RelatedEntity.put("SalesChannel", SalesChannel);
			
			if(GeographicAddress != null) {
				RelatedEntity.put("GeographicAddress",GeographicAddress);
			}
			CustomerOrder.put("ServiceRequest", ServiceRequest);
			CustomerOrder.put("RelatedParty", RelatedParty);
			CustomerOrder.put("RelatedEntity", RelatedEntity);
			
			body.put("CustomerOrder", CustomerOrder);
			JSONObject header = crearHeader(wsName, IDllamada, processID, sourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null;
			ServiceRequest= null;
			RelatedParty= null;
			RelatedEntity= null;
			Asset= null;
			CustomerOrder=null;
		}
		return resp;
	}

	public String GetServiceRequest(JSONObject IndividualIdentification,String area,String subarea,String IDllamada, String processID, String SourceID){
		String wsName="GetServiceRequest";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = ""; 
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject(); 
		JSONObject ServiceRequest = new JSONObject();

		try {
			ServiceRequest.put("IndividualIdentification", IndividualIdentification); 
			if(area != null && !area.equals("")) {
				ServiceRequest.put("area", area); 
			}
			if(subarea != null && !subarea.equals("")) {
				ServiceRequest.put("subArea", subarea); 
			}
			body.put("ServiceRequest", ServiceRequest);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout,debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null;
			ServiceRequest= null;

		}
		return resp;
	}

	public String CreateProductOrderItem(String PO_ID,String CustomerAccountID, String shoppingCartID, String action,JSONArray Resource,String biType,String roleRelated, String IDllamada, String processID, String SourceID){
		String wsName="CreateProductOrderItem";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = ""; 
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject(); 
		JSONObject CustomerOrderItem = new JSONObject();
		JSONObject CustomerOrder = new JSONObject();
		JSONObject RelatedParty = new JSONObject(); 
		JSONObject CustomerAccount = new JSONObject();
		JSONObject ProductOffering = new JSONObject();
		JSONObject RelatedItem = new JSONObject();
		JSONObject Quantity = new JSONObject();
		try {
			Quantity.put("amount", "1");
			ProductOffering.put("ID", PO_ID);
			ProductOffering.put("quantity", Quantity);
			CustomerOrderItem.put("ProductOffering", ProductOffering);
			CustomerOrderItem.put("action", action); 
			CustomerOrderItem.put("createBy", CustomerAccountID);
			CustomerOrderItem.put("requestID", IDllamada);
			CustomerOrderItem.put("biType", biType);
			CustomerOrder.put("shoppingCartID", shoppingCartID);
			CustomerAccount.put("ID", CustomerAccountID);
			RelatedParty.put("CustomerAccount", CustomerAccount);
			if(roleRelated != null) {
				RelatedItem.put("role", roleRelated);
			}
			CustomerOrderItem.put("CustomerOrder", CustomerOrder);
			if(Resource != null) {
				CustomerOrderItem.put("Resource", Resource);
			}
			CustomerOrderItem.put("RelatedParty", RelatedParty); ;
			body.put("CustomerOrderItem", CustomerOrderItem);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null;
			CustomerOrderItem= null;
			RelatedParty= null;
			CustomerOrder= null;
			Resource= null; 
		}
		return resp;
	}

	public String UpdateProductOrderItem(String IDllamada, String processID, String SourceID, String shoppingCartID,String action, String CustomerOrderItemID, JSONArray Resources, JSONObject ProductOffering){
		String wsName="UpdateProductOrderItem";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = ""; 
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject(); 
		JSONObject CustomerOrderItem = new JSONObject();
		JSONObject CustomerOrder = new JSONObject(); 
		String amount=null;
		String createBy =null;
		try {

			/**
			 *  "Body": {
					  "CustomerOrderItem": {
						"ID": "2627325016",
						"action": "Modify", 
						"CustomerOrder": { "shoppingCartID": "250045028" },
						 "Resource":[  
						  {  
							 "resourceCharacteristics":[  
								{  
								   "name":"resourceNumber",
								   "value":"56973827095"
								}
							 ],
							 "resourceSpecification":"LRS_MSISDN"
						  },
						  {  
							 "resourceCharacteristics":[  
								{  
								   "name":"serialNumber",
								   "value":"89560100000878875935"
								}
							 ],
							 "resourceSpecification":"PRS_SIM"
						  }
						]
					  }
			    }
			 * 
			 * **/
			CustomerOrderItem.put("amount", amount);
			CustomerOrderItem.put("createBy", createBy);
			CustomerOrderItem.put("ID", CustomerOrderItemID);
			if(action != null) {
				CustomerOrderItem.put("action", action);
			}
			CustomerOrder.put("shoppingCartID", shoppingCartID);
			CustomerOrderItem.put("CustomerOrder",CustomerOrder);
			if(Resources != null) {
				CustomerOrderItem.put("Resource",Resources);
			}

			if(ProductOffering != null) {
				CustomerOrderItem.put("ProductOffering",ProductOffering);
			}

			body.put("CustomerOrderItem", CustomerOrderItem);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null;
			CustomerOrderItem= null; 
			CustomerOrder= null; 
		}
		return resp;
	}


	public String UpdateProductOrder(String shoppingCartID, String status, String IDllamada, String processID, String SourceID){
		String wsName="UpdateProductOrder";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = ""; 
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject();  
		JSONObject CustomerOrder = new JSONObject(); 
		try { 
			CustomerOrder.put("shoppingCartID", shoppingCartID);
			CustomerOrder.put("state", status);
			body.put("CustomerOrder", CustomerOrder);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null; 
			CustomerOrder= null; 
		}
		return resp;
	}
	public String GetProductOrder(String shoppingCartID, String IDllamada, String processID, String SourceID){
		String wsName="GetProductOrder";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = ""; 
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject();  
		JSONObject CustomerOrder = new JSONObject(); 
		try { 
			CustomerOrder.put("shoppingCartID", shoppingCartID);
			body.put("CustomerOrder", CustomerOrder);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null; 
			CustomerOrder= null; 
		}
		return resp;
	}



	public String ValidateCustomerBlackList(JSONObject IndividualIdentification, String IDllamada, String processID, String SourceID){
		String wsName="ValidateCustomerBlackList";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = ""; 
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject();  
		JSONObject CustomerAccount = new JSONObject(); 
		try {
			CustomerAccount.put("IndividualIdentification", IndividualIdentification); 
			body.put("CustomerAccount", CustomerAccount);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null;
			CustomerAccount= null;  
		}
		return resp;
	}

	public String GetContract(String movil,String IDllamada, String processID, String SourceID){
		String wsName="GetContract";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject MSISDN = new JSONObject(); 
		JSONObject Session = new JSONObject();  
		JSONObject body = new JSONObject(); 
		try {	 

			Session.put("key", "BU_ID");
			Session.put("value", "2");
			MSISDN.put("SN", movil);
			MSISDN.put("Session", Session); 
			body.put("MSISDN", MSISDN);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			MSISDN = null; 
			Session = null; 
			body = null;
		}
		return resp;
	}

	public String GetCustomerPhysicalDevice(String movil,String IDllamada, String processID, String SourceID){
		String wsName="GetCustomerPhysicalDevice";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject MSISDN = new JSONObject(); 
		JSONObject Asset = new JSONObject();  
		JSONObject body = new JSONObject(); 
		try {	 

			MSISDN.put("SN", movil); 
			Asset.put("MSISDN", MSISDN);
			body.put("Asset", Asset);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			MSISDN = null; 
			Asset = null; 
			body = null;
		}
		return resp;
	}

	public String PublishPhysicalResourceNotification(String imei,String movil,String resourceType,JSONObject PhysicalResourceSpec,String status,String reason,String modality, String InIMSI,String system, String IDllamada, String processID, String SourceID){
		String wsName="PublishPhysicalResourceNotification";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject ResourceControl = new JSONObject(); 
		JSONObject Resource = new JSONObject(); 
		JSONObject Asset = new JSONObject();
		JSONObject MSISDN = new JSONObject();
		JSONObject body = new JSONObject(); 
		JSONObject IMSI = new JSONObject(); 
		try {	 
			ResourceControl.put("operatorNofiticationDate", ObtenerFechaXML());
			ResourceControl.put("lostDate", ObtenerFechaXML());
			ResourceControl.put("complaintDate", ObtenerFechaXML());
			ResourceControl.put("modality", modality);
			ResourceControl.put("operatorID", "220");
			ResourceControl.put("reason", reason);
			ResourceControl.put("requestDate", ObtenerFechaXML());
			ResourceControl.put("status", status);
			ResourceControl.put("system", system);

			MSISDN.put("SN", movil);
			Asset.put("MSISDN", MSISDN);
			IMSI.put("SN", InIMSI);
			Asset.put("IMSI", IMSI);
			Resource.put("imei", imei);
			Resource.put("resourceType", resourceType);
			Resource.put("Asset", Asset);
			Resource.put("PhysicalResourceSpec", PhysicalResourceSpec);
			ResourceControl.put("Resource", Resource);
			body.put("ResourceControl", ResourceControl);

			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			MSISDN = null; 
			Asset = null; 
			PhysicalResourceSpec = null;
			body = null;
			ResourceControl = null;
			Resource = null;
		}
		return resp;
	}

	public String ValidateResourceBlackList(String IMEI,String movil,String IDllamada, String processID, String SourceID){
		String wsName="ValidateResourceBlackList";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject PhysicalResource = new JSONObject(); 
		JSONObject Asset = new JSONObject(); 
		JSONObject body = new JSONObject(); 
		JSONObject MSISDN = new JSONObject(); 
		try {	 	  
			PhysicalResource.put("imei", IMEI);
			PhysicalResource.put("Asset", Asset);
			MSISDN.put("SN", movil);
			Asset.put("MSISDN", MSISDN);
			body.put("PhysicalResource", PhysicalResource);

			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			MSISDN = null; 
			Asset = null; 
			PhysicalResource = null;
			body = null; 
		}
		return resp;
	}

	public String CreateCustomerAuthenticationKey(String msisdn, String imei,String password,String IDllamada, String processID, String SourceID){
		String wsName="CreateCustomerAuthenticationKey";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		int maximumAttempt = Integer.parseInt(Params.GetValue(wsName+"_maximumAttempt", "3"));
		
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject Customer = new JSONObject(); 
		JSONObject MSISDN = new JSONObject();  
		JSONObject AuthenticationEntity = new JSONObject(); 
		JSONObject Mobile = new JSONObject();  
		JSONObject body = new JSONObject(); 
		try {	 

			MSISDN.put("SN", msisdn);
			Customer.put("maximumAttempt",maximumAttempt);
			Customer.put("MSISDN", MSISDN);
			AuthenticationEntity.put("password", password);
			Customer.put("AuthenticationEntity", AuthenticationEntity);
			Mobile.put("EMEI", imei);
			Customer.put("Mobile", Mobile); 
			body.put("Customer", Customer);

			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			Mobile = null; 
			Customer = null; 
			body = null;
			MSISDN = null;
			AuthenticationEntity = null;
		}
		return resp;
	}

	public String UpdateServiceRequest(String ServiceRequestID,String ShoppingCartId,String ServiceRequestState,String stateReason,String IDllamada, String processID, String SourceID){
		String wsName="UpdateServiceRequest";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = ""; 
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject(); 
		JSONObject ServiceRequest = new JSONObject();
		JSONObject CustomerOrder = new JSONObject();
		try {
			ServiceRequest.put("ID", ServiceRequestID);
			ServiceRequest.put("stateReason", stateReason);
			if(ServiceRequestState != null && !ServiceRequestState.equals("")) {
				ServiceRequest.put("state", ServiceRequestState);
			}
			if(ShoppingCartId != null && !ShoppingCartId.equals("")) {
				CustomerOrder.put("ID", ShoppingCartId);
				ServiceRequest.put("CustomerOrder",CustomerOrder);
			}
			body.put("ServiceRequest", ServiceRequest);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null;
			ServiceRequest= null;

		}
		return resp;
	}


	public String GetCustomerAuthenticationKey(String imei,String clave,String MSISDN,String IDllamada, String processID, String SourceID){
		String wsName="GetCustomerAuthenticationKey";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject Customer = new JSONObject(); 
		JSONObject Mobile = new JSONObject();  
		JSONObject body = new JSONObject(); 
		try {	 

			Mobile.put("Imei", imei);
		//	Mobile.put("MSISDN", MSISDN);
			Mobile.put("Clave", clave);
			Customer.put("Mobile", Mobile);
			body.put("Customer", Customer);

			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			Mobile = null; 
			Customer = null; 
			body = null;
		}
		return resp;
	}


	public String GetAvailableProductOffer(String MarketSegmentID,String specificationSubtype,String specificationType,String Family, String subFamily, String movil,String IDllamada, String processID, String SourceID, String prePayment, String maxAmountAllowed,String segmento,String subCategory, String Plan){
		String wsName="GetAvailableProductOffer";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject ProductOffering = new JSONObject(); 
		JSONObject CustomerAccount = new JSONObject();  
		JSONObject Asset = new JSONObject(); 
		JSONObject MSISDN = new JSONObject();  
		JSONObject SalesChannel = new JSONObject();  
		JSONObject MarketSegment = new JSONObject();
		JSONObject ProductCategory = new JSONObject(); 
		JSONObject body = new JSONObject(); 
		try {	 
			if(subCategory!=null && !subCategory.equals("")) {
				ProductCategory.put("subCategory", subCategory);
				ProductOffering.put("ProductCategory",ProductCategory);
			}
			
			if(Plan!=null && !Plan.equals("") && Plan.length()>5) {
				ProductOffering.put("ID",Plan);
			}
			
			MarketSegment.put("ID", MarketSegmentID);
			/*
			 * Modifcacion: Gabriel Santis Villalón
			 * Fecha: 2019-02-26
			 * Descripcion: Se requiere que estos campos puedan ser omitidos al crear request
			 *              para buscar bolsas disponibles segun family plan*/
			if(specificationSubtype!=null && !specificationSubtype.equals("")) {
				ProductOffering.put("specificationSubtype", specificationSubtype);
			}     
			if(specificationType!=null && !specificationType.equals("")) {
				ProductOffering.put("specificationType", specificationType);
			}

//			ProductOffering.put("specificationSubtype", specificationSubtype);
//			ProductOffering.put("specificationType", specificationType);
			/**/

			/*
			 * Modifcacion: Gabriel Santis Villalón
			 * Fecha: 2019-05-09
			 * Descripcion: Se requiere que campos family y subFamily pueda ser omitido al crear request
			 *              para buscar detalle de bolsa escogida por usuario en compra con canje
			 *              Ptos ZE*/	
			
			if(subFamily!=null && !subFamily.equals("")) {
				ProductOffering.put("subFamily", subFamily);
			}		
			if(Family!=null && !Family.equals("")) {
				ProductOffering.put("family", Family);
			}		
			//ProductOffering.put("family", Family);
			//ProductOffering.put("subFamily", subFamily);
			/**/

			ProductOffering.put("maxAmountAllowed",maxAmountAllowed);
			ProductOffering.put("prePayment",prePayment);
			MSISDN.put("SN", movil);
			Asset.put("MSISDN", MSISDN);
			CustomerAccount.put("Asset", Asset);
			CustomerAccount.put("revenueClass", segmento);
			CustomerAccount.put("MarketSegment",MarketSegment);
			SalesChannel.put("ID", "IVR");
			ProductOffering.put("SalesChannel", SalesChannel);
			ProductOffering.put("CustomerAccount", CustomerAccount);
			body.put("ProductOffering", ProductOffering);

			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			ProductOffering = null; 
			CustomerAccount = null;  
			Asset = null; 
			MSISDN = null;  
			SalesChannel = null;  
			body = null;
		}
		return resp;
	}

	public String PublishActivationRequest(String movil,String ICCID,String IDllamada, String processID, String SourceID){
		String wsName="PublishActivationRequest";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject MSISDN = new JSONObject(); 
		JSONObject SIMCard = new JSONObject(); 
		JSONObject body = new JSONObject();
		JSONObject Channel = new JSONObject();

		try {	 
			
			SIMCard.put("ICCID", ICCID);
			Channel.put("name", "IVR"); 
			MSISDN.put("SN", movil);  
			body.put("MSISDN", MSISDN);
			body.put("Channel", Channel);
			body.put("SIMCard", SIMCard);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			Channel = null;
			MSISDN = null;
			body = null;
		}
		return resp;
	}

	public String GetLoyaltyBalance(JSONObject IndividualIdentification, String salesChannelID, String transactionNumber, String IDllamada, String processID, String SourceID){
		String wsName = "GetLoyaltyBalance";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject body = new JSONObject();
		JSONObject CustomerAccount = new JSONObject();
		JSONObject Individual = new JSONObject();
		JSONObject SalesChannel = new JSONObject();
		JSONObject LoyaltyTransaction = new JSONObject();

		try {	 

			LoyaltyTransaction.put("transactionNumber", transactionNumber);
			SalesChannel.put("ID", salesChannelID);
			SalesChannel.put("LoyaltyTransaction", LoyaltyTransaction);
			Individual.put("SalesChannel", SalesChannel);
			Individual.put("IndividualIdentification", IndividualIdentification); 
			CustomerAccount.put("Individual", Individual);
			body.put("CustomerAccount", CustomerAccount);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			CustomerAccount = null;
			Individual = null;
			SalesChannel = null;
			LoyaltyTransaction = null;
		}
		return resp;
	}
	public String GetLoyaltyProduct(String rut,String orderCommercialChannel,String type,String typeCode,String id, String IDllamada, String processID, String SourceID){
		String wsName = "GetLoyaltyProduct";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject body = new JSONObject();
		JSONObject LoyaltyProgramProduct = new JSONObject();
		JSONObject LoyaltyTransaction = new JSONObject();
		JSONObject LoyaltyProgramMember = new JSONObject();
		JSONObject Product = new JSONObject();
		JSONObject ProductCategory = new JSONObject();
		JSONObject SalesChannel = new JSONObject();
		try {	 
			LoyaltyProgramMember.put("ID", rut);
			SalesChannel.put("orderCommercialChannel", orderCommercialChannel);
			ProductCategory.put("id",id);
			Product.put("type", type);
			Product.put("typeCode", typeCode);
			Product.put("ProductCategory", ProductCategory);
			LoyaltyTransaction.put("LoyaltyProgramMember", LoyaltyProgramMember);
			LoyaltyTransaction.put("SalesChannel", SalesChannel);
			LoyaltyProgramProduct.put("LoyaltyTransaction", LoyaltyTransaction);
			LoyaltyProgramProduct.put("Product", Product);
			LoyaltyProgramProduct.put("activeFlag", true);
			body.put("LoyaltyProgramProduct", LoyaltyProgramProduct);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			LoyaltyProgramProduct = null;
			LoyaltyTransaction = null;
			LoyaltyProgramMember = null;
			Product = null;
			ProductCategory = null;
			SalesChannel = null;
		}
		return resp;
	}

	public String GetCustomerInfo(String movil,String IDllamada, String processID, String SourceID){
		String wsName = "GetCustomerInfo";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject body = new JSONObject();
		JSONObject CustomerAccount = new JSONObject();
		JSONObject Asset = new JSONObject();
		JSONObject MSISDN = new JSONObject(); 

		try {	 
			MSISDN.put("SN", movil);
			Asset.put("MSISDN", MSISDN);
			CustomerAccount.put("Asset", Asset); 
			body.put("CustomerAccount", CustomerAccount);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			CustomerAccount = null;
			Asset = null;
			MSISDN = null; 
		}
		return resp;
	}
	public String GetCustomerInfoRUT(JSONObject IndividualIdentification,String IDllamada, String processID, String SourceID){
		String wsName = "GetCustomerInfoRUT";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject body = new JSONObject();
		JSONObject CustomerAccount = new JSONObject();
		JSONObject Individual = new JSONObject();
		try {	 
			Individual.put("IndividualIdentification", IndividualIdentification);
			CustomerAccount.put("Individual", Individual); 
			body.put("CustomerAccount", CustomerAccount);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			CustomerAccount = null;
			Individual = null;
		}
		return resp;
	}
	public String GetAsset(String SN, String IDllamada, String processID, String SourceID, String filterBlockNumber, String filterName){
		String wsName = "GetAsset";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));
		
		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject body = new JSONObject();
		JSONObject Product = new JSONObject();
		JSONObject Filter = new JSONObject();
		JSONObject ProductAccount = new JSONObject();
		JSONObject MSISDN = new JSONObject(); 

		try {	 
			if(!filterBlockNumber.equalsIgnoreCase("")){
				Filter.put("blockNumber", filterBlockNumber);
			}
			if(!filterName.equalsIgnoreCase("")){
				Filter.put("name", filterName);
			}
			MSISDN.put("SN", SN);
			ProductAccount.put("MSISDN", MSISDN);
			Product.put("Filter", Filter);
			Product.put("ProductAccount", ProductAccount);
			body.put("Product", Product);
			
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			Product = null;
			Filter = null;
			ProductAccount = null; 
			MSISDN = null;
		}
		return resp;
	}


	public String CreateOneClickToOrder(String PO_ID, String action, String biType_Item, String area, String orderType, String subArea, String operationType, String biType, String planActual, String SN, String IDllamada, String processID, String SourceID, String bscsCustomerId, String PtosZEToBurn, String Product_name, String Product_value, String PO_ID_DISC, String classification, String CampaignID, String CampaignOfferID, String CampaignOfferSubType, String CampaignSource ){
		/****
		 * Modficacion: Gabriel Santis Villalon
		 * Fecha: 2019-02-21
		 * Descripcion: Se debe cambiar URL y version de WS. http://10.49.15.149:7010/ES/JSON/CreateOneClickToOrder/v2
		 *              Se modifica request para WS CreateOneClickToOrder/v2
		 *              Se agrega parametro "bscsCustomerId" tipo String, último parametro
		
		 * Url antes modificación:
			String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
					
		 * Url modificada:
			String url = Params.GetValue("URL_"+wsName, "http://10.49.15.149:7010/ES/JSON/"+wsName+"/v2");	

         ***
		 * Fecha: 2019-05-11
		 * Descripcion: Se agrega cualidad para generar 2 requests. 
		 *              Request para compra de bolsa con saldo (operationType = "CompraBolsa")
		 *              Request para compra de bolsa con ptos ZE (operationType = "BolsaPuntos")
		 *  
         ***
		 * Fecha: 2019-06-10
		 * Descripcion: Se agrega cualidad para generar:
		 *              Request para Cambio de Plan (operationType = "CambioPlan")	
		 *              y se agrega opción por defecto
         ***
		 * Fecha: 2019-11-20
		 * Descripcion: Se modifica request con cambios solicitados por integración:
		 *              - Datos para procesar campañas ofrecidas a cliente	
		 *              - 5 campos agregados solo para cambio de plan
						--- RecommendationOffer.MarketingCampaign.source
						--- RecommendationOffer.MarketingCampaign.MSISDN.SN
						--- RecommendationOffer.MarketingCampaign.ID (código de la campaña).
						--- RecommendationOffer.MarketingCampaign.offerID (código de la offerta).
						--- RecommendationOffer.MarketingCampaign.recomendationType (tipo de la oferta).
		**********/				
				
		String wsName = "CreateOneClickToOrder";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.15.149:7010/ES/JSON/"+wsName+"/v2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));
		
		String resp = "";
		
		JSONObject request = new JSONObject();
		JSONObject body = new JSONObject();
		
		JSONObject Asset = new JSONObject(); //->body
		JSONObject MSISDN = new JSONObject();//->Asset
		
		JSONObject CustomerOrder = new JSONObject();//->body
		JSONObject SalesChannel = new JSONObject(); //->CustomerOrder
		
		JSONArray ArrayCustomerOrderItem = new JSONArray(); //->body
		
		JSONObject CustomerOrderItem1 = new JSONObject();//->ArrayCustomerOrderItem
		JSONObject CustomerOrderItem2 = new JSONObject();//->ArrayCustomerOrderItem
		
		JSONObject ProductOffering1 = new JSONObject();//->CustomerOrderItem1			
		JSONObject ProductOffering2 = new JSONObject();//->CustomerOrderItem2
		JSONObject quantity = new JSONObject();	//->ProductOffering
		
		JSONObject Account = new JSONObject(); //->RelatedEntity
		JSONObject BillingAccount = new JSONObject(); //->Account
		JSONObject RelatedEntity = new JSONObject(); //->body
		
		JSONObject RelatedParty = new JSONObject(); //->body
		JSONObject CustomerAccount = new JSONObject(); //->RelatedParty
		JSONObject LoyaltyAccount = new JSONObject(); //->CustomerAccount
		JSONObject LoyaltyBalance = new JSONObject(); //->LoyaltyAccount
		JSONObject remainingPoints = new JSONObject(); //->LoyaltyBalance
		
		JSONObject Product = new JSONObject(); //->CustomerOrderItem
		JSONObject ProductSpecification = new JSONObject(); //->Product
		JSONObject ProductSpecCharacteristic = new JSONObject(); //->ProductSpecification

		//RecommendedOffer->MarketingCampaign ::: mod 2019-11-19
		JSONObject RecommendedOffer = new JSONObject(); //->CustomerOrder
		JSONObject MarketingCampaign = new JSONObject(); //->RecommendedOffer		
		
		try {	 
			Random rng = new Random();
			long timeStamp=System.currentTimeMillis()/1000;
			long dig = rng.nextInt(900)+99;
			String requestID = "0078"+timeStamp+dig;				
			
			MSISDN.put("SN", SN);
			Asset.put("ID",planActual);
			Asset.put("MSISDN", MSISDN);			

			CustomerOrder.put("area", area); 
			CustomerOrder.put("biType", biType);
			CustomerOrder.put("channel", "IVR");			
			CustomerOrder.put("mode", "NON_INTERACTIVE");
			CustomerOrder.put("orderType", orderType);
			CustomerOrder.put("requestID", requestID);
			CustomerOrder.put("subArea", subArea);
			CustomerOrder.put("operationType", operationType);
			
			SalesChannel.put("createBy", "AUTOMATICOENTEL");
			SalesChannel.put("orderCommercialChannel", "IVR");
			
			BillingAccount.put("bscsCustomerId", bscsCustomerId);
			Account.put("BillingAccount", BillingAccount);
			RelatedEntity.put("Account", Account);			
			
			CustomerOrder.put("SalesChannel", SalesChannel);
			CustomerOrder.put("RelatedEntity", RelatedEntity);
			
			remainingPoints.put("amount", PtosZEToBurn);
			LoyaltyBalance.put("remainingPoints", remainingPoints);
			LoyaltyAccount.put("LoyaltyBalance", LoyaltyBalance);
			CustomerAccount.put("LoyaltyAccount", LoyaltyAccount);
			RelatedParty.put("CustomerAccount", CustomerAccount);
			
			CustomerOrderItem1.put("action", action);
			CustomerOrderItem1.put("biType", biType_Item);
			
			quantity.put("amount", 1);
			
			ProductOffering1.put("ID", PO_ID);
			ProductOffering1.put("quantity", quantity);			
			
			CustomerOrderItem1.put("ProductOffering", ProductOffering1);	
			
			ProductSpecCharacteristic.put("name", Product_name);
			ProductSpecCharacteristic.put("value", Product_value);
			ProductSpecCharacteristic.put("classification", classification);
			ProductSpecification.put("ProductSpecCharacteristic", ProductSpecCharacteristic);
			Product.put("ProductSpecification", ProductSpecification);
			
			CustomerOrderItem2.put("action", action);
			CustomerOrderItem2.put("biType", biType_Item);
			
			ProductOffering2.put("ID", PO_ID_DISC);
			ProductOffering2.put("Product", Product);
			ProductOffering2.put("quantity", quantity);	
			CustomerOrderItem2.put("ProductOffering", ProductOffering2);

			ArrayCustomerOrderItem.put(CustomerOrderItem1);
			ArrayCustomerOrderItem.put(CustomerOrderItem2);

			//RecommendedOffer->MarketingCampaign ::: mod 2019-11-19 
			MarketingCampaign.put("ID", CampaignID);
			MarketingCampaign.put("offerId", CampaignOfferID);
			MarketingCampaign.put("recomendationType", CampaignOfferSubType);
			MarketingCampaign.put("source", CampaignSource);
			MarketingCampaign.put("MSISDN", MSISDN);	

			RecommendedOffer.put("MarketingCampaign", MarketingCampaign);
					
	
			switch (operationType){
				case "BolsaPuntos":					
					Debug("[FunctionsEPCS."+wsName+"] Request para BolsaPuntos ", "DEBUG");
					
					CustomerOrder.put("RelatedParty", RelatedParty);

					body.put("Asset", Asset);	
					body.put("CustomerOrder", CustomerOrder);
					body.put("CustomerOrderItem", ArrayCustomerOrderItem);							
					
				break;
				
				case "CompraBolsa":
					Debug("[FunctionsEPCS."+wsName+"] Request para CompraBolsa" , "DEBUG");
					
					body.put("Asset", Asset);	
					body.put("CustomerOrder", CustomerOrder);
					body.put("CustomerOrderItem", CustomerOrderItem1);	
					
				break;
				
				case "CambioPlan":
					Debug("[FunctionsEPCS."+wsName+"] Request para CambioPlan", "DEBUG");
					
					body.put("Asset", Asset);	
					CustomerOrder.put("RecommendedOffer", RecommendedOffer);
					body.put("CustomerOrder", CustomerOrder);
					body.put("CustomerOrderItem", CustomerOrderItem1);	
					
				break;				
				
	            default: 
	            	//usa estructura de case "CompraBolsa" como request por defecto
	            	Debug("[FunctionsEPCS."+wsName+"] Request por defecto/generico ", "DEBUG");
	            	
					body.put("Asset", Asset);	
					body.put("CustomerOrder", CustomerOrder);
					body.put("CustomerOrderItem", CustomerOrderItem1);					
			
			}
			
			//***
			
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			Asset = null;
			MSISDN = null;
			CustomerOrder = null;
			SalesChannel = null;
			RelatedEntity= null;
			//***
			RelatedParty = null;
			
			CustomerOrderItem1 = null;
			CustomerOrderItem2 = null;
			
			ProductOffering1 = null;
			ProductOffering2 = null;
			//***
			quantity = null;
		
		}
		return resp;
	}
	
	
	
	public String GetMarketingCampaign(JSONObject IndividualIdentification, String SN, String IDllamada, String processID, String SourceID){
		String wsName = "GetMarketingCampaign";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject body = new JSONObject();
		JSONObject MarketingCampaign = new JSONObject();
		JSONObject CustomerAccount = new JSONObject();
		JSONObject Asset = new JSONObject();
		JSONObject MSISDN = new JSONObject();
		JSONObject Individual = new JSONObject();

		try {	 


			
			
			Individual.put("IndividualIdentification", IndividualIdentification); 
			CustomerAccount.put("Individual", Individual);
			MSISDN.put("SN", SN);
			Asset.put("MSISDN",MSISDN);
			CustomerAccount.put("Asset", Asset);
			MarketingCampaign.put("CustomerAccount", CustomerAccount);
			
			body.put("MarketingCampaign", MarketingCampaign);
			
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			MarketingCampaign = null;
			CustomerAccount = null;
			Asset = null;
			MSISDN = null;
			Individual = null;
		}
		return resp;
	}	
	public String ValidateLoyaltyBurn(String transactionType, String amount, String SN_Destino, String memberNumber, String SN_Origen, String IDllamada, String processID, String SourceID){
		String wsName = "ValidateLoyaltyBurn";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject body = new JSONObject();
		JSONObject destinyMSISDN = new JSONObject();
		JSONObject originMSISDN = new JSONObject();
		JSONObject LoyaltyProgramMember = new JSONObject();
		JSONObject objAmount = new JSONObject();
		JSONObject Channel = new JSONObject();
		JSONObject LoyaltyTransaction = new JSONObject();

		try {	 

			
			objAmount.put("amount", amount);
			Channel.put("description", "IVR");
			originMSISDN.put("SN", SN_Origen);
			destinyMSISDN.put("SN", SN_Destino);
			LoyaltyProgramMember.put("memberNumber",memberNumber);
			LoyaltyTransaction.put("transactionType", transactionType);
			LoyaltyTransaction.put("amount", objAmount);
			LoyaltyTransaction.put("Channel", Channel);
			LoyaltyTransaction.put("originMSISDN", originMSISDN);
			LoyaltyTransaction.put("destinyMSISDN", destinyMSISDN);
			LoyaltyTransaction.put("LoyaltyProgramMember", LoyaltyProgramMember);
			
			body.put("LoyaltyTransaction", LoyaltyTransaction);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			destinyMSISDN = null;
			originMSISDN = null;
			LoyaltyProgramMember = null;
			objAmount = null;
			Channel = null;
			LoyaltyTransaction = null;
		}
		return resp;
	}public String CreateRequestPaymentLoyaltyBurn(String exchangeType, String transactionSubType, String transactionType, String amount_rec, String ID,String SN,String quantity_rec,String description,String rut, String IDllamada, String processID, String SourceID){
		String wsName = "CreateRequestPaymentLoyaltyBurn";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();
		JSONObject body = new JSONObject();
		JSONObject LoyaltyTransaction = new JSONObject();
		JSONObject CustomerPayment = new JSONObject();
		JSONObject amount = new JSONObject();
		JSONObject LoyaltyBalance = new JSONObject();
		JSONObject LoyaltyAccount = new JSONObject();
		JSONObject LoyaltyProgramProduct = new JSONObject();
		JSONObject LoyaltyProgramMember = new JSONObject();
		JSONObject originMSISDN = new JSONObject();
		JSONObject quantity = new JSONObject();
		JSONObject SalesChannel = new JSONObject();

		try {
			GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
			XMLGregorianCalendar requestDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			amount.put("amount", amount_rec);
			CustomerPayment.put("paymentDate", requestDate);
			CustomerPayment.put("amount", amount);
			LoyaltyProgramProduct.put("ID", ID);
			LoyaltyProgramMember.put("memberNumber", rut);
			LoyaltyAccount.put("LoyaltyProgramProduct", LoyaltyProgramProduct);
			LoyaltyAccount.put("LoyaltyProgramMember", LoyaltyProgramMember);
			LoyaltyBalance.put("LoyaltyAccount", LoyaltyAccount);
			LoyaltyTransaction.put("exchangeType", exchangeType);
			LoyaltyTransaction.put("transactionSubType", transactionSubType);
			LoyaltyTransaction.put("transactionType", transactionType);
			LoyaltyTransaction.put("CustomerPayment", CustomerPayment);
			LoyaltyTransaction.put("LoyaltyBalance",LoyaltyBalance);
			originMSISDN.put("SN", SN);
			LoyaltyTransaction.put("originMSISDN", originMSISDN);
			quantity.put("amount", quantity_rec);
			LoyaltyTransaction.put("quantity", quantity);
			SalesChannel.put("description", description);
			LoyaltyTransaction.put("SalesChannel", SalesChannel);
			
			body.put("LoyaltyTransaction", LoyaltyTransaction);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			LoyaltyTransaction = null;
			CustomerPayment = null;
			amount = null;
			LoyaltyBalance = null;
			LoyaltyAccount = null;
			LoyaltyProgramProduct = null;
			originMSISDN = null;
			quantity = null;
			SalesChannel = null;
		}
		return resp;
	}
	
// CreateLoyaltyBurn
	
	public String CreateLoyaltyBurn(String lppID,String originSerie, String destinySerie, String channelDescription,String pAmount, String paymentDate, String exchangeType, String transType,String transSubType, String IDllamada,String processID,String sourceID){
		String wsName="CreateLoyaltyBurn";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = ""; 
		JSONObject request = new JSONObject();  
		JSONObject body = new JSONObject(); 

		JSONObject LoyaltyTransaction = new JSONObject();
		JSONObject CustomerPayment = new JSONObject();
		JSONObject amount = new JSONObject();
		JSONObject destinyMSISDN = new JSONObject();
		JSONObject LoyaltyBalance = new JSONObject();
		JSONObject LoyaltyAccount = new JSONObject();
		JSONObject LoyaltyProgramProduct = new JSONObject();
		JSONObject originMSISDN = new JSONObject();
		JSONObject SalesChannel = new JSONObject();
		
		try {	
			LoyaltyProgramProduct.put("ID", lppID); 
			LoyaltyAccount.put("LoyaltyProgramProduct",LoyaltyProgramProduct);
			LoyaltyBalance.put("LoyaltyAccount", LoyaltyAccount);
			originMSISDN.put("SN", originSerie);
			SalesChannel.put("description", channelDescription);
			destinyMSISDN.put("SN", destinySerie);
			amount.put("amount", pAmount);
			CustomerPayment.put("paymentDate", paymentDate);
			CustomerPayment.put("amount", amount);
			LoyaltyTransaction.put("exchangeType", exchangeType);
			LoyaltyTransaction.put("transactionSubType", transSubType);
			LoyaltyTransaction.put("transactionType", transType);			
			LoyaltyTransaction.put("CustomerPayment", CustomerPayment);
			LoyaltyTransaction.put("destinyMSISDN", destinyMSISDN);
			LoyaltyTransaction.put("LoyaltyBalance", LoyaltyBalance);
			LoyaltyTransaction.put("originMSISDN", originMSISDN);
			LoyaltyTransaction.put("salesChannel", SalesChannel);
			body.put("LoyaltyTransaction", LoyaltyTransaction);
			
			JSONObject header = crearHeader(wsName, IDllamada, processID, sourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;    
			body = null;
			 LoyaltyTransaction =  null;
			 CustomerPayment = null;
			 amount = null;
			 destinyMSISDN = null;
			 LoyaltyBalance =  null;
			 LoyaltyAccount = null;
			 LoyaltyProgramProduct = null;
			 originMSISDN = null;
			 SalesChannel = null;
			}
		return resp;
	}
	
	// GetLoyaltyAccount
	
	public String GetLoyaltyAccount(String salesChannelID, String serieNumber, String IDllamada, String processID, String SourceID){
		String wsName = "GetLoyaltyAccount";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();

		JSONObject body = new JSONObject();
		JSONObject CustomerAccount = new JSONObject();
		JSONObject Asset = new JSONObject();
		JSONObject SalesChannel = new JSONObject();
		JSONObject MSISON = new JSONObject();

		try {	 

			MSISON.put("SN", serieNumber);
			Asset.put("MSISON", MSISON);
			SalesChannel.put("ID", salesChannelID);
			CustomerAccount.put("Asset", Asset);
			CustomerAccount.put("SalesChannel", SalesChannel);
			body.put("CustomerAccount-MSISON", CustomerAccount);
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
			request.put("RequestHeader", header);
			request.put("Body", body);

			Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
			resp = ejecutarRest(url,request,maxTimeout, debug);				
			Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;  
			body = null;
			CustomerAccount = null;
			MSISON = null;
			SalesChannel = null;
			Asset = null;
		}
		return resp;
	}

// createNotification (SMS)

public String CreateNotification(String externalID, String senderAlias, String originSystem, String receiver, String subject, String textMessage, String SourceID, String processID, String IDllamada)
{
	String wsName = "CreateNotification";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

	String resp = "";
	JSONObject request = new JSONObject();
    JSONObject body = new JSONObject();
    JSONObject Notification = new JSONObject();
	JSONArray ArrayParameter= new JSONArray();
	JSONArray ArrayMessage= new JSONArray();
	JSONObject Parameter = new JSONObject();
    JSONObject Message = new JSONObject();
	
    try {
    	 
			Message.put("deliveryMethod","CellPhone");
			Message.put("senderAlias",senderAlias);
			Message.put("receiver",receiver);
			Message.put("subject",subject);
			Message.put("textMessage",textMessage);
	
			Parameter.put("name", "requestId");
			Parameter.put("value", IDllamada);
			ArrayMessage.put(Message);
			ArrayParameter.put(Parameter);

			Notification.put("Message",ArrayMessage);            
			Notification.put("originSystem", originSystem);
            Notification.put("externalID", externalID);
			Notification.put("Parameter", ArrayParameter);            
			
		    body.put("Notification", Notification);

            JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		    request.put("RequestHeader", header);
		    request.put("Body", body);

		    Debug("[FunctionsEPCS_PostPago."+wsName+"] Request "+request, "DEBUG");
		    resp = ejecutarRest(url,request,maxTimeout, debug);				
		    Debug("[FunctionsEPCS_PostPago."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

        }catch(Exception e) {
		    e.printStackTrace();
		    Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}finally { 
			request = null;           
			body = null;              
			Notification = null;      
			Message = null;           
			Parameter = null;         
		
		}
		return resp;
    }
public String GetUsageDetails(String SN,String externalID,String searchLimit, String key, String value, String IDllamada, String processID, String SourceID){
	String wsName = "GetUsageDetails";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

	String resp = "";
	JSONObject request = new JSONObject();

	JSONObject body = new JSONObject();
	JSONObject AppliedCustomerBillingCharge = new JSONObject();
	JSONObject BillingAccount = new JSONObject();
	JSONObject Filter = new JSONObject();
	JSONObject Product = new JSONObject();
	JSONObject Session = new JSONObject();
	JSONObject Contract = new JSONObject();
	JSONObject MSISDN = new JSONObject();
	
	try {	 
		BillingAccount.put("externalID", externalID);
		Filter.put("searchLimit", searchLimit);
		Product.put("Contract",Contract);
		Contract.put("MSISDN", MSISDN);
		MSISDN.put("SN", SN);
		Session.put("key", key);
		Session.put("value", value);
		AppliedCustomerBillingCharge.put("BillingAccount", BillingAccount);
		AppliedCustomerBillingCharge.put("Filter", Filter);
		AppliedCustomerBillingCharge.put("Product", Product);
		
		body.put("AppliedCustomerBillingCharge", AppliedCustomerBillingCharge);
		
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
	}finally { 
		request = null;  
		body = null;
		AppliedCustomerBillingCharge = null;
		BillingAccount = null;
		Filter = null;
		MSISDN = null;
		Product = null;
		Contract = null;
		Session = null;
		key = null;
	}
	return resp;
}

public String GetAppliedCustomerBillingCharge (String Rut,String CustomerAccountID, String IDllamada, String processID, String SourceID){
	String wsName = "GetAppliedCustomerBillingCharge";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));
	
	String resp = "";
	JSONObject request = new JSONObject();
	
	JSONObject body = new JSONObject();
	JSONObject BillDocument = new JSONObject();
	JSONObject CustomerAccount = new JSONObject();
	JSONObject IndividualIdentification = new JSONObject();
	JSONObject IssuingCompany = new JSONObject();
	JSONObject OrganizationName = new JSONObject();
	
	try {	 
		IndividualIdentification.put("number", Rut);
		IndividualIdentification.put("type", "RUT");
		CustomerAccount.put("IndividualIdentification",IndividualIdentification);
		OrganizationName.put("shortName", "PCS");
		IssuingCompany.put("OrganizationName", OrganizationName);
		BillDocument.put("operation", "1");
		BillDocument.put("CustomerAccount", CustomerAccount);
		BillDocument.put("IssuingCompany", IssuingCompany);
		body.put("BillDocument", BillDocument);
		
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
	}finally { 
		request = null;  
		body = null;
		BillDocument = null;
		CustomerAccount = null;
		IndividualIdentification = null;
		IssuingCompany = null;
		OrganizationName = null;
	}

	return resp;
}

public String GetRoamingStatus(String msisdn, String IDllamada, String processID, String SourceID){
	String wsName = "GetRoamingStatus";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));
	
	String resp = "";
	JSONObject request = new JSONObject();
	
	JSONObject body = new JSONObject();
	JSONObject Roaming = new JSONObject(); 
	 
	
	try {	  
		Roaming.put("MSISDN", msisdn);
		body.put("Roaming", Roaming);
		
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
	}finally { 
		request = null;  
		body = null;
		Roaming = null;
	}

	return resp;
}



public String CreateReactivationOrder(String area,String subarea,String order,JSONObject IndividualIdentification,String externalID, String IDllamada, String processID, String SourceID){
	String wsName = "CreateReactivationOrder";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));
	String IdProductOffering = Params.GetValue("ProductOfferingID_"+wsName, "");
	String resp = "";
	JSONObject request = new JSONObject(); 
	JSONObject body = new JSONObject(); 
	JSONObject CustomerOrder = new JSONObject();
	JSONObject CustomerOrderItem = new JSONObject();
	JSONObject CustomerAccount = new JSONObject();
	JSONObject BillingAccount = new JSONObject();
	JSONObject SalesChannel = new JSONObject();
	JSONObject ProductOffering = new JSONObject();
	try {	  
		BillingAccount.put("externalID",externalID);
		CustomerAccount.put("IndividualIdentification", IndividualIdentification);
		CustomerAccount.put("BillingAccount", BillingAccount);
		CustomerOrder.put("CustomerAccount", CustomerAccount);
		SalesChannel.put("createBy", "AutomaticoEntel");
		SalesChannel.put("orderCommercialChannel", "IVR");
		
		ProductOffering.put("ID", IdProductOffering);
		CustomerOrderItem.put("ProductOffering", ProductOffering);
		CustomerOrder.put("area", area);
		CustomerOrder.put("subArea", subarea);
		CustomerOrder.put("orderType", order);
		CustomerOrder.put("SalesChannel", SalesChannel);
		CustomerOrder.put("CustomerOrderItem", CustomerOrderItem);
		body.put("CustomerOrder", CustomerOrder);
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
	}finally { 
		request = null;  
		body = null; 
		CustomerOrder = null;
		CustomerAccount = null;
		BillingAccount = null;
		SalesChannel = null;
	}

	return resp;
}
public String ValidateCustomerBlackListReason(JSONObject individualIdentification,String IDllamada, String processID, String SourceID){
	String wsName="ValidateCustomerBlackListReason";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


	String resp = "";
	JSONObject request = new JSONObject();
	JSONObject CustomerAccount = new JSONObject(); 
	JSONObject body = new JSONObject();

	try {	 
		CustomerAccount.put("IndividualIdentification", individualIdentification);
		body.put("CustomerAccount", CustomerAccount);
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
	}finally { 
		request = null;  
		CustomerAccount = null;
		individualIdentification = null;
		body = null;
	}
	return resp;
}
public String GetRequestPayment(String SN, String plan, JSONObject ThirdPartyPayeeAgency, JSONObject CustomerAccount, String Type, String SubType, String IDllamada, String processID, String SourceID){
	String wsName="GetRequestPayment";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


	String resp = "";
	JSONObject request = new JSONObject();
	JSONObject Asset = new JSONObject();
	JSONObject MSISDN = new JSONObject();
	JSONObject InvoiceMSISDN = new JSONObject();
	JSONObject CustomerPayment = new JSONObject();		
	JSONObject body = new JSONObject();

	try {	 
		MSISDN.put("SN", SN);
		MSISDN.put("plan", plan);
		InvoiceMSISDN.put("SN", SN);
		InvoiceMSISDN.put("plan", plan);
		CustomerPayment.put("paymentMethodType",Type);
		CustomerPayment.put("paymentMethodSubType",SubType);
		Asset.put("MSISDN",MSISDN);
		Asset.put("InvoiceMSISDN",InvoiceMSISDN);
		Asset.put("ThirdPartyPayeeAgency",ThirdPartyPayeeAgency);
		Asset.put("CustomerAccount",CustomerAccount);
		Asset.put("CustomerPayment",CustomerPayment);
		body.put("Asset", Asset);
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
	}finally { 
		request = null;  
		CustomerPayment = null;
		InvoiceMSISDN = null;
		MSISDN = null;
		Asset = null;
		body = null;
	}
	return resp;
}
public String GetCreditAvailable(JSONObject GetCreditAvailable, String IDllamada, String processID, String SourceID){
	String wsName="GetCreditAvailable";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


	String resp = "";
	JSONObject request = new JSONObject();		
	JSONObject body = new JSONObject();

	try {	 
		body.put("GetCreditAvailable", GetCreditAvailable);
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
	}finally { 
		request = null;  
		body = null;
	}
	return resp;
}
public String CreateRequestPayment(String SN,JSONObject PaymentMethod,JSONObject ThirdPartyPayeeAgency,JSONObject amount,String IDllamada, String processID, String SourceID){
	String wsName="CreateRequestPayment";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

	String resp = "";
	JSONObject CustomerPayment = new JSONObject();
	JSONObject CustomerAccount = new JSONObject();
	JSONObject MSISDN = new JSONObject();
	JSONObject InvoiceMSISDN = new JSONObject();
	JSONObject Asset = new JSONObject();
	JSONObject request = new JSONObject();		
	JSONObject body = new JSONObject();
	try {	 
		GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
		XMLGregorianCalendar requestDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
		CustomerPayment.put("authorizationCode","92319"+SN);
		CustomerPayment.put("idTransactionRequester",SN+"92319");
		CustomerPayment.put("requestDate",requestDate);
		MSISDN.put("SN",SN);
		InvoiceMSISDN.put("SN", SN);
		Asset.put("MSISDN", MSISDN);
		Asset.put("InvoiceMSISDN", InvoiceMSISDN);
		CustomerAccount.put("Asset",Asset);
		CustomerPayment.put("PaymentMethod",PaymentMethod);
		CustomerPayment.put("ThirdPartyPayeeAgency",ThirdPartyPayeeAgency);
		CustomerPayment.put("amount",amount);
		CustomerPayment.put("CustomerAccount", CustomerAccount);
		body.put("CustomerPayment",CustomerPayment);
		
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
	}finally { 
		request = null;  
		body = null;
		Asset = null;
		InvoiceMSISDN = null;
		MSISDN = null;
		CustomerAccount = null;
		CustomerPayment = null;
	}
	return resp;
}

public String UpdateCreditUsed(String SN, String acumulatedAmount, String currency, String IDllamada, String processID, String SourceID){
	String wsName="UpdateCreditUsed";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


	String resp = "";
	JSONObject request = new JSONObject();		
	JSONObject Credit = new JSONObject();
	JSONObject Update = new JSONObject();
	JSONObject body = new JSONObject();

	try {	 
		Update.put("MSISDN",SN);
		Update.put("acumulatedAmount",acumulatedAmount);
		Update.put("currency",currency);
		Credit.put("Update",Update);
		body.put("Credit", Credit);
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
	}finally { 
		request = null;  
		body = null;
		Credit = null;
		Update = null;
	}
	return resp;
}
public String InsertPega(String Argumentos){
	String dbName="Insert_Pega";
	String dbhost = Params.GetValue("HOST_"+dbName, "10.49.18.75");
	String dbport = Params.GetValue("PORT_"+dbName, "1521");
	String dbServiceName = Params.GetValue("SERVICENAME_"+dbName, "ocdmut01");
	boolean dbSID = Boolean.parseBoolean(Params.GetValue("SID_"+dbName,"false"));
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+dbName, "1000000"));
	String dbSP=Params.GetValue("SP_"+dbName,"");
    String resp="";
	try {
		JSONObject argObject = new JSONObject(Argumentos);
		OracleDBAccess db = new OracleDBAccess();
		String conexion=db.getURLConexion(dbhost, dbport, dbServiceName, dbSID);
		DriverManager.setLoginTimeout(maxTimeout);
		conn=DriverManager.getConnection(conexion);
		try{
			stmt = conn.createStatement();
		}catch(SQLException ex) {
			ex.printStackTrace();
			Debug("[FunctionsEPCS."+dbName+"] SQLException "+ex.getMessage(), "DEBUG");
			Debug("[FunctionsEPCS."+dbName+"] SQLException "+ex.getSQLState(), "DEBUG");
			Debug("[FunctionsEPCS."+dbName+"] SQLException "+ex.getErrorCode(), "DEBUG");
		}
		Debug("[FunctionsEPCS."+dbName+"] Conexion Exitosa ", "DEBUG");
		if(conn!=null){
			Clob myClob = new javax.sql.rowset.serial.SerialClob(argObject.getString("MSSG_IN").toCharArray());
			Debug("[FunctionsEPCS."+dbName+"] Request "+Argumentos, "DEBUG");
			cs = conn.prepareCall ("BEGIN "+dbSP+" (?,?,?,?,?,?,?); END;");
            cs.setInt(1, argObject.getInt("ID_TYPE_IN"));
			cs.setInt(2, argObject.getInt("ID_STTS_IN"));
			cs.setString(3, argObject.getString("ID_BLOCK_IN"));
            cs.setString(4, argObject.getString("RETRY_IN"));
            cs.setClob(5, myClob);
            cs.registerOutParameter (6, OracleTypes.VARCHAR);
            cs.registerOutParameter (7, OracleTypes.VARCHAR);
            cs.execute ();
            Debug("[FunctionsEPCS."+dbName+"] V_OUTPUT_CODE "+cs.getString(6), "DEBUG");
            Debug("[FunctionsEPCS."+dbName+"] v_OUTPUT_DS "+cs.getString(7), "DEBUG");
            resp="{\"V_OUTPUT_CODE\":\""+cs.getString(6)+"\",\"V_OUTPUT_DS\":\""+cs.getString(7)+"\"}";
		}
	}catch(Exception e) {
		e.printStackTrace();
		Debug("[FunctionsEPCS."+dbName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		resp="{\"V_OUTPUT_CODE\":\"-1\",\"V_OUTPUT_DS\":\"Error Al momento de Insertar Data\"}";
	}
	return resp;
}

public String EvaluacionCrediticia(String RUT, String nroSerieRut,String fechaSolicitud, Long horaSolicitud,String IDDeServiceRequest){
	String respuesta ="";
	
	String wsName="EvaluacionCrediticia";
	String wsdlLocation=Params.GetValue("WSDL_"+wsName, "file:WSDL/Evaluacin.wsdl");
	String url = Params.GetValue("URL_"+wsName, "http://10.52.133.27:8095/SIO?service=Evaluacin");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));
	EvaluacionCrediticia client = new EvaluacionCrediticia(wsdlLocation,url, maxTimeout,maxTimeout, debug);
	
	try {
		respuesta = client.get(RUT,nroSerieRut,fechaSolicitud,horaSolicitud,IDDeServiceRequest);
	} catch (Exception e) {
		e.printStackTrace();
	}
		
	return respuesta;
}



	public String Teardown(String movil){
		String respuesta ="";
		
		String wsName="Teardown";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.15.141:8080/CAI3G1.2/services/CAI3G1.2");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		String debug = Params.GetValue("DEBUG_"+wsName,"false"); 
		String user = Params.GetValue("USER_"+wsName, "");
		String pass = Params.GetValue("PASS_"+wsName,""); 
		String MoType = Params.GetValue("MOType_"+wsName,"");
		try {
			int teardown = Integer.parseInt(Params.GetValue("Teardown_"+wsName,"1")); 
			Teardown client = new Teardown(url, maxTimeout,maxTimeout,debug,user,pass); 
			respuesta = client.set(movil,teardown,MoType);
			Debug("[FunctionsEPCS."+wsName+"] Response "+respuesta.replaceAll("\n", ""), "DEBUG");
		 
		} catch (Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}
			
		return respuesta;
	}
	

public String CreateMarketingCampaignContact_v2(String ProductOffering_ID, String RespCampaign, String TextRchazo, String Source, String IDllamada, String processID, String SourceID){
/*
 * Mod: Gabriel Santis Villalón
 * Fec: 2019-06-17
 * Desc: Se agrega metodo CreateMarketingCampaignContact_v2
 * */
	
	String wsName = "CreateMarketingCampaignContact";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.15.149:7010/ES/JSON/"+wsName+"/v2");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

	String resp = "";
	JSONObject request = new JSONObject();

	JSONObject body = new JSONObject();
	
	JSONObject MarketingCampaign = new JSONObject(); //->body
	JSONObject MarketSegment = new JSONObject();//->MarketingCampaign
	JSONObject ProductOffering = new JSONObject();//->MarketSegment		
	
	try {	 
		Random rng = new Random();
		long timeStamp=System.currentTimeMillis()/1000;
		long dig = rng.nextInt(900)+99;
		String requestID = "0078"+timeStamp+dig;				
		
		ProductOffering.put("ID", ProductOffering_ID);
		MarketSegment.put("ProductOffering", ProductOffering);
		
		MarketingCampaign.put("respuesta", RespCampaign);
		if ( RespCampaign.equalsIgnoreCase("Rechazado") ) {
			MarketingCampaign.put("estado", TextRchazo);
		}
		MarketingCampaign.put("source", Source);
		MarketingCampaign.put("MarketSegment", MarketSegment);
					
		body.put("MarketingCampaign", MarketingCampaign);
		
		request.put("Body", body);
					
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url, request, maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		
	}finally { 
		
		request = null;  
		body = null;
		MarketingCampaign=null;
		MarketSegment=null;
		ProductOffering=null;
		
	}
	return resp;
}

public String GetMarketingCampaign_v2(String documentNumber, String msisdn, String IDllamada, String processID, String SourceID){
	/*
	 * Mod: Gabriel Santis Villalón
	 * Fec: 2019-06-17
	 * Desc: Se agrega metodo GetMarketingCampaign_v2 para crear request de versión 2 del servicio
	 *       estructura del request cambio demasiado con respecto a la versión 1.
	 * */
	String wsName = "GetMarketingCampaign";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.15.149:7010/ES/JSON/"+wsName+"/v2");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

	String resp = "";
	JSONObject request = new JSONObject();

	JSONObject body = new JSONObject();
	JSONObject MarketingCampaign = new JSONObject(); //->Request
	JSONObject RequestBody = new JSONObject(); //->body

	try {	 

		MarketingCampaign.put("documentNumber", documentNumber);
		MarketingCampaign.put("msisdn", msisdn);
		MarketingCampaign.put("canal", "IVR");
		
		RequestBody.put("MarketingCampaign", MarketingCampaign);
		
		body.put("Request", RequestBody);
		
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"_v2] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"_v2] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"_v2] Ocurrió un error: "+e.getMessage(), "DEBUG");
		
	}finally { 
		
		request = null;  
		RequestBody = null;
		body = null;
		MarketingCampaign = null;
		
	}
	return resp;
}

public String ValidateCustomerCreditProfile(JSONObject body, String IDllamada, String processID, String SourceID){
	/*
	 * Evaluación crediticia mediante servicio de BUS
	 * */
	String wsName = "ValidateCustomerCreditProfile";
	String url = Params.GetValue("URL_"+wsName, "http://10.49.15.149:7010/ES/JSON/"+wsName+"/v1");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

	String resp = "";
	JSONObject request = new JSONObject();
	
	try {	 
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		
	}finally { 
		
		request = null;  
		
	}
	return resp;
}


public String CreateFastSubscription(String SN, String PO_FAMILY_PLAN, String PO_ID_BOLSA, String Price, String Payment_Method, String CustomerOrder_area, String CustomerOrder_orderType, String CustomerOrder_subArea, String CustomerOrder_operationType, String CustomerOrder_biType, String CustomerOrder_contractCode, String CustomerOrderItem_action, String CustomerOrderItem_biType, String IDllamada, String processID, String SourceID){
	/*
	 * 2019-10-30
	 * Autor: Gabriel Santis Villalón (Sistemas S.A.)
	 * Desc: - Genera Request para WS CreateFastSubscription
	 *       - Para Compra Bolsa contra Saldo y Puntos ZE 
	 *       - Payment_Method define tipo de compra (BALANCE, POINTS)
	 * */	
	
	String wsName = "CreateFastSubscription";
	String url = Params.GetValue("URL_"+wsName, "http://10.12.29.115:8280/FAST/REST/services/channels/v1/createsubscription");
	int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
	boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

	String resp = "";
	
	JSONObject request = new JSONObject();

	JSONObject body = new JSONObject();
	
	JSONObject CustomerOrderItem = new JSONObject();//->body
	JSONObject ProductOffering = new JSONObject(); //->CustomerOrderItem
	
	JSONObject quantity = new JSONObject(); //->ProductOffering
	JSONObject Product = new JSONObject();//->body
	
	JSONObject ProductSpecification = new JSONObject();//->Product	
	JSONObject ProductSpecCharacteristic = new JSONObject();//->Product	
	
	JSONObject CustomerOrder = new JSONObject();//->body
	JSONObject SalesChannel = new JSONObject(); //->CustomerOrder	
	JSONObject Asset = new JSONObject(); //->CustomerOrder
	JSONObject MSISDN = new JSONObject();//->Asset
	
	try {	 
				
		Debug("[FunctionsEPCS."+wsName+"] GSV LOG Request Compra de Bolsa tipo: "+Payment_Method, "DEBUG");
		
		Random rng = new Random();
		long timeStamp=System.currentTimeMillis()/1000;
		long dig = rng.nextInt(900)+99;
		String requestID = "0078"+timeStamp+dig;	

		quantity.put("amount", "1");
		
		ProductOffering.put("quantity", quantity);
		ProductOffering.put("ID", PO_ID_BOLSA);
							
		ProductSpecCharacteristic.put("classification", "PAYMENT");
		ProductSpecCharacteristic.put("name", "PAYMENT_METHOD");
		ProductSpecCharacteristic.put("value", Payment_Method); //"BALANCE, POINTS";
		
		ProductSpecification.put("ProductSpecCharacteristic", ProductSpecCharacteristic);
		
		Product.put("ProductSpecification", ProductSpecification);
		
		Product.put("category", "Bolsa");
		Product.put("family", "Bolsa");
		Product.put("name", "Bolsa");
		
		CustomerOrderItem.put("Product", Product);
		
		CustomerOrderItem.put("ProductOffering", ProductOffering);
		
		CustomerOrderItem.put("action", CustomerOrderItem_action);
		CustomerOrderItem.put("biType", CustomerOrderItem_biType);
		CustomerOrderItem.put("price", Price);				
		
		CustomerOrder.put("area", CustomerOrder_area); 
		CustomerOrder.put("biType", CustomerOrder_biType);
		CustomerOrder.put("channel", "IVR");			
		CustomerOrder.put("mode", "NON_INTERACTIVE");
		CustomerOrder.put("orderType", CustomerOrder_orderType);	
		CustomerOrder.put("requestID", requestID);
		CustomerOrder.put("subArea", CustomerOrder_subArea);
		CustomerOrder.put("operationType", CustomerOrder_operationType);
				
		SalesChannel.put("ID", "IVR");
		SalesChannel.put("createBy", "IVR");
		SalesChannel.put("orderCommercialChannel", "IVR");	
		
		MSISDN.put("SN", SN);
		Asset.put("ID", PO_FAMILY_PLAN);
		Asset.put("MSISDN", MSISDN);
		Asset.put("contractCode", CustomerOrder_contractCode);
		
		CustomerOrder.put("Asset", Asset);							
		CustomerOrder.put("SalesChannel", SalesChannel);
								
		body.put("CustomerOrderItem", CustomerOrderItem);
		body.put("CustomerOrder", CustomerOrder);	
				
		JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
		request.put("RequestHeader", header);
		request.put("Body", body);

		Debug("[FunctionsEPCS."+wsName+"] GSV LOG Request "+request, "DEBUG");
		resp = ejecutarRest(url,request,maxTimeout, debug);				
		Debug("[FunctionsEPCS."+wsName+"] GSV LOG Response "+resp.replaceAll("\n", ""), "DEBUG");

	}catch(Exception e) {
		
		e.printStackTrace();
		Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		
	}finally { 
		
		request = null;  
		body = null;
		CustomerOrderItem = null;
		ProductOffering = null;
		quantity = null;
		Product = null;
		ProductSpecification = null;
		ProductSpecCharacteristic = null;
		CustomerOrder = null;
		SalesChannel = null;
		Asset = null;
		MSISDN = null;
		
	}
	return resp;
}

	
}