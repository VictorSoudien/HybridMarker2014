/* Process a memo and all text contained in it
 * Author: Victor Soudien
 * Date: 27 August 2014
 * Student Number: SDNVIC001
 */

import java.io.*;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class MemoProcessor 
{
	private File memoToProcess;
	
	public MemoProcessor(String filename)
	{
		openFile(filename);
		getMemoText();
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
	
	// Get the text from the memo
	private void getMemoText()
	{
		try
		{
			PDDocument pdf = PDDocument.load(memoToProcess);
			
			PDFTextStripper textStripper = new PDFTextStripper();
			textStripper.setStartPage(2);
			//textStripper.setAddMoreFormatting(true);
			String extractedText = textStripper.getText(pdf);
			
			splitTextIntoQuestions(extractedText);
			
			//System.out.println (extractedText);
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
			// Question 1 treated separately since nothing before it should be added to the array list
			if ((line.indexOf("Question 1") == 0) || (line.indexOf("question 1") == 0))
			{
				//questions.add(temp);
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
		
		System.out.println (questions.get(0));
	}
	
	public static void main (String [] args)
	{
		MemoProcessor mProc = new MemoProcessor("ClassTest4.pdf");
	}
}
