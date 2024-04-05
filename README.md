# LeanChat Android Client

## Introduction

LeanChat is a demo of the [Realtime Messaging](https://leancloud.cn/docs/realtime.html) component of [LeanCloud](http://leancloud.cn), through which you can learn and understand the real-time communication feature of LeanCloud.

You can experience the app by downloading it from: [http://fir.im/leanchat](http://fir.im/leanchat)

## Demonstration

![leanchat-android1](https://cloud.githubusercontent.com/assets/5022872/7362725/665a7f14-eda0-11e4-8fc5-e7ea5dea0618.gif)

## Download
Please directly click on `Download Zip` on Github as shown in the image below to download the latest version only. If you use `git clone`, it might be very slow because it includes a large commit history. In a test, the difference was 1.5M:40M.

![qq20150618-2 2x](https://cloud.githubusercontent.com/assets/5022872/8223520/4c25415a-15ab-11e5-912d-b5dab916ce86.png)

## LeanChat Android Project Structure

* leanchatlib: The core library for chat logic and interface. With it, you can quickly integrate chat functionality, supporting text, audio, image, and emoji messages, as well as message notifications. There is also a corresponding [iOS version](https://github.com/leancloud/leanchat-ios/tree/master/LeanChatLib).
* leanchatlib-demo: The simplest usage example of leanchatlib. You can see how to configure AndroidManifest and additional user information to integrate chat, whether using LeanCloud's user system or your own user system.
* leanchat: The entire chat application. It includes features such as friend management, group management, location-based messaging, nearby users, personal pages, login, and registration, all based on LeanCloud's storage and communication capabilities.

## LeanChat Project Structure

* [Leanchat-android](https://github.com/leancloud/leanchat-android): Android client
* [Leanchat-ios](https://github.com/leancloud/leanchat-ios): iOS client
* [Leanchat-cloud-code](https://github.com/leancloud/leanchat-cloudcode): Optional server-side code, implemented using LeanCloud [Cloud Code](https://leancloud.cn/docs/cloud_code_guide.html) for chat signature, enhancing security.

## Tips for Running on Eclipse or Intellij IDEA
1. Please install the appropriate Gradle plugin.
2. Intellij IDEA users are recommended to use Android Studio, as AS is based on IDEA, providing the same experience as IDEA and being the officially recommended IDE.
3. Uses [ButterKnife](https://github.com/JakeWharton/butterknife) open-source library. Eclipse needs some settings to support ButterKnife annotations. See http://jakewharton.github.io/butterknife/ide-eclipse.html for specific settings. Otherwise, crashes may occur due to views not being bound.
4. If you do not install the Gradle plugin and insist on converting to Ant format, note that there are dependencies in the project: leanchat depends on leanchatlib, and leanchatlib-demo also depends on leanchatlib. It is recommended to convert leanchatlib to Ant format first, then leanchat and leanchatlib-demo, while manually fixing dependencies and compilation errors.

## How to Integrate leanchatlib
1. Run leanchatlib-demo and see [this code](https://github.com/leancloud/leanchat-android/blob/master/leanchatlib-demo/src/main/java/com/avoscloud/leanchatlib_demo/App.java#L40-L60) for configuration. Provide a UserInfo object, no AVUser is needed.

## Related Documentation

* [Realtime Messaging Service Development Guide](https://leancloud.cn/docs/realtime_v2.html)
* [Wiki](https://github.com/leancloud/leanchat-android/wiki)

## Dependency Components

LeanChat Android client depends on the following components of LeanCloud Android SDK:

* Base module
* Realtime communication module
* Analytics module

If you need these module SDKs, you can download them [here](https://cn.avoscloud.com/docs/sdk_down.html) or include them in your project using Gradle.

## Technical Support

For any issues encountered with this project, please raise an [issue](https://github.com/leancloud/leanchat-android/issues).