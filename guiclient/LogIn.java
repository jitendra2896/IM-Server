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
	private JPasswordField textFieldPassword;
	public JLabel infoLabel;
	
	LogIn(){
		super("Log In");
		setResizable(false);
		getContentPane().setBackground(new Color(135, 206, 235));
		setBounds(100, 100, 450, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		buttonLogIn = new JButton("Log In");
		buttonSignUp = new JButton("Sign Up");
		labelUsername = new JLabel("Username");
		labelPassword = new JLabel("Password");
		textFieldUsername = new JTextField();
		textFieldPassword = new JPasswordField();
		infoLabel = new JLabel();
		
		textFieldUsername.setFont(new Font("Verdana", Font.BOLD, 19));
		textFieldUsername.setBounds(102, 58, 235, 30);
		getContentPane().add(textFieldUsername);
		textFieldUsername.setColumns(10);
		
		labelUsername.setFont(new Font("Tahoma", Font.PLAIN, 15));
		labelUsername.setBounds(102, 36, 82, 16);
		getContentPane().add(labelUsername);
		
		textFieldPassword.setBounds(99, 120, 238, 30);
		getContentPane().add(textFieldPassword);

		labelPassword.setFont(new Font("Tahoma", Font.PLAIN, 15));
		labelPassword.setBounds(99, 100, 82, 16);
		getContentPane().add(labelPassword);

		buttonLogIn.setFont(new Font("Verdana", Font.PLAIN, 14));
		buttonLogIn.setForeground(new Color(255, 255, 255));
		buttonLogIn.setBackground(new Color(70, 130, 180));
		buttonLogIn.setBounds(99, 163, 97, 38);
		getContentPane().add(buttonLogIn);

		buttonSignUp.setFont(new Font("Verdana", Font.PLAIN, 14));
		buttonSignUp.setForeground(new Color(255, 255, 255));
		buttonSignUp.setBackground(new Color(70, 130, 180));
		buttonSignUp.setBounds(240, 163, 97, 38);
		getContentPane().add(buttonSignUp);

		infoLabel.setBounds(52, 214, 358, 16);
		infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(infoLabel);
	}

	public void addActionListener(ActionListener ae){
		buttonLogIn.addActionListener(ae);
		buttonSignUp.addActionListener(ae);
	}

	public String getUsernameText(){
		return textFieldUsername.getText();
	}

	public String getPasswordText(){
		return new String(textFieldPassword.getPassword());
	}
	
	public void showWindow(boolean visibility){
		setVisible(visibility);
	}
	
	public void setInfoLabel(String info){
		infoLabel.setText(info);
	}
	
	//sets color for both the text fields
	public void setUsernameTextFieldForegroundColor(Color c){
		textFieldUsername.setForeground(c);
	}
	
	public void setPasswordTextFieldForegroundColor(Color c){
		textFieldPassword.setForeground(c);
	}
	
	public void setUsernameTextFieldText(String s){
		textFieldUsername.setText(s);
	}
	
	public void setPasswordTextFieldText(String s){
		textFieldPassword.setText(s);
	}
	
	public void addFocusListener(FocusListener fl){
		textFieldUsername.addFocusListener(fl);
		textFieldPassword.addFocusListener(fl);
	}
	
	public JTextField getUsernameTextField(){
		return textFieldUsername;
	}
	
	public JPasswordField getPasswordTextField(){
		return textFieldPassword;
	}
	
	public void setInfoLabelColor(Color c){
		infoLabel.setForeground(c);
	}
}