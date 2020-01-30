package bot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.UUID;

//import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.CreateSessionResponse;
//import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.EndSessionResponse;
//import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.EventMessage;
//import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.MenuMessage;
//import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.MenuOption;
//import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.ReadMessageResponse;
//import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.SendMessageResponse;
//import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.SessionCapacity;
//import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.TextMessage;
//import org.econtact.client.BotMessageBrokerClient;


//import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
//import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
//import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
//import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
//import com.microsoft.schemas._2003._10.Serialization.Arrays.ArrayOfKeyValueOfstringstringKeyValueOfstringstring;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import sun.misc.BASE64Encoder;

//import eContact.ConexionHTTP;
import eContact.FunctionsGVP;

public class FunctionsWatson extends FunctionsGVP{

	
	
	public FunctionsWatson(String ParametersFile) {
		super(ParametersFile);
	}

	
	public FunctionsWatson(String ParametersFile, String id) {
		super(ParametersFile, id);
		
		
	}


	public JSONObject sendMessageToWatson(JSONObject request){
    	JSONObject json = null;
    	
    	try {
    		Debug("[sendMessageToWatson] INICIO ");
    		
//    		{\"input\": {\"text\": \"Hello\"}}" "https://gateway.watsonplatform.net/conversation/api/v1/workspaces/9978a49e-ea89-4493-b33d-82298d3db20d/message?version=2017-05-26"
    		
    		String autorizacion= this.Params.GetValue("WATSON_AUTH", "");
    		//String password = this.Params.GetValue("WATSON_PASSWORD", "");
    		//String workspaceId = this.Params.GetValue("WATSON_WORKSPACE", "");
			String url = this.Params.GetValue("WATSON_URL", "");
			
			
			String authString = autorizacion;
	        String authStringEnc = new BASE64Encoder().encode(authString.getBytes());
	        
	        String retorno = "";
	        
	        Debug("[sendMessageToWatson] URL: "+url);
	        Debug("[sendMessageToWatson] authString: "+authString);
	        
	        
	        Debug("[sendMessageToWatson] Request: "+request.toString());
			Client client = Client.create();
			WebResource webResource = client.resource(url);
			
			
			ClientResponse response = webResource.type("application/json")
											.header("Authorization", "Basic " + authStringEnc)
											.post(ClientResponse.class, request.toString());
			
			
//			Debug("[sendMessageToWatson] Response: "+response.toString());			
			retorno = response.getEntity(String.class);
			
			Debug("[sendMessageToWatson] Response: "+retorno);
			
			json = new JSONObject(retorno);
			
		}catch (Exception e) {
			// TODO Auto-generated catch block

			Debug("[sendMessageToWatson] Exception " + e.getMessage());
			e.printStackTrace();
		}finally{
			Debug("[sendMessageToWatson] FIN ");
		}
    	
    	return json;
    }
	
	
	/*
	public void sendMessage(String message){
//		JSONObject response = new JSONObject();
		
		String username = this.Params.GetValue("WATSON_USERNAME", "");
		String password = this.Params.GetValue("WATSON_PASSWORD", "");
		String workspaceId = this.Params.GetValue("WATSON_WORKSPACE", "");
		try{
			Debug("[sendMessage] Start - " + message);
			
			Conversation service = new Conversation(Conversation.VERSION_DATE_2017_05_26);
			service.setUsernameAndPassword(username, password);

			InputData input = new InputData.Builder(message).build();
			MessageOptions options = new MessageOptions.Builder(workspaceId).input(input).build();
			MessageResponse response = service.message(options).execute();
			System.out.println(response);
			
//			response.put("bot_clientId", clientId);
//			response.put("bot_sessionId", sessionId);
			
		}catch (Exception ex){
			Debug("[sendMessage] Exception " + ex.getMessage());
		}
		
//		return response;
	}
	 */
	
	
	/*DEPRECATED*/
	/*
	public JSONObject watsonIniciar(){
    	JSONObject json = null;
    	ConexionHTTP conexion = new ConexionHTTP();
		try {
		
			Debug("[watsonIniciar] INICIO ");
			String dialog_id = this.getParametro("dialog_id");//"089488c4-ec28-468b-9976-477f794a6d47";
			String requestURL = this.getParametro("dialog_url")+dialog_id+"/conversation";
			String retorno = "";
			
			HashMap<String, Object> postDataParams = new HashMap<String, Object>();		
			postDataParams.put("input", "");
			retorno = conexion.performPostCall(requestURL, postDataParams);
			
			Debug("[watsonIniciar] Retorno: "+retorno);
			json = new JSONObject(retorno);
			
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			Debug("[watsonIniciar] FIN ");
		}
		
		return json;
    }
    
    public JSONObject watsonEnviarMensaje(JSONObject datosEntrada){
    	JSONObject json = null;
    	ConexionHTTP conexion = new ConexionHTTP();
    	try {
    		Debug("[watsonEnviarMensaje] INICIO ");
    		
    		String dialog_id = this.getParametro("dialog_id");//"089488c4-ec28-468b-9976-477f794a6d47";
			String requestURL = this.getParametro("dialog_url")+dialog_id+"/conversation";
			String retorno = "";
			
			HashMap<String, Object> postDataParams = new HashMap<String, Object>();		
			postDataParams.put("input", datosEntrada.getString("input"));
			postDataParams.put("conversation_id", datosEntrada.getString("conversation_id"));
			postDataParams.put("client_id", datosEntrada.getString("client_id"));
			
			retorno = conexion.performPostCall(requestURL, postDataParams);
			Debug("[watsonEnviarMensaje] Retorno: "+retorno);
			json = new JSONObject(retorno);
			
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			Debug("[watsonEnviarMensaje] FIN ");
		}
    	
    	return json;
    }
    
    
    public String watsonRecognize(String archivo){
    	String retorno = "";
    	    	
    	try {
    		
    		Debug("[watsonRecognize] INICIO");
    		
    		AppSpeechToText app = new AppSpeechToText();
			retorno = app.recognize(archivo);
			
			Debug("[watsonRecognize] Retorno="+retorno);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Debug("[watsonRecognize] ERROR "+e.getMessage());
			e.printStackTrace();
		}
    	Debug("[watsonRecognize] FIN");
    	return retorno;
    }
    */
	
	
}
