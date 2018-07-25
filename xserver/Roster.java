package xserver;

public class Roster {
	public String username;
	public String rosterUsername;
	public String firstName;
	public String lastName;
	public String subscription;
	public String status;
	public String statusMessage;
	
	public Roster(String username,String rosterUsername,String firstName,String lastName,String subscription,String status,String statusMessage) {
		this.username = username;
		this.rosterUsername = rosterUsername;
		this.firstName = firstName;
		this.lastName = lastName;
		this.subscription = subscription;
		this.status = status;
		this.statusMessage = statusMessage;
	}
}
