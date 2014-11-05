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

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
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
	private int num_pages;

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
		String link = "http://people.cs.uct.ac.za/~vsoudien/Test/listOfTests.php";

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
	
	// Gets the number of pages for this test
	private void getNumPages(String testName, String courseName)
	{
		String link = "http://people.cs.uct.ac.za/~vsoudien/Test/numTestPages.php?";

		try
		{			        
			link += "op=Select&testName=" + testName.replaceAll(" ", "%20") + "&courseName=" + courseName.replaceAll(" ", "%20");
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
			
			num_pages = Integer.parseInt(result.trim());
		}
		catch (Exception e)
		{
			System.out.println ("An error has occured while retrieving the number of pages for this script");
			e.printStackTrace();
			num_pages = -1;
		}
	}

	// Determines whether or not this pdf contains multiple tests
	private boolean containsMultipleTests(PDDocument doc)
	{
		List<PDPage> pages = doc.getDocumentCatalog().getAllPages();

		return (pages.size() > num_pages);
	}

	private void processMultipleTests(PDDocument doc)
	{
		List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
		int numPagesInDoc = pages.size();

		// If pages is a multiple of the number of pages in the test
		if ((numPagesInDoc % num_pages) == 0)
		{
			int numTests = numPagesInDoc / num_pages;

			for (int i = 0; i < numTests; i++)
			{
				PDDocument tempDoc = new PDDocument();
				
				int basePage = (i * num_pages);
				
				for (int p = 0; p < num_pages; p++)
				{
					tempDoc.addPage(pages.get(basePage + p));
				}
				
				try 
				{
					tempDoc.save("tempDoc.pdf");
					tempDoc.close();
					
					File tempPDF = new File("tempDoc.pdf");
					processDocument(tempPDF, null);
					tempPDF.delete();
				} 
				catch (Exception e) 
				{
					System.out.println ("Error while trying to process a PDF containing multiple tests");
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		else
		{
			System.out.println ("Missing pages in scanned document");
			System.exit(0);
		}
	}

	// Finds the course code and name of a given test
	public void processDocument (File fileToProcess, PDDocument docToProcess)
	{
		try
		{
			PDDocument pdf = null;
			
			if (docToProcess == null)
			{
				pdf = PDDocument.load(fileToProcess);
			}
			else
			{
				pdf = docToProcess;
			}

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
						
						getNumPages(line.replaceAll("_", " "), courseName);
						if (num_pages != -1)
						{
							if (containsMultipleTests(pdf) == true)
							{
								processMultipleTests(pdf);
								return;
							}
						}

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
						if (num_pages != -1)
						{
							if (containsMultipleTests(pdf) == true)
							{
								processMultipleTests(pdf);
								return;
							}
						}
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
			System.out.println ("Unable to load properties file" + "\n" + e.getMessage());
			return;
		}

		String directoryToCount = "/home/" + username + "/Honours_Project/" + directoryToSaveTo;
		String numberOfFiles = uploader.getNumberOfFiles(directoryToCount);
		String uploadTime = new SimpleDateFormat("yyyyMMdd_HHmm").format(Calendar.getInstance().getTime());

		String fileName = numberOfFiles + "-" + testName + "-" + uploadTime + ".pdf";

		///////////////// Upload PDF as multiple images /////////////////////////////
		String originalDir =  directoryToSaveTo;

		try
		{
			PDDocument pdf = PDDocument.load(fileToUpload);
			List<PDPage> pages = pdf.getDocumentCatalog().getAllPages();

			for (int i = 0; i < pages.size(); i++)
			{
				directoryToSaveTo = originalDir;

				BufferedImage tempImage = pages.get(i).convertToImage(BufferedImage.TYPE_3BYTE_BGR, 200);

				dilateImage(tempImage, "page" + (i + 1) + ".png");

				File imageToUpload = new File("OriginalPage" + (i + 1)  + ".png");
				ImageIO.write(tempImage, "png", imageToUpload);

				File dilatedUpload = new File ("page" + (i + 1) + ".png");

				String filename = numberOfFiles + "-" + testName + "-" + uploadTime + "+/OriginalPage" + (i+1) + ".png";
				String tempDir = directoryToSaveTo + filename;
				//directoryToSaveTo += filename;

				// Upload the original file
				File temp = new File("OriginalPage" + (i + 1) + ".png");
				temp = new File(imageToUpload.getAbsolutePath());

				// Check if the file exists
				if (!temp.exists())
				{
					System.out.println ("Unable to open file: " + imageToUpload);
					System.exit(0);
				}

				uploader.uploadFileToServer(tempDir, temp);
				temp.delete();

				filename = numberOfFiles + "-" + testName + "-" + uploadTime + "+/page" + (i+1) + ".png";
				tempDir = directoryToSaveTo + filename;

				temp = new File("page" + (i + 1) + ".png");
				temp = new File(dilatedUpload.getAbsolutePath());

				// Check if the file exists
				if (!temp.exists())
				{
					System.out.println ("Unable to open file: " + dilatedUpload);
					System.exit(0);
				}

				uploader.uploadFileToServer(tempDir, temp);

				// Delete the file on the local file system
				temp.delete();

				System.out.println ("Uploaded page " + (i + 1));
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error while trying to convert pdf to images");
			e.printStackTrace();
		}
		/////////////////////////////////////////////////////////////////////////////

		System.out.println ("File Uploaded PDF");
	}

	// Dilate the image in an effort to improve readability
	private void dilateImage(BufferedImage imageToProcess, String filename)
	{
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

		int dilationSize = 1;

		byte[] imageData = ((DataBufferByte) imageToProcess.getRaster().getDataBuffer()).getData();
		Mat inputImage = new Mat(imageToProcess.getHeight(), imageToProcess.getWidth(), CvType.CV_8UC3);
		inputImage.put(0, 0, imageData);

		Mat outMat = inputImage;

		Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,		
				new Size(2*dilationSize + 1, 2*dilationSize+1));
		Imgproc.erode(inputImage, outMat, element1);

		Highgui.imwrite(filename, outMat);
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

//	public static void main(String[] args) 
//	{
//		//File imageFile = new File(args[0]);
//		/*File imageFile = new File("scanned_class_test_2/merged_2.pdf");*/
//
//		PDFProcessor proc = new PDFProcessor();
//		//proc.processDocument(imageFile, null);*/
//		
//		File [] allFiles = new File("Class_Test_2/").listFiles();
//		
//		for (File f : allFiles)
//		{
//			System.out.println (f.getName());
//			proc.processDocument(f, null);
//		}
//	}
}
