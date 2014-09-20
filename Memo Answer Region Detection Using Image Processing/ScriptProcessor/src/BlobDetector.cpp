/*
 * BlobDetector.cpp
 *
 *  Created on: 30 Jun 2014
 *      Author: victor
 */

#include <opencv2/opencv.hpp>
#include "../headers/BlobDetector.h"

using namespace cv;

std::vector<int> BlobDetector::getQuestionStartLocation(std::string filename)
{
	std::vector<int> questionLocations;

	Mat image;
	image = imread (filename, 1);

	// Ensure that the file has been loaded
	if (image.data == false)
	{
		std::cout << "Image not found" << std::endl;
		std::exit(1);
	}
	else
	{
		//std::cout << filename << " loaded" << std::endl;
	}

	Mat imageHSV;
	Mat imageThresh;

	cvtColor(image, imageHSV,COLOR_BGR2HSV);

	inRange(imageHSV, Scalar(0, 0, 60), Scalar(100, 110, 160), imageThresh);

	// The following 4 lines of code are from sample code

	//morphological opening (remove small objects from the foreground)
	erode(imageThresh, imageThresh, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)) );
	dilate( imageThresh, imageThresh, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)) );

	//morphological closing (fill small holes in the foreground)
	dilate( imageThresh, imageThresh, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)) );
	erode(imageThresh, imageThresh, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)) );

	std::vector<std::vector<Point> > contours;

	findContours(imageThresh, contours, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE);

	// Save the question location data to a vector
	int yMin = 1000000;
	bool minChanged;

	for (u_int cIndex = 0; cIndex < contours.size(); cIndex++)
	{
		minChanged = false;

		std::vector<Point> pointVector = contours[cIndex];

		for (u_int i = 0; i < pointVector.size(); i++)
		{
			// Both values can be tweaked
			// Check the x value to ensure that it is a question and not another element on the page e.g. a diagram
			if ((pointVector[i].y < (yMin - 15)) && (pointVector[i].x < 200))
			{
				yMin = pointVector[i].y;
				minChanged = true;
			}
		}

		if (minChanged == true)
		{
			questionLocations.push_back(yMin);
			//std::cout << "Start of question " << yMin << std::endl;
		}
	}

	return questionLocations;
}
/*
int main (int argc, char** argv)
{
	// check if image name has been provided
	if (argc != 2)
	{
		std::cout << "Please provide the name of the image";
		return -1;
	}

	Mat image;
	image = imread (argv[1], 1);

	// Ensure that the file has been loaded
	if (image.data == false)
	{
		std::cout << "Image not found";
		return -1;
	}
	else
	{
		std::cout << argv[1] << " loaded" << std::endl;
	}

	Mat imageHSV;
	Mat imageThresh;

	cvtColor(image, imageHSV,COLOR_BGR2HSV);

	inRange(imageHSV, Scalar(0, 0, 60), Scalar(100, 110, 160), imageThresh);

	//morphological opening (remove small objects from the foreground)
	erode(imageThresh, imageThresh, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)) );
	dilate( imageThresh, imageThresh, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)) );

	//morphological closing (fill small holes in the foreground)
	dilate( imageThresh, imageThresh, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)) );
	erode(imageThresh, imageThresh, getStructuringElement(MORPH_ELLIPSE, Size(5, 5)) );

	std::vector<std::vector<Point> > contours;

	findContours(imageThresh, contours, CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE);

	///////// Working with contour data /////////

	int yMin = 1000000;
	bool minChanged;

	for (u_int cIndex = 0; cIndex < contours.size(); cIndex++)
	{
		minChanged = false;

		std::vector<Point> pointVector = contours[cIndex];

		for (u_int i = 0; i < pointVector.size(); i++)
		{
			if (pointVector[i].y < (yMin - 15)) // This value can be tweaked
			{
				yMin = pointVector[i].y;
				minChanged = true;
			}
		}

		if (minChanged == true)
		{
			std::cout << "Start of question " << yMin << std::endl;
		}
	}

	///////// END /////////

	// Draw contours
	Mat drawing = Mat::zeros( imageThresh.size(), CV_8UC3 );

	drawContours(drawing, contours, -1, Scalar(0,255,0), 3);

	Mat mask = Mat::zeros(image.size(), CV_8UC3);

	for (u_int i = 0; i < contours.size(); i++)
	{
		// Create Polygon from vertices
		vector<Point> ROI_Poly;
		approxPolyDP(contours[i], ROI_Poly, 1.0, true);

		// Fill polygon white
		fillConvexPoly(mask, &ROI_Poly[0], ROI_Poly.size(), Scalar(255,255,225), 8, 0);
	}

	// Create new image for result storage
	Mat imageDest =  Mat::zeros(image.size(), image.type());

	// Cut out ROI and store it in imageDest
	image.copyTo(imageDest, mask);

	//imwrite("./images/newImage.png", imageDest);

	namedWindow("Processed Image", WINDOW_NORMAL);
	imshow("Processed Image", imageDest);

	waitKey(0);
}
*/
