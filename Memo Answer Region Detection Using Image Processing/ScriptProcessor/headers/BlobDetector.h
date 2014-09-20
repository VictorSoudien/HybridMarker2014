/*
 * BlobDetector.h
 *
 *  Created on: 03 Jul 2014
 *      Author: victor
 */

#ifndef BLOBDETECTOR_H_
#define BLOBDETECTOR_H_

#include <vector>

class BlobDetector
{
public:
	std::vector<int> getQuestionStartLocation(std::string filename);
};

#endif /* BLOBDETECTOR_H_ */
