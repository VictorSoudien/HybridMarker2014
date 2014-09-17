/* Process a memo and all text contained in it
 * Author: Victor Soudien
 * Date: 27 August 2014
 * Student Number: SDNVIC001
 */

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFTextStripper;

public class MemoProcessor
{
	private File memoToProcess;
	private File blankScript;
	private String memoName;
	
	// Used to track the coordinates of answer sections
	private ArrayList<Double> pixelCoords;
	private ArrayList<Integer> arrayOffset;
	
	private ArrayList<String> mainQuestions;
	private ArrayList<String> subQuestions;
	private ArrayList<String> answers;
	
	// Keeps track of which question a set of sub-questions and answers belongs to
	private ArrayList<Integer> mainQuestionIndex;
	
	// The strings which will be written to the file
	private String outputHeader;
	
	public MemoProcessor(String filename, String blankScriptFilename)
	{	
		memoName = filename.split("\\.")[0].replaceAll(" ", "_");
		memoName += ".txt";
		
		outputHeader = "";
		pixelCoords = new ArrayList<Double>();
		arrayOffset = new ArrayList<Integer>();
		arrayOffset.add(0); // offset of the first entry is zero
		
		mainQuestions = new ArrayList<String>();
		subQuestions = new ArrayList<String>();
		answers = new ArrayList<String>();
		mainQuestionIndex = new ArrayList<Integer>();
		
		openFiles(filename, blankScriptFilename);
		getTextInformation();
		//getAdditionalInformation();
		//getMemoText();
	}
	
	// Loads the file into memory
	private void openFiles(String filename, String blankScriptFilename)
	{
		memoToProcess = new File(filename);
		
		// Check if the file exists
		if (!memoToProcess.exists())
		{
			System.out.println ("Unable to open file: " + filename);
			System.exit(0);
		}
		
		blankScript = new File(blankScriptFilename);
		
		// Check if the file exists
		if (!blankScript.exists())
		{
			System.out.println ("Unable to open file: " + blankScriptFilename);
			System.exit(0);
		}
		
		System.out.println ("Files opened successfully");
	}
	
	// Gets the text and pixel locations of the text on the pdf
	private void getTextInformation()
	{
		try
		{
			// Process the blank script to get accurate text coords
			PDDocument blankScriptPDF = PDDocument.load(blankScript);
			TextStripperHelper txtHelper = new TextStripperHelper();
			List<PDPage> pages = blankScriptPDF.getDocumentCatalog().getAllPages();
			
			PDFTextStripper txtStripper = new PDFTextStripper();
			
			// Start from 1 since the cover page does not need to be processed
			for (int i = 1; i < pages.size(); i++)
			{
				System.out.println ("Processing Page..." + i);
				PDStream pageContent = pages.get(i).getContents();
				
				if (pageContent != null)
				{
					PDPage currentPage = pages.get(i);
					txtHelper.processStream(currentPage, currentPage.findResources(), currentPage.getContents().getStream());
					
					String [] currentCoords = txtHelper.getResult().split("\\n");
					
					for (String s : currentCoords)
					{
						//System.out.println (s);
						pixelCoords.add(Double.parseDouble(s));
					}
					
					// Calculate the offset of the current data in the pixelCoords array
					int offset = arrayOffset.get(i - 1) + currentCoords.length;
					arrayOffset.add(offset);
				}
			}
			
			PDDocument memoPDF = PDDocument.load(memoToProcess);
			txtStripper.setStartPage(2);
			String docText = txtStripper.getText(memoPDF);
			processMemoText(docText, txtStripper.getLineSeparator());
			//splitTextIntoQuestions(docText);			
		}
		catch (Exception e)
		{
			System.out.println ("Error while retrieving text: " + e.getMessage());
		}
	}
	
