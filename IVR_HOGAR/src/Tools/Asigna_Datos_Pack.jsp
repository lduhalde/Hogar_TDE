<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    JSONObject cliente_productos = new JSONObject();
    String jspName="Asigna_Datos_Pack";
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
       
    try{    	 
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	cliente_productos = (state.has("cliente_productos") ) ? state.getJSONObject("cliente_productos") : new JSONObject();
    	
    	fEPCS.Debug("["+jspName+"] Inicio", "INFO");
		int pack = Integer.parseInt(additionalParams.get("packSeleccionado"));
		fEPCS.Debug("["+jspName+"] index pack seleccionado : "+pack, "INFO");
		
		JSONObject bundle = cliente_productos.getJSONArray("Bundles").getJSONObject(pack);
		cliente_datos.put("bundle_seleccionado",bundle);
		fEPCS.Debug("["+jspName+"] Bundle: "+bundle.toString(), "INFO");
		
		String mercado="";
		if(!bundle.isNull("mercado")){
			mercado = bundle.getString("mercado");
			cliente_datos.put("mercado",mercado);
			fEPCS.Debug("["+jspName+"] mercado : "+mercado, "INFO");
		}
		if(!bundle.isNull("MSISDN")){
			String MSISDN = bundle.getString("MSISDN");
			if(!MSISDN.equals("null")){
				fEPCS.Debug("["+jspName+"] MSISDN : "+MSISDN, "INFO");	
				cliente_datos.put("PCS_Seleccionado",MSISDN);
			}else{
				fEPCS.Debug("["+jspName+"] SIN MSISDN", "INFO");	
			}
		}
		
		result.put("cliente_datos", cliente_datos);
		
		bundle=null;
    }catch (Exception ex){
    	fEPCS.DebugError("["+jspName+"] Hubo un ERROR: "+ex.getMessage());    	
    }finally{
    	cliente_productos=null;
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