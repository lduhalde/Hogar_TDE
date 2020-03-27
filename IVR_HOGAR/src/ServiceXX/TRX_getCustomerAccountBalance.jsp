<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!

/*
Variables Entrada
	-PCS
	-interactionExternalID

Variables de Salida:
	-trx_respuesta = OK /NOK
	-Var_SALDO = String Ej:5200.15
	-Var_SALDO_VIGENCIA = YYYY-MM-DD
	-CODE => Para Cuando Existe un ERROR
*/

// Implement this method to execute some server-side logic.

public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
	int saldo = 0;
	String saldoVigencia = "";
	String saldoVigencia_original = "";
	int fechaVencimiento = 0;
	String saldoVigMonedero_original="";
	String fechaVencimientoText = "";
	String saldoVigencia2="";
	String saldoVigencia2_original="";
	
    String externalId="";
    String description = "";
	String codeCanonical = "";
	String jspName="TRX_getCustomerAccountBalance";
	
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));
    JSONArray Bolsas = new JSONArray();
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	
    	String ani = additionalParams.get("PCS");
		ani = (ani.length() < 11 ) ? "56"+ani : ani; 
	 
    	fEPCS.Debug("["+jspName+"] INICIO", "INFO");
    	fEPCS.Debug("["+jspName+"] ANI: "+ani, "INFO");
    	
    	String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	
    	fEPCS.Debug("["+jspName+"] processID: "+processID, "INFO");
    	fEPCS.Debug("["+jspName+"] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("["+jspName+"] idLlamada: "+idLlamada, "INFO");
    	
		fEPCS.Debug("["+jspName+"] parametros_marcas_navegacion: "+parametros_marcas_navegacion, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_CUSTOMERACCOUNTBALANCE");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
  	
    	String trx_respuesta = "NOK"; 
    	String status = "";
    	String sTrx_datos_respuesta=fEPCS.GetCustomerAccountBalance(ani,"0078",idLlamada, processID, sourceID);
    	fEPCS.Debug("["+jspName+"] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("["+jspName+"] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("GetCustomerAccountBalance_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("getCustomerAccountBalance_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("["+jspName+"] Fault STATUS: "+status, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("["+jspName+"] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("["+jspName+"] STATUS: "+status, "INFO");
			
			int saldoXvencer = 0;
			String saldoVigMonedero = "";
			
			if(status.equalsIgnoreCase("OK")){ 
				parametros_marcas_navegacion.put("RC","0");
				trx_respuesta = "OK"; 
				
				JSONArray balances = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("CustomerAccountBalance");
				for(int i=0; i<balances.length()-1; i++){
					JSONObject jo = (JSONObject) balances.get(i);
					String IDBalance = jo.getString("ID");
					if(IDBalance.equals("11100")){//Recargas
						saldo += jo.getJSONObject("remainedAmount").getInt("amount");
						saldoVigencia = jo.getJSONObject("validFor").getString("endDate");
						saldoVigencia = saldoVigencia.substring(0,saldoVigencia.indexOf("T"));
						saldoVigencia_original = saldoVigencia;
						saldoVigencia = saldoVigencia.replaceAll("-","");

						if(fechaVencimiento>Integer.parseInt(saldoVigencia)  || fechaVencimiento==0){
							fechaVencimiento = Integer.parseInt(saldoVigencia);
							saldoXvencer = jo.getJSONObject("remainedAmount").getInt("amount");
							fechaVencimientoText = saldoVigencia_original;
						}else if(fechaVencimiento==Integer.parseInt(saldoVigencia)){
							saldoXvencer += jo.getJSONObject("remainedAmount").getInt("amount");
						}

						fEPCS.Debug("["+jspName+"] saldoVigencia: "+saldoVigencia, "INFO");

						cliente_datos.put("Var_SALDO_RECARGAS", jo.getJSONObject("remainedAmount").getInt("amount"));
						fEPCS.Debug("["+jspName+"] SALDO: "+jo.getJSONObject("remainedAmount").getInt("amount"), "INFO");
					}
					if(IDBalance.equals("11101")){//Monedero secundario
						saldo += jo.getJSONObject("remainedAmount").getInt("amount");
						cliente_datos.put("Var_SALDO_BONO", jo.getJSONObject("remainedAmount").getInt("amount"));
						fEPCS.Debug("["+jspName+"] SALDO BONO: "+jo.getJSONObject("remainedAmount").getInt("amount"), "INFO");

						saldoVigMonedero = jo.getJSONObject("validFor").getString("endDate");
						saldoVigMonedero = saldoVigMonedero.substring(0,saldoVigMonedero.indexOf("T"));
						saldoVigMonedero_original = saldoVigMonedero;
						saldoVigMonedero = saldoVigMonedero.replaceAll("-",""); 

						if(fechaVencimiento>Integer.parseInt(saldoVigMonedero)  || fechaVencimiento==0){
							fechaVencimiento = Integer.parseInt(saldoVigMonedero);
							saldoXvencer = jo.getJSONObject("remainedAmount").getInt("amount");
							fechaVencimientoText = saldoVigMonedero_original;
						}else if(fechaVencimiento==Integer.parseInt(saldoVigMonedero)){
							saldoXvencer += jo.getJSONObject("remainedAmount").getInt("amount");
						}

						fEPCS.Debug("["+jspName+"] saldoVigMonedero: "+saldoVigMonedero, "INFO");

					}
					if(IDBalance.equals("11500")){//Recurrente mensual
						saldo += jo.getJSONObject("remainedAmount").getInt("amount");
						saldoVigencia2 = jo.getJSONObject("validFor").getString("endDate");
						saldoVigencia2 = saldoVigencia2.substring(0,saldoVigencia2.indexOf("T"));
						saldoVigencia2_original = saldoVigencia2;
						saldoVigencia2 = saldoVigencia2.replaceAll("-","");

						if(fechaVencimiento>Integer.parseInt(saldoVigencia2) || fechaVencimiento==0){
							fechaVencimiento = Integer.parseInt(saldoVigencia2);
							saldoXvencer = jo.getJSONObject("remainedAmount").getInt("amount");
							fechaVencimientoText = saldoVigencia2_original;
						}else if(fechaVencimiento==Integer.parseInt(saldoVigencia2)){
							saldoXvencer += jo.getJSONObject("remainedAmount").getInt("amount");
						}

						fEPCS.Debug("["+jspName+"] saldoVigencia2: "+saldoVigencia2, "INFO");


						cliente_datos.put("Var_SALDO_RECURRENTE", jo.getJSONObject("remainedAmount").getInt("amount"));
						fEPCS.Debug("["+jspName+"] SALDO RECURRENTE: "+jo.getJSONObject("remainedAmount").getInt("amount"), "INFO");
					}

				}
				if(fechaVencimiento == 0){
					fechaVencimientoText=saldoVigencia2_original;
				}
				
				fEPCS.Debug("["+jspName+"] fechaVencimiento: ["+fechaVencimiento+"]", "INFO");
				fEPCS.Debug("["+jspName+"] fechaVencimientoText: ["+fechaVencimientoText+"]", "INFO");
				fEPCS.Debug("["+jspName+"] saldoXvencer: "+saldoXvencer, "INFO");

				fEPCS.Debug("["+jspName+"] SALDO FINAL: "+saldo, "INFO");
				fEPCS.Debug("["+jspName+"] FECHA VIGENCIA: "+saldoVigencia_original, "INFO");
				cliente_datos.put("Var_SALDO", saldo);
				cliente_datos.put("Var_SALDO_VIGENCIA", fechaVencimiento+"");
				cliente_datos.put("Var_SALDO_XVENCER", saldoXvencer); 
				
				
			}else{ ///Error Controlado
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
					fEPCS.Debug("["+jspName+"] DESCRIPTION: "+description, "INFO"); 
				}
				String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
				result.put("CODE", codigo);
				fEPCS.Debug("["+jspName+"] CODE: "+codigo, "INFO");
				parametros_marcas_navegacion.put("MSG","ERROR "+codigo);
			
			}
		}
    	
    	result.put("trx_respuesta", trx_respuesta); 
    	result.put("trx_datos_respuesta", "{}");
    	
    }catch(Exception ex){
    	fEPCS.Debug("["+jspName+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	result.put("Var_SALDO", saldo); 
		result.put("Var_SALDO_VIGENCIA", fechaVencimiento+""); 

		result.put("cliente_datos", cliente_datos);    	
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
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>