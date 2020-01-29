LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_USE_AAPT2 := true

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := KBBacklightManager
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res

include $(BUILD_PACKAGE) 
