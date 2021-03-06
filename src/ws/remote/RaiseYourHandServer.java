package ws.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;

import ws.SocketInterface;
import dblayout.ManageDatabase;
import entities.Lecture;
import exception.RaiseYourHandError;
import exception.RaiseYourHandException;

public class RaiseYourHandServer extends Thread implements SocketInterface {
	private int port;
	private ServerSocket servSock = null;
	private static LinkedHashMap<Integer, Lecture> lectures;
	private ManageDatabase db;

	public RaiseYourHandServer(int port) throws RaiseYourHandException {
		setPort(port);
		lectures = new LinkedHashMap<Integer, Lecture>();
		try {
			db = new ManageDatabase();
		} catch (Exception e) {
			throw new RaiseYourHandException(RaiseYourHandError.SQL_FAILURE, e.getMessage());
		}
	}

	public void run() {
		if (openConnection()) {
			handleSession();
			closeSession();
		}
	}

	@Override
	public boolean openConnection() {
		try {
			servSock = new ServerSocket(port);
			System.out.println("Config Server has a socket and is listening on port "+port);
		} catch (IOException e) {
			new RaiseYourHandException(RaiseYourHandError.SOCKET_FAILURE, e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void handleSession() {
		while(true) {
			try {
				Socket connection = servSock.accept();
				ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

				new ServiceThread(connection, in, out, db, lectures).start();
			} catch (IOException e) {
				new RaiseYourHandException(RaiseYourHandError.OTHER, e.getMessage());			
			}
		}
	}

	@Override
	public void closeSession() {
		try {
			servSock.close();
		} catch (IOException e) {
			// Log that the thread was interrupted.
			new RaiseYourHandException(RaiseYourHandError.OTHER, e.getMessage());		
		}
	}

	public void setPort(int port) {
		this.port = port;
	}
}
