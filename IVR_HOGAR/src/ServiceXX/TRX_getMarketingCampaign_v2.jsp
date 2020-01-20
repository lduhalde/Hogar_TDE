<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>
<%@page import="org.joda.time.DateTime, org.joda.time.format.DateTimeFormat, org.joda.time.format.DateTimeFormatter"%>

<%!
/*
 * autor: Gabriel Santis Villalon
 * Fecha: 2019-05-02
 * Descripción: Se crea TRX para procesar Response devuelto por 
 *              WS GetMarketingCampaign/v2
*/ 
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    JSONObject respJSON = new JSONObject();
    
    JSONObject cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();

    String trx_respuesta = "NOK";

    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    String jspName="TRX_getMarketingCampaign_v2";
	
 	try{

    	String documentNumber = additionalParams.get("documentNumber");
		String msisdn = additionalParams.get("msisdn");

		String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");	
		
    	fEPCS.Debug("["+jspName +"] INICIO", "INFO");
    	fEPCS.Debug("["+jspName +"] documentNumber: "+documentNumber, "INFO");
    	fEPCS.Debug("["+jspName +"] msisdn: "+msisdn, "INFO");

    	fEPCS.Debug("["+jspName+"] processID: "+processID, "INFO");
    	fEPCS.Debug("["+jspName+"] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("["+jspName+"] idLlamada: "+idLlamada, "INFO");
    	fEPCS.Debug("["+jspName +"] parametros_marcas_navegacion: "+parametros_marcas_navegacion, "INFO");
		
		//llamada WS
		String sTrx_datos_respuesta=fEPCS.GetMarketingCampaign_v2(documentNumber, msisdn, idLlamada, processID, sourceID);

		//String sTrx_datos_respuesta="{ \"ResponseHeader\" : {   \"Consumer\" : {     \"sysCode\" : \"CHL-IVR\",     \"enterpriseCode\" : \"ENTEL-CHL\",     \"countryCode\" : \"CHL\"   },   \"Trace\" : {     \"clientReqTimestamp\" : \"2019-07-29T09:24:37.673-04:00\",     \"reqTimestamp\" : \"2019-07-29T09:24:37.684-04:00\",     \"rspTimestamp\" : \"2019-07-29T09:24:37.924-04:00\",     \"processID\" : null,     \"eventID\" : \"001391564406677UR1NUB9SCH28T3RKOPNDLM8G7O048TCM000\",     \"sourceID\" : \"UR1NUB9SCH28T3RKOPNDLM8G7O048TCM56966316255\",     \"correlationEventID\" : null,     \"conversationID\" : \"c2db6041-659d-4e87-ae41-82ba0cdbdd18\",     \"correlationID\" : null,     \"Service\" : {       \"code\" : \"SASS100139\",       \"name\" : \"GetMarketingCampaign\",       \"operation\" : \"GetMarketingCampaign\"     }   },   \"Channel\" : {     \"name\" : \"IVR\",     \"mode\" : \"NO PRESENCIAL\"   },   \"Result\" : {     \"status\" : \"OK\",     \"description\" : \"Transaccion que finaliza exitosamente.\",     \"CanonicalError\" : {       \"type\" : \"NEG\",       \"code\" : \"A3\",       \"description\" : \"OK - Procesamiento concluÃ­do exitosamente\"     }   } }, \"Body\" : {   \"Response\" : {     \"documentNumber\" : \"13563422-0\",     \"msisdn\" : \"56966316255\",     \"Campaign\" : [ {       \"name\" : \"P-7\",       \"id\" : null,       \"mcType\" : null,       \"subType\" : null,       \"source\" : null,       \"status\" : null,       \"answerType\" : \"Recarga\",       \"ValidFor\" : {         \"endDate\" : null,         \"startDate\" : null       },       \"CustomerAccount\" : {         \"Asset\" : {           \"MSISDN\" : {             \"sn\" : \"56966316255\"           }         },         \"Individual\" : {           \"IndividualIdentification\" : {             \"number\" : null           }         }       },       \"MarketSegment\" : null,       \"Offer\" : [ {         \"id\" : \"21519643590063878\",         \"name\" : \"Exig20Pct500MB7dOrExig40Pct800MB7d\",         \"description\" : \"3000or3500\",         \"msisdn\" : null       } ],       \"ServiceRequest\" : null     } ]   } }}";
		
		fEPCS.Debug("["+jspName +"] sTrx_datos_respuesta: "+ sTrx_datos_respuesta, "INFO");
		respJSON = new JSONObject(sTrx_datos_respuesta);		
    	
   		parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_GETMARKETINGCAMPAIGN_v2");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");	    	

		if(!respJSON.isNull("faultstring")) {
			
    		String description = respJSON.getString("faultstring");
    		String status = respJSON.getJSONObject("detail").getJSONObject("Result").getString("status");
    		String code = respJSON.getJSONObject("detail").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
    		String CanonicalError = respJSON.getJSONObject("detail").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");   
    		
			fEPCS.Debug("["+jspName+"] faultstring Status: "+status, "INFO");
			fEPCS.Debug("["+jspName+"] faultstring description: "+description, "INFO");				
			fEPCS.Debug("["+jspName+"] faultstring CanonicalError: "+CanonicalError, "INFO");				
			fEPCS.Debug("["+jspName+"] faultstring CODE: "+code, "INFO");	    		
    		
		}else{
							
			if( !respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("status") ) {
				
				String description="";
				String status="";	
				String CanonicalError="";
				
				String code ="";
				
				status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");				
				CanonicalError = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");						
				code = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");				
				
				fEPCS.Debug("["+jspName+"] Status: "+status, "INFO");
				fEPCS.Debug("["+jspName+"] description: "+description, "INFO");				
				fEPCS.Debug("["+jspName+"] CanonicalError: "+CanonicalError, "INFO");				
				fEPCS.Debug("["+jspName+"] CODE: "+code, "INFO");
				
				switch (status){
				case "OK":				
					if(code.equalsIgnoreCase("A3")){
						trx_respuesta = "OK";
						fEPCS.Debug("["+jspName+"] A3: Cliente con campañas", "INFO"); 
//						fEPCS.Debug("["+jspName+"] ***", "INFO"); 
						
						parametros_marcas_navegacion.put("RC","0");
						
						JSONObject Response = (JSONObject) respJSON.getJSONObject("Body").getJSONObject("Response");
						JSONArray Campaign = Response.getJSONArray("Campaign");
						
						String PO_OFERTA_PLANES = "";
						String OFFER_ID = "";
						int QOfertas=0;
						
						//Lista de campañas y planes a ofrecer
						for(int c=0; c < Campaign.length(); c++){
							
							JSONObject Campaign_item = (JSONObject) Campaign.get(c);
							
							if ( Campaign_item.optString("canal").length() == 0 ) {
								/*
								 * autor: Gabriel Santis Villalon
								 * Fecha: 2019-07-29
								 * Descripción: Se agrega validacion de response porque este fue cambiado y NO response como fue definido por arquitectrua o integracion
								 * Se evita un Falo positivo por response con menos campos o valores irregulares o inesperados		
								*/ 								
					        	fEPCS.Debug("["+jspName+"] ERROR: Campaign("+c+") NO cumple condiciones para IVR:", "INFO");
					        	fEPCS.Debug("["+jspName+"] -----> Campaign("+c+").Canal: ["+Campaign_item.optString("canal") + "] (debe ser -IVR-)");
					        	fEPCS.Debug("["+jspName+"] -----> Campaign("+c+").subType: ["+Campaign_item.optString("subType") + "] (debe ser -Cambio de plan-)");								

								PO_OFERTA_PLANES = "";
								OFFER_ID = "";
								QOfertas=0;
								
								trx_respuesta = "NOK";

								break;

							}

							if ( Campaign_item.getJSONObject("ValidFor").isNull("endDate") ) {

								fEPCS.Debug("["+jspName+"] Campaign("+c+") GSV LOG ### FECHA FINAL ES NULL", "INFO");

							}

							String startDate = Campaign_item.getJSONObject("ValidFor").getString("startDate");
							String endDate = Campaign_item.getJSONObject("ValidFor").getString("endDate");
							
//				        	DateTime factual = new DateTime();					        	
				        	DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
				        	DateTime fec_endDate = dtf.parseDateTime(endDate);
				        	 
//				        	fEPCS.Debug(fec_endDate.isBeforeNow());	
				        	
				        	//Fecha de campaña esta despues de hoy
				        	if ( fec_endDate.isBeforeNow() ){
				        		fEPCS.Debug("["+jspName+"] Campaign("+c+") Plazo caducado", "INFO");
					        	fEPCS.Debug("["+jspName+"] Campaign("+c+").ValidFor.startDate: "+startDate, "INFO");	
					        	fEPCS.Debug("["+jspName+"] Campaign("+c+").ValidFor.endDate: "+endDate, "INFO");	
				        		
							}else if (Campaign_item.getString("canal").equalsIgnoreCase("IVR") && Campaign_item.getString("subType").equalsIgnoreCase("Cambio de plan")){
								
								//Datos de campaña
								String name = Campaign_item.getString("name");
								String id = Campaign_item.getString("id");
								String mcType = Campaign_item.getString("mcType");
								String subType = Campaign_item.getString("subType");
								String source = Campaign_item.getString("source");
							
								result.put("campana_source", source);
								result.put("campana_id", id);
								result.put("campana_tipooferta", subType);
																	
								fEPCS.Debug("["+jspName+"] ***", "INFO"); 
								
					        	fEPCS.Debug("["+jspName+"] Campaign("+c+").name: "+name, "INFO"); 				        	
					        	fEPCS.Debug("["+jspName+"] Campaign("+c+").id: "+id, "INFO");				        	
					        	fEPCS.Debug("["+jspName+"] Campaign("+c+").mcType: "+mcType, "INFO"); 				        	
					        	fEPCS.Debug("["+jspName+"] Campaign("+c+").subType: "+subType, "INFO");	
					        	fEPCS.Debug("["+jspName+"] Campaign("+c+").source: "+source, "INFO");							        	
					        	fEPCS.Debug("["+jspName+"] Campaign("+c+").ValidFor.startDate: "+startDate, "INFO");	
					        	fEPCS.Debug("["+jspName+"] Campaign("+c+").ValidFor.endDate: "+endDate, "INFO");							        	
					        	
					        	//Ofertas
					        	JSONArray Offer = Campaign_item.getJSONArray("Offer");
					        	
					        	for(int o=0; o< Offer.length(); o++){
					        		JSONObject Offer_item = (JSONObject) Offer.get(o);
					        		
					        		name = Offer_item.getString("name");
									id = Offer_item.getString("id");	//OFFER
									
									OFFER_ID += id+",";
									
									result.put("OFFER_ID", OFFER_ID);
	
						        	fEPCS.Debug("["+jspName+"] Campaign("+c+").Offer("+o+").name: "+name, "INFO"); 				        	
						        	fEPCS.Debug("["+jspName+"] Campaign("+c+").Offer("+o+").id: "+id, "INFO");						        						        
						        						        	
						        	JSONArray Product  = Offer_item.getJSONArray("Product");
						        	
						        	//Productos
						        	for(int p=0; p< Product.length(); p++){
						        		JSONObject Product_item = (JSONObject) Product.get(p);
						        		
						        		name = Product_item.getString("name");
										id = Product_item.getString("id");	//PO PLAN
										
										PO_OFERTA_PLANES += id+",";													
										QOfertas++;
	
							        	fEPCS.Debug("["+jspName+"] Campaign("+c+").Offer("+o+").Product("+p+").name: "+name, "INFO"); 				        	
							        	fEPCS.Debug("["+jspName+"] Campaign("+c+").Offer("+o+").Product("+p+").id: "+id, "INFO");	
							        	
							        	result.put("PO_OFERTA_PLANES", PO_OFERTA_PLANES);
							        	result.put("QOfertas", QOfertas);
						        		
						        	}
						        	
						        	//con esta condición, solo se toman las 4 primeras ofertas
					        		if (QOfertas >= 3) {						        			
					        			break;
					        		}		
						        	
					        	}//for Ofertas
					        		
					        }else{
					        	fEPCS.Debug("["+jspName+"] Error: Campaign("+c+") NO cumple con condiciones para IVR:", "INFO");
					        	fEPCS.Debug("["+jspName+"] -----> Campaign("+c+").Canal: "+Campaign_item.getString("canal") + " (debe ser -IVR-)");
					        	fEPCS.Debug("["+jspName+"] -----> Campaign("+c+").subType: "+Campaign_item.getString("subType") + " (debe ser -Cambio de plan-)");
					        }			        
				        								
						}// for campañas
						
					}
					if(code.equalsIgnoreCase("A0")){
						fEPCS.Debug("["+jspName+"] A0: Cliente sin campañas","DEBUG"); 
					}						
				break;
				//****
				case "WARNING":					
					String details = "";
					
					code = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("description");				
					details = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getJSONObject("ErrorSourceDetails").getString("details");						
									
					fEPCS.Debug("["+jspName+"] SourceError code: "+code, "INFO");
					fEPCS.Debug("["+jspName+"] SourceError description: "+description, "INFO");				
					fEPCS.Debug("["+jspName+"] SourceError details: "+details, "INFO");						
				break;							
				}
				
				result.put("status", status);	
				
			} else {
				
				result.put("description", "ResponseHeader->Status es Nulo");
				fEPCS.Debug("["+jspName+"] Status: TRX_DESCONOCIDO", "INFO");
				fEPCS.Debug("["+jspName+"] Fault description: ResponseHeader->Status es Nulo", "INFO");	
				
			}
			
		}

    }catch(Exception ex){
    	
    	fEPCS.Debug("["+jspName+"] Error : "+ex.getMessage(), "INFO");
    	ex.printStackTrace();
    	
    }finally{
    	
    	result.put("trx_respuesta", trx_respuesta);
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("["+jspName+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("["+jspName+"] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON = null;
    	
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