package xserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import xdatabase.DatabaseManager;
import xml.XMLException;
import xml.generator.Element;
import xml.parser.Parser;

public class Reader implements Runnable,DatabaseCallback {

	private DataInputStream din;
	private JobQueue outputQueue;
	private BlockingQueue<String> inputQueue;
	private DatabaseManager db;
	private boolean running;
	private String userJid;
	private Thread socketInputStreamReaderThread;
	
	//Thread that reads data from din
	class SocketInputStreamReader implements Runnable{

		@Override
		public void run() {
			while(running) {
				try {
					boolean success = inputQueue.offer(din.readUTF());
					//Queue is full.
					if(!success) {
						//TODO: Take action when this queue is full
						//Remember inputQueue capacity is 1000 set in the Server class
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public Reader(DataInputStream din, JobQueue outputQueue,BlockingQueue<String> inputQueue, DatabaseManager db, String userJid) {
		this.din = din;
		this.outputQueue = outputQueue;
		this.inputQueue = inputQueue;
		this.db = db;
		this.userJid = userJid;
		running = false;
		socketInputStreamReaderThread = new Thread(new SocketInputStreamReader());
		socketInputStreamReaderThread.start();
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				String xml = null;
				try {
					xml = inputQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Element root = Parser.parse(xml);
				takeAction(root);
			} catch (XMLException e) {
				e.printStackTrace();
			}
		}
	}

	private void takeAction(Element root) {
		String elementName = root.getElementName();
		if (elementName.equals("presence")) {
			handlePresenceStanza(root);
		} else if (elementName.equals("message"))
			handleMessageStanza(root);
		else
			handleIqStanza(root);
	}

	private void handlePresenceStanza(Element root) {
		Map<String,String> attributes = root.getAttributes();
		String id = attributes.get("id");
		String to = attributes.get("to");
		String type = attributes.get("type");
		//suscribtion request
		//add this to roster and send then push the iq stanza with subscription of none
		//and ask attribute to suscribe
		//TODO: How will the to guy know about this request
		if(type.equals("suscribe")) {
			db.addRoster(userJid, to, "none");
			User user = db.getUserInformation(to);
			Roster rost = new Roster(userJid,to,user.firstName,user.lastName,"none");
			List<Roster> list = new LinkedList<>();
			list.add(rost);
			String rosterXml = formRosterXml(list,id,"set","suscribe");
			outputQueue.put(rosterXml);
		}
	}

	private void handleIqStanza(Element root) {
		Map<String,String> attributes = root.getAttributes();
		String id = attributes.get("id");
		String type = attributes.get("type");
		List<Element> childElement = root.getChildElements();
		
		//Roster get request after users logs in
		if(type.equals("get")) {
			for(int i = 0;i<childElement.size();i++) {
				Element element = childElement.get(i);
				String elementName = element.getElementName();
				Map<String,String> attrib = element.getAttributes();
				String xmlns = attrib.get("xmlns");
				//query for roster get
				if(elementName.equals("query") && xmlns.equals("jabber:iq:roster")) {
					List<Roster> rost = db.getRosterItem(userJid);
					String xml = formRosterXml(rost,id,"result",null);
					outputQueue.put(xml);
				}
			}
		}
		
		//create, update or delete roster request
		//remember there should be only one item element
		else if(type.equals("set")) {
			Element queryElement = childElement.get(0);	//get query element
			List<Element> itemElementList = queryElement.getChildElements();
			
			if(itemElementList.size() > 1) {
				//TODO: Error Case
			}
			else {
				//TODO: itemElement can also contain a group child element(though that feature is not implemented yet)
				Element itemElement = itemElementList.get(0);
				Map<String,String> attrib = itemElement.getAttributes();
				String jid = attrib.get("jid");
				//remove request
				if(attrib.containsKey("subscription") && attrib.get("subscription").equals("remove")) {
					//send result stanza to initiating resource
					String resultXml = iqResultStanza(id);
					outputQueue.put(resultXml);
					//TODO send roster push to all interested resource
					Roster roster = new Roster(userJid,jid,null,null,"remove");
					List<Roster> list = new LinkedList<>();
					list.add(roster);
					String rosterXml = formRosterXml(list,id,"set",null);
					outputQueue.put(rosterXml);
				}
				//create or update request
				else {
					String name = attrib.get("name");
					db.addRoster(userJid, jid, "none");
					//send the result iq stanza
					String resultXml = iqResultStanza(id);
					outputQueue.put(resultXml);
				
					//push the newly updated or created roster item
					//TODO: implement sending this push notification to all the connected user resource
					//TODO: this push needs to pushed to all connected resources of this user
					User user = db.getUserInformation(jid);
					Roster roster = new Roster(userJid,jid,user.firstName,user.lastName,"none");
					List<Roster> list = new LinkedList<>();
					list.add(roster);
					String rosterXml = formRosterXml(list,id,"set",null);
					outputQueue.put(rosterXml);
				}
			}
		}
	}

	// just a simple iq stanza with type result and no childern
	private String iqResultStanza(String id) {
		Element root = new Element("iq");
		Map<String, String> attrib = new HashMap<>();
		attrib.put("id", id);
		attrib.put("to", userJid);
		attrib.put("type", "result");
		root.addAttribute(attrib);
		try {
			return root.toXML(new StringBuilder());
		} catch (XMLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// creates xml items representing roster items in response to roster get
	private String formRosterXml(List<Roster> rost, String id, String type,String askAttrib) {
		Element root = new Element("iq");
		Map<String, String> attrib = new HashMap<>();
		attrib.put("to", userJid);
		attrib.put("id", id);
		attrib.put("type", type);
		root.addAttribute(attrib);
		try {
			Element query = new Element("query").addAttribute("xmlns", "jabber:iq:roster");
			root.addElement(query);

			// create item elements
			for (int i = 0; i < rost.size(); i++) {
				Roster roster = rost.get(i);
				Map<String, String> itemAttrib = new HashMap<>();
				itemAttrib.put("userJid", roster.rosterBareJid);
				if(roster.firstName != null && roster.firstName.length() > 0)
					itemAttrib.put("name", roster.firstName + ":" + roster.lastName);
				if(roster.lastName != null && roster.lastName.length() > 0)
					itemAttrib.put("subscription", roster.subscription);
				if(askAttrib != null&& askAttrib.length() > 0)
					itemAttrib.put("ask", askAttrib);
				Element item = new Element("item");
				item.addAttribute(itemAttrib);
				query.addElement(item);
			}
			StringBuilder build = new StringBuilder();
			root.toXML(build);
			return build.toString();
		} catch (XMLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void handleMessageStanza(Element root) {
		Map<String, String> attributes = root.getAttributes();
		String id = attributes.get("id");
		String from = attributes.get("from");
		String to = attributes.get("to");
		List<Element> childElement = root.getChildElements();
		for (int i = 0; i < childElement.size(); i++) {
			Element element = childElement.get(i);
			String name = element.getElementName();
			// TODO
			if (name.equals("composing")) {
				db.storeMessage(from, to, "", id);
				db.updateMessageStatus(id, "composing");
			}
			// TODO
			else if (name.equals("body")) {
				String message = element.getText();
				db.storeMessage(from, to, message, id);
			}
			// TODO
			else if (name.equals("received")) {
				Map<String, String> attrib = element.getAttributes();
				String receviedId = attrib.get("id");
				db.updateMessageStatus(receviedId, "delivered");
			}
		}
	}

	@Override
	public void callback() {
		
	}
}