package simon.sms;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class HtmlPrinter {

	private static final Charset cs = Charset.forName("UTF8");
	private static final Calendar cal = Calendar.getInstance();
	
	public static void main(String[] args) {
		PrintStream err = System.err;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setErr(new PrintStream(baos));
		
		try {
			main0(args);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.setErr(err);
		// overwrite the html file to print the error message if necessary.
		String errors = new String(baos.toByteArray());
		if (errors.length() > 0) {
			try {
				FileOps.write(getOutputFile(), cs, errors);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static void main0(String[] args) throws IOException {
		List<String> contents = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				HtmlPrinter.class.getClassLoader().getResourceAsStream("simon/sms/html_template.html"), cs
		));
		
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				if ("//$".equals(line)) {
					List<Message> messages = new ArrayList<Message>();
					File[] files = getMessageFiles();
					for (File file : files) {
						messages.addAll(MessageReader.read(file, cs));
					}
					Collections.sort(messages);
					for (Message message : messages) {
						contents.add(message.toString());
					}
				} else {
					contents.add(line);
				}
			}
		} finally {
			reader.close();
		}
		
		FileOps.write(getOutputFile(), cs, contents);
	}
	
	private static File getOutputFile() {
		String fileName = String.format("sms-%04d%02d%02d.html", 
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
		return new File(fileName);
	}
	
	// parse the files with name pattern sms-*.txt in the current folder,
	// return the messages sorted by date.
	private static File[] getMessageFiles() throws FileNotFoundException {
		URL location = HtmlPrinter.class.getProtectionDomain().getCodeSource().getLocation();
		String parent = new File(location.getFile()).getParent();
		try {
			parent = URLDecoder.decode(parent, cs.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		File currentFolder = new File(parent);
		
		final Pattern p = Pattern.compile("^sms-\\d+.txt$");
		File[] messageFiles = currentFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return p.matcher(name).matches();
			}
		});
		
		if (messageFiles == null || messageFiles.length == 0) {
			throw new FileNotFoundException(currentFolder.getAbsolutePath() + 
					File.separator + "pattern(" + p.pattern() + ")");
		}
		
		return messageFiles;
	}
}
