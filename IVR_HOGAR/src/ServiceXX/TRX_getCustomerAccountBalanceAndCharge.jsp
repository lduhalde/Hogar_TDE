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
			int saldoBono =0;
			int saldoConjunto=0;
			int saldoPLK=0;
			int saldoP2LK=0;
			int saldoAuxiliar=0;
			int saldoDato=0; //variable creada para almacenar el saldo de datos
			int cuota = 0;
			String tipoDato="IVR/ConsultaTrafico/megas.wav"; //variable creada para almacenar el tipo de datos del saldo
			String tipoDatoCuota="IVR/ConsultaTrafico/megas.wav";
			String sTipoDato = "MB";
			String sTipoDatoCuota = "MB";
			String saldoBonoVigencia="";
			String saldoVigMonedero_original="";
			String saldoVigencia="";
			int fechaVencimiento = 0;
			String fechaVencimientoText = "";
			String saldoVigencia2="";
			String saldoVigencia_original="";
			String saldoVigencia2_original="";
			String externalId="";
			String description = "";
			String codeCanonical = "";
			String SMS ="";
			String SMS2 ="";
			String opcion="";
			String trx_dato="NOK";
			boolean flag_11100 = false;
			boolean flag_11500 = false;
			String resp="NOK";
			String sJSPName="TRX_getCustomerAccountBalanceAndCharge";
			String trafico_libre="NO";

			JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();

			FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));

			try{
				cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();

				String ani = additionalParams.get("PCS");
				ani = (ani.length() < 11 ) ? "56"+ani : ani; 

				fEPCS.Debug("["+sJSPName+"] INICIO", "INFO");
				fEPCS.Debug("["+sJSPName+"] ANI: "+ani, "INFO");

				String processID = additionalParams.get("processID");
				String sourceID = additionalParams.get("sourceID");
				String idLlamada = additionalParams.get("idLlamada");
				opcion = additionalParams.get("opcion");
				String conCobro = additionalParams.get("conCobro");
				fEPCS.Debug("["+sJSPName+"] processID: "+processID, "INFO");
				fEPCS.Debug("["+sJSPName+"] sourceID: "+sourceID, "INFO");
				fEPCS.Debug("["+sJSPName+"] idLlamada: "+idLlamada, "INFO");
				fEPCS.Debug("["+sJSPName+"] Opcion: "+opcion, "INFO");
				fEPCS.Debug("["+sJSPName+"] conCobro: "+conCobro, "INFO");
				String sTrx_datos_respuesta = "";
				String trx_respuesta = "NOK"; 
				String status = "";

				if(conCobro.equals("GRATIS") && cliente_datos.has("RSP_BALANCEANDCHARGE")){
					fEPCS.Debug("["+sJSPName+"] RSP_BALANCEANDCHARGE con datos - NO se ejecuta WS", "INFO");
					sTrx_datos_respuesta = cliente_datos.getString("RSP_BALANCEANDCHARGE");
				}else{
					fEPCS.Debug("["+sJSPName+"] parametros_marcas_navegacion: "+parametros_marcas_navegacion, "INFO");

					parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_CUSTOMERACCOUNTBALANCEACH");
					parametros_marcas_navegacion.put("DATA","GET");
					parametros_marcas_navegacion.put("RC","99");

					sTrx_datos_respuesta=fEPCS.GetCustomerAccountBalanceAndCharge(ani,"IVR",idLlamada, processID, sourceID);
				}


				//fEPCS.Debug("["+sJSPName+"] sTrx_datos_respuesta: "+sTrx_datos_respuesta, "INFO");
				respJSON = new JSONObject(sTrx_datos_respuesta);
				if(!respJSON.isNull("faultstring")) {
					description = respJSON.getString("faultstring");
					fEPCS.Debug("["+sJSPName+"] Fault description: "+description, "INFO"); 
					if(!respJSON.isNull("detail")) {
						if(!respJSON.getJSONObject("detail").getJSONObject("GetCustomerAccountBalanceAndCharge_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
							status = respJSON.getJSONObject("detail").getJSONObject("GetCustomerAccountBalanceAndCharge_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
							fEPCS.Debug("["+sJSPName+"] Fault STATUS: "+status, "INFO");
							if(!respJSON.getJSONObject("detail").getJSONObject("GetCustomerAccountBalanceAndCharge_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").isNull("CanonicalError")){
								String code = respJSON.getJSONObject("detail").getJSONObject("GetCustomerAccountBalanceAndCharge_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
								cliente_datos.put("code",code);
							}
						}
					}
				}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
					fEPCS.Debug("["+sJSPName+"] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
				}else{ 
					status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("["+sJSPName+"] STATUS: "+status, "INFO");

					int saldoXvencer = 0;
					String saldoVigMonedero = "";
					if(status.equalsIgnoreCase("OK")){ 
						parametros_marcas_navegacion.put("RC","0");
						trx_respuesta = "OK";
						cliente_datos.put("RSP_BALANCEANDCHARGE",sTrx_datos_respuesta);
						if(opcion.equalsIgnoreCase("saldo")){
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

									fEPCS.Debug("["+sJSPName+"] saldoVigencia: "+saldoVigencia, "INFO");

									cliente_datos.put("Var_SALDO_RECARGAS", jo.getJSONObject("remainedAmount").getInt("amount"));
									fEPCS.Debug("["+sJSPName+"] SALDO: "+jo.getJSONObject("remainedAmount").getInt("amount"), "INFO");
								}
								if(IDBalance.equals("11101")){//Monedero secundario
									saldo += jo.getJSONObject("remainedAmount").getInt("amount");
									cliente_datos.put("Var_SALDO_BONO", jo.getJSONObject("remainedAmount").getInt("amount"));
									fEPCS.Debug("["+sJSPName+"] SALDO BONO: "+jo.getJSONObject("remainedAmount").getInt("amount"), "INFO");

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

									fEPCS.Debug("["+sJSPName+"] saldoVigMonedero: "+saldoVigMonedero, "INFO");

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

									fEPCS.Debug("["+sJSPName+"] saldoVigencia2: "+saldoVigencia2, "INFO");


									cliente_datos.put("Var_SALDO_RECURRENTE", jo.getJSONObject("remainedAmount").getInt("amount"));
									fEPCS.Debug("["+sJSPName+"] SALDO RECURRENTE: "+jo.getJSONObject("remainedAmount").getInt("amount"), "INFO");
								}

							}
							if(fechaVencimiento == 0){
								fechaVencimientoText=saldoVigencia2_original;
							}
							if(fechaVencimientoText.equals("")){
								SMS = "Tu saldo disponible es de $"+saldo;
							}else{
								SMS= "Tu saldo disponible es de $"+saldo+" de los cuales $"+saldoXvencer+" vencen el "+fechaVencimientoText;
							}
							fEPCS.Debug("["+sJSPName+"] fechaVencimiento: ["+fechaVencimiento+"]", "INFO");
							fEPCS.Debug("["+sJSPName+"] fechaVencimientoText: ["+fechaVencimientoText+"]", "INFO");
							fEPCS.Debug("["+sJSPName+"] saldoXvencer: "+saldoXvencer, "INFO");

							fEPCS.Debug("["+sJSPName+"] SALDO FINAL: "+saldo, "INFO");
							fEPCS.Debug("["+sJSPName+"] FECHA VIGENCIA: "+saldoVigencia_original, "INFO");
							cliente_datos.put("Var_SALDO", saldo);
							cliente_datos.put("Var_SALDO_VIGENCIA", fechaVencimiento+"");
							cliente_datos.put("Var_SALDO_XVENCER", saldoXvencer); 
						}else if(opcion.equalsIgnoreCase("trafico")){
							JSONArray balanceDatos = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("CustomerService");
							for(int a=0; a<balanceDatos.length(); a++){
								JSONObject joDatos = (JSONObject) balanceDatos.get(a);
								if(!joDatos.isNull("CustomerAccountBalance")){
									JSONArray datos = joDatos.getJSONArray("CustomerAccountBalance");

									if(joDatos.optString("type").equals("0")){

										for(int c=0;c<datos.length();c++){
											JSONObject traficodatos =(JSONObject) datos.get(c);

											if(traficodatos.getJSONObject("remainedAmount").getString("units").equals("6")){


												if(!traficodatos.isNull("thresholdAmount")) {
													cuota = traficodatos.getInt("thresholdAmount");
												}

												saldoDato = cuota - traficodatos.getJSONObject("remainedAmount").getInt("amount");
												//saldoDato = traficodatos.getJSONObject("remainedAmount").getInt("amount");
												fEPCS.Debug("["+sJSPName+"] SALDO DATO: "+saldoDato, "INFO");
												fEPCS.Debug("["+sJSPName+"] CUOTA DATO: "+cuota, "INFO");
												trx_dato="OK";
												break;

											}
											if(traficodatos.getJSONObject("remainedAmount").getString("units").equals("16")){
												fEPCS.Debug("["+sJSPName+"] TRAFICO LIBRE - ILIMITADO", "INFO");
												trafico_libre="SI";
												break;
											}
										}
									}

								}

								if(!joDatos.isNull("ProductOffering")){
									if(joDatos.getJSONObject("ProductOffering").optString("ID").equals("PO_POS_A_PROMO_RECARGA")){
										String tipoRegalo="regalo_minutos";
										JSONArray datos = joDatos.getJSONArray("CustomerAllowance");

										if(datos.getJSONObject(0).getJSONObject("balance").getString("units").equals("6")){
											int regalo = datos.getJSONObject(0).getJSONObject("balance").getInt("amount");
											if(regalo>90000000){
												trafico_libre="SI-REGALO";
												SMS = "Recuerda que tienes internet libre para que disfrutes hasta el 31 de diciembre de 2019, disfrutalos!";
												cliente_datos.put("audio_trafico_libre", "IVR/ConsultaTrafico/regalo_datos_ilimitado.wav");
												cliente_datos.put("marca_trafico_libre", "PROMPT_REGALO_DATOS_LIBRES");
												fEPCS.Debug("["+sJSPName+"] REGALO DATOS LIBRE", "INFO");
											}else{
												regalo = 500-regalo;
												SMS2 = " Y de tu bolsa de regalo has consumido "+regalo+"MB de los 500MB de navegación que vence el 31 de Julio, disfrutalos!";
												cliente_datos.put("tipoRegaloBolsa", "datos");
												cliente_datos.put("consumoRegaloBolsa", regalo);
												fEPCS.Debug("["+sJSPName+"] REGALO 500MB", "INFO");
											}

										}else if(datos.getJSONObject(0).getJSONObject("balance").getString("units").equals("1")){
											int regalo = datos.getJSONObject(0).getJSONObject("balance").getInt("amount");
											fEPCS.Debug("["+sJSPName+"] BOLSA PROMO: "+joDatos.getJSONObject("ProductOffering").optString("name"), "INFO");
											fEPCS.Debug("["+sJSPName+"] SALDO BOLSA PROMO: "+regalo, "INFO");
											if(regalo>3000){
												SMS2 = "Y recuerda que tienes minutos libres para que disfrutes hasta el 31 de diciembre de 2019, disfrutalos!";
												cliente_datos.put("tipoRegaloBolsa", "minutos_ilimitados");
												fEPCS.Debug("["+sJSPName+"] REGALO MINUTOS LIBRES", "INFO");
											}else{
												regalo = 3000 - regalo;
												SMS2 = " Y de tu bolsa de regalo has consumido "+regalo/60+"minutos de los 50 minutos todo destino que vence el 31 de Julio, disfrutalos!";
												cliente_datos.put("tipoRegaloBolsa", "minutos");
												cliente_datos.put("consumoRegaloBolsa", regalo/60);
												fEPCS.Debug("["+sJSPName+"] REGALO 50 Min TD", "INFO");
											}
										}else{
											fEPCS.Debug("["+sJSPName+"] TIPO NO CONTEMPLADO", "INFO");
										}



									}
								}

							}
							if(trx_dato.equals("OK") && trafico_libre.equals("NO")){
								// conversion de datos
								int m = saldoDato;
								int g = saldoDato/1024; 
								int t = saldoDato/1048576; 

								if (t>0){
									saldoDato = t; 
									tipoDato = "IVR/ConsultaTrafico/teras.wav";
									sTipoDato = "TB";
								}else if (g>0){
									saldoDato = g; 
									tipoDato = "IVR/ConsultaTrafico/gigas.wav";
									sTipoDato = "GB";
								}else {//if (m>0){
									saldoDato = m; 
									tipoDato = "IVR/ConsultaTrafico/megas.wav";
									sTipoDato = "MB";
								}

								//conversion de datos para cuota

								m = cuota;
								g = cuota/1024; 
								t = cuota/1048576; 

								if (t>0){
									cuota = t; 
									tipoDatoCuota = "IVR/ConsultaTrafico/teras.wav";
									sTipoDatoCuota = "TB";
								}else if (g>0){
									cuota = g; 
									tipoDatoCuota = "IVR/ConsultaTrafico/gigas.wav";
									sTipoDatoCuota = "GB";
								}else {//if (m>0){
									cuota = m; 
									tipoDatoCuota = "IVR/ConsultaTrafico/megas.wav";
									sTipoDatoCuota = "MB";
								}



								saldoDato = (Math.round(saldoDato * 100) / 100);
								cuota = (Math.round(cuota * 100) / 100);
								if(opcion.equalsIgnoreCase("trafico")){
									SMS = "Has consumido "+saldoDato+sTipoDato+" de tus "+cuota+sTipoDatoCuota+" contratados.";
								}
								fEPCS.Debug("["+sJSPName+"] CUOTA: "+cuota, "INFO");
								fEPCS.Debug("["+sJSPName+"] TIPO DATO: "+tipoDato, "INFO");

							}
						}else{
							//consulta bolsas
							String path = "/IVR/Sub_Consulta_Bolsas/";
							JSONArray Bolsas = new JSONArray();
							JSONArray balanceDatos = respJSON.getJSONObject("Body").getJSONObject("CustomerAccount").getJSONArray("CustomerService");
							
							int amountBolsa =0;
							int cuotaBolsa=0;
							int consumoBolsa=0;
							for(int a=0; a<balanceDatos.length(); a++){
								JSONObject joDatos = (JSONObject) balanceDatos.get(a);
								fEPCS.Debug("["+sJSPName+"] balanceDatos : "+ joDatos.toString()); 
								if(!joDatos.isNull("CustomerAccountBalance")){
									if(joDatos.optString("type").equals("2")){ // 2 => Bolsas
										fEPCS.Debug("["+sJSPName+"] BOLSA: "+joDatos.optJSONObject("ProductOffering").optString("ID")); 
										JSONArray datos = joDatos.getJSONArray("CustomerAccountBalance");
										for(int c=0;c<datos.length();c++){
											JSONObject detalle_bolsa =(JSONObject) datos.get(c);
											//fEPCS.Debug("["+sJSPName+"] detalle_bolsa : "+ detalle_bolsa.toString()); 
											
												amountBolsa = (int)(Math.round(detalle_bolsa.getJSONObject("remainedAmount").getDouble("amount")));
												cuotaBolsa = (int)(Math.round(detalle_bolsa.getDouble("thresholdAmount")));
											
											fEPCS.Debug("["+sJSPName+"] AMOUNT: "+amountBolsa); 
											fEPCS.Debug("["+sJSPName+"] CUOTA: "+cuotaBolsa); 
											if(amountBolsa>0){
												JSONObject bolsa = new JSONObject();
												bolsa.put("audio",path+"N_"+joDatos.optJSONObject("ProductOffering").optString("ID")+".wav");
												bolsa.put("ilimitada","NO");
												consumoBolsa=cuotaBolsa-amountBolsa;
												fEPCS.Debug("["+sJSPName+"] CONSUMO: "+consumoBolsa);
												
												String vigencia = joDatos.getJSONObject("validFor").getString("endDate");
												vigencia = vigencia.substring(0,vigencia.indexOf("T"));
												vigencia = vigencia.replaceAll("-","");
												bolsa.put("vigencia",vigencia);
												fEPCS.Debug("["+sJSPName+"] VIGENCIA: "+vigencia);
												fEPCS.Debug("["+sJSPName+"] UNITS: "+detalle_bolsa.getJSONObject("remainedAmount").getString("units"));
												if(detalle_bolsa.getJSONObject("remainedAmount").getString("units").equals("6") || detalle_bolsa.getJSONObject("remainedAmount").getString("units").equals("7")){
													//Datos y RRSS
													int m = consumoBolsa;
													int g = consumoBolsa/1024; 
													int t = consumoBolsa/1048576; 

													if (t>0){
														consumoBolsa = t; 
														tipoDato = path+"teras.wav";
													}else if (g>0){
														consumoBolsa = g; 
														tipoDato = path+"gigas.wav";
													}else {//if (m>0){
														consumoBolsa = m; 
														tipoDato = path+"megas.wav";
													}

													consumoBolsa = (Math.round(consumoBolsa * 100) / 100);
													bolsa.put("consumo", consumoBolsa);
													bolsa.put("tipo_dato", tipoDato);
													
													
													m = cuotaBolsa;
													g = cuotaBolsa/1024; 
													t = cuotaBolsa/1048576; 

													if (t>0){
														cuotaBolsa = t; 
														tipoDato = path+"teras.wav";
													}else if (g>0){
														cuotaBolsa = g; 
														tipoDato = path+"gigas.wav";
													}else {//if (m>0){
														cuotaBolsa = m; 
														tipoDato = path+"megas.wav";
													}
													bolsa.put("cuotaBolsa", cuotaBolsa);
													bolsa.put("tipo_dato_cuota", tipoDato);
												
												
												
											
											}else if(detalle_bolsa.getJSONObject("remainedAmount").getString("units").equals("1")){
												//Voz
												consumoBolsa = (int) (Math.round(consumoBolsa/60));
												cuotaBolsa = (int) (Math.round(cuotaBolsa/60));
												bolsa.put("consumo", consumoBolsa);
												bolsa.put("tipo_dato", path+"minutos.wav");
												bolsa.put("cuotaBolsa", cuotaBolsa);
												bolsa.put("tipo_dato_cuota", path+"minutos.wav");
											}else if(detalle_bolsa.getJSONObject("remainedAmount").getString("units").equals("5")){
												//SMS
												bolsa.put("consumo", consumoBolsa);
												bolsa.put("tipo_dato", path+"sms.wav");
												bolsa.put("cuotaBolsa", cuotaBolsa);
												bolsa.put("tipo_dato_cuota", path+"sms.wav");
											}else if (detalle_bolsa.getJSONObject("remainedAmount").getString("units").equals("11") 
													|| detalle_bolsa.getJSONObject("remainedAmount").getString("units").equals("15") 
													|| detalle_bolsa.getJSONObject("remainedAmount").getString("units").equals("16")
													|| detalle_bolsa.getJSONObject("remainedAmount").getString("units").equals("17")){
												//Bolsas ilimitadas - No se puede indicar consumo
												bolsa.put("ilimitada","SI");
												bolsa.put("audio",path+joDatos.optJSONObject("ProductOffering").optString("ID")+".wav");
											}else{
												fEPCS.Debug("["+sJSPName+"] Tipo de bolsa desconocido ["+detalle_bolsa.getJSONObject("remainedAmount").getString("units")+"]", "INFO");
												bolsa.put("ilimitada","NA");
												bolsa.put("audio",path+joDatos.optJSONObject("ProductOffering").optString("ID")+".wav");
											}
											fEPCS.Debug("["+sJSPName+"] BOLSA : "+ bolsa.toString()); 
											Bolsas.put(bolsa);
										}

									}		
								}
							}
							cliente_datos.put("BOLSAS",Bolsas);
						}
					}
						
						
					}else{ ///Error Controlado
						if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
							description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
							fEPCS.Debug("["+sJSPName+"] DESCRIPTION: "+description, "INFO"); 
						}
						String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
						result.put("CODE", codigo);
						fEPCS.Debug("["+sJSPName+"] CODE: "+codigo, "INFO");
						parametros_marcas_navegacion.put("MSG","ERROR "+codigo);
					}
				}
				if(trafico_libre != null && opcion != null){
					if(trafico_libre.equals("SI") && opcion.equalsIgnoreCase("trafico")){
						SMS = "Despreocúpate, tu plan es totalmente libre! Olvídate de los excedidos.";
						cliente_datos.put("audio_trafico_libre", "IVR/ConsultaTrafico/InfoLibre.wav");
						cliente_datos.put("marca_trafico_libre", "PROMPT_INFO_TRAFICO_LIBRE");
					}
				}
				cliente_datos.put("trafico_libre", trafico_libre);

				result.put("trx_respuesta", trx_respuesta); 
				result.put("trx_datos_respuesta", "{}");

			}catch(Exception ex){
				fEPCS.Debug("["+sJSPName+"] Error : "+ex.getMessage());
				ex.printStackTrace();
			}finally{
				result.put("Var_SALDO", saldo); 
				result.put("Var_SALDO_VIGENCIA", fechaVencimiento+""); 

				cliente_datos.put("Var_SALDO_DATOS", saldoDato);
				cliente_datos.put("Var_TIPO_DATO", tipoDato);
				cliente_datos.put("Var_CUOTA_DATOS", cuota);
				cliente_datos.put("Var_TIPO_DATO_CUOTA", tipoDatoCuota);

				result.put("cliente_datos", cliente_datos);    	
				result.put("SMS", SMS+SMS2);
				state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
				parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
				fEPCS.Debug("["+sJSPName+"] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");

				result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
				fEPCS.Debug("["+sJSPName+"] FIN result: "+result.toString(), "INFO");

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