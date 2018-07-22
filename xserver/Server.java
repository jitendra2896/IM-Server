package xserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements ThreadCompletionCallback{
	private final static int PORT = 0;
	private static int noOfConnections = 0;
	
	public static void main(String args[]) {
		Server server = new Server();
		try {
			while (true) {
				ServerSocket sock = new ServerSocket(PORT);
				Socket s = sock.accept();
				Connection c = new Connection(s,server);
				Thread t = new Thread(c);
				t.start();
				noOfConnections++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void callback() {
		noOfConnections--;
	}
}
