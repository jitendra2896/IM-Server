package server;
import java.io.*;
import java.net.*;
import global.DataManager;
import protocol.*;

class Connection implements Runnable{

	private Socket sock;
	private DataInputStream din;
	private DataOutputStream dout;
	private String data[];
	private boolean sessionStarted = false;
	private String username;
	private String msgin;
	private boolean isConnected;
	private DataManager dm;
	
	Connection(Socket sock,DataManager dm)throws Exception{
		this.sock = sock;
		din = new DataInputStream(sock.getInputStream());
		dout = new DataOutputStream(sock.getOutputStream());
		msgin = "";
		username = "";
		isConnected = true;
		this.dm = dm;
	}

	public void run(){
		try{
			System.out.println("Thread Started");
			Server.increaseConnetion();
			Server.printConnection();
			while(isConnected){
				msgin = readData();
				System.out.println("Messsage Received: "+msgin);
				parseData(msgin);
				takeAction();
			}
		}catch(Exception e){
			isConnected = false;
			e.printStackTrace();
		}
		Server.decreaseConnection();
		Server.printConnection();
		System.out.println("Exiting the thread");
	}

	public void sendData(String data)throws Exception{
		dout.writeUTF(data);
		System.out.println("SERVER SENDING DATA: "+data);
		flush();
	}
	public String readData()throws Exception{
		return din.readUTF();
	}

	public void flush()throws Exception{
		dout.flush();
	}

	//Send new message to the user
	public void sendMessage()throws Exception{
		String msg = dm.getNewMessage(username);
		sendData(username+":"+msg);
	}

	private void parseData(String s){
		data = s.split(":");
	}


	//Take necessary action based on client request
	private void takeAction() throws Exception{
		System.out.println("data[0]: "+data[0]);
		if(data[0].equals(Protocols.LOG_IN_REQUEST)){
			if(dm.authenticate(data[1],data[2])){
				sessionStarted = true;
				username = data[1];
				sendData(Protocols.USER_SUCCESSFULLY_LOGGED_IN);
				sendUsernames();
				new Thread(new ReaderThread()).start();
			}
			else
				sendData(Protocols.LOG_IN_UNSUCCESSFUL);
		}

		else if(data[0].equals(Protocols.SIGN_UP_REQUEST)){
			if(dm.registerUser(data[1],data[2])){
				sendData(Protocols.SIGN_UP_SUCCESSFUL);
				sendUsernames();
			}
			else
				sendData(Protocols.USER_ALREADY_EXISTS);
		}
		
		else if(sessionStarted && data[0].equals(Protocols.GET_ALL_USERNAMES)) {
			sendUsernames();
		}

		else{ //new message
			if(sessionStarted && dm.isUser(data[0])){
				dm.storeMessage(username,data[0],data[1]);
			}
		}
	}
	
	private void sendUsernames() throws Exception{
		sendData(Protocols.USERNAME_STRINGS+":"+dm.getAllUsernames());
	}
	
	public String getUsername(){
		return username;
	}
	
	public synchronized void newMessages(){
		if(sessionStarted){
			File file = new File("server/"+username+".txt");
			boolean empty = !file.exists() || file.length() == 0;
			if(!empty){
				System.out.println("New messages available");
				try {
					sendMessage();
					file.delete();
					System.out.println("Message file deleted");
				} catch (Exception e) {
					isConnected = false;
					e.printStackTrace();
				}
			}
		}
	}
	
	public class ReaderThread implements Runnable{

		@Override
		public void run() {
			while(isConnected){
				newMessages();
			}
		}
		
	}
}