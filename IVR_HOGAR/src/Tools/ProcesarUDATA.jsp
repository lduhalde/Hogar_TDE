<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>
<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
        
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));  
    String trx_respuesta = "OK"; 
    try{
     	String contractID		="";
		String ICCID			="";
		String IMSI				="";
// 		String customerAccountID="";
// 		String IndividualIdentification="";
		String billingCycle		="";
		String bscsBillingAccountId="";
// 		String BillingAccount	="";
// 		String taxDocumentID	="";
		String bscsCustomerId	="";
// 		String BillingAccountPO	="";
// 		String segmento			="";
// 		String PO_PLAN			="";
// 		String Var_SALDO		="";
// 		String Var_SALDO_CONJUNTO="";
		
    	String separador = additionalParams.get("separador");
    	String VarIN = additionalParams.get("VarIN");
    	fEPCS.Debug("[ProcesarUDATA] VarIN: "+VarIN, "INFO");
    	fEPCS.Debug("[ProcesarUDATA] cliente_datos: "+state.get("cliente_datos"), "INFO");
    	fEPCS.Debug("[ProcesarUDATA] parametros_marcas_navegacion: "+state.getString("parametros_marcas_navegacion"), "INFO");
    	fEPCS.Debug("[ProcesarUDATA] separador: "+separador, "INFO");
    	String clDatos =state.getString("cliente_datos");
    	String pMarcas =state.getString("parametros_marcas_navegacion");
      	JSONObject cliente_datos = (!clDatos.equals("") && clDatos != null) ? new JSONObject(clDatos) : new JSONObject();
      	JSONObject parametros_marcas_navegacion = (!pMarcas.equals("") && pMarcas !=null) ? new JSONObject(pMarcas) : new JSONObject();
     	JSONObject out = new JSONObject();

    	fEPCS.Debug("[ProcesarUDATA] parametros_marcas_navegacion: "+parametros_marcas_navegacion, "INFO");
		if(!VarIN.isEmpty()) {
			String[] elementos = VarIN.split(separador);
			int a = elementos.length;
			//System.out.println("Q: "+a);
			if(a%2==0){ //Validar que los elementos son pares		
					
				for(int i=0; i<a;i++) {
					String key=elementos[i];
					i++;
					String value=elementos[i];
					
					//Manipulacion de String Paso Para recuperar las Variables
					if (key.equals("parametros_marcas")) {
 						fEPCS.Debug("[ProcesarUDATA] ELSE: KEY ["+key+"] - Value ["+value+"]", "INFO");
 						parametros_marcas_navegacion = new JSONObject(value.replace("\\", "")); 
					}else if(key.equals("CONTRACT_OBJ")) {
						value=value.replace("#", "\"");
						fEPCS.Debug("[ProcesarUDATA] CONTRACT_OBJ: "+value, "INFO");
						JSONObject obj = new JSONObject(value);
						out.put(key,obj);
						
						contractID	= obj.getString("ID");
						ICCID		= obj.getJSONObject("MSISDN").getJSONObject("SIMCard").getString("ICCID");
						IMSI 		= obj.getJSONObject("MSISDN").getJSONObject("IMSI").getString("SN");

						cliente_datos.put("ICCID", ICCID);
						cliente_datos.put("IMSI", IMSI);
						cliente_datos.put("contractID", contractID);
						
					}else if (key.equals("TAXDOCUMENTID")) {
						value=value.replace("#", "\"");
						JSONObject obj = new JSONObject(value);
						out.put(key,obj);
					
						cliente_datos.put("taxDocumentID",obj.toString());
						
					}else if (key.equals("INDIVIDUALIDENTIFICATION")) {
						value=value.replace("#", "\"");
						JSONObject obj = new JSONObject(value);
						out.put(key,obj);
						
						cliente_datos.put("IndividualIdentification",obj.toString());
						
					}else if (key.equals("BILLINGACCOUNT")) {
						value=value.replace("#", "\"");
						JSONObject obj = new JSONObject(value);
						out.put(key,obj);
						
						cliente_datos.put("BillingAccount",obj.toString());
						
					}else if (key.equals("BILLINGACCOUNTPO")) {
						value=value.replace("#", "\"");
						JSONObject obj = new JSONObject(value);
						out.put(key,obj);
						
						cliente_datos.put("BillingAccountPO",obj.toString());
					}else if (key.equals("CUSTOMERACCOUNTID")) {
						out.put(key,value);
						
						cliente_datos.put("CustomerAccountID",value);
					}else {
						fEPCS.Debug("[ProcesarUDATA] ELSE: KEY ["+key+"] - Value ["+value+"]", "INFO");
						out.put(key,value);
					}
				}
			}else {
				fEPCS.Debug("[ProcesarUDATA] ERROR: Variable no cumple la condicion de Elemntos Pares", "INFO");
			}
		}
		fEPCS.Debug("[ProcesarUDATA] out: "+out.toString(), "INFO");
		
		if(!out.isNull("Var_Tipo_Temp")){
			cliente_datos.put("mercado",out.getString("Var_Tipo_Temp"));
		}
		
		
		
		if(!out.isNull("RUT") || !out.isNull("RutClientePCS")){
			String r = "";
			if(!out.isNull("RutClientePCS")){
				r = out.getString("RutClientePCS");
				if(r.indexOf("-") == -1){
					char dv = r.charAt(r.length()-1);
			    	String rutsindv = r.substring(0,r.length()-1);
			    	r= rutsindv+"-"+dv;
				}
			}else{
				r = out.getString("RUT");
			}
			cliente_datos.put("RUT",r);
			JSONObject IndividualIdentification = new JSONObject();
			IndividualIdentification.put("number",r);
			IndividualIdentification.put("type","RUT");
			cliente_datos.put("IndividualIdentification",IndividualIdentification);
			IndividualIdentification=null;
		}
		
		if(!out.isNull("Var_Tipo_Call")){
			result.put("Var_Tipo_Call",out.getString("Var_Tipo_Call"));
		}
		
		if(!out.isNull("Var_PCS")){ 
			result.put("Var_PCS",out.getString("Var_PCS"));
		}else{
			result.put("Var_PCS",state.getString("ANI"));
		}
		
		if(!out.isNull("billingAccountStatus")){
			cliente_datos.put("billingAccountStatus",out.getString("billingAccountStatus"));
		}
		if(!out.isNull("CUSTOMERACCOUNTID")){
			cliente_datos.put("customerAccountID",out.getString("CUSTOMERACCOUNTID"));
			result.put("customerAccountID",out.getString("CUSTOMERACCOUNTID"));
		}
		
		if(!out.isNull("PO_PLAN")){
			cliente_datos.put("PO_PLAN",out.getString("PO_PLAN"));
			
		} 
		if(!out.isNull("MERCADO")){
			cliente_datos.put("mercado",out.getString("MERCADO"));
			fEPCS.Debug("[ProcesarUDATA] MERCADO UDATA"+out.getString("MERCADO"), "INFO");
		}
		
		if(!out.isNull("SEGMENTO")){
			cliente_datos.put("segmento",out.getString("SEGMENTO"));
			
		}
		if(!out.isNull("SALDO_PP")){
			cliente_datos.put("Var_SALDO",out.getString("SALDO_PP"));
			cliente_datos.put("Var_SALDO_CONJUNTO",out.getString("SALDO_PP"));
			result.put("Var_SALDO",out.getString("SALDO_PP"));
			result.put("Var_SALDO_CONJUNTO",out.getString("SALDO_PP"));
		}
		if(!out.isNull("idCanal")){
			result.put("idCanal",out.getString("idCanal"));
			
		}
		
		if(parametros_marcas_navegacion==null){
			parametros_marcas_navegacion = new JSONObject(pMarcas);
		}
	  	parametros_marcas_navegacion.put("appName",state.getString("appName"));
		fEPCS.Debug("[ProcesarUDATA] cliente_datos: "+cliente_datos.toString(), "INFO");
		fEPCS.Debug("[ProcesarUDATA] parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
		
		if(!parametros_marcas_navegacion.isNull("TRAZA_CONTADOR")){
			out.put("SECUENCIA",parametros_marcas_navegacion.getInt("TRAZA_CONTADOR"));
		}
		 
	
		result.put("cliente_datos", cliente_datos);
		
		
		result.put("contractID", contractID);
		result.put("ICCID", ICCID);
		result.put("IMSI", IMSI);
    	result.put("OUT",out);
    	result.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	                        
    }catch (Exception ex){
    	fEPCS.Debug("[ProcesarUDATA] Hubo un ERROR : Crear_Traza "+ex.getMessage());
    	ex.printStackTrace();
    	trx_respuesta="NOK";
    }finally{
    	result.put("trx_respuesta",trx_respuesta);
    }
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>