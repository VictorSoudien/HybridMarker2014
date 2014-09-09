package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
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
		String studentNum = studentNumberInput.getText().toString();
		
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
	}

	// Upload the script to the server
	private class UploadFile extends AsyncTask<String, Integer, String>
	{
		private JSch jsch;
		private Session sshSession;
		
		@Override
		protected String doInBackground(String... params) 
		{
			// TODO Auto-generated method stub
			connectToServer();
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
		private void uploadFiles()
		{
			try
			{
				Channel commChannel = sshSession.openChannel("sftp");
				commChannel.connect();
				ChannelSftp sftpChannel = (ChannelSftp) commChannel;
				
				String uploadDirectory = valueStore.getCurrentDirectory();
			}
			catch (Exception e)
			{
				// Handle the error
			}
			
	        // Get the path to external storage
	        String pathToSDCard = Environment.getExternalStorageDirectory().getPath();
			
			/*try
			{
				String filenames = executeCommandOnServer("cd " + directory + " && ls");
				//displayToast(directory);
				String [] files = filenames.split("\n");
				
				Channel commChannel = sshSession.openChannel("sftp");
				commChannel.connect();
				ChannelSftp sftpChannel = (ChannelSftp) commChannel;
				int numPages = 0;
				
				valueStore.initPageCollection();
				
				for (String file : files)
				{
					file = file.trim();
					
					if (file.contains(".png"))
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
				valueStore.setMemoText("memo.txt");
				publishProgress("Memo Downloaded");
				
				valueStore.setNumPages(numPages);
				//displayToast("Downloaded files from " + directory);
			}
			catch (Exception e)
			{
				displayToast("Error: Could not download file \n" + e);
			}*/
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
