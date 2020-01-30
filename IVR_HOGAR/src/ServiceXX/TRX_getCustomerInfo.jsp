<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {
	String jspName="TRX_GetCustomerInfo";
    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    JSONObject cliente_datos = new JSONObject(); 
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	//String PCS = additionalParams.get("PCS");
    	String PCS = state.getString("ANI");
		if(PCS.length()<11 && !PCS.startsWith("56")){
			PCS="56"+PCS;
		}
    	String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	String ID = additionalParams.get("ID");
		 	
    	fEPCS.Debug("["+jspName+"] INICIO", "INFO");
    	fEPCS.Debug("["+jspName+"] PCS: "+PCS, "INFO");    	
    	fEPCS.Debug("["+jspName+"] processID: "+processID, "INFO");
    	fEPCS.Debug("["+jspName+"] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("["+jspName+"] idLlamada: "+idLlamada, "INFO");
    	fEPCS.Debug("["+jspName+"] ID: "+ID, "INFO");
    	fEPCS.Debug("["+jspName+"] parametros_marcas_navegacion: "+parametros_marcas_navegacion, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_CUSTOMERINFO");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
    	
    	String trx_respuesta = "NOK"; 
    	String description = ""; 
    	String codeCanonical = "";
    	String status = "";
    	 
    	String sTrx_datos_respuesta="";
    	if(ID.equals("PCS")){
    		sTrx_datos_respuesta=fEPCS.GetCustomerInfo(PCS, idLlamada,processID, sourceID);
    	}else{
    		sTrx_datos_respuesta=fEPCS.GetCustomerInfoRUT(cliente_datos.getJSONObject("IndividualIdentification"), idLlamada,processID, sourceID);
    	}
    	fEPCS.Debug("["+jspName+"] sTrx_datos_respuesta: "+ sTrx_datos_respuesta, "INFO");
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	
    	if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("["+jspName+"] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("GetCustomerInfo_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("GetCustomerInfo_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
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
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
					codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
					fEPCS.Debug("["+jspName+"] Canonical code: "+codeCanonical, "INFO");
					fEPCS.Debug("["+jspName+"] Canonical Description: "+description, "INFO"); 
				}
			}
			
			if(status.equalsIgnoreCase("OK")){ 
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
				if(respJSON.has("Body")){
					if(respJSON.getJSONObject("Body").has("CustomerAccount")){
						if(ID.equals("PCS")){
							if(respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).has("Individual")){
								if(respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).getJSONObject("Individual").has("IndividualIdentification")){
									JSONObject IndividualIdentification = respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).getJSONObject("Individual").getJSONObject("IndividualIdentification");
									cliente_datos.put("IndividualIdentification",IndividualIdentification);
									fEPCS.Debug("["+jspName+"] INDIVIDUAL IDENTIFICATION: "+IndividualIdentification, "INFO");
								}
							}
							
							if(respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).has("Asset")){
								if(respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).getJSONObject("Asset").has("Contract")){
									if(respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).getJSONObject("Asset").getJSONObject("Contract").has("planType")){
										String mercado = respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).getJSONObject("Asset").getJSONObject("Contract").getString("planType");
										fEPCS.Debug("["+jspName+"] MERCADO: "+mercado, "INFO");
										cliente_datos.put("mercadoANI",mercado);
									}
								}
							}
						}else{
							JSONArray mercados = new JSONArray();
							for(int i=0;i<respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").length();i++){
								if(respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(i).has("Asset")){
									JSONObject asset = respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(i).getJSONObject("Asset");
									if(asset.has("Contract") && asset.has("MSISDN")){
										if(asset.getJSONObject("Contract").has("planType") && asset.getJSONObject("MSISDN").has("SN")){
											String mercado = asset.getJSONObject("Contract").getString("planType");
											String msisdn = asset.getJSONObject("MSISDN").getString("SN");
											fEPCS.Debug("["+jspName+"] MERCADO: "+mercado, "INFO");
											fEPCS.Debug("["+jspName+"] MSISDN: "+msisdn, "INFO");
											if(!mercado.equals("PP") && !mercado.equals("CC") && !mercado.equals("SS")){//Se omiten los MSISDN Mobile
												JSONObject objMercado = new JSONObject();
												objMercado.put("mercado",mercado);
												objMercado.put("msisdn",msisdn);
												mercados.put(objMercado);
												objMercado = null;
											}
										}
									}
								}
							}
							cliente_datos.put("mercados",mercados);
							mercados = null;
						}
					}
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
		result.put("cliente_datos", cliente_datos);
	
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