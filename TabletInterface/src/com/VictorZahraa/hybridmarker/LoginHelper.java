package com.VictorZahraa.hybridmarker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginHelper 
{
	public static class PositiveLoginButtonClicked implements View.OnClickListener
	{
		private final Dialog dialog;
		private final View loginView;
		private final EditText usernameField;
		private final EditText passwordField;
		private final TextView messageDisplay;

		private Handler loginHandler;

		@SuppressWarnings("unused")
		private TestScriptBrowserActivity callingActivity;

		public PositiveLoginButtonClicked(Dialog dia, View lView, TestScriptBrowserActivity caller, Handler handler)
		{
			dialog = dia;
			loginView = lView;
			callingActivity = caller;

			loginHandler = handler;

			usernameField = (EditText) loginView.findViewById(R.id.usernameInput);
			passwordField = (EditText) loginView.findViewById(R.id.passwordInput);
			messageDisplay = (TextView) loginView.findViewById(R.id.loginMessageDisplay);
		}

		@Override
		public void onClick(View v) 
		{
			String username = usernameField.getText().toString();
			String password = passwordField.getText().toString();

			messageDisplay.setVisibility(View.INVISIBLE);

			if ((username.length() == 0) || (password.length() == 0))
			{
				messageDisplay.setText("Please enter a username and password");
				messageDisplay.setVisibility(View.VISIBLE);
			}
			else
			{
				try
				{
					String response = new PHPCommunication().execute("Login", username, password).get();
					response  = "1"; // REMOVE AFTER TESTING
					if (response.equals("1"))
					{
						ValueStoringHelperClass.loggedIn = true;
						ValueStoringHelperClass.USERNAME = username;

						loginHandler.sendEmptyMessage(0);

						dialog.dismiss();
					}
					else
					{
						if (response.equals("0"))
						{
							messageDisplay.setText("Invalid username or password");
						}
						else
						{
							messageDisplay.setText(response);
						}

						messageDisplay.setVisibility(View.VISIBLE);
					}
				}
				catch (Exception e)
				{
					messageDisplay.setText(e.getMessage());
					messageDisplay.setVisibility(View.VISIBLE);
				}
			}
		}

		// Used to get information from the DB
		private class PHPCommunication extends AsyncTask<String, Integer, String>
		{
			@Override
			protected String doInBackground(String... params) 
			{
				if (params[0].equalsIgnoreCase("Login"))
				{
					return loginToServer(params[1], params[2]);
				}

				return "ERROR";
			}

			public String loginToServer(String username, String password)
			{
				String link = R.string.base_URL + "/loginCheck.php?q=";

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
					return "Please check your internet connection";
				}
			}
		}
	}

	public static class PositiveLogoutButtonClicked implements View.OnClickListener
	{

		@Override
		public void onClick(View v) 
		{
			new PHPCommunication().execute();
		}
		
		// Used to get information from the DB
		private class PHPCommunication extends AsyncTask<String, Integer, String>
		{
			@Override
			protected String doInBackground(String... params) 
			{
				return logoutOfServer();
			}

			public String logoutOfServer()
			{
				String link = R.string.base_URL + "/Logout.php?q=";

				try
				{	
					link += ValueStoringHelperClass.USERNAME;

					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet();
					request.setURI(new URI(link));

					// Execute the php file
					HttpResponse response = client.execute(request);
					
					return "Success";
				}
				catch (Exception e)
				{
					// Handle exception
					return "An error has occured while logging out. Please close this message and try again";
				}
			}
		}
	}

	// Handle the pressing of the cancel button
	public static class NegativeButtonClicked implements View.OnClickListener
	{
		TestScriptBrowserActivity controller;

		public NegativeButtonClicked(TestScriptBrowserActivity activity)
		{
			controller = activity;
		}

		@Override
		public void onClick(View v) 
		{
			controller.endApplication();
		}
	}
}