	// Processes the text in order to split it into questions and answers
	private void processMemoText(String memoText, String lineSeparator)
	{
		String [] lines = memoText.split(lineSeparator);
		
		String currentLine = "";
		boolean inAnswerSection = false;
		
		int answerStartIndex = 0;
		int answerEndIndex = 0;
		
		// Holds the current unassigned lines. Lines are assigned as being either question or answer sections.
		String tempSection = "";
		
		for (int i = 0; i < lines.length; i++)
		{
			if (lines[i].equalsIgnoreCase(""))
			{
				continue;
			}
			
			currentLine = lines[i].trim(); 
			currentLine += "\n";
			
			if (currentLine.contains("]$"))
			{
				System.out.println (currentLine);
			}
			
			if ((currentLine.indexOf("Question") == 0) || (currentLine.indexOf("question") == 0))
			{
				mainQuestionIndex.add(subQuestions.size());
				mainQuestions.add(currentLine.replaceAll("[ ]+", " "));
				
				// Anything before this can be assumed to be additional information e.g. page numbers
				tempSection = "";
				
				// Make sure there are only single spaces in the text
				outputHeader += currentLine.replaceAll("[ ]+", " ") + "\n";
				continue;
			}
			else if (inAnswerSection == true)
			{
				int indexOfEndMarker = currentLine.indexOf("{end}");
				
				// Check if this is the last line of the answer
				if (indexOfEndMarker != -1)
				{
					inAnswerSection = false;
					answerEndIndex = i;
					
					tempSection += currentLine.substring(0, indexOfEndMarker);
					
					tempSection += "\n{CoordSplit}\n" + pixelCoords.get(answerStartIndex) + "\n" + pixelCoords.get(answerEndIndex);
					
					answers.add(tempSection);
					tempSection = "";
				}
				else
				{
					tempSection += currentLine;
				}
				
				continue;
			}
			else if (currentLine.split("]$").length != 1)
			{
				inAnswerSection = true;
				answerStartIndex = (i + 1);
				
				tempSection += currentLine;
				subQuestions.add(tempSection);
				tempSection = "";
				
				continue;
			}
			else 
			{
				tempSection += currentLine;
			}
		}
		
		//System.out.println ();
		//System.out.println(answers.get(2));
		
		writeMetaFile();
	}
	
	// Write the metadata to the file
	private void writeMetaFile ()
	{
		int lowerBound = 0;
		int upperBound = 0;
		
		for (int i = 0; i < mainQuestions.size(); i++)
		{
			System.out.print (mainQuestions.get(i));
			
			if (i != (mainQuestions.size() - 1))
			{
				upperBound = mainQuestionIndex.get(i + 1);
			}
			else
			{
				upperBound = subQuestions.size();
			}
			
			for (int iter = lowerBound; iter < upperBound; iter++)
			{
				System.out.print (subQuestions.get(iter));
				System.out.println ("{QASplit}");
				System.out.println (answers.get(iter));
				System.out.println ("{SubQEnd}");
			}
			
			System.out.println ("{MainQEnd}");
			lowerBound = upperBound;
		}
	}
	
	// Get additional information from the text of the memo
	private void getAdditionalInformation()
	{
		try
		{
			PDDocument pdfDoc = PDDocument.load(memoToProcess);
			
			TextStripperHelper textHelper = new TextStripperHelper();
			List<PDPage> pages = pdfDoc.getDocumentCatalog().getAllPages();
			
			int num = 0;
			
			for (PDPage page : pages)
			{
				num++;
				
				// Skip the first page
				if (num == 1)
				{
					continue;
				}
				
				System.out.println ("Processing page... " + num);
				
				PDStream stream = page.getContents();
				if (stream != null)
				{
					//List<?> list = stream.getStream().getStreamTokens();
					//for (int i = 0; i < list.size(); i++)
					//{
						//System.out.println (list.get(i));
					//}
					textHelper.processStream(page, page.findResources(), page.getContents().getStream());
				}
			}
			
			/*PDFTextStripper txtStripper = new PDFTextStripper();
			txtStripper.setStartPage(2);

			String docText = txtStripper.getText(pdfDoc);
			
			System.out.println (docText);*/
			
			// Close the file when processing completes
			pdfDoc.close();
		}
		catch (Exception e)
		{
			System.out.println ("Error in getAdditionalInfo\n" + e.getMessage());
		}
	}
	
