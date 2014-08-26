package com.VictorZahraa.hybridmarker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.samm.common.SObject;
import com.samsung.samm.common.SObjectStroke;
import com.samsung.sdraw.StrokeInfo;
import com.samsung.spen.lib.gesture.SPenGestureInfo;
import com.samsung.spen.lib.gesture.SPenGestureLibrary;
import com.samsung.spen.settings.SettingStrokeInfo;
import com.samsung.spensdk.*;
import com.samsung.spensdk.applistener.SCanvasInitializeListener;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class MainScreenActivity extends Activity 
{
	// Used to display notifications to the user
	private Toast toast;
	
	private Context context;
	private RelativeLayout sCanvasContainer;
	private SCanvasView sCanvasView;
	
	// Fields required for gesture recognition
	private SPenGestureLibrary gestureLib;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        /*if (savedInstanceState == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
        
        context = this;
        sCanvasContainer = (RelativeLayout) findViewById(R.id.canvas_container);
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
        
        gestureLib = new SPenGestureLibrary(MainScreenActivity.this);
        gestureLib.openSPenGestureEngine();
        
        // Get the path to external storage
        String pathToSDCard = Environment.getExternalStorageDirectory().getPath();
        
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
        getMenuInflater().inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        if (id == R.id.menu_option_Pen)
        {
        	sCanvasView.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_PEN);
        	displayToast("Pen Mode Enabled");
        	return true;
        }
        else if (id == R.id.menu_option_Eraser)
        {
        	sCanvasView.setCanvasMode(SCanvasConstants.SCANVAS_MODE_INPUT_ERASER);
        	displayToast("Eraser Mode Enabled");
        	return true;
        }
        if (id == R.id.action_settings) 
        {
        	/*if (sCanvasView.isUndoable())
        	{
        		sCanvasView.undo();
        	}*/
        	
        	/*if(gestureLib.loadDefaultSPenGestureData())
        	{
        		displayToast("Default lib loaded");
        	}*/
        	
            return true;
        }
        else if (id == R.id.menu_option_recog_gesture)
        {
        	//detectGesture(null);
        	getViewBitmap(sCanvasView);
        	detectMultipleGestures(null);
        }
        else if (id == R.id.menu_option_view_pdf)
        {
        	Intent pdfViewScreen = new Intent(MainScreenActivity.this, PdfDisplayScreenActivity.class);
        	startActivity(pdfViewScreen);
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    // Used to test functionality of saving view to image
    public void getViewBitmap (View view)
    {
    	Bitmap image = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
    	Canvas bindCanvas = new Canvas(image);
    	
    	Drawable viewBackground = view.getBackground();
    	
    	if (viewBackground != null)
    	{
    		viewBackground.draw(bindCanvas);
    	}
    	else
    	{
    		bindCanvas.drawColor(Color.WHITE);
    	}
    	
    	view.draw(bindCanvas);
    	
    	ImageView tempImageView = (ImageView) findViewById(R.id.tempImageView);
    	tempImageView.setImageBitmap(image);
    }
    
    public void detectMultipleGestures(View view)
    {
    	LinkedList<SObject> sObjects = sCanvasView.getSObjectList(true);
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
			int scoreThreshold = 80; // The lower the number the great the chance of false positives
			
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
    	
    	// Create the string to be displayed
    	String resultString = "Ticks " + gestureCount.get("tick") + "\n" +
    						  "Half Ticks " + gestureCount.get("halfTick") + "\n" +
    						  "Crosses " + gestureCount.get("x");
    	
    	displayToast(resultString);
    	sCanvasView.clearScreen();
    }
    
    public void detectGesture(View view)
    {
    	LinkedList<SObject> sObjects = sCanvasView.getSObjectList(true);
    	
    	if ((sObjects == null) || (sObjects.size() <= 0))
    	{
    		// No objects found
    		return;
    	}
    	
    	PointF [][] currentPoints = new PointF[sObjects.size()][];
    	int index = 0;
    	
    	for (SObject obj : sObjects)
    	{
    		currentPoints[index] = ((SObjectStroke) obj).getPoints();
    		index++;
    	}
    	
    	ArrayList<SPenGestureInfo> gestureInfo = gestureLib.recognizeSPenGesture(currentPoints);

    	if ((gestureInfo == null) || (gestureInfo.size() <= 0))
    	{
    		// Gesture not recognized
    		return;
    	}
		
		int maxIndex = -1;
		int maxValue = -100;
		int scoreThreshold = 80; // The lower the number the great the chance of false positives
		
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
			displayToast("Not recognised");
			sCanvasView.clearScreen();
		}
		else
		{
			displayToast(gestureInfo.get(maxIndex).mName);
			sCanvasView.clearScreen();
		}
    }
    
    // Will display a toast with the given string
    private void displayToast(String textToDisplay)
    {
    	if (toast == null)
    	{
    		toast = Toast.makeText(this, textToDisplay, Toast.LENGTH_SHORT);
    	}
    	else
    	{
    		toast.setText(textToDisplay);
    	}
    	
    	toast.show();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_screen, container, false);
            return rootView;
        }
    }

}
