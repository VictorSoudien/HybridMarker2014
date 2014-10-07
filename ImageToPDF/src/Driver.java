/* Converts a set of images to a single pdf
 * Author: Victor Soudien
 * Date: 7 October 2014
 * Student Number: SDNVIC001
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Driver 
{
	private final float PAGE_WIDTH = 612;
	private final float PAGE_HEIGHT = 792;
	
	private String outputFilename;
	private PDDocument outputPDF;
	
	private String fileDir;
	private String imagePrefix;
	
	public Driver(String outFilename, String dir, String prefix)
	{
		outputFilename = outFilename;
		outputPDF = new PDDocument();
		
		fileDir = dir.endsWith("/") ? dir : (dir + "/");
		imagePrefix = prefix;
	}
	
	// Iterates through all images which need to be added
	public void createPDF()
	{
		System.out.println ("Writing images to PDF...");
		
		// Add the first page
		File tempFile= new File (fileDir + "page1.png");
		if (tempFile.exists())
		{
			addImageToPDF(tempFile);
		}
		else
		{
			System.out.println ("Invalid drirectory provided");
			return;
		}
		
		boolean allProcessed = false;
		int counter = 2;
		
		while (allProcessed == false)
		{
			tempFile = new File (fileDir + imagePrefix + counter + ".png");
			
			if (tempFile.exists())
			{
				addImageToPDF(tempFile);
				counter++;
			}
			else
			{
				allProcessed = true;
				finalizePDF();
			}	
		}
		
		System.out.println ("Done");
	}
	
	// Add an image to the PDF
	private void addImageToPDF (File imageToAdd)
	{
		try
		{
			PDPage cleanPage = new PDPage();
			outputPDF.addPage(cleanPage);
		
			BufferedImage inputImage = ImageIO.read(imageToAdd);
			PDXObjectImage ximage = new PDJpeg(outputPDF, inputImage);
			
			float imageWidth = inputImage.getWidth();
			float imageHeight = inputImage.getHeight();
			
			PDPageContentStream contentStream = new PDPageContentStream(outputPDF, cleanPage, true, false);
            contentStream.drawXObject(ximage, 0, 0, imageWidth*(PAGE_WIDTH / imageWidth), imageHeight *(PAGE_HEIGHT / imageHeight));
            contentStream.close();
		}
		catch (Exception e)
		{
			System.out.println ("An error occured while adding an image to the PDF");
			e.printStackTrace();
		}
	}
	
	// Write the doc to storage
	private void finalizePDF()
	{
		System.out.println ("Finalizing PDF...");
		try
		{
			outputPDF.save(outputFilename);
			outputPDF.close();
		}
		catch (Exception e)
		{
			System.out.println ("Error while writing PDF to disk");
			e.printStackTrace();
		}
	}
	
	public static void main (String [] args)
	{
		if (args.length != 3)
		{
			
		}
		
		Driver driver = new Driver("Remarked.pdf", "MPHNOK005+/", "ReMarkedPage");
		driver.createPDF();
	}
}
