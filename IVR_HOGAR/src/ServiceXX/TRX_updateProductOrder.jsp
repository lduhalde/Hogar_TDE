<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    String codigoResp="";
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    String trx_respuesta = "NOK";
    try{
		 
		String shoppingCartID = additionalParams.get("shoppingCartID"); 
    	String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada"); 
    	String status = additionalParams.get("status");
		
    	fEPCS.Debug("[TRX_updateProductOrder] INICIO", "INFO"); 
    	fEPCS.Debug("[TRX_updateProductOrder] shoppingCartID: "+shoppingCartID, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrder] status: "+status, "INFO"); 
    	fEPCS.Debug("[TRX_updateProductOrder] processID: "+processID, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrder] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrder] idLlamada: "+idLlamada, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_PRODUCTORDER");
    	parametros_marcas_navegacion.put("DATA","UPDATE");
    	parametros_marcas_navegacion.put("RC","99");
    	String estado = "";
    	String sTrx_datos_respuesta=fEPCS.UpdateProductOrder(shoppingCartID,status,idLlamada,processID SourceID);
    	fEPCS.Debug("[TRX_updateProductOrder] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
    	
		respJSON = new JSONObject(sTrx_datos_respuesta); 
		if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("[TRX_updateProductOrder] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("UpdateProductOrder_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("UpdateProductOrder_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("[TRX_updateProductOrder] Fault STATUS: "+estado, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("[TRX_updateProductOrder] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			estado = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			String codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
			
			fEPCS.Debug("[TRX_updateProductOrder] STATUS: "+estado, "INFO");
			fEPCS.Debug("[TRX_updateProductOrder] Canonical code: "+codeCanonical, "INFO");
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").isNull("description")){
				String description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("[TRX_updateProductOrder] DESCRIPTION: "+description, "INFO"); 
				String description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
				fEPCS.Debug("[TRX_updateProductOrder] Canonical Description: "+description, "INFO");
				
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("description")){
					codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					description =  respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("description");
					result.put("CODE", codigo);
					fEPCS.Debug("[TRX_updateProductOrder] SourceError code: "+codigo, "INFO");
					fEPCS.Debug("[TRX_updateProductOrder] SourceError Description: "+description, "INFO");
				} 
			}
			if(status.equalsIgnoreCase("OK")){ 
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
			}else{

				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("code")){
					codigoResp = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					fEPCS.Debug("[TRX_updateProductOrder] Código: "+codigoResp, "INFO");
				}
				fEPCS.Debug("[TRX_updateProductOrder] Error. Código: "+codigoResp, "INFO");
			}
		} 
    	
    }catch(Exception ex){
    	fEPCS.Debug("[TRX_updateProductOrder] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_updateProductOrder] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("trx_respuesta", trx_respuesta);
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_updateProductOrder] FIN result: "+result.toString(), "INFO");
    	
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