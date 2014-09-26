/* Processes a given memo in order to determine the answer regions and memo text
 * Author: Victor Soudien
 * Date: 24 September 2014
 * Student Number: SDNVIC001
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.opencv.core.Core;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class FinalMemoProcessor 
{
	private File memoFile;
	private File blankScriptFile;

	// The name of the output file
	private String outputFileName;
	
	// The name of the text file which contains the answers per page
	private String answersPerPageOutputFile;
	private String answersPerPageText; // The data to be written to the file

	// Stores the answer regions in terms of their start and end y position
	private ArrayList<String> answerRegions;

	// Keeps track of which question a set of sub-questions and answers belongs to
	private ArrayList<Integer> mainQuestionIndex;

	private ArrayList<String> mainQuestions;
	private ArrayList<String> subQuestions;
	private ArrayList<String> answers;

	// The strings which will be written to the file
	private String outputHeader;

	public FinalMemoProcessor (String memoFileName, String blankScriptFileName, String dir)
	{		
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

		dir = dir.trim();
		
		// Verify the ouput directory
		isDirValid(dir);
		
		outputFileName = memoFileName.split("\\.")[0].replaceAll(" ", "_");
		answersPerPageOutputFile = outputFileName + "_answersPerPage.txt";
		
		outputFileName += ".txt";
		
		if (dir.endsWith("/"))
		{
			outputFileName = dir + outputFileName;
			answersPerPageOutputFile = dir + answersPerPageOutputFile;
		}
		else
		{
			outputFileName = dir + "/" + outputFileName;
			answersPerPageOutputFile = dir + "/" + answersPerPageOutputFile;
		}

		outputHeader = "";
		answersPerPageText = "";

		answerRegions = new ArrayList<String>();
		mainQuestionIndex = new ArrayList<Integer>();
		mainQuestions = new ArrayList<String>();
		subQuestions = new ArrayList<String>();
		answers = new ArrayList<String>();

		openFiles(memoFileName, blankScriptFileName);
		getAnswerRegions();
		getMemoText();
	}

	// Test whether the output directory is valid
	private boolean isDirValid(String directory)
	{
		File dirCheck = new File (directory);

		if ((dirCheck.exists() == true) && (dirCheck.isDirectory() == false))
		{
			System.out.println ("Please provide a valid output directory");
			System.exit(0);
		}

		return true;
	}

	// Load the files that need to be processed into memory
	private void openFiles (String memoFileName, String blankScriptFileName)
	{
		memoFile = new File(memoFileName);
		blankScriptFile = new File(blankScriptFileName);

		// Check if the file exists
		if (!memoFile.exists())
		{
			System.out.println ("Unable to open file: " + memoFileName);
			System.exit(0);
		}

		// Check if the file exists
		if (!blankScriptFile.exists())
		{
			System.out.println ("Unable to open file: " + blankScriptFileName);
			System.exit(0);
		}

		System.out.println ("Files opened successfully");
	}

	// Get the start and end y coordinates of the answer regions
	private void getAnswerRegions()
	{
		try
		{
			PDDocument blankScript = PDDocument.load(blankScriptFile);
			List<PDPage> pages = blankScript.getDocumentCatalog().getAllPages();

			HorizontalLineDetection lineDetector = new HorizontalLineDetection();

			// Process each page and get the answer regions
			for (int i = 1; i < pages.size(); i++)
			{
				File output = new File("temp/page.png");
				ImageIO.write(pages.get(i).convertToImage(), "png", output);

				ArrayList<String> result = lineDetector.processImage("temp/page.png");
				output.delete();

				for (String line : result)
				{
					answerRegions.add(line);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println ("An error occured while detecting answer regions:\n" + e);
			System.exit(0);
		}
	}

	// Get the text of the memo
	private void getMemoText()
	{
		try
		{
			PDDocument memoPDF = PDDocument.load(memoFile);
			PDFTextStripper txtStripper = new PDFTextStripper();
			txtStripper.setStartPage(2);
			String docText = txtStripper.getText(memoPDF);

			for (int i = 2; i <= memoPDF.getNumberOfPages(); i++)
			{
				txtStripper.setStartPage(i);
				txtStripper.setEndPage(i);
				String tempText = txtStripper.getText(memoPDF);
				getMemoTextPerPage(tempText, txtStripper.getLineSeparator(), (i == memoPDF.getNumberOfPages()));
			}
			
			// Process the entire document
			processMemoText(docText, txtStripper.getLineSeparator(), true);
		}
		catch (Exception e)
		{
			System.out.println ("Error while getting memo text:");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	// Get memo text for each page
	private void getMemoTextPerPage(String text, String lineSeparator, boolean lastPage)
	{
		processMemoText(text, lineSeparator, false);
		
		for (int i = 0; i < answers.size(); i++)
		{
			answersPerPageText += answers.get(i);
			
			if (i != answers.size() - 1)
			{
				answersPerPageText += "\n{AnswerSplit}";
			}
		}
		
		if (lastPage == false)
		{
			answersPerPageText += "\n{Page}";
		}
		
		mainQuestionIndex.clear();
		mainQuestions.clear();
		answers.clear();
		subQuestions.clear();
		
		outputHeader = "";
	}

	// Processes the text in order to split it into questions and answers
	private void processMemoText(String memoText, String lineSeparator, boolean writeOut)
	{
		String [] lines = memoText.split(lineSeparator);

		String currentLine = "";
		boolean inAnswerSection = false;

		int answerStartIndex = 0;
		int answerEndIndex = 0;

		int answerCounter = 0;
		
		int lastPagePrintIndex = 0;

		// Holds the current unassigned lines. Lines are assigned as being either question or answer sections.
		String tempSection = "";
		
		String subTotalMarks = "{";

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

				if (!subTotalMarks.equals("{"))
				{
					outputHeader += subTotalMarks + "}\n";
					subTotalMarks = "{";
				}
				
				// Make sure there are only single spaces in the text
				outputHeader += currentLine.replaceAll("[ ]+", " ");
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

					answerCounter++;

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
				
				String tempMarks = currentLine.substring(currentLine.lastIndexOf("[") + 1, currentLine.lastIndexOf("]"));
				
				if (subTotalMarks.equals("{"))
				{
					subTotalMarks += tempMarks;
				}
				else
				{
					subTotalMarks += "," + tempMarks;
				}

				continue;
			}
			else 
			{
				tempSection += currentLine;
			}
		}
		
		// Add the subQuestion marks for the last question to the header
		if (!subTotalMarks.equals("{"))
		{
			outputHeader += subTotalMarks + "}\n";
			subTotalMarks = "{";
		}

		if (writeOut == true)
		{
			writeMetaFiles();
		}
	}

	// Write the metadata to the file
	private void writeMetaFiles ()
	{
		int lowerBound = 0;
		int upperBound = 0;

		PrintWriter fileWriter = null;
		PrintWriter answerPerPageWriter = null;

		try
		{
			fileWriter = new PrintWriter(new File (outputFileName));
			fileWriter.append(calculateTestTotal() + "\n");
			fileWriter.append(outputHeader);
			fileWriter.append("{HeaderEnd}\n");
			
			answerPerPageWriter = new PrintWriter(new File (answersPerPageOutputFile));
			answerPerPageWriter.append(answersPerPageText);

			for (int i = 0; i < mainQuestions.size(); i++)
			{
				//System.out.print (mainQuestions.get(i));
				fileWriter.append(mainQuestions.get(i));

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
					/*System.out.print (subQuestions.get(iter));
					System.out.println ("{QASplit}");
					System.out.println (answers.get(iter));
					System.out.println (answerRegions.get(iter));
					System.out.println ("{SubQEnd}");*/

					fileWriter.append (subQuestions.get(iter));
					fileWriter.append ("{QASplit}\n");
					fileWriter.append (answers.get(iter) + "\n");
					fileWriter.append ("{CoordsSplit}\n");
					fileWriter.append (answerRegions.get(iter) + "\n");
					fileWriter.append ("{SubQEnd}\n");
				}

				//System.out.println ("{MainQEnd}");
				fileWriter.append ("{MainQEnd}\n");
				lowerBound = upperBound;
			}
		}
		catch (Exception e)
		{
			System.out.println ("An error occured while writing to the file: ");
			e.printStackTrace();
			System.exit(0);
		}
		finally
		{
			if (fileWriter != null)
			{
				fileWriter.close();
			}
			if (answerPerPageWriter != null)
			{
				answerPerPageWriter.close();
			}
		}
	}
 
	// Calculate the total of the test
	private int calculateTestTotal()
	{
		int total = 0;

		String [] questions = outputHeader.split("\\n");

		for (String q : questions)
		{
			// Only process main question headings
			if (q.startsWith("Q") || q.startsWith("q"))
			{
				String marks = q.substring(q.indexOf("[") + 1).split(" ")[0];
				total += Integer.parseInt(marks);
			}
		}

		return total;
	}

	public static void main (String [] args)
	{
		// Ensure that the file names have been provided
		if (args.length != 3)
		{
			System.out.println ("Usage: java -jar ProcessMemo.jar memoFilename.pdf blankScript.pdf directoryToStoreMetaData");
			System.exit(0);
		}

		FinalMemoProcessor memoProc = new FinalMemoProcessor(args[0], args[1], args[2]);
	}
}
