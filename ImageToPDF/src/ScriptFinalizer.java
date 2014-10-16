import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.stream.FileImageInputStream;

/* Created a PDF of the marked tests and emails it to the students
 * Author: Victor Soudien
 * Date: 16 October 2014
 * Student Number: SDNVIC001
 */

public class ScriptFinalizer 
{
	private String courseName;
	private String testName;
	private String taEmail;
	private String [] studentData;
	
	public ScriptFinalizer (String cName, String tName, String csvFileName, String TAEmail)
	{
		courseName = cName;
		testName = tName;
		taEmail = TAEmail;
	}
	
	// Processes the csv file in order to get the student data
	private void processCSV (String filename)
	{
		//BufferedReader buffReader = new BufferedReader(new FileInputStream(new File(filename)));
		
		/*if (csv.exists() == false)
		{
			System.out.println ("Unable to open file: " + filename);
			System.exit(0);
		}*/
	}
	
	public static void main (String [] args)
	{
		if (args.length != 4)
		{
			System.out.println ("Invalid number of args");
			System.out.println ("Usage: java -jar ScriptFinalizer.jar courseName testName students.csv taEmail");
			System.exit(0);
		}
	}
}
