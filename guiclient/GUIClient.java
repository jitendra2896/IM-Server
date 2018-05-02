package guiclient;
import client.*;
import protocol.Protocols;

import javax.swing.*;

import com.sun.java.swing.plaf.windows.resources.windows;

import java.awt.event.*;
import java.io.IOException;
import java.awt.*;

class GUIClient extends Client{
	LogIn login;
	SignUp signup;
	MessageWindow mWindow;
	
	private String ip;
	int port;
	String usernames = "";
	
	public GUIClient(String ip,int port)throws Exception{
			super(ip,port);
	}

	public static void main(String args[]){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				try{
					new GUIClient("192.168.225.160",4000).operate();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}

	public void operate(){
		login = new LogIn();
		login.showWindow(true);
		signup = new SignUp();
		mWindow = new MessageWindow();
		mWindow.addActionListener(new SendActionListener());
		login.addActionListener(new LogInActionListener());
	}

	class LogInActionListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			String command = ae.getActionCommand();
			if(command.equals("Log In")){
				String username = login.getUsernameText();
				String password = login.getPasswordText();
				if(logIn(username,password)){
					login.dispose();
					mWindow.showWindow(true);
					
					//Create a new thread that checks for new messages 
					new Thread(new MessageRecieverThread()).start();
				}
					
				else
					login.test.setText("Login failed");
			}
			else{
				signup = new SignUp();
				signup.addActionListener(new SignUpActionListener());
				signup.showWindow(true);
			}
		}
	}

	class SignUpActionListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			String command = ae.getActionCommand();
			if(command.equals("Create Account")){
				String username = signup.getUsernameText();
				String password = signup.getPasswordText();
				if(signUp(username,password))
					signup.test.setText("Account Created successfully");
				else
					signup.test.setText("Username already taken");
			}
		}
	}
	
	class SendActionListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			String msg = mWindow.getMessage();
			if(msg != null){
				String username = mWindow.getUsername();
				System.out.println("How come "+username);
				try {
					sendMessage(username+":"+msg);
					System.out.println(recieveMessage());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	class MessageRecieverThread implements Runnable{
		public void run(){
			while(mWindow.isWindowVisible()){
				try {
					sendMessage(Protocols.GET_NEW_MESSAGES);
					String msg = recieveMessage();
					
					if(!msg.equals(Protocols.NO_NEW_MESSAGES))
						mWindow.appendTextArea(msg);
					
					sendMessage(Protocols.GET_ALL_USERNAMES);
					usernames = recieveMessage();
					SwingUtilities.invokeAndWait(new UpdateUsernameList());
					Thread.sleep(3000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class UpdateUsernameList implements Runnable{
		
		public void run() {
			mWindow.setListData(usernames.split(":"));
		}
		
	}
}