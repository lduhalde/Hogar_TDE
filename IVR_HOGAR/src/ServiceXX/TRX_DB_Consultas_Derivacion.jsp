<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	
	String nameJSP="TRX_DB_Consulta_Derivacion";
	String trx = "TRX_CONSULTA_DERIVACION";
    String trx_respuesta="NOK";
    JSONObject result = new JSONObject();
    JSONObject  DataEnvioSckt = new JSONObject();
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    JSONObject cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    try{
    	
    	String Segmento=additionalParams.get("Segmento");
    	String PCS=additionalParams.get("PCS");
    	String TransferID="";
    			
         //DEBUG VALORES INICIALES		
    	fEPCS.Debug("["+nameJSP+"] INICIO", "INFO");
    	fEPCS.Debug("["+nameJSP+"] SEGMENTO: "+Segmento, "INFO");
    	fEPCS.Debug("["+nameJSP+"] PCS: "+PCS, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,trx);
    	parametros_marcas_navegacion.put("DATA",PCS);
    	parametros_marcas_navegacion.put("RC","99");
    	
    	if(!PCS.startsWith("56")){
    		PCS="56"+PCS;
    	}
    	if(Segmento.equals("Diamante")){
    		Segmento="AV";
    	}else{
    		Segmento="BV";
    	}
    	
    	if(cliente_datos.getString("nmro_scoring").equals("1") || cliente_datos.getString("nmro_scoring").equals("0") ){
    		if(cliente_datos.getString("DESC_OFERTA").equals("HOGAR")){
    			TransferID="TDE_CC_"+Segmento+"_S2S";
    		}else{
    			TransferID="TDE_CC_"+Segmento+"_M_S2S";
    		}
    		
    	}else{
    		TransferID="TDE_CC_"+Segmento;
    	}
   	
		DataEnvioSckt.put("servicio",fEPCS.getParametro(trx + "_DB"));
		DataEnvioSckt.put("query",fEPCS.getParametro(trx + "_SP"));
		DataEnvioSckt.put("parameters",TransferID+"|"+PCS+"|4");
		DataEnvioSckt.put("select","1");
		DataEnvioSckt.put("requestID",state.getString("idLlamada"));
		
	
		String respuestaDB= fEPCS.Socket_SendRecvHA("CONSULTAS_DERIVACION", DataEnvioSckt.toString(),"DB");
		fEPCS.Debug("["+nameJSP+"] respuestaDB: "+respuestaDB, "INFO");
		
		
		if(!respuestaDB.equals("[]")||respuestaDB.indexOf("errorMessage")==-1){
			JSONObject jObjRespuesta = new JSONObject(respuestaDB.replaceAll("\\[", "").replaceAll("\\]", ""));
			if(!jObjRespuesta.isNull("CC_NOMBRE")){
				result.put("CALLCENTER_OFERTA",jObjRespuesta.getString("CC_NOMBRE"));
			}
			if(!jObjRespuesta.isNull("VDN")){
				result.put("VDNSCORING",jObjRespuesta.getString("VDN"));
			}
			parametros_marcas_navegacion.put("RC","0");
			trx_respuesta="OK";
			result.put("VDN",TransferID);
			jObjRespuesta = null;
		}	
    }catch(Exception ex){
    	fEPCS.Debug("["+nameJSP+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	result.put("trx_respuesta", trx_respuesta);
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+nameJSP+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+nameJSP+"] FIN result: "+result.toString(), "INFO");
    	result.put("trx_respuesta", trx_respuesta);
    	DataEnvioSckt = null;
    	parametros_marcas_navegacion = null;
    }
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>