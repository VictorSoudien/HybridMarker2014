///*
// * Main.cpp
// *
// *  Created on: 03 Jul 2014
// *      Author: Victor Soudien
// */
//
//#include <iostream>
//#include <stdio.h>
//#include <stdlib.h>
//#include <sstream>
//#include <fstream>
//
//#include "../headers/BlobDetector.h"
//
//// Adapted from the solution at
//// http://stackoverflow.com/questions/478898/how-to-execute-a-command-and-get-output-of-command-within-c
//// on 3 July 2014
//std::string getNumberOfPages(std::string outputDirectory)
//{
//	std::string commandToExecute =  "ls " + outputDirectory + " | wc -l";
//
//	FILE * pipe = popen(commandToExecute.c_str(), "r");
//
//	if (!pipe)
//	{
//		std::cout << "Error" << std::endl;
//		return "-1";
//	}
//
//	char buffer[128];
//
//	std::string result = "";
//
//	// While not the end of the file
//	while (!feof(pipe))
//	{
//		if(fgets(buffer, 128, pipe) != NULL)
//		{
//			result += buffer;
//		}
//	}
//
//	pclose(pipe);
//
//	return result;
//}
//
//int main (int argc, char** argv)
//{
//	// Ensure that a filename has been provided
//	if (argc != 4)
//	{
//		std::cout << "Usage: ./ScriptProcessor InputFilePath TempFileOutputPath metadataOutputPath" << std::endl;
//		return -1;
//	}
//
//	int numberOfPages;
//	std::string filePath = argv[1];
//	std::string outputDirectory = argv[2];
//	std::string commandToExecute = "pdftoppm -png " + filePath + " " +  outputDirectory + "/testPages";
//
//	if (system(commandToExecute.c_str()) == 0)
//	{
//		std::cout << "PDF Processed" << std::endl;
//
//		numberOfPages = atoi(getNumberOfPages(outputDirectory).c_str());
//		std::cout << "Number of pages created: " << numberOfPages << std::endl;
//	}
//	else
//	{
//		std::cout << "Error while trying to create files" << std::endl;
//	}
//
//	BlobDetector * blobDetect = new BlobDetector();
//
//	std::stringstream finalOutput;
//	std::string filePathPrefix = "";
//
//	for (int i = 2; i <= numberOfPages; i++)
//	{
//		std::stringstream pageNumStream; // used to convert int to string
//		pageNumStream << i;
//		finalOutput << "Page " << i << std::endl;
//
//		if (numberOfPages < 10 )
//		{
//			filePathPrefix = outputDirectory + "/testPages-";
//		}
//		else
//		{
//			if (i < 10)
//			{
//				filePathPrefix = outputDirectory + "/testPages-0";
//			}
//			else
//			{
//				filePathPrefix = outputDirectory + "/testPages-";
//			}
//		}
//
//		std::string filePath = filePathPrefix + pageNumStream.str() + ".png";
//
//		std::vector<int> questionLocations = blobDetect->getQuestionStartLocation(filePath);
//
//		// Check if an error occured during page processing
//		/*if (questionLocations.size() == 0)
//		{
//			std::cout << "Error during page processing" << std::endl;
//			return -1;
//		}
//		else
//		{*/
//			// Results are stored in descending order so it needs to be read in reverse to get ascending order
//			for (int i = questionLocations.size() - 1; i >= 0; --i)
//			{
//				finalOutput << questionLocations[i] << std::endl;
//			}
//		//}
//
//		std::cout << "Processed page " << pageNumStream.str() << std::endl;
//	}
//
//	// Save the data to a file
//	std::string metadataOutputFilename = argv[3];
//	std::ofstream fileOut;
//	fileOut.open(metadataOutputFilename.c_str());
//	fileOut << finalOutput.str();
//	fileOut.close();
//
//	std::cout << "Processing complete" << std::endl;
//
//	return 0;
//}
//
//
