/* Processes the PDF that was included in the email
 * Author: Victor Soudien
 * Date: 11 August 2014
 * Student Number: SDNVIC001
 */

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

import org.apache.pdfbox.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

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
		String directoryToCount = "/home/vsoudien/Honours_Project/" + directoryToSaveTo;
		String numberOfFiles = uploader.getNumberOfFiles(directoryToCount);
		String uploadTime = new SimpleDateFormat("yyyyMMdd_HHmm").format(Calendar.getInstance().getTime());
		
		String fileName = numberOfFiles + "-" + testName + "-" + uploadTime + ".pdf";
		
		directoryToSaveTo += fileName;
		uploader.uploadFileToServer(directoryToSaveTo, fileToUpload);
		System.out.println ("File Uploaded PDF");
	}
	
	public static void main(String[] args) 
	{
		File imageFile = new File("201408080951.pdf");
		PDFProcessor proc = new PDFProcessor();
		proc.processDocument(imageFile);
	}
}
