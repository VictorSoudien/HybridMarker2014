/* Processes a given memo in order to determine the answer regions and memo text
 * Author: Victor Soudien
 * Date: 24 September 2014
 * Student Number: SDNVIC001
 */

import java.io.File;
import java.util.ArrayList;

public class FinalMemoProcessor 
{
	private File memoFile;
	private File blankScriptFile;
	
	// The name of the output file
	private String ouptutFileName;
	
	// Stores the answer regions in terms of their start and end y position
	private ArrayList<String> answerRegions;
	
	public FinalMemoProcessor (String memoFileName, String blankScriptFileName)
	{
		ouptutFileName = memoFileName.split("\\.")[0].replaceAll(" ", "_");
		ouptutFileName += ".txt";
		
		openFiles(memoFileName, blankScriptFileName);
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
		
	}
	
	public static void main (String [] args)
	{
		// Ensure that the file names have been provided
		if (args.length != 2)
		{
			System.out.println ("Please provide the name of the memo and test script files");
			System.exit(0);
		}
	}
}
