<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	String idCanal=additionalParams.get("idCanal");
	String ani = additionalParams.get("ANI");
	String Tenant = additionalParams.get("Tenant");
	String Encuesta = additionalParams.get("Encuesta");
	String idLlamada = additionalParams.get("idLlamada");
	String Uniqueid = additionalParams.get("Uniqueid");
	String RutCli = additionalParams.get("Input_RUT");
	String Mercado = additionalParams.get("ivr");
	String nameJSP="TRX_Valida_Encuesta";
    String trx_respuesta="NOK";
    JSONObject result = new JSONObject();
    JSONObject  DataEnvioSckt = new JSONObject();
    String EpaStatus="";
    String IdRequest = "";
    String Tramo="";
    String RC="";
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    RutCli = Mercado; //donde envio el mercado a la variable hacia la base de datos (fam)
    
    try{
    	if(ani.length()==9){
    		ani="56"+ani;
    	}
    	//DEBUG VALORES INICIALES		
    	fEPCS.Debug("["+nameJSP+"] INICIO", "INFO");
    	fEPCS.Debug("["+nameJSP+"] Tenant : "+Tenant, "INFO");
    	fEPCS.Debug("["+nameJSP+"] ID_Canal : "+idCanal, "INFO");
    	fEPCS.Debug("["+nameJSP+"] Encuesta : "+Encuesta, "INFO");
    	fEPCS.Debug("["+nameJSP+"] ANI : "+ani, "INFO");
    	fEPCS.Debug("["+nameJSP+"] UNIQUEID : "+Uniqueid, "INFO");
    	fEPCS.Debug("["+nameJSP+"] MERCADO : "+RutCli, "INFO");
    	
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_VALIDA_ENCUESTA");
    	parametros_marcas_navegacion.put("DATA","STATUS");
    	parametros_marcas_navegacion.put("RC","99");
    	parametros_marcas_navegacion.put("MSG","-1");
    	 
		DataEnvioSckt.put("servicio","BD_EPA");
		DataEnvioSckt.put("query","[EPA_ONLINE_VALIDA_REQUEST]");
		DataEnvioSckt.put("parameters",Tenant+"|"+idCanal+"|"+Encuesta+"|"+ani+"|"+Uniqueid+"|"+idLlamada+"|"+RutCli);
		DataEnvioSckt.put("select","1");
		DataEnvioSckt.put("requestID",state.getString("idLlamada"));
			
		fEPCS.Debug("["+nameJSP+"] - DataEnvioSckt: "+DataEnvioSckt.toString(), "INFO");
		String respuestaDB= fEPCS.Socket_SendRecvHA("VALIDA_ENCUESTA", DataEnvioSckt.toString(),"DB2");
		fEPCS.Debug("["+nameJSP+"] respuestaDB: "+respuestaDB, "INFO");	
			
		if(!respuestaDB.equals("[]")||respuestaDB.indexOf("errorMessage")==-1){
			JSONObject jObjRespuesta = new JSONObject(respuestaDB.replaceAll("\\[", "").replaceAll("\\]", ""));
			RC = jObjRespuesta.getString("RC");
			if(RC.equals("0")){
				EpaStatus= jObjRespuesta.getString("EPA_STATUS");
				trx_respuesta="OK";
				parametros_marcas_navegacion.put("RC","0");
				parametros_marcas_navegacion.put("MSG",EpaStatus); 
				if(EpaStatus.equals("0") || EpaStatus.equals("112")){
					IdRequest= jObjRespuesta.getString("ID_EPA_REQUEST");
					Tramo= jObjRespuesta.getString("TRAMO_HORARIO");
					result.put("IdRequest", IdRequest);	
					result.put("Tramo", Tramo);
					result.put("EpaStatus", EpaStatus);
				}
			}
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