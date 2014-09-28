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
	private static ArrayList<ArrayList<Integer>> answerCoords;
	private static ArrayList<ArrayList<ArrayList<Integer>>> answerCoordsPerPage;
	private static ArrayList<String> questions;
	private static ArrayList<String> answers;
	private static ArrayList<ArrayList<String>> memoPerPage;
	private static ArrayList<Integer> answerCoordsOffset;

	// Used to store the marks
	private static double [] marksPerMainQuestion;
	private static double [][] subQuestionMarks;
	private static int numMainQuestions;
	private static ArrayList<Integer> numSubQuestions;
	private static ArrayList<ArrayList<Double>> maxMarks;
	private static boolean marksPerMainQuestionBeingModified;
	
	// Stores the text the user entered as part of the complaint about a script
	private static String scriptFlagText;
	private static boolean scriptFlagged;

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
	
	public void setFlagText (String txt) {scriptFlagText = txt; scriptFlagged = !(scriptFlagText.equals(""));}
	public String getFlagText () {return scriptFlagText;}
	
	public boolean isScriptFlagged () {return scriptFlagged;}

	// Returns the total marks for a questions
	public double getMarksForQuestion (int questionIndex)
	{
		while (marksPerMainQuestionBeingModified == true)
		{
			// wait
		}
		
		return marksPerMainQuestion[questionIndex];
	}
	
	// Returns the marks for the subquestions with the specified main question
	public String getMarksForSubQuestion(int questionIndex)
	{
		String returnVal = "";
		
		while (marksPerMainQuestionBeingModified == true)
		{
			// wait
		}
		
		for (int i = 0; i < subQuestionMarks[questionIndex].length; i++)
		{
			returnVal += "*" + subQuestionMarks[questionIndex][i];
		}
		
		return returnVal;
	}
	
	// Return a string representing the test marks as it is to be uploaded to the database
	public String getResultsInDBFormat()
	{
		String resultString = "";
		
		while (marksPerMainQuestionBeingModified == true)
		{
			// wait
		}
		
		for (int i = 0; i < numMainQuestions; i++)
		{
			resultString += "+" + marksPerMainQuestion[i];
			resultString += getMarksForSubQuestion(i);
		}
		
		return resultString;
	}
	
	public int getNumberOfMainQuestion () {return numMainQuestions;}

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
		scriptFlagText = "";
		scriptFlagged = false;

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
		marksPerMainQuestionBeingModified = false;
		int tempSubQuestionCount = 0;
		
		numSubQuestions = new ArrayList<Integer>();

		currentQuestion = 0;
		currentAnswer = 0;

		String [] headerAndBody = memoText.split("\\{HeaderEnd\\}");
		metadataFileHeader = headerAndBody[0];

		// Process the header of the metadata file
		processHeader(metadataFileHeader);

		int questionCounter = 0;

		String [] mainQuestions = headerAndBody[1].split("\\{MainQEnd\\}");

		for (String mainQ : mainQuestions)
		{
			questionCounter++;
			mainQ = mainQ.trim();

			// Skip blank lines
			if (mainQ.equals(""))
			{
				continue;
			}

			String [] subQuestions = mainQ.split("\\{SubQEnd\\}");

			for (String subQ : subQuestions)
			{
				subQ = subQ.trim();
				tempSubQuestionCount++;
				
				if (subQ.equals(""))
				{
					continue;
				}

				String [] qaAndCoords = subQ.split("\\{CoordsSplit\\}");
				ArrayList<Integer> temp = new ArrayList<Integer>();

				// Process and store the question and answer
				String [] qAndA = qaAndCoords[0].split("\\{QASplit\\}");

				questions.add(qAndA[0].trim());
				answers.add(qAndA[1].trim());

				// Process and store the answer coords
				String [] tempCoords = qaAndCoords[1].split(";");
				temp.add(Integer.parseInt(tempCoords[0].trim()));
				temp.add(Integer.parseInt(tempCoords[1].trim()));
				temp.add(questionCounter);
				temp.add(tempSubQuestionCount);

				answerCoords.add(temp);
			}
			
			numSubQuestions.add(tempSubQuestionCount);
			tempSubQuestionCount = 0;
		}
		
		getAnswerCoordsPerPage();
		marksPerMainQuestion = new double[numMainQuestions];
		subQuestionMarks = new double [numMainQuestions][];
		
		for (int i = 0; i < numMainQuestions; i++)
		{
			subQuestionMarks[i] = new double[numSubQuestions.get(i)];
		}
	}

	// Process the header of the metadata file
	private void processHeader(String header)
	{	
		maxMarks = new ArrayList<ArrayList<Double>>();

		String [] headerLines = header.split("\n");
		totalMarks = Integer.parseInt(headerLines[0]);

		numMainQuestions = 0;

		for (String line : headerLines)
		{
			if (line.startsWith("Q") || line.startsWith("q"))
			{
				numMainQuestions++;
			}
			else
			{
				line = line.replaceAll("\\{","");
				line = line.replaceAll("\\}", "");

				ArrayList<Double> tempMax = new ArrayList<Double>();

				String [] subMax = line.split(",");
				for (String max : subMax)
				{
					tempMax.add(Double.parseDouble(max));
				}

				maxMarks.add(tempMax);
			}
		}
	}

	// Process the answerPerPageText
	private void processAnswersPerPage(String perPageText)
	{
		memoPerPage = new ArrayList<ArrayList<String>>();
		answerCoordsOffset = new ArrayList<Integer>();
		ArrayList<String> temp = new ArrayList<String>();
		int counter = 0;

		// Get the answers for each page
		String [] pages = perPageText.split("\\{Page\\}");

		for (String page : pages)
		{
			answerCoordsOffset.add(counter);
			page = page.trim();

			if (page.equals(""))
			{
				continue;
			}

			String [] answersTemp = page.split("\\{AnswerSplit\\}");


			for (String ans: answersTemp)
			{
				counter++;
				temp.add(ans.trim());
			}

			// Add the answers for this page
			memoPerPage.add(temp);
			temp = new ArrayList<String>();
		}
	}

	// Return the memo for a given page
	public ArrayList<String> getMemoForPage(int page)
	{
		if (page != (memoPerPage.size()))
		{
			return memoPerPage.get(page);
		}
		else
		{
			return null;
		}
	}

	// Returns the coords of the given answer
	public int getStartAnswerCoords(int page, int selectedItemIndex)
	{
		double scalingFactor = 0.8;
		int returnVal = -1;

		returnVal = (int) (answerCoords.get(answerCoordsOffset.get(page) + selectedItemIndex).get(0) * scalingFactor);

		return returnVal;
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
		//String returnVal = answersPerPage.get(1).get(currentAnswer);

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
		if (score < 0)
		{
			scorePerPage[pageIndex] = 0;
		}
		else
		{
			scorePerPage[pageIndex] = score;
		}
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

	// Allocates marks to the appropriate question
	public String allocateMark (int pageNum, int coord, double mark)
	{
		// Ensures that only one thread executes this code at a time
		synchronized (this)
		{
			marksPerMainQuestionBeingModified = true;
			double scaling_factor = 0.83;

			if (marksPerMainQuestion == null)
			{
				getAnswerCoordsPerPage();
				marksPerMainQuestion = new double[numMainQuestions];
				subQuestionMarks = new double [numMainQuestions][];
				
				for (int i = 0; i < numMainQuestions; i++)
				{
					subQuestionMarks[i] = new double[numSubQuestions.get(i)];
				}
			}

			ArrayList<ArrayList<Integer>> pageCoords = answerCoordsPerPage.get(pageNum);
			boolean hasBeenAllocated = false;

			int minDiff = Integer.MAX_VALUE;
			int indexOfNearest = -1;

			for (int i = 0; i < pageCoords.size(); i++)
			{
				int startY = (int) (pageCoords.get(i).get(0) * scaling_factor);
				int endY = (int) Math.ceil(pageCoords.get(i).get(1) * scaling_factor);

				if ((startY <= coord) && (endY >= coord))
				{
					marksPerMainQuestion[pageCoords.get(i).get(2) - 1] = marksPerMainQuestion[pageCoords.get(i).get(2) - 1] +  mark;
					subQuestionMarks[pageCoords.get(i).get(2) - 1][pageCoords.get(i).get(3) - 1] = subQuestionMarks[pageCoords.get(i).get(2) - 1][pageCoords.get(i).get(3) - 1] +  mark;
					
					// Prevent scores from going below 0
					if (marksPerMainQuestion[pageCoords.get(i).get(2) - 1] < 0)
					{
						marksPerMainQuestion[pageCoords.get(i).get(2) - 1] = 0;
					}
					if (subQuestionMarks[pageCoords.get(i).get(2) - 1][pageCoords.get(i).get(3) - 1] < 0)
					{
						subQuestionMarks[pageCoords.get(i).get(2) - 1][pageCoords.get(i).get(3) - 1] = 0;
					}
							
					hasBeenAllocated = true;
					break;
				}
				else
				{
					if (Math.abs(coord - startY) < minDiff)
					{
						minDiff = Math.abs(coord - startY);
						indexOfNearest = i;
					}
					else if (Math.abs(coord - endY) < minDiff)
					{
						minDiff = Math.abs(coord - endY);
						indexOfNearest = i;
					}
				}
			}

			// Ensure that every tick is counted
			if (hasBeenAllocated == false)
			{
				marksPerMainQuestion[pageCoords.get(indexOfNearest).get(2) - 1] = marksPerMainQuestion[pageCoords.get(indexOfNearest).get(2) - 1] + mark;
				subQuestionMarks[pageCoords.get(indexOfNearest).get(2) - 1][pageCoords.get(indexOfNearest).get(3) - 1] = subQuestionMarks[pageCoords.get(indexOfNearest).get(2) - 1][pageCoords.get(indexOfNearest).get(3) - 1] +  mark;
				
				// Prevent scores from going below 0
				if (marksPerMainQuestion[pageCoords.get(indexOfNearest).get(2) - 1] < 0)
				{
					marksPerMainQuestion[pageCoords.get(indexOfNearest).get(2) - 1] = 0;
				}
				if (subQuestionMarks[pageCoords.get(indexOfNearest).get(2) - 1][pageCoords.get(indexOfNearest).get(3) - 1] < 0)
				{
					subQuestionMarks[pageCoords.get(indexOfNearest).get(2) - 1][pageCoords.get(indexOfNearest).get(3) - 1] = 0;
				}
				
				hasBeenAllocated = true;
			}

			marksPerMainQuestionBeingModified = false;
			return "ALLOCATED";
		}
	}

	// Gets the answer Coords per page
	private void getAnswerCoordsPerPage()
	{
		answerCoordsPerPage = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<Integer>> temp = new ArrayList<ArrayList<Integer>>();
		int prevY = -1;

		for (int i = 0; i < answerCoords.size(); i++)
		{
			int currentY = answerCoords.get(i).get(0);

			if (currentY < prevY)
			{
				answerCoordsPerPage.add((ArrayList<ArrayList<Integer>>) temp.clone());
				temp = new ArrayList<ArrayList<Integer>>();
				temp.add(answerCoords.get(i));
			}
			else
			{
				temp.add(answerCoords.get(i));
			}

			prevY = currentY;
		}

		if (temp.size() != 0)
		{
			answerCoordsPerPage.add((ArrayList<ArrayList<Integer>>) temp.clone());
		}
	}
}
