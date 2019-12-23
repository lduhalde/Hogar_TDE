<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    JSONObject IndividualIdentification = new JSONObject();
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    String sJSPName="TRX_GetCustomerInfoRUT";
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	IndividualIdentification =  !cliente_datos.isNull("IndividualIdentification")  ? (cliente_datos.getJSONObject("IndividualIdentification")) : new JSONObject();
    	String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
		 	
    	fEPCS.Debug("["+sJSPName+"] INICIO", "INFO");
    	fEPCS.Debug("["+sJSPName+"] RUT: "+IndividualIdentification.toString(), "INFO");    	
    	fEPCS.Debug("["+sJSPName+"] processID: "+processID, "INFO");
    	fEPCS.Debug("["+sJSPName+"] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("["+sJSPName+"] idLlamada: "+idLlamada, "INFO");
    	fEPCS.Debug("["+sJSPName+"] parametros_marcas_navegacion: "+parametros_marcas_navegacion, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_CUSTOMERINFO");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
    	
    	String trx_respuesta = "NOK"; 
    	String description = ""; 
    	String codeCanonical = "";
    	String mercado= "";
    	int CantidadlineasActivas= 0;
    	String status = "";
    	 
    	String sTrx_datos_respuesta=fEPCS.GetCustomerInfoRUT(IndividualIdentification, idLlamada,processID, sourceID);
    	fEPCS.Debug("["+sJSPName+"] sTrx_datos_respuesta: "+ sTrx_datos_respuesta, "INFO");
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	
    	if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("["+sJSPName+"] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("GetCustomerInfo_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("GetCustomerInfo_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("["+sJSPName+"] Fault STATUS: "+status, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("["+sJSPName+"] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("["+sJSPName+"] STATUS: "+status, "INFO");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("["+sJSPName+"] DESCRIPTION: "+description, "INFO"); 
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("CanonicalError")){
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
					codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
					fEPCS.Debug("["+sJSPName+"] Canonical code: "+codeCanonical, "INFO");
					fEPCS.Debug("["+sJSPName+"] Canonical Description: "+description, "INFO"); 
				}
			}
			
			if(status.equalsIgnoreCase("OK")){ 
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
				if(!respJSON.isNull("Body")){
					if(!respJSON.getJSONObject("Body").isNull("CustomerAccount")){
						JSONArray CustomerAccount = respJSON.getJSONObject("Body").getJSONArray("CustomerAccount");
						mercado = respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).getJSONObject("Asset").getJSONObject("Contract").getString("planType");
						for(int i=0; i<CustomerAccount.length();i++){
							JSONObject BillingAccount = CustomerAccount.getJSONObject(i);
							fEPCS.Debug("["+sJSPName+"] BillingAccount:"+BillingAccount.toString(), "INFO");
							if(BillingAccount.getJSONObject("Asset").getJSONObject("Contract").getString("planType").equals("SS") || BillingAccount.getJSONObject("Asset").getJSONObject("Contract").getString("planType").equals("CC")){
								CantidadlineasActivas=CantidadlineasActivas+1;
							}
						}
					}
				}
			}else{ ///Error Controlado
				String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("description");
				result.put("CODE", codigo);
				fEPCS.Debug("["+sJSPName+"] Source Code: "+codigo, "INFO");
				fEPCS.Debug("["+sJSPName+"] Source Description: "+description, "INFO");
				parametros_marcas_navegacion.put("MSG","ERROR "+codigo);		
			}
		}
    	fEPCS.Debug("["+sJSPName+"] CantidadlineasActivas: "+CantidadlineasActivas, "INFO");
    	cliente_datos.put("CantidadlineasActivas",CantidadlineasActivas);
    	result.put("trx_respuesta", trx_respuesta); 
		result.put("cliente_datos", cliente_datos);
	
    }catch(Exception ex){
    	fEPCS.Debug("["+sJSPName+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+sJSPName+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+sJSPName+"] FIN result: "+result.toString(), "INFO");
    	
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