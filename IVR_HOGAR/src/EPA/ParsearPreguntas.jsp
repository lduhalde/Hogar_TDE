<%@page language="java" contentType="application/json;charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.*, java.io.*, java.text.SimpleDateFormat" %>
<%@page import="eContact.*, epcs.*"%>

<%!
// Implement this method to execute some server-side logic.
public JSONObject performLogic(JSONObject state, Map<String, String> additionalParams) throws Exception {

	String respuestaDB = additionalParams.get("RespuestaDB");
	int QPreguntas = Integer.parseInt(additionalParams.get("QPreguntas"));
	int Contador = Integer.parseInt(additionalParams.get("ContadorEPA"));
	String Pregunta = "";
	String nameJSP="ParsearPreguntas";
    String trx_respuesta="NOK";
    JSONObject result = new JSONObject(); 
    JSONObject  DataEnvioSckt = new JSONObject();
    String AudioPregunta="";
    String RespuestaMinima="";
    String RespuestaMaxima="";
    String IdPregunta="";
    String Salida="1";
    
    FunctionsEPCS fEPCS = new FunctionsEPCS(state.getString("ConfigFile"), state.getString("idLlamada"));
    
    try{
    	
    	fEPCS.Debug("["+nameJSP+"] INICIO", "INFO");
    	fEPCS.Debug("["+nameJSP+"] RespuestaDB  :"+respuestaDB, "INFO");
    	fEPCS.Debug("["+nameJSP+"] QPreguntas   :"+QPreguntas, "INFO");
    	fEPCS.Debug("["+nameJSP+"] Contador EPA :"+Contador, "INFO");
    	
    	if(Contador<=QPreguntas){
    		Salida="2";
    		
    		if(!respuestaDB.equals("[]")||respuestaDB.indexOf("errorMessage")==-1){
				JSONObject jObjRespuesta = new JSONObject(respuestaDB.replaceAll("\\[", "").replaceAll("\\]", ""));
				Pregunta = jObjRespuesta.getString("P"+Contador);
				String[] PreguntaArray = Pregunta.split(";"); // se cambia Pregunta.split(",") por Pregunta.split(";") modificacion FAM 
				IdPregunta=PreguntaArray[0];
				AudioPregunta=PreguntaArray[6];
				RespuestaMinima=PreguntaArray[2];
				RespuestaMaxima=PreguntaArray[3];
				trx_respuesta="OK";
				fEPCS.Debug("["+nameJSP+"] Pregunta: "+Pregunta, "INFO");
				fEPCS.Debug("["+nameJSP+"] IDPregunta: "+IdPregunta, "INFO");
				fEPCS.Debug("["+nameJSP+"] AudioPregunta: "+AudioPregunta, "INFO");
				fEPCS.Debug("["+nameJSP+"] RespuestaMinima: "+RespuestaMinima, "INFO");
				fEPCS.Debug("["+nameJSP+"] RespuestaMaxima: "+RespuestaMaxima, "INFO");
				jObjRespuesta = null;
			}
			
    		result.put("IdPregunta", IdPregunta);
			result.put("AudioPregunta", AudioPregunta);
			result.put("RespuestaMinima", RespuestaMinima);
			result.put("RespuestaMaxima", RespuestaMaxima);
    	}
    	result.put("AudioPregunta", AudioPregunta);//fam prueba
    	fEPCS.Debug("["+nameJSP+"] AudioPregunta: "+AudioPregunta, "INFO"); //fam prueba
    	
    	fEPCS.Debug("["+nameJSP+"] Salida: "+Salida, "INFO");
    	result.put("Salida", Salida);
    }catch(Exception ex){
    	fEPCS.Debug("["+nameJSP+"] Error : "+ex.getMessage());
    	ex.printStackTrace();
    }finally{
 
    	result.put("trx_respuesta", trx_respuesta);
    	fEPCS.Debug("["+nameJSP+"] FIN result: "+result.toString(), "INFO");
    	
    	DataEnvioSckt = null;
    }
    
    return result;
    
};
%>
<%-- GENERATED: DO NOT REMOVE --%> 
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<%@page import="java.util.Map"%>
<%@include file="../../include/backend.jspf" %>