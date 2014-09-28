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
import android.text.Editable;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ScriptFinalizeAndUploadActivity extends Activity implements OnClickListener {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	private ImageView studentNumberImageView;
	private EditText studentNumberInput;
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

		studentNumberInput = (EditText) findViewById(R.id.student_number_field);

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

		/*ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				context, 
				android.R.layout.simple_list_item_1,
				data);*/

		markSummary.setAdapter(new CustomListAdapter(context, 0, data));

		/*if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.script_finalize_and_upload, menu);
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
		else if (id == R.id.action_flag_script)
		{
			// Display the flagging dialog
			final EditText input = new EditText(context);

			new AlertDialog.Builder(context)
			.setTitle("Flag Script")
			.setMessage("Please state the reason for flagging this script:")
			.setView(input)
			.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Editable value = input.getText(); 
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing.
				}
			}).show();
		}
		return super.onOptionsItemSelected(item);
	}


	// Called when the upload script button is clicked
	public void uploadScript(View view)
	{
		String studentNum = studentNumberInput.getText().toString().trim();

		if (studentNum.equals("") || studentNum.length() != 9)
		{
			// Display the flagging dialog
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
			new UploadFile().execute(studentNum);
		}
	}

	// Upload the script to the server
	private class UploadFile extends AsyncTask<String, String, String>
	{
		private JSch jsch;
		private Session sshSession;

		private ProgressDialog progressDialog;

		private String pathToSDCard;

		@Override
		protected String doInBackground(String... params) 
		{
			// Get the path to external storage
			pathToSDCard = Environment.getExternalStorageDirectory().getPath();

			connectToServer();
			uploadFiles(params[0]);
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
				//displayToast("Error while connecting to nightmare\n" + e.getMessage());
			}
		}

		// Download the images needed for each test from the server
		private void uploadFiles(String studentNumber)
		{	
			try
			{
				Channel commChannel = sshSession.openChannel("sftp");
				commChannel.connect();
				ChannelSftp sftpChannel = (ChannelSftp) commChannel;

				String baseDirectory =  valueStore.getCurrentDirectory() + studentNumber + "+/";

				// Rename the directory to the student number
				sftpChannel.rename(valueStore.getCurrentDirectory() + valueStore.getTestName() + "/", baseDirectory);

				File temp;// = new File (pathToSDCard + "/tempCoverPage.png");
				FileOutputStream fileOut;
				String basePageName = "MarkedPage";

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

				deleteDownloadedFiles();
			}
			catch (Exception e)
			{
				// Handle the error
				publishProgress("ERROR\n" + e.getMessage());
			}
		}

		// Deletes the files that were downloaded
		private void deleteDownloadedFiles()
		{
			for (int i = 0; i < valueStore.getNumPage(); i++)
			{
				File temp = new File (pathToSDCard + "/page" + (i+1) + ".png");
				temp.delete();
			}
		}

		@Override
		protected void onPreExecute()
		{
			progressDialog = ProgressDialog.show(ScriptFinalizeAndUploadActivity.this, "", 
					"Uploading Script", true);
		}

		@Override
		protected void onProgressUpdate(String... messages)
		{
			progressDialog.setMessage(messages[0]);
		}

		@Override
		protected void onPostExecute(String param)
		{
			progressDialog.dismiss();

			new AlertDialog.Builder(context)
			.setTitle("Script Upload")
			.setMessage("File uploaded successfully")
			.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					// Return the user to the script browser screen
					/*Intent scriptUploadScreen = new Intent(ScriptFinalizeAndUploadActivity.this, TestScriptBrowserActivity.class);
			    	startActivity(scriptUploadScreen);*/

					valueStore.recycleBitmaps();

					// Restart the application
					Intent i = getBaseContext().getPackageManager()
							.getLaunchIntentForPackage( getBaseContext().getPackageName() );
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				}}
					).show();
		}
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
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
			
			/*TextView textView = (TextView) rowView.findViewById(R.id.label);
			ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
			textView.setText(values[position]);
			// change the icon for Windows and iPhone
			String s = values[position];
			if (s.startsWith("iPhone")) {
				imageView.setImageResource(R.drawable.no);
			} else {
				imageView.setImageResource(R.drawable.ok);
			}*/

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
