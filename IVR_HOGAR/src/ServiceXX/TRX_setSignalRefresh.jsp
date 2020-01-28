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
    
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	
		String ani = state.getString("Var_PCS");
		ani = (ani.length() < 11 ) ? "56"+ani : ani;
    	fEPCS.Debug("[TRX_getContract] INICIO", "INFO");
    	fEPCS.Debug("[TRX_getContract] ANI: "+ani, "INFO");
    	
    	String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	fEPCS.Debug("[TRX_getContract] processCode: "+processID, "INFO");
    	fEPCS.Debug("[TRX_getContract] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_getContract] idLlamada: "+idLlamada, "INFO");
    	
    	parametros_marcas_navegacion.put("appName",(state.has("appName") ) ? state.getString("appName") : "PP_TDE");
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_CONTRACT");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
    	
    	String trx_respuesta = "NOK";
    	String codigoResp = "";
    	
    	String sTrx_datos_respuesta=fEPCS.GetContract(ani,idLlamada, processID, sourceID);
    	fEPCS.Debug("[TRX_getContract] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");   	
    	respJSON = new JSONObject(sTrx_datos_respuesta); 

    	
		if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("[TRX_getContract] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			String status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				String description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("[TRX_getContract] DESCRIPTION: "+description, "INFO"); 
			
			}
			fEPCS.Debug("[TRX_getContract] STATUS: "+status, "INFO"); 
			
			if(status.equalsIgnoreCase("OK")){ 
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
				String contractID 		= respJSON.getJSONObject("Body").getJSONObject("Contract").getString("ID");
				String ICCID 			= respJSON.getJSONObject("Body").getJSONObject("Contract").getJSONObject("MSISDN").getJSONObject("SIMCard").getString("ICCID");
				String IMSI 			= respJSON.getJSONObject("Body").getJSONObject("Contract").getJSONObject("MSISDN").getJSONObject("IMSI").getString("SN");
				String plmnCode 		= respJSON.getJSONObject("Body").getJSONObject("Contract").getJSONObject("MSISDN").getJSONObject("INRoutingDevice").getJSONObject("Operator").getString("pmlnCode");
				String statusContract 	= respJSON.getJSONObject("Body").getJSONObject("Contract").getString("status");
				String reasonContract 	= respJSON.getJSONObject("Body").getJSONObject("Contract").getString("reason");
				
				fEPCS.Debug("[TRX_getContract] contractID: "		+contractID, "INFO");
				fEPCS.Debug("[TRX_getContract] ICCID: "				+ICCID, "INFO");
				fEPCS.Debug("[TRX_getContract] IMSI: "				+IMSI, "INFO");
				fEPCS.Debug("[TRX_getContract] plmnCode: "			+plmnCode, "INFO");
				fEPCS.Debug("[TRX_getContract] status Contract: "	+statusContract, "INFO");
				fEPCS.Debug("[TRX_getContract] reason Contract: "	+reasonContract, "INFO");
				
				result.put("contractID", contractID);
				result.put("ICCID", ICCID);
				result.put("IMSI", IMSI);
				result.put("plmnCode", plmnCode);
				result.put("statusContract", statusContract);
				result.put("reasonContract", reasonContract);
				
				cliente_datos.put("ICCID", ICCID);
				cliente_datos.put("IMSI", IMSI);
				cliente_datos.put("contractID", contractID);
				cliente_datos.put("statusContract", statusContract);
				cliente_datos.put("reasonContract", reasonContract);
			}else{
				
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("code")){
					codigoResp = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					fEPCS.Debug("[TRX_getContract] Código: "+codigoResp, "INFO");
					parametros_marcas_navegacion.put("MSG","ERROR "+codigoResp);
				}
				fEPCS.Debug("[TRX_getContract] Error. Código: "+codigoResp, "INFO");
			}
		}
		//trx_respuesta = "OK";
    	result.put("trx_respuesta", trx_respuesta);
    	result.put("cliente_datos", cliente_datos);
    	
    }catch(Exception ex){
    	fEPCS.Debug("[TRX_getContract] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_getContract] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_getContract] FIN result: "+result.toString(), "INFO");
    	
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