package com.VictorZahraa.hybridmarker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.samsung.spensdk.SCanvasView;

import java.io.File;
import java.util.ArrayList;

public class ValueStoringHelperClass 
{
	private static int numPages;
	private static double [] scorePerPage;
	private static SCanvasView [] sCanvasCollection;
	private static byte [][] drawingData;
	
	private static String pathToSDCard;
	private static ArrayList<Bitmap> pageBitmaps;
	
	public ValueStoringHelperClass()
	{
		// Get the path to external storage
        pathToSDCard = Environment.getExternalStorageDirectory().getPath();
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
	
	public void addViewToCollection (int index, SCanvasView view, byte [] drawData)
	{
		sCanvasCollection[index] = view;
		drawingData[index] = drawData;
	}
	
	public SCanvasView getStoredView (int index) {return sCanvasCollection[index];}
	
	public byte[] getDrawingData(int index){return drawingData[index];}
}
