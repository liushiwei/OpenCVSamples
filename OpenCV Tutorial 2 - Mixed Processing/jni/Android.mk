LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include C:/Users/Jesus/workspace/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk
 
LOCAL_MODULE    := mixed_sample
LOCAL_SRC_FILES := jni_part.cpp
LOCAL_SRC_FILES += CMT.cpp
LOCAL_SRC_FILES += libopentld/tld/Settings.cpp
LOCAL_SRC_FILES += libopentld/tld/tld.cpp
LOCAL_SRC_FILES += libopentld/imacq/ImAcq.cpp
LOCAL_SRC_FILES +=    libopentld/mftracker/BB.cpp
LOCAL_SRC_FILES +=    libopentld/mftracker/BBPredict.cpp
LOCAL_SRC_FILES +=    libopentld/mftracker/FBTrack.cpp
LOCAL_SRC_FILES +=    libopentld/mftracker/Lk.cpp
 LOCAL_SRC_FILES +=   libopentld/mftracker/Median.cpp
 LOCAL_SRC_FILES +=   libopentld/tld/Clustering.cpp
     LOCAL_SRC_FILES +=   libopentld/tld/DetectionResult.cpp
     LOCAL_SRC_FILES +=   libopentld/tld/DetectorCascade.cpp
     LOCAL_SRC_FILES +=   libopentld/tld/EnsembleClassifier.cpp
     LOCAL_SRC_FILES +=   libopentld/tld/ForegroundDetector.cpp
     LOCAL_SRC_FILES +=   libopentld/tld/MedianFlowTracker.cpp
     LOCAL_SRC_FILES +=   libopentld/tld/NNClassifier.cpp
     LOCAL_SRC_FILES +=   libopentld/tld/TLDUtil.cpp
     LOCAL_SRC_FILES +=   libopentld/tld/VarianceFilter.cpp
     LOCAL_SRC_FILES +=   libopentld/3rdparty/cvblobs/blob.cpp
     LOCAL_SRC_FILES +=   libopentld/3rdparty/cvblobs/BlobContour.cpp
     LOCAL_SRC_FILES +=   libopentld/3rdparty/cvblobs/BlobOperators.cpp
     LOCAL_SRC_FILES +=   libopentld/3rdparty/cvblobs/BlobProperties.cpp
     LOCAL_SRC_FILES +=   libopentld/3rdparty/cvblobs/BlobResult.cpp
     LOCAL_SRC_FILES +=   libopentld/3rdparty/cvblobs/ComponentLabeling.cpp
     
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
