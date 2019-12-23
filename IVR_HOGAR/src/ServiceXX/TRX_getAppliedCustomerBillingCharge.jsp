<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>
<%@page import="org.json.JSONArray" %>
<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {
    
	JSONObject result = new JSONObject();
	JSONObject respJSON = new JSONObject();
    JSONObject cliente_datos = new JSONObject();
	
	JSONObject parametros_marcas_navegacion = (state.has("parametros_marcas_navegacion") ) ? state.getJSONObject("parametros_marcas_navegacion") : new JSONObject();
    FunctionsEPCS_PostPago fEPCS = new FunctionsEPCS_PostPago(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	
		cliente_datos = (state.has("cliente_datos") ) ? state.getJSONObject("cliente_datos") : new JSONObject();
    	
    	String ani = additionalParams.get("PCS");
		ani = (ani.length() < 11 ) ? "56"+ani : ani;
		
		fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] INICIO", "INFO");
    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] ANI: "+ani, "INFO");
		
		String rut = cliente_datos.getJSONObject("IndividualIdentification").getString("number");
		String customerAccountID = cliente_datos.getString("CustomerAccountID");
    	
    	String processID = additionalParams.get("processID");
    	String sourceID = additionalParams.get("sourceID");
    	String idLlamada = additionalParams.get("idLlamada");
    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] processID: "+processID, "INFO");
    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] sourceID: "+sourceID, "INFO");
    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] idLlamada: "+idLlamada, "INFO");
		fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] parametros_marcas_navegacion: "+parametros_marcas_navegacion, "INFO");
		
		parametros_marcas_navegacion=fEPCS.startNavegacion(state,"TRX_GETAPPLIEDCUSTOMERBILLINGCHARGE");
    	parametros_marcas_navegacion.put("DATA","GET");
    	parametros_marcas_navegacion.put("RC","99");
		
		String trx_respuesta = "NOK"; 
    	String status = "";
    	String type="";
		String description = "";
		String codeCanonical = "";
    	String deuda = "";
    	int deuda_total = 0;
    	int deudaXvencer = 0;
    	int fechaPago = 0;
    	int Totdeuda_vencida=0;
    	int Totdeuda_xvencer=0;
    	int contador=1;
    	String salidaDeuda="0";
    	String sTrx_datos_respuesta=fEPCS.GetAppliedCustomerBillingCharge(rut,customerAccountID,idLlamada, processID, sourceID);
    	boolean PoseeDeuda = false;
    	String Mensaje_SMS="Detalle boletas \n";
    	int totBoletas_vencidas=0;
    	int totBoletas_x_vencer=0;
    	
    	respJSON = new JSONObject(sTrx_datos_respuesta);
    	if(!respJSON.isNull("faultstring")) {
    		description = respJSON.getString("faultstring");
    		fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Fault description: "+description, "INFO"); 
			if(!respJSON.isNull("detail")) {
				if(!respJSON.getJSONObject("detail").getJSONObject("GetAppliedCustomerBillingCharge_FRSP").getJSONObject("ResponseHeader").isNull("Result")) {
					status = respJSON.getJSONObject("detail").getJSONObject("GetAppliedCustomerBillingCharge_FRSP").getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
					fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Fault STATUS: "+status, "INFO");
				}
			}
		}else if(!respJSON.isNull("codigoError")){ //ha ocurrido una excepcion 
			fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Ha ocurrido una excepcion: "+respJSON.getString("descripcion"), "INFO"); 
		}else{ 
			status = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("status");
			fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] STATUS: "+status, "INFO");
			
			if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("description")){
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] DESCRIPTION: "+description, "INFO"); 
				if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").isNull("CanonicalError")){
					if(!respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").isNull("description")){
						description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("description");
						codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
						fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Canonical code: "+codeCanonical, "INFO");
						fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Canonical Description: "+description, "INFO"); 
					}
				}
			}
			if(status.equalsIgnoreCase("OK")){ 
				trx_respuesta = "OK"; 
				parametros_marcas_navegacion.put("RC","0");
				JSONObject BillingAccount = new JSONObject();
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getString("description");
				codeCanonical = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("code");
				type = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("CanonicalError").getString("type");
				try{
					JSONArray Products = respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).getJSONArray("CustomerBill");
					for(int j=0;j<Products.length();j++){
						deuda = respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).getJSONArray("CustomerBill").getJSONObject(j).getString("balanceDue");
				    	String fechaPagoText = respJSON.getJSONObject("Body").getJSONArray("CustomerAccount").getJSONObject(0).getJSONArray("CustomerBill").getJSONObject(j).getString("paymentDueDate");
				    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Fecha deuda: "+deuda, "INFO");
				    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Fecha fechaPagoText: "+fechaPagoText, "INFO");
				    	
				    	fechaPagoText = fechaPagoText.substring(0,fechaPagoText.indexOf("T"));
				    	deuda_total = deuda_total + Integer.parseInt(deuda);
				    
				    	Date objDate = new Date(); 
				    	String strDateFormat = "yyyy-MM-dd";
				    	SimpleDateFormat objSDF = new SimpleDateFormat(strDateFormat);
				    	String[] FechaPagoFormateada =  fechaPagoText.split("-");
				    	String FechaPagoFormat = FechaPagoFormateada[2] + "/"+FechaPagoFormateada[1]+"/"+FechaPagoFormateada[0];
				        Date fechaPagoOrig= objSDF.parse(fechaPagoText);
				        String Snewdate = objSDF.format(new Date());
				        Date newDate = objSDF.parse(Snewdate);
				        
				        Mensaje_SMS = Mensaje_SMS +"Boleta "+contador+" \n Monto: $"+deuda+" \n Fecha Venc: "+FechaPagoFormat+" \n\n";
				        contador=contador+1;
				        if(newDate.after(fechaPagoOrig)){ //vencida
				        	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] DEUDA VENCIDA!!! ", "INFO");
				        	totBoletas_vencidas=totBoletas_vencidas+1;
				        }else if(newDate.before(fechaPagoOrig) || newDate.equals(fechaPagoOrig)){ //x vencer
				        	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] DEUDA X VENCER ", "INFO");
				        	totBoletas_x_vencer=totBoletas_x_vencer+1;
				        }
			    		
				           
				    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Deuda Cliente: "+deuda, "INFO");
				    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Fecha de Pago: "+FechaPagoFormat, "INFO");
				    	fechaPagoText = fechaPagoText.replaceAll("-","");
				    	int fechaPago2 = Integer.parseInt(fechaPagoText);
				    	
				    	if(totBoletas_vencidas > 0 || totBoletas_x_vencer > 0){
				    		PoseeDeuda = true;
				    		fechaPago =  fechaPago2;
				    		deudaXvencer = Integer.parseInt(deuda);
				    	} 

					}
					
			    	
			    	if(totBoletas_x_vencer==1 && totBoletas_vencidas==0){
			    		salidaDeuda="1";
			    	}
			    	if(totBoletas_x_vencer==0 && totBoletas_vencidas==1){
			    		salidaDeuda="2";
			    	}
			    	if(totBoletas_x_vencer==0 && totBoletas_vencidas>=2){
			    		salidaDeuda="3";
			    	}
			    	if(totBoletas_x_vencer==2 && totBoletas_vencidas==0){
			    		salidaDeuda="4";
			    	}
			    	if(totBoletas_x_vencer==2 && totBoletas_vencidas==1){
			    		salidaDeuda="5";
			    	}
			    	if(totBoletas_x_vencer==1 && totBoletas_vencidas==1){
			    		salidaDeuda="6";
			    	}
			    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] totBoletas_x_vencer: "+totBoletas_x_vencer, "INFO");
			    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] totBoletas_vencidas: "+totBoletas_vencidas, "INFO");
			    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] salidaDeuda: "+salidaDeuda, "INFO");
			    	
					cliente_datos.put("salidaDeuda",salidaDeuda);
					cliente_datos.put("Mensaje_SMS",Mensaje_SMS);
					cliente_datos.put("totBoletas_vencidas",String.valueOf(totBoletas_vencidas));
					cliente_datos.put("totBoletas_x_vencer",String.valueOf(totBoletas_x_vencer));
					cliente_datos.put("totBoletas",String.valueOf(totBoletas_x_vencer+totBoletas_vencidas));
				}catch(Exception ex)
				{
					fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Body NULL: No posee deuda.", "INFO");
					deuda = "0";
					
				}
				
					
				
			}else{ ///Error Controlado 
				String codigo = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("code");
				description = respJSON.getJSONObject("ResponseHeader").getJSONObject("Result").getJSONObject("SourceError").getString("description");
				result.put("CODE", codigo);
				fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Source Code: "+codigo, "INFO");
				fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Source Description: "+description, "INFO");
				
				if(codigo.equals("0010")){
					parametros_marcas_navegacion.put("RC","0");
					trx_respuesta = "OK";
					parametros_marcas_navegacion.put("MSG",description);
					
				}else{
					parametros_marcas_navegacion.put("MSG","ERROR "+codigo);
				}
				
			}
			result.put("trx_respuesta", trx_respuesta);
			result.put("PoseeDeuda", String.valueOf(PoseeDeuda));
			result.put("Deuda_cliente", String.valueOf(deuda_total));
			result.put("DeudaPorVencer", String.valueOf(deudaXvencer));
			result.put("FechaPago_cliente", fechaPago);
			result.put("CustomerAccountID",customerAccountID);

    	
			fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] PoseeDeuda: "+String.valueOf(PoseeDeuda), "INFO");
			fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Deuda Total: "+String.valueOf(deuda_total), "INFO");
			cliente_datos.put("PoseeDeuda",String.valueOf(PoseeDeuda));
			result.put("cliente_datos", cliente_datos);
			fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] cliente_datos: "+cliente_datos.toString(), "INFO");
		}
    	
    	
    }catch(Exception ex){
		fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] Error : "+ex.getMessage());
		ex.printStackTrace();
	}finally{
		state.put("parametros_marcas_navegacion",parametros_marcas_navegacion);
    	parametros_marcas_navegacion=fEPCS.stopNavegacion(state);
    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] FIN parametros_marcas_navegacion: "+parametros_marcas_navegacion.toString(), "INFO");
    	
    	result.put("parametros_marcas_navegacion", parametros_marcas_navegacion);
    	fEPCS.Debug("[TRX_getAppliedCustomerBillingCharge] FIN result: "+result.toString(), "INFO");
    	
    	parametros_marcas_navegacion = null;
    	respJSON = null;
    	cliente_datos=null;
	}
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>