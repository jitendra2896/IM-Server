package server;

import java.io.*;
import java.net.*;
import java.util.*;
import protocol.*;

class Connection implements Runnable{

	private Socket sock;
	private DataInputStream din;
	private DataOutputStream dout;
	private String data[];
	private boolean sessionStarted = false;
	private String userName;
	private String msgin;

	Connection(Socket sock)throws Exception{
		this.sock = sock;
		din = new DataInputStream(sock.getInputStream());
		dout = new DataOutputStream(sock.getOutputStream());
		msgin = "";
		userName = "";
	}

	public void run(){
		try{
			while(!msgin.equals("exit")){
				msgin = readData();
				parseData(msgin);
				takeAction();
				System.out.println("Message Received: "+msgin);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void sendData(String data)throws Exception{
		dout.writeUTF(data);
		flush();
	}
	public String readData()throws Exception{
		return din.readUTF();
	}

	public void flush()throws Exception{
		dout.flush();
	}

	//Send new message to the user
	public void sendMessage(){
		try{
			String msg = Saver.getMessages(userName);
			sendData(msg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void parseData(String s){
		data = s.split(":");
	}


	//Take necessary action based on client request
	private void takeAction() throws Exception{

		if(data[0].equals(Protocols.LOG_IN_REQUEST)){
			if(Authentication.authenticate(data[1],data[2])){
				sessionStarted = true;
				userName = data[1];
				sendData(Protocols.USER_SUCCESSFULLY_LOGGED_IN);
			}
			else
				sendData(Protocols.LOG_IN_UNSUCCESSFUL);
		}

		else if(data[0].equals(Protocols.SIGN_UP_REQUEST)){
			if(Authentication.registerUser(data[1],data[2])){
				sendData(Protocols.SIGN_UP_SUCCESSFUL);
			}
			else
				sendData(Protocols.USER_ALREADY_EXISTS);
		}

		else if(data[0].equals(Protocols.GET_NEW_MESSAGES) && sessionStarted){
			File file = new File(userName+".txt");
			boolean empty = !file.exists() || file.length() == 0;
			if(!empty){
				sendMessage();
				file.delete();
			}
			else
				sendData(Protocols.NO_NEW_MESSAGES);
		}

		else{ //new message
			parseData(msgin);
			if(sessionStarted && Authentication.isUser(data[0])){
				Saver.saveToFile(data[0],userName+":"+data[1]);
			}	
			else
				sendData(Protocols.USER_DOESNT_EXIST);
		}
	}
}