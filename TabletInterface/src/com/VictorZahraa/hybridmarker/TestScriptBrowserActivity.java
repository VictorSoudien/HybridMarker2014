package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

import com.VictorZahraa.hybridmarker.LoginHelper.PositiveLoginButtonClicked;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class TestScriptBrowserActivity extends Activity {

	private Context context;
	private Toast toast;
	private ActionBar actionBar;
	private TextView instructionText;
	
	private ProgressBar listUpdateProgressBar;
	
	private ExpandableListView exListView;
	private CustomExpandableListAdapter exListAdapter;
	private List<String> listHeaders;
	private HashMap<String, List<String>> listItems;
	
	private String [] drawerItems;
	private DrawerLayout drawerLayout;
	private ListView drawerListView;
	private ArrayAdapter<String> navDrawArrayAdapter;
	private ActionBarDrawerToggle drawerToggle;
	
	// Stores which item in the drawer is currently selected
	private String selectedItemInDrawer;
	
	// Used top determine whether a refresh is currently being performed
	private boolean viewBeingRefreshed;
	
	// Allows for values to be stored and accessed across activities
	private ValueStoringHelperClass valueStore;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_browser_drawer_layout);
		
		context = this;
		actionBar = this.getActionBar();
		selectedItemInDrawer = "";
		viewBeingRefreshed = false;
		instructionText = (TextView) findViewById(R.id.instructionText);
		
		valueStore = new ValueStoringHelperClass();
		
		listUpdateProgressBar = (ProgressBar) findViewById(R.id.list_update_progress_bar);
		
		toast = Toast.makeText(context, "initialise", Toast.LENGTH_SHORT); // Initialise the toast but don't display this message
		
		//new LoginToServer().execute("username", "password");
		
		//if (login() == true)
		{
			initExpandableListView();
			initNavDrawer();

			new ServerConnect().execute("Update Nav Drawer");
		}
		
		/*if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
	}
	
	@Override
	public void onBackPressed()
	{
		// Handle the pressing of the 'physical' back button
	}
	
	// Close the application
	public void endApplication()
	{
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	
	// Displays the login dialog
	/*private boolean login()
	{
		final boolean returnValue = true;
		
		LayoutInflater inflater = this.getLayoutInflater();
		
		final View loginView = inflater.inflate(R.layout.login_layout, null);
		final TextView messageDisplay = (TextView) loginView.findViewById(R.id.loginMessageDisplay);
		messageDisplay.setVisibility(View.INVISIBLE);

		AlertDialog loginDialog = new AlertDialog.Builder(context)
	    .setTitle("Login")
	    .setView(loginView)
	    .setPositiveButton("Log In", null)
	    .setNegativeButton("Exit", null)
	    .show();
		
		// Add a listener to the positive button
		Button positiveButton = loginDialog.getButton(Dialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(new LoginHelper.PositiveLoginButtonClicked(loginDialog, loginView, this));
		
		// Add a listener to the negative button
		Button negativeButton = loginDialog.getButton(Dialog.BUTTON_NEGATIVE);
		negativeButton.setOnClickListener(new LoginHelper.NegativeButtonClicked(this));
		
		return returnValue;
	}*/
	
	// Sets up the expandable list view used to display tests for each course
	private void initExpandableListView()
	{
		exListView = (ExpandableListView) findViewById(R.id.scriptListView);
		
		listHeaders = new ArrayList<String>();
		listItems = new HashMap<String, List<String>>();
		
		// Initialise with empty lists so that it can be modified by the AsyncTask
		exListAdapter = new CustomExpandableListAdapter(context, listHeaders, listItems);
		
		exListView.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				String tempTestName = listItems.get(listHeaders.get(groupPosition)).get(childPosition);
				tempTestName = tempTestName.replace("*", "\\*");
				
				String fileDirectory = "Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(groupPosition) + "/" + tempTestName + "/";
				String memoDirectory = "Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(groupPosition) + "/memo.txt";
				String ansPerPageDirectory = "Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(groupPosition) + "/answersPerPage.txt";
				
				// Store the current directory and test name
				valueStore.setCurrentDirectory("Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(groupPosition) + "/");
				valueStore.setTestName(tempTestName);
				
				new ServerConnect().execute("Download Files", fileDirectory, memoDirectory, ansPerPageDirectory);
				
				return false;
			}
		});
	}
	
	// Sets up the navigation drawer
	private void initNavDrawer()
	{
		drawerItems = new String [] {};
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerListView = (ListView) findViewById(R.id.left_drawer);
		
		navDrawArrayAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItems);
		
		// Set the adapter for the list view
		drawerListView.setAdapter(navDrawArrayAdapter);
		
		drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) 
			{
				// Remove the instruction text
				instructionText.setVisibility(View.INVISIBLE);
				
				drawerListView.setItemChecked(position, true);
				drawerLayout.closeDrawer(drawerListView);
				getActionBar().setTitle(drawerItems[position]);
				
				selectedItemInDrawer = drawerItems[position];
				
				new ServerConnect().execute("Update Lists", drawerItems[position]);
			}
		});
		
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 
				R.drawable.ic_drawer, R.string.nav_drawer_open, R.string.app_name)
		{
			/** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
        		super.onDrawerOpened(drawerView);
        		invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
		};
		
		drawerLayout.setDrawerListener(drawerToggle);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
	}
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
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
		
		if (drawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		if (id == R.id.action_settings) 
		{
			return true;
		}
		else if ((id == R.id.action_refresh)  && (viewBeingRefreshed == false))
		{
			// Navigation drawer is updated if it is open
			if (drawerLayout.isDrawerOpen(drawerListView) == true)
			{
				new ServerConnect().execute("Update Nav Drawer");
			}
			else if (selectedItemInDrawer != "") // else the expandable list view is updated
			{
				new ServerConnect().execute("Update Lists", selectedItemInDrawer);
			}
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
	
	
	
	private class ServerConnect extends AsyncTask<String, String, Long>
	{
		JSch jsch;
		Session sshSession;
		
		String operationBeingPerformed;
		
		@Override
		protected Long doInBackground(String... params) 
		{
			if (params.length != 0)
			{
				if (params[0].equalsIgnoreCase("Update Nav Drawer"))
				{
					operationBeingPerformed = "Update Nav Drawer";
					connectToServer();
					populateNavDrawer();
				}
				if (params[0].equalsIgnoreCase("Update Lists"))
				{
					operationBeingPerformed = "Update Lists";
					
					connectToServer();
					populateLists(params[1]);
				}
				else if (params[0].equalsIgnoreCase("Request File List"))
				{
					operationBeingPerformed = "Request File List";
					
					connectToServer();
					displayToast(executeCommandOnServer(params[1]));
				}
				else if (params[0].equalsIgnoreCase("Download Files"))
				{
					operationBeingPerformed = "Download Files";
					connectToServer();
					downloadFiles(params[1], params[2], params[3]);
				}	
			}
			
			return null;
		}
		
		// Connect to the server in order to download the memo content
		public void connectToServer()
		{
			try
			{
				jsch = new JSch();
				sshSession = jsch.getSession("zmathews", "nightmare.cs.uct.ac.za");
				sshSession.setPassword("800hazhtM");
				
				Properties connProps = new Properties();
				connProps.put("StrictHostKeyChecking", "no");
				sshSession.setConfig(connProps);
				
				sshSession.connect();
			}
			catch (Exception e)
			{
				displayToast("Error while connecting to nightmare\n" + e.getMessage());
			}
		}
		
		// Execute the given command on the server
		private String executeCommandOnServer(String command)
		{
			String result = "";
			
			try
			{
				// Create a communication channel with the server and execute the command
				Channel commChannel = sshSession.openChannel("exec");
				((ChannelExec)commChannel).setCommand(command);
				commChannel.setInputStream(null);
				
				InputStream inStream = commChannel.getInputStream();
				commChannel.connect();
				
				int readValue;
				
				while ((readValue = inStream.read()) != -1)
				{
					result += Character.toString((char) readValue);
				}
				
				// Close the communication channel
				commChannel.disconnect();
			}
			catch (Exception e)
			{
				displayToast("ERROR: Could not execute command ( "  + command + " ) on server\n" + e.getMessage());
			}
			
			return result;
		}
		
		// Populate the expandable list layout
		private void populateLists(String courseCode)
		{	
			String listOfTests = executeCommandOnServer("cd Honours_Project/" + courseCode + "/ && ls");
			String [] tests = listOfTests.split("\n");
			
			listHeaders.clear();
			
			// Populate group list
			for (String testName : tests)
			{
				listHeaders.add(testName);
				
				if (listItems.get(testName) != null)
				{
					listItems.get(testName).clear();
				}
				
				List<String> temp = new ArrayList<String>();
				
				String listOfScripts = executeCommandOnServer("cd Honours_Project/" + courseCode + "/" + testName + " && ls");
				String [] scripts = listOfScripts.split("\n");
				
				// Populate the sublist for this category
				for (String script : scripts)
				{
					temp.add(script);
				}
				
				listItems.put(testName, temp);
			}
		}
		
		// Update the options available in the navigation drawer
		private void populateNavDrawer()
		{
			String listOfCourses = executeCommandOnServer("cd Honours_Project && ls");
			drawerItems = listOfCourses.split("\n");
		}
		
		// Download the images needed for each test from the server
		private void downloadFiles(String directory, String memoDir, String ansPerPageDir)
		{	
	        // Get the path to external storage
	        String pathToSDCard = Environment.getExternalStorageDirectory().getPath();
			
			try
			{
				String filenames = executeCommandOnServer("cd " + directory + " && ls");
				//displayToast(directory);
				String [] files = filenames.split("\n");
				
				Channel commChannel = sshSession.openChannel("sftp");
				commChannel.connect();
				ChannelSftp sftpChannel = (ChannelSftp) commChannel;
				int numPages = 0;
				
				ValueStoringHelperClass.isRemark = false;
				
				valueStore.initPageCollection();
				
				for (String file : files)
				{
					file = file.trim();
					
					if (file.contains("Marked"))
					{
						ValueStoringHelperClass.isRemark = true;
					}
					
					if (file.startsWith("page") && file.contains(".png"))
					{
						String saveDir = pathToSDCard + "/" + file;
						String fileDir = directory + file;
						
						sftpChannel.get(fileDir, saveDir);
						numPages++;
						boolean saved = valueStore.addPage(numPages);
						publishProgress(file + " ... File Downloaded   " + saved);
					}
				}
				
				// Download the memo for this test
				String saveDir = pathToSDCard + "/" + "memo.txt";
				
				sftpChannel.get(memoDir, saveDir);
				//valueStore.setMemoText("memo.txt");
				publishProgress("Memo Downloaded");
				
				// Download the answersPerPageFile for this test
				saveDir = pathToSDCard + "/" + "answersPerPage.txt";
				
				sftpChannel.get(ansPerPageDir, saveDir);
				//valueStore.setMemoText("ansPerPageDir.txt");
				publishProgress("AnswersPerPage Downloaded");
				
				valueStore.processMemoText("memo.txt", "answersPerPage.txt");
				valueStore.setNumPages(numPages);
				//displayToast("Downloaded files from " + directory);
			}
			catch (Exception e)
			{
				displayToast("Error: Could not download file \n" + e);
			}
		}
		
		protected void onProgressUpdate(String... message) 
		{
	         displayToast(message[0]);
	    }
		
		@Override
		protected void onPreExecute()
		{
			// Display visual feedback for loading
			listUpdateProgressBar.setVisibility(View.VISIBLE);
			exListView.setBackgroundColor(Color.GRAY);
			exListView.setEnabled(false);
			
			viewBeingRefreshed = true;
		}
		
		@Override
		protected void onPostExecute(Long params)
		{
			exListAdapter = new CustomExpandableListAdapter(context, listHeaders, listItems);
			exListView.setAdapter(exListAdapter);
			
			listUpdateProgressBar.setVisibility(View.INVISIBLE);
			exListView.setBackgroundColor(Color.WHITE);
			exListView.setEnabled(true);
			
			if (operationBeingPerformed.equalsIgnoreCase("Update Nav Drawer"))
			{
				navDrawArrayAdapter =  new ArrayAdapter<String>(context, R.layout.drawer_list_item, drawerItems);
				drawerListView.setAdapter(navDrawArrayAdapter);
			}
			
			viewBeingRefreshed = false;
			
			if (operationBeingPerformed.equals("Download Files"))
			{
				Intent pdfViewScreen = new Intent(TestScriptBrowserActivity.this, MainMarkingScreenActivity.class);
	        	startActivity(pdfViewScreen);
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
