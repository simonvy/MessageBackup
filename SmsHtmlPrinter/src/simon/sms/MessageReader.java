package simon.sms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessageReader {
	
	public static Collection<Message> read(File file, Charset cs) throws IOException {
		List<Message> messages = new ArrayList<Message>();
		
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), cs));
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} finally {
			reader.close();
		}
		
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.length() == 0) {
				continue;
			}
			if (!isEndline(line)) {
				for (i++; i < lines.size(); i++) {
					String nline = lines.get(i);
					line += " " + nline;
					if (isEndline(nline)) {
						break;
					}
				}
			}
			String[] parts = line.split("'");
			if (parts.length != 4) {
				System.err.println(line);
			} else {
				messages.add(new Message(parts[1], parts[0], parts[2], parts[3]));
			}
		}
		
		return messages;
	}

	private static boolean isEndline(String line) {
		return line.endsWith("'recv") || line.endsWith("'send");
	}
}
