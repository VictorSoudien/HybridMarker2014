package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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

		AsyncTask<String, Integer, String> s = new PHPCommunication().execute("ADMINS001", "hello");
		
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
			return loginToServer(params[0], params[1]);
		}
		
		public String loginToServer(String username, String password)
		{
			String link = "http://people.cs.uct.ac.za/~vsoudien/Test/loginCheck.php?q=";
			
			try
			{	
				link += username + "+" + password;
				
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(link));
				
				HttpResponse response = client.execute(request);
				BufferedReader in = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
				
				String result = "";
                String line = null;
                // Read Server Response
                while((line = in.readLine()) != null)
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
}
