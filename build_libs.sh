#!/bin/bash

ndk-build -C ./xhook/src/main/jni
cp -f ./xhook/src/main/libs/armeabi-v7a/libxhook.so ./xhook/libs/armeabi-v7a/
cp -f ./xhook/src/main/libs/arm64-v8a/libxhook.so   ./xhook/libs/arm64-v8a/
cp -f ./xhook/src/main/libs/x86/libxhook.so         ./xhook/libs/x86/