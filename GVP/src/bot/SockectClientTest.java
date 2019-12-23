package bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SockectClientTest {

	public static void main(String[] args) {
//		BotSocket.SERVER_HOST = "127.0.0.1";
//		BotSocket.SERVER_HOST = "localhost";
//		BotSocket.SERVER_PORT = 2004;
//		BotSocket.SERVER_TIMEOUT = 10000;
		
//		BotSocket.SERVER_HOST = "10.40.66.24";
		BotSocket.SERVER_HOST = "10.33.32.48";
		BotSocket.SERVER_PORT = 50039;
		BotSocket.SERVER_TIMEOUT = 10000;
		
		BotSocket bot = new BotSocket();
		int i = 0;
		System.out.println("Conectando");
		String response;
		try {
			response = bot.messageToBot("Session1", "IvrToMQ Accion  Login\n");
			System.out.println("response: " + response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						
		
	}
}
