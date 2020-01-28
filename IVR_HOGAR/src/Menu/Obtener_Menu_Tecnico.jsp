<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    JSONObject cliente_productos = new JSONObject();
    String jspName="Obtener_Menu_Tecnico";
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
       
    try{    	 
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	cliente_productos = (state.has("cliente_productos") ) ? state.getJSONObject("cliente_productos") : new JSONObject();
    	String opcionesMenu = "";
        String opcionesDTMF = "";
        String Path="es-CL/IVR/Menus/";
        String AudioDefecto="IVR/General/Silencio.wav";
        String audioDTMF="/IVR/Menus/marca";
    	
    	fEPCS.Debug("["+jspName+"] Inicio", "INFO");
		int pack = Integer.parseInt(additionalParams.get("packSeleccionado"));
		fEPCS.Debug("["+jspName+"] index pack seleccionado : "+pack, "INFO");
		
		JSONObject bundle = cliente_productos.getJSONArray("Bundles").getJSONObject(pack);
		cliente_datos.put("bundle_seleccionado",bundle);
		fEPCS.Debug("["+jspName+"] Bundle: "+bundle.getString("commercialName"), "INFO");
		
		
		String family = bundle.getString("family");
		String technology = bundle.getString("technology");
		String mercado = bundle.getString("mercado");
		String MSISDN = bundle.getString("MSISDN");
		
		fEPCS.Debug("["+jspName+"] family : "+family, "INFO");
		fEPCS.Debug("["+jspName+"] technology : "+technology, "INFO");
		fEPCS.Debug("["+jspName+"] mercado : "+mercado, "INFO");
		fEPCS.Debug("["+jspName+"] MSISDN : "+MSISDN, "INFO");
		
		cliente_datos.put("PCS_Seleccionado",MSISDN);
		
		int cont =1;
		
		if(family.indexOf("Telephony")>-1 || family.indexOf("Internet")>-1 || family.indexOf("Television")>-1){
			if(family.indexOf("Television") || family.indexOf("Internet")){
				opcionesMenu += cont+";"+Path+"OP_Agendamiento.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;AGENDAMIENTO|";
	    		opcionesDTMF += cont+"|";
	    		cont++;
			}    		
    		if(family.indexOf("Telephony")>-1){
    			opcionesMenu += cont+";"+Path+"OP_Problemas_Llamadas.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;PRBLM_LLAMADAS|";
        		opcionesDTMF += cont+"|";
        		cont++;
    		}
    		if(family.indexOf("Television")>-1){
    			if(technology.equals("Inalambrico")){
    				opcionesMenu += cont+";"+Path+"OP_Problemas_TV.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;PRBLM_TV|";
    			}else{
    				opcionesMenu += cont+";"+Path+"OP_Problemas_TV.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;PRBLM_TV_FIBRA|";
    			}
        		opcionesDTMF += cont+"|";
        		cont++;
    		}
    		if(family.indexOf("Internet")>-1){
    			if(technology.equals("Inalambrico")){//BAFI
    				opcionesMenu += cont+";"+Path+"OP_Problemas_Nav.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;PRBLM_NAVEGACION|";
    			}else{
    				opcionesMenu += cont+";"+Path+"OP_Problemas_Nav.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;PRBLM_NAVEGACION_FIBRA|";
    			}
        		opcionesDTMF += cont+"|";
        		cont++;
    		}
    		opcionesMenu += cont+";"+Path+"OP_Ejecutivo.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;EJECUTIVO|";
    		opcionesDTMF += cont+"|";
    		cont++;
    		
    		opcionesMenu += "9;"+Path+"OP_Volver.wav;"+AudioDefecto+";;"+AudioDefecto+";SI;VOLVER|";
    		opcionesDTMF += "9|";
		}else{
			fEPCS.Debug("["+jspName+"] ERROR FAMILY desconocida", "INFO");
		}

    	fEPCS.Debug("["+jspName+"] OPCS : "+opcionesMenu, "INFO");
    	fEPCS.Debug("["+jspName+"] DTMF : "+opcionesDTMF, "INFO");
    	
    	result.put("OpcionesMenu", opcionesMenu);
		result.put("OpcionesDTMF", opcionesDTMF);
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