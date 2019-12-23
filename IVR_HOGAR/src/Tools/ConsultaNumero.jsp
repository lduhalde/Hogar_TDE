<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    //String ani = state.getString("ANI");
    String ani = state.getString("Var_PCS");
    String salida_mp = "";
	String varNumero ="";
	String Formato_Numero = fEPCS.Params.GetValue("FormatoLecturaNumero");
	int lastcant = 0;
	int contador = 0;
    
    fEPCS.Debug("[ConsultaNumero] INICIO", "INFO");
	fEPCS.Debug("[ConsultaNumero] ANI: "+ani, "INFO");
		
	String [] arrFormato = Formato_Numero.split(",");
	contador=Integer.parseInt(additionalParams.get("contadorNumPartes"));
	String numeroRestante = additionalParams.get("numeroRestante");
	
	fEPCS.Debug("[ConsultaNumero] PCS: "+numeroRestante, "INFO");
	
	fEPCS.Debug("[ConsultaNumero]- Contador: ="+contador,"INFO");
	
	if(contador<=(arrFormato.length-1)){
		lastcant= Integer.parseInt(arrFormato[contador].replace(" ",""));
		if(lastcant>numeroRestante.length()){
			lastcant = numeroRestante.length();
		}
		varNumero= numeroRestante.substring(0,lastcant);
		fEPCS.Debug("[ConsultaNumero]- varNumero: =" +varNumero,"INFO");
	
		numeroRestante = numeroRestante.substring(lastcant,numeroRestante.length());
		contador++;
		result.put("contadorNumPartes",Integer.toString(contador));
		result.put("varNumero",varNumero);
		result.put("numeroRestante",numeroRestante);
		if(varNumero.substring(0,1).equals("0")&& varNumero.length()>1){
			result.put("salida_mp","3");
		}else{
			result.put("salida_mp","2");
		}
	}else{
		result.put("salida_mp","1");
	}
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>