package xml.parser;

import java.util.HashMap;
import java.util.Map;

import xml.XMLException;
import xml.generator.Element;

public class Parser {
	
	public static Element parse(String xml) throws XMLException {
		xml.trim();
		Element root = new Element();
		parseXMLV2(root,xml,0);
		return root;
	}
	
	private static void parseXML(Element root,String xml,int index) throws XMLException {
		boolean gotKey = false;
		boolean gotValue = false;
		boolean hasText = false;
		int currentPos = index;
		char c = xml.charAt(currentPos);
		currentPos++;
		StringBuilder elementName = new StringBuilder();
		if(c == '<') {
			c = xml.charAt(currentPos);
			for(int i = currentPos;c !='/'&&c != '>'&&c!=' ';i++) {
				elementName.append(c);
				currentPos++;
				c = xml.charAt(currentPos);
			}
		}
		root.addElementName(elementName.toString());
		//TODO: there can be more than one space check for that as well
		if(c == ' ' && xml.charAt(currentPos) != '/') {
			StringBuilder key = new StringBuilder();
			StringBuilder value = new StringBuilder();
			Map<String,String> attributes = new HashMap<>();
			//currentPos++;
			//c = xml.charAt(currentPos);
			System.out.println("BEFORE: "+c);
			while(c != '/' && c!= '>') {
				if(c == ' ') {
					currentPos++;
					c = xml.charAt(currentPos);
					System.out.println("CHECKING: "+c);
					continue;
				}
				else if(c == '"') {
					currentPos++;
					c = xml.charAt(currentPos);
					while(c != '"') {
						value.append(c);
						currentPos++;
						c = xml.charAt(currentPos);
					}
					gotValue = true;
					currentPos++;
					c = xml.charAt(currentPos);
				}
				else {
					//currentPos++;
					//c = xml.charAt(currentPos);
					while(c != '=') {
						key.append(c);
						currentPos++;
						c = xml.charAt(currentPos);
					}
					gotKey = true;
					currentPos++;
					c = xml.charAt(currentPos);
				}
				System.out.println("LOOP: "+c);
				System.out.println("KEY: "+key.toString());
				if(gotKey && gotValue) {
					attributes.put(key.toString(), value.toString());
					root.addAttribute(attributes);
					gotValue = gotKey = false;
				}
			}
		}
		currentPos++;
		c = xml.charAt(currentPos);
		System.out.println("Hello: "+c);
		StringBuilder text = new StringBuilder();
		if(c != '<' && c != '/' && c != '>') {
			/*currentPos++;
			c = xml.charAt(currentPos);*/
			while(c!='<') {
				text.append(c);
				currentPos++;
				c = xml.charAt(currentPos);
			}
			hasText = true;
			System.out.println("TEXT: "+text.toString());
			root.addText(text.toString());
		}
		//currentPos++;
		c = xml.charAt(currentPos);
		System.out.println("SEE: "+c);
		if(!hasText && c!='/' && c != '>') {
			Element child = new Element();
			root.addElement(child);
			System.out.println("calling again");
			parseXML(child,xml,currentPos);
		}
	}
	
	//It returns Integer because Integer are immutable
	public static Integer parseXMLV2(Element root,String xml,Integer pointer) {
		System.out.println("Just checking");
		char c = xml.charAt(pointer);
		//Removal of whitespaces before <
		while(c == ' ')
			c = xml.charAt(++pointer);
		
		//Get the element name
		StringBuilder elementName = new StringBuilder();
		if(c == '<') {
			c = xml.charAt(++pointer);
			while(c != '/' && c != '>' && c != ' ') {
					elementName.append(c);
					c = xml.charAt(++pointer);
			};
			root.addElementName(elementName.toString());
			
			//There could be attributes(we are not sure because these spaces could lead to / or >)
			if(c == ' ') {
				//clear all the whitespaces
				while((c = xml.charAt(++pointer)) == ' ') {}
				
				//Attributes available
				if(c != '/' && c != '>') {
					Map<String,String> attributes = new HashMap<>();
					//get all attributes
					while(c != '/' && c!='>') {
						StringBuilder key = new StringBuilder();
						StringBuilder value = new StringBuilder();
						//get the key
						while(c != '=') {
							//skip spaces between = and key
							if(c != ' ')
								key.append(c);
							c = xml.charAt(++pointer);
						}
						//skip spaces between = and "
						while(c != '"') {
							c = xml.charAt(++pointer);
						}
						//get the value
						while((c = xml.charAt(++pointer)) != '"')
							value.append(c);
						attributes.put(key.toString(),value.toString());
						//skip spaces between next key or / or >
						while((c = xml.charAt(++pointer)) == ' ') {}
					}
					
					root.addAttribute(attributes);
				}
			}
			//Either a text or child element or a closing tag
			if(c == '>') {
				//Text is present
				StringBuilder text = new StringBuilder();
						
				//Still it is not sure if what we are adding is text(only if whitespaces are present)
				while((c = xml.charAt(++pointer)) != '<')
					text.append(c);
				//Congo it is a text
				//TODO: is spaces allowed between < and / (ex: <  /stream>)
				if(xml.charAt(pointer+1) == '/') {
					try {
						root.addText(text.toString());
					} catch (XMLException e) {
						e.printStackTrace();
					}
					//Eat the string till the closing tag
					while((c = xml.charAt(++pointer)) != '>') {}
				}
				else {
					do {
						Element child = new Element();
						pointer = parseXMLV2(child,xml,pointer);
						try {
							root.addElement(child);
						} catch (XMLException e) {
							e.printStackTrace();
						}
						//eat the white space before <
						while((c = xml.charAt(++pointer)) != '<') {}
					}while(xml.charAt(pointer+1) != '/'); //TODO: Again is spaces allowed between < and / (ex: <  /stream>)
				}
			}
			//A self closing element
			else {
				//eat the string till the closing tag
				while(xml.charAt(++pointer) != '>') {}
			}
		}
		return pointer;
	}
	
	public static void main(String args[]) {
		try {
			StringBuilder build1 = new StringBuilder();
			StringBuilder build2 = new StringBuilder();
			
			Element child = new Element("message").addAttribute("hello", "bye").addAttribute("wow","how").addAttribute("THis","is");
			Element child2 = new Element("iq");
			Element iqChild = new Element("Whatiq");
			Element iqChild2 = new Element("Whatiq2");
			Element whatIq2Child = new Element("How").addAttribute("How","many").addText("Man");
			iqChild2.addElement(whatIq2Child);
			child2.addElement(iqChild).addElement(iqChild2);
			new Element("stream").addAttribute("to", "Jitendra").addAttribute("from","me").addElement(child).addElement(child2).toXML(build1);
			System.out.println("Actual\n"+build1.toString());
			System.out.println("Parsed");
			Element root = Parser.parse(build1.toString());
			root.toXML(build2);
			System.out.println(build2.toString());
			System.out.println(build1.toString().equals(build2.toString()));
			
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}
}