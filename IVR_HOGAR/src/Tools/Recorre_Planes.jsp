<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {
    
	JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    String jspName="Recorre_Planes";
    String contador = additionalParams.get("contador");
    int cont = Integer.parseInt(contador);
    
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	fEPCS.Debug("["+jspName+"] INICIO", "INFO");
    	String path = "/IVR/Sub_Consulta_Plan/";
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	JSONObject bundle = cliente_datos.getJSONObject("bundle_seleccionado");
		JSONArray basics = bundle.getJSONArray("Basics");
		String bundleId = bundle.getString("bundleId");
		fEPCS.Debug("["+jspName+"] Basics: " +basics.toString(), "INFO");
		fEPCS.Debug("["+jspName+"] Contador: " +cont, "INFO");
		
		String CONTINUAR = "NO";
		if(cont==0){
			fEPCS.Debug("["+jspName+"] Audio Inicial: " +path+basics.length()+".wav", "INFO");
			result.put("Var_Audio_Inicial",path+"Packs/"+bundleId+".wav");
		}
		if(basics.length()>cont){
			CONTINUAR = "SI";
    		fEPCS.Debug("["+jspName+"] PO: "+ basics.getJSONObject(cont).getJSONObject("Plan").getJSONObject("FreeUnits").getString("PO"), "INFO");
    		result.put("Var_Audio_Plan", path+basics.getJSONObject(cont).getJSONObject("Plan").getJSONObject("FreeUnits").getString("PO")+".wav");
    		//result.put("Var_AUDIO", path+Bolsas.get(cont)+".wav");
    		result.put("contador", cont+1);
		}else{
			result.put("contador", 0);
		}
		result.put("CONTINUAR",CONTINUAR);
    	fEPCS.Debug("["+jspName+"] FIN Contador Planes: " +cont, "INFO");
    	
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