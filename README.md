# LeanChat Android 客户端

## 简介

LeanChat 是 [LeanCloud](http://leancloud.cn) [实时通信](https://leancloud.cn/docs/realtime.html) 组件的 Demo，通过该应用你可以学习和了解 LeanCloud 实时通信功能。

应用体验下载地址：[http://fir.im/leanchat](http://fir.im/leanchat)

## 效果截图

![img](https://raw.githubusercontent.com/lzwjava/plan/master/im361.png)

![img](https://raw.githubusercontent.com/lzwjava/plan/master/im362.png)

## Leanchat 项目构成

* [Leanchat-android](https://github.com/leancloud/leanchat)：Android 客户端
* [Leanchat-ios](https://github.com/leancloud/leanchat-ios)：iOS 客户端
* [Leanchat-cloud-code](https://github.com/leancloud/leanchat-cloudcode)：服务端，使用 LeanCloud [云代码](https://leancloud.cn/docs/cloud_code_guide.html) 实现

如果你从 github clone 速度很慢，可以从 [这里](https://download.leancloud.cn/demo/) 下载项目源码压缩包。

## 部署

### 创建应用

注册并登录 [LeanCloud](http://leancloud.cn)，创建一个新应用，并记下 appId 和 appKey。

### 部署 Android 客户端

请按照以下步骤进行初始化：

#### 卸载已有的 LeanChat

为了防止数据影响而出现问题，如果已经安装 LeanChat，请卸载。

#### 修改 appId 和 appKey

为了使自己可以拥有独立的应用和数据，请修改 `com.avoscloud.chat.base.App.onCreate()` 方法中初始化 `AVOSCloud` 部分，使用自己应用的 appId 和 appKey：  

   ```
   AVOSCloud.initialize(this, <appId>, <appKey>); 
   ```

#### 初始化数据表

LeanChat 的运行会依赖一些表，使用下面的步骤进行创建：

1. `com.avoscloud.chat.ui.activity.MainActivity#onCreate(Bundle)` 方法的最后，取消对 `App.initTables();` 语句的注释。
2. 编译并安装运行应用。
3. 注册账号并登陆。

登陆完成后，`App.initTables();` 语句会被执行，数据表就会被创建。可以从 LeanCloud 网站的数据控制台确认是否存在如下几张表：

```
AddRequest
Avatar
UpdateInfo
```
确认无误后，退出应用。**并将 `App.initTabes();` 语句注释或者删除**。

**提示**：如果需要，你还可以查看 LeanChat 范例应用的表结构，请以公共账号登录 [leancloud.cn](http://leancloud.cn) ，账号/密码：leancloud@163.com/Public123 ，选择应用 LeanChat-Public 即可，这只是一个示例，和本 demo 的表结构一样，但数据不一样。



### 部署服务端

详见 [这里](https://github.com/leancloud/leanchat-cloudcode)

### 完成

重新编译 Android 客户端并安装，你就拥有属于自己的 LeanChat 了！

## 开发相关

### 相关文档

* [实时通信服务开发指南](https://leancloud.cn/docs/realtime.html)
* [Android 实时通信服务](https://leancloud.cn/docs/android_realtime.html)
* [Android 开发指南](https://leancloud.cn/docs/android_guide.html)

### 依赖组件

LeanChat Android 客户端依赖 LeanCloud Android SDK 如下组件：

* 基础模块
* 实时通信模块
* 统计模块

这些包都保存在 [libs](https://github.com/avoscloud/Adventure/tree/master/libs) 目录中。如果需要更新这些模块 SDK，可以到 [这里](https://cn.avoscloud.com/docs/sdk_down.html) 下载。

### 其他文档

* [代码结构](https://github.com/leancloud/leanchat-android/wiki/%E4%BB%A3%E7%A0%81%E7%BB%93%E6%9E%84)
* [wiki](https://github.com/leancloud/leanchat-android/wiki)
