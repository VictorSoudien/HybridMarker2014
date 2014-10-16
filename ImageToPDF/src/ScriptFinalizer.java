/* Created a PDF of the marked tests and emails it to the students
 * Author: Victor Soudien
 * Date: 16 October 2014
 * Student Number: SDNVIC001
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ScriptFinalizer 
{
	private String courseName;
	private String testName;
	private String taEmail;
	private ArrayList<String> studentData;
	
	public ScriptFinalizer (String cName, String tName, String csvFileName, String TAEmail)
	{
		courseName = cName;
		testName = tName;
		taEmail = TAEmail;
		
		processCSV(csvFileName);
	}
	
	// Processes the csv file in order to get the student data
	private void processCSV(String filename)
	{
		BufferedReader buffReader = null;
		
		try
		{
			buffReader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			
			while (line != null)
			{
				line = buffReader.readLine();
				studentData.add(line);
				
				System.out.println (line);
			}
			
			buffReader.close();
		}
		catch (Exception e)
		{
			System.out.println ("Unable to read csv file");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main (String [] args)
	{
		if (args.length != 4)
		{
			System.out.println ("Invalid number of args");
			System.out.println ("Usage: java -jar ScriptFinalizer.jar courseName testName students.csv taEmail");
			System.exit(0);
		}
		
		ScriptFinalizer scriptFinal = new ScriptFinalizer(args[0], args[1], args[2], args[3]);
	}
}
