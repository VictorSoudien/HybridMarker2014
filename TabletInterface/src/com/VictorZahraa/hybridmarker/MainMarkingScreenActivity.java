package com.VictorZahraa.hybridmarker;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.VictorZahraa.hybridmarker.ScrollViewHelper.OnScrollViewListner;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.samsung.samm.common.SObject;
import com.samsung.samm.common.SObjectStroke;
import com.samsung.sdraw.CanvasView.OnHistoryChangeListener;
import com.samsung.spen.lib.gesture.SPenGestureInfo;
import com.samsung.spen.lib.gesture.SPenGestureLibrary;
import com.samsung.spen.lib.input.SPenEvent;
import com.samsung.spen.lib.input.SPenLibrary;
import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.HistoryUpdateListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SCanvasLongPressListener;
import com.samsung.spensdk.applistener.SPenTouchListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

public class MainMarkingScreenActivity extends Activity implements ActionBar.TabListener 
{
	private TextView questionTextView;
	private TextView answerTextView;
	private ScrollViewHelper scriptScrollView;
	private ImageView scriptDisplay;
	
	private ActionBar actionBar;
	
	// Used to display short messages to the user
	private Toast toast;
	
	private Context context;
	private RelativeLayout sCanvasContainer;
	private SCanvasView sCanvasView;
	
	// Required for gesture recognition
	private SPenGestureLibrary gestureLib;
	
	private String pathToSDCard; // The path to the device's local storage
	private int currentPage;
	private double currentPageScore;
	
