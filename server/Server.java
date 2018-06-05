package server;

import java.net.*;

import filestorage.FileManager;
class Server{
	public static void main(String args[]){
		
		FileManager manager = new FileManager();
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
}