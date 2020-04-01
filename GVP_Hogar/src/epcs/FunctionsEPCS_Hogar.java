package epcs;   
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

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

import eContact.FunctionsGVP;
import eContact.Parameters;

public class FunctionsEPCS_Hogar extends FunctionsGVP
{
	public static JSONObject Token_API = new JSONObject(); 
	public Parameters parametrosBD = new Parameters();
	public String  hostIvrToDB     = "127.0.0.1";
	public int     portIvrToDB     = 50081;
	public String InstanceID = "";
	public FunctionsEPCS_Hogar(String ParametersFile)
	{
		super(ParametersFile);  			
		inicializar();
	}

	public FunctionsEPCS_Hogar(String ParametersFile, String id)
	{    	
		super(ParametersFile, id);
		inicializar();
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

			String sysCode = Params.GetValue("Hogar_sysCode", "IVR");
			String enterpriseCode = Params.GetValue("Hogar_enterpriseCode","ENTEL-CHL");
			String countryCode = Params.GetValue("Hogar_countryCode","CHL");
			String channelName = Params.GetValue("Hogar_channelName", "IVR");
			String channelMode = Params.GetValue("Hogar_channelMode","NO PRESENCIAL"); 
			String eventID = Params.GetValue("Hogar_eventID","0078"); 
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
	/*
	 * GetActivities
	 */

	public String CreateOneClickToOrder(String PO_ID, String action, String biType_Item, String area, String orderType, String subArea, String operationType, String biType, String planActual, String SN, String IDllamada, String processID, String SourceID, String bscsCustomerId, String PtosZEToBurn, String Product_name, String Product_value, String PO_ID_DISC, String classification ){
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

	public String ejecutarRest(String url, JSONObject JSONrequest, long timeout, boolean debug, JSONObject headerHTTP) {
		/*
		 * 2020-03-03
		 * Autor: Gabriel Santis Villalón (Sistemas S.A.)
		 * Desc:  - sobrecarga de metodo ejecutarRest para recibir campos a enviar en http header (String header[])
		 *          exigido por WS CreateFastSubscription/v2
		 * 
		 * Response: {"ws":{"code":"0","responseTimeStamp":"2020-03-05T13:53:31.356Z","description":"Operación que termina con éxito"}, "httpcode":"201"}}
		 * Uso: if(!respJSON.isNull("ws")) {	
		 *			String fast_code = respJSON.getJSONObject("ws").getString("code");
		 *			String fast_httpcode = respJSON.getString("httpcode");
		 *			String fast_description = respJSON.getJSONObject("ws").getString("description");
		 *      }
		 * 
		 * 2020-03-05
		 * Lduhalde
		 * Se modifica método para hacerlo más "genérico".
		 * Recibe por parametro JSONObject headerHTTP y luego lo recorre para asignar las cabeceras http. 
		 * 
		 * */
		String respuesta = "";

		try {

			WebClient client = WebClient.create(url); 
			client = client.accept(MediaType.APPLICATION_JSON).type("application/json; charset=utf-8");
			HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
			conduit.getClient().setConnectionTimeout(timeout);
			conduit.getClient().setReceiveTimeout(timeout); 
			String  request = JSONrequest.toString();					

			if(debug) {
				ClientConfiguration config = WebClient.getConfig(client);
				config.getInInterceptors().add(new LoggingInInterceptor());
				config.getOutInterceptors().add(new LoggingOutInterceptor());
			}

			Iterator<String> keys = headerHTTP.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				String value = headerHTTP.getString(key);
				client.header(key, value);
			}

			Response r = client.post(request);

			respuesta = "{\"ws\":"+r.readEntity(String.class)+", \"httpcode\":\""+r.getStatus()+"\"}}";

			client.close();

		}catch(Exception e) {

			e.printStackTrace();
			respuesta = "";

		}finally {

			JSONrequest = null;

		}

		return respuesta;
	}


	public String ExecuteAPI(JSONObject request, String method, String idLlamada){
		String wsName="ExecuteAPI";
		String url = Params.GetValue("URL_"+method, "https://apiinternaluat.entel.cl/customer/v1/customerProblem/troubleshoot/search");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+method, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+method,"false"));

