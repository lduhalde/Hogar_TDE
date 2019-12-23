<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>
<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject cliente_datos = new JSONObject();    
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));  
    String trx_respuesta = "OK"; 
    String newUdata = "";
    
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
        String parametros_marcas = state.getString("parametros_marcas_navegacion");
        JSONObject marcas = new JSONObject(parametros_marcas);
        String Uniqueid = (state.has("AsteriskID") ) ? state.getString("AsteriskID") : ((marcas.has("AsteriskID") ) ? marcas.getString("AsteriskID") : "0");
        String OP_Bloqueos = (state.has("OP_Bloqueos") ) ? state.getString("OP_Bloqueos") : ((marcas.has("OP_Bloqueos") ) ? marcas.getString("OP_Bloqueos") : "0");
        //String OP_Bloqueos = (state.has("OP_Bloqueos") ) ? state.getString("OP_Bloqueos") : "0"; 
                newUdata = "parametros_marcas*"+parametros_marcas.toString()
                +"*UNIQUEID*"+Uniqueid
                +"*Appname*"+state.getString("appName")
                +"*Var_Tipo_Call*"+state.getString("Var_Tipo_Call")
                +"*ANI*"+state.getString("ANI")
                +"*IndividualIdentification*"+cliente_datos.optString("IndividualIdentification")
                +"*CustomerAccountID*"+cliente_datos.optString("CustomerAccountID")
                +"*VIPExecutiveDocument*"+cliente_datos.optString("VIPExecutiveDocument")
                +"*MERCADO*"+state.optString("MERCADO")
                +"*SEGMENTO*"+cliente_datos.optString("segmento")
                +"*reasonContract*"+state.optString("reasonContract")
                +"*Var_PCS*"+state.optString("Var_PCS")
                +"*OP_Bloqueos*"+OP_Bloqueos;

                
        fEPCS.Debug("[PutUDATA] newUdata: "+newUdata, "INFO");
        fEPCS.Debug("[PutUDATA] marcas: "+marcas.toString(), "INFO");
    }catch (Exception ex){
        fEPCS.Debug("[PutUDATA] Hubo un ERROR : Crear_Traza "+ex.getMessage());
        ex.printStackTrace(); 
    }finally{
        result.put("UserDataComposer",newUdata);
    }
    fEPCS.Debug("[PutUDATA] result :"+result.toString());
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>
