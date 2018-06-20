package xml.generator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import database.ChatDatabaseContract.Message;
import xml.XMLException;

public class Element {
	
	private Map<String,String> attributes;
	private String elementName;
	private List<Element> childElements;
	private String text;
	private static StringBuilder xml = new StringBuilder();
	public Element(String elementName) {
		this.elementName = new String(elementName);
		attributes = new HashMap<>();
		childElements = new LinkedList<>();
	}
	
	public Element addAttribute(String name,String value) throws XMLException {
		if(attributes.containsKey(name))
			throw new XMLException("Duplicate Attribute: "+name);
		attributes.put(name, value);
		return this;
	}
	
	public Element addElement(Element element) throws XMLException {
		if(text != null)
			throw new XMLException("Cannot add child elements, element contains text");
		childElements.add(element);
		return this;
	}
	
	public Element addText(String text) throws XMLException {
		if(childElements.size() > 0)
			throw new XMLException("Cannot add Text, Element contains child elements");
		this.text = text;
		return this;
	}
	
	public String toXML() {
		
		xml.append("<"+elementName+" ");
		Iterator<String> keys = attributes.keySet().iterator();
		String key;
		while(keys.hasNext()) {
			key = keys.next();
			String value = attributes.get(key);
			xml.append(key+"="+"\""+value+"\" ");
		}
		if(text != null) {
			xml.append(">"+text);
			xml.append("<"+elementName+"/>");
		}
		else if(childElements.size() <= 0)
			xml.append("/>");
		else {
			xml.append(">\n\t");
			for(int i = 0;i<childElements.size();i++) {
				childElements.get(i).toXML();
			}
			xml.append("\n<"+elementName+"/>");
		}
		return xml.toString();
	}
	
	public static void main(String args[]) {
		Element root = new Element("stream");
		Element child = new Element("message");
		Element child2 = new Element("iq");
		try {
			root.addAttribute("name", "jitendra").addAttribute("game", "Tennis");
			child.addAttribute("to","jitendra");
			child2.addAttribute("to","saurav").addText("Tell me");
			root.addElement(child);
			child.addElement(child2);
		} catch (XMLException e) {
			e.printStackTrace();
		}
		System.out.println(root.toXML());
	}
}