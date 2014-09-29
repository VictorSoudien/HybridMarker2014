package com.VictorZahraa.hybridmarker;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class ProfilePopUpHelper 
{
	private Context callerContext;
	private LayoutInflater inflater;
	
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
		final ListView messageDisplay = (ListView) loginView.findViewById(R.id.loginMessageDisplay);
		//messageDisplay.setVisibility(View.INVISIBLE);
		
		AlertDialog loginDialog = new AlertDialog.Builder(callerContext)
		.setTitle("User Options - " + ValueStoringHelperClass.USERNAME)
		.setView(loginView)
		.setPositiveButton("Log In", null)
		.setNegativeButton("Exit", null)
		.show();
	}
}
