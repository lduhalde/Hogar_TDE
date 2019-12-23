<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	 
    String trx_respuesta="NOK";
	//Parametros de Entradas
	JSONObject cliente_datos = new JSONObject();
    JSONObject result = new JSONObject();
    JSONObject  DataEnvioSckt = new JSONObject();
    String nameJSP="TRX_ACTUALIZA_CDR";
    String trx="TRX_ACTUALIZA_CDR";
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
 	 //DEBUG VALORES INICIALES		
	fEPCS.Debug("["+nameJSP+"] INICIO", "INFO"); 
	
    try{
    	
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
        String uniqueid = state.optString("AsteriskID");
    	String mercado = cliente_datos.optString("mercado");
    	String segmento = cliente_datos.optString("segmento");
    	String Rut = "";
    	String plan = cliente_datos.optString("PO_PLAN");
    	String appname = parametros_marcas_navegacion.optString("appName");
    	
    	if(!cliente_datos.optJSONObject("IndividualIdentification").isNull("number")){
    		if(!cliente_datos.optJSONObject("IndividualIdentification").isNull("type")){
    			if(cliente_datos.optJSONObject("IndividualIdentification").getString("type").equalsIgnoreCase("RUT")){
    				Rut=cliente_datos.optJSONObject("IndividualIdentification").getString("number"); 
				}
		 	}
    	}
    
    	fEPCS.Debug("["+nameJSP+"] APPNAME: "+appname, "INFO"); 	 
    	fEPCS.Debug("["+nameJSP+"] UNIQUEID: "+uniqueid, "INFO"); 
    	fEPCS.Debug("["+nameJSP+"] MERCADO: "+mercado, "INFO"); 
    	fEPCS.Debug("["+nameJSP+"] SEGMENTO: "+segmento, "INFO"); 
    	fEPCS.Debug("["+nameJSP+"] RUT: "+Rut, "INFO"); 
    	fEPCS.Debug("["+nameJSP+"] PLAN: "+plan, "INFO"); 
    	
    	DataEnvioSckt.put("servicio",fEPCS.getParametro(trx + "_DB"));
		DataEnvioSckt.put("query",fEPCS.getParametro(trx + "_SP"));
		DataEnvioSckt.put("parameters",uniqueid+"|"+mercado+"|"+segmento+"|"+Rut+"|"+plan+"|"+appname);
		DataEnvioSckt.put("select","1");
		DataEnvioSckt.put("requestID",state.getString("idLlamada"));
			
		String respuestaDB= fEPCS.Socket_SendRecvHA("REGISTRA_NAVEGACION", DataEnvioSckt.toString(),"NAV");
		fEPCS.Debug("["+nameJSP+"] respuestaDB: "+respuestaDB, "INFO");
			
		if(!respuestaDB.equals("[]")){
			JSONObject resp = new JSONObject(respuestaDB);
			if(resp.optString("errorCode").equals("0")){
				trx_respuesta="OK";
				fEPCS.Debug("["+nameJSP+"] Actualización OK");
			}
		}
			
    }catch(Exception ex){
    	fEPCS.Debug("["+nameJSP+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	result.put("trx_respuesta", trx_respuesta); 
    	fEPCS.Debug("["+nameJSP+"] FIN result: "+result.toString(), "INFO"); 
    	DataEnvioSckt = null;
    	parametros_marcas_navegacion = null;
    }
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>