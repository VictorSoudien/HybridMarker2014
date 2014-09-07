package com.VictorZahraa.hybridmarker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.samsung.spensdk.SCanvasView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ValueStoringHelperClass 
{
	private static int numPages;
	private static double [] scorePerPage;
	private static SCanvasView [] sCanvasCollection;
	private static byte [][] drawingData;
	
	private static String pathToSDCard;
	private static ArrayList<Bitmap> pageBitmaps;
	
	// Stores the text of the memo
	private static String memoText;
	private static String[] questionsAndAnswers;
	private static int currentQuestion;
	private static int currentAnswer;
	
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
	
	public int getNumPage () {return numPages;}
	
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
	
	public void setMemoText (String filename)
	{
		File textFile = new File (pathToSDCard + "/" + filename);
		StringBuilder textBuilder = new StringBuilder();
		
		try
		{
			BufferedReader buffReader = new BufferedReader(new FileReader(textFile));
			String currentLine;
			
			while ((currentLine = buffReader.readLine()) != null)
			{
				textBuilder.append(currentLine);
				textBuilder.append("\n");
			}
			
			memoText = textBuilder.toString();
			questionsAndAnswers = memoText.split("<split_marker>");
			currentQuestion = -1;
			currentAnswer = 0;
		}
		catch (Exception e)
		{
			// Display error message
		}
	}

	public String getMemoText () {return memoText;}
	
	public String getNextQuestion()
	{
		try
		{
			currentQuestion += 2;
			String q = questionsAndAnswers[currentQuestion];
			
			return q.trim();
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			currentQuestion -= 2;
			return "No more questions";
		}
	}
	
	public String getNextAnswer()
	{
		try
		{
			currentAnswer += 2;
			String ans = questionsAndAnswers[currentAnswer];
			
			return ans.trim();
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			currentAnswer -= 2;
			return "No more answers";
		}
	}
	
	public String getPreviousQuestion()
	{
		try
		{
			currentQuestion -= 2;
			String q = questionsAndAnswers[currentQuestion];		
			
			return q.trim();
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			currentQuestion += 2;
			return "No more questions";
		}
	}
	
	public String getPreviousAnswer()
	{
		try
		{
			currentAnswer -= 2;
			String ans = questionsAndAnswers[currentAnswer];
			
			return ans.trim();
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			currentAnswer += 2;
			return "No more answers";
		}
	}
	
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
