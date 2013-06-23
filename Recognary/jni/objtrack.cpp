#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <opencv2/features2d/features2d.hpp>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <vector>

using namespace std;
using namespace cv;

int lowerH=170;
int lowerS=160;
int lowerV=60;

int upperH=180;
int upperS=255;
int upperV=255;

int lastX = -1;
int lastY = -1;

IplImage* imgScribble = NULL;

void DrawTrack(IplImage* imgThreshed) {
	// Calculate the moments to estimate the position of the ball
	CvMoments *moments = new CvMoments();
	cvMoments(imgThreshed, moments, 1);
	double moment10 = cvGetSpatialMoment(moments, 1, 0);
	double moment01 = cvGetSpatialMoment(moments, 0, 1);
	double area = cvGetCentralMoment(moments, 0, 0);

	if(area>1000){
		// calculate the position of the ball
		int posX = moment10/area;
		int posY = moment01/area;

		if(lastX>=0 && lastY>=0 && posX>=0 && posY>=0) {
			// Draw a yellow line from the previous point to the current point
			cvLine(imgScribble, cvPoint(posX, posY), cvPoint(lastX, lastY), cvScalar(0,0,255), 4);
		}

		lastX = posX;
		lastY = posY;
	}

	free(moments);
}

IplImage* GetThresholdedImage(IplImage* imgHSV) {
	IplImage* imgThreshed = cvCreateImage(cvGetSize(imgHSV), IPL_DEPTH_8U, 1);
	//Now we do the actual thresholding
	//cvInRangeS(imgHSV, cvScalar(170,160,60), cvScalar(180,256,256), imgThreshed);
	cvInRangeS(imgHSV, cvScalar(lowerH,lowerS,lowerV), cvScalar(upperH,upperS,upperV), imgThreshed);
	return imgThreshed;
}

extern "C" {
JNIEXPORT void JNICALL Java_se_swg_recognary_ObjTrackView_findFeatures(JNIEnv* env, jobject thiz, jint width,
		jint height, jbyteArray yuv, jintArray bgra, jboolean debug)
	{
		jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
		jint*  _bgra = env->GetIntArrayElements(bgra, 0);

		Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
		Mat mbgra(height, width, CV_8UC4, (unsigned char *)_bgra);
		//Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

		IplImage img_color = mbgra;
		//IplImage img_gray = mgray;

		if(imgScribble == NULL) {
			imgScribble=cvCreateImage(cvGetSize(&img_color),IPL_DEPTH_8U, 4);
			cvZero(imgScribble);	//make the scribble picture black
		}

		cvSmooth(&img_color, &img_color, CV_GAUSSIAN,9,9);

		//ARGB stored in java as int array becomes BGRA at native level
		cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);

		// create a new hsv image and do thresholding on it
		IplImage* imgHSV = cvCreateImage(cvGetSize(&img_color), IPL_DEPTH_8U, 3);
		cvCvtColor(&img_color, imgHSV, CV_BGR2HSV);
		//create a new image that will hold the threholded image
		IplImage* imgThreshed = GetThresholdedImage(imgHSV);

		//release the temporary HSV image and return this thresholded image
		cvReleaseImage(&imgHSV);
		cvSmooth(imgThreshed, imgThreshed, CV_GAUSSIAN,9,9); //smooth the binary image using Gaussian kernel

		DrawTrack(imgThreshed);
		cvAdd(&img_color, imgScribble, &img_color, 0);

		CvMemStorage* storage = cvCreateMemStorage(0);

		// show thresholded
		if(debug)
			cvCvtColor(imgThreshed, &img_color, CV_GRAY2BGR);

		// -- above vector will contain the circles
		CvSeq* circles = cvHoughCircles(imgThreshed, storage, CV_HOUGH_GRADIENT, 2, imgThreshed->height/8, 200, 100, 0, 0);

		for (int i = 0; i<circles->total && i<3; i++) { // max 3 circles
			float* p = (float*)cvGetSeqElem( circles, i );
			circle(mbgra, Point(p[0],p[1]), 3, Scalar(0,255,0,255), 2);
			circle(mbgra, Point(p[0],p[1]), p[2], Scalar(0,0,255,255), 4);
		}

		// cleanup resources
		cvReleaseMemStorage(&storage);
		cvReleaseImage(&imgHSV);
		cvReleaseImage(&imgThreshed);

		env->ReleaseIntArrayElements(bgra, _bgra, 0);
		env->ReleaseByteArrayElements(yuv, _yuv, 0);
	}
	JNIEXPORT jboolean JNICALL Java_se_swg_recognary_MainActivity_clearScribble(JNIEnv* env) {
		imgScribble = NULL;
		return imgScribble == NULL;
	}

	JNIEXPORT void JNICALL Java_se_swg_recognary_MainActivity_resetHSVSettings(JNIEnv* env) {
		//reset
		lowerH=170;
		lowerS=160;
		lowerV=60;
		upperH=180;
		upperS=255;
		upperV=255;
	}

	//H=1, S=2, V=3
	//lower=1, upper=2
	JNIEXPORT jint JNICALL Java_se_swg_recognary_MainActivity_changeHSVbound(JNIEnv* env, jobject thiz, jint lu, jint id, jint val) {
		if(lu == 1) {
			if(id == 1)
				lowerH = val;
			else if (id == 2)
				lowerS = val;
			else if(id == 3)
				lowerV = val;
		}
		else if(lu == 2) {
			if(id == 1)
				upperH = val;
			else if (id == 2)
				upperS = val;
			else if(id == 3)
				upperV = val;
		}
		return lu;
	}
}
