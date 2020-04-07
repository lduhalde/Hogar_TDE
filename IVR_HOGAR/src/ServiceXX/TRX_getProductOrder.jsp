<%@page language="java" contentType="application/json;charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat"%>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    String codigoResp="";
    String trx_respuesta = "NOK";    
    
	String ProductOffering = "";
	String ItemServiceID = "";
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
//     FunctionsEPCS_Hogar fEPCS = new FunctionsEPCS_Hogar(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
		
// 		String ani = "56"+additionalParams.get("PCS");
		String shoppingCartID=additionalParams.get("shoppingCartID");
		
		String PO_BASIC=additionalParams.get("PO_BASIC");
		
		
    	fEPCS.Debug("[TRX_getProductOrder] INICIO", "INFO");
//     	fEPCS.Debug("[TRX_getProductOrder] ANI: "+ani, "INFO");
    	fEPCS.Debug("[TRX_getProductOrder] shoppingCartID: "+shoppingCartID, "INFO");
    	
    	String processCode = additionalParams.get("processCode");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	fEPCS.Debug("[TRX_getProductOrder] processCode: "+processCode, "INFO");
    	fEPCS.Debug("[TRX_getProductOrder] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_getProductOrder] idLlamada: "+idLlamada, "INFO");

    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_PRODUCTORDER");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");

    	String sTrx_datos_respuesta=fEPCS.getProductOrder(shoppingCartID,sourceID, processCode, idLlamada);
    	fEPCS.Debug("[TRX_getProductOrder] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
		respJSON = new JSONObject(sTrx_datos_respuesta); 

    	
		if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			
			fEPCS.Debug("[TRX_getProductOrder] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO");
			
		}else{ 
			
			String status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				String description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("[TRX_getProductOrder] DESCRIPTION: "+description, "INFO");
			
			}
			
			fEPCS.Debug("[TRX_getProductOrder] STATUS: "+status); 
			
			if(status.equalsIgnoreCase("OK")){ 								
				
				if(!respJSON.getJSONObject("Body").getJSONObject("CustomerOrder").isNull("items")){
					
					parametros_marcas_navegacion.put("RC","0");
					
					trx_respuesta = "OK";
					
					JSONArray items = respJSON.getJSONObject("Body").getJSONObject("CustomerOrder").getJSONArray("items");						
				
					for(int i=0; i< items.length(); i++){
						
						JSONObject item = (JSONObject) items.get(i);
						
						ProductOffering = item.getJSONObject("item").getJSONObject("ProductOffering").getString("ID");
						
// 						fEPCS.Debug("[TRX_getProductOrder] ProductOffering: "+ ProductOffering, "INFO");
						
						if (ProductOffering.equalsIgnoreCase(PO_BASIC)) {
							
							ItemServiceID = item.getJSONObject("item").getString("id");

							fEPCS.Debug("[TRX_getProductOrder] ProductOffering: "+ ProductOffering, "INFO");
							fEPCS.Debug("[TRX_getProductOrder] ItemServicdID: "+ ItemServiceID, "INFO");
							
							break;
						
						}

					}
					items=null;
					
				}else{
					
					trx_respuesta = "NOK";
					
				}
				
				if(!respJSON.getJSONObject("Body").getJSONObject("CustomerOrder").isNull("oneTimePrice")){
					
					String price = respJSON.getJSONObject("Body").getJSONObject("CustomerOrder").getJSONObject("oneTimePrice").getString("basePrice");
					result.put("Price",price);
					
					fEPCS.Debug("[TRX_getProductOrder] Price: "+price);
					
				}
			}else{
				
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").isNull("code")){
					codigoResp = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					fEPCS.Debug("[TRX_getProductOrder] Código: "+codigoResp, "INFO");
				}
				
				fEPCS.Debug("[TRX_getProductOrder] Error. Código: "+codigoResp, "INFO");
				
			}
		}
    	
    }catch(Exception ex){
    	fEPCS.Debug("[TRX_getProductOrder] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	result.put("trx_respuesta", trx_respuesta);
    	result.put("ItemServiceID", ItemServiceID);
    	
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_getProductOrder] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_getProductOrder] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON=null;
    }
    
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf"%>