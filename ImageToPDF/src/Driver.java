import org.apache.pdfbox.pdmodel.PDDocument;

/* Converts a set of images to a single pdf
 * Author: Victor Soudien
 * Date: 7 October 2014
 * Student Number: SDNVIC001
 */

public class Driver 
{
	private String outputFilename;
	private PDDocument outputPDF;
	
	public Driver(String outFilename)
	{
		outputFilename = outFilename;
		outputPDF = new PDDocument();
	}
	
	// Add an image to the PDF
	public void addImageToPDF (String imageFileName)
	{
		
	}
	
	public static void main (String [] args)
	{
		/*if (args.length != 1)
		{
			
		}*/
		
		Driver driver = new Driver("Test.pdf");
	}
}
