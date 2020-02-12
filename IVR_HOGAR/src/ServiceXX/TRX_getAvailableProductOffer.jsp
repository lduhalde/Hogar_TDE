<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    int QOfertas=0;
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    String trx_respuesta = "NOK";
    try{
		String ani = "56"+state.getString("ANI");
		String Plan = additionalParams.get("Plan");
		String subfamily = additionalParams.get("subfamily");
		String Tipo = additionalParams.get("Tipo");
		String subTipo = additionalParams.get("SubTipo");
		String subcategory= additionalParams.get("subcategory");
		String prePayment= additionalParams.get("prePayment");
		String maxAmountAllowed= additionalParams.get("maxAmountAllowed");
		String segmento= additionalParams.get("segmento");
		
		if(subTipo==null){
			subTipo="Service";
		}
		
    	fEPCS.Debug("[TRX_getAvailableProductOffer] INICIO", "INFO");
    	fEPCS.Debug("[TRX_GetAvailableProductOffer] ANI: "+ani, "INFO");
    	fEPCS.Debug("[TRX_GetAvailableProductOffer] TIPO: "+Tipo, "INFO");
    	fEPCS.Debug("[TRX_GetAvailableProductOffer] subTipo: "+subTipo, "INFO");
    	fEPCS.Debug("[TRX_GetAvailableProductOffer] Plan: "+Plan, "INFO");
    	String marketSegment = additionalParams.get("marketSegment");
    	String processCode = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	fEPCS.Debug("[TRX_GetAvailableProductOffer] processCode: "+processCode, "INFO");
    	fEPCS.Debug("[TRX_GetAvailableProductOffer] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_GetAvailableProductOffer] idLlamada: "+idLlamada, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_AVAILABLEPRODUCTOFFER");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
    	
    	
    	String status="NOK";
    	String description = "";
    	String codeCanonical = "";
    	
    	String sTrx_datos_respuesta=fEPCS.GetAvailableProductOffer(marketSegment,subTipo,Tipo,"Mobile",subfamily,ani,idLlamada, processCode,sourceID, prePayment, maxAmountAllowed,segmento,subcategory,Plan);
    	//fEPCS.Debug("[TRX_GetAvailableProductOffer] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
    	
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("[TRX_GetAvailableProductOffer] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("GetAvailableProductOffer_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("GetAvailableProductOffer_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("[TRX_GetAvailableProductOffer] Fault STATUS: "+status, "INFO");
				}
			}
			result.put("AvailableProduct","");
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("[TRX_GetAvailableProductOffer] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("[TRX_GetAvailableProductOffer] STATUS: "+status, "INFO");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("[TRX_GetAvailableProductOffer] DESCRIPTION: "+description, "INFO"); 
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").isNull("description")){
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
					codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
					fEPCS.Debug("[TRX_GetAvailableProductOffer] Canonical code: "+codeCanonical, "INFO");
					fEPCS.Debug("[TRX_GetAvailableProductOffer] Canonical Description: "+description, "INFO"); 
				}
			}
			 
			if(status.equalsIgnoreCase("OK") && codeCanonical.equals("A3")){ 
				trx_respuesta = "OK";  
				parametros_marcas_navegacion.put("RC","0");
				//Recuperar los Productos a Ofrecer
				 JSONObject var1= (JSONObject) respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("ProductOffering").get(0);
				 JSONArray var2= var1.getJSONArray("ProductSpecification");
				 String var3="";
				 for(int i=0; i< var2.length(); i++){
			        	JSONObject var4 = (JSONObject) var2.get(i);
			        	var3+=var4.getString("id")+",";
			        	var4=null;
			        	QOfertas++;
			     }
				 
				 //Se excluye esta validacion para agregar Numeros Favoritos porque no recupera estos campos
				 if(!subTipo.equals("FAF")){
					 //Recupera el Precio del Producto
					 JSONObject varpre1= (JSONObject) respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("ProductOffering").get(0);
					 JSONArray varpre2= varpre1.getJSONArray("ProductSpecification");
					 String precioBolsa="";
					 String precioPlan="";
					 for(int i=0; i< varpre2.length(); i++){
				        	JSONObject varpre3 = (JSONObject) varpre2.get(i);
				        	JSONArray varpre4 = varpre3.getJSONArray("ProductOfferingPrice");
				        	int precio = 0;
				        	for(int a=0; a<varpre4.length(); a++){
				        		if(((JSONObject) varpre4.get(a)).getString("priceType").equalsIgnoreCase("One-Time")){
				        			JSONObject varpre5=(JSONObject) varpre4.get(a);
					        		JSONObject varpre6=(JSONObject) varpre5.getJSONObject("amount");
					        		precioBolsa += varpre6.getString("amount")+",";
					        		result.put("precioBolsa",precioBolsa);
				        		}
				        		if(varpre4.getJSONObject(a).getString("priceType").equalsIgnoreCase("Recurring") && varpre4.getJSONObject(a).getString("description").equals("POP_PLAN_RECURRING")){
				        			//fEPCS.Debug("[TRX_GetAvailableProductOffer] Precio Recurring ("+i+"): "+varpre4.getJSONObject(a).getJSONObject("amount").getInt("amount"), "INFO");
				        			precio += varpre4.getJSONObject(a).getJSONObject("amount").getInt("amount");
				        			//fEPCS.Debug("[TRX_GetAvailableProductOffer] Precio: "+precio, "INFO");
				        		}
				    
				        	}
				        	precioPlan += precio+",";
				        	precio = 0;
				        	varpre4=null;
						 	varpre3=null;
				       	}
					 	varpre1=null;
					    varpre2=null;
					    result.put("precioPlan",precioPlan);
				 	}
				 
				 	
				 result.put("AvailableProduct",var3);
				 fEPCS.Debug("[TRX_GetAvailableProductOffer] QOfertas: "+QOfertas, "INFO");
				 fEPCS.Debug("[TRX_GetAvailableProductOffer] Productos Disponibles: "+var3, "INFO");
			    //FIN Recuperar los Productos a Ofrecer
			    var1=null;
			    var2=null;
			   
			}else{
				String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
				result.put("CODE", codigo);
				fEPCS.Debug("[TRX_GetAvailableProductOffer] CODE: "+codigo, "INFO");
				parametros_marcas_navegacion.put("MSG","ERROR "+codigo);
		
			}
		}
    	
		
    	
    	
    }catch(Exception ex){
    	trx_respuesta = "NOK";
    	fEPCS.Debug("[TRX_GetAvailableProductOffer] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	result.put("trx_respuesta", trx_respuesta);
    	//Devolver Q planes, se coloca en el finally para que soporte en FAF aquellos planes limitados
    	result.put("QOfertas",QOfertas);
    	
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_GetAvailableProductOffer] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_GetAvailableProductOffer] FIN result: "+result.toString(), "INFO");
    	
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
<%@include file="../../include/backend.jspf" %>