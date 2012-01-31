package simon.sms;

public class Message implements Comparable<Message> {
	
	private String address;
	private String stringTime;
	private String content;
	private boolean received;
	
	public Message(String address, String time, String body, String type) {
		this.address = address != null ? address : "";
		this.received = "recv".equals(type);
		
		if (body != null && body.length() > 0) {
			body = body.replace('\'', ' ').replace('\n', ' ').replace('\r', ' ');
			body = body.trim();
	    	this.content = body;
		} else {
			this.content = "";
		}
		
		this.stringTime = time;
	}

//	@Override
	public int compareTo(Message another) {
		return stringTime.compareTo(another.stringTime);
	}
	
	@Override
	public String toString() {
		return String.format("a('%s', '%s', '%s', '%s');", 
				this.stringTime, this.address, this.content, this.received ? "recv" : "send");
	}
	
}
