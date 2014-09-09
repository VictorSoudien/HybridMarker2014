package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.os.Build;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ScriptFinalizeAndUploadActivity extends Activity {

	private ImageView studentNumberImageView;
	private EditText studentNumberInput;
	
	private ImageView testDisplay;
	
	private ValueStoringHelperClass valueStore;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_script_finalize_and_upload);
		
		valueStore = new ValueStoringHelperClass();

		studentNumberImageView = (ImageView) findViewById(R.id.script_student_number_display);
		studentNumberImageView.setImageBitmap(valueStore.getPage(0));
				
		studentNumberImageView.setScaleX(2);
		studentNumberImageView.setScaleY(2);
		
		studentNumberImageView.setScrollY(540);
		studentNumberImageView.setScrollX(-85);
		
		studentNumberInput = (EditText) findViewById(R.id.student_number_field);
		
		testDisplay = (ImageView) findViewById(R.id.testDisplay);
		testDisplay.setImageBitmap(valueStore.getMergedBitmap(0));
		
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
		if (id == R.id.action_settings) {
			return true;
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
			
			Intent scriptUploadScreen = new Intent(ScriptFinalizeAndUploadActivity.this, TestScriptBrowserActivity.class);
	    	startActivity(scriptUploadScreen);
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
