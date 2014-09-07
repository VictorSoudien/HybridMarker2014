package com.VictorZahraa.hybridmarker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.view.View;

import com.samsung.spensdk.SCanvasView;
import com.samsung.samm.common.SObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

public class ValueStoringHelperClass 
{
	private static int numPages;
	private static double [] scorePerPage;
	private static SCanvasView [] sCanvasCollection;
	private static byte [][] drawingData;
	private static Bitmap [] viewBitmaps;
	private static String [] SAMMData;
	
	private static ArrayList<LinkedList<SObject>> sObjectList;
	
	private static String pathToSDCard;
	private static ArrayList<Bitmap> pageBitmaps;
	
	public ValueStoringHelperClass()
	{
		// Get the path to external storage
        pathToSDCard = Environment.getExternalStorageDirectory().getPath();
        sObjectList = new ArrayList<LinkedList<SObject>>();
	}
	
	public void initPageCollection()
	{
		pageBitmaps = new ArrayList<Bitmap>();
	}
	
	public void setNumPages (int value) 
	{
		numPages = value;
		scorePerPage = new double [value];
		sCanvasCollection = new SCanvasView [value];
		drawingData = new byte[value][];
		
		SAMMData = new String[value];
		
		viewBitmaps = new Bitmap[value];
	}
	
	public boolean addPage (int pageNum)
	{
		File page = new File (pathToSDCard + "/page" + pageNum + ".png");
		Bitmap imageBitmap = BitmapFactory.decodeFile(page.getAbsolutePath());
		
		if (imageBitmap != null)
		{
			pageBitmaps.add(imageBitmap);
			return true;
		}
		else
		{
			// Display an error message
			return false;
		}
	}
	
	public Bitmap getPage(int index) {return pageBitmaps.get(index);}
	
	public int getNumPage () {return numPages;}
	
	public void setPageScore (int pageIndex, double score)
	{
		scorePerPage[pageIndex] = score;
	}
	
	public double getPageScore (int pageIndex) {return scorePerPage[pageIndex];}
	
	public void addViewToCollection (int index, SCanvasView view, byte [] drawData, String sammData, LinkedList<SObject> sObjList)
	{
		sCanvasCollection[index] = view;
		drawingData[index] = drawData;
		//viewBitmaps[index] = bitmap;
		
		sObjectList.add(sObjList);
		
		SAMMData[index] = sammData;
	}
	
	public Bitmap getMergedBitmap()
	{
		Bitmap test = pageBitmaps.get(1);
		Bitmap saved = Bitmap.createBitmap(test.getWidth(), test.getHeight(), test.getConfig());
		Canvas canvas = new Canvas(saved);
		canvas.drawBitmap(test, new Matrix(), null);
		View v = sCanvasCollection[0];
		v.draw(canvas);
		
		return saved;
	}
	
	public LinkedList<SObject> getSObjectList(int index){return sObjectList.get(index);}
	
	public String getSAMMData (int index) {return SAMMData[index];}
	
	public byte[] getDrawingData(int index){return drawingData[index];}
	
	public SCanvasView getStoredView (int index) {return sCanvasCollection[index];}
}
