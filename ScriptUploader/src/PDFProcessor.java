/* Processes the PDF that was included in the email
 * Author: Victor Soudien
 * Date: 11 August 2014
 * Student Number: SDNVIC001
 */

import java.io.*;

import org.apache.pdfbox.*;
import org.apache.pdfbox.pdmodel.PDDocument;

import net.sourceforge.tess4j.*;

public class PDFProcessor 
{
	public void processDocument ()
	{
		
	}
	
	public static void main(String[] args) 
	{
		File imageFile = new File("FileName Here");
		Tesseract instance = Tesseract.getInstance();

		try
		{
			String result = instance.doOCR(imageFile);
			System.out.println(result);
		} 
		catch (TesseractException e) 
		{
			System.err.println(e.getMessage());
		}
	}
}
