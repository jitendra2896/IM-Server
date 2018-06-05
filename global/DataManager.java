package global;

import java.io.IOException;

public interface DataManager {
	public String getAllUsernames();
	public String getFriendList(String username);
	public boolean authenticate(String username,String password);
	public boolean isUser(String username);
	public boolean registerUser(String username,String password);
	public boolean isNewMessageAvailable(String username);
	public String getNewMessage(String username) throws Exception;
	public void storeMessage(String username,String msg) throws IOException;
}
