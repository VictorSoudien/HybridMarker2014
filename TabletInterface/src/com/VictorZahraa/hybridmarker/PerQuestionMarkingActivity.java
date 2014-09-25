package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.VictorZahraa.hybridmarker.ScrollViewHelper.OnScrollViewListner;
import com.samsung.samm.common.SObject;
import com.samsung.samm.common.SObjectStroke;
import com.samsung.spen.lib.gesture.SPenGestureInfo;
import com.samsung.spen.lib.gesture.SPenGestureLibrary;
import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.HistoryUpdateListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;
import com.samsung.spensdk.applistener.SCanvasLongPressListener;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class PerQuestionMarkingActivity extends Activity 
{	
	private ScrollViewHelper scriptScrollView;
	private ImageView scriptDisplay;

	private Context context;
	private RelativeLayout sCanvasContainer;
	private SCanvasView sCanvasView;

	// Required for gesture recognition
	private SPenGestureLibrary gestureLib;

	// The path to the device's local storage
	private String pathToSDCard;
	
	private double currentPageScore;
	
	// Used to show short message to the user
	private Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_per_question_marking);

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

		initiliseSCanvas();
		loadGestureLibrary();
	}

	// Initialise both the sCanvasContainer and sCanvasView
	public void initiliseSCanvas()
	{
		// Set up the SCanvas on which marks will be made
		context = this;
		sCanvasContainer = (RelativeLayout) findViewById(R.id.markingScreenCanvasContainer);   

		sCanvasContainer.removeAllViews();

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
		gestureLib = new SPenGestureLibrary(PerQuestionMarkingActivity.this);
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
		scriptScrollView.scrollTo(scriptScrollView.getScrollX(), 536);
		sCanvasContainer.setScrollY(scriptScrollView.getScrollY());
	}

	// Display the previous question
	public void prevQuestion(View view)
	{
		scriptScrollView.scrollTo(scriptScrollView.getScrollX(), 288);
		sCanvasContainer.setScrollY(scriptScrollView.getScrollY());
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
				currentPageScore = 0;
				return;
			}
			else
			{
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
						// Gesture not recognized

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
			}
		}

		@Override
		protected void onPostExecute(Long params)
		{
			/*pageMarkTextView.setText("" +(prevScore + currentPageScore));*/

			if (!resultString.equals(""))
			{
				displayToast(resultString);
			}
		}
	}
}
