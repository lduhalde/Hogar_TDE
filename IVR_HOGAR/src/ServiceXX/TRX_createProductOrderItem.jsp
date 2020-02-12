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
    String description = "";
	String codeCanonical = "";
	
    try{
		String CA_ID = additionalParams.get("CA_ID");
		String PO_ID = additionalParams.get("PO_ID"); 
		String action = additionalParams.get("action");
		String IS_Activacion = additionalParams.get("IS_Activacion");
		String shoppingCartID = additionalParams.get("shoppingCartID"); 
    	String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	String roleRelated = "ReliesOn";
		
    	fEPCS.Debug("[TRX_createProductOrderItem] INICIO", "INFO");
    	fEPCS.Debug("[TRX_createProductOrderItem] CustomerAccountID: "+CA_ID, "INFO"); 
    	fEPCS.Debug("[TRX_createProductOrderItem] PO_ID: "+PO_ID, "INFO"); 
    	fEPCS.Debug("[TRX_createProductOrderItem] Role: "+roleRelated, "INFO"); 
    	fEPCS.Debug("[TRX_createProductOrderItem] shoppingCartID: "+shoppingCartID, "INFO");
    	fEPCS.Debug("[TRX_createProductOrderItem] action: "+action, "INFO");
    	fEPCS.Debug("[TRX_createProductOrderItem] processID: "+processID, "INFO");
    	fEPCS.Debug("[TRX_createProductOrderItem] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_createProductOrderItem] idLlamada: "+idLlamada, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_PRODUCTORDERITEM");
    	parametros_marcas_navegacion.put("DATA","CREATE");
    	parametros_marcas_navegacion.put("RC","99");
    	
    	JSONArray Resource = null;
    	 
    	String biType="ProductOfferingOrder";
    	String sTrx_datos_respuesta=fEPCS.CreateProductOrderItem(PO_ID,CA_ID,shoppingCartID,action,Resource,biType,roleRelated,idLlamada,processID,sourceID);
    	fEPCS.Debug("[TRX_createProductOrderItem] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
    	String status = "";
		respJSON = new JSONObject(sTrx_datos_respuesta);
		if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("[TRX_createProductOrderItem] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("CreateProductOrderItem_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("CreateProductOrderItem_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("[TRX_createProductOrderItem] Fault STATUS: "+status, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("[TRX_createProductOrderItem] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("[TRX_createProductOrderItem] STATUS: "+status, "INFO");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("[TRX_createProductOrderItem] DESCRIPTION: "+description, "INFO"); 
				//if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").isNull("description")){
				//	description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
				//	codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
				//	fEPCS.Debug("[TRX_createProductOrderItem] Canonical code: "+codeCanonical, "INFO");
				//	fEPCS.Debug("[TRX_createProductOrderItem] Canonical Description: "+description, "INFO"); 
				//}
			}
			if(status.equalsIgnoreCase("OK")){ 
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
				String customerOrderItemID = respJSON.getJSONObject("Body").getJSONArray("CustomerOrderItem").getJSONObject(0).getString("ID");
				fEPCS.Debug("[TRX_createProductOrderItem] customerOrderItemID: "+customerOrderItemID, "INFO");
				result.put("customerOrderItemID", customerOrderItemID);
			}else{

				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("code")){
					codigoResp = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					fEPCS.Debug("[TRX_createProductOrderItem] Código: "+codigoResp, "INFO");
				}
				fEPCS.Debug("[TRX_createProductOrderItem] Error. Código: "+codigoResp, "INFO");
			}
		} 
    	
   
    }catch(Exception ex){
    	fEPCS.Debug("[TRX_createProductOrderItem] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_createProductOrderItem] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("trx_respuesta", trx_respuesta);
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_createProductOrderItem] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON = null;
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