<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>
<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
        
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));  

    try{
    	JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    	String INICIO = additionalParams.get("INICIO");
    	String AsteriskID = (parametros_marcas_navegacion.has("AsteriskID") ) ? parametros_marcas_navegacion.getString("AsteriskID") : state.getString("AsteriskID");
    	parametros_marcas_navegacion.put("AsteriskID",AsteriskID);
    	if(INICIO.equalsIgnoreCase("SI")){
    		String traza = additionalParams.get("TRAZA");
    		parametros_marcas_navegacion=fEPCS.startNavegacion(state, traza);
    		result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    		fEPCS.Debug("[Crear_Traza] INICIO - parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	}else if(INICIO.equalsIgnoreCase("DBG")){
    		parametros_marcas_navegacion.put("TRAZA",additionalParams.get("TRAZA"));
    		parametros_marcas_navegacion.put("DATA",additionalParams.get("DATA"));
    		parametros_marcas_navegacion.put("MSG",additionalParams.get("MSG"));
    		parametros_marcas_navegacion.put("RC",additionalParams.get("RC"));
    		state.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    		
    		parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    		result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    		fEPCS.Debug("[Crear_Traza] FIN DBG - parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	}else{
    		parametros_marcas_navegacion.put("DATA",additionalParams.get("DATA"));
    		parametros_marcas_navegacion.put("MSG",additionalParams.get("MSG"));
    		parametros_marcas_navegacion.put("RC",additionalParams.get("RC"));
    		state.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    		
    		parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    		result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    		fEPCS.Debug("[Crear_Traza] FIN - parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	}
    	                        
    }catch (Exception ex){
    	fEPCS.Debug("[Crear_Traza] Hubo un ERROR : Crear_Traza "+ex.getMessage());
    	ex.printStackTrace();
    }
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../../include/backend.jspf" %>