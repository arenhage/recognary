LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES  :=on
OPENCV_INSTALL_MODULES :=on

include ../../OpenCV-2.4.5-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := objtrack
LOCAL_SRC_FILES := objtrack.cpp
LOCAL_LDLIBS    += -llog -ldl

include $(BUILD_SHARED_LIBRARY)