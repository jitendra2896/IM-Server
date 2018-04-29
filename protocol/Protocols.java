package protocol;
public class Protocols{
	//Client Requests
	public static final String LOG_IN_REQUEST = "LOG_IN_REQUEST";
	public static final String SIGN_UP_REQUEST = "SIGN_UP_REQUEST";
	public static final String GET_NEW_MESSAGES = "GET_NEW_MESSAGES";

	//Server Responses
	public static final String EVERYTHING_NOT_OKAY = "EVERYTHING_NOT_OKAY";
	public static final String EVERYTHING_OKAY = "EVERYTHING_OKAY";
	public static final String NEW_MESSAGES_AVAILABLE = "NEW_MESSAGES_AVAILABLE";
	public static final String NO_NEW_MESSAGES = "NO_NEW_MESSAGES";
	public static final String USER_DOESNT_EXIST = "USER_DOESNT_EXIST";
	public static final String USER_SUCCESSFULLY_LOGGED_IN = "USER_SUCCESSFULLY_LOGGED_IN";
	public static final String LOG_IN_UNSUCCESSFUL = "LOG_IN_UNSUCCESSFUL";
	public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
	public static final String SIGN_UP_SUCCESSFUL = "LOG_IN_UNSUCCESSFULL";
}