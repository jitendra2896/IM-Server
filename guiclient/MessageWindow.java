package guiclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MessageWindow extends JFrame{
	
	JButton sendButton;
	JTextField messageField;
	JTextArea messageArea;
	JList onlineList;
	private String selectedUsername = "";
	private boolean isVisible;
	
	public MessageWindow(){
		super("Message Window");
		getContentPane().setLayout(null);
		setBounds(100, 100, 1024, 768);
		
		sendButton = new JButton("SEND");
		sendButton.setFont(new Font("Verdana", Font.BOLD, 20));
		sendButton.setBackground(new Color(0, 206, 209));
		sendButton.setBounds(811, 667, 183, 41);
		getContentPane().add(sendButton);
		
		messageField = new JTextField();
		messageField.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 15));
		messageField.setBounds(12, 667, 787, 41);
		messageField.setColumns(10);
		getContentPane().add(messageField);
		
		messageArea = new JTextArea();
		messageArea.setBounds(12, 13, 787, 641);
		getContentPane().add(messageArea);
		
		onlineList = new JList();
		onlineList.setBounds(811, 13, 183, 641);
		onlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		onlineList.setCellRenderer(new SelectedListCellRenderer());
		onlineList.addListSelectionListener(new ListSelectionListener(){
			
			public void valueChanged(ListSelectionEvent ls) {
				selectedUsername = onlineList.getSelectedValue().toString();
			}
			
		});
		getContentPane().add(onlineList);
		
		isVisible = false;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		System.out.println("Message WIndow setup");
	}
	
	public void addActionListener(ActionListener al){
		sendButton.addActionListener(al);
	}
	
	public String getMessage(){
		String msg = messageField.getText();
		if(msg.trim().length() > 0)
			return msg.trim();
		return null;
	}
	
	public void showWindow(boolean visibility){
		setVisible(visibility);
		isVisible = visibility;
	}
	
	public void setTitle(String title){
		setTitle(title);
	}
	
	public String getUsername(){
		return selectedUsername;
	}
	
	public boolean isWindowVisible(){
		return isVisible;
	}
	
	public void setTextArea(String msg){
		messageArea.setText(msg);
	}
	
	public void appendTextArea(String msg){
		messageArea.setText(messageArea.getText()+msg);
	}
	
	public void setListData(String[] data){
		onlineList.setListData(data);
	}
	
	public class SelectedListCellRenderer extends DefaultListCellRenderer {
	     @Override
	     public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	         Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	         if (isSelected) {
	             c.setBackground(Color.RED);
	         }
	         return c;
	     }
	}
}
