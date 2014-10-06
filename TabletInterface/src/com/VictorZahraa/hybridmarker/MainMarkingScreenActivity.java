package com.VictorZahraa.hybridmarker;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.VictorZahraa.hybridmarker.ScrollViewHelper.OnScrollViewListner;
import com.samsung.samm.common.SObject;
import com.samsung.samm.common.SObjectStroke;
import com.samsung.samm.common.SObjectText;
import com.samsung.spen.lib.gesture.SPenGestureInfo;
import com.samsung.spen.lib.gesture.SPenGestureLibrary;
import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spen.settings.SettingTextInfo;
import com.samsung.spensdk.SCanvasConstants;
import com.samsung.spensdk.SCanvasView;
import com.samsung.spensdk.applistener.HistoryUpdateListener;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TooManyListenersException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class MainMarkingScreenActivity extends Activity implements ActionBar.TabListener 
{
	private final int PAGE_OFFSET = 200;
	private enum GestureMode{NORMAL, UNDO};

	private GestureMode currentGestureMode;

	private TextView pageMarkTextView;
	private ScrollViewHelper scriptScrollView;
	private ImageView scriptDisplay;
	private ListView memoAnswersListView;

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
	private double prevScore;
	private double currentPageScore;
	private LinkedList<SObject> previousSObjects;

	// Allows for values to be stored and accessed across activities
	private ValueStoringHelperClass valueStore;

	// A flag used to check if the bitmaps are being merged
	private boolean bitmapsBeingMerged;
	
	// Used during gesture recognition
	HashMap<Double, ArrayList<Double>> prevGesturePoints;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_marking_screen);
		this.setTitle(R.string.app_name);

		context = this;

		valueStore = new ValueStoringHelperClass();

		currentPage = 1;
		currentPageScore = 0;

		currentGestureMode = GestureMode.NORMAL;
		bitmapsBeingMerged = false;

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
		scriptDisplay.setScaleX(1.05f);
		scriptDisplay.setScaleY(1.05f);

		ArrayList<String> data = valueStore.getMemoForPage(0);

		memoAnswersListView = (ListView) findViewById(R.id.memoAnswersListView);
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				context, 
				R.layout.memo_answer_item,R.id.memo_text,
				data);

		memoAnswersListView.setAdapter(arrayAdapter);
		memoAnswersListView.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				scriptScrollView.setScrollY(valueStore.getStartAnswerCoords(currentPage - 1, position)  - PAGE_OFFSET);
			}
		});

		pageMarkTextView = (TextView) findViewById(R.id.markText);

		initTabs();
		initiliseSCanvas();
		loadGestureLibrary();

		pageMarkTextView.setText(valueStore.getMarkDisplay());

		/*if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}*/
	}

	// Handle the pressing of the 'physical' back button
	@Override
	public void onBackPressed()
	{
		// Notify the user that any marking progress will be lost
		new AlertDialog.Builder(context)
		.setTitle("Are you sure you wish to exit?")
		.setMessage("All marking progress on this script will be lost")
		.setCancelable(false)
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				valueStore.recycleBitmaps();
				finish();
			}})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					//Dismiss the dialog
				}}
					).show();
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
	}

	// Initialise both the sCanvasContainer and sCanvasView
	public void initiliseSCanvas()
	{
		// Set up the SCanvas on which marks will be made
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

				SettingTextInfo textInfo = new SettingTextInfo();
				textInfo.setTextColor(Color.RED);
				textInfo.setTextSize(1.0f);
				sCanvasView.setSettingTextInfo(textInfo);

				sCanvasView.setZoomEnable(false);
			}
		});

		sCanvasView.setHistoryUpdateListener(new HistoryUpdateListener() 
		{	
			@Override
			public void onHistoryChanged(boolean arg0, boolean arg1) 
			{	
				if (bitmapsBeingMerged == false && ((sCanvasView.getSelectedSObjectList() == null) || (sCanvasView.getSelectedSObjectList().size() == 0)))
				{
					new GestureRecognition().execute(sCanvasView, currentGestureMode);
				}
				//else do nothing
			}
		});

		sCanvasView.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
		sCanvasContainer.addView(sCanvasView);
	}

	// Load the custom gesture library
	public void loadGestureLibrary()
	{
		previousSObjects = new LinkedList<SObject>();

		gestureLib = new SPenGestureLibrary(MainMarkingScreenActivity.this);
		gestureLib.openSPenGestureEngine();

		// Load the gesture library from the SD Card
		if(gestureLib.loadUserSPenGestureData(pathToSDCard + "/marking_gesture_data.dat"))
		{
			// Gesture data loaded successfully
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
				//displayToast("Custom Gesture Library Loaded");
			}
		}
	}

	// Called when the next question button is clicked
	public void displayMarksForNextQuestion(View view)
	{
		pageMarkTextView.setText(valueStore.getNextMarkDisplay());
	}

	// Called when the previous question button is clicked
	public void displayMarksForPreviousQuestion(View view)
	{
		pageMarkTextView.setText(valueStore.getPreviousMarkDisplay());
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
		else if (id == R.id.action_flag_script)
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
		else if (id == R.id.action_upload_script)
		{
			new BitmapMerger().execute();
		}
		else if (id == R.id.user_display)
		{
			new ProfilePopUpHelper(context, this.getLayoutInflater());
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) 
	{
		// Reset the scroll view when a new tab is selected
		scriptScrollView.setScrollY(0);
		prevGesturePoints = null;

		if (previousSObjects != null)
		{
			previousSObjects.clear();
		}

		String pageNum = tab.getText().toString().split(" ")[1];
		int page = Integer.parseInt(pageNum);

		currentPage = page;
		prevScore = valueStore.getPageScore(currentPage); // Get the previous mark for this page

		pageMarkTextView.setText(valueStore.getMarkDisplay());

		if (valueStore.getStoredView(page) != null)
		{
			sCanvasView.clearScreen();
			sCanvasView.setData(valueStore.getDrawingData(page));
		}
		else
		{
			initiliseSCanvas();
		}

		scriptDisplay.setImageBitmap(valueStore.getPage(page));

		ArrayList<String> listData = valueStore.getMemoForPage(page - 1);

		if (listData != null)
		{
			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
					context, 
					R.layout.memo_answer_item,R.id.memo_text/*android.R.layout.simple_list_item_1*/,
					listData);

			memoAnswersListView.setAdapter(arrayAdapter);
		}
		else
		{
			new AlertDialog.Builder(context)
			.setTitle("Unable to retrieve memo for this page. Please report this issue to your network admin.")
			.setPositiveButton("Continue", null)
			.setCancelable(false)
			.show();
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) 
	{
		valueStore.setPageScore(currentPage, currentPageScore + prevScore);
		currentPageScore = 0;

		valueStore.addViewToCollection(currentPage, sCanvasView, sCanvasView.getData());
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

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

	private class GestureRecognition extends AsyncTask<Object, String, Long>
	{
		// Stores details about the previous mark allocation that was made
		double previousMarkAllocated = 0;
		ArrayList<Float> previousPointsX = null;
		ArrayList<Float> previousPointsY = null;
		
		boolean isX = false;
		int previousMedian = 0;
		String tempMessage = "";
		
		@Override
		protected Long doInBackground(Object... params)
		{
			if (params.length == 2)
			{
				performGestureRecog((SCanvasView)params[0], (GestureMode) params[1]);
			}

			return null;
		}

		private void performGestureRecog(SCanvasView view, GestureMode currentMode)
		{
			LinkedList<SObject> sObjects = view.getSObjectList(true);

			ArrayList<Double> xList = new ArrayList<Double>();
			ArrayList<Double> yList = new ArrayList<Double>();
			double medianY = 0;

			// Used to keep track of the amount of each gesture
			HashMap<String, Integer> gestureCount = new HashMap<String, Integer>();

			boolean atLeastOneProcessed = false;
			boolean undo = false;
			int counter = 0;

			boolean doElse = true;

			if ((sObjects == null) || (sObjects.size() <= 0))
			{
				// No objects found
				currentPageScore = 0;

				if (previousSObjects != null)
				{
					if (previousSObjects.size() == 0)
					{
						previousSObjects.clear();
						return;
					}
					else
					{
						undo = true;
						sObjects.add(previousSObjects.removeLast());
					}
				}
			}
			if (doElse == true)
			{
				for (SObject objs : sObjects)
				{	
					counter++;
					yList = new ArrayList<Double>();
					xList = new ArrayList<Double>();

					if (currentMode == GestureMode.NORMAL)
					{
						// Check whether this sObject has already been processed
						if (previousSObjects.contains(objs))
						{
							// The user performed and undo operation
							if (counter == sObjects.size() && (atLeastOneProcessed == false) && (sObjects.size() < previousSObjects.size()))
							{
								objs = previousSObjects.removeLast();
								undo = true;
							}
							else
							{
								continue;
							}
						}
						else if (undo == false)
						{
							previousSObjects.add(objs);
						}
					}

					// Do not attempt to recognize comments
					if (objs.getClass().isInstance(new SObjectText()))
					{
						continue;
					}

					atLeastOneProcessed = true;

					PointF [][] currentPoints = new PointF[1][1];
					currentPoints[0] = ((SObjectStroke) objs).getPoints();
					
					HashMap<Double, ArrayList<Double>> currentGesturePoints = new HashMap<Double, ArrayList<Double>>();
					
					// Used to find the median yPosition of the tick
					for (int i = 0; i < currentPoints[0].length; i++)
					{
						double px = currentPoints[0][i].x;
						double py = currentPoints[0][i].y;

						xList.add(px);
						yList.add(py);
						
						if (currentGesturePoints.containsKey(py))
						{
							// Add the x coord to the ArrayList
							currentGesturePoints.get(py).add(px);
						}
						else
						{
							ArrayList<Double> temp = new ArrayList<Double>();
							temp.add(px);
							
							currentGesturePoints.put(py, (ArrayList<Double>) temp.clone());
						}
					}
					Collections.sort(xList);
					Collections.sort(yList);
					medianY = yList.get(yList.size() / 2);
					
					// Determine if an x is being processed
					if (prevGesturePoints != null)
					{
						for (Double key : currentGesturePoints.keySet())
						{
							if (prevGesturePoints.containsKey(key))
							{
								ArrayList<Double> prevTemp = prevGesturePoints.get(key);
								ArrayList<Double> currentTemp = currentGesturePoints.get(key);
								
								for (double x : prevTemp)
								{
									if (currentTemp.contains(x))
									{
										isX = true;
										tempMessage = "X";
										break;
									}
								}
								
								if (isX == true)
								{
									break;
								}
								
								tempMessage = "Y in common";
							}
						}
					}
					
					prevGesturePoints = (HashMap<Double, ArrayList<Double>>) currentGesturePoints.clone();
					
//					// Coords of the current Rect
//					int xMin = (int) Math.ceil(xList.get(0));
//					int yMin = (int) Math.ceil(yList.get(0));
//					int xMax = (int) Math.ceil(xList.get(xList.size() - 1));
//					int yMax = (int) Math.ceil(yList.get(yList.size() - 1));
//					
//					// Determine if an x is being processed
//					if (prevRect == null)
//					{
//						prevRect = objs.getRect();
//						
//						prevXMin = xMin;
//						prevYMin = yMin;
//						prevXMax = xMax;
//						prevYMax = yMax;
//					}
//					else
//					{
//						//Rect currentRect = new Rect(xMin, yMin, xMax, yMax);
//						RectF currentRect = objs.getRect();
//						
//						/*if (currentRect.contains(previousRect) || previousRect.contains(currentRect))
//						{
//							isX = true;
//							tempMessage = "Bounding Boxes Intersect";
//						}*/
//						
//						//if (Rect.intersects(previousRect, currentRect))
//						if (currentRect.intersect(prevRect))//(RectF.intersects(currentRect, prevRect))
//						{
//							isX = true;
//							//tempMessage = "Intersect";
//						}
//						else
//						{
//							//tempMessage = "No intersection";
//						}
//						
//						/*float d = (prevYMax - prevYMin) * (xMax - xMin) - (prevXMax - prevXMin) * (yMax - yMin);
//						
//						if (d == 0)
//						{
//							tempMessage = ("Not intersecting");
//						}
//						else
//						{
//							tempMessage = ("Intersecting");
//						}*/
//						
//						prevXMin = xMin;
//						prevYMin = yMin;
//						prevXMax = xMax;
//						prevYMax = yMax;
//						
//						prevRect = objs.getRect();
//					}

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
					}
					else
					{
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

				double markToBeAllocated = (undo == true) ? -(tickCount + (0.5 * halfTickCount)): (tickCount + (0.5 * halfTickCount));
				
				/*if (isX == true)
				{
					markToBeAllocated = -(previousMarkAllocated);
					valueStore.allocateMark((currentPage - 1), previousMedian, markToBeAllocated);
				}
				else
				{*/
					valueStore.allocateMark((currentPage - 1), (int) medianY, markToBeAllocated);
					previousMarkAllocated = Math.abs(markToBeAllocated);
				//}
				
				previousMedian = (int) medianY;

				currentPageScore += markToBeAllocated;

				// Ensure that the score never goes below 0
				currentPageScore = (currentPageScore < 0) ? 0 : currentPageScore;
			}
		}

		@Override
		protected void onPostExecute(Long params)
		{
			pageMarkTextView.setText(valueStore.getMarkDisplay());
			displayToast(tempMessage);

			// Check that mark allocation is within bounds
			if (valueStore.tooManyMarksAssignedToCurrentQuestion() == true)
			{
				// Display a mark allocation error
				new AlertDialog.Builder(context)
				.setCancelable(false)
				.setTitle("Mark allocation")
				.setMessage("Too many marks have been assigned to Question " + (valueStore.getIndexOfLastIncrementedQuestion() + 1))
				.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) 
					{
						// Dismiss the dialog
					}})
					.setNegativeButton("Undo Last Mark Allocation", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) 
						{
							sCanvasView.undo();
						}}
							).show();
			}
		}
	}

	private class BitmapMerger extends AsyncTask<Bitmap, String, Long>
	{
		ProgressDialog progressDialog;

		@Override
		protected Long doInBackground(Bitmap... params) 
		{
			prepareMergedBitmaps();

			return null;
		}

		private void prepareMergedBitmaps()
		{
			// Store the data for the current question
			valueStore.setPageScore(currentPage, currentPageScore + prevScore);
			currentPageScore = 0;

			valueStore.addViewToCollection(currentPage, sCanvasView, sCanvasView.getData());

			int pageCount = actionBar.getTabCount();
			Bitmap [] pageBitmaps = new Bitmap[pageCount];

			// Loops through each page and merge the overlays with the bitmap
			for (int i = 0; i < pageCount; i++)
			{
				sCanvasView.clearScreen();
				sCanvasView.setData(valueStore.getDrawingData(i + 1));

				pageBitmaps[i] = sCanvasView.getBitmap(true);
			}

			mergeBitmaps(pageBitmaps);
		}

		// Merge all pages with their overlays
		private void mergeBitmaps (Bitmap... overlays)
		{
			// Used to increase quality of saved overlay
			Paint painter = new Paint();
			painter.setFilterBitmap(true);

			double scalingFactor = 1.6;//2.3;
			int counter = 1;

			for (Bitmap overlay : overlays)
			{	
				//Bitmap baseBitmap = valueStore.getPage(counter);
				Bitmap temp = Bitmap.createBitmap(valueStore.getPage(counter).getWidth(), valueStore.getPage(counter).getHeight(), valueStore.getPage(counter).getConfig());
				Canvas drawCanvas = new Canvas (temp);

				drawCanvas.drawColor(Color.WHITE);

				int overlayWidth = overlay.getWidth();
				int overlayHeight = overlay.getHeight();

				drawCanvas.drawBitmap(valueStore.getPage(counter), new Matrix(), null);
				drawCanvas.drawBitmap(overlay, null, new Rect(45, 90, (int) (overlayWidth * scalingFactor), (int) (overlayHeight * scalingFactor)), painter);

				valueStore.addMergedBitmap(temp);

				counter++;
			}
		}

		@Override
		protected void onPreExecute()
		{
			bitmapsBeingMerged = true;
			progressDialog = ProgressDialog.show(MainMarkingScreenActivity.this, "", 
					"Preparing Files...", true);
		}

		@Override
		protected void onPostExecute(Long params)
		{
			bitmapsBeingMerged = false;
			progressDialog.dismiss();

			Intent scriptUploadScreen = new Intent(MainMarkingScreenActivity.this, ScriptFinalizeAndUploadActivity.class);
			startActivity(scriptUploadScreen);
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
