<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	
	String nameJSP="TRX_DB_TransferParam";
	String trx = "TRX_TRANSFER_PARAM";
    String trx_respuesta="NOK";
	//Parametros de Entradas
	String token=additionalParams.get("token");
	//FIN
    JSONObject result = new JSONObject();
    JSONObject  DataEnvioSckt = new JSONObject();
    
    
       
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
 	 //DEBUG VALORES INICIALES		
	fEPCS.Debug("["+nameJSP+"] INICIO", "INFO");
	fEPCS.Debug("["+nameJSP+"] TOKEN: "+token, "INFO");
	
    
    try{
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,trx);
    	parametros_marcas_navegacion.put("DATA",token);
    	parametros_marcas_navegacion.put("RC","99");
    	 
    	//OBTENCION DE LA FECHA EN CASO DE QUE 
   	
		DataEnvioSckt.put("servicio",fEPCS.getParametro(trx + "_DB"));
		DataEnvioSckt.put("query",fEPCS.getParametro(trx + "_SP"));
		DataEnvioSckt.put("parameters",token);
		DataEnvioSckt.put("select","1");
		DataEnvioSckt.put("requestID",state.getString("idLlamada"));
		
	
		String respuestaDB= fEPCS.Socket_SendRecvHA("TRANSFER_PARAM", DataEnvioSckt.toString(),"DB");
		fEPCS.Debug("["+nameJSP+"] respuestaDB: "+respuestaDB, "INFO");
		
		
		if(!respuestaDB.equals("[]")||respuestaDB.indexOf("errorMessage")==-1){
			JSONObject jObjRespuesta = new JSONObject(respuestaDB.replaceAll("\\[", "").replaceAll("\\]", ""));
			
			String transferID= jObjRespuesta.getString("TRANSFER_ID");
			String sga= jObjRespuesta.getString("SGA");
			
			parametros_marcas_navegacion.put("RC","0");
			trx_respuesta="OK";
			
			result.put("transferID", transferID);	
			result.put("sga", sga);
			
			jObjRespuesta = null;
		}
		
		
			
    }catch(Exception ex){
    	fEPCS.Debug("["+nameJSP+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	result.put("trx_respuesta", trx_respuesta);
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+nameJSP+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+nameJSP+"] FIN result: "+result.toString(), "INFO");
    	result.put("trx_respuesta", trx_respuesta);
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