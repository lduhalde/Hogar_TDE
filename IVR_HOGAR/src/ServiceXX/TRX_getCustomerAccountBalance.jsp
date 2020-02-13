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
    int saldo2 = 0;
    int saldoBono =0;
	int saldoConjunto=0;
	int saldoPLK=0;
	int saldoP2LK=0;
	int saldoAuxiliar=0;
	int saldoDato=0; //variable creada para almacenar el saldo de datos
	String tipoDato=""; //variable creada para almacenar el tipo de datos del saldo
    String saldoBonoVigencia="";
    String saldoVigencia="";
    String saldoVigencia2="";
    String saldoVigencia_original="";
    String saldoVigencia2_original="";
    String externalId="";
    String description = "";
	String codeCanonical = "";
	String sTipoDato = "";
	String SMS ="";
	String opcion="";
	boolean flag_11100 = false;
	boolean flag_11500 = false;
    
    JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    JSONArray Bolsas = new JSONArray();
    try{
    	cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	
    	String ani = additionalParams.get("PCS");
		ani = (ani.length() < 11 ) ? "56"+ani : ani; 
	 
    	fEPCS.Debug("[TRX_getCustomerAccountBalance] INICIO", "INFO");
    	fEPCS.Debug("[TRX_getCustomerAccountBalance] ANI: "+ani, "INFO");
    	
    	String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	opcion = additionalParams.get("opcion");
    	
    	fEPCS.Debug("[TRX_getCustomerAccountBalance] processID: "+processID, "INFO");
    	fEPCS.Debug("[TRX_getCustomerAccountBalance] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_getCustomerAccountBalance] idLlamada: "+idLlamada, "INFO");
    	fEPCS.Debug("[TRX_getCustomerAccountBalance] Opcion: "+opcion, "INFO");
    	
		fEPCS.Debug("[TRX_getCustomerAccountBalance] parametros_marcas_navegacion: "+parametros_marcas_navegacion, "INFO");
    	
    	parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_CUSTOMERACCOUNTBALANCE");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
  	
    	String trx_respuesta = "NOK"; 
    	String status = "";
    	String sTrx_datos_respuesta=fEPCS.GetCustomerAccountBalance(ani,"0078",idLlamada, processID, sourceID);
    	fEPCS.Debug("[TRX_getCustomerAccountBalance] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	String PO_PLAN = cliente_datos.optString("PO_PLAN");
    	if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("[TRX_getCustomerAccountBalance] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("GetCustomerAccountBalance_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("getCustomerAccountBalance_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("[TRX_getCustomerAccountBalance] Fault STATUS: "+status, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("[TRX_getCustomerAccountBalance] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("[TRX_getCustomerAccountBalance] STATUS: "+status, "INFO");
			String BolsaConDatos = "NO";
			if(status.equalsIgnoreCase("OK")){ 
				parametros_marcas_navegacion.put("RC","0");
				trx_respuesta = "OK"; 
				JSONArray balances = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("CustomerAccountBalance");
	 			
				for(int i=0; i<balances.length()-1; i++){
					JSONObject jo = (JSONObject) balances.get(i);
					String IDBalance = jo.getString("ID");
					fEPCS.Debug("[TRX_getCustomerAccountBalance] CustomerAccountBalance.ID: "+IDBalance, "INFO");
					if(IDBalance.equals("11100")){//cuenta principal
						saldo = jo.getJSONObject("remainedAmount").getInt("amount");
						saldoVigencia = jo.getJSONObject("validFor").getString("endDate");
						saldoVigencia = saldoVigencia.substring(0,saldoVigencia.indexOf("T"));
						saldoVigencia_original = saldoVigencia;
						
						if(opcion.equalsIgnoreCase("saldo")){
					    	SMS = "Tu saldo disponible es "+saldo;
					    	if(!saldoVigencia.equals("") && saldoVigencia.length()>=8){
					    		SMS += " con vigencia hasta "+saldoVigencia;

					    	}
					    }
						
						saldoVigencia = saldoVigencia.replaceAll("-","");
						fEPCS.Debug("[TRX_getCustomerAccountBalance] SALDO: "+saldo, "INFO");
						flag_11100 = true;
					}
					if(IDBalance.equals("11500")){//recurrente mensual
						saldo2 = jo.getJSONObject("remainedAmount").getInt("amount");
						saldoVigencia2 = jo.getJSONObject("validFor").getString("endDate");
						saldoVigencia2 = saldoVigencia2.substring(0,saldoVigencia2.indexOf("T"));
						saldoVigencia2_original = saldoVigencia2;
						
						if(opcion.equalsIgnoreCase("saldo")){
					    	SMS = "Tu saldo disponible es "+saldo2;
					    	if(!saldoVigencia2.equals("") && saldoVigencia2.length()>=8){
					    		SMS += " con vigencia hasta "+saldoVigencia2;

					    	}
					    }
						
						saldoVigencia = saldoVigencia2.replaceAll("-","");
						fEPCS.Debug("[TRX_getCustomerAccountBalance] SALDO2: "+saldo2, "INFO");
						if(saldo == 0)
						{
							saldo = saldo + saldo2;
						}
						flag_11500 = true;
					}
					
					if(flag_11100 && flag_11500)
					{
						saldo = saldo + saldo2;
						try{
						    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

						    Date date1 = format.parse(saldoVigencia_original);
						    Date date2 = format.parse(saldoVigencia2_original);

						    if (date1.compareTo(date2) <= 0) {
						    	SMS += " con vigencia hasta "+saldoVigencia_original;
						    	
						    }
						    else
						    {
						    	SMS += " con vigencia hasta "+saldoVigencia2_original;
						    	saldoVigencia = saldoVigencia2.replaceAll("-","");
						    }
						}catch(Exception ex)
						{
							System.out.println("Error: " + ex.getMessage());
						}
						fEPCS.Debug("[TRX_getCustomerAccountBalance] SUMA SALDO: "+saldo, "INFO");
						fEPCS.Debug("[TRX_getCustomerAccountBalance] FECHA VIGENCIA: "+saldoVigencia, "INFO");
						flag_11100 = false;
						flag_11500 = false;
						
					}
					
					if(IDBalance.equals("11101")){//Monedero secundario
						saldoBono = jo.getJSONObject("remainedAmount").getInt("amount");
						saldoBonoVigencia = jo.getJSONObject("validFor").getString("endDate");
						saldoBonoVigencia = saldoBonoVigencia.substring(0,saldoBonoVigencia.indexOf("T"));
						saldoBonoVigencia = saldoBonoVigencia.replaceAll("-","");			
						fEPCS.Debug("[TRX_getCustomerAccountBalance] SALDO BONO: "+saldoBono, "INFO");
					}
					if(IDBalance.equals("11103")){
						saldoAuxiliar=jo.getJSONObject("remainedAmount").getInt("amount");
						fEPCS.Debug("[TRX_getCustomerAccountBalance] SALDO AUXILIAR: "+saldoAuxiliar,"INFO");
					}
					if(IDBalance.equals("11301")){//Presta luka
						saldoPLK = jo.getJSONObject("remainedAmount").getInt("amount");
						fEPCS.Debug("[TRX_getCustomerAccountBalance] SALDO PLK: "+saldoPLK, "INFO");
					}
					if(IDBalance.equals("11302")){//Presta 2 lukas
						saldoP2LK = jo.getJSONObject("remainedAmount").getInt("amount");
						fEPCS.Debug("[TRX_getCustomerAccountBalance] SALDO P2LK: "+saldoP2LK, "INFO");

					}
					
					if(IDBalance.equals("16406")){ //datos navegacion
						saldoDato = jo.getJSONObject("remainedAmount").getInt("amount");

                        // conversion de datos
                   				
    				    int m = saldoDato;
    					int g = saldoDato/1024; 
    					int t = saldoDato/1048576; 

								    if (t>0){
								    	saldoDato = t; 
								    	tipoDato = "IVR/ConsultaTrafico/teras.vox";
								    	sTipoDato = "TB";
								    	
								    }else if (g>0){
								    	saldoDato = g; 
								    	tipoDato = "IVR/ConsultaTrafico/gigas.vox";
								    	sTipoDato = "GB";
								    }else {//if (m>0){
    									saldoDato = m; 
    									tipoDato = "IVR/ConsultaTrafico/megas.vox";
    									sTipoDato = "MB";
    								}

								    saldoDato = (Math.round(saldoDato * 100) / 100);
								    if(opcion.equalsIgnoreCase("trafico")){
								    	SMS = "Has consumido "+saldoDato+" "+sTipoDato;
								    }

						
                        fEPCS.Debug("[TRX_getCustomerAccountBalance] SALDO DATO: "+saldoDato, "INFO");
					    fEPCS.Debug("[TRX_getCustomerAccountBalance] TIPO DATO: "+tipoDato, "INFO");
					    

					} 
				} 
    			balances=null; 
    			JSONObject InfoDatosNav = new JSONObject();
    			InfoDatosNav.put("PlanConDatos","NO");
    			JSONArray balanceDatos = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("CustomerService");
    			fEPCS.Debug("[TRX_getCustomerAccountBalance] balanceDatos : "+ balanceDatos.toString()); 
    			
    			int amountBolsa =0;
    			for(int a=0; a<balanceDatos.length(); a++){
					JSONObject joDatos = (JSONObject) balanceDatos.get(a);
					fEPCS.Debug("[TRX_getCustomerAccountBalance] balanceDatos : "+ joDatos.toString()); 
					if(!joDatos.isNull("CustomerAccountBalance")){
						if(joDatos.optString("type").equals("2")){ // 2 => Bolsas
							JSONArray datos = joDatos.getJSONArray("CustomerAccountBalance");
							for(int c=0;c<datos.length();c++){
								JSONObject traficodatos =(JSONObject) datos.get(c);
								if(traficodatos.getJSONObject("remainedAmount").has("amount")){
									amountBolsa = traficodatos.getJSONObject("remainedAmount").getInt("amount");
								}
								fEPCS.Debug("[TRX_getCustomerAccountBalance] SALDO Bolsa: "+amountBolsa, "INFO");
								if(amountBolsa>0){
									Bolsas.put(joDatos.optJSONObject("ProductOffering").optString("ID"));
									fEPCS.Debug("[TRX_getCustomerAccountBalance] PO Bolsa : "+joDatos.optJSONObject("ProductOffering").optString("ID")); 
									if(BolsaConDatos.equals("NO") && (traficodatos.getJSONObject("remainedAmount").getString("units").equals("6") || traficodatos.getJSONObject("remainedAmount").getString("units").equals("16"))){
										BolsaConDatos = "SI";
									}
								} 
					 		}
						 
						}		
					}
					
					//info del plan 
					if(!joDatos.isNull("ProductOffering")){
						fEPCS.Debug("[TRX_getCustomerAccountBalance]PO_PLAN : "+joDatos.optJSONObject("ProductOffering").optString("ID")); 
						if(joDatos.optJSONObject("ProductOffering").optString("ID").equals(PO_PLAN)){
							if(!joDatos.isNull("CustomerAccountBalance")){
								JSONArray traficodatos = joDatos.getJSONArray("CustomerAccountBalance");
								for(int c=0;c<traficodatos.length();c++){
									JSONObject balancePlan =  traficodatos.optJSONObject(c); 
									InfoDatosNav.put("ID_Plan",PO_PLAN); 
									if(balancePlan.optJSONObject("remainedAmount").optString("units").equals("6") || balancePlan.optJSONObject("remainedAmount").optString("units").equals("16")){ //Con Datos
										InfoDatosNav.put("PlanConDatos","SI");
										InfoDatosNav.put("remainedAmountPlan",balancePlan.optJSONObject("remainedAmount"));
										break;
									} 
								}
							}
						} 
					}
					}
    				InfoDatosNav.put("BolsaConDatos",BolsaConDatos);
    				cliente_datos.put("InfoDatosNav",InfoDatosNav);		
			}else{ ///Error Controlado
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
					description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
					fEPCS.Debug("[TRX_getCustomerAccountBalance] DESCRIPTION: "+description, "INFO"); 
				}
				String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
				result.put("CODE", codigo);
				fEPCS.Debug("[TRX_getCustomerAccountBalance] CODE: "+codigo, "INFO");
				parametros_marcas_navegacion.put("MSG","ERROR "+codigo);
			
			}
		}
    	
    	result.put("trx_respuesta", trx_respuesta); 
    	result.put("trx_datos_respuesta", "{}");
    	
    }catch(Exception ex){
    	fEPCS.Debug("[TRX_getCustomerAccountBalance] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
    	
    	saldoConjunto=saldo+saldoAuxiliar;
		fEPCS.Debug("[TRX_getCustomerAccountBalance] SUMATORIA SALDOS: "+saldoConjunto, "INFO");
		
    	result.put("Var_SALDO", saldo); 
		result.put("Var_SALDO_BONO", saldoBono);
		result.put("Var_SALDO_CONJUNTO", saldoConjunto);
		
		result.put("Var_SALDO_BONO_VIGENCIA", saldoBonoVigencia);
		result.put("Var_SALDO_VIGENCIA", saldoVigencia); 
		
		cliente_datos.put("BOLSAS",Bolsas);	
		cliente_datos.put("Var_SALDO", saldo); 
		cliente_datos.put("Var_SALDO_BONO", saldoBono);
		cliente_datos.put("Var_SALDO_CONJUNTO", saldoConjunto);
		//add by FAM
		cliente_datos.put("Var_SALDO_DATOS", saldoDato);
		cliente_datos.put("Var_TIPO_DATO", tipoDato);
		
		cliente_datos.put("Var_SALDO_BONO_VIGENCIA", saldoBonoVigencia);
		cliente_datos.put("Var_SALDO_VIGENCIA", saldoVigencia);
		 
    	result.put("cliente_datos", cliente_datos);    	
    	result.put("SMS", SMS);
    	state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_getCustomerAccountBalance] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_getCustomerAccountBalance] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	Bolsas = null;
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