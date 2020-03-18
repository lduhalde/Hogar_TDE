<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    String jspName = "TRX_getTroubleShoot";
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	
		fEPCS.Debug("["+jspName+"] INICIO", "INFO");
    	
    	String family = (additionalParams.get("idConsulta").equals("TV"))?"Television":"Internet";
    	String idLlamada = additionalParams.get("idLlamada");
    	fEPCS.Debug("["+jspName+"] consulta: "+consulta, "INFO");
    	fEPCS.Debug("["+jspName+"] idLlamada: "+idLlamada, "INFO");
    	String serviceId = "";
    	
    	JSONObject bundle = cliente_datos.getJSONObject("bundle_seleccionado");
    	JSONArray basics = bundle.getJSONArray("Basics");
    	for(int i=0;i<Basics.length();i++){
    		if(Baiscs.getJSONObject(i).getString("family").equals(family)){
    			serviceId = Baiscs.getJSONObject(i).getString("serviceIdExt");
    		}
    	}
    	
    	JSONObject request = new JSONObject();
    	
    	JSONObject identification = new JSONObject();
    	identification.put("serviceId",serviceId);
    	identification.put("serviceIdType","serviceid");
    	
    	JSONObject context = new JSONObject();
    	context.put("source","oneclick");
    	context.put("sourceDetail","n1");
    	String[] tags = new String[] {"ivr"};
    	context.put("tags",tags);
    	
    	String[] datasource = new String[] {"ivr"}; 
    	
    	request.put("identification",identification);
    	request.put("context",context);
    	request.put("datasource",datasource);
    	
    	String cods_ok = fEPCS.Params.GetValue("GetTroubleShoot_Cod_OK", "");
    	String cods_ejecutivo = fEPCS.Params.GetValue("GetTroubleShoot_Cod_Ejecutivo", "");
    	String cods_execute = fEPCS.Params.GetValue("GetTroubleShoot_Cod_ExecuteAction", "");
    	String cods_mensaje = fEPCS.Params.GetValue("GetTroubleShoot_Cod_mensaje", "");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_GETTROUBLESHOOT");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
    	
    	String trx_respuesta = "NOK";
    	String codigoResp = "";
    	
    	String sTrx_datos_respuesta=fEPCS.ExecuteSCHAMAN(request,"GetTroubleShoot",idLlamada);
    	fEPCS.Debug("["+jspName+"] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");   	
    	respJSON = new JSONObject(sTrx_datos_respuesta); 

		if(respJSON.has("httpcode")){
			codigoResp = respJSON.getString("httpcode");
			parametros_marcas_navegacion.put("RC","0");
			if(cods_ok.indexOf(codigoResp)>-1){
				trx_respuesta = "OK-SIN_PROBLEMAS";
			}else if(cods_ejecutivo.indexOf(codigoResp)>-1){
				trx_respuesta = "OK-EJECUTIVO";
			}else if(cods_execute.indexOf(codigoResp)>-1){
				trx_respuesta = "OK-EXECUTE";
			}else if(cods_mensaje.indexOf(codigoResp)>-1){
				trx_respuesta = "OK-MENSAJE";
			}else{
				trx_respuesta = "NOK";//????
			}
		}
		
    	result.put("trx_respuesta", trx_respuesta);
    	result.put("cliente_datos", cliente_datos);
    	
    }catch(Exception ex){
    	fEPCS.Debug("["+jspName+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+jspName+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+jspName+"] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON=null;
    	cliente_datos=null;
    }
    
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>