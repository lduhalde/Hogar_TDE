<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
    JSONObject cliente_productos = new JSONObject();
    JSONArray arrayBundle = new JSONArray();
    String jspName="TRX_getAsset_MSISDN";
    
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
    	
    	String trx_respuesta = "NOK";
    	String description = "";
		String codeCanonical = "";
		String status = ""; 
		
		if(cliente_datos.has("PCS_Seleccionado")){
			JSONObject bundle = cliente_datos.getJSONObject("bundle_seleccionado");
			String technology = bundle_seleccionado.getString("technology");
			String CFS = fEPCS.Params.GetValue("CFSS_"technology.toUpperCase(),"CFSS_HG_BARRING_POS_PRE_TFI");
			String rfsLDI = fEPCS.Params.GetValue("RFS_LDI_"technology.toUpperCase(),"RFSS_HG_CL_BAR_VOZ_INTE");
			String rfsCEL = fEPCS.Params.GetValue("RFS_CEL_"technology.toUpperCase(),"RFSS_HG_CL_BAR_NUMBER_MOBILE");
			
			JSONObject Body = new JSONObject();
			JSONObject Product = new JSONObject();
			JSONObject Filter = new JSONObject();
			JSONObject ProductAccount = new JSONObject();
			JSONObject MSISDN = new JSONObject();
			
			MSISDN.put("SN",cliente_datos.getString("PCS_Seleccionado"));
			ProductAccount.put("MSISDN", MSISDN);
			Product.put("ProductAccount", ProductAccount);
			
			if(!filterBlockNumber.equalsIgnoreCase("")){
				Filter.put("blockNumber", filterBlockNumber);
			}
			if(!filterName.equalsIgnoreCase("")){
				Filter.put("name", filterName);
			}
			Product.put("Filter", Filter);
			Body.put("Product",Product);
			
			String sTrx_datos_respuesta=fEPCS.GetAsset(Body, idLlamada, processCode, sourceID);
	    	//fEPCS.Debug("["+jspName+"] sTrx_datos_respuesta: "+ sTrx_datos_respuesta, "INFO");
	    	
	    	Body = null;
	    	Product = null;
	    	Filter = null;
	    	ProductAccount = null;
	    	MSISDN = null;
	    	
	    	respJSON = new JSONObject(sTrx_datos_respuesta);
	    	
	    	if(!respJSON.isNull("faultstring")) {
	    		description = respJSON.getString("faultstring");
	    		fEPCS.Debug("["+jspName+"] Fault description: "+description, "INFO"); 
				if(!respJSON.isNull("detail")) {
					if(!respJSON.getJSONObject("detail").getJSONObject("GetAsset_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
						status = respJSON.getJSONObject("detail").getJSONObject("GetAsset_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
						fEPCS.Debug("["+jspName+"] Fault STATUS: "+status, "INFO");
					}
				}
			}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
				fEPCS.Debug("["+jspName+"] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
			}else{ 
				status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
				fEPCS.Debug("["+jspName+"] STATUS: "+status, "INFO");
				
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
					fEPCS.Debug("["+jspName+"] DESCRIPTION: "+description, "INFO"); 
				
				}
				
				if(status.equalsIgnoreCase("OK")){ 
					trx_respuesta = "OK"; 
					parametros_marcas_navegacion.put("RC","0");

					if(!respJSON.getJSONObject("Body").isNull("Product")){
						JSONArray Products = respJSON.getJSONObject("Body").getJSONArray("Product");
		        		//JSONObject Product = (JSONObject) Products.get(0);
		        		for(int j=0;j<Products.length();j++){
		        			String productStatus = Products.get(j).getString("productStatus");
		        			JSONObject PO = Product.getJSONObject("ProductOffering");
		        			
		        			if(productStatus.equalsIgnoreCase("ACTIVE")){
		        				if(PO.getString("specificationSubtype").equals("VBundle")){
		        					JSONArray ProductRelationships = Product.getJSONObject("ProductSpecification").getJSONArray("productRelationships");
		        					for(int pr=0;pr<ProductRelationships.length();pr++){
		        						JSONArray Services = ProductRelationships.getJSONObject(pr).getJSONArray("Service");
		        						for(int s=0;s<Services.length();s++){
		        							if(CFS.indexOf(Services.getJSONObject(s).getString("serviceSpecification"))>-1){
		        								JSONArray serviceCharacteristics = Services.getJSONObject(s).getJSONArray("serviceCharacteristics");
		        								String bloq_ldi = "true";
		        								String bloq_cel = "true";
		        								for(int sc=0; sc<serviceCharacteristics.length();sc++){
		        									if(serviceCharacteristics.getJSONObject(sc).getString("name").equals(rfsLDI)){
		        										bloq_ldi = serviceCharacteristics.getJSONObject(sc).getString("value");
		        									}
													if(serviceCharacteristics.getJSONObject(sc).getString("name").equals(rfsCEL)){
														bloq_cel = serviceCharacteristics.getJSONObject(sc).getString("value");
		        									}
		        								}
		        								bundle.put("BLOQUEO_LDI",bloq_ldi);
		        								bundle.put("BLOQUEO_CEL",bloq_cel);
		        								break;
		        							}
		        						}
		        						if(bundle.has("BLOQUEO_LDI")){
		        							break;
		        						}
		        					}
		        					
		        				}
		        			}
		        			if(bundle.has("BLOQUEO_LDI")){
    							break;
    						}
		        		}
		        		Products=null;
					}
				}else{ ///Error Controlado
					String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					result.put("CODE", codigo);
					fEPCS.Debug("["+jspName+"] CODE: "+codigo, "INFO");
					parametros_marcas_navegacion.put("MSG","ERROR "+codigo);				
				}
			} 
	    	result.put("trx_respuesta", trx_respuesta);
	    	cliente_datos.put("bundle_seleccionado",bundle);
			result.put("cliente_datos", cliente_datos);
			
			
		}else{
			fEPCS.Debug("["+jspName+"] SIN MSISDN", "INFO");
		}
	
    }catch(Exception ex){
    	fEPCS.Debug("["+jspName+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	cliente_productos.put("Bundles",arrayBundle);
    	result.put("cliente_productos", cliente_productos);
    	
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+jspName+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+jspName+"] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON = null;
    	cliente_datos = null;
    	arrayBundle = null;
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