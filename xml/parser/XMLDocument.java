package xml.parser;

import xml.XMLException;
import xml.generator.Element;

public class XMLDocument {
	private static Element root;
	
	public XMLDocument(String xml) {
		try {
			root = Parser.parse(xml);
		} catch (XMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Element getRootElement() {
		return root;
	}
}