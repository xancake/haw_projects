package org.haw.praktikum2.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.haw.praktikum2.Protokoll;
import org.haw.praktikum2.client.ui.ChatClientCLI;
import org.haw.praktikum2.client.ui.ChatClientUI;
import org.haw.praktikum2.old.shared.connection.LoggingPrintWriter;
import org.haw.praktikum2.shared.io.LoggingBufferedReader;

public class ChatClient {
	private static final Logger LOGGER = Logger.getLogger(ChatClient.class.getName());
	
	private static final String PROPERTIES_DEFAULT = "praktikum2/chat_client.properties";
	private static final String PROPERTY_SERVER_ADDRESS = "chat.server.address";
	private static final String PROPERTY_SERVER_PORT   = "chat.server.port";
	
	private ChatClientUI _ui;
	private Socket socket;
	private BufferedReader _in;
	private PrintWriter _out;
	
	public ChatClient(ChatClientUI ui, String hostname, int serverPort) throws IOException {
		_ui = ui;
		socket = new Socket(hostname, serverPort);
		_in = new LoggingBufferedReader(socket.getInputStream());
		_out = new LoggingPrintWriter(socket.getOutputStream());
	}
	
	public void run() {
		try {
			boolean authentifiziert = false;
			do {
				_ui.showStatusmeldung("Bitte geben Sie ihren Benutzernamen ein: ");
				String username = _ui.getEingabe();
				authentifiziert = authenticate(username);
				if(!authentifiziert) {
					_ui.showStatusmeldung("Der Benutzername wurde vom Server nicht akzeptiert");
				}
			} while(!authentifiziert);
			
			
			ChatClientEmpfaengerThread empfaenger = new ChatClientEmpfaengerThread(_ui, _in);
			ChatClientSenderThread sender = new ChatClientSenderThread(_ui, _out);
			
			empfaenger.start();
			sender.start();
			
			empfaenger.join();
			sender.join();
			
		} catch(IOException e) {
			LOGGER.severe("Connection aborted by server!");
		} catch(InterruptedException e) {
			LOGGER.severe(e.toString());
		} finally {
			try {
				socket.close();
			} catch(IOException e) {
				LOGGER.warning(e.toString());
			}
		}
		LOGGER.fine("TCP Client stopped!");
	}
	
	private boolean authenticate(String username) throws IOException {
		_out.println(Protokoll.AUTHENTIFICATION + " " + username);
		String response = _in.readLine();
		if(Protokoll.AUTH_ACCEPT.equals(response)) {
			return true;
		} else if(Protokoll.AUTH_DECLINE.equals(response)) {
			return false;
		} else {
			// Könnte auch eine Exception aufgrund fehlerhaften Protokolls werfen
			return false;
		}
	}
	
	public static void main(String[] args) throws SecurityException, IOException {
		LogManager.getLogManager().readConfiguration(ClassLoader.getSystemResourceAsStream("praktikum2/chat_client.properties"));
		
		try {
			Properties properties = new Properties();
			properties.load(ClassLoader.getSystemResourceAsStream(PROPERTIES_DEFAULT));
			
			String address = properties.getProperty(PROPERTY_SERVER_ADDRESS);
			int port = Integer.parseInt(properties.getProperty(PROPERTY_SERVER_PORT));
			
			ChatClient client = new ChatClient(new ChatClientCLI(System.in, System.out), address, port);
			client.run();
		} catch(Exception e) {
			LOGGER.severe(e.toString());
			throw e;
		}
	}
}
