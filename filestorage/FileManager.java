package filestorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import global.DataManager;

/**
 * This class manages and access data present in normal text files
 * @author DELL
 *
 */
public class FileManager implements DataManager{
	
	private static final String AUTH_FILE_NAME = "server/auth.txt";
	private static final String PATH_TO_MESSAGE_FILE = "server/";
	
	/**
	 * Returns all the registered users as string object seperated by :
	 * @return String of usernames seprated by :
	 */
	@Override
	public String getAllUsernames() {
		ArrayList<String> usernames = getUserNames();
		StringBuilder build = new StringBuilder();
		for(int i = 0;i<usernames.size();i++){
			if(i == usernames.size()-1)
				build.append(usernames.get(i));
			else
				build.append(usernames.get(i)+":");
		}
		return build.toString();
	}
	
	/**
	 * Method to return friends of a user
	 * @param username name of user whose friends are required
	 * @return friends of the username
	 */
	@Override
	public String getFriendList(String username) {
		return null;
	}
	
	/**
	 * Method to check if username and password are registered
	 * @param username username to check
	 * @param password password to check
	 * @return true if username and password exist in auth.txt false otherwise
	 */
	@Override
	public boolean authenticate(String username, String password) {
		ArrayList<Pair> pair = new ArrayList<>();
		readData(pair);
		for(int i = 0;i<pair.size();i++)
			if(equals(pair.get(i),username,password))
				return true;
		return false;
	}
	
	/**
	 * It checks weather a user is registered or not
	 * @param username username of user
	 * @return true if username is in auth.txt false otherwise
	 */
	@Override
	public boolean isUser(String username) {
		ArrayList<String> userNames = getUserNames();
		return userNames.contains(username);
	}
	
	/**
	 * Use to register new user
	 * @param username username of the new user
	 * @param password password of the new user
	 * @return true if registration was successful false if the user already exist
	 */
	@Override
	public synchronized boolean registerUser(String username, String password) {
		File file = new File(AUTH_FILE_NAME);

		//check if the username already exists in auth.txt
		if(file.exists()){
			if(isUser(username))
				return false;
		}

		//New user put its entry in auth.txt
		try{
			try(PrintWriter out = new PrintWriter(new FileWriter(AUTH_FILE_NAME,true))){
				out.println(username+":"+password);
			}
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Check if new messages are available for the given user
	 * @param username username of the user for whom new messages are to be checked
	 * @return true if new messages are available false otherwise.
	 */
	@Override
	public boolean isNewMessageAvailable(String username) {
		File file = new File(PATH_TO_MESSAGE_FILE+username+".txt");
		return (file.exists() || file.length() != 0);
	}
	
	/**
	 * get new message for a user
	 * @param username username for whom new message are required
	 * @return message string where each message is seperated by :, null if no new message is available
	 */
	@Override
	public synchronized String getNewMessage(String username) throws Exception{
		if(isNewMessageAvailable(username)) {
			StringBuilder builder = new StringBuilder();
			try(BufferedReader br = new BufferedReader(new FileReader(PATH_TO_MESSAGE_FILE+username+".txt"))){
				String line = null;
				while((line = br.readLine()) != null){
					builder.append(line+":");
				}
			}
			return builder.toString();
		}
		return null;
	}
	
	/**
	 * Method to store messages for a user
	 * @param username username for whom the message is sent
	 * @param msg actual message
	 */
	@Override
	public synchronized void storeMessage(String senderUsername,String recepientUsername, String msg) throws IOException {
		System.out.println("Message is being saved!");
		String formatedMessage = senderUsername+":"+msg;
		try(PrintWriter out = new PrintWriter(new FileWriter(PATH_TO_MESSAGE_FILE+recepientUsername+".txt",true))){
			out.println(formatedMessage);
		}
	}
	
	//reads the data from auth.txt in pair
	private synchronized void readData(ArrayList<Pair> pair){
		try{
			try(BufferedReader br = new BufferedReader(new FileReader(AUTH_FILE_NAME))){
				String line = "";
				String[] parts;
				while((line = br.readLine()) != null){
					parts = line.split(":");
					pair.add(new Pair(parts[0],parts[1]));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//returns all the registered username in string
	private ArrayList<String> getUserNames(){
		ArrayList<String> userNames = new ArrayList<>();
		ArrayList<Pair> pair = new ArrayList<>();
		readData(pair);
		for(int i = 0;i<pair.size();i++){
			userNames.add(pair.get(i).id);
		}
		return userNames;
	}
	
	//checks if content of the pair is equal to username and password
	private boolean equals(Pair pair,String username,String password){
		if(pair.id.equals(username)&&pair.password.equals(password))
			return true;
	return false;
	}
	
	//class to hold username and password for a user
	static class Pair{
		String id,password;
		public Pair(String id,String password){
			this.id = id;
			this.password = password;
		}
	}
	
}
