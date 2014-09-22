#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include "libopentld/tld/tld.h"
#include "cmt.h"
#include <vector>

using namespace std;
using namespace cv;
using namespace tld;

extern "C" {
JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);
JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_OpenTLD(JNIEnv*, jobject, jlong addrGray, jlong addrRgba,jlong x,jlong y,jlong width,jlong height);

bool initiated=false;
  TLD * etld=NULL ;
  CMT * cmt=new CMT();
  long rect[4];

JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> v;

    FastFeatureDetector detector(50);
    detector.detect(mGr, v);
    for( unsigned int i = 0; i < v.size(); i++ )
    {
        const KeyPoint& kp = v[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }
}

JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_OpenTLD(JNIEnv*, jobject, jlong addrGray, jlong addrRgba,
		jlong x, jlong y, jlong width, jlong height)
{
	if (etld!=NULL)
	{
		etld->release();
		delete etld;
	}
    etld = new TLD();
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;

    int t=  mRgb.cols;
    etld->detectorCascade->imgWidth =mGr.cols;
    etld->detectorCascade->imgHeight = mGr.rows;
    etld->detectorCascade->imgWidthStep = mGr.step;

    Rect r;
    r.x= x;//mGr.size().width/2-mGr.size().width/4;
    r.y= y;// mGr.size().height/2-mGr.size().height/4;
    r.width= width;//mGr.size().width/2;
    r.height= height;//mGr.size().height/2;
    etld->selectObject(mGr,& r );

}


JNIEXPORT jintArray JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_getRect(JNIEnv *env, jobject)
{

	 jintArray result;
	 result = env->NewIntArray(4);

	 if (etld->currBB == NULL) {
	     return NULL;
	 }

	jint fill[4];
	if (etld->currBB!=NULL)
	{
		fill[0]=etld->currBB->x;
		fill[1]=etld->currBB->y;
		fill[2]=etld->currBB->width;
		fill[3]=etld->currBB->height;
		env->SetIntArrayRegion(result, 0, 4, fill);
		return result;
	}

	return NULL;

}


JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_ProcessTLD(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
	 Mat& mRgb = *(Mat*)addrRgba;

	 etld->processImage(mRgb);

	 if (etld->currBB!=NULL)
	 {
		 Rect r;
		 r.x=etld->currBB->x;
		 r.y=etld->currBB->y;
		 r.width= etld->currBB->width;
		 r.height= etld->currBB->height;

		 rectangle(mRgb ,r,Scalar(0,0,255,0),5);
		/*   for(size_t i = 0; i < etld->detectorCascade->detectionResult->fgList->size(); i++)
		                {
		                    Rect r = etld->detectorCascade->detectionResult->fgList->at(i);
		                    rectangle(mRgb, r, Scalar(255,0,0,0), 1);
		                }
		                */
	 }
	 else
	 {
		 Rect r;
		 r.x=mRgb.size().width/2;
		 r.y=mRgb.size().height/2;;
		 r.width= 100;
	     r.height= 100;
	     rectangle(mRgb ,r,Scalar(0,0,0,255),5);
	 }

}

JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_OpenCMT(JNIEnv*, jobject, jlong addrGray, jlong addrRgba,jlong x, jlong y, jlong width, jlong height)
{

//	 if (cmt!=NULL)
//	 {
//		 delete cmt;
//	 }
//	 cmt = new CMT();
	 Mat& im_gray  = *(Mat*)addrGray;
	 Point p1(x,y);
	 Point p2(x+width,y+height);

    initiated=false;
	 if (cmt->initialise(im_gray, p1, p2))
		 initiated=true;

}

JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_ProcessCMT(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
	if (!initiated)
		return;
	Mat& img  = *(Mat*)addrRgba;
	Mat& im_gray  = *(Mat*)addrGray;

	cmt->processFrame(im_gray);

//	        for(int i = 0; i<cmt->trackedKeypoints.size(); i++)
//	            cv::circle(img, cmt->trackedKeypoints[i].first.pt, 3, cv::Scalar(255,255,255));
	        cv::line(img, cmt->topLeft, cmt->topRight, cv::Scalar(255,255,255));
	        cv::line(img, cmt->topRight, cmt->bottomRight, cv::Scalar(255,255,255));
	        cv::line(img, cmt->bottomRight, cmt->bottomLeft, cv::Scalar(255,255,255));
	        cv::line(img, cmt->bottomLeft, cmt->topLeft, cv::Scalar(255,255,255));

}


JNIEXPORT jintArray JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_CMTgetRect(JNIEnv *env, jobject)
{

	if (!initiated)
		return NULL;

	 jintArray result;
	 result = env->NewIntArray(8);



	jint fill[8];

	{

		fill[0]=cmt->topLeft.x;
		fill[1]=cmt->topLeft.y;
		fill[2]=cmt->topRight.x;
		fill[3]=cmt->topRight.y;
		fill[4]=cmt->bottomLeft.x;
		fill[5]=cmt->bottomLeft.y;
		fill[6]=cmt->bottomRight.x;
		fill[7]=cmt->bottomRight.y;
		env->SetIntArrayRegion(result, 0, 8, fill);
		return result;
	}

	return NULL;

}


}

