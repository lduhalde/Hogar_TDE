<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    String ani = state.getString("Var_PCS");
    String PCS ="";
    fEPCS.Debug("[Divide_Numero] INICIO", "INFO");
    fEPCS.Debug("[Divide_Numero] - PCS "+ani,"INFO");
    
    	String Var_PCS = ani;
    	if(Var_PCS.length()==11){
			Var_PCS=Var_PCS.substring(2);
		}
	
		result.put("numeroRestante",Var_PCS);

    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>