	// Allows for values to be stored and accessed across activities
	private ValueStoringHelperClass valueStore;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_marking_screen);
		this.setTitle(R.string.app_name);
		
		valueStore = new ValueStoringHelperClass();
		currentPageScore = 0;
		
		// Get the path to external storage
        pathToSDCard = Environment.getExternalStorageDirectory().getPath();
		
		scriptScrollView = (ScrollViewHelper) findViewById(R.id.scriptDisplayScrollView);
		scriptScrollView.setOnScrollViewListener(new OnScrollViewListner() {
			
			@Override
			public void onScrollChanged(ScrollViewHelper scrollView, int l, int t,
					int prevL, int prevT) {
				sCanvasContainer.setScrollY(scriptScrollView.getScrollY());
			}
		});
		
		scriptDisplay = (ImageView) findViewById(R.id.scriptDisplay);
		
		// Scale the image so that it fills the display area
		scriptDisplay.setScaleX(1.1f);
		scriptDisplay.setScaleY(1.1f);

		questionTextView = (TextView) findViewById(R.id.questionText);
		
		questionTextView.setText("Consider the following problem. Answer it appropriately." + 

"\n The Petersens have recently moved to a new town and are arranging a surprise birthday party for their son Andre, and have invited three families from the neighbourhood, the Smiths, the Januarys and the Hectors. They plan to make up party packets for the kids to take home after the party, blue for boys and pink for girls. " +

"\n Being super organised, Mrs Petersen with the help of Mr Petersen wants to determine how many of each colour party packet she needs to buy, and also how many of each colour she needs to put aside for each family." +

"\n They sit down and come up with the following information. Mrs Petersen remembers that the Hectors have a “pigeon pair”, i.e. a boy and a girl. Mr Petersen recalls that the Januarys only have a set of identical twin boys. Mrs Petersen notes that she’s only ever noticed two girls from these local families to come over to play. Mr Petersen notes that the Smiths have three children, since the family fits nicely into their family sedan when they go out." +

"\n You happen to be visiting the Petersens at this point, and want to impress them with the problem solving skills you’ve learnt at university. Using the information they’ve provided, determine how many of each colour party packet they need to buy and how many of each colour they need to allocate to each family and what the total number of party packets are." +

"\n Use a diagram to show how you solve the problem.");
		
		answerTextView = (TextView) findViewById(R.id.answerText);
		
		answerTextView.setText("Consider the following problem. Answer it appropriately." + 

"\n The Petersens have recently moved to a new town and are arranging a surprise birthday party for their son Andre, and have invited three families from the neighbourhood, the Smiths, the Januarys and the Hectors. They plan to make up party packets for the kids to take home after the party, blue for boys and pink for girls. " +

"\n Being super organised, Mrs Petersen with the help of Mr Petersen wants to determine how many of each colour party packet she needs to buy, and also how many of each colour she needs to put aside for each family." +

"\n They sit down and come up with the following information. Mrs Petersen remembers that the Hectors have a “pigeon pair”, i.e. a boy and a girl. Mr Petersen recalls that the Januarys only have a set of identical twin boys. Mrs Petersen notes that she’s only ever noticed two girls from these local families to come over to play. Mr Petersen notes that the Smiths have three children, since the family fits nicely into their family sedan when they go out." +

"\n You happen to be visiting the Petersens at this point, and want to impress them with the problem solving skills you’ve learnt at university. Using the information they’ve provided, determine how many of each colour party packet they need to buy and how many of each colour they need to allocate to each family and what the total number of party packets are." +

"\n Use a diagram to show how you solve the problem.");
		
		//answerTextView.setText("Hello World! I'm the Answer.");
		
		initTabs();
		initiliseSCanvas();
		loadGestureLibrary();
		
		/*if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
	}
	
	// Initialises tab layout
	private void initTabs()
	{
		actionBar = this.getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		//int numPages = Integer.parseInt(getResources().getString(R.string.number_of_pages));
		int numPages = valueStore.getNumPage();
		
		// Leave out the first page (cover page) -- could be changed
		for (int i = 1; i < numPages; i++)
		{
			// Add tabs to the action bar
			actionBar.addTab(actionBar.newTab().setText("Page " + i).setTabListener(this));
		}
		
		actionBar.addTab(actionBar.newTab().setText("Mark Summary").setTabListener(this));
	}
	
	// Initialise both the sCanvasContainer and sCanvasView
	public void initiliseSCanvas()
	{
		// Set up the SCanvas on which marks will be made
		context = this;
        sCanvasContainer = (RelativeLayout) findViewById(R.id.markingScreenCanvasContainer);    
        sCanvasView = new SCanvasView(context);
        
        // Set the properties of the SPen Stroke
        sCanvasView.setSCanvasInitializeListener(new SCanvasInitializeListener() {
			
			@Override
			public void onInitialized() 
			{
			   SettingStrokeInfo strokeInfo = new SettingStrokeInfo();
			   strokeInfo.setStrokeColor(Color.RED);
			   strokeInfo.setStrokeWidth(1.0f);
			   sCanvasView.setSettingStrokeInfo(strokeInfo);
			}
		});
        
        sCanvasView.setSCanvasLongPressListener(new SCanvasLongPressListener() {
			
			@Override
			public void onLongPressed(float arg0, float arg1) 
			{
				displayToast ("Long Press Args");
			}
			
			@Override
			public void onLongPressed() 
			{
				displayToast ("Long Press");
			}
		});

        sCanvasView.setSPenTouchListener(new SPenTouchListener() {
			
			@Override
			public boolean onTouchPenEraser(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onTouchPen(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onTouchFinger(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onTouchButtonUp(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onTouchButtonDown(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				
			}
		});
        
        sCanvasView.setHistoryUpdateListener(new HistoryUpdateListener() 
        {	
			@Override
			public void onHistoryChanged(boolean arg0, boolean arg1) 
			{	
				new GestureRecognition().execute(sCanvasView);
			}
		});
     
        sCanvasView.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
        sCanvasContainer.addView(sCanvasView);
	}

	// Load the custom gesture library
	public void loadGestureLibrary()
	{
		gestureLib = new SPenGestureLibrary(MainMarkingScreenActivity.this);
        gestureLib.openSPenGestureEngine();
        
        // Load the gesture library from the SD Card
        if(gestureLib.loadUserSPenGestureData(pathToSDCard + "/marking_gesture_data.dat"))
    	{
    		displayToast("Custom Gesture Library Loaded");
    	}
        else // if the file is not found, then load it onto the SD Card
        {	
        	try
        	{
	        	InputStream inStream = getResources().openRawResource(R.raw.marking_gesture_data);
	        	FileOutputStream outStream = new FileOutputStream(pathToSDCard + "/marking_gesture_data.dat");
	        	int read = 0;
	        	
	        	byte [] buffer = new byte[1024];
	        	
	        	while ((read = inStream.read(buffer)) > 0)
	        	{
	        		outStream.write(buffer, 0, read);
	        	}
	        	
	        	inStream.close();
	        	outStream.close();
        	}
        	catch (Exception e)
        	{
        		displayToast("ERROR:" + e.getMessage());
        	}
        	
        	if(gestureLib.loadUserSPenGestureData(pathToSDCard + "/marking_gesture_data.dat"))
        	{
        		displayToast("Custom Gesture Library Loaded");
        	}
        }
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
		
		if (id == R.id.action_settings) 
		{
			return true;
		}
		else if (id == R.id.action_undo)
		{
			sCanvasView.undo();
		}
		else if (id == R.id.action_add_comment)
		{
			if (sCanvasView.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_PEN)
			{
				item.setIcon(R.drawable.ic_action_edit_selected);
				sCanvasView.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_TEXT);
			}
			else if (sCanvasView.getCanvasMode() == SCanvasConstants.SCANVAS_MODE_INPUT_TEXT)
			{
				item.setIcon(R.drawable.ic_action_edit);
				sCanvasView.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) 
	{
		// Reset the scroll view when a new tab is selected
		scriptScrollView.setScrollY(0);
		
		if (tab.getText().equals("Mark Summary"))
		{
			// Display the mark summary screen
		}
		else
		{
			String pageNum = tab.getText().toString().split(" ")[1];
			int page = Integer.parseInt(pageNum) + 1;
			
			currentPage = page;
			
			File page1 = new File (pathToSDCard + "/page" + page + ".png");
			Bitmap imageBitmap = BitmapFactory.decodeFile(page1.getAbsolutePath());
			
			if (imageBitmap != null)
			{
				scriptDisplay.setImageBitmap(imageBitmap);
			}
			else
			{
				displayToast("ERROR setting image");
			}
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) 
	{
		tab.setText(tab.getText() + " [" + valueStore.getPageScore(currentPage) + "]");
		currentPageScore = 0;
		
		sCanvasView.clearScreen();
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
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
	
	private class GestureRecognition extends AsyncTask<SCanvasView, String, Long>
	{
		String resultString = "";
		
		@Override
		protected Long doInBackground(SCanvasView... params) 
		{
			if (params.length == 1)
			{
				performGestureRecog(params[0]);
			}
			
			return null;
		}
		
		private void performGestureRecog(SCanvasView view)
		{
	    	LinkedList<SObject> sObjects = view.getSObjectList(true);
	    	
	    	// Used to keep track of the amount of each gesture
	    	HashMap<String, Integer> gestureCount = new HashMap<String, Integer>();
	    	
	    	if ((sObjects == null) || (sObjects.size() <= 0))
	    	{
	    		// No objects found
	    		return;
	    	}
	    	
	    	for (SObject objs : sObjects)
	    	{
		    	PointF [][] currentPoints = new PointF[1][1];
		    	currentPoints[0] = ((SObjectStroke) objs).getPoints();
		    	/*int index = 0;
		    	
		    	for (SObject obj : sObjects)
		    	{
		    		currentPoints[index] = ((SObjectStroke) obj).getPoints();
		    		index++;
		    	}*/
		    	
		    	ArrayList<SPenGestureInfo> gestureInfo = gestureLib.recognizeSPenGesture(currentPoints);
		
		    	if ((gestureInfo == null) || (gestureInfo.size() <= 0))
		    	{
		    		// Gesture not recognized
		    		return;
		    	}
				
				int maxIndex = -1;
				int maxValue = -100;
				int scoreThreshold = 80; // The lower the number the greater the chance of false positives
				
				for (int i = 0; i < gestureInfo.size(); i++)
				{
					if ((gestureInfo.get(i).mScore > maxValue) && (gestureInfo.get(i).mScore >= scoreThreshold))
					{
						maxValue = gestureInfo.get(i).mScore;
						maxIndex = i;
					}
				}
				
				if (maxIndex == -1)
				{
					//displayToast("Not recognised");
					//sCanvasView.clearScreen();
				}
				else
				{
					//displayToast(gestureInfo.get(maxIndex).mName);
					//sCanvasView.clearScreen();
					
					String key = gestureInfo.get(maxIndex).mName.trim();
					int currentCount = -1;
					
					if (gestureCount.get(key) != null)
					{
						currentCount = gestureCount.get(key);
						gestureCount.put(key, currentCount + 1);
					}
					else
					{
						gestureCount.put(key, 1);
					}
				}
	    	}
	    	
	    	int tickCount = (gestureCount.get("tick") == null) ? 0 : gestureCount.get("tick");
	    	int halfTickCount = (gestureCount.get("halfTick") == null) ? 0 : gestureCount.get("halfTick");
	    	
	    	resultString = "Ticks " + tickCount + "\n" +
					  "Half Ticks " + halfTickCount + "\n" +
					  "Crosses " + gestureCount.get("x");
	    	
	    	currentPageScore = tickCount + ((0.5) * halfTickCount);
	    	valueStore.setPageScore(currentPage, currentPageScore);
		}
		
		@Override
		protected void onPostExecute(Long params)
		{
	    	displayToast(resultString);
		}
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
