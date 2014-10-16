/* Created a PDF of the marked tests and emails it to the students
 * Author: Victor Soudien
 * Date: 16 October 2014
 * Student Number: SDNVIC001
 */

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

public class ScriptFinalizer 
{
	private final float PAGE_WIDTH = 612;
	private final float PAGE_HEIGHT = 792;
	
	private String courseName;
	private String testName;
	private String taEmail;
	private ArrayList<String> studentData;
	private PDDocument outputPDF;

	public ScriptFinalizer (String cName, String tName, String TAEmail)
	{
		courseName = cName;
		testName = tName;
		taEmail = TAEmail;
		
		outputPDF = new PDDocument();

		processCSV("/home/zmathews/Honours_Project/" + courseName + "/" + testName + "/FinalScripts/emailStudents.csv");
		
	}

	// Processes the csv file in order to get the student data
	private void processCSV(String filename)
	{
		BufferedReader buffReader = null;
		studentData = new ArrayList<String>();

		try
		{
			buffReader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";

			while (line != null)
			{
				line = buffReader.readLine();

				if (line != null)
				{
					studentData.add(line);
				}
			}

			buffReader.close();
			createPDFS();
		}
		catch (Exception e)
		{
			System.out.println ("Unable to read csv file");
			e.printStackTrace();
			System.exit(0);
		}
	}

	// Iterate over the list of students and send emails to them
	private void createPDFS()
	{
		try
		{
			for (String student : studentData)
			{
				String [] data = student.split(",");
				String studentNum = data[0];
				String result = data[1];
				String email = data[2];

				outputPDF = new PDDocument();
				createSinglePDF(studentNum, email, result);
			}
		}
		catch (Exception e)
		{
			System.out.println ("Error while processing student data");
			e.printStackTrace();
			System.exit(0);
		}
	}

	// Iterates through all images which need to be added
	public void createSinglePDF(String studentNum, String email, String result)
	{
		System.out.println ("Writing images to PDF...");

		String fileDir = "/home/zmathews/Honours_Project/" + courseName + "/" + testName + "/" + studentNum.toUpperCase() + "+/";

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

		String prefix = null;

		while (allProcessed == false)
		{
			if (prefix != null)
			{
				tempFile = new File (fileDir + prefix + counter + ".png");

				if (tempFile.exists())
				{
					addImageToPDF(tempFile);
					counter++;
				}
				else
				{
					allProcessed = true;
					finalizePDF(studentNum, email, result);
				}
			}
			else
			{
				tempFile = new File (fileDir + "ReMarkedPage" + counter + ".png");

				if (tempFile.exists())
				{
					prefix = "ReMarkedPage";
					
					addImageToPDF(tempFile);
					counter++;
				}
				else
				{
					tempFile = new File (fileDir + "save" + counter + ".png");

					if (tempFile.exists())
					{
						prefix = "save";
						
						addImageToPDF(tempFile);
						counter++;
					}
					else
					{
						tempFile = new File (fileDir + "MarkedPage" + counter + ".png");

						if (tempFile.exists())
						{
							prefix = "MarkedPage";
							
							addImageToPDF(tempFile);
							counter++;
						}
						else
						{
							System.out.println (studentNum + "'s test has not been marked");
							return;
						}
					}
				}	
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
	private void finalizePDF(String studentNum, String email, String result)
	{
		System.out.println ("Finalizing PDF...");
		
		try
		{
			outputPDF.save("/home/zmathews/Honours_Project/" + courseName + "/" + testName + "/FinalScripts/" + studentNum + ".pdf");
			outputPDF.close();
			
			SendMail emailSender =  new SendMail(email, "/home/zmathews/Honours_Project/" + courseName + "/" + testName + "/FinalScripts/" + studentNum + ".pdf", result);
			emailSender.sendMail();
			System.out.println ("Email Sent");
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
			for (String s : args)
			{
				System.out.println (s);
			}
			
			System.out.println ("Invalid number of args");
			System.out.println ("Usage: java -jar ScriptFinalizer.jar courseName testName taEmail");
			System.exit(0);
		}

		ScriptFinalizer scriptFinal = new ScriptFinalizer(args[0], args[1], args[2]);
	}
}
