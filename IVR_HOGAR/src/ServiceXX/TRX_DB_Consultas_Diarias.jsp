<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	
	String nameJSP="TRX_DB_Consultas_Diarias";
	String trx = "TRX_CONSULTAS_DIARIAS";
    String trx_respuesta="NOK";
	//Parametros de Entradas
	String id_consulta=additionalParams.get("id_consulta");
	String PCS=additionalParams.get("PCS");
	//FIN
    JSONObject result = new JSONObject();
    JSONObject  DataEnvioSckt = new JSONObject();
    String enBase = "";
    
       
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    
 	 //DEBUG VALORES INICIALES		
	fEPCS.Debug("["+nameJSP+"] INICIO", "INFO");
	fEPCS.Debug("["+nameJSP+"] ID_CONSULTA: "+id_consulta, "INFO");
	fEPCS.Debug("["+nameJSP+"] PCS: "+PCS, "INFO");
	
    
    try{
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,trx);
    	parametros_marcas_navegacion.put("DATA",PCS);
    	parametros_marcas_navegacion.put("RC","99");
    	
    	if(!PCS.startsWith("56")){
    		PCS="56"+PCS;
    	}
    	 
    	
   	
		DataEnvioSckt.put("servicio",fEPCS.getParametro(trx + "_DB"));
		DataEnvioSckt.put("query",fEPCS.getParametro(trx + "_SP"));
		DataEnvioSckt.put("parameters",PCS+"|"+id_consulta);
		DataEnvioSckt.put("select","1");
		DataEnvioSckt.put("requestID",state.getString("idLlamada"));
		
	
		String respuestaDB= fEPCS.Socket_SendRecvHA("CONSULTAS_DIARIAS", DataEnvioSckt.toString(),"DB");
		fEPCS.Debug("["+nameJSP+"] respuestaDB: "+respuestaDB, "INFO");
		
		
		if(!respuestaDB.equals("[]")||respuestaDB.indexOf("errorMessage")==-1){
			JSONObject jObjRespuesta = new JSONObject(respuestaDB.replaceAll("\\[", "").replaceAll("\\]", ""));
			if(!jObjRespuesta.isNull("Contador")){
				result.put("contador",jObjRespuesta.getInt("Contador"));
			}		
			parametros_marcas_navegacion.put("RC","0");
			trx_respuesta="OK";
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