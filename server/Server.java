package server;

import java.net.*;
class Server{
	
	public static void main(String args[]){
		
		Saver save = new Saver();
		Authentication auth = new Authentication();
		try{
			ServerSocket sock = new ServerSocket(4000);
			while(true){
				Socket s = sock.accept();
				Connection c = new Connection(s,auth,save);
				Thread t = new Thread(c);
				t.start();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}