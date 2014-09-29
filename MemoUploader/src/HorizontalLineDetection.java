/* Used to detect the horizontal lines of an answer region
 * Author: Victor Soudien
 * Date: 22 September 2014
 * Student Number: SDNVIC001
 */

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.*;

import java.util.ArrayList;
import java.util.Collections;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.imageio.ImageIO;

public class HorizontalLineDetection 
{
	private ArrayList<String> regions;
	
	public HorizontalLineDetection()
	{
		regions = new ArrayList<String>();
	}
	
	public ArrayList<String> processImage(String filename)
	{
		BufferedImage imageToProcess = null;
		regions.clear();
		
		try
		{
			File input = new File(filename);
			imageToProcess = ImageIO.read(input);
		}
		catch (Exception e)
		{
			System.out.println ("Unable to open file: " + filename);
		}
		
		byte[] imageData = ((DataBufferByte) imageToProcess.getRaster().getDataBuffer()).getData();
		Mat inputImage = new Mat(imageToProcess.getHeight(), imageToProcess.getWidth(), CvType.CV_8UC3);
		inputImage.put(0, 0, imageData);
		
		Mat procImage = inputImage;
		
		// Check if the image has been loaded
		if (inputImage.get(0, 0) == null)
		{
			System.out.println ("ERROR");
			System.exit(0);
		}
		
		// Convert the image to grayscale
		Imgproc.cvtColor(inputImage, procImage, Imgproc.COLOR_RGB2GRAY);
		
		// Perform Canny edge detection to highlight edges
		Imgproc.Canny(procImage, procImage, 80, 120);
		
		Imgproc.HoughLinesP(procImage, procImage, 1, Math.PI / 2.0, 2, 30, 1);
		
		ArrayList <Integer> yPositions = new ArrayList<Integer>();
		int xStart = -1;
		int xEnd = -1;
		
		for (int x = 0; x < procImage.cols(); x++) 
	    {
			double[] vec = procImage.get(0, x);
	          
	        Point ptStart = new Point(vec[0], vec[1]);
	        Point ptEnd = new Point (vec[2], vec[3]);
		          
		       // Only detect horizontal lines
			if (ptStart.y == ptEnd.y)
			{
				yPositions.add( (int) (Math.ceil(ptEnd.y)));

				xStart = (int) Math.ceil(Math.max(ptStart.x, xStart)); // Use max to ensure that largest x values are stored
				xEnd = (int) Math.ceil(Math.max(ptEnd.x, xEnd));
			}
	    }
		
		// Sort the ArrayList in increasing order
		Collections.sort(yPositions);
		
		Point regionStart = new Point();
		Point regionEnd = new Point();

		regionStart.x = xStart;
		regionEnd.x = xEnd;

		regionStart.y = yPositions.get(0) - 70;
		int prevY = yPositions.get(0);

		for (int iter = 1; iter < yPositions.size(); iter++)
		{
			if (yPositions.get(iter) - prevY > 77)
			{
				regionEnd.y = prevY + 20;

				regions.add((int) regionStart.y + ";" + (int) regionEnd.y);

				regionStart.y = yPositions.get(iter) - 70;
			}
			if (iter == yPositions.size() - 1)
			{
				regionEnd.y = yPositions.get(iter) + 20;

				regions.add((int) regionStart.y + ";" + (int) regionEnd.y);
			}

			prevY = yPositions.get(iter);
		}
		
		///////////////////////////// WRITE TO A FILE
		/*BufferedImage imageToProcess = null;
		
		try
		{
			File input = new File(filename);
			imageToProcess = ImageIO.read(input);
		}
		catch (Exception e)
		{
			System.out.println ("Unable to open file: " + filename);
		}*/
	try
	{
		byte[] data1 = new byte[inputImage.rows()*inputImage.cols()*(int)(inputImage.elemSize())];
		inputImage.get(0, 0, data1);
		BufferedImage image1=new BufferedImage(inputImage.cols(),inputImage.rows()
	      ,imageToProcess.getType());
	      image1.getRaster().setDataElements(0,0,inputImage.cols(),inputImage.rows(),data1);
	
	      File ouptut = new File("grayscale.jpg");
	      ImageIO.write(image1, "jpg", ouptut);
	}catch (Exception e) {
	       System.out.println("Error: " + e.getMessage());
	     }

		return regions;
	}
}
