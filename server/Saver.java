package server;

import java.io.*;
import java.util.*;

class Saver{
	
	private static final String PACKAGE_NAME = "server";
	
	//write the message to the username file
	public synchronized void saveToFile(String fileName,String s)throws Exception{
		try(PrintWriter out = new PrintWriter(new FileWriter(PACKAGE_NAME+"/"+fileName+".txt",true))){
			out.println(s);
		}
	}
	
	//Reads message from the username file
	public synchronized String getMessages(String userName)throws Exception {
		StringBuilder builder = new StringBuilder();
		try(BufferedReader br = new BufferedReader(new FileReader(PACKAGE_NAME+"/"+userName+".txt"))){
			String line = null;
			while((line = br.readLine()) != null){
					builder.append(line+":");
			}
		}
		return builder.toString();
	}
}