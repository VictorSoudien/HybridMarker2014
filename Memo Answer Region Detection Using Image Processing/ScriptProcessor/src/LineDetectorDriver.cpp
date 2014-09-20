/*
 * LineDetectorDriver.cpp
 *
 *  Created on: 20 Sep 2014
 *      Author: victor
 */

#include <iostream>
#include <fstream>

#include "../headers/HorizontalLineDetection.h"

int main (int argc, char** argv)
{
	if (argc != 2)
	{
		std::cout << "usage: ./ScriptProcessor imageName.png" << std::endl;
		return -1;
	}

	HorizontalLineDetection * lineDetect = new HorizontalLineDetection();
	std::cout << "Begin processing..." << std::endl;
	lineDetect->processImage(argv[1]);
	std::cout << "Processing complete" << std::endl;
};


