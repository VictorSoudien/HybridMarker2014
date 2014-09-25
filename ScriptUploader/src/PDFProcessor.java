/* Processes the PDF that was included in the email
 * Author: Victor Soudien
 * Date: 11 August 2014
 * Student Number: SDNVIC001
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;

import net.sourceforge.tess4j.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

public class PDFProcessor
{
	private FileUploader uploader;
	private enum Operation {TEST, COURSE};
	
	private ArrayList<String> testNames;
	private ArrayList<String> courseNames;
	
	// Back log of files
	private ArrayList<File> fileBacklog;
	
	// Saves the last successful OCR performed on a scanned script
	private boolean inSuccessfulUploadWindow;
	private String currentTestName;
	private String currentUploadDirectory;
	
	// Used to control the use of the successful upload window
	private TimerTask windowReset;
	private Timer windowTimer;
	private final int WINDOW_DURATION = 300;
	
	public PDFProcessor()
	{
		uploader = new FileUploader();
		
		testNames = new ArrayList<String>();
		courseNames = new ArrayList<String>();
		
		fileBacklog = new ArrayList<File>();
		
		inSuccessfulUploadWindow = false;
		currentTestName = "";
		currentUploadDirectory = "";
		
		populateListOfTestsFromServer();
		
		windowTimer = new Timer();
	}
	
	public boolean isInList (String temp, Operation op)
	{
		if (op.equals(Operation.TEST))
		{
			return (testNames.contains(temp));
		}
		else
		{
			return (courseNames.contains(temp));
		}
	}
	
	public void populateListOfTestsFromServer()
	{
		String link = "http://people.cs.uct.ac.za/~vsoudien/listOfTests.php";
		
		try
		{			        
            URL url = new URL(link);
            URLConnection urlConn = url.openConnection();
            urlConn.setDoOutput(true);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String result = "";
            String line = null;
            
            // Read Server Response
            while((line = reader.readLine()) != null)
            {
               result += line;
               break;
            }
            
            String [] resultLines = result.split("<br>");
            
            for (String rLine : resultLines)
            {
            	rLine = rLine.trim();
            	
            	if(!rLine.equals(""))
            	{
            		String [] testAndCourse = rLine.split("\\+");
            		
            		String tempTestName = testAndCourse[0].replaceAll(" ", "_");
            		String tempCourseName = testAndCourse[1].replaceAll(" ", "_");
            		
            		// Ensure that adding to the arrayList does not cause duplicates
            		if (testNames.contains(tempTestName) == false)
            		{
            			testNames.add(tempTestName);
            		}
            		
            		if (courseNames.contains(tempCourseName) == false)
            		{
            			courseNames.add(tempCourseName);
            		}
            	}
            }
		}
		catch (Exception e)
		{
			System.out.println ("An error occured while trying to retrieve the list of tests from the database: ");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	// Finds the course code and name of a given test
	public void processDocument (File fileToProcess)
	{
		try
		{
			PDDocument pdf = PDDocument.load(fileToProcess);
			List<PDPage> pages = pdf.getDocumentCatalog().getAllPages();
			
			// Convert the first page to an image
			BufferedImage imageOfFirstPage = pages.get(0).convertToImage();
			
			// Use tesseract to perform OCR on the first page
			Tesseract tesseract = Tesseract.getInstance();
			String ocrText = tesseract.doOCR(imageOfFirstPage);
			
			// Process the ocr text to get the directory to which the file needs to be uploaded
			String [] textLines = ocrText.split("\n");
			String uploadDirectory = "";
			
			for (int i = 0; i < textLines.length; i++)
			{	
				String line = textLines[i];
				// Identify the line containing the course code
				if (line.contains("CSC"))
				{
					String [] temp = line.split(" ");
					String courseName = temp[temp.length - 1];
					
					// I know the course code is the last element since this is specified in the template
					uploadDirectory += courseName + "/";
					
					// Get the name of the test
					line = textLines[i+2];
					line = line.replaceAll(" ", "_");
					uploadDirectory += line + "/";
					
					if (isInList(line, Operation.TEST) && isInList(courseName, Operation.COURSE))
					{	
						windowTimer.cancel();
						
						prepareFileForUpload(fileToProcess, uploadDirectory, line);
						
						// Upload the files in the back log
						for (File f : fileBacklog)
						{
							prepareFileForUpload(f, uploadDirectory, line);
							System.out.println ("Backlog file uploaded");
						}
						
						// Clear the backlog
						fileBacklog.clear();
						
						currentTestName = line;
						currentUploadDirectory = uploadDirectory;
						
						inSuccessfulUploadWindow = true;
						windowTimer = new Timer();
						windowTimer.schedule(new ResetUploadWindow(), WINDOW_DURATION * 1000); // time in milliseconds (seconds * 1000)
					}
					else if (inSuccessfulUploadWindow == true)
					{
						prepareFileForUpload(fileToProcess, currentUploadDirectory, currentTestName);
					}
					else
					{
						fileBacklog.add(fileToProcess);
						System.out.print (line + "------" + uploadDirectory + "------");
						System.out.println ("File stored in backlog");
					}
					
					break;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error while processing file");
			e.printStackTrace();
		}
	}
	
	// Prepares a file before it is uploaded to the server
	public void prepareFileForUpload(File fileToUpload, String directoryToSaveTo, String testName)
	{
		String username = "";
		
		try
		{
			Properties properties = new Properties();
			properties.load(new FileInputStream("ConnectionProperties.txt"));
			username = properties.getProperty("username");
		}
		catch (Exception e)
		{
			System.out.println ("Unable to load properties file");
			e.printStackTrace();
			return;
		}
		
		String directoryToCount = "/home/" + username + "/Honours_Project/" + directoryToSaveTo;
		String numberOfFiles = uploader.getNumberOfFiles(directoryToCount);
		String uploadTime = new SimpleDateFormat("yyyyMMdd_HHmm").format(Calendar.getInstance().getTime());
		
		String fileName = numberOfFiles + "-" + testName + "-" + uploadTime + ".pdf";
		
		///////////////// Upload pdf as multiple images /////////////////////////////
		String originalDir =  directoryToSaveTo;
		
		try
		{
			PDDocument pdf = PDDocument.load(fileToUpload);
			List<PDPage> pages = pdf.getDocumentCatalog().getAllPages();
			
			for (int i = 0; i < pages.size(); i++)
			{
				directoryToSaveTo = originalDir;
				
				BufferedImage tempImage = pages.get(i).convertToImage(BufferedImage.TYPE_INT_RGB, 200);
				File imageToUpload = new File("page" + (i + 1)  + ".png");
				ImageIO.write(tempImage, "png", imageToUpload);
				
				String filename = numberOfFiles + "-" + testName + "-" + uploadTime + "+/page" + (i+1) + ".png";
				directoryToSaveTo += filename;
				
				File temp = new File("page" + (i + 1) + ".png");
				temp = new File(imageToUpload.getAbsolutePath());
				
				// Check if the file exists
				if (!temp.exists())
				{
					System.out.println ("Unable to open file: " + imageToUpload);
					System.exit(0);
				}
				
				uploader.uploadFileToServer(directoryToSaveTo, temp);
				
				// Delete the file on the local file system
				temp.delete();
				
				System.out.println ("Uploaded page " + (i + 1));
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error while trying to convert pdf to images");
		}
		/////////////////////////////////////////////////////////////////////////////
		
		System.out.println ("File Uploaded PDF");
	}
	
	// Reset the upload window if the uploader is inactive for the time specified
	private class ResetUploadWindow extends TimerTask
	{
		@Override
		public void run() 
		{
			inSuccessfulUploadWindow = false;
		}
	}
	
	public static void main(String[] args) 
	{
		//File imageFile = new File("201408080948.pdf");
		//File imageFile = new File("ScannedScript.pdf");
		
		PDFProcessor proc = new PDFProcessor();
		
		//imageFiles[0] = new File ("scanned_class_test_2/201408201304.pdf");
		
		File dir = new File("scanned_class_test_2");
		File imageFiles [] = dir.listFiles();
		int count = 1;
		
		for (File f : imageFiles)
		{
			System.out.println ("Uploading file " + count);
			proc.processDocument(f);
			System.out.println ();
			count++;
		}
		
		/*File imageFile = new File("scanned_class_test_2/201408201312.pdf");
		proc.processDocument(imageFile);
		
		imageFile = new File("scanned_class_test_2/201408201316 (2).pdf");
		proc.processDocument(imageFile);*/
		
		System.out.println ("Upload complete");
	}
}
