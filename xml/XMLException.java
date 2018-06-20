package xml;

public class XMLException extends Exception{
	String detail;
	public XMLException(String detail) {
		this.detail = detail;
	}
	
	@Override
	public String toString() {
		return "XML ERROR: "+detail;
	}
}
