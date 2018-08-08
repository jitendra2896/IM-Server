package xserver;

public class Roster {
	public String userBareJid;
	public String rosterBareJid;
	public String firstName;
	public String lastName;
	public String subscription;
	
	public Roster(String userBareJid,String rosterBareJid,String firstName,String lastName,String subscription) {
		this.userBareJid = userBareJid;
		this.rosterBareJid = rosterBareJid;
		this.firstName = firstName;
		this.lastName = lastName;
		this.subscription = subscription;
	}
}
