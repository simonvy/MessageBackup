package simon.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Comparable<Message> {
	
	private static final DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	private String address;
	private long time;
	private String stringTime;
	private String content;
	private boolean received;
	
	public Message(String address, String time, String body, String type) {
		this.address = address != null ? address : "";
		this.received = "1".equals(type);
		
		if (body != null && body.length() > 0) {
			if (body.length() >= 2 && body.startsWith("\"") && body.endsWith("\"")) {
				body = body.substring(1, body.length() - 1);
	    	}
			body = body.replace('\'', ' ').replace('\n', ' ').replace('\r', ' ');
			body = body.trim();
	    	this.content = body;
		} else {
			this.content = "";
		}
		
		this.time = Long.parseLong(time);
		this.stringTime = format.format(new Date(this.time));
	}

//	@Override
	public int compareTo(Message another) {
		if (this == another) {
			return 0;
		}
		if (another == null) {
			return 1;
		}
		return Long.signum(this.time - another.time);
	}
	
	@Override
	public String toString() {
		return String.format("%s'%s'%s'%s", this.stringTime, this.address, this.content, this.received ? "recv" : "send");
	}
	
}
