<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));
    
	String jspName = "TRX_getServiceRequest";
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
		 	
    	fEPCS.Debug("["+jspName+"] INICIO", "INFO");  	
    	fEPCS.Debug("["+jspName+"] processID: "+processID, "INFO");
    	fEPCS.Debug("["+jspName+"] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("["+jspName+"] idLlamada: "+idLlamada, "INFO");
    	fEPCS.Debug("["+jspName+"] parametros_marcas_navegacion: "+parametros_marcas_navegacion, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_SERVICEREQUEST");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
    	
    	
    	JSONObject body = new JSONObject();
    	JSONObject ServiceRequest = new JSONObject();
    	ServiceRequest.put("type","Orden");
    	ServiceRequest.put("area","Venta Servicios");
    	ServiceRequest.put("subArea","Hogar - Postpago");
    	ServiceRequest.put("state","Abierto");
    	ServiceRequest.put("IndividualIdentification", cliente_datos.getJSONObject("IndividualIdentification"));
		body.put("ServiceRequest", ServiceRequest);
    	
    	String trx_respuesta = "NOK"; 
    	String description = ""; 
    	String codeCanonical = "";
    	
    	String status = "";
    	String sTrx_datos_respuesta=fEPCS.GetServiceRequest(body,idLlamada,processID, sourceID);
    	
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("["+jspName+"] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("GetServiceRequest_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("GetServiceRequest_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("["+jspName+"] Fault STATUS: "+status, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("["+jspName+"] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("["+jspName+"] STATUS: "+status, "INFO");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("["+jspName+"] DESCRIPTION: "+description, "INFO"); 
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").isNull("description")){
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
					codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
					fEPCS.Debug("["+jspName+"] Canonical code: "+codeCanonical, "INFO");
					fEPCS.Debug("["+jspName+"] Canonical Description: "+description, "INFO"); 
				}
			}
			
			if(status.equalsIgnoreCase("OK")){ 
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
				//A0 sin datos
				
				if(codeCanonical.equals("A3")){
					trx_respuesta = "OK-AGENDA";
				}
			}else{ ///Error Controlado
				String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("description");
				result.put("CODE", codigo);
				fEPCS.Debug("["+jspName+"] Source Code: "+codigo, "INFO");
				fEPCS.Debug("["+jspName+"] Source Description: "+description, "INFO");
				parametros_marcas_navegacion.put("MSG","ERROR "+codigo);		
			}
		}
    	result.put("trx_respuesta", trx_respuesta);
	
    }catch(Exception ex){
    	fEPCS.Debug("["+jspName+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+jspName+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+jspName+"] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON = null;
    	cliente_datos=null;
    }
    
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>