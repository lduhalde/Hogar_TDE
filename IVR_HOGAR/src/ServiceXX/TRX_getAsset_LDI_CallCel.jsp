<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {
    
	/*
	 * autor: Gabriel Santis Villalon
	 * Fecha: 2020-02-12
	 * Descripción: Busca flag y datos relacionados para activar servicio LDI/Llamada Celular 
	 *              
	*/ 		
	
    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();	    

//     JSONObject parametros_marcas_navegacion = new JSONObject();
 	JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();    	

    String jspName="TRX_getAsset_LDI_CallCel";  
    
    String trx_respuesta = "NOK";
    
    String Product_poBundle = "";
    String Product_name = "";
    String Product_id = "";
    String serviceSpecification = "";
    String serviceId = "";
    String serviceEnabled = "";
    String rfsSpecification = "";
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	cliente_productos = (state.has("cliente_productos") ) ? state.getJSONObject("cliente_productos") : new JSONObject();
    	
    	fEPCS.Debug("["+jspName+"] INICIO", "INFO");

    	String processCode = additionalParams.get("processCode");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	String filterBlockNumber = additionalParams.get("filterBlockNumber");
    	String filterName = additionalParams.get("filterName");
    	fEPCS.Debug("["+jspName+"] processCode: "+processCode, "INFO");
    	fEPCS.Debug("["+jspName+"] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("["+jspName+"] idLlamada: "+idLlamada, "INFO");
   
    	fEPCS.Debug("["+jspName+"] parametros_marcas_navegacion: "+parametros_marcas_navegacion, "INFO");
    	fEPCS.Debug("["+jspName+"] cliente_datos: "+cliente_datos, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_ASSET");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
    	
    	String description = "";
		String status = ""; 	
		
    	Product = null;
    	Service = null;
    	ResourceFacingService = null;
    	serviceCharacteristic = null;
		
		String sTrx_datos_respuesta=fEPCS.GetAsset(Body, idLlamada, processCode, sourceID);    	
    	if(!respJSON.isNull("faultstring")) {
    		
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("["+jspName+"] GSV LOG ### Fault description: "+description, "INFO"); 
    		
			if(!respJSON.isNull("detail")) {
				
				if(!respJSON.getJSONObject("detail").getJSONObject("GetAsset_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("GetAsset_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("["+jspName+"] GSV LOG ### Fault STATUS: "+status, "INFO");
				}
				
			}
			
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			
			fEPCS.Debug("["+jspName+"] GSV LOG ### Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
			
		}else{ 
						
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");								
			
			fEPCS.Debug("["+jspName+"] GSV LOG ### Result.status: "+status, "INFO");
			fEPCS.Debug("["+jspName+"] GSV LOG ### Result.description: "+description, "INFO");
			
			//### Products
			JSONArray Products = respJSON.getJSONObject("Body").getJSONArray("Product");	
			
			Product = (JSONObject) Products.get(0);				
			Product_poBundle = Product.getString("name");
			
			Product = (JSONObject) Products.get(1);				
			Product_name = Product.getString("name");
			Product_id = Product.getString("id");  
			
			fEPCS.Debug("["+jspName+"] GSV LOG ### Product(1).MSISDN.SN: " + Product.getJSONObject("MSISDN").getString("SN"), "INFO");
			fEPCS.Debug("["+jspName+"] GSV LOG ### Product(1).Descripcion: " + Product.getString("description"), "INFO");
			fEPCS.Debug("["+jspName+"] GSV LOG ### Product(1).productStatus: " + Product.getString("productStatus"), "INFO");
			
			fEPCS.Debug("["+jspName+"] GSV LOG ### Product(0).name: "+Product_poBundle, "INFO");		
			
			fEPCS.Debug("["+jspName+"] GSV LOG ### Product(1).name: "+Product_name, "INFO");
			fEPCS.Debug("["+jspName+"] GSV LOG ### Product(1).id: "+Product_id, "INFO");    			
			
			//### Services	
			JSONArray Services = Product.getJSONArray("Service");	
			
			for (int s = 0; s < Services.length(); s++) {
			
				Service = (JSONObject) Services.get(s);
				
    			serviceSpecification = Service.getString("serviceSpecification");
    			serviceId = Service.getString("id");	  			
				
				//### ResourceFacingServices
				JSONArray ResourceFacingServices = Service.getJSONArray("ResourceFacingService");					
				
				for (int rf = 0; rf < ResourceFacingServices.length(); rf++) {
					
					ResourceFacingService = (JSONObject) ResourceFacingServices.get(rf);
					
					rfsSpecification = ResourceFacingService.getString("rfsSpecification");
					
//					fEPCS.Debug("["+jspName+"] GSV LOG ### .....ResourceFacingService("+ rf +").rfsSpecification: " + rfsSpecification, "INFO");

					if ( ResourceFacingService.getString("rfsSpecification").equalsIgnoreCase("RFSS_HG_CL_BAR_VOZ_INTE") ) {	
						
						serviceEnabled = Service.getString("enabled");
						
						//### serviceCharacteristics
						JSONArray serviceCharacteristics = ResourceFacingService.getJSONArray("serviceCharacteristics");
						serviceCharacteristic = (JSONObject) serviceCharacteristics.get(0);		    		
						
		    			fEPCS.Debug("["+jspName+"] GSV LOG ### ...Services("+ s +").enabled: " + serviceEnabled, "INFO");
		    			fEPCS.Debug("["+jspName+"] GSV LOG ### ...Services("+ s +").ID: " + serviceId, "INFO");
		    			fEPCS.Debug("["+jspName+"] GSV LOG ### ...Services("+ s +").serviceSpecification: " + serviceSpecification, "INFO");
		    			
		    			fEPCS.Debug("["+jspName+"] GSV LOG ### .....ResourceFacingService("+ rf +").rfsSpecification: " + rfsSpecification, "INFO");
		    				    			
						if ( !serviceCharacteristic.isNull("name") ) {
							
							String serviceCharacteristic_name = serviceCharacteristic.getString("name");
							String serviceCharacteristic_value = serviceCharacteristic.getString("value");
							
			    			fEPCS.Debug("["+jspName+"] GSV LOG ### .......serviceCharacteristics.name: " + serviceCharacteristic_name);
			    			fEPCS.Debug("["+jspName+"] GSV LOG ### .......serviceCharacteristics.value: " + serviceCharacteristic_value);	
			    			
						}else {
							
							fEPCS.Debug("["+jspName+"] GSV LOG ### .......serviceCharacteristics NO se encuentra flag: barVoiceInt", "INFO");
							
						}
						
						trx_respuesta = "OK";
						
						break;
		    					    						    		
					}
					
				}
				//### Fin ResourceFacingServices
				
				if ( trx_respuesta.equalsIgnoreCase("OK") ) break;
				
			}//### Fin Services
			
		}
    	
    	fEPCS.Debug("["+jspName+"] GSV LOG ### trx_respuesta: " + trx_respuesta, "INFO");
		
		
    }catch(Exception ex){
    	
    	fEPCS.Debug("["+jspName+"] GSV LOG ### Error : "+ex.getMessage(), "INFO");
    	ex.printStackTrace();
    	
    }finally{
    	
    	result.put("trx_respuesta", trx_respuesta);
    	result.put("Product_poBundle", Product_poBundle);
    	result.put("Product_name", Product_name);
    	result.put("Product_id", Product_id);
    	result.put("serviceSpecification", serviceSpecification);
    	result.put("serviceId", serviceId);
    	result.put("serviceEnabled", serviceEnabled);
    	result.put("rfsSpecification", rfsSpecification);	
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+jspName+"] GSV LOG ### FIN result: "+result.toString(), "INFO");
    	   	
    	result.put("cliente_productos", cliente_productos);
    	
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+jspName+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+jspName+"] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON = null;
    	cliente_datos = null;    	

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