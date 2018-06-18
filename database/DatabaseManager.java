package database;

import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
		
		//check if tables exist if not create them
		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(DATABASE_NAME, null, ChatDatabaseContract.User.TABLE_NAME, null);
			if(!rs.next() || !rs.getString(3).equals(ChatDatabaseContract.User.TABLE_NAME)) {
				createUserTable();
			}
			rs = md.getTables(null, null, ChatDatabaseContract.Message.TABLE_NAME, null);
			if(!rs.next() || !rs.getString(3).equals(ChatDatabaseContract.Message.TABLE_NAME)) {
				createMessageTable();
			}
			rs = md.getTables(null, null, ChatDatabaseContract.MessageRecepient.TABLE_NAME, null);
			if(!rs.next() || !rs.getString(3).equals(ChatDatabaseContract.MessageRecepient.TABLE_NAME)) {
				createMessageRecepientTable();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getAllUsernames(){
		String query = "select "+ChatDatabaseContract.User.COLUMN_USERNAME+" from "+ChatDatabaseContract.User.TABLE_NAME;
		StringBuilder builder = new StringBuilder();
		try {
			ResultSet rs = stat.executeQuery(query);
			while(rs.next()) {
				builder.append(rs.getString(1)+":");
			}
			String result = builder.toString();
			return result.substring(0,result.length()-1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	@Override
	public String getFriendList(String username) {
		return null;
	}

	@Override
	public boolean authenticate(String username, String password) {
		//TODO: This query is vulnerable to SQL injection. Fix it
		String query = "select * from "+ChatDatabaseContract.User.TABLE_NAME+" where "+ChatDatabaseContract.User.COLUMN_USERNAME+
				" = '" + username+ "' AND "+ChatDatabaseContract.User.COLUMN_PASSWORD + " = '"+ password+"';";
		System.out.println(query);
		
		try {
			ResultSet rs = stat.executeQuery(query);
			if(rs.next()) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isUser(String username) {
		//TODO: This query is vulnerable to SQL injection. Fix it
			String query = "select * from "+ChatDatabaseContract.User.TABLE_NAME+" where "+ChatDatabaseContract.User.COLUMN_USERNAME+
					" = '" + username+ "'";
			System.out.println(query);
				
			try {
				ResultSet rs = stat.executeQuery(query);
				if(rs.next()) {
					return true;
				}
				return false;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
	}

	@Override
	public synchronized boolean registerUser(String username, String password) {
		String query = "INSERT INTO "+ChatDatabaseContract.User.TABLE_NAME+"("+ChatDatabaseContract.User.COLUMN_USERNAME+","+
						ChatDatabaseContract.User.COLUMN_PASSWORD+") values('"+username+"', '"+password+"');";
		System.out.println(query);
		if(!isUser(username)) {
			try {
				int count = stat.executeUpdate(query);
				if(count > 0)
					return true;
				return false;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isNewMessageAvailable(String username) {
		//query to get id given the username
		String query1 = "SELECT "+ChatDatabaseContract.User.COLUMN_ID+" from "+ChatDatabaseContract.User.TABLE_NAME+
				" WHERE "+ChatDatabaseContract.User.COLUMN_USERNAME+" = '"+username+"';";
		System.out.println(query1);
		long userId = -1;
		try {
			ResultSet rs = stat.executeQuery(query1);
			if(rs.next()) {
				userId = rs.getLong(1);
				String query2 = "SELECT * FROM "+ChatDatabaseContract.MessageRecepient.TABLE_NAME+" WHERE "+
								ChatDatabaseContract.MessageRecepient.COLUMN_RECEPIENT_ID+" = "+userId;
				System.out.println(query2);
				rs = stat.executeQuery(query2);
				if(rs.next())
					return true;
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getNewMessage(String username) {
		//query to get id given the username
		if(isNewMessageAvailable(username)) {
			String query1 = "SELECT "+ChatDatabaseContract.User.COLUMN_ID+" FROM "+ChatDatabaseContract.User.TABLE_NAME+
							" WHERE "+ChatDatabaseContract.User.COLUMN_USERNAME+" = '"+username+"';";
			
			try {
				ResultSet rs = stat.executeQuery(query1);
				rs.next();
				long userId = rs.getLong(1);
				String query2 = "SELECT "+ChatDatabaseContract.Message.COLUMN_MESSAGE+" FROM "+ChatDatabaseContract.Message.TABLE_NAME+
								" INNER JOIN "+ChatDatabaseContract.MessageRecepient.TABLE_NAME+" ON "+
								ChatDatabaseContract.Message.TABLE_NAME+"."+ChatDatabaseContract.Message.COLUMN_MESSAGE_ID +" = "+
								ChatDatabaseContract.MessageRecepient.TABLE_NAME+"."+ChatDatabaseContract.MessageRecepient.COLUMN_MESSAGE_ID+
								" WHERE "+ChatDatabaseContract.MessageRecepient.COLUMN_RECEPIENT_ID+" = "+userId+
								" AND "+ChatDatabaseContract.MessageRecepient.COLUMN_DELIVERED+" = b'0';";
				
				System.out.println(query2);
				rs = stat.executeQuery(query2);
				StringBuilder builder = new StringBuilder();
				while(rs.next()) {
					builder.append(rs.getString(1)+":");
				}
				//update message delivered in message recepient to true i.e.(1)
				String query3 = "UPDATE "+ChatDatabaseContract.MessageRecepient.TABLE_NAME+" SET "+
								ChatDatabaseContract.MessageRecepient.COLUMN_DELIVERED+" = "+"b'1';";
				int count = stat.executeUpdate(query3);
				if(count <= 0)
					System.out.println("ERROR: "+query3);
				return builder.toString();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		return null;
	}

	@Override
	public synchronized void storeMessage(String senderUsername,String recepientUsername, String msg){
		String query1 = "SELECT "+ChatDatabaseContract.User.COLUMN_ID+" FROM "+ChatDatabaseContract.User.TABLE_NAME+
				" WHERE "+ChatDatabaseContract.User.COLUMN_USERNAME+" = '"+senderUsername+"';";
		
		String query2 = "SELECT "+ChatDatabaseContract.User.COLUMN_ID+" FROM "+ChatDatabaseContract.User.TABLE_NAME+
				" WHERE "+ChatDatabaseContract.User.COLUMN_USERNAME+" = '"+recepientUsername+"';";
		
		try {
			ResultSet rs = stat.executeQuery(query1);
			rs.next();
			long senderId = rs.getInt(1);
			rs = stat.executeQuery(query2);
			rs.next();
			long receiverId = rs.getInt(1);
			String query3 = "INSERT INTO "+ChatDatabaseContract.Message.TABLE_NAME+"("+
							ChatDatabaseContract.Message.COLUMN_CREATOR_ID+", "+ChatDatabaseContract.Message.COLUMN_MESSAGE+
							", "+ChatDatabaseContract.Message.COLUMN_DATE+") VALUES("+
							senderId+", '"+msg+"', '2018-06-11 12:00:00');";
			int count = stat.executeUpdate(query3);
			if(count > 0){
				String query4 = "INSERT INTO "+ChatDatabaseContract.MessageRecepient.TABLE_NAME+" VALUES(LAST_INSERT_ID(), "+
								receiverId+",b'0');";
				count = stat.executeUpdate(query4);
				if(count <=0 )
					System.out.println("ERROR: "+query4);
			}
			else
				System.out.print("ERROR: "+query3);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/*public static void main(String args[]) {
		DatabaseManager manager = new DatabaseManager();
		manager.registerUser("jitu", "jitu");
		manager.registerUser("how", "how");
		manager.registerUser("hello", "hello");
		manager.storeMessage("jitu", "how", "hello how are you");
		System.out.println(manager.getNewMessage("how"));
		manager.storeMessage("jitu", "how", "This is me");
		System.out.println(manager.getNewMessage("how"));
	}*/


	private void createMessageRecepientTable() throws SQLException {
		String queryMessageRecepientTable = "CREATE TABLE "+ChatDatabaseContract.MessageRecepient.TABLE_NAME+"("+
				ChatDatabaseContract.MessageRecepient.COLUMN_MESSAGE_ID+" BIGINT NOT NULL,"+
				ChatDatabaseContract.MessageRecepient.COLUMN_RECEPIENT_ID+" BIGINT NOT NULL,"+
				ChatDatabaseContract.MessageRecepient.COLUMN_DELIVERED+" BIT(1), "+
				"FOREIGN KEY("+ChatDatabaseContract.MessageRecepient.COLUMN_MESSAGE_ID+")"+
				"REFERENCES "+ChatDatabaseContract.Message.TABLE_NAME+"("+ChatDatabaseContract.Message.COLUMN_MESSAGE_ID+"),"+
				"FOREIGN KEY("+ChatDatabaseContract.MessageRecepient.COLUMN_RECEPIENT_ID+")"+
				"REFERENCES "+ChatDatabaseContract.User.TABLE_NAME+"("+ChatDatabaseContract.User.COLUMN_ID+")"+
				"ON DELETE CASCADE ON UPDATE CASCADE);";
		stat.executeUpdate(queryMessageRecepientTable);
		System.out.println("Created "+ChatDatabaseContract.MessageRecepient.TABLE_NAME+" successfully");
	}

	private void createMessageTable() throws SQLException {
		String queryMessageTable = "CREATE TABLE "+ChatDatabaseContract.Message.TABLE_NAME+"("+
				ChatDatabaseContract.Message.COLUMN_MESSAGE_ID+" BIGINT NOT NULL AUTO_INCREMENT,"+
				ChatDatabaseContract.Message.COLUMN_CREATOR_ID+" BIGINT NOT NULL,"+
				ChatDatabaseContract.Message.COLUMN_MESSAGE+" LONGTEXT,"+
				ChatDatabaseContract.Message.COLUMN_DATE+" DATETIME,"+
						"PRIMARY KEY("+ChatDatabaseContract.Message.COLUMN_MESSAGE_ID+"),"+
						"FOREIGN KEY("+ChatDatabaseContract.Message.COLUMN_CREATOR_ID+")"+
						"REFERENCES "+ChatDatabaseContract.User.TABLE_NAME +"("+ChatDatabaseContract.User.COLUMN_ID+") "+
						"ON UPDATE CASCADE ON DELETE CASCADE);";

		stat.executeUpdate(queryMessageTable);
		System.out.println("Created "+ChatDatabaseContract.Message.TABLE_NAME+" successfully");
	}

	private void createUserTable() throws SQLException {
		String queryUserTable = "CREATE TABLE "+ChatDatabaseContract.User.TABLE_NAME+"("+
				ChatDatabaseContract.User.COLUMN_ID+" BIGINT NOT NULL AUTO_INCREMENT,"+
				ChatDatabaseContract.User.COLUMN_USERNAME+" VARCHAR(10),"+
				ChatDatabaseContract.User.COLUMN_PASSWORD+" VARCHAR(30),"+
				ChatDatabaseContract.User.COLUMN_FIRST_NAME+" VARCHAR(20),"+
				ChatDatabaseContract.User.COLUMN_LAST_NAME+" VARCHAR(20),"+
				ChatDatabaseContract.User.COLUMN_IS_ACTIVE+" BIT(1),"
						+ "PRIMARY KEY("+ChatDatabaseContract.User.COLUMN_ID+"));";
		
		stat.executeUpdate(queryUserTable);
		System.out.println("Created "+ChatDatabaseContract.User.TABLE_NAME+" successfully");
	}
}