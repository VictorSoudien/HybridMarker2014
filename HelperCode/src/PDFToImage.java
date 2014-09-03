/* Used to convert all pages (except the first) of a PDF to an image
 * Author: Victor Soudien
 * Date: 3 September 2014
 * Student Number: SDNVIC001
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

public class PDFToImage 
{
	File loadedFile;
	
	public PDFToImage(String filename)
	{
		loadImage(filename);
	}
	
	// Loads the file into memory
	private void loadImage(String filename)
	{
		loadedFile = new File(filename);
		
		// Check if the file exists
		if (!loadedFile.exists())
		{
			System.out.println ("Unable to open file: " + filename);
			System.exit(0);
		}
		
		System.out.println ("File opened successfully");
	}
	
	public void convertToImages()
	{
		try
		{
			PDDocument pdfDoc = PDDocument.load(loadedFile);
			List<PDPage> pages = pdfDoc.getDocumentCatalog().getAllPages();
			
			for (int i = 1; i < pages.size(); i++)
			{
				BufferedImage tempImage = pages.get(i).convertToImage();
				File outputFile = new File (i + ".png");
				ImageIO.write(tempImage, "png", outputFile);
				System.out.println ("Saved Page " + i);
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error while trying to load pdf");
		}
	}
	
	public static void main (String [] args)
	{
		PDFToImage toImage = new PDFToImage("Scanned Script.pdf");
		toImage.convertToImages();
	}
}
