<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	String Encuesta = additionalParams.get("Encuesta");
	String nameJSP="TRX_Recupera_Parametros_EPA";
    String trx_respuesta="NOK";
    JSONObject result = new JSONObject();
    JSONObject  DataEnvioSckt = new JSONObject();
    String RC ="99";
    String CantidadPreguntas="";
    String PreguntaGlobal="";

    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	//DEBUG VALORES INICIALES		
    	fEPCS.Debug("["+nameJSP+"] INICIO", "INFO");
    	fEPCS.Debug("["+nameJSP+"] ENCUESTA: "+Encuesta, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_RECUPERA_PARAMETROS");
    	parametros_marcas_navegacion.put("DATA","QPREGUNTAS");
    	parametros_marcas_navegacion.put("RC","99");
    	parametros_marcas_navegacion.put("MSG","-1");
    	 
		DataEnvioSckt.put("servicio","BD_EPA");
		DataEnvioSckt.put("query","[EPA_ONLINE_OBTIENE_DETALLE_ENCUESTA]");
		DataEnvioSckt.put("parameters",Encuesta);
		DataEnvioSckt.put("select","1");
		DataEnvioSckt.put("requestID",state.getString("idLlamada"));
			
		fEPCS.Debug("["+nameJSP+"] - DataEnvioSckt: "+DataEnvioSckt.toString(), "INFO");
		String respuestaDB= fEPCS.Socket_SendRecvHA("RECUPERA_PARAMETROS", DataEnvioSckt.toString(),"DB2");
		fEPCS.Debug("["+nameJSP+"] respuestaDB: "+respuestaDB, "INFO");	
			
		if(!respuestaDB.equals("[]")||respuestaDB.indexOf("errorMessage")==-1){
			JSONObject jObjRespuesta = new JSONObject(respuestaDB.replaceAll("\\[", "").replaceAll("\\]", ""));
			RC= jObjRespuesta.get("RC").toString();
			if(RC.equals("0")){
				CantidadPreguntas= jObjRespuesta.get("Q_PREGUNTAS").toString();
				PreguntaGlobal=jObjRespuesta.get("PREGUNTA_GLOBAL").toString();
				trx_respuesta="OK";
				parametros_marcas_navegacion.put("RC","0");
				parametros_marcas_navegacion.put("MSG",CantidadPreguntas);
			}
			jObjRespuesta = null;
		}
		result.put("RC", RC);
		result.put("QPreguntas", CantidadPreguntas);
		result.put("RespuestaDB", respuestaDB);
		result.put("PreguntaGlobal", PreguntaGlobal);
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