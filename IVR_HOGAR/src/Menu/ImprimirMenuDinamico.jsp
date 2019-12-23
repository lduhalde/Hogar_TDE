<%@page language="java" contentType="application/json" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.io.*" %>
<%@page import="eContact.*, epcs.*"%>
<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {
    
	JSONObject result = new JSONObject(); 
	
	  
	FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));  
	  
	try{
		String willaccept = state.getString("willaccept");
	    String esOpcionValido = state.getString("esOpcionValido");
	    String Menu1 = state.getString("Menu1");    
	    
	    String idMenuSalidas = state.getString("idMenuSalidas");
	    String tokenSalidas = state.getString("tokenSalidas");
	    String OpcionesDTMF = state.getString("OpcionesDTMF");
	   
	    String Datosvalidos = "";
		                 
	    fEPCS.Debug(" ******** Menu Seleccionado ******* ");
	    fEPCS.Debug("DTMF presionado = " + Menu1);
	    fEPCS.Debug("ID Salida: " + idMenuSalidas);
	    fEPCS.Debug("Token Salida: " + tokenSalidas);
	    fEPCS.Debug("OpcionesDTMF: " + OpcionesDTMF);
		fEPCS.Debug(" ******** FIN Imprimir Menu ******* ");
	    
		String [] arrDTMF = OpcionesDTMF.split("\\|");
		String [] arrToken = tokenSalidas.split(";");
		String [] arrIdMenu = idMenuSalidas.split(";");
		
		String idMenu = "";
		String token = "";
		fEPCS.Debug("Menu1: "+Menu1); 
		fEPCS.Debug("arrDTMF: "+arrDTMF.toString()); 
		
		
		if (Menu1.equals("x")){
			String volverMenu = state.getString("usa_asterisco");
			
			if (volverMenu.equals("SI")){
				willaccept = "MP";
		    	token = "MP";		    	
		    	esOpcionValido = "true";
		    	fEPCS.Debug("Menu Valido. ID: x Token: "+token); 			
			}else{
				 esOpcionValido="false";
			}
		}else{
			for (int i = 0; i < arrDTMF.length; i++){
				String opcionDTMF = arrDTMF[i];
				fEPCS.Debug("opcionDTMF "+opcionDTMF); 
				if(opcionDTMF.equals(Menu1)){
			    	willaccept = arrToken[i];
			    	token = arrToken[i];
			    	idMenu = arrIdMenu[i];
			    	esOpcionValido = "true";
			    	fEPCS.Debug("Menu Valido. ID: "+idMenu+" Token: "+token); 
			    	break;
			    }else{
				   esOpcionValido="false";
			   }	
			}
		}
		
				
		result.put("esOpcionValido", esOpcionValido);
		result.put("willaccept", willaccept);	
		result.put("Token", token);
		result.put("IDMenu", idMenu);
		fEPCS.Debug("esOpcionValido: " + esOpcionValido);
		fEPCS.Debug("willaccept: " + willaccept);
		fEPCS.Debug("Token Salida: " + token);
	    fEPCS.Debug("IDMenu: " + idMenu);
	}catch (Exception e){
		fEPCS.DebugError("Hubo un ERROR : ImprimirMenu "+e.getMessage());
		e.printStackTrace();
	}
    
	
	
	return result; 
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>