package com.VictorZahraa.hybridmarker;

import java.net.URI;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ProfilePopUpHelper 
{
	private Context callerContext;
	private LayoutInflater inflater;
	private final ArrayList<String> options = new ArrayList<String>();

	public ProfilePopUpHelper(Context context, LayoutInflater layoutInflater)
	{
		callerContext = context;
		inflater = layoutInflater;

		showPopUp();
	}

	// Displays the list of options available to the user
	private void showPopUp()
	{			
		final View loginView = inflater.inflate(R.layout.profile_popup_layout, null);
		final ListView optionsList = (ListView) loginView.findViewById(R.id.optionsList);

		options.add("Logout");

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				callerContext, 
				android.R.layout.simple_list_item_1,
				options);

		optionsList.setAdapter(arrayAdapter);
		optionsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) 
			{
				if (options.get(position).equalsIgnoreCase("Logout"))
				{
					new PHPCommunication().execute("Logout");
				}
			}
		});

		@SuppressWarnings("unused")
		AlertDialog optionsDialog = new AlertDialog.Builder(callerContext)
		.setTitle("User Options - " + ValueStoringHelperClass.USERNAME)
		.setView(loginView)
		.setPositiveButton("Back", null)
		.show();
	}

	// Used to get information from the DB
	private class PHPCommunication extends AsyncTask<String, String, String>
	{	
		private boolean successfulLogout = false;

		@Override
		protected String doInBackground(String... params) 
		{
			if (params[0].equalsIgnoreCase("Logout"))
			{
				return logoutOfServer();
			}

			return "ERROR";
		}

		public String logoutOfServer()
		{
			String link = callerContext.getText(R.string.base_URL) + "/Logout.php?q=";

			try
			{	
				link += ValueStoringHelperClass.USERNAME;

				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(link));

				client.execute(request);

				successfulLogout = true;
				return "Success";
			}
			catch (Exception e)
			{
				// Handle exception
				return "Please check your internet connection";
			}
		}

		@Override
		public void onPostExecute(String params)
		{
			if (successfulLogout == true)
			{
				try
				{
					ValueStoringHelperClass valueStore = new ValueStoringHelperClass();
					valueStore.recycleBitmaps();
				}
				catch (NullPointerException e)
				{
					// Ignore since this is simply the case if the user logs out without having marked anything
				}
				
				ValueStoringHelperClass.loggedIn = false;
				ValueStoringHelperClass.USERNAME = "";

				// Restart the application
				Intent i = ((Activity) callerContext).getBaseContext().getPackageManager()
						.getLaunchIntentForPackage( ((Activity) callerContext).getBaseContext().getPackageName() );
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				callerContext.startActivity(i);
				
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				callerContext.startActivity(intent);
			}
			else
			{
				@SuppressWarnings("unused")
				AlertDialog optionsDialog = new AlertDialog.Builder(callerContext)
				.setTitle("Logout Error")
				.setMessage("Unable to logout.\nPlease check your internet connection")
				.setPositiveButton("Continue", null)
				.show();
			}
		}
	}
}
