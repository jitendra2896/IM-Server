package client;

import protocol.*;
import java.io.*;
import java.net.*;
import java.util.*;
public class Client {

	protected static DataInputStream din;
	protected static DataOutputStream dout;
	protected String ip;
	protected int port;
	private Socket sock;
	private boolean sessionStarted;
	
	public Client(String ip,int port)throws UnknownHostException,IOException{
		this.ip = ip;
		this.port = port;
		sessionStarted = false;
		sock = new Socket(ip,port);
		din = new DataInputStream(sock.getInputStream());
		dout = new DataOutputStream(sock.getOutputStream());
	}

	/*protected final boolean logIn(String userName,String password){
		String message = "";

		try{
			sendMessage(Protocols.LOG_IN_REQUEST+":"+userName+":"+password);
			message = recieveMessage();
			System.out.println(message);
			if(message.equals(Protocols.USER_SUCCESSFULLY_LOGGED_IN)){
				sessionStarted = true;
				System.out.println("User logged in successfully");
				return sessionStarted;
			}
		}catch(IOException e){
			e.printStackTrace();
			sessionStarted = false;
		}
		return sessionStarted;
	}
	*/
	/*protected boolean signUp(String username,String password){
		try{
			sendMessage(Protocols.SIGN_UP_REQUEST+":"+username+":"+password);
			String response = recieveMessage();
			if(response.equals(Protocols.USER_ALREADY_EXISTS))
				return false;
			else if(response.equals(Protocols.SIGN_UP_SUCCESSFUL))
				return true;
			return false;
		}catch(IOException e){
			e.printStackTrace();
			sessionStarted = false;
		}
		return false;
	}*/

	protected boolean sendMessage(String message)throws IOException{
		synchronized(dout){
			System.out.println("Inside send Message");
			dout.writeUTF(message);
			dout.flush();
			return true;
		}
	}

	protected String recieveMessage()throws IOException{
		synchronized(din){
			String s = din.readUTF();
			System.out.println("Message from server: "+s);
			return s;
		}
	}

	protected final DataInputStream getInputStream(){
		if(sessionStarted)
			return din;
		return null;
	}

	protected final DataOutputStream getOutputStream(){
		if(sessionStarted)
			return dout;
		return null;
	}
}