package server;

import java.io.*;
import java.util.*;

class Authentication{

	//this file holds the username and password of all the regiestered user.
	private final String fileName = "server/auth.txt";
	private final ArrayList<Pair> pair = new ArrayList<>();
	
	//class to hold username and password for a user
	static class Pair{
		String id;
		String password;
		public Pair(String id,String password){
			this.id = id;
			this.password = password;
		}
	}

	//Method to register new user
	public synchronized boolean registerUser(String id,String password){
		pair.clear();
		File file = new File(fileName);

		//check if the username already exists in auth.txt
		if(file.exists()){
			if(isUser(id))
				return false;
		}

		//New user put its entry in auth.txt
		try{
			try(PrintWriter out = new PrintWriter(new FileWriter(fileName,true))){
				out.println(id+":"+password);
			}
			
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	//check if the given username and password is valid
	public synchronized boolean authenticate(String id,String password){
		pair.clear();
		readData(pair);
		for(int i = 0;i<pair.size();i++)
			if(equals(pair.get(i),id,password))
				return true;
		return false;
	}



	//reads the data from auth.txt in pair
	private void readData(ArrayList<Pair> pair){
		try{
			try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
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


	private boolean equals(Pair pair,String id,String password){
		if(pair.id.equals(id)&&pair.password.equals(password))
				return true;
		return false;
	}


	public synchronized ArrayList<String> getUserNames(){
		ArrayList<String> userNames = new ArrayList<>();
		pair.clear();
		readData(pair);
		for(int i = 0;i<pair.size();i++){
			userNames.add(pair.get(i).id);
		}
		return userNames;
	}
	
	//return all registered usernames as a string seperated by :
	public String getUsernameStrings(){
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
	
	//checks if a username is a valid registered username
	public boolean isUser(String name){
		ArrayList<String> userNames = getUserNames();
		return userNames.contains(name);
	}

	public String[] parseData(String s){
		return s.split(":");
	}
	
}