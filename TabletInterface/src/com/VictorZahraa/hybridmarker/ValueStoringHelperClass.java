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

	// These data structures are used to store the information regarding the test memo
	private static String metadataFileHeader;
	private static ArrayList<ArrayList<String>> answersPerPage;
	private static ArrayList<ArrayList<Integer>> answerCoords;
	private static ArrayList<String> questions;
	private static ArrayList<String> answers;
	
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

	public void processMemoText (String memoFilename, String questionsPerPageFilename)
	{
		File memo = new File (pathToSDCard + "/" + memoFilename);
		File perPageFile = new File (pathToSDCard + "/" + questionsPerPageFilename);

		StringBuilder textBuilder = new StringBuilder();

		try
		{
			// Read in the memo
			BufferedReader buffReader = new BufferedReader(new FileReader(memo));
			String currentLine;

			while ((currentLine = buffReader.readLine()) != null)
			{
				textBuilder.append(currentLine);
				textBuilder.append("\n");
			}

			String memoText = textBuilder.toString();
			processTextFromMemo(memoText);
			
			buffReader.close();
			textBuilder = new StringBuilder();

			// Read the answers per page file
			buffReader = new BufferedReader(new FileReader(perPageFile));

			while ((currentLine = buffReader.readLine()) != null)
			{
				textBuilder.append(currentLine);
				textBuilder.append("\n");
			}
			
			String perPageText = textBuilder.toString();
			processAnswersPerPage(perPageText);
		}
		catch (Exception e)
		{
			// HANDLE ERROR
		}
	}
	
	// Process the memoText
	private void processTextFromMemo(String memoText)
	{
		questions = new ArrayList<String>();
		answers = new ArrayList<String>();
		answerCoords = new ArrayList<ArrayList<Integer>>();
		
		currentQuestion = 0;
		currentAnswer = 0;
		
		String [] headerAndBody = memoText.split("{HeaderEnd}");
		metadataFileHeader = headerAndBody[0];
		
		String [] mainQuestions = headerAndBody[1].split("{MainQEnd}");
		
		for (String mainQ : mainQuestions)
		{
			String [] subQuestions = mainQ.split("{SubQEnd}");
			
			for (String subQ : subQuestions)
			{
				String [] qaAndCoords = subQ.split("{CoordsSplit}");
				ArrayList<Integer> temp = new ArrayList<Integer>();
				
				// Process and store the question and answer
				String [] qAndA = qaAndCoords[0].split("{QASplit}");
				
				questions.add(qAndA[0]);
				answers.add(qAndA[1]);
				
				// Process and store the answer coords
				String [] tempCoords = qaAndCoords[1].split(";");
				temp.add(Integer.parseInt(tempCoords[0]));
				temp.add(Integer.parseInt(tempCoords[1]));
				
				answerCoords.add(temp);
			}
		}
	}

	// Process the answerPerPageText
	private void processAnswersPerPage(String perPageText)
	{
		answersPerPage = new ArrayList<ArrayList<String>>();
		
		// Get the answers for each page
		String [] pages = perPageText.split("{Page}");

		for (String page : pages)
		{
			String [] answers = page.split("{AnswerSplit}");
			ArrayList<String> temp = new ArrayList<String>();

			for (String ans: answers)
			{
				temp.add(ans);
			}

			// Add the answers for this page
			answersPerPage.add(temp);
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
		/*try
		{
			currentAnswer += 2;
			String ans = questionsAndAnswers[currentAnswer];

			return ans.trim();
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			currentAnswer -= 2;
			return "No more answers";
		}*/
		
		String returnVal = answers.get(currentAnswer);
		
		currentAnswer = (currentAnswer != answers.size() - 1) ? (currentAnswer + 1) : currentAnswer;
		
		return returnVal;
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

	public void recycleBitmaps()
	{
		for (Bitmap b : pageBitmaps)
		{
			b.recycle();
		}
		for (Bitmap bitmap : mergedBitmaps)
		{
			bitmap.recycle();
		}
	}
}
