<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

    JSONObject result = new JSONObject();
    
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
       
    try{    	   
    	
    	String opcionesMenu = "";
        String opcionesDTMF = "";
        String Path="es-CL/IVR/CompraBolsas/";
        String AudioDefecto="es-CL/IVR/General/Silencio.vox";
        String audioDTMF="/IVR/Menus/marca";
        int cont = 0;
        String DatosObtenidos = additionalParams.get("DatosObtenidos");
        String TipoBolsa = additionalParams.get("TipoBolsa");
        String precioBolsa = additionalParams.get("precioBolsa");
        String aplicaPtosZE = additionalParams.get("aplicaPtosZE");
        String Saldo = state.getString("Var_SALDO");
        String Bolsa="";
        String precio="";
        String ZE="";
        String Bolsa_precio="";
    	
    	fEPCS.Debug("[Obtener Menu Principal] Inicio", "INFO");
    	fEPCS.Debug("[Obtener Menu Principal] Saldo: "+Saldo,"INFO");
    	fEPCS.Debug("[Obtener Menu Principal] precio Bolsas: "+precioBolsa,"INFO");
    	fEPCS.Debug("[Obtener Menu Principal] aplica Ptos: "+aplicaPtosZE,"INFO");
    	fEPCS.Debug("[Obtener Menu Principal] Datos Obtenidos: "+DatosObtenidos,"INFO");
    	fEPCS.Debug("[Obtener Menu Principal] TipoBolsa: "+TipoBolsa,"INFO");
    	
    	String[] DatosArray = DatosObtenidos.split(",");
    	String[] PreciosArray = precioBolsa.split(",");
    	String[] ZEArray = aplicaPtosZE.split(",");

	
    	
    	for (int x = 0; x < PreciosArray.length ; x++) {
	        for (int i = 0; i < PreciosArray.length-x-1; i++) {
	        	Double prim = Double.parseDouble(PreciosArray[i]);
	        	Double prim2 = Double.parseDouble(PreciosArray[i+1]);
	        	String b1 = DatosArray[i];
	        	String b2 = DatosArray[i+1];
	        	String c1 = ZEArray[i];
	        	String c2 = ZEArray[i+1];
	            if(prim.intValue() <prim2.intValue()){
	                int tmp = prim2.intValue();
	                String tmp2 = b2;
	                String tmp3 = c2;
	                PreciosArray[i+1] =prim.intValue()+"";
	                PreciosArray[i] = tmp+"";
	                DatosArray[i+1] =b1+"";
	                DatosArray[i] = tmp2+"";
	                ZEArray[i+1] =c1+"";
	                ZEArray[i] = tmp3+"";
	            }else {
	            	PreciosArray[i] = prim.intValue()+"";
	            	DatosArray[i] = b1+"";
	            	ZEArray[i] = c1+"";
	            	
	            }
	        }
	    }
    	
    	//String[] Token = new String[DatosArray.length+PreciosArray.length];
    	
    	//fEPCS.Debug("[Obtener Menu Principal] TOKEN : "+Token.toString(), "INFO");
    	
    	/*CODIGO DE ADAPTACION DEBIDO A QUE EL WS getAvailableProductOffer Devuelve una caldantida  de Bolsas para ofrecer.
    	SE PROCEDE A CREAR LOGICA QUE PERMITA FILTRAR LAS BOLSAS QUE SE DESEAN SEGUN PROPERTIES
    	Version 2 
    	Motivado a que Recuperar las Bolsas de Roaming se complica porque tienen 3 tipos de specificationSubType y solo se puede consultar 1 a la vez
    	por ende se aplica que se recupere todas las bolsas Existente(sin usar especificationSubType) y por un Filtro se reproducen las necesarias, 
    	solo se va ser uso de este filtro cuando la variable TipoBolsa tiene como valor Vacío
    	*/
    	
    	String filtro = "";
    	if(!TipoBolsa.equals("")){//Bolsas XXXX 
    		filtro=fEPCS.getParametro("FILTRO_BOLSAS") ;
    	}else{//Bolsas Roaming
    		filtro=fEPCS.getParametro("FILTRO_ROAM") ;
    	}
    		
    	fEPCS.Debug("[Obtener Menu Principal] FILTRO: "+filtro,"INFO");
    	
    	if(!filtro.equals("") || !filtro.isEmpty()){
    		int x=0;
    		for(int b=0; b < DatosArray.length && x<8 ; b++){
    			if(filtro.indexOf(DatosArray[b]+",")>-1){
    				Bolsa=DatosArray[b];
    	    		precio=PreciosArray[b];
    	    		ZE=ZEArray[b];
    	    		Bolsa_precio=Bolsa+"."+precio+"."+ZE+"|";
    	    		opcionesMenu += x+";"+Path+Bolsa+".vox;"+AudioDefecto+";;"+audioDTMF+(x+1)+".vox;SI;"+Bolsa_precio;
    	    		opcionesDTMF += (x+1)+"|";
    	      		fEPCS.Debug("[Obtener Menu Principal] OPCS : "+opcionesMenu, "INFO");
    	      		x++;
    			}
    		}
    		opcionesMenu += "9;"+Path+"VolverMenu.vox;"+AudioDefecto+";;IVR/General/Silencio.vox;SI;VOLVER";
    		opcionesDTMF += "9|";
    	}
    	
    	
    	
    	
    	/*//LOGICA QUE TOMA LOS ID DESDE EL WS, Y SOLO LOS PRIMEROS 5 
    	for(int b=0; b<DatosArray.length && b<5; b++){
    		Bolsa=DatosArray[b];
    		precio=PreciosArray[b];
    		Bolsa_precio=Bolsa+"."+precio+"|";
    		opcionesMenu += b+";"+Path+Bolsa+".vox;"+AudioDefecto+";;"+audioDTMF+(b+1)+".vox;SI;"+Bolsa_precio;
    		opcionesDTMF += (b+1)+"|";
    		
    		fEPCS.Debug("[Obtener Menu Principal] OPCS : "+opcionesMenu, "INFO");
    	}*/
			
    	fEPCS.Debug("[Obtener Menu Principal] OPCS : "+opcionesMenu, "INFO");
    	fEPCS.Debug("[Obtener Menu Principal] DTMF : "+opcionesDTMF, "INFO");
    	
    	result.put("OpcionesMenu", opcionesMenu);
		result.put("OpcionesDTMF", opcionesDTMF);
		                    
    }catch (Exception ex){
    	fEPCS.DebugError("[Obtener Menu Principal] Hubo un ERROR: "+ex.getMessage());    	
    }
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>
