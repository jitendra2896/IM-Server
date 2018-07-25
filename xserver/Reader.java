package xserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xdatabase.DatabaseManager;
import xml.XMLException;
import xml.generator.Element;
import xml.parser.Parser;

public class Reader implements Runnable {

	private DataInputStream din;
	private JobQueue queue;
	private DatabaseManager db;
	private boolean running;
	private String username;
	
	public Reader(DataInputStream din, JobQueue queue,DatabaseManager db,String username) {
		this.din = din;
		this.queue = queue;
		this.db = db;
		this.username = username;
		running = false;
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				String xml = din.readUTF();
				Element root = Parser.parse(xml);
				takeAction(root);
			} catch (IOException e) {
				e.printStackTrace();
				running = false;
			} catch (XMLException e) {
				e.printStackTrace();
			}
		}
	}

	private void takeAction(Element root) {
		String elementName = root.getElementName();
		if(elementName.equals("presence")) {
			handlePresenceStanza(root);
		}
		else if(elementName.equals("message"))
			handleMessageStanza(root);
		else
			handleIqStanza(root);
	}
	
	private void handlePresenceStanza(Element root) {
		Map<String,String> attributes = root.getAttributes();
		
		//suscribtion related request
		if(attributes.containsKey("to")) {
			String username2 = attributes.get("to");
			String suscribtionType = attributes.get("type");
			
			if(suscribtionType.equals("suscribe"))
				db.addRoster(username, username2, suscribtionType);
			else if(suscribtionType.equals("unsuscribe")) {
				db.updateSubscription(username, username2, suscribtionType);
				db.updateSubscription(username2, username, suscribtionType);
			}
			else if(suscribtionType.equals("suscribed")) {
				db.updateSubscription(username, username2, suscribtionType);
				db.updateSubscription(username2, username, suscribtionType);
			}
			else if(suscribtionType.equals("unsuscribed")) {
				db.updateSubscription(username, username2, suscribtionType);
			}
		}
		
		else {
			
			Set<String> keySet = attributes.keySet();
			Iterator<String> keySetIterator = keySet.iterator();
			for(int i = 0;i<keySet.size();i++) {
				String key = attributes.get(keySetIterator.next());
				if(key.equals("unavailable")) {
					db.updateStatus(username, "offline");
				}
			}
			
			List<Element> childElements = root.getChildElements();
			for(int i = 0;i<childElements.size();i++) {
				Element element = childElements.get(i);
				String elementName = element.getElementName();
				if(elementName.equals("status")) {
					String statusMessage = element.getText();
					db.updateStatusMessage(username, statusMessage);
				}
				if(elementName.equals("show")) {
					String status = element.getText();
					db.updateStatus(username, status);
				}
			}
		}
	}
	
	private void handleIqStanza(Element root) {
		Map<String,String> attributes = root.getAttributes();
		String id = attributes.get("id");
		String type = attributes.get("type");
		if(type.equals("get")) {
			List<Element> childElement = root.getChildElements();
			for(int i = 0;i<childElement.size();i++) {
				Element element = childElement.get(i);
				String elementName = element.getElementName();
				Map<String,String> attrib = element.getAttributes();
				String xmlns = attrib.get("xmlns");
				if(elementName.equals("query") && xmlns.equals("jabber:iq:roster")) {
					List<Roster> rost = db.getRosterItem(username);
					String xml = formRosterXml(rost,id,"result");
					queue.put(xml);
				}
			}
		}
	}
	
	private String formRosterXml(List<Roster> rost,String id,String type) {
		Element root = new Element("iq");
		Map<String,String> attrib = new HashMap<>();
		attrib.put("to", username);
		attrib.put("id", id);
		attrib.put("type", type);
		root.addAttribute(attrib);
		try {
			Element query = new Element("query").addAttribute("xmlns","jabber:iq:roster");
			root.addElement(query);
			for(int i = 0;i<rost.size();i++) {
				Roster roster = rost.get(i);
				if (roster.subscription.equals("suscribed")) {
					Map<String, String> itemAttrib = new HashMap<>();
					itemAttrib.put("username", roster.rosterUsername);
					itemAttrib.put("first_name", roster.firstName);
					itemAttrib.put("last_name", roster.lastName);
					itemAttrib.put("status", roster.status);
					itemAttrib.put("status_message", roster.statusMessage);
					Element item = new Element("item");
					item.addAttribute(itemAttrib);
					query.addElement(item);
				}
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
		Map<String,String> attributes = root.getAttributes();
		String id = attributes.get("id");
		String from = attributes.get("from");
		String to = attributes.get("to");
		List<Element> childElement = root.getChildElements();
		for(int i = 0;i<childElement.size();i++) {
			Element element = childElement.get(i);
			String name = element.getElementName();
			//TODO
			if(name.equals("composing")) {
				db.storeMessage(from, to, "", id);
				db.updateMessageStatus(id, "composing");
			}
			//TODO
			else if(name.equals("body")) {
				String message = element.getText();
				db.storeMessage(from, to, message, id);
			}
			//TODO
			else if(name.equals("received")) {
				Map<String,String> attrib = element.getAttributes();
				String receviedId = attrib.get("id");
				db.updateMessageStatus(receviedId, "delivered");
			}
		}
	}
}