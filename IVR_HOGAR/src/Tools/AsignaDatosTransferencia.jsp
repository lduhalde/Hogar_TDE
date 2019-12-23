<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    String RUT="";        
    JSONObject cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    try{
    	if(!cliente_datos.isNull("IndividualIdentification")){
    		JSONObject OindividualIdentification= cliente_datos.getJSONObject("IndividualIdentification");
        	fEPCS.Debug("[AsignaDatosTransferencia] IndividualIdentification: "+OindividualIdentification.toString(), "INFO");
        	if(!OindividualIdentification.isNull("number")){
        		if(!OindividualIdentification.isNull("type")){
        			if(OindividualIdentification.getString("type").equalsIgnoreCase("RUT")){
        				RUT=OindividualIdentification.getString("number");
					 	fEPCS.Debug("[AsignaDatosTransferencia] Cliente identificado por RUT: ["+RUT+"]", "INFO");
					}
			 	}
        	}
		}
    	
    }catch(Exception ex){
    	fEPCS.DebugError("[AsignaDatosTransferencia] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	result.put("RutClientePCS",RUT);
    	fEPCS.Debug("[AsignaDatosTransferencia] FIN result: "+result.toString(), "INFO");
    }
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>