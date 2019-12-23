package bot;

import java.io.IOException;
import java.util.UUID;

import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.CreateSessionResponse;
import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.EndSessionResponse;
import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.EventMessage;
import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.MenuMessage;
import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.MenuOption;
import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.ReadMessageResponse;
import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.SendMessageResponse;
import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.SessionCapacity;
import org.datacontract.schemas._2004._07.WCF_Voice_Bot_Engine.TextMessage;
import org.econtact.client.BotMessageBrokerClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.microsoft.schemas._2003._10.Serialization.Arrays.ArrayOfKeyValueOfstringstringKeyValueOfstringstring;

import eContact.FunctionsGVP;

public class FunctionsBot extends FunctionsGVP{

	
	
	public FunctionsBot(String ParametersFile) {
		super(ParametersFile);
	}

	
	public FunctionsBot(String ParametersFile, String id) {
		super(ParametersFile, id);
		
		
	}

//	public String sendMessageToBot(String message) throws IOException{
//		String response = "";
//		
//		BotSocket.SERVER_HOST = this.Params.GetValue("BotSocketServer");
//		BotSocket.SERVER_PORT = Integer.valueOf(this.Params.GetValue("BotSocketPort"));
//		BotSocket.SERVER_TIMEOUT = Integer.valueOf(this.Params.GetValue("BotSocketTimeout"));
//		
//		Debug("[sendMessageToBot] Request: " + message);
//		BotSocket bot = new BotSocket();
//			
//		try {
//			response = bot.messageToBot(this.InstanceID, message);			
//			Debug("[sendMessageToBot] Response: " + response);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//		
//		return response;
//	}
	
	public JSONObject createSessionBot(JSONObject input){
		JSONObject response = new JSONObject();
		
		String endpoint = this.Params.GetValue("BOT_URL", "http://e-contact-bankbot-api.azurewebsites.net/MessagesController.svc");
		int timeout = Integer.valueOf(this.Params.GetValue("BOT_TIMEOUT", "8000"));
		BotMessageBrokerClient client = new BotMessageBrokerClient(endpoint);
		client.setTimeout(timeout);
		
		try{
			Debug("[createSessionBot] Start - " + endpoint);
			
			SessionCapacity[] capacities = new SessionCapacity[1];			
			String[] cap = {"PhoneCall"};	
			String clientId = UUID.nameUUIDFromBytes(input.getString("ConnID").getBytes()).toString();	
			
			SessionCapacity capacity = new SessionCapacity();
			capacity.setClientId(clientId);
			capacity.setName("GVP");
			capacity.setCapacityType(cap);
			
			capacities[0] = capacity;
			
			client.inicializar("CreateSession");
			CreateSessionResponse session = client.createSession(5, "990856037", "1234", null, capacities);
			String sessionId = session.getSessionId();
			
			Debug("[createSessionBot] Bot ClientID : " + clientId);
			Debug("[createSessionBot] Bot SessionID : " + sessionId);
			
			EventMessage[] messages = session.getEventMessages();
			for (EventMessage eventMessage : messages) {
				String[] events = eventMessage.getEvent();
				String[] eventTypes = eventMessage.getMessageType();
				
				for (int i=0; i < events.length; i++){
					Debug("[createSessionBot] Event " + eventTypes[i] + " : " + events[i]);
				}
			}
			
			response.put("bot_clientId", clientId);
			response.put("bot_sessionId", sessionId);
			
		}catch (Exception ex){
			Debug("[createSessionBot] Exception " + ex.getMessage());
		}
		
		return response;
	}

