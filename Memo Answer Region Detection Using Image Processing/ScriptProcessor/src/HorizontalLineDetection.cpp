/*
 * HorizontalLineDetection.cpp
 *
 *  Created on: 20 Sep 2014
 *      Author: victor
 */

#include <opencv2/opencv.hpp>
#include <math.h>
#include <vector>
#include <iostream>
#include <fstream>

#include "../headers/HorizontalLineDetection.h"

using namespace cv;

void HorizontalLineDetection::processImage(std::string imageName)
{
	std::ofstream outputFile;
	outputFile.open("answerRegions.txt");

	Mat image;
	image = imread(imageName, 1);

	// Ensure that the file has been loaded
	if (image.data == false)
	{
		std::cout << "Image not found" << std::endl;
		std::exit(1);
	}

	Mat grayImage;
	cvtColor(image, grayImage, COLOR_BGR2GRAY);

	Mat cannyEdges;
	Canny(grayImage, cannyEdges, 80, 120);

	vector<Vec4i> lines;
	HoughLinesP(cannyEdges, lines, 1, M_PI / 2.0, 2, 30, 1);

	vector<int> yPositions;
	int xStart = 0;
	int xEnd = 0;

	if (lines.size() != 0)
	{
		for (size_t i = 0; i < lines.size(); i++)
		{
			Point ptStart, ptEnd;
			ptStart.x = lines[i][0];
			ptStart.y = lines[i][1];
			ptEnd.x = lines[i][2];
			ptEnd.y = lines[i][3];


			// Only detect horizontal lines
			if (ptStart.y == ptEnd.y)
			{
				//line(image, ptStart, ptEnd, (0,0,255), 3);
				yPositions.push_back(ptEnd.y);

				xStart = max(ptStart.x, xStart); // Use max to ensure that largest x values are stored
				xEnd = max(ptEnd.x, xEnd);
			}
		}

		std::sort(yPositions.begin(), yPositions.end());

		Point regionStart;
		Point regionEnd;

		regionStart.x = xStart;
		regionEnd.x = xEnd;

		regionStart.y = yPositions[0] - 70;
		int prevY = yPositions[0];

		for (size_t iter = 1; iter < yPositions.size(); iter++)
		{
			if (yPositions[iter] - prevY > 77)
			{
				regionEnd.y = prevY + 20;

				outputFile << regionStart.y << ";" << regionEnd.y << std::endl;
				//rectangle(image, regionStart, regionEnd, 255, 3);

				regionStart.y = yPositions[iter] - 70;
			}
			if (iter == yPositions.size() - 1)
			{
				regionEnd.y = yPositions[iter] + 20;

				outputFile << regionStart.y << ";" << regionEnd.y << std::endl;
				//rectangle(image, regionStart, regionEnd, 255, 3);
			}

			prevY = yPositions[iter];
		}

		outputFile.close();
		//imwrite("imageOut.png", image);
	}
}


