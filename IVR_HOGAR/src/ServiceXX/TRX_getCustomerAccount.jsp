<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {
	String jspName="TRX_getCustomerAccount";
    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
 	JSONObject Indentification = null;
 	JSONArray GeographicalAddressResponse = new JSONArray();
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));
    String VIPExecutiveDocument="";
    String CustomerAccountID="";
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
    	fEPCS.Debug("["+jspName+"] cliente_datos: "+cliente_datos, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_CUSTOMERACCOUNT");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
    	JSONObject BillingAccountPO = new JSONObject();
    	String trx_respuesta = "NOK";
		String segmento="";
		String description = "";
		String codeCanonical = "";
		
	    String PCS= null;
	    if(cliente_datos.has("IndividualIdentification")){
	    	Indentification= cliente_datos.getJSONObject("IndividualIdentification"); 
	    }else if(cliente_datos.has("RUT")){
	    	Indentification= new JSONObject(); 
	    	Indentification.put("number",cliente_datos.getString("RUT"));
	    	Indentification.put("type","RUT");
	    }else{
	    	fEPCS.Debug("["+jspName+"] SIN IndividualIndentification y SIN RUT", "INFO"); 
	    }
	    
	    fEPCS.Debug("["+jspName+"] IndividualIndentification: "+Indentification, "INFO"); 
	    
		String status = "";
    	String sTrx_datos_respuesta=fEPCS.GetCustomerAccount(PCS,Indentification,idLlamada,processID,sourceID);
    
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("["+jspName+"] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("GetCustomerAccount_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("GetCustomerAccount_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
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
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("CanonicalError")){
					if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").isNull("description")){
						description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
						codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
						fEPCS.Debug("["+jspName+"] Canonical code: "+codeCanonical, "INFO");
						fEPCS.Debug("["+jspName+"] Canonical Description: "+description, "INFO"); 
					}
				}
			}
			 
			if(status.equalsIgnoreCase("OK")){// && codeCanonical.equals("A3")){ 
				trx_respuesta = "OK"; 
				JSONObject BillingAccount = new JSONObject();
				parametros_marcas_navegacion.put("RC","0");
				
				if(!respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").isNull("Contact")){
					VIPExecutiveDocument = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("Contact").getJSONObject(0).optString("VIPExecutiveDocument"); 
				}
				
				/*
				//No se recupera segmento, ya que no es de utilidad para Hogar
				if(!respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONObject("MarketSegment").isNull("class")){
					 segmento = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONObject("MarketSegment").getString("class"); 
				}*/
				if(!respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").isNull("accountType")){
					String accountType = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getString("accountType");
					cliente_datos.put("accountType", accountType);
				}
				if(!respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").isNull("customerType")){
					String customerType = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getString("customerType");
					cliente_datos.put("customerType", customerType);
				}
				String CA_ID = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getString("ID");
				if(CA_ID.length()>3){
					CustomerAccountID=CA_ID;	
				}
				
				
				
				if(!respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").isNull("GeographicalAddress")){
					JSONArray addressArray = new JSONArray();
					GeographicalAddressResponse = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("GeographicalAddress");
					for(int g=0; g<GeographicalAddressResponse.length();g++){
						JSONObject GeographicalAddress = GeographicalAddressResponse.getJSONObject(g);
						//fEPCS.Debug("["+jspName+"] GeographicalAddress : "+GeographicalAddress.toString(), "INFO");
						if(GeographicalAddress.has("Address")){
							if(GeographicalAddress.getJSONObject("Address").has("xygoAddressId") && GeographicalAddress.getJSONObject("Address").has("addressLine1") &&	GeographicalAddress.getJSONObject("Address").has("addressNumber")){
								JSONObject address = new JSONObject();
								address.put("addressId",GeographicalAddress.getJSONObject("Address").getString("xygoAddressId")); 
								address.put("address",GeographicalAddress.getJSONObject("Address").getString("addressLine1")+" "+GeographicalAddress.getJSONObject("Address").getString("addressNumber"));
								address.put("addressComumune",GeographicalAddress.getJSONObject("GeographicArea").getString("commune"));
								//fEPCS.Debug("["+jspName+"] Address : "+address.toString(), "INFO");
								addressArray.put(address);
							}else{
								fEPCS.Debug("["+jspName+"] DIRECCION INCOMPLETA", "INFO");
							}
						}
					}
					cliente_datos.put("addressArray",addressArray);
				    
				}
				
				
				JSONObject Invoice = new JSONObject();
				if(!respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").isNull("BillingAccount")){
					JSONArray ArrayBillingAccount = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("BillingAccount");
					JSONObject BillingAccountProfile = new JSONObject();
					if(ArrayBillingAccount.length()>0){
						for(int i=0; i<ArrayBillingAccount.length(); i++){
							BillingAccount = ArrayBillingAccount.getJSONObject(i);	
							if(BillingAccount.getString("productLine").equalsIgnoreCase("PostPago") && BillingAccount.getString("accountStatus").equalsIgnoreCase("Activo")){
								break;
							}
						}
					}  
					
						if(!BillingAccount.isNull("CustomerBillingCycle")){
							JSONArray Invoices = BillingAccount.getJSONObject("CustomerBillingCycle").optJSONArray("ComInvoiceProfile");
							if(Invoices!= null){
								if(Invoices.length()>0) {
									for(int x =0; x<Invoices.length(); x++) {
										if(Invoices.getJSONObject(x).optString("paymentType").equals("Postpago Hogar") && Invoices.getJSONObject(x).optString("billStatus").equalsIgnoreCase("Activo") ) {
											Invoice = Invoices.getJSONObject(x);
											break;
										}
									}
								}else {
									Invoice = Invoices.getJSONObject(0);
								}
							}
						}

					if(!BillingAccount.optString("billingId").equals("null") && !BillingAccount.optString("billingId").equals("")){
						JSONObject BA = new JSONObject();
						String billingID = BillingAccount.getString("billingId"); 
						BA.put("billingID", billingID);
						BA.put("externalID",CustomerAccountID);
						BA.put("type","Account");
						BA.put("billDocumentType","1"); 
						if(!BillingAccount.isNull("CustomerBillingCycle")){
							String billingCycle = "";
							if(!BillingAccount.getJSONObject("CustomerBillingCycle").isNull("ID") && !BillingAccount.getJSONObject("CustomerBillingCycle").getString("ID").equals("null")){
								billingCycle = BillingAccount.getJSONObject("CustomerBillingCycle").getString("ID");
								BA.put("billingCycle", billingCycle);
								cliente_datos.put("ConCicloFacturacion", "SI");
							}
							
						}
						if(!BillingAccount.optString("integrationId").equals("null") && !BillingAccount.optString("integrationId").equals("")){
							String bscsBillingAccountId = BillingAccount.getString("integrationId"); 
							BA.put("bscsBillingAccountId", billingID);
							BA.put("bscsCustomerId", BillingAccount.getString("integrationId"));
						}
						if(Invoice.length()>0){
							
							if(!Invoice.optString("billBrailleFlag").equals("null") && !Invoice.optString("billBrailleFlag").equals("")){
							 	BillingAccountProfile.put("billBrailleFlag", Invoice.opt("billBrailleFlag"));
							}
							if(!Invoice.optString("billStatus").equals("null") && !Invoice.optString("billStatus").equals("")){
						 		BillingAccountProfile.put("billStatus", Invoice.opt("billStatus"));
							}
							if(!Invoice.optString("billType").equals("null") && !Invoice.optString("billType").equals("")){
								BillingAccountProfile.put("billType", Invoice.opt("billType"));
							}
							if(!Invoice.optString("invoiceProfileId").equals("null") && !Invoice.optString("invoiceProfileId").equals("")){
								BillingAccountProfile.put("billingProfileId", Invoice.opt("invoiceProfileId"));
							}
							if(!Invoice.optString("currencyCode").equals("null") && !Invoice.optString("currencyCode").equals("")){
								BillingAccountProfile.put("currency", Invoice.opt("currencyCode"));
							}
							if(!Invoice.optString("mediaType").equals("null") && !Invoice.optString("mediaType").equals("")){
								BillingAccountProfile.put("mediaType",Invoice.opt("mediaType"));
							}
							if(!Invoice.optString("emailForSend").equals("null") && !Invoice.optString("emailForSend").equals("")){
								BillingAccountProfile.put("email", Invoice.opt("emailForSend"));
							}
							if(!Invoice.optString("paymentMethod").equals("null") && !Invoice.optString("paymentMethod").equals("")){
								BillingAccountProfile.put("paymentMethod",Invoice.opt("paymentMethod"));
							}
							if(!Invoice.optString("integrationId").equals("null") && !Invoice.optString("integrationId").equals("")){
								BillingAccountProfile.put("bscsBillingAccountId",Invoice.opt("integrationId"));
							}
						}
						String addressId = "";
						if(!respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").isNull("GeographicalAddress")){
							GeographicalAddressResponse = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("GeographicalAddress");
						    if(GeographicalAddressResponse.optJSONObject(0) != null) {
						    	int largo = GeographicalAddressResponse.length();
						    	addressId = GeographicalAddressResponse.getJSONObject(largo-1).optString("addressId");
						    	fEPCS.Debug("["+jspName+"] addressId : "+addressId, "INFO");
							    BillingAccountProfile.put("addressId", addressId); 
							    cliente_datos.put("CONDIRECCFACTURACION", "SI");
						    }
						}
					
						BillingAccountProfile.put("billDocumentType", "Boleta"); 
						BillingAccountProfile.put("type", "Bill"); 
						BillingAccountPO.put("BillingAccount", BA);
						BillingAccountPO.put("BillingAccountProfile", BillingAccountProfile);
						BillingAccountPO.put("externalID",billingID);
						BillingAccountPO.put("type","billing");
						BillingAccountPO.put("reference",billingID);

					} 
				}
				cliente_datos.put("BillingAccountPO", BillingAccountPO);
				cliente_datos.put("VIPExecutiveDocument", VIPExecutiveDocument);
			}else{ ///Error Controlado 
				
				if(status.equalsIgnoreCase("WARNING") && (codeCanonical.equals("A0")|| codeCanonical.equals("W13"))){
					cliente_datos.put("ClienteNuevo", "SI");
					cliente_datos.put("IndividualIdentification", Indentification);
					fEPCS.Debug("["+jspName+"] Indentification: "+Indentification, "INFO");
				}
				String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("description");
				result.put("CODE", codigo);
				fEPCS.Debug("["+jspName+"] Source Code: "+codigo, "INFO");
				fEPCS.Debug("["+jspName+"] Source Description: "+description, "INFO");
				parametros_marcas_navegacion.put("MSG","ERROR "+codigo);
			}
		}
  
    	result.put("trx_respuesta", trx_respuesta);
		fEPCS.Debug("["+jspName+"] SEGMENTO: "+segmento, "INFO");
		fEPCS.Debug("["+jspName+"] CustomerAccountID: "+CustomerAccountID,"INFO");

		fEPCS.Debug("["+jspName+"] BillingAccountPO: "+BillingAccountPO.toString(), "INFO");
		cliente_datos.put("BillingAccountPO",BillingAccountPO);
		cliente_datos.put("CustomerAccountID", CustomerAccountID);
		cliente_datos.put("segmento", segmento);
		cliente_datos.put("Var_PCS", PCS);
		result.put("cliente_datos", cliente_datos);
		fEPCS.Debug("["+jspName+"] cliente_datos: "+cliente_datos.toString(), "INFO");
	
    }catch(Exception ex){
    	fEPCS.Debug("["+jspName+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+jspName+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+jspName+"] FIN result: "+result.toString(), "INFO");
    	GeographicalAddressResponse = null;
    	parametros_marcas_navegacion = null;
    	respJSON = null;
    	cliente_datos=null; 
    	Indentification = null;
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