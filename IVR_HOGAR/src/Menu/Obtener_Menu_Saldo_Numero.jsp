<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    JSONObject cliente_productos = new JSONObject();
    String jspName="Obtener_Menu_Saldo_Numero";
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
		
		
		opcionesMenu += "1;"+Path+"OP_Saldo.wav;"+AudioDefecto+";;"+audioDTMF+"1.wav;SI;SALDO|";
		opcionesDTMF += "1|";
		
		opcionesMenu += "2;"+Path+"OP_Numero.wav;"+AudioDefecto+";;"+audioDTMF+"2.wav;SI;NUMERO|";
		opcionesDTMF += "2|";
		
		if(family.indexOf("Internet")>-1 && technology.equals("Inalambrico") && mercado.equals("HP")){ //BAFI PP
			opcionesMenu += "3;"+Path+"OP_Consulta_Bolsas.wav;"+AudioDefecto+";;"+audioDTMF+"3.wav;SI;BOLSAS|";
    		opcionesDTMF += "3|";
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