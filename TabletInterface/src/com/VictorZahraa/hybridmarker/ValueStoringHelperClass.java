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
	private static int totalMarks;
	private static SCanvasView [] sCanvasCollection;
	private static byte [][] drawingData;
	
	private static String pathToSDCard;
	private static ArrayList<Bitmap> pageBitmaps;
	
	// Stores the text of the memo
	private static String memoText;
	private static String[] questionsAndAnswers;
	private static int currentQuestion;
	private static int currentAnswer;
	
	public static Bitmap merged;
	
	// The list of merged bitmaps
	private static ArrayList<Bitmap> mergedBitmaps;
	
	// The directory of the currentTest
	public static String currentDirectory;
	public static String nameOfTestBeingMarked;
	
	public ValueStoringHelperClass()
	{
		// Get the path to external storage
        pathToSDCard = Environment.getExternalStorageDirectory().getPath();
	}
	
	public void setCurrentDirectory (String value) {currentDirectory = value;}
	public String getCurrentDirectory () {return currentDirectory;}
	
	public void setTestName (String value) {nameOfTestBeingMarked = value;}
	public String getTestName () {return nameOfTestBeingMarked;}
	
	public int getTotalMark () {return totalMarks;}
	
	public double getSumOfPageScores ()
	{
		double sum = 0;
		
		for (double num : scorePerPage)
		{
			sum += num;
		}
		
		return sum;
	}
	
	public void initPageCollection()
	{
		pageBitmaps = new ArrayList<Bitmap>();
		mergedBitmaps = new ArrayList<Bitmap>();
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
			
			totalMarks = Integer.parseInt(questionsAndAnswers[0].split("\n")[0]);
			
			buffReader.close();
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
	
	public void addMergedBitmap (Bitmap merged)
	{
		mergedBitmaps.add(merged);
	}
	
	public Bitmap getMergedBitmap (int index)
	{
		return mergedBitmaps.get(index);
	}
}