	public void sendMessageToBot(JSONObject input){
//		JSONObject response = new JSONObject();
		
		String endpoint = this.Params.GetValue("BOT_URL", "http://e-contact-bankbot-api.azurewebsites.net/MessagesController.svc");
		int timeout = Integer.valueOf(this.Params.GetValue("BOT_TIMEOUT", "8000"));
		BotMessageBrokerClient client = new BotMessageBrokerClient(endpoint);
		client.setTimeout(timeout);
		
		try{
			Debug("[sendMessageToBot] Start");
			
			String clientId = input.getString("bot_clientId");
			String sessionId = input.getString("bot_sessionId");
			String message = input.getString("bot_message");
						
			
			String[] cap = {"PhoneCall"};	
			
			SessionCapacity capacity = new SessionCapacity();
			capacity.setClientId(clientId);
			capacity.setName("GVP");
			capacity.setCapacityType(cap);
			
			client.inicializar("SendTextMessage");
			SendMessageResponse sendMessageResponse = client.sendTextMessage(sessionId, message, capacity);			
			
//			Debug("[sendMessageToBot] Bot ClientID : " + clientId);
//			Debug("[sendMessageToBot] Bot SessionID : " + sessionId);
			
			EventMessage[] messages = sendMessageResponse.getEventMessages();
			for (EventMessage eventMessage : messages) {
				String[] events = eventMessage.getEvent();
				String[] eventTypes = eventMessage.getMessageType();
				
				for (int i=0; i < events.length; i++){
					Debug("[sendMessageToBot] Event " + eventTypes[i] + " : " + events[i]);
				}
				
			}
			
			
		}catch (Exception ex){
			Debug("[sendMessageToBot] Exception " + ex.getMessage());
		}
		
	}
	
	
	public JSONObject sendMenuMessageToBot(JSONObject input){
		JSONObject response = new JSONObject();
		
		String endpoint = this.Params.GetValue("BOT_URL", "http://e-contact-bankbot-api.azurewebsites.net/MessagesController.svc");
		int timeout = Integer.valueOf(this.Params.GetValue("BOT_TIMEOUT", "8000"));
		BotMessageBrokerClient client = new BotMessageBrokerClient(endpoint);
		client.setTimeout(timeout);
		
		try{
			Debug("[sendMenuMessageToBot] Start");
			
			String clientId = input.getString("bot_clientId");
			String sessionId = input.getString("bot_sessionId");
			String message = input.getString("bot_message");
						
			
			String[] cap = {"PhoneCall"};	
			
			SessionCapacity capacity = new SessionCapacity();
			capacity.setClientId(clientId);
			capacity.setName("GVP");
			capacity.setCapacityType(cap);
			
			SessionCapacity[] capacities = new SessionCapacity[1];
			capacities[0] = capacity;
			
			client.inicializar("SendMenuResponseByLevenshteinDistance");
			
			JSONArray jMenuMessageArray = input.getJSONArray("bot_message_menu_options");			
			for(int i=0; i<jMenuMessageArray.length(); i++){
				
				JSONObject jMenuMessage = jMenuMessageArray.getJSONObject(i);				
				
				MenuMessage baseMenu = new MenuMessage();
				baseMenu.setCapacities(capacities);
				baseMenu.setId(jMenuMessage.getString("id"));
				baseMenu.setAvailableByText(jMenuMessage.getBoolean("availableByText"));
				baseMenu.setAvailableByVoice(jMenuMessage.getBoolean("availableByVoice"));
				baseMenu.setCorrelation(jMenuMessage.getInt("correlation"));
				baseMenu.setSessionId(jMenuMessage.getString("sessionId"));
				baseMenu.setTitle(jMenuMessage.getString("title"));
				baseMenu.setVoiceTitle(jMenuMessage.getString("voiceTitle"));				
				
				JSONArray jOptionsArray = jMenuMessage.getJSONArray("menuOptions");
				MenuOption[] options = new MenuOption[jOptionsArray.length()];
				for (int j=0; j < jOptionsArray.length(); j++){
					JSONObject jMenuOption = jOptionsArray.getJSONObject(j);
					
					MenuOption option = new MenuOption();
					option.setIndex(jMenuOption.getInt("index"));
					option.setText(jMenuOption.getString("text"));					
					options[j] = option;
				}
				baseMenu.setOptions(options);
				
				SendMessageResponse sendMessageResponse = client.sendMenuResponseByLevenshteinDistance(sessionId, message, capacity, baseMenu);			

				String eventResponse = "";
				
				EventMessage[] messages = sendMessageResponse.getEventMessages();
				for (EventMessage eventMessage : messages) {
					String[] events = eventMessage.getEvent();
					String[] eventTypes = eventMessage.getMessageType();
					
					eventResponse = eventMessage.getDetails();
					
					response.put("eventMessage_detail", eventResponse);
					
					Debug("[sendMenuMessageToBot] Event Detail " + eventResponse);
					
					
					for (int h=0; h < events.length; h++){
						Debug("[sendMenuMessageToBot] " + eventTypes[h] + " : " + events[h]);
						response.put("eventMessage_event", events[h]);
					}
					
				}
				
			}
		}catch (Exception ex){
			Debug("[sendMenuMessageToBot] Exception " + ex.getMessage());
		}
		return response;
	}
	
