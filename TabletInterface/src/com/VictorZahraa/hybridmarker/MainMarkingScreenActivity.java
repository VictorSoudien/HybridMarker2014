package com.VictorZahraa.hybridmarker;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainMarkingScreenActivity extends Activity implements ActionBar.TabListener 
{

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
