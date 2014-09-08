package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.os.Build;

public class ScriptFinalizeAndUploadActivity extends Activity {

	ImageView studentNumberImageView;
	EditText studentNumberInput;
	
	ValueStoringHelperClass valueStore;
	
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
