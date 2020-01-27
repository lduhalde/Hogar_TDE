<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    JSONObject cliente_productos = new JSONObject();
    String jspName="Obtener_Menu_Comercial";
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
			if((family.indexOf("Telephony")>-1 && mercado.equals("HP")) || (family.indexOf("Internet")>-1 && technology.equals("Inalambrico") && mercado.equals("HP"))){
				opcionesMenu += cont+";"+Path+"OP_Saldo_Numero.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;SALDO_NUMERO|";
	    		opcionesDTMF += cont+"|";
	    		cont++;
			}
			
			opcionesMenu += cont+";"+Path+"OP_Plan_Monto_LPagos.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;PLAN_MONTO_LPAGOS|";
    		opcionesDTMF += cont+"|";
    		cont++;
    		
    		if(family.indexOf("Telephony")>-1 || family.indexOf("Television")>-1){
    			opcionesMenu += cont+";"+Path+"OP_Activaciones.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;ACTIVACIONES|";
        		opcionesDTMF += cont+"|";
        		cont++;
    		}
    		if(family.indexOf("Internet")>-1 && technology.equals("Inalambrico")){ //BAFI
    			opcionesMenu += cont+";"+Path+"OP_Compra_Bolsa.wav;"+AudioDefecto+";;"+audioDTMF+cont+".wav;SI;COMPRA_BOLSA|";
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