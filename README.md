# LeanChat Android 客户端

## 简介

LeanChat 是 [LeanCloud](http://leancloud.cn) [实时通信](https://leancloud.cn/docs/realtime.html) 组件的 Demo，通过该应用你可以学习和了解 LeanCloud 实时通信功能。

应用体验下载地址：[http://fir.im/leanchat](http://fir.im/leanchat)

## 效果截图

![img](https://raw.githubusercontent.com/lzwjava/plan/master/im361.png)

![img](https://raw.githubusercontent.com/lzwjava/plan/master/im362.png)

## Leanchat 项目构成

* [Leanchat-android](https://github.com/leancloud/leanchat-android)：Android 客户端
* [Leanchat-ios](https://github.com/leancloud/leanchat-ios)：iOS 客户端
* [Leanchat-cloud-code](https://github.com/leancloud/leanchat-cloudcode)：可选服务端，使用 LeanCloud [云代码](https://leancloud.cn/docs/cloud_code_guide.html) 实现，实现了聊天的签名，更安全。

## Eclipse 运行需知
1. 用到了 [ButterKnife](https://github.com/JakeWharton/butterknife) 开源库，Eclipse需要设置一下来支持 ButterKnife 的注解，具体如何设置见 http://jakewharton.github.io/butterknife/ide-eclipse.html 。否则会因为 view 没有绑定上，导致崩溃。

### 相关文档

[实时通信服务开发指南](https://leancloud.cn/docs/realtime_v2.html)

### 依赖组件

LeanChat Android 客户端依赖 LeanCloud Android SDK 如下组件：

* 基础模块
* 实时通信模块
* 统计模块

如果需要这些模块 SDK，可以到 [这里](https://cn.avoscloud.com/docs/sdk_down.html) 下载。

### 其他文档

* [wiki](https://github.com/leancloud/leanchat-android/wiki)

