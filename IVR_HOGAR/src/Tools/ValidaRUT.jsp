<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>


<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();

    //Para Obtener Variables (setPageItem)    
    String rut = additionalParams.get("RUT");
    String dv = "";    
    String retorno = "NOK";
    String rutCompleto = "";
    
    FunctionsEPCS fGVP = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{    	
        fGVP.Debug("[ValidaRUT] Ingreso RUT : "+rut, "Detail");
        
        dv = rut.substring(rut.length() - 1);
        rut = rut.substring(0, rut.length() - 1);
                
        if ( fGVP.ValidaRUT(rut, dv, "1") ){
        	retorno = "OK";
        }else{
        	retorno = "NOK";        	
        }
        
        //Se incorpora la siguiente Rutina para eliminar los 0 a la Izquierda del RUT
        while(rut.indexOf("0")==0){ rut=rut.substring(1); }
        
        
        if (fGVP.isDVesK()){
        	rutCompleto = rut+"K";
        }else{
        	rutCompleto = rut+dv;
        }
        
        if (rutCompleto.equals("111111111")){
        	rutCompleto="";
        }
        if (rut.equals("11111111")){
        	rut="";
        }
        
        result.put("retorno", retorno);
        result.put("RUT_CON_DV", rutCompleto);
        result.put("RUT_SIN_DV", rut);
    }catch (Exception ex){
    	fGVP.DebugError("Hubo un ERROR : Ingreso RUT "+ex.getMessage() );
    	result.put("retorno", retorno);
    	
    }
    fGVP.Debug("[ValidaRUT] RUT_SIN_DV: "+rut, "Detail");
    fGVP.Debug("[ValidaRUT] RUT_CON_DV : "+rutCompleto, "Detail");
    fGVP.Debug("[ValidaRUT] retorno : "+retorno, "Detail");
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>