	// Get the text from the memo
	private void getMemoText()
	{
		try
		{
			PDDocument pdf = PDDocument.load(memoToProcess);
			
			PDFTextStripper textStripper = new PDFTextStripper();
			textStripper.setStartPage(2);
			String extractedText = textStripper.getText(pdf);
			
			splitTextIntoQuestions(extractedText);
			
			// Close the file when processing completes
			pdf.close();
		}
		catch (IOException e)
		{
			System.out.println ("Unable to load file");
		}
	}
	
	// Identifies all text associated with each question
	private void splitTextIntoQuestions(String text)
	{
		ArrayList<String> questions = new ArrayList<String>();
		String [] lines = text.split("\\n");
		
		String temp = "";
		
		for (String line : lines)
		{
			line = line.trim();
			// Question 1 treated separately since nothing before it should be added to the array list
			if ((line.indexOf("Question 1") == 0) || (line.indexOf("question 1") == 0))
			{
				temp = line;
			}
			else if ((line.indexOf("Question") == 0) || (line.indexOf("question") == 0))
			{
				questions.add(temp);
				temp = line;
			}
			else
			{
				temp += "\n" + line;
			}
		}
		
		// Add the lines of the last question
		questions.add(temp);
		
		splitIntoQuestionsAndAnswers(questions);
		//System.out.println (questions.get(0));
	}
	
	// Processes each question into the question and associated answer
	private void splitIntoQuestionsAndAnswers(ArrayList<String> sections)
	{
		PrintWriter writer = null;
		
		try
		{
			writer = new PrintWriter(new File (memoName));
			
			int iterCounter = 0;
			
			for (String question : sections)
			{
				iterCounter++;
				
				String [] lines = question.split("\\n");
				
				String questionNumber = "Question " + iterCounter;
				String totalMarkAllocation = lines[0].substring(lines[0].indexOf("["));
				
				ArrayList<String> subQuestions = new ArrayList<String>();
				ArrayList<String> subAnswers = new ArrayList<String>();
				
				boolean startOfAnswerFound = false;
				String tempQuestion = "";
				String tempAnswer = "";
				String prevLine = "null";
				
				for (int i = 1; i < lines.length; i++)
				{
					String currentLine = lines[i] + "\n";
					
					if (currentLine.equals(prevLine))
					{
						continue;
					}
					else
					{
						prevLine = currentLine;
					}
					
					// if there is a closing square bracket at the end of a line then it's the last line of a question
					if (currentLine.split("]$").length != 1)
					{
						startOfAnswerFound = true;
						
						tempQuestion += currentLine;
						subQuestions.add(tempQuestion);
						tempQuestion = "";
						
						continue;
					}
					else if (startOfAnswerFound == true)
					{
						int indexOfEndMarker = currentLine.indexOf("{end}");
						
						// Check if this is the last line of the answer
						if (indexOfEndMarker != -1)
						{
							startOfAnswerFound = false;
							
							tempAnswer += currentLine.substring(0, indexOfEndMarker);
							subAnswers.add(tempAnswer);
							tempAnswer = "";
						}
						else
						{
							tempAnswer += currentLine;
						}
						
						continue;
					}
					else
					{
						tempQuestion += currentLine;
					}
				}
				
				writer.println(questionNumber + "\t" + totalMarkAllocation);
				
				for (int i = 0; i < subQuestions.size(); i++)
				{
					writer.println ("Question:");
					writer.println (subQuestions.get(i));
					writer.println ("Answer:");
					writer.println (subAnswers.get(i));
				}
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error while writing memo data to file: " + memoName);
		}
		finally
		{
			if (writer != null)
			{
				writer.close();
				System.out.println ("Memo data written to file: " + memoName);
			}
		}
	}
	
	public static void main (String [] args)
	{
		new MemoProcessor("test.pdf", "testAsHandedToStudents.pdf");
	}
}
