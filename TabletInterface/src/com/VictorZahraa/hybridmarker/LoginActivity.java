package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends Activity {

	private TextView informationTextView;
	private CalendarView deadlineCal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		informationTextView = (TextView) findViewById(R.id.remaining_marking_text_view );
		informationTextView.setText("CSC1010H\nClass Test 2");
		
		deadlineCal = (CalendarView) findViewById(R.id.deadline_calendar);
		deadlineCal.setFirstDayOfWeek(Calendar.MONDAY);

		AsyncTask<String, Integer, String> s = new PHPCommunication().execute("username", "password");
		
		try {
			String result = s.get();
			new AlertDialog.Builder(this)
		    .setTitle("Server Result")
		    .setMessage(result)
		    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            
		        }
		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            // Do nothing.
		        }
		    }).show();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class PHPCommunication extends AsyncTask<String, Integer, String>
	{
		@Override
		protected String doInBackground(String... params) 
		{
			// TODO Auto-generated method stub
			
			return loginToServer(params[0], params[1]);
			
			//return null;
		}
		
		public String loginToServer(String username, String password)
		{
			String link = "http://people.cs.uct.ac.za/~vsoudien/login.php";
			
			try
			{
				String data  = URLEncoder.encode("operation", "UTF-8") + "=" + URLEncoder.encode("select", "UTF-8");
				data += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
	            data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");
				
				//String data  = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode("Victor", "UTF-8");
	            
	            URL url = new URL(link);
	            URLConnection urlConn = url.openConnection();
	            urlConn.setDoOutput(true);
	            
	            OutputStreamWriter outWriter = new OutputStreamWriter(urlConn.getOutputStream()); 
	            outWriter.write(data); 
	            outWriter.flush();
	            
	            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                String result = "";
                String line = null;
                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                   result += line;
                   break;
                }
                
                return result;
			}
			catch (Exception e)
			{
				// Handle exception
				return "Error";
			}
		}
	}
	
	/*
	/**
	 * A placeholder fragment containing a simple view.
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_login,
					container, false);
			return rootView;
		}
	}*/
}
