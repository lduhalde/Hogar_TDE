package bot;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
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

import com.microsoft.schemas._2003._10.Serialization.Arrays.ArrayOfKeyValueOfstringstringKeyValueOfstringstring;

public class TestMessageBroker {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 String aString="JUST_A_TEST_STRING";
		 String result = UUID.nameUUIDFromBytes(aString.getBytes()).toString();		
		 System.out.println(result);
		 
		 aString = "12312313";
		 result = UUID.nameUUIDFromBytes(aString.getBytes()).toString();		 
		 System.out.println(result);
		 

		 aString = "12312314";
		 result = UUID.nameUUIDFromBytes(aString.getBytes()).toString();		 
		 System.out.println(result);
		
		String endpoint = "http://e-contact-bankbot-api.azurewebsites.net/MessagesController.svc";
		
		BotMessageBrokerClient client = new BotMessageBrokerClient(endpoint);
		client.setTimeout(8000);
		try {
			
			
			ArrayOfKeyValueOfstringstringKeyValueOfstringstring[] initializedEntities = new ArrayOfKeyValueOfstringstringKeyValueOfstringstring[1];
			
			SessionCapacity[] capacities = new SessionCapacity[1];
			
			String[] cap = {"PhoneCall"};	
			
			SessionCapacity capacity = new SessionCapacity();
			capacity.setClientId("937b6400-a8f1-4fe9-a8f7-86fd5afb2396");
			capacity.setName("GVP");
			capacity.setCapacityType(cap);
			
			capacities[0] = capacity;
						
			client.inicializar("CreateSession");
			CreateSessionResponse session = client.createSession(5, "990856037", "1234", null, capacities);
			String sessionId = session.getSessionId();
			System.out.println(sessionId);
			
			EventMessage[] messages = session.getEventMessages();
			for (EventMessage eventMessage : messages) {
				String[] events = eventMessage.getEvent();
				
				for (String event : events) {
					System.out.println("Message Event: " + event);
				}
				
				String[] eventTypes = eventMessage.getMessageType();
				for (String eventType : eventTypes) {
					System.out.println("Message Event Types: " + eventType);
				}
			}
			
			
			SendMessageResponse sendMessageResponse = client.sendTextMessage(sessionId, "Hola como estas", capacity);
			System.out.println(sendMessageResponse.getId());
			System.out.println(sendMessageResponse.getEventMessages());
			
			messages = sendMessageResponse.getEventMessages();
			for (EventMessage eventMessage : messages) {
				String[] events = eventMessage.getEvent();
				
				for (String event : events) {
					System.out.println("Send Message Event: " + event);
				}
				
				String[] eventTypes = eventMessage.getMessageType();
				for (String eventType : eventTypes) {
					System.out.println("Send Message Event Types: " + eventType);
				}
			}
			
			
			int count = 0; 
			while (count < 20){
				
				ReadMessageResponse readMessagesResponse = client.readMessages(sessionId, capacity);
				
//				messages = readMessagesResponse.getEventMessages();
//				
//				if (messages.length > 0){
//					for (EventMessage eventMessage : messages) {
//						String[] events = eventMessage.getEvent();
//						System.out.println("Read Message:" + eventMessage.getDetails());
//						
//						for (String event : events) {
//							System.out.println("Read Message Event: " + event);						
//						}
//						
//						String[] eventTypes = eventMessage.getMessageType();
//						for (String eventType : eventTypes) {
//							System.out.println("Read Message Event Types: " + eventType);
//						}						
//					}
//				}else{
//					System.out.println("Read Message Sin mensajes");
//				}
				
				
				TextMessage[] textMessages = readMessagesResponse.getTextMessages();
				
				if (textMessages.length > 0){
					for (TextMessage textMessage : textMessages) {
						System.out.println(textMessage.getVoiceText());
					}
				}else{
					System.out.println("Read Message Sin Text mensajes");
				}
				
				
				MenuMessage[] menuMessages = readMessagesResponse.getMenuMessages();
				
				if (menuMessages.length > 0){
					for (MenuMessage menuMessage : menuMessages) {
						System.out.println(menuMessage.getVoiceTitle());
						
						MenuOption[] options = menuMessage.getOptions();
						
						for (MenuOption menuOption : options) {
							System.out.println("Menu: " + menuOption.getText());
						}
					}
				}else{
					System.out.println("Read Message Sin Menu mensajes");
				}
				
				
				
				Thread.sleep(3000);
				count++;
				
				
				if (count == 4){
					sendMessageResponse = client.sendTextMessage(sessionId, "Quiero el saldo", capacity);
					System.out.println(sendMessageResponse.getId());
					System.out.println(sendMessageResponse.getEventMessages());
				}
			}
			
			
			
			System.out.println("Finalizar");
			
//			client.inicializar("EndSession");
			EndSessionResponse endSession = client.endSession(sessionId);
			System.out.println(endSession.getSessionId());
			TextMessage[] messagesEnd = endSession.getMessages();
			for (TextMessage textMessage : messagesEnd) {
				System.out.println(textMessage.getId());
			}
			
			System.out.println("fin");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
