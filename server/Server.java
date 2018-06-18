package server;

import java.net.*;

import filestorage.FileManager;
import global.DataManager;
class Server{
	private static int noOfConnection = 0;
	public static void main(String args[]){
		
		DataManager manager = new FileManager();
		try{
			ServerSocket sock = new ServerSocket(80);
			while(true){
				Socket s = sock.accept();
				Connection c = new Connection(s,manager);
				Thread t = new Thread(c);
				t.start();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static synchronized void increaseConnetion() {
		noOfConnection++;
	}
	public static synchronized void decreaseConnection() {
		noOfConnection--;
	}
	
	public static void printConnection() {
		System.out.println("No of Connections: "+ noOfConnection);
	}
}