<%@page language="java" contentType="application/json" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>
<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception 
{    
	JSONObject result = new JSONObject();	

    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));

    try{
    	String OpcionesMenu = state.getString("willaccept");// Parámetro de entrada
    	String OpcionesDTMF = state.getString("OpcionesDTMF");

    	String idMenuSalidas = "";
    	String tokenSalidas = "";      	

    	fEPCS.Debug("********************** Menu Dinamico **************************");
    	fEPCS.Debug(" Opciones Menu : " + OpcionesMenu);
    	fEPCS.Debug(" Opciones DTMF : " + OpcionesDTMF);	 	
    	

    	String audio_para = state.getString("audio_para");    	

    	//Inicializando Valores para emision del menu de Productos
    	String audioPara[] = new String[10];    	
    	String tipoProducto[] = new String[10];
    	String audioFinalizado[] = new String[10];
    	String opcionProducto[] = new String[10];
    	String marque[] = new String[10];
    	String dtmfs[] = new String[10];
    	
    	for (int h=0; h < 10; h++){
    		audioPara[h] = "es-CL/IVR/General/Silencio.vox";        	
        	tipoProducto[h] = "es-CL/IVR/General/Silencio.vox";    	
        	audioFinalizado[h] = "es-CL/IVR/General/Silencio.vox";
        	opcionProducto[h] = "";        	
        	marque[h] = "es-CL/IVR/General/Silencio.vox";
        	dtmfs[h] = "";
    	}
			
    	//Extrayendo los valores a emitir en el menu
    	/*
    	 *FORMATO 
    	 *ID;AudioPara;AudioTipo;Variable_Opcion;AudioFinalizado;ESTATUS;TOKEN|..|ID;AudioPara;AudioTipo;AudioFinalizado;Variable_Opcion;Token 
    	 *   
    	 *EJEMPLO:
    	 *Menu Standard - Para solicitar aumento de cupo, marque 3.
    	 *3;1061.wav;Silencio.wav;;Silencio.wav;SI;OPC3
    	 *
    	 *Menu con Valor al Final 	- Para tomar un Avance por <MontoAV> pesos, marque 2.
    	 *2;1060.wav;Silencio.wav;5000;1055.wav;SI;OPC2    
    	 *	 
    	 *
    	 *NOTA: 1055.wav = pesos
    	 */
    	String [] arrMenu = OpcionesMenu.split("\\|");		
    	String [] arrDTMF = OpcionesDTMF.split("\\|");
  	
    	
    	for (int i = 0; i < arrDTMF.length; i++){
    		String opcionDTMF = arrDTMF[i];
    		
    		String id = arrMenu[i].split(";")[0];
    		String audioParai = arrMenu[i].split(";")[1];
    		String audioTipoi = arrMenu[i].split(";")[2];
    		String valor = arrMenu[i].split(";")[3];
    		String audioFinalizadoi = arrMenu[i].split(";")[4];    		
    		String estatus = arrMenu[i].split(";")[5];
    		String token = arrMenu[i].split(";")[6];

    		int dtmf = Integer.valueOf(opcionDTMF);
    		
    		if (estatus.equalsIgnoreCase("si")){
    			audioPara[dtmf] = audioParai;
    			tipoProducto[dtmf] = audioTipoi;
    			opcionProducto[dtmf] = valor;
    			audioFinalizado[dtmf] = audioFinalizadoi;
    			dtmfs[dtmf] = opcionDTMF;
    			tokenSalidas = tokenSalidas + token +";";
    			idMenuSalidas = idMenuSalidas +id + ";";
    			
//     			if (dtmf == 1){
//     				marque[dtmf] = "1010.wav";
//     			}if (dtmf == 0 || dtmf == 9){
//     				dtmfs[dtmf] = "";
//     			}			
    		}else{
    			tokenSalidas = tokenSalidas + token +";";
    			idMenuSalidas = idMenuSalidas +id + ";";
    		}
    		
    		fEPCS.Debug("audioFinalizado"+dtmf +"-" +audioFinalizado[dtmf]);
    		fEPCS.Debug("audioPara"+dtmf  +"-" +audioPara[dtmf]);
    		
    		fEPCS.Debug("opcionProducto"+dtmf+"-" + opcionProducto[dtmf]);
    		fEPCS.Debug("tipoProducto"+dtmf+"-" +tipoProducto[dtmf]);
    		
    		// Parametros a enviar para emitir audios
			result.put("audioPara"+dtmf, audioPara[dtmf]);
        	result.put("tipoProducto"+dtmf, tipoProducto[dtmf]);
        	result.put("opcionProducto"+dtmf, opcionProducto[dtmf]);
        	result.put("audioFinalizado"+dtmf, audioFinalizado[dtmf]);        	
        	result.put("marque"+dtmf, marque[dtmf]);
        	result.put("dtmf"+dtmf, dtmfs[dtmf]);
    	}				  	    
    	
    	tokenSalidas.trim();
    	fEPCS.Debug("Token de Salidas: " + tokenSalidas);	
    	result.put("tokenSalidas", tokenSalidas);
    	result.put("idMenuSalidas", idMenuSalidas);
    	fEPCS.Debug("*************************************************************************");
    }catch (Exception ex){
    	fEPCS.DebugError("Hubo un ERROR : MenuDinamico "+ex.getMessage());
    	ex.printStackTrace();
    }
    				
	
	return result;    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>