package database;

public final class ChatDatabaseContract {
	
	public static class User{
		public static final String TABLE_NAME = "users";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_USERNAME = "username";
		public static final String COLUMN_PASSWORD = "password";
		public static final String COLUMN_FIRST_NAME = "first_name";
		public static final String COLUMN_LAST_NAME = "last_name";
		public static final String COLUMN_IS_ACTIVE = "is_active";
	}
	
	public static class Message{
		public static final String TABLE_NAME = "messages";
		public static final String COLUMN_MESSAGE_ID = "message_id";
		public static final String COLUMN_CREATOR_ID = "creator_id";
		public static final String COLUMN_RECEIVER_ID = "receiver_id";
		public static final String COLUMN_MESSAGE = "message";
		public static final String COLUMN_DATE = "date";
	}
	
	public static class MessageRecepient{
		public static final String TABLE_NAME = "message_recepient";
		public static final String COLUMN_MESSAGE_ID = "message_id";
		public static final String COLUMN_RECEPIENT_ID = "recepient_id";
		public static final String COLUMN_DELIVERED = "delivered";
	}
	
	public static class Friends{
		public static final String TABLE_NAME = "friends";
		public static final String COLUMN_MEMBER_1 = "member_1";
		public static final String COLUMN_MEMBER_2 = "member_2";
		public static final String COLUMN_IS_REQUESTED = "is_requested";
	}
}
