package simon.android;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SmsBackupActivity extends Activity {
	
	private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms/");
	private static final Calendar cal = Calendar.getInstance();
	private static final int toast_period = 15 * 1000;
	private StringBuilder notes = new StringBuilder();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Account[] accounts = AccountManager.get(this).getAccounts();
        List<String> accountList = new ArrayList<String>();
        for (Account account : accounts) {
        	accountList.add(account.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, 
                accountList.toArray(new String[accountList.size()]));
        AutoCompleteTextView emailEdit = (AutoCompleteTextView)findViewById(R.id.email);
        emailEdit.setAdapter(adapter);
        
        Button sendBtn = (Button)findViewById(R.id.sendBtn);
        
        sendBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View btn) {
				backup();
			}
		});
    }
	
	private void backup() {
		
		String state = Environment.getExternalStorageState();
    	
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			String fileName = String.format("sms-%04d%02d%02d.txt", 
					cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
			
			File path = Environment.getExternalStorageDirectory();
    		File file = new File(path, fileName);
    		path.mkdirs();
    		
    		if (saveToFile(file)) {
				sendFile(file);
			} else {
				Toast.makeText(this, notes.toString(), toast_period).show();
			}
			notes.delete(0, notes.length());
    	} else {
    		Toast.makeText(this, "Cannot write to the external storage.", toast_period).show();
    	}
	}
	
	private boolean saveToFile(File file) {
		
		List<Message> messages = new ArrayList<Message>();
    	for (String folder : new String[] {"inbox", "sent"}) {
    		messages.addAll(getMessages(folder));
    	}
    	Collections.sort(messages);
    	
    	appendProgress(String.format("Find %d messages.", messages.size()));
    	
    	StringBuilder builder = new StringBuilder();
    	for (Message message : messages) {
    		builder.append(message);
    		builder.append("\n");
    	}
    	
		FileWriter writer = null;
		
		try {
			writer = new FileWriter(file);
			writer.write(builder.toString());
			appendProgress("Done @" + file.getAbsolutePath());
			return true;
		} catch(IOException e) {
			appendProgress(e.getMessage());
			return false;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					appendProgress(e.getMessage());
				}
			}
		}
	}
	
	private void sendFile(File file) {		
		String subject = this.getString(R.string.subject, 
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
		
		EditText emailEdit = (EditText)findViewById(R.id.email);
		String receiver = emailEdit.getText().toString();
		
		Intent openEmail = new Intent(Intent.ACTION_SEND);
		openEmail.setType("text/plain");
		openEmail.putExtra(Intent.EXTRA_SUBJECT, subject);
		openEmail.putExtra(Intent.EXTRA_EMAIL, new String[] {receiver});
		openEmail.putExtra(Intent.EXTRA_TEXT, notes.toString());
		openEmail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		
		startActivity(Intent.createChooser(openEmail, "send"));
	}
	
	private List<Message> getMessages(String category) {
    	List<Message> retval = new ArrayList<Message>();
    	
    	Uri folder = Uri.withAppendedPath(SMS_CONTENT_URI, category);
    	
    	Cursor cursor = getContentResolver().query(folder, 
				new String[] {"address", "date", "type", "body"}, 
				null, null, null);
    	
    	try {
    		if (cursor.moveToFirst()) {
    			do {
    				String address = cursor.getString(0);
    				String date = cursor.getString(1);
    				String type = cursor.getString(2);
    				String body = cursor.getString(3);
    				
    				String contact = getContact(address);
    				if (contact.length() > 0) {
    					address = contact;
    				}
    				
    				retval.add(new Message(address, date, body, type));
    			} while (cursor.moveToNext());
    		}
    	} finally {
    		cursor.close();
    	}
		
		return retval;
    }
	
	private Map<String, String> contacts = new HashMap<String, String>();
	
	private String getContact(String address) {
    	if (!contacts.containsKey(address)) {
    		String contact = "";
    		Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, address);
        	Cursor cursor = getContentResolver().query(phoneUri, 
        			new String[] {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME},
        			null, null, null);
        	try {
        		if (cursor.moveToFirst()) {
            		contact = cursor.getString(1);
            	}
        	} finally {
        		cursor.close();
        	}
        	contacts.put(address, contact);
    	}
    	return contacts.get(address);
    }
	
	private void appendProgress(String info) {
		notes.append(info);
		notes.append("\n");
	}
}