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
	
	public TextStripperHelper() throws IOException 
	{
		super.setSortByPosition(true);
	}
	
	// Print the y coordinate of the text
	@Override
	protected void processTextPosition(TextPosition textPosition)
	{
		float current = textPosition.getYDirAdj();
		
		if (current != previous)
		{
			System.out.println ("y: " + current);
			previous = current;
		}
	}
}
