<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
	String jsp="RecuperaParametroFunctionsPropertie";
    
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    String key = additionalParams.get("key");
    String defaultValue = additionalParams.get("defaultValue");
    
    fEPCS.Debug("["+jsp+"] KEY: "+key, "INFO");
    fEPCS.Debug("["+jsp+"] DEFAULT VALUE: "+defaultValue, "INFO");
    
    String value = fEPCS.Params.GetValue(key,defaultValue);
    fEPCS.Debug("["+jsp+"] PROPERTIE VALUE: "+value, "INFO");
    
    result.put("value",value);
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>