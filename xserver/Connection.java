package xserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import xml.XMLException;
import xml.generator.Element;
import xml.parser.Parser;

public class Connection implements Runnable {
	private Socket s;
	private ThreadCompletionCallback callback;
	private boolean running;
	DataInputStream din;
	DataOutputStream dout;

	public Connection(Socket s, ThreadCompletionCallback callback) throws IOException {
		this.s = s;
		this.callback = callback;
		running = false;
		din = new DataInputStream(s.getInputStream());
		dout = new DataOutputStream(s.getOutputStream());
	}

	public void run() {
		running = true;
		while (running) {
			try {
				String xml = din.readUTF();
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
