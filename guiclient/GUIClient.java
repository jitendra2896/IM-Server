package guiclient;
import client.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class GUIClient extends Client{
	LogIn login;
	SignUp signup;
	private String ip;
	int port;
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
		login.addActionListener(new LogInActionListener());
	}

	class LogInActionListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			String command = ae.getActionCommand();
			if(command.equals("Log In")){
				String username = login.getUsernameText();
				String password = login.getPasswordText();
				if(logIn(username,password))
					login.test.setText("Login Successfull");
				else
					login.test.setText("Login failed");
			}
			else{
				signup = new SignUp();
				signup.addActionListener(new SignUpActionListener());
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
}