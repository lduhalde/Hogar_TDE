<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	
	String nameJSP="TRX_DB_Consulta_Parametros_GUI";
	String trx = "TRX_GETPARAMS";
    String trx_respuesta="NOK";
	//Parametros de Entradas
	String key = additionalParams.get("key");
	String value = additionalParams.get("defaultValue");
	//FIN
    JSONObject result = new JSONObject();
    JSONObject  DataEnvioSckt = new JSONObject();
    JSONObject  DataEnvioSckt2 = new JSONObject();   

    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    
 	 //DEBUG VALORES INICIALES		
	fEPCS.Debug("["+nameJSP+"] INICIO", "INFO");
	
    try{
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,trx);
    	parametros_marcas_navegacion.put("DATA",key);
    	parametros_marcas_navegacion.put("RC","99");
    	 
		DataEnvioSckt2.put("servicio","BD_APP_GVP");
		DataEnvioSckt2.put("query","SP_CONSULTA_ATRIBUTOS_GUI_V2");
		DataEnvioSckt2.put("parameters",key);
		DataEnvioSckt2.put("select","1");
		DataEnvioSckt2.put("requestID",state.getString("idLlamada"));
		
	
		String respuestaDB= fEPCS.Socket_SendRecvHA("GETPARAMS", DataEnvioSckt2.toString(),"DB");
		fEPCS.Debug("["+nameJSP+"] respuestaDB: "+respuestaDB, "INFO");
		
		
		if(!respuestaDB.equals("[]")||respuestaDB.indexOf("errorMessage")==-1){
			JSONObject jObjRespuesta = new JSONObject(respuestaDB.replaceAll("\\[", "").replaceAll("\\]", ""));
			if(!jObjRespuesta.isNull(key)){
				value = jObjRespuesta.getString(key);
			}
			parametros_marcas_navegacion.put("RC","0");
			trx_respuesta="OK";
			jObjRespuesta = null;
		}
		
    }catch(Exception ex){
    	fEPCS.Debug("["+nameJSP+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	result.put("value",value);
    	result.put("trx_respuesta", trx_respuesta);
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+nameJSP+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+nameJSP+"] FIN result: "+result.toString(), "INFO");
    	result.put("trx_respuesta", trx_respuesta);
    	DataEnvioSckt2 = null;
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