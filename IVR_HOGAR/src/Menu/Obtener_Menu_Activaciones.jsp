<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    String jspName="Obtener_Menu_Activaciones";
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
       
    try{    	 
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
  
    	String opcionesMenu = "";
        String opcionesDTMF = "";
        String Path="es-CL/IVR/Menus/";
        String AudioDefecto="IVR/General/Silencio.wav";
        String audioDTMF="/IVR/Menus/marque";
    	
    	fEPCS.Debug("["+jspName+"] Inicio", "INFO");
    	JSONObject bundle = cliente_datos.getJSONObject("bundle_seleccionado");
		
		String family = bundle.getString("family");
		String technology = bundle.getString("technology");
		String mercado = bundle.getString("mercado");
		String MSISDN = bundle.getString("MSISDN");
		
		fEPCS.Debug("["+jspName+"] family : "+family, "INFO");
		fEPCS.Debug("["+jspName+"] technology : "+technology, "INFO");
		fEPCS.Debug("["+jspName+"] mercado : "+mercado, "INFO");
		fEPCS.Debug("["+jspName+"] MSISDN : "+MSISDN, "INFO");
		
		
		int cont =1;
		
		if(family.indexOf("Telephony")>-1 ){
			opcionesMenu += "1;"+Path+"OP_Act_Llamadas_Celular.wav;"+AudioDefecto+";;"+audioDTMF+"1.wav;SI;CELULAR|";
	    	opcionesDTMF += "1|";
	    	
	    	opcionesMenu += "2;"+Path+"OP_Act_LDI.wav;"+AudioDefecto+";;"+audioDTMF+"2.wav;SI;LDI|";
	    	opcionesDTMF += "2|";
	    	cont = 3;
		}
		
		if(family.indexOf("Television")>-1 ){
			opcionesMenu += cont+";"+Path+"OP_Contrata_Canales.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;CANALES|";
    		opcionesDTMF += cont+"|";
		}
		
		opcionesMenu += "9;"+Path+"OP_Volver.wav;"+AudioDefecto+";;"+AudioDefecto+";SI;VOLVER|";
		opcionesDTMF += "9|";

    	fEPCS.Debug("["+jspName+"] OPCS : "+opcionesMenu, "INFO");
    	fEPCS.Debug("["+jspName+"] DTMF : "+opcionesDTMF, "INFO");
    	
    	result.put("OpcionesMenu", opcionesMenu);
		result.put("OpcionesDTMF", opcionesDTMF);

		
		bundle=null;
    }catch (Exception ex){
    	fEPCS.DebugError("["+jspName+"] Hubo un ERROR: "+ex.getMessage());    	
    }finally{
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