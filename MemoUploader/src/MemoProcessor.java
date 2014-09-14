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
	private String memoName;
	
	public MemoProcessor(String filename)
	{
		memoName = filename.split("\\.")[0].replaceAll(" ", "_");
		memoName += ".txt";
		
		openFile(filename);
		getAdditionalInformation();
		//getMemoText();
	}
	
	// Loads the file into memory
	private void openFile(String filename)
	{
		memoToProcess = new File(filename);
		
		// Check if the file exists
		if (!memoToProcess.exists())
		{
			System.out.println ("Unable to open file: " + filename);
			System.exit(0);
		}
		
		System.out.println ("File opened successfully");
	}
	
	// Get additional information from the text of the memo
	private void getAdditionalInformation()
	{
		try
		{
			PDDocument pdfDoc = PDDocument.load(memoToProcess);
			
			/*TextStripperHelper textHelper = new TextStripperHelper();
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
			}*/
			
			PDFTextStripper txtStripper = new PDFTextStripper();
			txtStripper.setStartPage(2);

			String docText = txtStripper.getText(pdfDoc);
			
			System.out.println (docText);
			
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
		
		// Add the lines of the last quesiton
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
		MemoProcessor mProc = new MemoProcessor("endMarkers.pdf");
	}
}
