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
    
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    String trx_respuesta = "NOK";
    JSONObject BillingAccount = null;
    JSONObject cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    try{
		
		String ItemServiceID = additionalParams.get("ItemServiceID");
		String shoppingCartID = additionalParams.get("shoppingCartID");
		String ServiceID = additionalParams.get("ServiceID");
		String serviceSpecification = additionalParams.get("serviceSpecification"); 
    	String srvCharacteristics_name = additionalParams.get("srvCharacteristics_name");
    	String srvCharacteristics_value = additionalParams.get("srvCharacteristics_value");
    	String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	
    	
    	fEPCS.Debug("[TRX_updateProductOrderItem] INICIO", "INFO"); 
    	fEPCS.Debug("[TRX_updateProductOrderItem] ItemServiceID: "+ItemServiceID, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrderItem] shoppingCartID: "+shoppingCartID, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrderItem] ServiceID: "+ServiceID, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrderItem] serviceSpecification: "+serviceSpecification, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrderItem] srvCharacteristics_name: "+srvCharacteristics_name, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrderItem] srvCharacteristics_value: "+srvCharacteristics_value, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrderItem] processID: "+processID, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrderItem] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_updateProductOrderItem] idLlamada: "+idLlamada, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_PRODUCTORDERITEM");
    	parametros_marcas_navegacion.put("DATA","UPDATE");
    	parametros_marcas_navegacion.put("RC","99");
    	String description = "";
    	String codeCanonical = "";
    	
//     	JSONArray Resource = null;
//     	if(IS_Activacion.equals("SI")){
//     		action = null;
// 	    	String resource = "[{ " +  
// 					"         \"resourceCharacteristics\":[  " + 
// 					"            {  " + 
// 					"               \"name\":\"resourceNumber\"," + 
// 					"               \"value\":\""+MSISDN+"\"" + 
// 					"            }" + 
// 					"         ]," + 
// 					"         \"ResorceSpecification\":{" + 
// 					"			\"ID\" : \"LRS_MSISDN_POSTPAID\"" + 
// 					"		}}," + 
// 					"      {\"resourceCharacteristics\":[  " + 
// 					"            {  " + 
// 					"               \"name\":\"serialNumber\"," + 
// 					"               \"value\":\""+ICCID+"\"" + 
// 					"            }]," + 
// 					"        \"ResorceSpecification\":{" + 
// 					"			\"ID\":\"PRS_SIM_POS\"" + 
// 					"		}}]";
// 	    	Resource = new JSONArray(resource);
//     	}
    	
//     	JSONObject PO = null;
//     	if(IS_Bloqueos.equals("SI")){
//     		PO = new JSONObject();
//     		JSONObject Product = new JSONObject();
//     		JSONObject ProductSpe = new JSONObject(); 
//     		JSONArray ArrproductSpecCharacteristic = new JSONArray();
//     		if(!BillingAccount.isNull("BillingAccountProfile") && !BillingAccount.isNull("BillingAccount")){
//     			if(!BillingAccount.getJSONObject("BillingAccount").optString("billingID").equals("")){
// 		    		JSONObject productSpecCharacteristic = new JSONObject();
// 		    		productSpecCharacteristic.put("name", "billingID");		
// 		    		productSpecCharacteristic.put("classification", "CONF");		
// 		    		productSpecCharacteristic.put("value", BillingAccount.getJSONObject("BillingAccount").getString("billingID"));		
// 		    		ArrproductSpecCharacteristic.put(productSpecCharacteristic);	
//     			}
//     			if(!BillingAccount.getJSONObject("BillingAccountProfile").optString("billingProfileId").equals(""))	{
// 		    		JSONObject productSpecCharacteristicBillingProfileId = new JSONObject();
// 		    		productSpecCharacteristicBillingProfileId.put("name","BillingProfileId");
// 		    		productSpecCharacteristicBillingProfileId.put("classification","CONF");
// 		    		productSpecCharacteristicBillingProfileId.put("value",BillingAccount.getJSONObject("BillingAccountProfile").optString("billingProfileId"));
// 		    		ArrproductSpecCharacteristic.put(productSpecCharacteristicBillingProfileId); 
// 	    		}
//     		}
//     		ProductSpe.put("ProductSpecCharacteristic",ArrproductSpecCharacteristic);
//     		Product.put("ProductSpecification",ProductSpe);
//     		PO.put("Product",Product);
//     	}
    	 
    	String status="";
    	                                  
    	String sTrx_datos_respuesta=fEPCS.UpdateProductOrderItem(shoppingCartID, ItemServiceID, ServiceID, serviceSpecification, srvCharacteristics_name, srvCharacteristics_value, idLlamada, processID, sourceID);
    	fEPCS.Debug("[TRX_updateProductOrderItem] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
    	
		respJSON = new JSONObject(sTrx_datos_respuesta);
		if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("[TRX_updateProductOrderItem] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("UpdateProductOrderItem_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("UpdateProductOrderItem_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("[TRX_updateProductOrderItem] Fault STATUS: "+status, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("[TRX_updateProductOrderItem] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("[TRX_updateProductOrderItem] STATUS: "+status, "INFO");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("[TRX_updateProductOrderItem] DESCRIPTION: "+description, "INFO"); 
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").isNull("description")){
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
					codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
					fEPCS.Debug("[TRX_updateProductOrderItem] Canonical code: "+codeCanonical, "INFO");
					fEPCS.Debug("[TRX_updateProductOrderItem] Canonical Description: "+description, "INFO"); 
				}
			}
			
			if(status.equalsIgnoreCase("OK")){ 
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
				String customerOrderItemID = respJSON.getJSONObject("Body").getJSONArray("CustomerOrderItem").getJSONObject(0).getString("ID");
				fEPCS.Debug("[TRX_updateProductOrderItem] customerOrderItemID: "+customerOrderItemID, "INFO");
				result.put("customerOrderItemID", customerOrderItemID);
			}else{

				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("code")){
					codigoResp = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					fEPCS.Debug("[TRX_updateProductOrderItem] Código: "+codigoResp, "INFO");
				}
				fEPCS.Debug("[TRX_updateProductOrderItem] Error. Código: "+codigoResp, "INFO");
			}
		} 
     
    }catch(Exception ex){
    	fEPCS.Debug("[TRX_updateProductOrderItem] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_updateProductOrderItem] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("trx_respuesta", trx_respuesta);
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_updateProductOrderItem] FIN result: "+result.toString(), "INFO");
    	
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