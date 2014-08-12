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

import net.sourceforge.tess4j.*;

public class PDFProcessor 
{
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
			String [] textLines = ocrText.split("/n");
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
					line.replaceAll(" ", "_");
					uploadDirectory += line;
					break;
				}
			}
			
			System.out.println (uploadDirectory);
		}
		catch (Exception e)
		{
			System.out.println ("Error while processing file");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
	{
		File imageFile = new File("201408080948.pdf");
		PDFProcessor proc = new PDFProcessor();
		proc.processDocument(imageFile);
	}
}
