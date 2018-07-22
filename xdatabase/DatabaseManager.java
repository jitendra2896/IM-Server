package xdatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.ChatDatabaseContract;
import xdatabase.DatabaseContract;

public class DatabaseManager {
	private final String DATABASE_NAME = "chat_database";
	private final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
	private final String USERNAME = "root";
	private final String PASSWORD = "";
	private final String CONNECTION_URL = "jdbc:mysql://localhost/" + DATABASE_NAME + "?&useSSL=false";
	private Connection conn;
	private Statement stat;

	public DatabaseManager() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);
			stat = conn.createStatement();
			System.out.println("Connected to database successfully");
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// check if tables exist if not create them
		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(DATABASE_NAME, null, DatabaseContract.User.TABLE_NAME, null);
			if (!rs.next() || !rs.getString(3).equals(DatabaseContract.User.TABLE_NAME)) {
				createUserTable();
			}
			rs = md.getTables(null, null, DatabaseContract.Roster.TABLE_NAME, null);
			if (!rs.next() || !rs.getString(3).equals(DatabaseContract.Roster.TABLE_NAME)) {
				createRosterTable();
			}
			rs = md.getTables(null, null, DatabaseContract.Message.TABLE_NAME, null);
			if (!rs.next() || !rs.getString(3).equals(DatabaseContract.Message.TABLE_NAME)) {
				createMessageTable();
			}
			rs = md.getTables(null, null, DatabaseContract.MessageRecepient.TABLE_NAME, null);
			if (!rs.next() || !rs.getString(3).equals(DatabaseContract.MessageRecepient.TABLE_NAME)) {
				createMessageRecepientTable();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean addUser(String username, String password, String firstName, String lastName, String status,
			String statusMessage) {
		String query = "INSERT INTO " + DatabaseContract.User.TABLE_NAME + "(" + DatabaseContract.User.COLUMN_USERNAME
				+ "," + DatabaseContract.User.COLUMN_PASSWORD + "," + DatabaseContract.User.COLUMN_FIRST_NAME + ","
				+ DatabaseContract.User.COLUMN_LAST_NAME + "," + DatabaseContract.User.COLUMN_STATUS + ","
				+ DatabaseContract.User.COLUMN_STATUS_MESSAGE + ")VALUES('" + username + "','" + password + "','"
				+ firstName + "','" + lastName + "','" + status + "','" + statusMessage + "');";

		if (!isUser(username)) {
			try {
				System.out.println(query);
				stat.executeUpdate(query);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		System.out.println("User already exist");
		return false;

	}

	public boolean updateStatus(String username, String status) {
		String query = "UPDATE " + DatabaseContract.User.TABLE_NAME + " SET " + DatabaseContract.User.COLUMN_STATUS
				+ " = '" + status + "' WHERE " + DatabaseContract.User.COLUMN_USERNAME + " = '" + username + "';";
		if (isUser(username)) {
			System.out.println(query);
			try {
				int count = stat.executeUpdate(query);
				if (count <= 0) {
					System.out.println("No row was updated");
					return false;
				}
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public boolean updateStatusMessage(String username, String statusMessage) {
		String query = "UPDATE " + DatabaseContract.User.TABLE_NAME + " SET "
				+ DatabaseContract.User.COLUMN_STATUS_MESSAGE + " = '" + statusMessage + "' WHERE "
				+ DatabaseContract.User.COLUMN_USERNAME + " = '" + username + "';";
		if (isUser(username)) {
			System.out.println(query);
			try {
				int count = stat.executeUpdate(query);
				if (count <= 0) {
					System.out.println("No row was updated");
					return false;
				}
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public boolean addRoster(String username, String username2, String subscription) {
		if (isUser(username) && isUser(username2)) {
			long id1 = getUsernameId(username);
			long id2 = getUsernameId(username2);
			String query = "INSERT INTO " + DatabaseContract.Roster.TABLE_NAME + "("
					+ DatabaseContract.Roster.COLUMN_USER_ID + "," + DatabaseContract.Roster.COLUMN_ROSTER_ID + ","
					+ DatabaseContract.Roster.COLUMN_SUBSCRIPTION + ")VALUES(" + id1 + "," + id2 + ",'" + subscription
					+ "');";
			try {
				int count = stat.executeUpdate(query);
				if (count <= 0)
					return false;
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean updateSubscription(String username, String username2, String subscription) {

		if (isUser(username) && isUser(username2)) {
			long id1 = getUsernameId(username);
			long id2 = getUsernameId(username2);
			String query = "UPDATE " + DatabaseContract.Roster.TABLE_NAME + " SET "
					+ DatabaseContract.Roster.COLUMN_SUBSCRIPTION + " = '" + subscription + "' WHERE "
					+ DatabaseContract.Roster.COLUMN_USER_ID + " = " + id1 + " AND "
					+ DatabaseContract.Roster.COLUMN_ROSTER_ID + " = " + id2 + ";";
			try {
				long count = stat.executeUpdate(query);
				if (count <= 0)
					return false;
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean storeMessage(String senderUsername, String receiverUsername, String message, String messageId) {
		message = formatString(message);
		System.out.println("Formated Message: "+message);
		if (isUser(senderUsername) && isUser(receiverUsername)) {
			long senderId = getUsernameId(senderUsername);
			long receiverId = getUsernameId(receiverUsername);
			String query1 = "INSERT INTO " + DatabaseContract.Message.TABLE_NAME + "("
					+ DatabaseContract.Message.COLUMN_ID + "," + DatabaseContract.Message.COLUMN_FROM + ","+
					DatabaseContract.Message.COLUMN_MESSAGE+","
					+ DatabaseContract.Message.COLUMN_STATUS + ")VALUES('" + messageId + "',"+senderId+",'"+message+"','ND');";
			String query2 = "INSERT INTO "+DatabaseContract.MessageRecepient.TABLE_NAME+"("+
							DatabaseContract.MessageRecepient.COLUMN_MESSAGE_ID+","+
							DatabaseContract.MessageRecepient.COLUMN_RECEPIENT_ID+")VALUES('"+
							messageId+"',"+receiverId+");";
			System.out.println(query1);
			try {
				int count1 = stat.executeUpdate(query1);
				int count2 = stat.executeUpdate(query2);
				if(count1>0 && count2>0)
					return true;
				return false;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean updateMessageStatus(String messageId,String status) {
		String query = "UPDATE "+DatabaseContract.Message.TABLE_NAME+" SET "+
						DatabaseContract.Message.COLUMN_STATUS+" = '"+status+"' WHERE "+
						DatabaseContract.Message.COLUMN_ID +" = '"+messageId+"';";
		int count = 0;
		try {
			count = stat.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(count > 0)
			return true;
		return false;
	}

	private void createRosterTable() throws SQLException {
		String queryRosterTable = "CREATE TABLE " + DatabaseContract.Roster.TABLE_NAME + "("
				+ DatabaseContract.Roster.COLUMN_USER_ID + " BIGINT NOT NULL,"
				+ DatabaseContract.Roster.COLUMN_ROSTER_ID + " BIGINT NOT NULL,"
				+ DatabaseContract.Roster.COLUMN_SUBSCRIPTION + " VARCHAR(15)," + "FOREIGN KEY ("
				+ DatabaseContract.Roster.COLUMN_USER_ID + ")" + "REFERENCES " + DatabaseContract.User.TABLE_NAME + "("
				+ DatabaseContract.User.COLUMN_ID + ")," + "FOREIGN KEY (" + DatabaseContract.Roster.COLUMN_ROSTER_ID
				+ ")" + "REFERENCES " + DatabaseContract.User.TABLE_NAME + "(" + DatabaseContract.User.COLUMN_ID + ")"
				+ "ON DELETE CASCADE ON UPDATE CASCADE);";
		System.out.println(queryRosterTable);
		stat.executeUpdate(queryRosterTable);
		System.out.println("Created " + DatabaseContract.Roster.TABLE_NAME + " successfully");
	}

	private void createMessageTable() throws SQLException {
		String queryMessageTable = "CREATE TABLE " + DatabaseContract.Message.TABLE_NAME + "("
				+ DatabaseContract.Message.COLUMN_ID + " VARCHAR(20) PRIMARY KEY NOT NULL,"
				+ DatabaseContract.Message.COLUMN_FROM + " BIGINT NOT NULL," + 
				DatabaseContract.Message.COLUMN_MESSAGE+" LONGTEXT,"+
				DatabaseContract.Message.COLUMN_STATUS+ " VARCHAR(15)," + 
				"FOREIGN KEY (" + DatabaseContract.Message.COLUMN_FROM + ")" + "REFERENCES "
				+ DatabaseContract.User.TABLE_NAME + "(" + DatabaseContract.User.COLUMN_ID + ")"
				+ "ON DELETE CASCADE ON UPDATE CASCADE);";
		System.out.println(queryMessageTable);
		stat.executeUpdate(queryMessageTable);
		System.out.println("Created " + DatabaseContract.Message.TABLE_NAME + " successfully");
	}

	private void createMessageRecepientTable() throws SQLException {
		String queryMessageRecepientTable = "CREATE TABLE " + DatabaseContract.MessageRecepient.TABLE_NAME + "("
				+ DatabaseContract.MessageRecepient.COLUMN_MESSAGE_ID + " VARCHAR(20) NOT NULL,"
				+ DatabaseContract.MessageRecepient.COLUMN_RECEPIENT_ID + " BIGINT NOT NULL," + "FOREIGN KEY ("
				+ DatabaseContract.MessageRecepient.COLUMN_MESSAGE_ID + ") REFERENCES "
				+ DatabaseContract.Message.TABLE_NAME + "(" + DatabaseContract.Message.COLUMN_ID
				+ ") ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY ("
				+ DatabaseContract.MessageRecepient.COLUMN_RECEPIENT_ID + ") REFERENCES "
				+ DatabaseContract.User.TABLE_NAME + "(" + DatabaseContract.User.COLUMN_ID
				+ ") ON DELETE CASCADE ON UPDATE CASCADE);";

		System.out.println(queryMessageRecepientTable);
		stat.executeUpdate(queryMessageRecepientTable);
		System.out.println("Created " + DatabaseContract.MessageRecepient.TABLE_NAME + " successfully");
	}

	private void createUserTable() throws SQLException {
		String queryUserTable = "CREATE TABLE " + DatabaseContract.User.TABLE_NAME + "("
				+ DatabaseContract.User.COLUMN_ID + " BIGINT NOT NULL AUTO_INCREMENT,"
				+ DatabaseContract.User.COLUMN_USERNAME + " VARCHAR(10)," + DatabaseContract.User.COLUMN_PASSWORD
				+ " VARCHAR(30)," + DatabaseContract.User.COLUMN_FIRST_NAME + " VARCHAR(20),"
				+ DatabaseContract.User.COLUMN_LAST_NAME + " VARCHAR(20)," + DatabaseContract.User.COLUMN_STATUS
				+ " VARCHAR(10)," + DatabaseContract.User.COLUMN_STATUS_MESSAGE + " VARCHAR(50)," + "PRIMARY KEY("
				+ DatabaseContract.User.COLUMN_ID + "));";

		stat.executeUpdate(queryUserTable);
		System.out.println("Created " + DatabaseContract.User.TABLE_NAME + " successfully");
	}

	private boolean isUser(String username) {
		// TODO: This query is vulnerable to SQL injection. Fix it
		String query = "select * from " + DatabaseContract.User.TABLE_NAME + " where "
				+ DatabaseContract.User.COLUMN_USERNAME + " = '" + username + "'";
		System.out.println(query);

		try {
			ResultSet rs = stat.executeQuery(query);
			if (rs.next()) {
				return true;
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private long getUsernameId(String username) {
		String query = "SELECT " + DatabaseContract.User.COLUMN_ID + " FROM " + DatabaseContract.User.TABLE_NAME
				+ " WHERE " + DatabaseContract.User.COLUMN_USERNAME + " = '" + username + "';";
		long id = 0;
		if (isUser(username)) {
			try {
				ResultSet result = stat.executeQuery(query);
				while (result.next()) {
					id = result.getLong(DatabaseContract.User.COLUMN_ID);
				}
				return id;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return id;
	}
	
	private String formatString(String message) {
		StringBuilder build = new StringBuilder();
		for(int i = 0;i<message.length();i++) {
			char c = message.charAt(i);
			if(c == '\'') {
				build.append("\\'");
			}
			else if(c == '"') {
				build.append("\\"+"\"");
			}
			else
				build.append(c);
		}
		return build.toString();
	}
	
	public static void main(String args[]) {
		DatabaseManager dm = new DatabaseManager();
		
		 /*dm.addUser("jitu", "jitu", "Jitendra", "Jitendra", "Online","Just started using");
		 dm.addUser("pitu", "pitu", "Pitu", "pitu", "Offline","Pitu I am");*/
		 dm.updateStatus("pitu", "Offline");
		 //dm.addRoster("jitu","pitu", "suscribe"); 
		 dm.updateSubscription("jitu", "pitu", "suscribed");
		 dm.storeMessage("jitu", "pitu", "Hey what's \"Goa\" up", "jitu@1789556");
		 dm.updateMessageStatus("jitu@1789456", "D");
	}
}