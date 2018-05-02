package guiclient;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
class LogIn extends JFrame{
	private JButton buttonLogIn;
	private JButton buttonSignUp;
	private JLabel labelUsername;
	private JLabel labelPassword;
	private JTextField textFieldUsername;
	private JTextField textFieldPassword;
	public JLabel test;
	LogIn(){
		super("Log In");
		setSize(500,500);
		buttonLogIn = new JButton("Log In");
		buttonSignUp = new JButton("Sign Up");
		labelUsername = new JLabel("Username");
		labelPassword = new JLabel("Password");
		textFieldUsername = new JTextField("Enter username");
		textFieldPassword = new JTextField("Enter Password");

		test = new JLabel("Press a button");
		setLayout(new FlowLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(labelUsername);
		add(textFieldUsername);
		add(labelPassword);
		add(textFieldPassword);
		add(buttonLogIn);
		add(buttonSignUp);
		add(test);
	}

	public void addActionListener(ActionListener ae){
		buttonLogIn.addActionListener(ae);
		buttonSignUp.addActionListener(ae);
	}

	public String getUsernameText(){
		return textFieldUsername.getText();
	}

	public String getPasswordText(){
		return textFieldPassword.getText();
	}
	
	public void showWindow(boolean visibility){
		setVisible(visibility);
	}
}