package com.VictorZahraa.hybridmarker;

import android.app.Dialog;
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
		
		private TestScriptBrowserActivity callingActivity;
		
		public PositiveLoginButtonClicked(Dialog dia, View lView, TestScriptBrowserActivity caller)
		{
			dialog = dia;
			loginView = lView;
			callingActivity = caller;
			
			usernameField = (EditText) loginView.findViewById(R.id.usernameInput);
			passwordField = (EditText) loginView.findViewById(R.id.passwordInput);
			messageDisplay = (TextView) loginView.findViewById(R.id.loginMessageDisplay);
		}
		
		@Override
		public void onClick(View v) 
		{
			String username = usernameField.getText().toString();
			String password = passwordField.getText().toString();
			
			if ((username.length() == 0) || (password.length() == 0))
			{
				messageDisplay.setText("Please enter a username and password");
				messageDisplay.setVisibility(View.VISIBLE);
			}
			else
			{
				//checkCredentials(username, password);
				//controller.
				
				dialog.dismiss();
			}
		}
	}
	
	
	
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
