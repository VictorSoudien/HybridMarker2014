/*
 * DisplayImage.cpp
 *
 *  Created on: 24 Jun 2014
 *      Author: victor
 */

/*#include <opencv2/opencv.hpp>
#include <cstdio>

using namespace cv;

int main( int argc, char** argv )
{
  Mat image;
  image = imread( argv[1], 1 );

  if( argc != 2 || !image.data )
    {
      printf( "No image data \n" );
      return -1;
    }

  // set up the parameters (check the defaults in opencv's code in blobdetector.cpp)
  cv::SimpleBlobDetector::Params params;
  params.minDistBetweenBlobs = 50.0f;
  params.filterByInertia = false;
  params.filterByConvexity = false;
  params.filterByColor = false;
  params.filterByCircularity = false;
  params.filterByArea = true;
  params.minArea = 20.0f;
  params.maxArea = 500.0f;
  // ... any other params you don't want default value

  // set up and create the detector using the parameters
  cv::Ptr<cv::FeatureDetector> blob_detector = new cv::SimpleBlobDetector(params);
  blob_detector->create("SimpleBlob");

  // detect!
  vector<cv::KeyPoint> keypoints;
  blob_detector->detect(image, keypoints);

  // extract the x y coordinates of the keypoints:

  for (int i=0; i<keypoints.size(); i++){
      float X=keypoints[i].pt.x;
      float Y=keypoints[i].pt.y;

      putText(image, "Blob", Point(X,Y),FONT_HERSHEY_SIMPLEX | FONT_ITALIC, 1.0, Scalar(255,255,0));
  }

  namedWindow( "Display Image", WINDOW_AUTOSIZE );
  imshow( "Display Image", image );

  waitKey(0);

  return 0;
}*/



