package server;

import java.net.*;
class Server{

	public static void main(String args[]){
		try{
			ServerSocket sock = new ServerSocket(4000);
			while(true){
				Socket s = sock.accept();
				Thread t = new Thread(new Connection(s));
				t.start();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}