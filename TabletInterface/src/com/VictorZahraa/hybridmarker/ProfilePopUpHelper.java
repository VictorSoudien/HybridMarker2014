package com.VictorZahraa.hybridmarker;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
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
		final ListView optionsList = (ListView) loginView.findViewById(R.id.optionsList);
		
		ArrayList<String> options = new ArrayList<String>();
		options.add("Train Gesture Recognition");
		options.add("Logout");
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				callerContext, 
				android.R.layout.simple_list_item_1,
				options);

		optionsList.setAdapter(arrayAdapter);
		
		AlertDialog optionsDialog = new AlertDialog.Builder(callerContext)
		.setTitle("User Options - " + ValueStoringHelperClass.USERNAME)
		.setView(loginView)
		.setPositiveButton("Back", null)
		.show();
	}
}
