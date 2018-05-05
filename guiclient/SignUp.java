package guiclient;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
class SignUp extends JFrame{
	private JButton buttonCreateAccount;
	private JButton buttonLogIn;
	private JLabel labelUsername;
	private JLabel labelPassword;
	private JTextField textFieldUsername;
	private JPasswordField textFieldPassword;
	private JPasswordField textFieldRetypePassword;
	public JLabel infoLabel;
	JLabel lblRetypePassword;
	
	SignUp(){
		super("Sign Up");
		setResizable(false);
		getContentPane().setBackground(new Color(135, 206, 235));
		setBounds(100, 100, 508, 621);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		buttonCreateAccount = new JButton("Create Account");
		buttonLogIn = new JButton("Log In");
		labelUsername = new JLabel("Username");
		labelPassword = new JLabel("Password");
		textFieldUsername = new JTextField();
		textFieldPassword = new JPasswordField();
		infoLabel = new JLabel();
		lblRetypePassword = new JLabel("Re-type Password");
		textFieldRetypePassword = new JPasswordField();
		
		labelUsername.setFont(new Font("Tahoma", Font.PLAIN, 15));
		labelUsername.setBounds(114, 96, 82, 16);
		getContentPane().add(labelUsername);
		
		textFieldUsername = new JTextField();
		textFieldUsername.setFont(new Font("Verdana", Font.BOLD, 19));
		textFieldUsername.setBounds(114, 118, 238, 30);
		getContentPane().add(textFieldUsername);
		textFieldUsername.setColumns(10);
		
		textFieldPassword.setBounds(114, 180, 238, 30);
		getContentPane().add(textFieldPassword);
		
		labelPassword.setFont(new Font("Tahoma", Font.PLAIN, 15));
		labelPassword.setBounds(114, 160, 82, 16);
		getContentPane().add(labelPassword);
		
		buttonLogIn.setFont(new Font("Verdana", Font.PLAIN, 14));
		buttonLogIn.setForeground(new Color(255, 255, 255));
		buttonLogIn.setBackground(new Color(70, 130, 180));
		buttonLogIn.setBounds(191, 402, 97, 38);
		getContentPane().add(buttonLogIn);
		
		buttonCreateAccount.setFont(new Font("Verdana", Font.PLAIN, 14));
		buttonCreateAccount.setForeground(new Color(255, 255, 255));
		buttonCreateAccount.setBackground(new Color(70, 130, 180));
		buttonCreateAccount.setBounds(114, 330, 235, 38);
		getContentPane().add(buttonCreateAccount);
		

		infoLabel.setForeground(Color.RED);
		infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		infoLabel.setFont(new Font("Verdana",Font.BOLD,15));
		infoLabel.setBounds(74, 471, 358, 46);
		getContentPane().add(infoLabel);
		
		lblRetypePassword.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblRetypePassword.setBounds(114, 225, 128, 16);
		getContentPane().add(lblRetypePassword);
		
		textFieldRetypePassword.setBounds(114, 245, 238, 30);
		getContentPane().add(textFieldRetypePassword);
	}

	public void addActionListener(ActionListener ae){
		buttonLogIn.addActionListener(ae);
		buttonCreateAccount.addActionListener(ae);
	}

	public String getUsernameText(){
		return textFieldUsername.getText();
	}

	public String getPasswordText(){
		return new String(textFieldPassword.getPassword());
	}
	
	public String getRetypePasswordText(){
		return new String(textFieldRetypePassword.getPassword());
	}
	
	public boolean validatePassword(){
		char[] pass1 = textFieldPassword.getPassword();
		char[] pass2 = textFieldRetypePassword.getPassword();
		if(pass1.length > 0 && pass2.length > 0 && new String(pass1).equals(new String(pass2)))
			return true;
		return false;
	}
	
	public void setInfoLabel(String s){
		infoLabel.setText(s);
	}
	
	public void showWindow(boolean visibility){
		setVisible(visibility);
	}
	
	public void setInfoLabelColor(Color c){
		infoLabel.setForeground(c);
	}
	
	public void addFocusListener(FocusListener fl){
		textFieldUsername.addFocusListener(fl);
		textFieldPassword.addFocusListener(fl);
		textFieldRetypePassword.addFocusListener(fl);
	}
}