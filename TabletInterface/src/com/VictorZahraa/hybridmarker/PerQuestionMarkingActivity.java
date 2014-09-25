package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;

public class PerQuestionMarkingActivity extends Activity 
{	
	private ScrollView scriptScrollView;
	private ImageView scriptDisplay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_per_question_marking);
		
		scriptScrollView = (ScrollView) findViewById(R.id.scriptDisplayScrollView);
		//scriptScrollView.setScrollY(840);
		
		scriptDisplay = (ImageView) findViewById(R.id.scriptDisplay);
		
		//scriptDisplay.setScrollY(288);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.per_question_marking, menu);
		return true;
	}
	
	// Display the next question
	public void nextQuestion(View view)
	{
		/*questionTextView.setText(valueStore.getNextQuestion());
		answerTextView.setText(valueStore.getNextAnswer());*/
		scriptScrollView.scrollTo(scriptScrollView.getScrollX(), 536);
	}
	
	// Display the next question
	public void prevQuestion(View view)
	{
		/*questionTextView.setText(valueStore.getNextQuestion());
		answerTextView.setText(valueStore.getNextAnswer());*/
		scriptScrollView.scrollTo(scriptScrollView.getScrollX(), 288);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if (id == R.id.action_settings) 
		{
			scriptScrollView.scrollTo(scriptScrollView.getScrollX(), 288);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
