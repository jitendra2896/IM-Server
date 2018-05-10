package filestorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import global.DataManager;

public class FileManager implements DataManager{
	
	private static final String AUTH_FILE_NAME = "server/auth.txt";
	private static final String PATH_TO_MESSAGE_FILE = "server/";
	private final ArrayList<Pair> pair = new ArrayList<>();
	
	//Returns all registered username in a string separated by a colon(:)
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

	@Override
	public String getFriendList(String username) {
		return null;
	}

	@Override
	public boolean authenticate(String username, String password) {
		pair.clear();
		readData();
		for(int i = 0;i<pair.size();i++)
			if(equals(pair.get(i),username,password))
				return true;
		return false;
	}

	@Override
	public boolean isUser(String username) {
		ArrayList<String> userNames = getUserNames();
		return userNames.contains(username);
	}

	@Override
	public boolean registerUser(String username, String password) {
		pair.clear();
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

	@Override
	public boolean isNewMessagAvailable(String username) {
		File file = new File(PATH_TO_MESSAGE_FILE+username+".txt");
		return (file.exists() || file.length() != 0);
	}

	@Override
	public String getNewMessage(String username) throws Exception{
		StringBuilder builder = new StringBuilder();
		try(BufferedReader br = new BufferedReader(new FileReader(PATH_TO_MESSAGE_FILE+username+".txt"))){
			String line = null;
			while((line = br.readLine()) != null){
				builder.append(line+":");
			}
		}
		return builder.toString();
	}
	
	@Override
	public void storeMessage(String username, String msg) throws IOException {
		try(PrintWriter out = new PrintWriter(new FileWriter(PATH_TO_MESSAGE_FILE+username+".txt",true))){
			out.println(msg);
		}
	}
	
	//reads the data from auth.txt in pair
	private synchronized void readData(){
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
	
	private ArrayList<String> getUserNames(){
		ArrayList<String> userNames = new ArrayList<>();
		pair.clear();
		readData();
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
