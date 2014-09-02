package com.VictorZahraa.hybridmarker;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;

public class MainMarkingScreenActivity extends Activity implements ActionBar.TabListener 
{
	
	private TextView questionTextView;
	private TextView answerTextView;
	
	private Context context;
	private RelativeLayout sCanvasContainer;
	private SCanvasView sCanvasView;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_marking_screen);
		this.setTitle(R.string.app_name);
		
		ActionBar actionBar = this.getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Add tabs to the action bar
		actionBar.addTab(actionBar.newTab().setText("Question 1").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Question 2").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Question 3").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Question 4").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Question 5").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Question 6").setTabListener(this));
		
		actionBar.addTab(actionBar.newTab().setText("Mark Summary").setTabListener(this));

		questionTextView = (TextView) findViewById(R.id.questionText);
		
		questionTextView.setText("Consider the following problem. Answer it appropriately." + 

"\n The Petersens have recently moved to a new town and are arranging a surprise birthday party for their son Andre, and have invited three families from the neighbourhood, the Smiths, the Januarys and the Hectors. They plan to make up party packets for the kids to take home after the party, blue for boys and pink for girls. " +

"\n Being super organised, Mrs Petersen with the help of Mr Petersen wants to determine how many of each colour party packet she needs to buy, and also how many of each colour she needs to put aside for each family." +

"\n They sit down and come up with the following information. Mrs Petersen remembers that the Hectors have a �pigeon pair�, i.e. a boy and a girl. Mr Petersen recalls that the Januarys only have a set of identical twin boys. Mrs Petersen notes that she�s only ever noticed two girls from these local families to come over to play. Mr Petersen notes that the Smiths have three children, since the family fits nicely into their family sedan when they go out." +

"\n You happen to be visiting the Petersens at this point, and want to impress them with the problem solving skills you�ve learnt at university. Using the information they�ve provided, determine how many of each colour party packet they need to buy and how many of each colour they need to allocate to each family and what the total number of party packets are." +

"\n Use a diagram to show how you solve the problem.");
		
		answerTextView = (TextView) findViewById(R.id.answerText);
		
		answerTextView.setText("Consider the following problem. Answer it appropriately." + 

"\n The Petersens have recently moved to a new town and are arranging a surprise birthday party for their son Andre, and have invited three families from the neighbourhood, the Smiths, the Januarys and the Hectors. They plan to make up party packets for the kids to take home after the party, blue for boys and pink for girls. " +

"\n Being super organised, Mrs Petersen with the help of Mr Petersen wants to determine how many of each colour party packet she needs to buy, and also how many of each colour she needs to put aside for each family." +

"\n They sit down and come up with the following information. Mrs Petersen remembers that the Hectors have a �pigeon pair�, i.e. a boy and a girl. Mr Petersen recalls that the Januarys only have a set of identical twin boys. Mrs Petersen notes that she�s only ever noticed two girls from these local families to come over to play. Mr Petersen notes that the Smiths have three children, since the family fits nicely into their family sedan when they go out." +

"\n You happen to be visiting the Petersens at this point, and want to impress them with the problem solving skills you�ve learnt at university. Using the information they�ve provided, determine how many of each colour party packet they need to buy and how many of each colour they need to allocate to each family and what the total number of party packets are." +

"\n Use a diagram to show how you solve the problem.");
		
		
		
		
		// Set up the SCanvas on which marks will be made
		context = this;
        sCanvasContainer = (RelativeLayout) findViewById(R.id.markingScreenCanvasContainer);
        sCanvasView = new SCanvasView(context);
        
        // Perform certain actions when the canvas is initialized
        sCanvasView.setSCanvasInitializeListener(new SCanvasInitializeListener() {
			
			@Override
			public void onInitialized() 
			{
			   SettingStrokeInfo strokeInfo = new SettingStrokeInfo();
			   strokeInfo.setStrokeColor(Color.RED);
			   sCanvasView.setSettingStrokeInfo(strokeInfo);
			}
		});
        
        sCanvasContainer.addView(sCanvasView);
		
		/*if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_marking_screen, menu);
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

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) 
	{
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	/*public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_main_marking_screen, container, false);
			return rootView;
		}
	}*/
}
