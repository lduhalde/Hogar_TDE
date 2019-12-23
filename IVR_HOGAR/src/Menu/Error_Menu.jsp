<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();

    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
//    String varLEM = state.getString("LAST_EVENT_MSG");
    String varLEN = state.getString("LAST_EVENT_NAME");
    
    fEPCS.Debug("Ha ocurrido un error: "+additionalParams.get("Flujo"), "Detail");
//    fGVP.Debug("Last Event Message: " + varLEM, "Detail");
    fEPCS.Debug("Last Event Name: " + varLEN, "Detail");
    
    // result.put("OutParam", "Value");   
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>