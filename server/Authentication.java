package server;

import java.io.*;
import java.util.*;
class Authentication{

	//this file holds the username and password of all the regiestered user.
	private static String fileName = "server/auth.txt";
	private static ArrayList<Pair> pair = new ArrayList<>();

	//class to hold username and password for a user
	static class Pair{
		String id;
		String password;
		public Pair(String id,String password){
			this.id = id;
			this.password = password;
		}
	}

	//Method to regiester new user
	public static boolean registerUser(String id,String password){
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
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}


	//check if the given usename and password is valid
	public static boolean authenticate(String id,String password){
		pair.clear();
		readData(pair);
		for(int i = 0;i<pair.size();i++)
			if(equals(pair.get(i),id,password))
				return true;
		return false;
	}



	//reads the data from auth.txt in pair
	private static void readData(ArrayList<Pair> pair){
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


	private static boolean equals(Pair pair,String id,String password){
		if(pair.id.equals(id)&&pair.password.equals(password))
				return true;
		return false;
	}


	public static ArrayList<String> getUserDetails(){
		ArrayList<String> userNames = new ArrayList<>();
		pair.clear();
		readData(pair);
		for(int i = 0;i<pair.size();i++){
			userNames.add(pair.get(i).id);
		}
		return userNames;
	}

	//checks if a username is a valid registered username
	public static boolean isUser(String name){
		ArrayList<String> userNames = getUserDetails();
		return userNames.contains(name);
	}

	public static String[] parseData(String s){
		return s.split(":");
	}
}