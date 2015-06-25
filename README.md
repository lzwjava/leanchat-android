# LeanChat Android 客户端

## 简介

LeanChat 是 [LeanCloud](http://leancloud.cn) [实时通信](https://leancloud.cn/docs/realtime.html) 组件的 Demo，通过该应用你可以学习和了解 LeanCloud 实时通信功能。

应用体验下载地址：[http://fir.im/leanchat](http://fir.im/leanchat)

## 效果

![leanchat-android1](https://cloud.githubusercontent.com/assets/5022872/7362725/665a7f14-eda0-11e4-8fc5-e7ea5dea0618.gif)

## 下载
请直接点击 Github 上的`Download Zip`，如图所示，这样只下载最新版本。如果是 `git clone`，则可能非常慢，因为含杂很大的提交历史。某次测试两者是1.5M:40M。

![qq20150618-2 2x](https://cloud.githubusercontent.com/assets/5022872/8223520/4c25415a-15ab-11e5-912d-b5dab916ce86.png)

## LeanChat Android 项目构成

* leanchatlib，核心的聊天逻辑和聊天界面库。有了它，可以快速集成聊天功能，支持文字、音频、图片、表情消息，消息通知。同时也有相应的 [iOS版本](https://github.com/leancloud/leanchat-ios/tree/master/LeanChatLib) 。
* leancahtlib-demo，leanchatlib 最简单的使用例子。可以看到配置一下 AndroidManifest，以及额外配置一下 user 信息，即可集成聊天，不管是用 LeanCloud 的用户系统还是自己的用户系统。
* leanchat ，为整个聊天应用。它包含好友管理、群组管理、地理消息、附近的人、个人页面、登录注册的功能，完全基于 LeanCloud 的存储和通信功能。


## LeanChat 项目构成

* [Leanchat-android](https://github.com/leancloud/leanchat-android)：Android 客户端
* [Leanchat-ios](https://github.com/leancloud/leanchat-ios)：iOS 客户端
* [Leanchat-cloud-code](https://github.com/leancloud/leanchat-cloudcode)：可选服务端，使用 LeanCloud [云代码](https://leancloud.cn/docs/cloud_code_guide.html) 实现，实现了聊天的签名，更安全。
* 

## Eclipse 或 Intellij IDEA 运行需知
1. 请装相应的 Gradle 插件
1. Intellij IDEA 用户建议装 Android Studio，AS 是基于 IDEA 的，不仅有和 IDEA 一样的体验，还是官方推荐的 IDE。
1. 用到了 [ButterKnife](https://github.com/JakeWharton/butterknife) 开源库，Eclipse需要设置一下来支持 ButterKnife 的注解，具体如何设置见 http://jakewharton.github.io/butterknife/ide-eclipse.html 。否则会因为 view 没有绑定上，导致崩溃。
1. 如果不装 Gradle 插件，非要转换成 Ant 格式，注意项目存在依赖关系，leanchat 依赖 leanchatlib，leanchatlib-demo 也依赖 leanchatlib，建议，先转换 leanchatlib 为 Ant 格式，再转换 leanchat 和 leanchatlib-demo，同时手工修复一下依赖和编译错误。

## 如何集成 leanchatlib
1. 请运行 leanchatlib-demo ，看[这段代码](https://github.com/leancloud/leanchat-android/blob/master/leanchatlib-demo/src/main/java/com/avoscloud/leanchatlib_demo/App.java#L40-L60)配置，提供一个UserInfo对象即可，完全不需要AVUser。


## 相关文档

[实时通信服务开发指南](https://leancloud.cn/docs/realtime_v2.html)


## 依赖组件

LeanChat Android 客户端依赖 LeanCloud Android SDK 如下组件：

* 基础模块
* 实时通信模块
* 统计模块

如果需要这些模块 SDK，可以到 [这里](https://cn.avoscloud.com/docs/sdk_down.html) 下载。

## 其他文档



