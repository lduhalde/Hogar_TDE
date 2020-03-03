<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject(); 
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    String jspName="TRX_validateRequestProductOrder";
    try{
    	String idLlamada = state.getString("idLlamada");
		String ani = additionalParams.get("ANI");
		String ICCID = additionalParams.get("ICCID"); 
		String area = additionalParams.get("area");
		String subArea = additionalParams.get("subArea");
		String orderType = additionalParams.get("orderType");
		String processID = additionalParams.get("processID");
		String sourceID = additionalParams.get("sourceID");	
		String code = "";
		
    	fEPCS.Debug("["+jspName+"] INICIO", "INFO");
    	fEPCS.Debug("["+jspName+"] ANI: "+ani, "INFO");
    	fEPCS.Debug("["+jspName+"] ICCID: "+ICCID, "INFO"); 
    	fEPCS.Debug("["+jspName+"] area: "+area, "INFO");
    	fEPCS.Debug("["+jspName+"] subArea: "+subArea, "INFO");
    	fEPCS.Debug("["+jspName+"] orderType: "+orderType, "INFO");
    	fEPCS.Debug("["+jspName+"] processID: "+processID, "INFO");
    	fEPCS.Debug("["+jspName+"] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("["+jspName+"] idLlamada: "+idLlamada, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_REQUESTPRODUCTORDER");
    	parametros_marcas_navegacion.put("DATA","VALIDATE");
    	parametros_marcas_navegacion.put("RC","99");
    	String trx_respuesta = "OK"; //Valor por defecto NOK 
    	String codigo = "";
       	String status = "";
       	String description = "";
    	String sTrx_datos_respuesta=fEPCS.ValidateRequestProductOrder("56"+ani,ICCID,idLlamada,orderType, area,subArea,processID,sourceID);
    	fEPCS.Debug("["+jspName+"] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
    	
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("["+jspName+"] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("ValidateRequestProductOrder_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("ValidateRequestProductOrder_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("["+jspName+"] Fault STATUS: "+status, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("["+jspName+"] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("["+jspName+"] STATUS: "+status, "INFO");
			
			String codeCanonical = "";
			if(respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").has("CanonicalError")){
				codeCanonical=respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").isNull("description")){
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
					fEPCS.Debug("["+jspName+"] Canonical Description: "+description, "INFO");
				}
			}
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("description")){
				codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
				description =  respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("description");
				result.put("CODE", codigo);
				fEPCS.Debug("["+jspName+"] SourceError code: "+codigo, "INFO");
				fEPCS.Debug("["+jspName+"] SourceError Description: "+description, "INFO");
			} 
			
			fEPCS.Debug("["+jspName+"] Canonical code: "+codeCanonical, "INFO");
			
			
			if(status.equalsIgnoreCase("OK")){ 
				parametros_marcas_navegacion.put("RC","0");
				trx_respuesta = "OK";
				if(!codeCanonical.equals("A3") && !codeCanonical.equals("")){
					trx_respuesta = "OK-1";
					result.put("ordenAbierta", "SI");
				}
			}else{
				parametros_marcas_navegacion.put("MSG","ERROR "+codigo);
			}
		} 
    	result.put("trx_respuesta", trx_respuesta); 
    	fEPCS.Debug("["+jspName+"] FIN trx_respuesta: "+trx_respuesta, "INFO");
    	
    }catch(Exception ex){
    	fEPCS.DebugError("["+jspName+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+jspName+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+jspName+"] FIN result: "+result.toString(), "INFO");
    	
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