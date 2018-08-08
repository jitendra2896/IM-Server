package xserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import xdatabase.DatabaseManager;
import xml.XMLException;
import xml.generator.Element;
import xml.parser.Parser;

public class Connection implements Runnable {
	
	private Socket s;
	private ThreadCompletionCallback callback;
	private boolean running;
	DataInputStream din;
	DataOutputStream dout;
	BlockingQueue<String> queue;
	private Reader reader;
	String bareJid;
	private DatabaseManager dbManager;
	Thread readerThread;
	Thread writerThread; //TODO: implementation required
	
	public Connection(Socket s, ThreadCompletionCallback callback,BlockingQueue<String> queue,DatabaseManager dbManager) throws IOException {
		this.s = s;
		this.callback = callback;
		running = false;
		din = new DataInputStream(s.getInputStream());
		dout = new DataOutputStream(s.getOutputStream());
		this.queue = queue;
		this.dbManager = dbManager;
	}

	public void run() {
		running = true;
		while (running) {
			try {
				String xml = din.readUTF();
				String[] components = xml.split(":");
				/*
				 * components[0] = LOG_IN_REQUEST or SIGN_UP_REQUEST
				 * components[1] = bareJid(if SIGN_UP_REQUEST first name and last name seprated by #);
				 * components[2] = password
				 * components[3] = resource
				 * 
				 */
				
				if(components[0].equals("LOG_IN_REQUEST")) {
					String bareJid = components[1];
					String password = components[2];
					if(dbManager.authenticate(bareJid, password)) {
						Reader reader = new Reader(din, null, queue, dbManager, bareJid);//TODO: add OutputQueue
						readerThread = new Thread(reader);
						dbManager.addConnectedUser(bareJid, queue);
						readerThread.start();
						try {
							readerThread.join();
							dbManager.removeConnectedUser(bareJid);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				else if(components[0].equals("SIGN_UP_REQUEST")) {
					String moreComponents[] = components[1].split("#");
					String password = components[2];
					String bareJid = moreComponents[0];
					String firstName = moreComponents[1];
					String lastName = moreComponents[2];
					if(dbManager.addUser(bareJid, password, firstName, lastName, "Online", "New User")) {
						//TODO: Sign up successful
					}
					else {
						//TODO: Sign up failed
					}
				}
				
				Element root = processXML(xml);
				takeAction(root);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

	private Element processXML(String xml) {
		try {
			return Parser.parse(xml);
		} catch (XMLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void takeAction(Element root) {
		
	}
}
