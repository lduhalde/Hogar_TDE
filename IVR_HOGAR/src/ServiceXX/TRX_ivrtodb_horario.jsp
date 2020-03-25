<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	String idHorario=additionalParams.get("idHorario");
	String fecha=additionalParams.get("fecha");
	String nameJSP="TRX_ivrtodb_horario";
    String trx_respuesta="NOK";
    JSONObject result = new JSONObject();
    JSONObject  DataEnvioSckt = new JSONObject();
    String accion="";
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	//CALCULO DE LA FECHA SINO SE RECIBE COMO PARAMETRO
    	if(fecha.isEmpty()){
    		 fecha = fEPCS.getFechaHora("yyyy-MM-dd HH:mm:ss");
    		 
    	}

    	//DEBUG VALORES INICIALES		
    	fEPCS.Debug("["+nameJSP+"] INICIO", "INFO");
    	fEPCS.Debug("["+nameJSP+"] ID_HORARIO: "+idHorario, "INFO");
    	fEPCS.Debug("["+nameJSP+"] FECHA: "+fecha, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_HORARIO");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
    	 
    	//OBTENCION DE LA FECHA EN CASO DE QUE 
    	if(!idHorario.isEmpty()){
			DataEnvioSckt.put("servicio","BD_GUI");
			DataEnvioSckt.put("query","[SP_HORARIOATENCION_IVRTDE]");
			DataEnvioSckt.put("parameters",idHorario+"|"+fecha);
			DataEnvioSckt.put("select","1");
			DataEnvioSckt.put("requestID",state.getString("idLlamada"));
			
			//String respuestaDB= fEPCS.Socket_SendRecvHA("GETHORARIO", DataEnvioSckt.toString());
			String respuestaDB= fEPCS.Socket_SendRecvHA("GETHORARIO", DataEnvioSckt.toString(),"DB");
			fEPCS.Debug("["+nameJSP+"] respuestaDB: "+respuestaDB, "INFO");
			
			
			if(!respuestaDB.equals("[]")||respuestaDB.indexOf("errorMessage")==-1){
				JSONObject jObjRespuesta = new JSONObject(respuestaDB.replaceAll("\\[", "").replaceAll("\\]", ""));
				String status= jObjRespuesta.getString("RESULT");
				parametros_marcas_navegacion.put("RC","0");
				trx_respuesta="OK";
				result.put("statusHorario", status);	
				
				jObjRespuesta = null;
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