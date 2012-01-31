package simon.sms;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessageReader {
	
	public static Collection<Message> read(File file, Charset cs) throws IOException {
		List<Message> messages = new ArrayList<Message>();
		
		List<String> lines = Files.readAllLines(file.toPath(), cs);
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
