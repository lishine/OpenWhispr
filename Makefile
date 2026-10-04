SHELL := /bin/bash
APK := app/build/outputs/apk/debug/app-debug.apk
ADB ?= adb

.PHONY: build test adb-install clean

build:
	./gradlew assembleDebug

test:
	./gradlew testDebugUnitTest

adb-install: build
	$(ADB) install -r $(APK)

clean:
	./gradlew clean
