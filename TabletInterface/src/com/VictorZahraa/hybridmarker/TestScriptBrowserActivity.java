package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.os.Build;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class TestScriptBrowserActivity extends Activity {

	private Context context;
	private Toast toast;
	
	private ExpandableListView exListView;
	private CustomExpandableListAdapter exListAdapter;
	private List<String> listHeaders;
	private HashMap<String, List<String>> listItems;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_script_browser);
		
		context = this;
		
		toast = Toast.makeText(context, "initialise", Toast.LENGTH_SHORT); // Initialise the toast but don't display this message
		new ServerConnect().execute();
		
		exListView = (ExpandableListView) findViewById(R.id.scriptListView);
		
		populateListView();
		
		exListAdapter = new CustomExpandableListAdapter(context, listHeaders, listItems);
		exListView.setAdapter(exListAdapter);
		
		/*if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
	}
	
	private void populateListView()
	{
		listHeaders = new ArrayList<String>();
		listItems = new HashMap<String, List<String>>();
		
		// Adding child data
		listHeaders.add("Top 250");
		listHeaders.add("Now Showing");
		listHeaders.add("Coming Soon..");
 
        // Adding child data
        List<String> top250 = new ArrayList<String>();
        top250.add("The Shawshank Redemption");
        top250.add("The Godfather");
        top250.add("The Godfather: Part II");
        top250.add("Pulp Fiction");
        top250.add("The Good, the Bad and the Ugly");
        top250.add("The Dark Knight");
        top250.add("12 Angry Men");
 
        List<String> nowShowing = new ArrayList<String>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");
        nowShowing.add("Turbo");
        nowShowing.add("Grown Ups 2");
        nowShowing.add("Red 2");
        nowShowing.add("The Wolverine");
 
        List<String> comingSoon = new ArrayList<String>();
        comingSoon.add("2 Guns");
        comingSoon.add("The Smurfs 2");
        comingSoon.add("The Spectacular Now");
        comingSoon.add("The Canyons");
        comingSoon.add("Europa Report");
 
        listItems.put(listHeaders.get(0), top250); // Header, Child data
        listItems.put(listHeaders.get(1), nowShowing);
        listItems.put(listHeaders.get(2), comingSoon);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_script_browser, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if (id == R.id.action_settings) 
		{
			return true;
		}
		else if (id == R.id.action_mark_pdf)
		{
			Intent pdfViewScreen = new Intent(TestScriptBrowserActivity.this, MainMarkingScreenActivity.class);
        	startActivity(pdfViewScreen);
		}
		return super.onOptionsItemSelected(item);
	}
	
	// Displays a toast containing the specified message
	private void displayToast(String message)
	{
		if (toast == null)
		{
			toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		}
		else
		{
			toast.setText(message);
		}
		
		toast.show();
	}
	
	private class ServerConnect extends AsyncTask<URL, Integer, Long>
	{
		@Override
		protected Long doInBackground(URL... params) 
		{
			connectToServer();
			return null;
		}
		
		// Connect to the server in order to download the memo content
		public void connectToServer()
		{
			try
			{
				JSch jsch = new JSch();
				Session session = jsch.getSession("vsoudien", "nightmare.cs.uct.ac.za");
				session.setPassword("compsci2");
				
				Properties connProps = new Properties();
				connProps.put("StrictHostKeyChecking", "no");
				session.setConfig(connProps);
				
				session.connect();
				displayToast("Successfully connected to nightmare");
			}
			catch (Exception e)
			{
				displayToast("Error while connecting to nightmare\n" + e.getMessage());
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
			View rootView = inflater.inflate(
					R.layout.fragment_test_script_browser, container, false);
			return rootView;
		}
	}*/

}
