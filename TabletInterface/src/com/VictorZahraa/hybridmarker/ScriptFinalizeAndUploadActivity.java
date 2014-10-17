package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class ScriptFinalizeAndUploadActivity extends Activity {

	private static final int SWIPE_MIN_DISTANCE = 50;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	
	private String studentNum;

	private ImageView studentNumberImageView;
	private AutoCompleteTextView studentNumberInput;
	private ImageView testDisplay;
	private TextView markTextView;
	private ListView markSummary;

	private Context context;

	private ValueStoringHelperClass valueStore;

	private int numMarkedPages;
	private int currentPageBeingShown;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_script_finalize_and_upload);

		context = this;

		valueStore = new ValueStoringHelperClass();

		numMarkedPages = valueStore.getNumPage() - 1;
		currentPageBeingShown = 0;

		studentNumberImageView = (ImageView) findViewById(R.id.script_student_number_display);
		studentNumberImageView.setImageBitmap(valueStore.getPage(0));

		studentNumberImageView.setScaleX(2);
		studentNumberImageView.setScaleY(2);

		studentNumberImageView.setScrollY(540);
		studentNumberImageView.setScrollX(-85);

		studentNumberInput = (AutoCompleteTextView) findViewById(R.id.student_number_field);

		ArrayAdapter<String> stuAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_expandable_list_item_1, ValueStoringHelperClass.STUDENTS_LIST);
		studentNumberInput.setAdapter(stuAdapter);

		// Gesture detection
		gestureDetector = new GestureDetector(this, new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		};

		testDisplay = (ImageView) findViewById(R.id.testDisplay);
		testDisplay.setImageBitmap(valueStore.getMergedBitmap(currentPageBeingShown));

		testDisplay.setOnTouchListener(gestureListener);

		markTextView = (TextView) findViewById(R.id.scoreTextView);
		markTextView.setText("Final Mark: " + valueStore.getSumOfPageScores() + " / " + valueStore.getTotalMark() + "\t\tMarks for this page: "  + valueStore.getPageScore(currentPageBeingShown + 1));

		markSummary = (ListView) findViewById(R.id.questionTotalsListView);

		ArrayList<String> data = new ArrayList<String>();

		for (int i = 0; i < valueStore.getNumberOfMainQuestion(); i++)
		{
			data.add("" + valueStore.getMarksForQuestion(i)); 
		}

		markSummary.setAdapter(new CustomListAdapter(context, 0, data));

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.script_finalize_and_upload, menu);

		MenuItem userDisplay = menu.findItem(R.id.user_display);
		userDisplay.setTitle(ValueStoringHelperClass.USERNAME);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_flag_script)
		{
			// Display the flagging dialog
			final EditText input = new EditText(context);
			input.setText(valueStore.getFlagText());

			new AlertDialog.Builder(context)
			.setTitle("Flag Script")
			.setMessage("Please state the reason for flagging this script:")
			.setView(input)
			.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					valueStore.setFlagText(input.getText().toString());
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing.
				}
			}).show();
		}
		else if (id == R.id.user_display)
		{
			new ProfilePopUpHelper(context, this.getLayoutInflater());
		}
		return super.onOptionsItemSelected(item);
	}


	// Called when the upload script button is clicked
	public void uploadScript(View view)
	{
		studentNum = studentNumberInput.getText().toString().trim();

		if (studentNum.equals("") || studentNum.length() != 9 || (ValueStoringHelperClass.STUDENTS_LIST.contains(studentNum) == false))
		{
			// Display an error for student number entry
			new AlertDialog.Builder(this)
			.setTitle("Invalid Student Number")
			.setMessage("Please enter a valid student number")
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					// Dismiss the dialog
				}}
					).show();
		}
		else
		{
			new UploadFile().execute(studentNum, "No");
		}
	}

	// Upload the script to the server
	private class UploadFile extends AsyncTask<String, String, String>
	{
		private JSch jsch;
		private Session sshSession;

		private ProgressDialog progressDialog;

		private String pathToSDCard;

		private boolean success;
		private String error;
		private boolean reportOption = false;


		@Override
		protected String doInBackground(String... params) 
		{
			// Get the path to external storage
			pathToSDCard = Environment.getExternalStorageDirectory().getPath();

			success = connectToServer();

			if (success == true)
			{
				uploadFiles(params[0], params[1]);
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
				error = "An error occured while trying to establish a connection to the server: \nPlease check your network connection";
				return false;
			}
		}

		// Download the images needed for each test from the server
		private void uploadFiles(String studentNumber, String reportUpload)
		{	
			try
			{
				Channel commChannel = sshSession.openChannel("sftp");
				commChannel.connect();
				ChannelSftp sftpChannel = (ChannelSftp) commChannel;

				String baseDirectory =  valueStore.getCurrentDirectory() + studentNumber.toUpperCase() + "+/";
				String oldDir = valueStore.getCurrentDirectory() + valueStore.getTestName() + "/";

				if (reportUpload.equalsIgnoreCase("No"))
				{
					// Rename the directory to the student number
					sftpChannel.rename(valueStore.getCurrentDirectory() + valueStore.getTestName() + "/", baseDirectory);
				}

				File temp;
				FileOutputStream fileOut;
				String basePageName = "";

				if (reportUpload.equalsIgnoreCase("Yes"))
				{
					basePageName = "ConflictPage";
					
					// Upload the cover page
					temp = new File (pathToSDCard + "/tempMarkedPage.png");
					fileOut = new FileOutputStream(temp);

					Bitmap currentPage = valueStore.getPage(0);
					currentPage.compress(Bitmap.CompressFormat.PNG, 100, fileOut);

					String tempUploadDir = baseDirectory + basePageName + "1.png";
					sftpChannel.put(new FileInputStream(temp), tempUploadDir);

					// Delete the temp file that was created
					temp.delete();
				}
				else if (ValueStoringHelperClass.isRemark == true)
				{
					basePageName = "ReMarkedPage";
				}
				else
				{
					basePageName = "MarkedPage";
				}

				// Subtract 1 because I don't reupload the cover page
				for (int i = 0; i < valueStore.getNumPage() - 1; i++)
				{
					temp = new File (pathToSDCard + "/tempMarkedPage.png");
					fileOut = new FileOutputStream(temp);

					Bitmap currentPage = valueStore.getMergedBitmap(i);
					currentPage.compress(Bitmap.CompressFormat.PNG, 100, fileOut);

					String tempUploadDir = baseDirectory + basePageName + (i+2) + ".png";
					sftpChannel.put(new FileInputStream(temp), tempUploadDir);

					// Delete the temp file that was created
					temp.delete();
				}

				sftpChannel.disconnect();

				uploadMarks();
			}
			catch (Exception e)
			{
				// The case when a folder with the same name has been marked
				if (e.getMessage().trim().equalsIgnoreCase("Failure"))
				{
					error = "A test with this name has already been marked. \nPlease check the student number or submit a report";
					reportOption = true;
					success = false;
					return;
				}
				else
				{
					error = "An error occured during file upload: \nPlease check your network connection";
				}
				success = false;
			}
		}

		// Uploads the marks to the database and determines whether or not the script should be flagged
		private void uploadMarks()
		{
			String link = context.getText(R.string.base_URL) + "/uploadMarks.php?";

			try
			{	
				link += "Course=" + ValueStoringHelperClass.COURSE_NAME.trim() +
						"&Test=" + ValueStoringHelperClass.TEST_NAME.trim().replaceAll(" ", "%20") + 
						"&user=" + ValueStoringHelperClass.USERNAME.trim() + 
						"&studentNumber=" + studentNumberInput.getText().toString().trim() + 
						"&Mark=" + valueStore.getSumOfPageScores() +
						"&Result=" + valueStore.getResultsInDBFormat().replaceAll("\\+", "%2B");

				if (valueStore.isScriptFlagged() == true)
				{
					
					String flagText = encode(valueStore.getFlagText().replaceAll("\n", ""));
					link += "&Flag=true" + "&Comment=" + flagText;//valueStore.getFlagText().replaceAll(" ", "%20");
				}
				else
				{
					link += "&Flag=false";
				}

				if (ValueStoringHelperClass.isRemark == true)
				{
					link += "&Remark=Yes";
				}
				else
				{
					link += "&Remark=No";
				}

				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(link));

				HttpResponse response = client.execute(request);
			}
			catch (Exception e)
			{
				// Handle exception
				error = "An error occured during mark uploading: \nPlease check your network connection";
				error += "\n" + e.getMessage();
				success = false;
			}
		}
		
		/////////// Retrieved from stackoverflow.com/questions/4571346/how-to-encode-url-to-avoid-special-characters-in-java on 10/17/2014
		public String encode(String input) {
	        StringBuilder resultStr = new StringBuilder();
	        for (char ch : input.toCharArray()) {
	            if (isUnsafe(ch)) {
	                resultStr.append('%');
	                resultStr.append(toHex(ch / 16));
	                resultStr.append(toHex(ch % 16));
	            } else {
	                resultStr.append(ch);
	            }
	        }
	        return resultStr.toString();
	    }

	    private char toHex(int ch) {
	        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
	    }

	    private boolean isUnsafe(char ch) {
	        if (ch > 128 || ch < 0)
	            return true;
	        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
	    }
		/////////// End of external code

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(ScriptFinalizeAndUploadActivity.this, "", 
					"Uploading Script", true);
		}

		@Override
		protected void onPostExecute(String param)
		{
			progressDialog.dismiss();

			if (success == true)
			{
				new AlertDialog.Builder(context)
				.setCancelable(false)
				.setTitle("Script Upload")
				.setMessage("File uploaded successfully")
				.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) 
					{
						valueStore.recycleBitmaps();

						// Restart the application
						Intent i = getBaseContext().getPackageManager()
								.getLaunchIntentForPackage( getBaseContext().getPackageName() );
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}}
						).show();
			}
			else
			{
				if (reportOption == false)
				{
					new AlertDialog.Builder(context)
					.setTitle("Script Upload Unsuccessful")
					.setMessage(error)
					.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) 
						{
							// Dismiss the dialog
						}}
							).show();
				}
				else
				{
					new AlertDialog.Builder(context)
					.setTitle("Script Upload Unsuccessful")
					.setMessage(error)
					.setPositiveButton("Submit Report", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) 
						{
							if (valueStore.getFlagText().trim().equals(""))
							{
								valueStore.setFlagText("This test conflicts with another on the server.\n Please check both scripts");
							}
							else if (!(valueStore.getFlagText().contains("This test conflicts with another on the server.\n Please check both scripts")))
							{
								valueStore.setFlagText(valueStore.getFlagText() + "\nThis test conflicts with another on the server.\n Please check both scripts");
							}
							
							// Display the flagging dialog
							final EditText input = new EditText(context);
							input.setText(valueStore.getFlagText());

							new AlertDialog.Builder(context)
							.setTitle("Flag Script")
							.setMessage("Please state the reason for flagging this script:")
							.setView(input)
							.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									valueStore.setFlagText(input.getText().toString());
									
									new UploadFile().execute(studentNum, "Yes");
								}
							}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									// Do nothing.
								}
							}).show();
						}})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) 
							{
								// Dismiss the dialog
							}})
							.show();
				}
			}
		}
	}

	// From http://stackoverflow.com/questions/937313/android-basic-gesture-detection
	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				{
					return false;
				}
				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) 
				{
					// Left Swipe
					currentPageBeingShown = (currentPageBeingShown == numMarkedPages) ? currentPageBeingShown : (currentPageBeingShown + 1);
					testDisplay.setImageBitmap(valueStore.getMergedBitmap(currentPageBeingShown));
					markTextView.setText("Final Mark: " + valueStore.getSumOfPageScores() + " / " + valueStore.getTotalMark() + "\t\tMarks for this page: "  + valueStore.getPageScore(currentPageBeingShown + 1));
				} 
				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) 
				{
					// Right Swipe
					currentPageBeingShown = (currentPageBeingShown == 0) ? 0 : (currentPageBeingShown - 1);
					testDisplay.setImageBitmap(valueStore.getMergedBitmap(currentPageBeingShown));
					markTextView.setText("Final Mark: " + valueStore.getSumOfPageScores() + " / " + valueStore.getTotalMark() + "\t\tMarks for this page: "  + valueStore.getPageScore(currentPageBeingShown + 1));
				}
			} catch (Exception e) 
			{
				// nothing
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
	}

	private class CustomListAdapter extends ArrayAdapter<String>
	{
		private Context context;
		private List<String> data;

		public CustomListAdapter(Context contextParam, int textViewResourceId,List<String> objects) 
		{
			super(contextParam, R.layout.mark_summary_item, objects);
			context = contextParam;
			data = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View rowView = inflater.inflate(R.layout.mark_summary_item, null);

			TextView questionName = (TextView) rowView.findViewById(R.id.questionName);
			TextView markArea = (TextView) rowView.findViewById(R.id.markArea);

			questionName.setText("Question " + (position + 1));
			markArea.setText(data.get(position));

			return rowView;
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
					R.layout.fragment_script_finalize_and_upload, container,
					false);
			return rootView;
		}
	}*/

}
