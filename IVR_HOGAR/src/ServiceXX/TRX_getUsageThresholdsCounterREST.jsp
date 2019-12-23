<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {
	String jspName = "TRX_getUsageThresholdsCounterREST";
    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    
    String cobro="SI";
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	JSONObject cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	String ani = additionalParams.get("PCS");
    	ani = (ani.length() < 11 ) ? "56"+ani : ani;
		
    	fEPCS.Debug("["+jspName+"] INICIO", "INFO");
    	fEPCS.Debug("["+jspName+"] ANI: "+ani, "INFO");
    	
    	String processCode = additionalParams.get("processCode");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	String productID = additionalParams.get("productID");
    	fEPCS.Debug("["+jspName+"] processCode: "+processCode, "INFO");
    	fEPCS.Debug("["+jspName+"] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("["+jspName+"] idLlamada: "+idLlamada, "INFO");
    	fEPCS.Debug("["+jspName+"] productID: "+productID, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_USAGETHRESHOLDSCOUNTER");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
    	
    	String trx_respuesta = "NOK";
    	
    	String sTrx_datos_respuesta=fEPCS.getUsageThresholdCounterREST(ani,productID,idLlamada,processCode,sourceID);
  
    	fEPCS.Debug("["+jspName+"] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
    	
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("["+jspName+"] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			String status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("["+jspName+"] STATUS: "+status, "INFO");
			
			if(status.equalsIgnoreCase("OK")){ 
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
				if(!respJSON.isNull("Body")){
					if(respJSON.getJSONObject("Body").has("Product")){
						JSONArray arrayProduct = respJSON.getJSONObject("Body").getJSONArray("Product");
						for(int i=0; i<arrayProduct.length(); i++){
							JSONObject jo = (JSONObject) arrayProduct.get(i);
							String id = jo.getString("ID");
							//int ProductConfiguration = jo.getJSONObject("ProductConfiguration").getInt("amount");
							int ProductUsage = jo.getJSONObject("ProductUsage").getInt("amount");
							//fEPCS.Debug("["+jspName+"] ProductConfiguration "+ProductConfiguration, "INFO");
							fEPCS.Debug("["+jspName+"] ProductUsage "+ProductUsage, "INFO"); 
							if(id.equals(productID)){
								if(ProductUsage<1){
									cobro="NO";
									cliente_datos.put("SinCobroOtroFlujo","SI");
								}
							}
						}
					}
				}else{
					cobro="NO";
					fEPCS.Debug("["+jspName+"] BODY NULL - TRANSFERENCIA GRATIS ", "INFO"); 
					cliente_datos.put("SinCobroOtroFlujo","SI");
				}
			}else{ ///Error Controlado
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
					String description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
					fEPCS.Debug("["+jspName+"] DESCRIPTION: "+description, "INFO"); 
				}
				String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
				result.put("CODE", codigo);
				fEPCS.Debug("["+jspName+"] CODE: "+codigo, "INFO");
				parametros_marcas_navegacion.put("MSG","ERROR "+codigo);
			
			}
		}
    	 
    	result.put("trx_respuesta", trx_respuesta); 
    	result.put("cobro", cobro);
    	cliente_datos.put("cobro", cobro);
    	result.put("cliente_datos",cliente_datos);
    	fEPCS.Debug("["+jspName+"] FIN cliente_datos: "+cliente_datos.toString(), "INFO");
    	
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