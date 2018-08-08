package xdatabase;

public class DatabaseContract {

	public static class User {
		public static final String TABLE_NAME = "users";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_BARE_JID = "jid";
		public static final String COLUMN_PASSWORD = "password";
		public static final String COLUMN_FIRST_NAME = "first_name";
		public static final String COLUMN_LAST_NAME = "last_name";
		public static final String COLUMN_STATUS = "status";
		public static final String COLUMN_STATUS_MESSAGE = "status_message";
	}
	
	public static class UserResources{
		public static final String TABLE_NAME = "user_resource";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_RESOURCE_NAME = "resource_name";
	}
	
	//TODO: Is creating sperate roster table for each user better or just one
	public static class Roster{
		public static final String TABLE_NAME = "roster";//each user will have it's own roster
		public static final String COLUMN_USER_ID = "id"; //ID of the 1st user
		public static final String COLUMN_ROSTER_ID = "roster_id";//ID of the second user
		public static final String COLUMN_SUBSCRIPTION = "subscription";
	}
	
	//TODO: should You create sepreate table for message_recpient;
	public static class Message{
		public static final String TABLE_NAME = "message";
		public static final String COLUMN_ID = "id";		//this will be generated by the sender of this message(i.e client)
		public static final String COLUMN_FROM = "from_col";//col is added because from is a sql keyword
		public static final String COLUMN_MESSAGE = "message";
		public static final String COLUMN_STATUS = "status";
	}
	
	public static class MessageRecepient{
		public static final String TABLE_NAME = "message_recepient";
		public static final String COLUMN_MESSAGE_ID = "message_id";
		public static final String COLUMN_RECEPIENT_ID = "recepient_id";
	}
	
	//The table is different for each user and is a function of its barejid
	public static class PrivateUserTable{
		public static final String COLUMN_ID = "id";//just for primary key
		public static final String COLUMN_STANZA = "stanza";
	}
}