		String resp = "";
		try {
			ValidateTokenAPI();
			if(Token_API.has("token_string")){
				JSONObject headerHTTP = new JSONObject();
				headerHTTP.put("Authorization", Token_API.getString("token_string"));
				headerHTTP.put("applicationCode", "IVR");
				headerHTTP.put("countryCode", "CHL");
				headerHTTP.put("consumerId", idLlamada);

				GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
				XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);

				headerHTTP.put("requestTimestamp", xgcal.toString());
				Debug("[FunctionsEPCS."+wsName+"] headerHTTP "+headerHTTP.toString(), "DEBUG");
				
				resp = ejecutarRest(url,request,maxTimeout, debug,headerHTTP);				
				Debug("[FunctionsEPCS."+wsName+"] Response "+resp.replaceAll("\n", ""), "DEBUG");
			}else{
				resp="SIN TOKEN";
			}
		}catch(Exception e) {
			e.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
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

	/*
	 * GetBundleProductOffering
	 */
	public String GetAsset(JSONObject Body, String IDllamada, String processID, String SourceID){
		String wsName = "GetAsset";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));

		String resp = "";
		JSONObject request = new JSONObject();
		try {	 
			JSONObject header = crearHeader(wsName, IDllamada, processID, SourceID);
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
		}
		return resp;
	}

	/*
	 * GetLifAmount
	 */
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
			Body = null; 
		}
		return resp;
	}
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

	//GetCustomerAccountBalance

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
	public String GetServiceRequest(JSONObject body,String IDllamada, String processID, String SourceID){
		String wsName="GetServiceRequest";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.232:7011/ES/JSON/"+wsName+"/v1");
		int maxTimeout = Integer.parseInt(Params.GetValue("TIMEOUT_"+wsName, "1000000"));
		boolean debug = Boolean.parseBoolean(Params.GetValue("DEBUG_"+wsName,"false"));


		String resp = ""; 
		JSONObject request = new JSONObject();  

		try {

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

		}
		return resp;
	}

	public String GetTokenAPI(String URLToken, String ClientID, String ClientSecret, String GrantType) throws JSONException, IOException{

		/*
		 * Desc:  Recupera y retorna JSON de respuesta con información de TOKEN
		 *        para consumir WS desde API. Si falla, revisar que certficados 
		 *        esten instalados en la keystore de java
		 * Autor: Gabriel Santis V.
		 * Fecha: 2020-02-11		 
		 *        
		 * */

		String resp_JsonToken = "";
		String wsName="GetTokenAPI";

		try {

			URL url_gettoken = new URL(URLToken);
			Map<String,Object> params = new LinkedHashMap<>();

			params.put("client_id", ClientID);
			params.put("client_secret", ClientSecret);
			params.put("grant_type", GrantType);        

			StringBuilder postData = new StringBuilder();

			for (Map.Entry<String,Object> param : params.entrySet()) {

				if (postData.length() != 0) postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));

			}

			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			HttpURLConnection conn = (HttpURLConnection)url_gettoken.openConnection();

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			conn.setDoOutput(true);
			conn.getOutputStream().write(postDataBytes);

			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

			StringBuilder sb = new StringBuilder();
			for (int c; (c = in.read()) >= 0;)
				sb.append((char)c);
			resp_JsonToken = sb.toString();

		} catch (MalformedURLException e) {
			Debug("[FunctionsEPCS."+wsName+"] URL no es valida: " + e.getMessage(), "DEBUG");

		} catch (IOException e) {
			Debug("[FunctionsEPCS."+wsName+"] Error de I/O: " + e.getMessage(), "DEBUG");
			e.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e2.getMessage(), "DEBUG");

		}


		return resp_JsonToken;

	}

	public String GetUsageThresholdCounter(String movil, String ProductID, String IDllamada, String processID, String SourceID){
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
	// createNotification (SMS)
	/*Parametros adicionales a Leer que no lee el FGVP*/
	private void inicializar(){
		hostIvrToDB = this.Params.GetValue("IvrToDBhost", "127.0.0.1");
		portIvrToDB = Integer.valueOf(this.Params.GetValue("IvrToDBport", "50080"));
	}
	public XMLGregorianCalendar ObtenerFechaXML() {
		GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
		XMLGregorianCalendar xgcal = null;
		try {
			xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
		} catch (DatatypeConfigurationException e1) {
			e1.printStackTrace();
		}
		return xgcal;
	}
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
			Debug("[RegistraMarcaNavegacion] INICIO "+servicio+" - "+queryIvrToDB, "INFO");

			String mensaje = "{\"servicio\":\""+servicio+"\", \"select\":\"2\", \"query\":\"" + queryIvrToDB + "\", \"parameters\":\"" + paramsIvrToDB + "\"}\n";
			Debug("[RegistraMarcaNavegacion] ENTRADA "+mensaje, "DEBUG");

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
				jsonRetorno.put("CODMSG", "NOK");
				if(jObjRespuesta.getString("errorMessage").equalsIgnoreCase("ResultSet is from UPDATE. No Data.")){
					jsonRetorno.put("CODMSG", "IGNORE");
				}
				jsonRetorno.put("MSG", "["+jObjRespuesta.getString("errorCode")+"] "+jObjRespuesta.getString("errorMessage"));
			}
			retorno = jsonRetorno.toString();

		} catch (Exception ex) {
			Debug("[RegistraMarcaNavegacion] Exception "+ex.getMessage(), "DEBUG");
			retorno="{\"CODMSG\":\"Error\",\"MSG\":"+ex.getMessage()+"}";
			ex.printStackTrace();
		}finally{
			Debug("[RegistraMarcaNavegacion] FIN", "INFO");

		}
		return retorno;
	}
	public String SetSignalRefresh(JSONObject body, String IDllamada, String processID, String SourceID){
		String wsName="SetSignalRefresh";
		String url = Params.GetValue("URL_"+wsName, "http://10.49.4.86:7011/ES/JSON/"+wsName+"/v1");
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
			body = null;
		}
		return resp;
	}
	public void SetTokenAPI(){
		String wsName="SetTokenAPI";
		String URLToken = Params.GetValue("URL_GetTokenAPI", "https://apiinternaluat2.entel.cl/auth/oauth/v2/token");
		String ClientID = Params.GetValue("USER_GetTokenAPI", "l779febd401d7348cba2ba4cabecd183cc");
		String ClientSecret= Params.GetValue("PASS_GetTokenAPI", "aeb13fb74957479e9e6a023a5ceb87eb");
		String GrantType= Params.GetValue("GRANT_GetTokenAPI", "client_credentials");
		try {
			String resp = GetTokenAPI(URLToken, ClientID, ClientSecret, GrantType);
			Debug("[FunctionsEPCS."+wsName+"] resp GetTokenAPI: "+resp.replaceAll("\n", " "), "DEBUG");
			if(resp.startsWith("{")){
				JSONObject newToken = new JSONObject(resp);
				if(newToken.has("access_token")){
					int time = (int) (System.currentTimeMillis()/1000);
					newToken.put("last_call", time);
					String token_string = newToken.getString("token_type")+" "+newToken.getString("access_token");
					newToken.put("token_string", token_string);
					Token_API = newToken;
				}else{
					Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: No se encuentra access_token", "DEBUG");
					Token_API = newToken;
				}
			}else{
				Debug("[FunctionsEPCS."+wsName+"] Respuesta no es JSON", "DEBUG");
			}
		} catch (JSONException e) {
			Debug("[FunctionsEPCS."+wsName+"] Ocurrió un error: "+e.getMessage(), "DEBUG");
		} catch (IOException e) {
			Debug("[FunctionsEPCS."+wsName+"] Error de I/O: " + e.getMessage(), "DEBUG");
		}
	}
	public String Socket_SendRecv (String Server, int Port, int Timeout, String Transaccion, String Message2Send)throws Exception
	{
		String sReturn = "";
		Socket SocketServer = null;
		PrintWriter SocketServerOutputStream = null;
		BufferedReader SocketServerInputStream = null;

		try
		{

			Debug("[FunctionsEPCS.Socket_SendRecv] - Se ejecutar la transacción : " + Transaccion + " - " + Server + ":" + Port + " - TimeOut:" + String.valueOf(Timeout), "Detail");
			Debug("[FunctionsEPCS.Socket_SendRecv] -  > Data [" + Message2Send + "]", "Detail");

			try
			{
				SocketServer = new Socket(Server, Port);
				SocketServer.setKeepAlive(true);
				SocketServer.setSoTimeout(Timeout);

				SocketServerInputStream = new BufferedReader(new InputStreamReader(SocketServer.getInputStream()));
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

		Debug("[FunctionsEPCS.Socket_SendRecvHA] Conectando con SocketServer Primario.", "Detail");

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

		Debug("[FunctionsEPCS.Socket_SendRecvHA] Conectando con SocketServer Primario.", "Detail");

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

			long inicio = 0;
			if (parametros_marcas_navegacion.has("TRAZA_INICIO") ){
				inicio = parametros_marcas_navegacion.getLong("TRAZA_INICIO");
			}

			if (inicio>0){
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

			if(resultado.getString("CODMSG").equalsIgnoreCase("OK") || resultado.getString("CODMSG").equalsIgnoreCase("IGNORE")){
				Debug("[FunctionsEPCS.stopNavegacion] Registro exitoso", "DEBUG");
				parametros_marcas_navegacion.put("TRAZA_CONTADOR", contadorTrazas);
				parametros_marcas_navegacion.remove("TRAZA_INICIO");
				parametros_marcas_navegacion.remove("TRAZA");
				parametros_marcas_navegacion.remove("RC");
				parametros_marcas_navegacion.remove("DATA");
				parametros_marcas_navegacion.remove("MSG");
			}else{
				Debug("[FunctionsEPCS.stopNavegacion] Error de registro", "DEBUG");
				Debug("[FunctionsEPCS.stopNavegacion] "+resultado, "DEBUG");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return parametros_marcas_navegacion;
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
	/*
	 * GetUserProfile
	 */
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
	public void ValidateTokenAPI(){
		try {
			if(!Token_API.has("last_call")){
				SetTokenAPI();
			}else{
				int last_call = Token_API.getInt("last_call");
				Debug("[FunctionsEPCS.ValidateTokenAPI] last_call: "+last_call, "DEBUG");
				int time = (int) (System.currentTimeMillis()/1000);
				int expires_in = Integer.parseInt(Token_API.getString("expires_in"));
				Debug("[FunctionsEPCS.ValidateTokenAPI] expires_in: "+expires_in, "DEBUG");
				if((time-last_call)>=expires_in){
					SetTokenAPI();
				}
			}
			Debug("[FunctionsEPCS.ValidateTokenAPI] Token_API: "+Token_API.toString(), "DEBUG");
		} catch (JSONException e) {
			Debug("[FunctionsEPCS.ValidateTokenAPI] Ocurrió un error: "+e.getMessage(), "DEBUG");
		}
	}

}