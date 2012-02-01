package simon.sms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FileOps {
	
	public static void write(File file, Charset cs, String content) throws IOException {
		List<String> contents = new ArrayList<String>();
		contents.add(content);
		write(file, cs, contents);
	}
	
	public static void write(File file, Charset cs, List<String> contents) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), cs));
		try {
			for (String content : contents) {
				writer.write(content);
				writer.newLine();
			}
		} finally {
			writer.close();
		}
	}
	
	public static List<String> read(File file, Charset cs) throws IOException {
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
		return lines;
	}
}
