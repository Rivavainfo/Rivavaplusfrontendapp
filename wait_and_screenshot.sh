#!/bin/bash
adb="/Users/adityanarayan/Documents/android_studio/platform-tools/adb"
echo "Waiting for device..."
$adb wait-for-device
echo "Waiting for boot completed..."
while [ "$($adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do
    sleep 2
done
echo "Boot completed. Waiting 5s for UI to settle..."
sleep 5
echo "Launching app..."
$adb shell monkey -p com.rivavafi.universal -c android.intent.category.LAUNCHER 1
echo "Waiting 5s for app to open..."
sleep 5
echo "Taking screenshot..."
$adb shell screencap -p /sdcard/screen.png
$adb pull /sdcard/screen.png /Users/adityanarayan/.gemini/antigravity-ide/brain/5852cefd-b42c-48b7-bcaa-2c064853076f/app_screenshot.png
echo "Done"
