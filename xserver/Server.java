package xserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import xdatabase.DatabaseManager;

public class Server implements ThreadCompletionCallback{
	private final static int PORT = 0;
	private static int noOfConnections = 0;
	private static final int QUEUE_CAPACITY = 1000;
	public static void main(String args[]) {
		Server server = new Server();
		DatabaseManager databaseManager = new DatabaseManager();
		try {
			while (true) {
				ServerSocket sock = new ServerSocket(PORT);
				Socket s = sock.accept();
				BlockingQueue<String> queue = new ArrayBlockingQueue(QUEUE_CAPACITY);
				Connection c = new Connection(s,server,queue,databaseManager);
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