/* Process a memo and all text contained in it
 * Author: Victor Soudien
 * Date: 27 August 2014
 * Student Number: SDNVIC001
 */

import java.io.File;

public class MemoProcessor 
{
	private File memoToProcess;
	
	public MemoProcessor(String filename)
	{
		openFile(filename);
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
	
	public static void main (String [] args)
	{
		MemoProcessor mProc = new MemoProcessor("ClassTest4.pdf");
	}
}
