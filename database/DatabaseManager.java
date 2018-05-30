package database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import global.DataManager;

public class DatabaseManager implements DataManager{
	
	private final int DATABASE_VERSION = 1;
	private final String DATABASE_NAME = "chat_database";
	private final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
	private final String USERNAME = "root";
	private final String PASSWORD = "";
	private final String CONNECTION_URL = "jdbc:mysql://localhost/"+DATABASE_NAME+"?&useSSL=false";
	private Connection conn;
	private Statement stat;
	
	public DatabaseManager() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(CONNECTION_URL,USERNAME,PASSWORD);
			stat = conn.createStatement();
			System.out.println("Connected to database successfully");
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getAllUsernames() {
		return null;
	}

	@Override
	public String getFriendList(String username) {
		return null;
	}

	@Override
	public boolean authenticate(String username, String password) {
		return false;
	}

	@Override
	public boolean isUser(String username) {
		return false;
	}

	@Override
	public boolean registerUser(String username, String password) {
		return false;
	}

	@Override
	public boolean isNewMessagAvailable(String username) {
		return false;
	}

	@Override
	public String getNewMessage(String username) {
		return null;
	}

	@Override
	public void storeMessage(String username, String msg){
		
	}
	
	public static void main(String args[]) {
		new DatabaseManager();
	}
}
