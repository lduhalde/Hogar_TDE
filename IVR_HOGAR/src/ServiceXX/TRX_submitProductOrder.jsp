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
    
    try{
		
		String shoppingCartID = additionalParams.get("shoppingCartID");
		String codeCanonical = "";
    	fEPCS.Debug("[TRX_SubmitProductOrder] INICIO", "INFO");
    	fEPCS.Debug("[TRX_SubmitProductOrder] shoppingCartID: "+shoppingCartID, "INFO");
    	
    	String processCode = additionalParams.get("processCode");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	fEPCS.Debug("[TRX_SubmitProductOrder] processCode: "+processCode, "INFO");
    	fEPCS.Debug("[TRX_SubmitProductOrder] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_SubmitProductOrder] idLlamada: "+idLlamada, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_PRODUCTORDER");
    	parametros_marcas_navegacion.put("DATA","SUBMIT");
    	parametros_marcas_navegacion.put("RC","99");
    	
    	
    	String trx_respuesta = "NOK";
    	String codigoResp="";
    	String sTrx_datos_respuesta=fEPCS.SubmitProductOrder(shoppingCartID,idLlamada,processCode, sourceID);
    	fEPCS.Debug("[TRX_SubmitProductOrder] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
    	
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	String status = "";
		String description = "";
    	if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("[TRX_SubmitProductOrder] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("SubmitProductOrder_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("SubmitProductOrder_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("[TRX_SubmitProductOrder] Fault STATUS: "+status, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("[TRX_SubmitProductOrder] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("[TRX_SubmitProductOrder] STATUS: "+status, "INFO");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("[TRX_SubmitProductOrder] DESCRIPTION: "+description, "INFO"); 
				//if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").isNull("description")){
				//	description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
				//	codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
				//	fEPCS.Debug("[TRX_SubmitProductOrder] Canonical code: "+codeCanonical, "INFO");
				//	fEPCS.Debug("[TRX_SubmitProductOrder] Canonical Description: "+description, "INFO"); 
				//}
			}
			 
			if(status.equalsIgnoreCase("OK")){ 
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
			}else{
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("code")){
					codigoResp = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					fEPCS.Debug("[TRX_SubmitProductOrder] Código: "+codigoResp, "INFO");
					parametros_marcas_navegacion.put("MSG","ERROR "+codigoResp);
				}
				fEPCS.Debug("[TRX_SubmitProductOrder] Error. Código: "+codigoResp, "INFO");
			}
		}
		//trx_respuesta = "OK";
    	result.put("trx_respuesta", trx_respuesta);
    	
    }catch(Exception ex){
    	fEPCS.Debug("[TRX_SubmitProductOrder] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_SubmitProductOrder] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_SubmitProductOrder] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON = null;
    }
    
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>