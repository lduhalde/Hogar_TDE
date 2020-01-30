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
    String jspName="TRX_getAsset";
    
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
		
		if(cliente_datos.has("CustomerAccountID")){
			
			JSONObject Body = new JSONObject();
			JSONObject Product = new JSONObject();
			JSONObject Filter = new JSONObject();
			JSONObject Resource = new JSONObject();
			JSONObject Asset = new JSONObject();
			JSONObject CustomerAccount = new JSONObject();
			
			CustomerAccount.put("ID",cliente_datos.getString("CustomerAccountID"));
			Asset.put("CustomerAccount", CustomerAccount);
			Resource.put("Asset", Asset);
			Product.put("Resource", Resource);
			
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
	    	Resource = null;
	    	Asset = null;
	    	CustomerAccount = null;
	    	
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
					String BillingProfileId = "";
					String billingID = "";
					JSONArray addressArray = cliente_datos.getJSONArray("addressArray");
					JSONArray bundles = new JSONArray();
					JSONArray basics = new JSONArray();
					JSONArray planes = new JSONArray();
					JSONArray freeUnits = new JSONArray();
					JSONArray others = new JSONArray();
					JSONArray equipments = new JSONArray();
					JSONArray mercados = cliente_datos.getJSONArray("mercados");
					if(!respJSON.getJSONObject("Body").isNull("Product")){
						JSONArray Products = respJSON.getJSONObject("Body").getJSONArray("Product");
		        		//JSONObject Product = (JSONObject) Products.get(0);
		        		for(int j=0;j<Products.length();j++){
		        			Product = (JSONObject) Products.get(j);
		        			String productStatus = Product.getString("productStatus");
		        			JSONObject PO = Product.getJSONObject("ProductOffering");
		        			
		        			if(productStatus.equalsIgnoreCase("ACTIVE")){
		        				if(PO.getString("specificationSubtype").equals("VBundle")){
		        					
				        			//fEPCS.Debug("["+jspName+"] PO: "+PO.getString("ID"), "INFO");
				        			//fEPCS.Debug("["+jspName+"] specificationSubtype: "+PO.getString("specificationSubtype"), "INFO");
				        			
		        					JSONObject Bundle = new JSONObject();
		        					Bundle.put("ID",Product.getString("id"));
		        					Bundle.put("PO",PO.getString("ID"));
		        					if(Product.has("MSISDN")){
		        						Bundle.put("MSISDN",Product.getJSONObject("MSISDN").getString("SN"));
		        						for(int m=0;m<mercados.length();m++){
		        							JSONObject mercado = mercados.getJSONObject(m);
		        							if(mercado.getString("msisdn").equals(Product.getJSONObject("MSISDN").getString("SN"))){
		        								Bundle.put("mercado",mercado.getString("mercado"));
		        								break;
		        							}
		        							mercado = null;
		        						}
		        					}
		        					JSONObject PS = Product.getJSONObject("ProductSpecification");
		        					JSONArray ProductSpecCharacteristic = PS.getJSONArray("ProductSpecCharacteristic");
		        					for(int psc =0; psc<ProductSpecCharacteristic.length();psc++){
		        						if(ProductSpecCharacteristic.getJSONObject(psc).getString("name").equals("commercialName")){
		        							Bundle.put("commercialName",ProductSpecCharacteristic.getJSONObject(psc).getString("value"));
		        						}
		        						if(ProductSpecCharacteristic.getJSONObject(psc).getString("name").equals("bundleId")){
		        							Bundle.put("bundleId",ProductSpecCharacteristic.getJSONObject(psc).getString("value"));
		        						}
		        						if(ProductSpecCharacteristic.getJSONObject(psc).getString("name").equals("technology")){
		        							Bundle.put("technology",ProductSpecCharacteristic.getJSONObject(psc).getString("value"));
		        						}
		        						if(ProductSpecCharacteristic.getJSONObject(psc).getString("name").equals("serviceIdExt")){
		        							Bundle.put("serviceIdExt",ProductSpecCharacteristic.getJSONObject(psc).getString("value"));
		        						}
		        						if(ProductSpecCharacteristic.getJSONObject(psc).getString("name").equals("addressId")){
		        							String addressId=ProductSpecCharacteristic.getJSONObject(psc).getString("value");
		        							for(int a=0; a<addressArray.length();a++){
		        								if(addressArray.getJSONObject(a).getString("addressId").equals(addressId)){
		        									Bundle.put("address",addressArray.getJSONObject(a).getString("address"));
		        									Bundle.put("addressCommune",addressArray.getJSONObject(a).getString("addressComumune"));
		        									break;
		        								}
		        							}
		        						}
		        					}
		        					JSONArray ProductRelationships = PS.getJSONArray("productRelationships");
		        					String parentOf = "";
		        					for(int pr=0;pr<ProductRelationships.length();pr++){
		        						if(ProductRelationships.getJSONObject(pr).getString("type").equals("ParentOf")){
		        							parentOf+=ProductRelationships.getJSONObject(pr).getString("id")+",";
		        						}
		        					}
		        					Bundle.put("ParentOf",parentOf);
		        					bundles.put(Bundle);
		        					//fEPCS.Debug("["+jspName+"] Bundle: "+Bundle.toString(), "INFO");
		        					Bundle = null;
		        				}
		        				else if(PO.getString("specificationSubtype").equals("Basic")){
				        			//fEPCS.Debug("["+jspName+"] PO: "+PO.getString("ID"), "INFO");
				        			//fEPCS.Debug("["+jspName+"] specificationSubtype: "+PO.getString("specificationSubtype"), "INFO");
		        					JSONObject Basic = new JSONObject();
		        					Basic.put("ID",Product.getString("id"));
		        					Basic.put("PO",PO.getString("ID"));
		        					
		        					JSONObject PS = Product.getJSONObject("ProductSpecification");
		        					JSONArray ProductSpecCharacteristic = PS.getJSONArray("ProductSpecCharacteristic");
		        					for(int psc =0; psc<ProductSpecCharacteristic.length();psc++){
		        						if(ProductSpecCharacteristic.getJSONObject(psc).getString("name").equals("contractState")){
		        							Basic.put("contractState",ProductSpecCharacteristic.getJSONObject(psc).getString("value"));
		        						}
		        						if(ProductSpecCharacteristic.getJSONObject(psc).getString("name").equals("family")){
		        							Basic.put("family",ProductSpecCharacteristic.getJSONObject(psc).getString("value"));
		        							//fEPCS.Debug("["+jspName+"] Family: "+ProductSpecCharacteristic.getJSONObject(psc).getString("value"), "INFO");
		        						}
		        						if(ProductSpecCharacteristic.getJSONObject(psc).getString("name").equals("serviceIdExt")){
		        							Basic.put("serviceIdExt",ProductSpecCharacteristic.getJSONObject(psc).getString("value"));
		        						}
		        					}
		        					JSONArray ProductRelationships = PS.getJSONArray("productRelationships");
		        					String parentOf = "";
		        					for(int pr=0;pr<ProductRelationships.length();pr++){
		        						if(ProductRelationships.getJSONObject(pr).getString("type").equals("ParentOf") && parentOf.indexOf(ProductRelationships.getJSONObject(pr).getString("id"))==-1){
		        							parentOf+=ProductRelationships.getJSONObject(pr).getString("id")+",";
		        						}
		        					}
		        					Basic.put("ParentOf",parentOf);
		        					basics.put(Basic);
		        					//fEPCS.Debug("["+jspName+"] Basic: "+Basic.toString(), "INFO");
		        					Basic = null;
		        				}
		        				else if(PO.getString("specificationSubtype").equals("Plan")){
				        			//fEPCS.Debug("["+jspName+"] PO: "+PO.getString("ID"), "INFO");
				        			//fEPCS.Debug("["+jspName+"] specificationSubtype: "+PO.getString("specificationSubtype"), "INFO");
		        					JSONObject Plan = new JSONObject();
		        					Plan.put("ID",Product.getString("id"));
		        					Plan.put("PO",PO.getString("ID"));
		        					
		        					JSONObject PS = Product.getJSONObject("ProductSpecification");
		        					JSONArray ProductRelationships = PS.getJSONArray("productRelationships");
		        					String parentOf = "";
		        					for(int pr=0;pr<ProductRelationships.length();pr++){
		        						if(ProductRelationships.getJSONObject(pr).getString("type").equals("ParentOf") && parentOf.indexOf(ProductRelationships.getJSONObject(pr).getString("id"))==-1){
		        							parentOf+=ProductRelationships.getJSONObject(pr).getString("id")+",";
		        						}
		        					}
		        					
		        					Plan.put("ParentOf",parentOf);
		        					planes.put(Plan);
		        					//fEPCS.Debug("["+jspName+"] Plan: "+Plan.toString(), "INFO");
		        					Plan = null;
		        				}
		        				else if(PO.getString("specificationSubtype").equals("Free Units")){
				        			//fEPCS.Debug("["+jspName+"] PO: "+PO.getString("ID"), "INFO");
				        			//fEPCS.Debug("["+jspName+"] specificationSubtype: "+PO.getString("specificationSubtype"), "INFO");
		        					JSONObject FreeUnits = new JSONObject();
		        					FreeUnits.put("ID",Product.getString("id"));
		        					FreeUnits.put("PO",PO.getString("ID"));
		        					
		        					JSONObject PS = Product.getJSONObject("ProductSpecification");
		        					JSONArray ProductSpecCharacteristic = PS.getJSONArray("ProductSpecCharacteristic");
		        					freeUnits.put(FreeUnits);
		        					//fEPCS.Debug("["+jspName+"] Free Units: "+FreeUnits.toString(), "INFO");
		        					FreeUnits = null;
		        				}else if(PO.getString("specificationSubtype").equals("Equipment")){
		        					fEPCS.Debug("["+jspName+"] PO FULL: "+PO.toString(), "INFO");
				        			fEPCS.Debug("["+jspName+"] PO: "+PO.getString("ID"), "INFO");
				        			fEPCS.Debug("["+jspName+"] specificationSubtype: "+PO.getString("specificationSubtype"), "INFO");
		        					JSONObject Equipment = new JSONObject();
		        					Equipment.put("ID",Product.getString("id"));
		        					Equipment.put("PO",PO.getString("ID"));
		        					
		        					JSONObject PS = Product.getJSONObject("ProductSpecification");
		        					JSONArray ProductSpecCharacteristic = PS.getJSONArray("ProductSpecCharacteristic");
		        					for(int psc =0; psc<ProductSpecCharacteristic.length();psc++){
		        						if(ProductSpecCharacteristic.getJSONObject(psc).getString("name").equals("corelativo")){
		        							Equipment.put("correlativo",ProductSpecCharacteristic.getJSONObject(psc).getString("value"));
		        						}
		        					}
		        					equipments.put(Equipment);
		        					fEPCS.Debug("["+jspName+"] Equipment: "+Equipment.toString(), "INFO");
		        					Equipment = null;
		        				}else{
		        					//fEPCS.Debug("["+jspName+"] PO: "+PO.getString("ID"), "INFO");
				        			//fEPCS.Debug("["+jspName+"] specificationSubtype: "+PO.getString("specificationSubtype"), "INFO");
		        					JSONObject Other = new JSONObject();
		        					Other.put("ID",Product.getString("id"));
		        					Other.put("PO",PO.getString("ID"));
		        					Other.put("specificationSubtype",PO.getString("specificationSubtype"));
		        					others.put(Other);
		        					//fEPCS.Debug("["+jspName+"] Other: "+Other.toString(), "INFO");
		        					Other = null;
		        				}
		        			}
		        			Product=null;
		        		}
		        		Products=null;
					}
					
					for(int i=0;i<bundles.length();i++){ //Recorremos los VBundles
						JSONObject bundle = bundles.getJSONObject(i);
						String bundleParentOf = bundle.getString("ParentOf");
						JSONArray arrayBasic = new JSONArray();
						fEPCS.Debug("["+jspName+"] bundle: "+bundle.toString(), "INFO");
						String family="";
						for(int j=0;j<basics.length();j++){ //Recorre los Basics
							JSONObject basic = basics.getJSONObject(j);
							String idBasic = basic.getString("ID");
							//fEPCS.Debug("["+jspName+"] basic: "+basic.toString(), "INFO");
							if(bundleParentOf.indexOf(idBasic)>-1){ //Recorre los Planes
								String basicParentOf = basic.getString("ParentOf");
								if(!basic.getString("family").equals("Access")){
									family+=basic.getString("family")+"|";
									for(int k=0;k<planes.length();k++){
										JSONObject plan = planes.getJSONObject(k);
										String idPlan = plan.getString("ID");
										if(basicParentOf.indexOf(idPlan)>-1){
											String planParentOf = plan.getString("ParentOf");
											for(int l=0;l<freeUnits.length();l++){//Recorre los Free Units
												JSONObject freeunits = freeUnits.getJSONObject(l);
												String idFreeUnits = freeunits.getString("ID");
												if(planParentOf.indexOf(idFreeUnits)>-1){
													plan.put("FreeUnits",freeunits);
													//fEPCS.Debug("["+jspName+"] plan: "+plan.toString(), "INFO");
													break;
												}
											}
											
											/*for(int l=0;l<equipments.length();l++){//Recorre los Equipments
												JSONObject equipment = equipments.getJSONObject(l);
												String idequipment = equipment.getString("ID");
												if(planParentOf.indexOf(idequipment)>-1){
													plan.put("Equipment",equipment);
													
													break;
												}
											}*/
											
											basic.put("Plan",plan);
											break;
										}
									}
									arrayBasic.put(basic);
								}
							}
						}
						
						fEPCS.Debug("["+jspName+"] family: "+family, "INFO");
						if(family.indexOf("Mobile") == -1 && !family.equals("")){
							bundle.put("family",family);
							bundle.put("Basics",arrayBasic);
							arrayBundle.put(bundle);
						}
						
						//fEPCS.Debug("["+jspName+"] ------------------------------------------------------------------------", "INFO");
					}
					fEPCS.Debug("["+jspName+"] {\"arrayBundle\": "+arrayBundle.toString()+"}", "INFO");
					
					addressArray = null;
					bundles = null;
					basics = null;
					planes = null;
					freeUnits = null;
					others = null;
					mercados = null;

				}else{ ///Error Controlado
					String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
					result.put("CODE", codigo);
					fEPCS.Debug("["+jspName+"] CODE: "+codigo, "INFO");
					parametros_marcas_navegacion.put("MSG","ERROR "+codigo);				
				}
			} 
	    	result.put("trx_respuesta", trx_respuesta);		
			result.put("cliente_datos", cliente_datos);
			
			
		}else{
			fEPCS.Debug("["+jspName+"] SIN CustomerAccount ID", "INFO");
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