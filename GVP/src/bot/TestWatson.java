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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.microsoft.schemas._2003._10.Serialization.Arrays.ArrayOfKeyValueOfstringstringKeyValueOfstringstring;

public class TestWatson {

	public static void main(String[] args) {
		FunctionsWatson watson = new FunctionsWatson("D:\\Git\\IBM_Watson\\FunctionsGVP.IVR_Bot.properties");
		
		
		try {
			JSONObject datosEntrada = new JSONObject();
			JSONObject text = new JSONObject();
			text.put("text", "puedes darme el número de pizza hut de huechuraba");			
			datosEntrada.put("input", text);
			
			JSONObject response = watson.sendMessageToWatson(datosEntrada);
			
			System.out.println("Response: " + response.toString());

			JSONArray intents = response.getJSONArray("intents");
			JSONArray entities = response.getJSONArray("entities");
			JSONObject context = response.getJSONObject("context");
			JSONObject output = response.getJSONObject("output");
			
			System.out.println("Intents Size: " + intents.length());
			for (int i=0; i < intents.length(); i++){
				JSONObject intent = intents.getJSONObject(i);
				System.out.println("intent " + i + " : " +intent.toString());
				
				System.out.println(intent.getString("intent"));
				System.out.println(intent.getDouble("confidence"));
				
				if (intent.getDouble("confidence") > 0.8){
					System.out.println("Confident");
				} 
				
			}
			
			System.out.println("Entities Size: " + entities.length());
			for (int i=0; i < entities.length(); i++){
				JSONObject entity = entities.getJSONObject(i);
				System.out.println("entity " + i + " : " +entity.toString());
				
				System.out.println("entity: " +entity.getString("entity"));
				System.out.println("value: " +entity.getString("value"));
				System.out.println("confidence: " +entity.getDouble("confidence"));
				
				if (entity.getDouble("confidence") > 0.8){
					System.out.println("Confident");
				}
			}
			/*

{
	"input": {
		"text": "puedes darme el número de pizza hut"
	},
	"intents": [{
		"confidence": 0.9219482421875,
		"intent": "consulta_numero"
	}],
	"context": {
		"system": {
			"dialog_request_counter": 1,
			"dialog_stack": [{
				"dialog_node": "root"
			}],
			"dialog_turn_counter": 1
		},
		"conversation_id": "4da34f95-f758-4e30-be80-d6b62bf64cfa"
	},
	"output": {
		"nodes_visited": [],
		"text": [],
		"log_messages": [{
			"level": "warn",
			"msg": "No dialog node condition matched to true in the last dialog round - context.nodes_visited is empty. Falling back to the root node in the next round."
		}],
		"warning": "No dialog node condition matched to true in the last dialog round - context.nodes_visited is empty. Falling back to the root node in the next round."
	},
	"entities": []
}
			 * */
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
