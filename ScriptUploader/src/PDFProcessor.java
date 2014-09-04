/* Processes the PDF that was included in the email
 * Author: Victor Soudien
 * Date: 11 August 2014
 * Student Number: SDNVIC001
 */

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDPageAdditionalActions;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;

import net.sourceforge.tess4j.*;

public class PDFProcessor
{
	private FileUploader uploader;
	
	public PDFProcessor()
	{
		uploader = new FileUploader();
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
					// I know the course code is the last element since this is specified in the template
					uploadDirectory += temp[temp.length - 1] + "/";
					
					// Get the name of the test
					line = textLines[i+2];
					line = line.replaceAll(" ", "_");
					uploadDirectory += line + "/";
					
					prepareFileForUpload(fileToProcess, uploadDirectory, line);
					
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
		//String filename = numberOfFiles + "-" + testName + "-" + uploadTime + "/";
		//directoryToSaveTo += filename;
		String originalDir =  directoryToSaveTo;
		
		try
		{
			PDDocument pdf = PDDocument.load(fileToUpload);
			List<PDPage> pages = pdf.getDocumentCatalog().getAllPages();
			
			for (int i = 0; i < pages.size(); i++)
			{
				directoryToSaveTo = originalDir;
				
				BufferedImage tempImage = pages.get(i).convertToImage(BufferedImage.TYPE_INT_RGB, 300);
				File imageToUpload = new File("page" + (i + 1)  + ".png");
				ImageIO.write(tempImage, "png", imageToUpload);
				
				String filename = numberOfFiles + "-" + testName + "-" + uploadTime + "/page" + (i+1) + ".png";
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
				
				System.out.println ("Uploaded page " + (i + 1));
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error while trying to convert pdf to images");
		}
		/////////////////////////////////////////////////////////////////////////////
		
		//directoryToSaveTo += fileName;
		//uploader.uploadFileToServer(directoryToSaveTo, fileToUpload);
		System.out.println ("File Uploaded PDF");
	}
	
	public static void main(String[] args) 
	{
		//File imageFile = new File("201408080948.pdf");
		File imageFile = new File("ScannedScript.pdf");
		PDFProcessor proc = new PDFProcessor();
		proc.processDocument(imageFile);
		System.out.println("Done, I need to stop, please help!!!");
	}
}
