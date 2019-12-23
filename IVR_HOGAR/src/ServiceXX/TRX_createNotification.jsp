<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
      
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));
    String trx_respuesta = "NOK";
    String codigoResp = "";
    
    try{
		String ani = additionalParams.get("PCS");
    	ani = (ani.length() < 11 ) ? "56"+ani : ani;
		
		
		
		String sMensaje = additionalParams.get("MENSAJE");
		String externalID = additionalParams.get("externalID");
				
    	fEPCS.Debug("[TRX_createNotification] INICIO", "INFO");
    	fEPCS.Debug("[TRX_createNotification] ANI: "+ani, "INFO");
    	fEPCS.Debug("[TRX_createNotification] Mensaje: "+sMensaje, "INFO");
    	
    	String processCode = additionalParams.get("processCode");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	fEPCS.Debug("[TRX_createNotification] processCode: "+processCode, "INFO");
    	fEPCS.Debug("[TRX_createNotification] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_createNotification] idLlamada: "+idLlamada, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_NOTIFICATION");
    	parametros_marcas_navegacion.put("DATA","CREATE");
    	parametros_marcas_navegacion.put("RC","99");
  
    	String sTrx_datos_respuesta=fEPCS.CreateNotification(externalID,"entel","IVR",ani,"",sMensaje,sourceID, processCode, idLlamada);
    	fEPCS.Debug("[TRX_createNotification] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO"); 
		respJSON = new JSONObject(sTrx_datos_respuesta); 

    	
		if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("[TRX_createNotification] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			String status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				String description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("[TRX_createNotification] DESCRIPTION: "+description, "INFO"); 
			
			}
			
			fEPCS.Debug("[TRX_createNotification] STATUS: "+status, "INFO"); 
			
			if(status.equalsIgnoreCase("OK")){
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("SourceError")){
					if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("code")){
						codigoResp = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
						fEPCS.Debug("[TRX_createNotification] Código: "+codigoResp, "INFO");
					}
				}
				fEPCS.Debug("[TRX_createNotification] OK!", "INFO");
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
			}else{			
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("code")){
					codigoResp = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					fEPCS.Debug("[TRX_createNotification] Código: "+codigoResp, "INFO");
					parametros_marcas_navegacion.put("MSG","ERROR "+codigoResp);
				}
			}
		}
		//trx_respuesta = "OK";
    	result.put("trx_respuesta", trx_respuesta);
    	
    }catch(Exception ex){
    	fEPCS.Debug("[TRX_createNotification] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_createNotification] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_createNotification] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON=null;
    }
    
   
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>