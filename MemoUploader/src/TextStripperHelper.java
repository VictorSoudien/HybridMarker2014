/* Extends the pdfTextStripper in order to obtain additional information
 * Author: Victor Soudien
 * Date: 13 September 2014
 * Student Number: SDNVIC001
 */

import java.io.IOException;

import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

public class TextStripperHelper extends PDFTextStripper
{
	float previous = -1;
	private String result;
	
	private final double PIXEL_MAPPING_VALUE = 2.770641;
	
	public String getResult () {return result;}
	
	public TextStripperHelper() throws IOException 
	{
		result = "";
		super.setSortByPosition(true);
	}
	
	// Print the y coordinate of the text
	@Override
	protected void processTextPosition(TextPosition textPosition)
	{
		float current = textPosition.getY();
		
		if (current != previous)
		{
			result += (current * PIXEL_MAPPING_VALUE) + "\n";
			//System.out.println ("y: " + current);
			previous = current;
		}
	}
}
