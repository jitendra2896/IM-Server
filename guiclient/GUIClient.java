package guiclient;
import client.*;
import protocol.Protocols;

import javax.swing.*;

import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.awt.*;

class GUIClient extends Client{
	LogIn login;
	SignUp signup;
	MessageWindow mWindow;
	
	private String ip;
	int port;
	String userNames = "";
	
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
		LogInActionListener fl = new LogInActionListener();
		login.addActionListener(fl);
		login.addFocusListener(fl);
		Thread t = new Thread(new ReaderThread());
		t.start();
		System.out.println("Hey man");
	}

	class LogInActionListener implements ActionListener,FocusListener{
		public void actionPerformed(ActionEvent ae){
			String command = ae.getActionCommand();
			execute(command);
		}
		
		private void execute(String command){
			if(command.equals("Log In")){
				String username = login.getUsernameText();
				String password = login.getPasswordText();
				if(username.length() <=0 || password.length() <= 0){
					login.setInfoLabelColor(Color.RED);
					if(username.length() <=0 && password.length() > 0){
						login.setInfoLabel("Username required");
					}
					else if(password.length() <= 0 && username.length() > 0){
						login.setInfoLabel("Password required");
					}
					else{
						login.setInfoLabel("Username and Password required");
					}
				}
				else{
					new SwingWorker<Void,Void>(){
						@Override
						public Void doInBackground(){
							try {
								System.out.println("Inside this thread");
								sendMessage(Protocols.LOG_IN_REQUEST+":"+username+":"+password);
								userNames = username;
								System.out.println("Method finished");
							} catch (IOException e) {
								e.printStackTrace();
							}
							return null;
						}
					}.execute();
				}
			}
			else{
				signup = new SignUp();
				login.setVisible(false);
				SignUpActionListener fl = new SignUpActionListener();
				signup.addActionListener(fl);
				signup.addFocusListener(fl);
				signup.showWindow(true);
			}
		}

		@Override
		public void focusGained(FocusEvent fe) {
			Component c = fe.getComponent();
			if(c == login.getUsernameTextField()){
				login.setUsernameTextFieldText("");
			}
			else{
				login.setPasswordTextFieldText("");
			}
			login.setUsernameTextFieldForegroundColor(Color.BLACK);
			login.setInfoLabel("");
			login.setPasswordTextFieldForegroundColor(Color.BLACK);
		}
		
		@Override
		public void focusLost(FocusEvent arg0) {}
	}

	class SignUpActionListener implements ActionListener,FocusListener{
		public void actionPerformed(ActionEvent ae){
			String command = ae.getActionCommand();
			if(command.equals("Create Account")){
				String username = signup.getUsernameText();
				String password = signup.getPasswordText();
				if(signup.validatePassword()){
					new SwingWorker<Void,Void>(){
						public Void doInBackground(){
							try {
								sendMessage(Protocols.SIGN_UP_REQUEST+":"+username+":"+password);
							} catch (IOException e) {
								e.printStackTrace();
							}
							return null;
						}
					}.execute();
				}
				else{
					int pass1length = signup.getPasswordText().length();
					int pass2length = signup.getRetypePasswordText().length();
					if(signup.getUsernameText().length() <= 0){
						signup.setInfoLabel("Username Required");
					}
					else if(pass1length <= 0){
						signup.setInfoLabel("Password required!");
					}
					else if(pass1length >= 0 && pass2length <= 0){
						signup.setInfoLabel("Re-type password field required");
					}
					else{
						signup.setInfoLabel("Passwords do not match");
					}
				}
			}
			else{
				signup.dispose();
				login.setVisible(true);
			}
		}

		@Override
		public void focusGained(FocusEvent arg0) {
			signup.setInfoLabel("");
		}

		@Override
		public void focusLost(FocusEvent arg0) {}
	}
	
	class SendActionListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			String msg = mWindow.getMessage();
			if(msg != null){
				String username = mWindow.getUsername();
				System.out.println("How come "+username);
				new SwingWorker<Void,Void>(){
					public Void doInBackground(){
						try {
							sendMessage(username+":"+msg);
						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}
				}.execute();
			}
		}
	}
	
	class ReaderThread implements Runnable{
		public void run(){
			while(true){
				try {
					System.out.println("Working");
					String msg = recieveMessage();
					takeAction(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void takeAction(String msg){
			String[] data = parseMessage(msg);
			if(data[0].equals(Protocols.USER_SUCCESSFULLY_LOGGED_IN)){
				try {
					SwingUtilities.invokeAndWait(new Runnable(){

						public void run() {
							login.dispose();
							mWindow.showWindow(true);
							mWindow.setTitle(userNames);
						}
						
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else if(data[0].equals(Protocols.LOG_IN_UNSUCCESSFUL)){
				try {
					SwingUtilities.invokeAndWait(new Runnable(){

						public void run() {
							login.setInfoLabel("username or password is incorrect. Try Again!");
							login.setUsernameTextFieldForegroundColor(Color.RED);
							login.setPasswordTextFieldForegroundColor(Color.RED);
							login.setInfoLabelColor(Color.RED);
						}
						
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else if(data[0].equals(Protocols.USERNAME_STRINGS)){
				String username[] = new String[data.length-1];
				for(int i = 0;i<username.length;i++){
					username[i] = data[i+1];
				}
				try {
					SwingUtilities.invokeAndWait(new Runnable(){

						public void run() {
							mWindow.setListData(username);
						}
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else if(data[0].equals(Protocols.SIGN_UP_SUCCESSFUL)){
				try {
					SwingUtilities.invokeAndWait(new Runnable(){
						public void run(){
							signup.setInfoLabel("Account Created successfully!");
							signup.setInfoLabelColor(Color.WHITE);
						}
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else if(data[0].equals(Protocols.USER_ALREADY_EXISTS)){
				try {
					SwingUtilities.invokeAndWait(new Runnable(){
						public void run(){
							signup.setInfoLabel("Username already taken. Try a different one!");
						}
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else if(data[0].equals(userNames)){
				
				System.out.println("Actual Message");
				for(int i = 0;i<data.length;i++){
					System.out.println(data[i]);
				}
				
				try {
					SwingUtilities.invokeAndWait(new Runnable(){

						public void run() {
							StringBuilder build = new StringBuilder();
							for(int i = 1;i<data.length;i++){
								if(i %2 != 0)
									build.append("<"+data[i]+"> ");
								else
									build.append(data[i]+"\n");
							}
							System.out.println("Builded Message: "+build.toString());
							mWindow.appendTextArea(build.toString());
						}
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private String[] parseMessage(String msg){
			return msg.split(":");
		}
	}
}