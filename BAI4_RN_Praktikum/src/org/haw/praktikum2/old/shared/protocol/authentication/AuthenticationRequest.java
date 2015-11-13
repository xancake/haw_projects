package org.haw.praktikum2.old.shared.protocol.authentication;

import org.haw.praktikum2.old.shared.ChatServer;
import org.haw.praktikum2.old.shared.connection.ChatConnection;
import org.haw.praktikum2.old.shared.exception.ChatException;
import org.haw.praktikum2.old.shared.protocol.AbstractRequest;
import org.haw.praktikum2.old.shared.protocol.Request;
import org.haw.praktikum2.old.shared.protocol.Response;

@SuppressWarnings("serial")
public class AuthenticationRequest extends AbstractRequest implements Request {
	private String _username;
	
	public AuthenticationRequest(String username) {
		_username = username;
	}
	
	@Override
	public Response respond(ChatServer server, ChatConnection connection) {
		try {
			server.authenticate(connection, _username);
			return new AuthenticationResponse(this);
		} catch(ChatException e) {
			return new AuthenticationResponse(this, e);
		}
	}
	@Override
	public final String toString() {
		return getClass().getName() + " " + _username;
	}
}