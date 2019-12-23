<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    JSONObject cliente_productos = new JSONObject();
    String jspName="Obtener_Menu_Packs";
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
       
    try{    	 
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	cliente_productos = (state.has("cliente_productos") ) ? state.getJSONObject("cliente_productos") : new JSONObject();
    	String opcionesMenu = "";
        String opcionesDTMF = "";
        String Path="es-CL/IVR/Menus/";
        String AudioDefecto="IVR/General/Silencio.wav";
        String audioDTMF="/IVR/Menus/marque";
        String rutaDefecto = "IVR/General/";
    	
    	fEPCS.Debug("["+jspName+"] Inicio", "INFO");
		JSONArray bundles = cliente_productos.getJSONArray("Bundles");
		String comuna = "";
    	for(int i =0;i<bundles.length();i++){
    		comuna = bundles.getJSONObject(i).getString("addressCommune");
    		fEPCS.Debug("["+jspName+"] Bundle: "+bundles.getJSONObject(i).getString("commercialName"), "INFO");
    		fEPCS.Debug("["+jspName+"] Comuna: "+ comuna,"INFO");
    		fEPCS.Debug("["+jspName+"] Index: "+i, "INFO");
    		opcionesMenu += (i+1)+";"+Path+"Pack"+(i+1)+".wav;"+rutaDefecto+comuna+".wav;;"+audioDTMF+(i+1)+".wav;SI;"+i+"|";
    		opcionesDTMF += (i+1)+"|";
    	}

    	fEPCS.Debug("["+jspName+"] OPCS : "+opcionesMenu, "INFO");
    	fEPCS.Debug("["+jspName+"] DTMF : "+opcionesDTMF, "INFO");
    	
    	result.put("OpcionesMenu", opcionesMenu);
		result.put("OpcionesDTMF", opcionesDTMF);
		                    
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