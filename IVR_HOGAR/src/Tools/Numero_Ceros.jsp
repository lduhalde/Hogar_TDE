<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    String varNumero = additionalParams.get("varNumero");
    String varFinCiclo = additionalParams.get("varFinCiclo");
    String salida_mp = "1";
    int numPrompt = 0;
    int contadorPromptCeros = Integer.parseInt(additionalParams.get("contadorPromptCeros"));
    
    fEPCS.Debug("[Numeros_Ceros] - contadorPromptCeros: ="+ contadorPromptCeros, "INFO");
    fEPCS.Debug("[Numeros_Ceros] - varNumero ="+ varNumero, "INFO");
    fEPCS.Debug("[Numeros_Ceros] - varFinCiclo: ="+ varFinCiclo, "INFO");
    
    if(varFinCiclo !="1" && varNumero.length()>1){
    	numPrompt = Integer.parseInt(varNumero.substring(contadorPromptCeros,contadorPromptCeros+1));
    	result.put("varNumero",varNumero.substring(contadorPromptCeros));
    	if(numPrompt>0){
    		numPrompt = Integer.parseInt(varNumero.substring(contadorPromptCeros,varNumero.length()));
    		result.put("varFinCiclo","1");
    		fEPCS.Debug("[Numeros_Ceros] - numPrompt: = "+numPrompt,"INFO");
    	}
    	salida_mp ="2";
    	fEPCS.Debug("[Numeros_Ceros] - numPrompt: = "+numPrompt, "INFO");
    	contadorPromptCeros++;
    	result.put("numPromptCeros",Integer.toString(numPrompt));
    	result.put("contadorPromptCeros",Integer.toString(contadorPromptCeros));
    	result.put("salida_mp",salida_mp);
    }
    if(salida_mp.equals("1")){
    	result.put("varFinCiclo","0");
    	result.put("contadorPromptCeros","0");
    	result.put("salida_mp",salida_mp);
    }
    return result;
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>
