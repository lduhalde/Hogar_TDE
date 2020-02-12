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
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    JSONObject GeographicAddress = null;
    
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
		
	 	String MSISDN = additionalParams.get("MSISDN");
		String type = "MSISDN";
		if(!MSISDN.equals("") && MSISDN.length()<11 && !MSISDN.startsWith("56")){
			MSISDN="56"+MSISDN;
		}
		String areaActivacion = fEPCS.Params.GetValue("areaActivacion_CreateProductOrder", "Activación de Línea");
		String CA_ID=additionalParams.get("CustomerAccountID");
		String RUT = additionalParams.get("RUT");
		String area = additionalParams.get("area"); 
		String ServiceRequestID = additionalParams.get("ServiceRequestID");
		fEPCS.Debug("[TRX_createProductOrder] cliente_datos:"+cliente_datos, "INFO");
		IndividualIdentification =  !cliente_datos.isNull("IndividualIdentification") ? (cliente_datos.getJSONObject("IndividualIdentification")) : new JSONObject();
		BillingAccount = (!cliente_datos.isNull("BillingAccountPO") ) ? cliente_datos.getJSONObject("BillingAccountPO") : new JSONObject();
		String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
		
    	fEPCS.Debug("[TRX_createProductOrder] INICIO", "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] MSISDN: "+MSISDN, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] ServiceRequestID: "+ServiceRequestID, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] CustomerAccountID: "+CA_ID, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] RUT: "+RUT, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] AREA: "+area, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] IndividualIdentification: "+IndividualIdentification, "INFO"); 
    	fEPCS.Debug("[TRX_createProductOrder] BillingAccount: "+BillingAccount, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] processID: "+processID, "INFO");
    	fEPCS.Debug("[TRX_createProductOrder] sourceID: "+sourceID, "INFO");
    	
    	 
    	String description = "";
    	String codeCanonical = "";
    	String number=MSISDN;
    	if(RUT.length()>6){
    		//Formatear el RUT de colocando un "-" guion medio antes del DV
    		RUT=RUT.substring(0,RUT.length()-1)+"-"+RUT.substring(RUT.length()-1);
    		number=RUT;
    		type="1";
    	}
    	
    	if(area.equalsIgnoreCase(areaActivacion)){
    		IndividualName = new JSONObject(); 
			IndividualName.put("firstName", cliente_datos.optJSONObject("IndividualIdentificationTransunion").optJSONObject("Individual").optString("firstName"));
			IndividualName.put("lastName",  cliente_datos.optJSONObject("IndividualIdentificationTransunion").optJSONObject("Individual").optString("lastName"));
			GeographicAddress = cliente_datos.optJSONObject("GeographicAddress");
			if(GeographicAddress != null) {
				GeographicAddress.getJSONObject("Address").put("streetNameFull", GeographicAddress.getJSONObject("Address").getJSONObject("StreetNameFull"));
				GeographicAddress.getJSONObject("Address").remove("StreetNameFull");
			}		
    	}
    	if(IndividualIdentification == null || IndividualIdentification.equals("undefined")){
    		IndividualIdentification = new JSONObject();
    		IndividualIdentification.put("number",number);
    		IndividualIdentification.put("type",type); 
    		fEPCS.Debug("[TRX_createProductOrder] SIN IndividualIdentification", "INFO");  
    	}
    	
    	
    	ServiceRequest = new JSONObject();
    	ServiceRequest.put("ID",ServiceRequestID);
    	
    	String mode = "NON_INTERACTIVE";
    	String orderType = "Orden";
    	String subArea = "Móvil - Postpago";
    	String requester = CA_ID+"-"+idLlamada;
    	sTrx_datos_respuesta=fEPCS.CreateProductOrder(MSISDN,BillingAccount,CA_ID,IndividualIdentification,ServiceRequest,area,mode,orderType,subArea,requester,IndividualName,GeographicAddress,idLlamada,processID,sourceID);
    	String status = "";
    	fEPCS.Debug("[TRX_createProductOrder] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
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