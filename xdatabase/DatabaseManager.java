package xdatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import database.ChatDatabaseContract;
import xdatabase.DatabaseContract;
import xserver.Roster;
import xserver.User;

public class DatabaseManager {
	private final String DATABASE_NAME = "chat_database";
	private final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
	private final String USERNAME = "root";
	private final String PASSWORD = "";
	private final String CONNECTION_URL = "jdbc:mysql://localhost/" + DATABASE_NAME + "?&useSSL=false";
	private Connection conn;
	private Statement stat;
	
	private HashMap<String,BlockingQueue<String>> connectedUsersQueue;

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
			rs = md.getTables(null, null, DatabaseContract.UserResources.TABLE_NAME, null);
			if (!rs.next() || !rs.getString(3).equals(DatabaseContract.UserResources.TABLE_NAME)) {
				createResourceTable();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		connectedUsersQueue = new HashMap<>();
	}
	
	public void addConnectedUser(String bareJid,BlockingQueue<String> queue) {
		connectedUsersQueue.put(bareJid, queue);
	}
	
	public void removeConnectedUser(String bareJid) {
		connectedUsersQueue.remove(bareJid);
	}
	
	public boolean authenticate(String bareJid,String password) {
		String query = "SELECT * FROM "+DatabaseContract.User.TABLE_NAME+" WHERE "+
				DatabaseContract.User.COLUMN_BARE_JID + " '"+bareJid+"' AND "+
				DatabaseContract.User.COLUMN_PASSWORD+" '"+password+"';";
		
		boolean result = false;
		
		try {
			ResultSet resultSet = stat.executeQuery(query);
			result = resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean addUser(String bareJid, String password, String firstName, String lastName, String status,
			String statusMessage) {
		bareJid = bareJid.toLowerCase();
		statusMessage = formatString(statusMessage);
		String query = "INSERT INTO " + DatabaseContract.User.TABLE_NAME + "(" + DatabaseContract.User.COLUMN_BARE_JID
				+ "," + DatabaseContract.User.COLUMN_PASSWORD + "," + DatabaseContract.User.COLUMN_FIRST_NAME + ","
				+ DatabaseContract.User.COLUMN_LAST_NAME + "," + DatabaseContract.User.COLUMN_STATUS + ","
				+ DatabaseContract.User.COLUMN_STATUS_MESSAGE + ")VALUES('" + bareJid + "','" + password + "','"
				+ firstName + "','" + lastName + "','" + status + "','" + statusMessage + "');";

		if (!isUser(bareJid)) {
			try {
				System.out.println(query);
				stat.executeUpdate(query);
				createPrivateUserTable(bareJid);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		System.out.println("User already exist");
		return false;

	}

	public boolean updateStatus(String bareJid, String status) {
		String query = "UPDATE " + DatabaseContract.User.TABLE_NAME + " SET " + DatabaseContract.User.COLUMN_STATUS
				+ " = '" + status + "' WHERE " + DatabaseContract.User.COLUMN_BARE_JID + " = '" + bareJid + "';";
		if (isUser(bareJid)) {
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

	public boolean updateStatusMessage(String bareJid, String statusMessage) {
		String query = "UPDATE " + DatabaseContract.User.TABLE_NAME + " SET "
				+ DatabaseContract.User.COLUMN_STATUS_MESSAGE + " = '" + statusMessage + "' WHERE "
				+ DatabaseContract.User.COLUMN_BARE_JID + " = '" + bareJid + "';";
		if (isUser(bareJid)) {
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
	
	//create a new roster or update it if it already exist
	public boolean addRoster(String bareJid, String bareJid2, String subscription) {
		if (isUser(bareJid) && isUser(bareJid2)) {
			long id1 = getIntegerId(bareJid);
			long id2 = getIntegerId(bareJid2);
			if (!rosterAlreadyPresent(id1, id2, subscription)) {
				String query = "INSERT INTO " + DatabaseContract.Roster.TABLE_NAME + "("
						+ DatabaseContract.Roster.COLUMN_USER_ID + "," + DatabaseContract.Roster.COLUMN_ROSTER_ID + ","
						+ DatabaseContract.Roster.COLUMN_SUBSCRIPTION + ")VALUES(" + id1 + "," + id2 + ",'"
						+ subscription + "');";
				try {
					int count = stat.executeUpdate(query);
					if (count <= 0)
						return false;
					return true;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	private boolean rosterAlreadyPresent(long id1, long id2,String subscription) {
		String query = "SELECT "+DatabaseContract.Roster.COLUMN_ROSTER_ID+" FROM "+
				DatabaseContract.Roster.TABLE_NAME+" WHERE "+
				DatabaseContract.Roster.COLUMN_USER_ID+" = "+id1+" AND "+
				DatabaseContract.Roster.COLUMN_ROSTER_ID+" = "+id2+" AND "+
				DatabaseContract.Roster.COLUMN_SUBSCRIPTION+" = '"+subscription+"';";
		ResultSet result;
		try {
			result = stat.executeQuery(query);
			return result.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public List<Roster> getRosterItem(String bareJid) {
		List<Roster> rosters = new LinkedList<>();
		if (isUser(bareJid)) {
			long id = getIntegerId(bareJid);
			String query = "SELECT " + DatabaseContract.User.COLUMN_BARE_JID + ","
					+ DatabaseContract.User.COLUMN_FIRST_NAME + "," + DatabaseContract.User.COLUMN_LAST_NAME + ","
					+ DatabaseContract.User.COLUMN_STATUS_MESSAGE + "," + DatabaseContract.User.COLUMN_STATUS +","
					+ DatabaseContract.Roster.COLUMN_SUBSCRIPTION+" FROM "
					+ DatabaseContract.Roster.TABLE_NAME + "," + DatabaseContract.User.TABLE_NAME + " WHERE "
					+ DatabaseContract.Roster.COLUMN_USER_ID + " = " + id + " AND "
					+ DatabaseContract.Roster.COLUMN_ROSTER_ID + " = " + DatabaseContract.User.COLUMN_ID + ";";
			
			try {
				ResultSet result = stat.executeQuery(query);
				while(result.next()) {
					String bareJidResult = result.getString(DatabaseContract.User.COLUMN_BARE_JID);
					String firstName = result.getString(DatabaseContract.User.COLUMN_FIRST_NAME);
					String lastName = result.getString(DatabaseContract.User.COLUMN_LAST_NAME);
					String statusMessage = result.getString(DatabaseContract.User.COLUMN_STATUS_MESSAGE);
					String status = result.getString(DatabaseContract.User.COLUMN_STATUS);
					String subscription = result.getString(DatabaseContract.Roster.COLUMN_SUBSCRIPTION);
					Roster rost = new Roster(bareJid,bareJidResult,firstName,lastName,subscription);
					rosters.add(rost);
				}
				return rosters;
			} catch (SQLException e) {
				e.printStackTrace();
			}	
		}
		return null;

	}

	public boolean updateSubscription(String bareJid, String bareJid2, String subscription) {

		if (isUser(bareJid) && isUser(bareJid2)) {
			long id1 = getIntegerId(bareJid);
			long id2 = getIntegerId(bareJid2);
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

	public boolean storeMessage(String senderBareJid, String receiverBareJid, String message, String messageId) {
		message = formatString(message);
		System.out.println("Formated Message: " + message);
		if (isUser(senderBareJid) && isUser(receiverBareJid)) {
			long senderId = getIntegerId(senderBareJid);
			long receiverId = getIntegerId(receiverBareJid);
			String query1 = "INSERT INTO " + DatabaseContract.Message.TABLE_NAME + "("
					+ DatabaseContract.Message.COLUMN_ID + "," + DatabaseContract.Message.COLUMN_FROM + ","
					+ DatabaseContract.Message.COLUMN_MESSAGE + "," + DatabaseContract.Message.COLUMN_STATUS
					+ ")VALUES('" + messageId + "'," + senderId + ",'" + message + "','ND');";
			String query2 = "INSERT INTO " + DatabaseContract.MessageRecepient.TABLE_NAME + "("
					+ DatabaseContract.MessageRecepient.COLUMN_MESSAGE_ID + ","
					+ DatabaseContract.MessageRecepient.COLUMN_RECEPIENT_ID + ")VALUES('" + messageId + "',"
					+ receiverId + ");";
			System.out.println(query1);
			try {
				int count1 = stat.executeUpdate(query1);
				int count2 = stat.executeUpdate(query2);
				if (count1 > 0 && count2 > 0)
					return true;
				return false;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean updateMessageStatus(String messageId, String status) {
		String query = "UPDATE " + DatabaseContract.Message.TABLE_NAME + " SET "
				+ DatabaseContract.Message.COLUMN_STATUS + " = '" + status + "' WHERE "
				+ DatabaseContract.Message.COLUMN_ID + " = '" + messageId + "';";
		int count = 0;
		try {
			count = stat.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (count > 0)
			return true;
		return false;
	}
	
	public boolean addResource(String bareJid,String resourceName) {
		bareJid = bareJid.toLowerCase();
		resourceName = resourceName.toLowerCase();
		if(isUser(bareJid)) {
			long id = getIntegerId(bareJid);
			String query = "INSERT INTO "+DatabaseContract.UserResources.TABLE_NAME+"("+
					DatabaseContract.UserResources.COLUMN_ID+","+
					DatabaseContract.UserResources.COLUMN_RESOURCE_NAME+") VALUES ("+
					id+",'"+resourceName+"');";
			int count = 0;
			try {
				count = stat.executeUpdate(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(count <= 0)
				return false;
			return true;
		}
		return false;
	}
	
	public boolean removeResource(String bareJid,String resourceName) {
		resourceName = resourceName.toLowerCase();
		if(isUser(bareJid)) {
			long id = getIntegerId(bareJid);
			String query = "DELETE FROM "+DatabaseContract.UserResources.TABLE_NAME+" WHERE "+
					DatabaseContract.UserResources.COLUMN_ID +" = "+id+" AND "+
					DatabaseContract.UserResources.COLUMN_RESOURCE_NAME+" = '"+resourceName+"';";
			int count = 0;
			try {
				stat.executeUpdate(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(count <= 0)
				return false;
			return true;
		}
		return false;
	}
	
	public User getUserInformation(String jid) {
		if(isUser(jid)) {
			String query = "SELECT * FROM "+DatabaseContract.User.TABLE_NAME+" WHERE "+
					DatabaseContract.User.COLUMN_BARE_JID+" = '"+jid+"';";
		
			try {
				ResultSet result = stat.executeQuery(query);
				while(result.next()) {
					String firstName = result.getString(DatabaseContract.User.COLUMN_FIRST_NAME);
					String lastName = result.getString(DatabaseContract.User.COLUMN_LAST_NAME);
					User user = new User(jid,firstName,lastName);
					return user;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public boolean putDataInUserPrivateTable(String bareJid,String stanza) {
		String legalTableName = getLegalPrivateTableName(bareJid);
		String query = "INSERT INTO "+legalTableName+"("+
				DatabaseContract.PrivateUserTable.COLUMN_STANZA+")VALUES('"+
				stanza+"');";
		
		try {
			int count = stat.executeUpdate(query);
			if(count > 0) {
				BlockingQueue<String> queue = connectedUsersQueue.get(bareJid);
				if(queue!=null)
					queue.offer(stanza);
				//TODO: call the callback method of Connection class
				return true;
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public ArrayList<String> getData(String bareJid){
		bareJid = getLegalPrivateTableName(bareJid);
		String query = "SELECT * FROM "+bareJid+";";
		ResultSet result;
		try {
			result = stat.executeQuery(query);
			ArrayList<String> list = new ArrayList<>();
			while(result.next()) {
				list.add(result.getString(DatabaseContract.PrivateUserTable.COLUMN_STANZA));
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//limit is inclusive
	public boolean deletePrivateUserData(String bareJid,int limit) {
		bareJid = getLegalPrivateTableName(bareJid);
		String query = "DELETE FROM "+bareJid+" WHERE "+
				DatabaseContract.PrivateUserTable.COLUMN_ID+"<="+limit+");";
		try {
			int count = stat.executeUpdate(query);
			if(count > 0)
				return true;
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
				+ DatabaseContract.Message.COLUMN_FROM + " BIGINT NOT NULL," + DatabaseContract.Message.COLUMN_MESSAGE
				+ " LONGTEXT," + DatabaseContract.Message.COLUMN_STATUS + " VARCHAR(15)," + "FOREIGN KEY ("
				+ DatabaseContract.Message.COLUMN_FROM + ")" + "REFERENCES " + DatabaseContract.User.TABLE_NAME + "("
				+ DatabaseContract.User.COLUMN_ID + ")" + "ON DELETE CASCADE ON UPDATE CASCADE);";
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
				+ DatabaseContract.User.COLUMN_BARE_JID + " VARCHAR(30) NOT NULL," + DatabaseContract.User.COLUMN_PASSWORD
				+ " VARCHAR(30)," + DatabaseContract.User.COLUMN_FIRST_NAME + " VARCHAR(20),"
				+ DatabaseContract.User.COLUMN_LAST_NAME + " VARCHAR(20)," + DatabaseContract.User.COLUMN_STATUS
				+ " VARCHAR(10)," + DatabaseContract.User.COLUMN_STATUS_MESSAGE + " VARCHAR(50)," + "PRIMARY KEY("
				+ DatabaseContract.User.COLUMN_ID + ","+DatabaseContract.User.COLUMN_BARE_JID+"));";

		System.out.println(queryUserTable);
		stat.executeUpdate(queryUserTable);
		System.out.println("Created " + DatabaseContract.User.TABLE_NAME + " successfully");
	}
	
	private void createResourceTable() throws SQLException{
		String queryResourceTable = "CREATE TABLE "+DatabaseContract.UserResources.TABLE_NAME+"("
				+DatabaseContract.UserResources.COLUMN_ID+" BIGINT NOT NULL,"
				+DatabaseContract.UserResources.COLUMN_RESOURCE_NAME+" VARCHAR(30),"
				+"FOREIGN KEY ("+DatabaseContract.UserResources.COLUMN_ID+") REFERENCES "
				+DatabaseContract.User.TABLE_NAME+"("+DatabaseContract.User.COLUMN_ID+")"
				+" ON DELETE CASCADE ON UPDATE CASCADE);";
		
		System.out.println(queryResourceTable);
		stat.executeUpdate(queryResourceTable);
		System.out.println("Created " + DatabaseContract.UserResources.TABLE_NAME + " successfully");
	}
	
	private void createPrivateUserTable(String bareJid) throws SQLException {
		String legalTableName = getLegalPrivateTableName(bareJid);
		String queryPrivateTable = "CREATE TABLE "+legalTableName+"("
				+DatabaseContract.PrivateUserTable.COLUMN_ID+" BIGINT NOT NULL AUTO_INCREMENT,"
				+DatabaseContract.PrivateUserTable.COLUMN_STANZA+" LONGTEXT,PRIMARY KEY ("+
				DatabaseContract.PrivateUserTable.COLUMN_ID+"));";
		System.out.println(queryPrivateTable);
		stat.executeUpdate(queryPrivateTable);
		System.out.println("Create "+legalTableName+" private table");
	}
	
	//replaces '@','.' in jid to 'a' and 'd' to make it a legal mysql table name
	private String getLegalPrivateTableName(String jid) {
		String temp = jid.replace('@', 'a');
		return temp.replace('.', 'd');
	}

	private boolean isUser(String bareJid) {
		bareJid = bareJid.toLowerCase();
		// TODO: This query is vulnerable to SQL injection. Fix it
		String query = "select * from " + DatabaseContract.User.TABLE_NAME + " where "
				+ DatabaseContract.User.COLUMN_BARE_JID + " = '" + bareJid + "'";
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

	private long getIntegerId(String bareJid) {
		bareJid = bareJid.toLowerCase();
		String query = "SELECT " + DatabaseContract.User.COLUMN_ID + " FROM " + DatabaseContract.User.TABLE_NAME
				+ " WHERE " + DatabaseContract.User.COLUMN_BARE_JID + " = '" + bareJid + "';";
		long id = 0;
		if (isUser(bareJid)) {
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
		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);
			if (c == '\'') {
				build.append("\\'");
			} else if (c == '"') {
				build.append("\\" + "\"");
			} else
				build.append(c);
		}
		return build.toString();
	}

	public static void main(String args[]) {
		DatabaseManager dm = new DatabaseManager();
		dm.addUser("jitendra@gmail.com", "Hello", "Jitendra", "Tiwari", "Online", "message");
		dm.addUser("Lola@gmail.com", "Zero", "Lola", "Williams", "Offline", "lets do it");
		dm.addUser("Pola@gmail.com", "Goa", "Pola", "Turner", "Online", "you're the best");
		dm.addResource("Lola@gmail.com", "Android mobile");
		dm.addResource("Lola@gmail.com", "Dell desktop");
		dm.putDataInUserPrivateTable("jitendra@gmail.com", "This is just a sample");
	}
}