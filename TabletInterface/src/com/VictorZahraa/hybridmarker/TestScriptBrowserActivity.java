package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class TestScriptBrowserActivity extends Activity {

	private Context context;
	private ActionBar actionBar;
	private TextView instructionText;
	
	private MenuItem userDisplay;

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

	// Used as a flag when files are being downloaded
	private boolean downloadingFiles;

	// Stores which item in the drawer is currently selected
	private String selectedItemInDrawer;

	// Used top determine whether a refresh is currently being performed
	private boolean viewBeingRefreshed;

	// Allows for values to be stored and accessed across activities
	private ValueStoringHelperClass valueStore;
	
	// Used to wait for login to complete before initiliase other view elements
	private Handler loginHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_browser_drawer_layout);

		context = this;
		actionBar = this.getActionBar();
		selectedItemInDrawer = "";
		viewBeingRefreshed = false;
		downloadingFiles = false;

		loginHandler = new Handler(){
			@Override
		    public void handleMessage(Message msg) {
		        switch (msg.what) {
		        case 0:
		        	instructionText.setVisibility(View.VISIBLE);
		        	listUpdateProgressBar.setVisibility(View.INVISIBLE);
		        	
		        	invalidateOptionsMenu();
		        	
		    		new ServerConnect().execute("Update Nav Drawer");
		    		
		            break;
		        default:
		            break;
		        }
		    }
		};
		
		if (ValueStoringHelperClass.loggedIn == false)
		{
			userLogin(loginHandler);
		}
		else
		{
			loginHandler.sendEmptyMessage(0);
		}
		
	  	instructionText = (TextView) findViewById(R.id.instructionText);
	  	instructionText.setVisibility(View.INVISIBLE);
		valueStore = new ValueStoringHelperClass();
		listUpdateProgressBar = (ProgressBar) findViewById(R.id.list_update_progress_bar);
		listUpdateProgressBar.setVisibility(View.INVISIBLE);

		initExpandableListView();
		initNavDrawer();
		
		/*if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
	}

	@Override
	public void onBackPressed()
	{
		// Handle the pressing of the 'physical' back button
		endApplication();
	}

	// Close the application
	public void endApplication()
	{
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	// Presents the user with the login dialog and handles other login activities
	private void userLogin(Handler handler)
	{
		LayoutInflater inflater = this.getLayoutInflater();

		final View loginView = inflater.inflate(R.layout.login_layout, null);
		final TextView messageDisplay = (TextView) loginView.findViewById(R.id.loginMessageDisplay);
		messageDisplay.setVisibility(View.INVISIBLE);

		AlertDialog loginDialog = new AlertDialog.Builder(context)
		.setTitle("Login")
		.setView(loginView)
		.setPositiveButton("Log In", null)
		.setNegativeButton("Exit", null)
		.setCancelable(false)
		.show();

		// Add a listener to the positive button
		Button positiveButton = loginDialog.getButton(Dialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(new LoginHelper.PositiveLoginButtonClicked(loginDialog, loginView, this, loginHandler));

		// Add a listener to the negative button
		Button negativeButton = loginDialog.getButton(Dialog.BUTTON_NEGATIVE);
		negativeButton.setOnClickListener(new LoginHelper.NegativeButtonClicked(this));
	}

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
				
				final int gPos = groupPosition;
				final int cPos = childPosition;
				
				// Display a list of options
				final ArrayList<String> options = new ArrayList<String>();
				View optionsView = getLayoutInflater().inflate(R.layout.profile_popup_layout, null);
				ListView optionsList = (ListView) optionsView.findViewById(R.id.optionsList);
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
				.setTitle("Script Options - " + listItems.get(listHeaders.get(groupPosition)).get(childPosition))
				.setView(optionsView)
				.setPositiveButton("Back", null);
				
				if (ValueStoringHelperClass.LOCKED_TESTS.contains(listItems.get(listHeaders.get(groupPosition)).get(childPosition)))
				{
					AlertDialog lockedScriptDialog = new AlertDialog.Builder(context)
					.setTitle("Script Options - " + listItems.get(listHeaders.get(groupPosition)).get(childPosition))
					.setMessage("This script is currently being marked.")
					.setPositiveButton("Ok", null)
					.show();
					return false;
				}
				
				final AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();

				options.add("Mark Script");
				options.add("View Marked Version");
				options.add("View Unprocessed File");

				ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
						context, 
						android.R.layout.simple_list_item_1,
						options);

				optionsList.setAdapter(arrayAdapter);
				optionsList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) 
					{
						if (options.get(position).equalsIgnoreCase("Mark Script"))
						{		
							if (alertDialog != null)
							{
								alertDialog.dismiss();
							}
							
							String tempTestName = listItems.get(listHeaders.get(gPos)).get(cPos);
							tempTestName += "+";
							
							ValueStoringHelperClass.TEST_NAME = listHeaders.get(gPos).replaceAll("_", " ").trim();

							String fileDirectory = "/home/zmathews/Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(gPos).replaceAll(" ", "_") + "/" + tempTestName + "/";
							String memoDirectory = "/home/zmathews/Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(gPos).replaceAll(" ", "_") + "/memo.txt";
							String ansPerPageDirectory = "/home/zmathews/Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(gPos).replaceAll(" ", "_") + "/answersPerPage.txt";

							// Store the current directory and test name
							valueStore.setCurrentDirectory("/home/zmathews/Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(gPos).replaceAll(" ", "_") + "/");
							valueStore.setTestName(tempTestName);

							downloadingFiles = true;
							new ServerConnect().execute("Download Files", fileDirectory, memoDirectory, ansPerPageDirectory);
						}
					}
				});
				
//				String tempTestName = listItems.get(listHeaders.get(groupPosition)).get(childPosition);
//				//tempTestName = tempTestName.replace("*", "\\*");
//				tempTestName += "+";
//				
//				ValueStoringHelperClass.TEST_NAME = listHeaders.get(groupPosition).replaceAll("_", " ").trim();
//
//				String fileDirectory = "/home/zmathews/Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(groupPosition).replaceAll(" ", "_") + "/" + tempTestName + "/";
//				String memoDirectory = "/home/zmathews/Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(groupPosition).replaceAll(" ", "_") + "/memo.txt";
//				String ansPerPageDirectory = "/home/zmathews/Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(groupPosition).replaceAll(" ", "_") + "/answersPerPage.txt";
//
//				// Store the current directory and test name
//				valueStore.setCurrentDirectory("/home/zmathews/Honours_Project/" + selectedItemInDrawer + "/" + listHeaders.get(groupPosition).replaceAll(" ", "_") + "/");
//				valueStore.setTestName(tempTestName);
//
//				downloadingFiles = true;
//				new ServerConnect().execute("Download Files", fileDirectory, memoDirectory, ansPerPageDirectory);

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
				
				ValueStoringHelperClass.COURSE_NAME = drawerItems[position];

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
		
		if (ValueStoringHelperClass.loggedIn == true)
		{
			MenuItem userDisplay = menu.findItem(R.id.user_display);
			userDisplay.setTitle(ValueStoringHelperClass.USERNAME);
		}
		
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
		else if (id == R.id.user_display)
		{
			new ProfilePopUpHelper(context, this.getLayoutInflater());
		}
		return super.onOptionsItemSelected(item);
	}

	// Connect to the server and execute various tasks
	private class ServerConnect extends AsyncTask<String, String, Long>
	{
		JSch jsch;
		Session sshSession;

		String operationBeingPerformed;

		private ProgressDialog progressDialog;
		private boolean downloadSuccess = false;

		@Override
		protected Long doInBackground(String... params) 
		{
			if (params.length != 0)
			{
				if (params[0].equalsIgnoreCase("Update Nav Drawer"))
				{
					operationBeingPerformed = "Update Nav Drawer";

					if (connectToServer() == true)
					{
						populateNavDrawer();
					}
				}
				if (params[0].equalsIgnoreCase("Update Lists"))
				{
					operationBeingPerformed = "Update Lists";

					if (connectToServer() == true)
					{
						populateLists(params[1]);
					}
				}
				else if (params[0].equalsIgnoreCase("Request File List"))
				{
					operationBeingPerformed = "Request File List";

					connectToServer();
				}
				else if (params[0].equalsIgnoreCase("Download Files"))
				{
					operationBeingPerformed = "Download Files";

					if (connectToServer() == true)
					{
						downloadFiles(params[1], params[2], params[3]);
					}
				}	
			}

			return null;
		}

		// Connect to the server in order to download the memo content
		public boolean connectToServer()
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

				return true;
			}
			catch (Exception e)
			{
				publishProgress("An error has occured: Please check your network connection");

				return false;
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
				publishProgress("An error has occured: Please check your network connection");
			}

			return result;
		}

		// Populate the expandable list layout
		private void populateLists(String courseCode)
		{	
			controlFileLock("Select", courseCode, "", "");
			
			String listOfTests = executeCommandOnServer("cd Honours_Project/" + courseCode + "/ && ls");
			String [] tests = listOfTests.split("\n");

			listHeaders.clear();

			// Populate group list
			for (String testName : tests)
			{
				listHeaders.add(testName.replaceAll("_", " "));

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
					// Do not show the metadata files to the user
					if (script.endsWith(".txt") == false)
					{
						temp.add(script.replaceAll("\\+", ""));
					}
				}

				listItems.put(testName.replaceAll("_",  " "), temp);
			}
		}

		// Update the options available in the navigation drawer
		private void populateNavDrawer()
		{
			String listOfCourses = executeCommandOnServer("cd Honours_Project && ls");
			
			if (ValueStoringHelperClass.CAN_VIEW_ALL_COURSES == true)
			{
				String [] lsOut = listOfCourses.split("\n");
				ArrayList<String> courses = new ArrayList<String>();
				
				for (String course : lsOut)
				{
					if (course.trim().length() == 8)
					{
						courses.add(course);
					}
				}
				
				drawerItems = new String [courses.size()];
				for (int i = 0; i < courses.size(); i++)
				{
					drawerItems[i] = courses.get(i);
				}
			}
			else
			{
				int numCourses = ValueStoringHelperClass.VIEWABLE_COURSES.size();
				drawerItems = new String [numCourses];
				
				for (int i  = 0; i < numCourses; i++)
				{
					drawerItems[i] = ValueStoringHelperClass.VIEWABLE_COURSES.get(i);
				}
			}
		}

		// Download the images needed for each test from the server
		private void downloadFiles(String directory, String memoDir, String ansPerPageDir)
		{	
			String testName = directory.split("/")[directory.split("/").length - 1].replaceAll("\\+", "");
			
			// Ensure that the script is not being marked
			controlFileLock("Select", ValueStoringHelperClass.COURSE_NAME, "", "");
			
			if (ValueStoringHelperClass.LOCKED_TESTS.contains(testName.trim()) == false)
			{
				// Lock the file
				controlFileLock("Insert", ValueStoringHelperClass.COURSE_NAME, ValueStoringHelperClass.TEST_NAME.trim(), testName);
			}
			else
			{
				downloadSuccess = false;
				populateLists(ValueStoringHelperClass.COURSE_NAME);
				return;
			}
			
			ValueStoringHelperClass.ORIGINAL_FOLDER_NAME = testName;
			
			// Get the path to external storage
			String pathToSDCard = Environment.getExternalStorageDirectory().getPath();

			try
			{
				String filenames = executeCommandOnServer("cd " + directory + " && ls");
				String [] files = filenames.split("\n");

				Channel commChannel = sshSession.openChannel("sftp");
				commChannel.connect();
				ChannelSftp sftpChannel = (ChannelSftp) commChannel;
				int numPages = 0;

				ValueStoringHelperClass.isRemark = false;
				valueStore.setFlagText("");

				valueStore.initPageCollection();

				for (String file : files)
				{
					file = file.trim();

					// Ignore previously marked image files
					if (file.contains("Marked") || file.contains("saved"))
					{
						ValueStoringHelperClass.isRemark = true;
					}

					if (file.startsWith("page") && file.contains(".png"))
					{
						String saveDir = pathToSDCard + "/" + file;
						String fileDir = directory + file;

						sftpChannel.get(fileDir, saveDir);
						numPages++;
						valueStore.addPage(numPages);
					}
				}

				// Download the memo for this test
				String saveDir = pathToSDCard + "/" + "memo.txt";

				sftpChannel.get(memoDir, saveDir);

				// Download the answersPerPageFile for this test
				saveDir = pathToSDCard + "/" + "answersPerPage.txt";

				sftpChannel.get(ansPerPageDir, saveDir);

				valueStore.processMemoText("memo.txt", "answersPerPage.txt");
				valueStore.setNumPages(numPages);
				
				// Populate the list of students
				String link = getText(R.string.base_URL) + "/getListOfStudents.php?";
				link += "Course=" + ValueStoringHelperClass.COURSE_NAME;

				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(link));

				// Execute the php file
				HttpResponse response = client.execute(request);
				BufferedReader in = new BufferedReader
						(new InputStreamReader(response.getEntity().getContent()));

				StringBuffer sb = new StringBuffer("");
				String line="";
				while ((line = in.readLine()) != null) {
					sb.append(line);
					break;
				}
				in.close();
				
				String out = sb.toString();
				String [] tempArray = out.split("<br>");
				ArrayList<String> students = new ArrayList<String>();
				
				for (String stu : tempArray)
				{
					students.add(stu);
				}
				
				ValueStoringHelperClass.STUDENTS_LIST = (ArrayList<String>) students.clone();
				
				downloadSuccess = true;
			}
			catch (Exception e)
			{
				publishProgress(/*"An error has occured: Please check your network connection" + */e.getMessage());
			}
		}
		
		public void controlFileLock (String op, String course, String test, String name)
		{
			String link = getText(R.string.base_URL) + "/controlFileLock.php?";

			try
			{	
				link += "op=" + op + "&Course=" + course + "&Test=" + test.replaceAll(" ", "_") + "&Name=" + name;
			
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(link));

				HttpResponse response = client.execute(request);
				
				if (op.equals("Select"))
				{
					ValueStoringHelperClass.LOCKED_TESTS = new ArrayList<String>();
					BufferedReader in = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
					
					String result = "";
					String line = null;
					// Read Server Response
					while((line = in.readLine()) != null)
					{
						result += line;
						break;
					}
					
					String [] temp = result.split("<br>");
					for (String s : temp)
					{
						ValueStoringHelperClass.LOCKED_TESTS.add(s.trim());
					}
				}
			}
			catch (Exception e)
			{
				publishProgress("Error during file lock management: \n" + e.getMessage());
			}
		}

		protected void onProgressUpdate(String... message) 
		{
			message[0] = (message[0].trim().equals("")) ? "An error has occured: Please check your network connection" : message[0];
			
			new AlertDialog.Builder(context)
			.setTitle("Connection Error")
			.setMessage(message[0])
			.setPositiveButton("Continue", null)
			.setCancelable(false)
			.show();
		}

		@Override
		protected void onPreExecute()
		{
			if (downloadingFiles == true)
			{
				progressDialog = ProgressDialog.show(TestScriptBrowserActivity.this, "", 
						"Downloading Script", true);
			}
			else
			{
				// Display visual feedback for loading
				listUpdateProgressBar.setVisibility(View.VISIBLE);
				exListView.setBackgroundColor(Color.GRAY);
				exListView.setEnabled(false);
			}

			viewBeingRefreshed = true;
		}

		@Override
		protected void onPostExecute(Long params)
		{
			exListAdapter = new CustomExpandableListAdapter(context, listHeaders, listItems);
			exListView.setAdapter(exListAdapter);

			if (downloadingFiles == true)
			{
				downloadingFiles = false;
				progressDialog.dismiss();
			}
			else
			{
				listUpdateProgressBar.setVisibility(View.INVISIBLE);
				exListView.setBackgroundColor(Color.WHITE);
				exListView.setEnabled(true);
			}

			if (operationBeingPerformed.equalsIgnoreCase("Update Nav Drawer"))
			{
				navDrawArrayAdapter =  new ArrayAdapter<String>(context, R.layout.drawer_list_item, drawerItems);
				drawerListView.setAdapter(navDrawArrayAdapter);
			}

			viewBeingRefreshed = false;

			if (operationBeingPerformed.equals("Download Files") && (downloadSuccess == true))
			{
				Intent pdfViewScreen = new Intent(TestScriptBrowserActivity.this, MainMarkingScreenActivity.class);
				startActivity(pdfViewScreen);
			}
			else if (operationBeingPerformed.equals("Download Files") && (downloadSuccess == false))
			{
				AlertDialog lockedScriptDialog = new AlertDialog.Builder(context)
				.setTitle("Scipt Options - " + ValueStoringHelperClass.TEST_NAME)
				.setMessage("This script is currently being marked.")
				.setPositiveButton("Ok", null)
				.show();
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
