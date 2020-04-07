<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    String codigoResp="";
    String trx_respuesta = "NOK";
    String sTrx_datos_respuesta;
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    JSONObject IndividualIdentification = null;
    JSONObject cliente_datos = new JSONObject();
    JSONObject ServiceRequest = null;
    JSONObject BillingAccount = null;
    JSONObject Asset = null;
    JSONObject IndividualName = null;
    
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
		
	 	String MSISDN = additionalParams.get("MSISDN");
		String type = "MSISDN";
		if(!MSISDN.equals("") && MSISDN.length()<11 && !MSISDN.startsWith("56")){
			MSISDN="56"+MSISDN;
		}
		
		String area = additionalParams.get("area");
		String CustomerAccountID = additionalParams.get("CustomerAccountID");
		String RUT = additionalParams.get("RUT");
		String billingID = additionalParams.get("billingID");
		String addressId = additionalParams.get("addressId");
		String billingCycle = additionalParams.get("billingCycle");
		String bscsCustomerId = additionalParams.get("bscsCustomerId");
		String processID = additionalParams.get("processID");		
		String sourceID = additionalParams.get("sourceID");
		String idLlamada = additionalParams.get("idLlamada");
		String mode = additionalParams.get("mode");
		String orderType = additionalParams.get("orderType");
		String subArea = additionalParams.get("subArea");
		String serviceIdExt = additionalParams.get("serviceIdExt");					 

		fEPCS.Debug("[TRX_createProductOrder] cliente_datos:"+cliente_datos, "INFO");
		
    	fEPCS.Debug("[TRX_createProductOrder] INICIO", "INFO");
    	
    	fEPCS.Debug("[TRX_createProductOrder] area: "+area, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] CustomerAccountID: "+CustomerAccountID, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] RUT: "+RUT, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] billingID: "+billingID, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] addressId: "+addressId, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] billingCycle: "+billingCycle, "INFO"); 
    	fEPCS.Debug("[TRX_createProductOrder] bscsCustomerId: "+bscsCustomerId, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] processID: "+processID, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] IDllamada: "+idLlamada, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] MSISDN: "+MSISDN, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] mode: "+mode, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] orderType: "+orderType, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] subArea: "+subArea, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] serviceIdExt: "+serviceIdExt, "INFO");
    	                                             
    	String description = "";
    	String status = "";
    	String codeCanonical = "";
    	                           
        sTrx_datos_respuesta=fEPCS.CreateProductOrder(CustomerAccountID,billingID,addressId,bscsCustomerId,RUT,billingCycle,area,mode,orderType,subArea,"requester",serviceIdExt,idLlamada,processID,sourceID);
    	fEPCS.Debug("[TRX_getProductOrder] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
		respJSON = new JSONObject(sTrx_datos_respuesta); 
  
		
		if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("[TRX_createProductOrder] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("CreateProductOrder_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("CreateProductOrder_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("[TRX_createProductOrder] Fault STATUS: "+status, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("[TRX_createProductOrder] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("[TRX_createProductOrder] STATUS: "+status, "INFO");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("[TRX_createProductOrder] DESCRIPTION: "+description, "INFO"); 
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").isNull("description")){
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
					codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
					fEPCS.Debug("[TRX_createProductOrder] Canonical code: "+codeCanonical, "INFO");
					fEPCS.Debug("[TRX_createProductOrder] Canonical Description: "+description, "INFO"); 
				}
			}
			if(status.equalsIgnoreCase("OK")){ 
				String CustomerOrderItemID = "";
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
				String shoppingCartID = respJSON.getJSONObject("Body").getJSONObject("CustomerOrder").getString("shoppingCartID");
				JSONArray CustomerOrderItems = respJSON.getJSONObject("Body").optJSONObject("CustomerOrder").optJSONArray("CustomerOrderItem");
				if(CustomerOrderItems != null){
					if(CustomerOrderItems.length()>0){
						for(int x =0; x<CustomerOrderItems.length(); x++) {
							if(CustomerOrderItems.optJSONObject(x).optJSONObject("ProductOffering").optString("ID").equals("PO_BSC_MOVILES_POSTPAID")){
								CustomerOrderItemID = CustomerOrderItems.optJSONObject(x).optString("ID");
								break;
							}
						}
					}
				}
				
				
				fEPCS.Debug("[TRX_createProductOrder] shoppingCartID: "+shoppingCartID, "INFO");
				result.put("shoppingCartID", shoppingCartID);
				result.put("CustomerOrderItemID", CustomerOrderItemID);
				
			}else{
				
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("code")){
					codigoResp = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					fEPCS.Debug("[TRX_createProductOrder] Código: "+codigoResp, "INFO");
					parametros_marcas_navegacion.put("MSG","ERROR "+codigoResp);
				}
				fEPCS.Debug("[TRX_createProductOrder] Error. Código: "+codigoResp, "INFO");
			}
		} 

    }catch(Exception ex){
    	fEPCS.Debug("[TRX_createProductOrder] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	result.put("trx_respuesta", trx_respuesta);
    	ServiceRequest = null;
        IndividualIdentification = null;
        BillingAccount = null;
        Asset = null;
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_createProductOrder] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_createProductOrder] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON=null;
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