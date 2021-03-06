package server;

import java.io.*;
import java.util.*;

class Saver{
	private static final String PACKAGE_NAME = "server";
	public static synchronized void saveToFile(String fileName,String s)throws Exception{
		try(PrintWriter out = new PrintWriter(new FileWriter(PACKAGE_NAME+"/"+fileName+".txt",true))){
			out.println(s);
		}
	}
	public static synchronized String getMessages(String userName)throws Exception {
		StringBuilder builder = new StringBuilder();
		try(BufferedReader br = new BufferedReader(new FileReader(PACKAGE_NAME+"/"+userName+".txt"))){
			String line = null;
			while((line = br.readLine()) != null){
					builder.append(line+"\n");
			}
		}
		return builder.toString();
	}
}