	public JSONObject getMessagesFromBot(JSONObject input){
		JSONObject response = new JSONObject();
		
		String endpoint = this.Params.GetValue("BOT_URL", "http://e-contact-bankbot-api.azurewebsites.net/MessagesController.svc");
		int timeout = Integer.valueOf(this.Params.GetValue("BOT_TIMEOUT", "8000"));
		BotMessageBrokerClient client = new BotMessageBrokerClient(endpoint);
		client.setTimeout(timeout);
		
		try{
			Debug("[getMessagesFromBot] Start - " + endpoint);

			
			
			response.put("bot_message", "");
			response.put("bot_message_type", "");
//			response.put("bot_message_menu_options", "");
			
			String clientId = input.getString("bot_clientId");
			String sessionId = input.getString("bot_sessionId");
						
			String[] cap = {"PhoneCall"};	
			
			SessionCapacity capacity = new SessionCapacity();
			capacity.setClientId(clientId);
			capacity.setName("GVP");
			capacity.setCapacityType(cap);
			
			client.inicializar("ReadMessages");
			ReadMessageResponse readMessagesResponse = client.readMessages(sessionId, capacity);			
			
//			Debug("[getMessagesFromBot] Bot ClientID : " + clientId);
//			Debug("[getMessagesFromBot] Bot SessionID : " + sessionId);
			
			TextMessage[] textMessages = readMessagesResponse.getTextMessages();			
			if (textMessages.length > 0){
				Debug("[getMessagesFromBot] Bot has Text Messages " + textMessages.length);
				for (TextMessage textMessage : textMessages) {
					Debug("[getMessagesFromBot] Bot Message: " + textMessage.getVoiceText());
					response.put("bot_message_type", "TEXT");
					response.put("bot_message", textMessage.getVoiceText());
				}
			}else{
				Debug("[getMessagesFromBot] Bot doesnt have Text Messages ");
			}
			
			
			MenuMessage[] menuMessages = readMessagesResponse.getMenuMessages();			
			if (menuMessages.length > 0){
				JSONArray jMenuMessageArray = new JSONArray();
				JSONArray jOptionsArray = new JSONArray();
								
				Debug("[getMessagesFromBot] Bot has Menu Messages " + textMessages.length);
				String botMessage = "";
				
				for (MenuMessage menuMessage : menuMessages) {

					JSONObject jMenuMessage = new JSONObject();
					jMenuMessage.put("id", menuMessage.getId());
					jMenuMessage.put("availableByText", menuMessage.getAvailableByText());
					jMenuMessage.put("availableByVoice", menuMessage.getAvailableByVoice());
					jMenuMessage.put("correlation", menuMessage.getCorrelation());
					jMenuMessage.put("sessionId", menuMessage.getSessionId());
					jMenuMessage.put("title", menuMessage.getTitle());
					jMenuMessage.put("voiceTitle", menuMessage.getVoiceTitle());
					
					botMessage = menuMessage.getVoiceTitle();
					MenuOption[] options = menuMessage.getOptions();					
					for (MenuOption menuOption : options) {							
						botMessage += "\n " + menuOption.getText() + ", ";
						JSONObject option = new JSONObject();
						option.put("index", menuOption.getIndex());
						option.put("text", menuOption.getText());
						jOptionsArray.put(option);
					}
					
					jMenuMessage.put("menuOptions", jOptionsArray);					
					jMenuMessageArray.put(jMenuMessage);
					
				}
				
				Debug("[getMessagesFromBot] Bot Menu Message " + botMessage);
				response.put("bot_message_type", "MENU");
				response.put("bot_message", botMessage);
				response.put("bot_message_menu_options", jMenuMessageArray);
			}else{
				Debug("[getMessagesFromBot] Bot doesnt have Menu Messages.");
			}
			
			
		}catch (Exception ex){
			Debug("[getMessagesFromBot] Exception " + ex.getMessage());
		}
		
		return response;
	}
	
	
	public void endSessionBot(JSONObject input){
		
		String endpoint = this.Params.GetValue("BOT_URL", "http://e-contact-bankbot-api.azurewebsites.net/MessagesController.svc");
		int timeout = Integer.valueOf(this.Params.GetValue("BOT_TIMEOUT", "8000"));
		BotMessageBrokerClient client = new BotMessageBrokerClient(endpoint);
		client.setTimeout(timeout);
		
		try{
			Debug("[endSessionBot] Start");
			
			String sessionId = input.getString("bot_sessionId");
						
			client.inicializar("EndSession");
			EndSessionResponse endSession = client.endSession(sessionId);
			
			
			TextMessage[] messages = endSession.getMessages();
			for (TextMessage eventMessage : messages) {
				Debug("[endSessionBot] Message " + eventMessage.getText());
			}
			
			
		}catch (Exception ex){
			Debug("[endSessionBot] Exception " + ex.getMessage());
		}
		
	}
	
	public static void delay(int milliseconds){
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
