/* Monitors a folder and uploads files as they are added to it
 * Author: Victor Soudien
 * Date: 28 July 2014
 * Student Number: SDNVIC001
 */

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.*;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;

public class FileUploaderDriver 
{	
	// Find the course code and test name of the script which needs to be uploaded
	public String[] getFileInfo(String filePath)
	{
		File pdfFile = new File(filePath);
		
		// Ensure that the file exists
		if (!pdfFile.exists())
		{
			System.out.println ("Could not locate the file " + filePath);
			
			return null;
		}
		
		try
		{
			// Read in the PDF File
			PDDocument pdfDoc = PDDocument.load(new File(filePath));
			
			// Remove all pages except the first one
			for (int i = pdfDoc.getNumberOfPages() - 1; i > 0; i--)
			{
				pdfDoc.removePage(i);
			}
			
			PDFTextStripper textStripper = new PDFTextStripper();
			
			// Get all text on the first page
			String text = textStripper.getText(pdfDoc);
			pdfDoc.close();
			
			// Extract the 3rd (index 2) and 4th (index 3) lines of the header
			String [] lines = text.split(textStripper.getLineSeparator());
			String courseCode = lines[2].split(" ")[2];
			String testName =  lines[3].replaceAll(" ", "_");
			
			String [] docInfo = {courseCode, testName};
			
			return docInfo;
		}
		catch (Exception e)
		{
			System.out.println ("Error while trying to access file data");
			System.out.println (e.getMessage());
			
			return null;
		}
	}
	
	// Uploads a file to the server
	public void uploadFile (String filePath)
	{	
		System.out.println ("Retrieving file information");
		
		// Retrieve file information
		String [] docInfo = getFileInfo(filePath);
			
		if (docInfo != null)
		{
			FileUploader fileUploader = new FileUploader();
			
			// Create the new file name
			String uploadDirectory = "/home/zmathews/Honours_Project/" + docInfo[0] + "/";
			String fileNamePrefix = fileUploader.getNumberOfFiles(uploadDirectory);
			String uploadTime = new SimpleDateFormat("yyyyMMdd_HHmm").format(Calendar.getInstance().getTime());
			
			String newFileName = fileNamePrefix + "-" + docInfo[1] + "-" + uploadTime + ".pdf";
			String serverPath = docInfo[0] + "/" + docInfo[1] + "/" + newFileName;
			
			// Attempt to upload file to the server
			if (fileUploader.uploadFileToServer(serverPath, filePath) == true)
			{
				System.out.println ("File uploaded to server " + serverPath);
			}
			else
			{
				System.out.println ("An error occured during file upload");
			}
		}
		else
		{
			System.out.println ("No document information returned");
		}
	}
	
	public static void main (String [] args)
	{	
		if (args.length < 1)
		{
			System.out.println ("Please provide the path of the directory to monitor");
			System.exit(0);
		}
		
		// The directory which will be monitored
		String directory = args[0];
		
		FileUploaderDriver driver = new FileUploaderDriver();
		
		// Used to monitor the directory which will receive files from the scanner
		WatchService watchService;
		WatchKey key;
		String workingDirectory;
		
		try
		{
			Path dir = Paths.get(directory);
			watchService = FileSystems.getDefault().newWatchService();
			key = dir.register(watchService, ENTRY_CREATE);
			
			// This is adjusted during the process to allow for more efficient CPU usage
			int timeInterval = 60;
			
			System.out.println ("Monitoring " + directory + "...");
			
			while (true)
			{
				WatchKey tempKey = watchService.poll(timeInterval, TimeUnit.SECONDS);
				List<WatchEvent<?>> watchEvents = tempKey.pollEvents(); // Get all the events that have occured
				
				for (WatchEvent<?> event : watchEvents)
				{
					if (event.kind().equals(ENTRY_CREATE))
					{
						String createdFilename= event.context().toString();
						driver.uploadFile(directory + createdFilename);
						Runtime.getRuntime().exec("rm " + directory + createdFilename);
					}
				}
				
				if (!tempKey.reset())
				{
					// Look into this issue
				}
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error during directory monitoring");
			System.out.println (e);
			System.exit(0);
		}
	}
}
