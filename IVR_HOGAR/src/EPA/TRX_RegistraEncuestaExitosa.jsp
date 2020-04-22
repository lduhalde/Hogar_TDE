<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	String Tenant = additionalParams.get("Tenant");
	String Encuesta = additionalParams.get("Encuesta");
	String IdPregunta = additionalParams.get("IdPregunta");
	String IdRequest = additionalParams.get("IdRequest");
	String StatusID = additionalParams.get("StatusID");
	String Tramo = additionalParams.get("Tramo");
	String nameJSP="TRX_RegistraEncuestaExitosa";
	String RC="-1";
    String trx_respuesta="NOK";
    JSONObject result = new JSONObject();
    JSONObject  DataEnvioSckt = new JSONObject();
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	//DEBUG VALORES INICIALES		
    	fEPCS.Debug("["+nameJSP+"] INICIO", "INFO");
    	fEPCS.Debug("["+nameJSP+"] Tenant : "+Tenant, "INFO");
    	fEPCS.Debug("["+nameJSP+"] Encuesta : "+Encuesta, "INFO");
    	fEPCS.Debug("["+nameJSP+"] IdRequest : "+IdRequest, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_RENCUESTA_EXITOSA_EPA");
		parametros_marcas_navegacion.put("DATA","REGISTRA");
		parametros_marcas_navegacion.put("RC","99");
		parametros_marcas_navegacion.put("MSG","NO");
    	
		StatusID="4";
		DataEnvioSckt.put("servicio","BD_EPA");
    	DataEnvioSckt.put("query","[EPA_ONLINE_CERRAR_ENCUESTA]");
    	DataEnvioSckt.put("parameters",Encuesta+"|"+Tenant+"|"+IdRequest+"|"+StatusID+"|"+Tramo);
    	DataEnvioSckt.put("select","1");
    	DataEnvioSckt.put("requestID",state.getString("idLlamada"));
    		
    	fEPCS.Debug("["+nameJSP+"] - DataEnvioSckt: "+DataEnvioSckt.toString(), "INFO");
    	String respuestaDB= fEPCS.Socket_SendRecvHA("RENCUESTA_EXITOSA_EPA", DataEnvioSckt.toString(),"DB2");
    	fEPCS.Debug("["+nameJSP+"] respuestaDB: "+respuestaDB, "INFO");
    		
    	if(!respuestaDB.equals("[]")||respuestaDB.indexOf("errorMessage")==-1){
    		JSONObject jObjRespuesta = new JSONObject(respuestaDB.replaceAll("\\[", "").replaceAll("\\]", ""));
    		RC=jObjRespuesta.getString("RC");
    		fEPCS.Debug("["+nameJSP+"] RC : "+RC, "INFO");
    		if(RC.equals("0")){
    			trx_respuesta="OK";
    			parametros_marcas_navegacion.put("RC","0");
    			parametros_marcas_navegacion.put("MSG","SI");
    		}
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