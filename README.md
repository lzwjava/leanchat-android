##[应用下载](http://fir.im/leanchat)

[项目下载地址](https://download.leancloud.cn/demo/)

## Leanchat 项目构成
* [Leanchat-android](https://github.com/leancloud/leanchat)
* [Leanchat-ios](https://github.com/leancloud/leanchat-ios)
* [Leanchat-cloud-code](https://github.com/leancloud/leanchat-cloudcode)，Leanchat 云代码后端

用 LeanCloud 实时通信 SDK做的沟通工具  Leanchat

![img](https://raw.githubusercontent.com/lzwjava/plan/master/im361.png)

![img](https://raw.githubusercontent.com/lzwjava/plan/master/im362.png)


##当前特性

###单聊
* 消息支持文字，图片
* 接受回执，有发送失败、发送成功，已接收状态。
* 本地保存消息记录
* 统一的服务器时间戳


###群聊
* 邀请群成员
* 踢人
* 建群
* 退群

## 依赖

用到的依赖包，都在[libs](https://github.com/avoscloud/Adventure/tree/master/libs)中：

* [AVOS Cloud SDK](https://cn.avoscloud.com/docs/sdk_down.html)，选择基础模块、实时通信模块和统计模块即可。
* [AdventureCloud](https://github.com/avoscloud/AdventureCloud) ，相关的服务器代码

## 搭建
*  申请应用，替换App.java中的appId,appKey
*  fork [AdventureCloud](https://github.com/avoscloud/AdventureCloud)，部署到自己的云代码中去
*  建表`AddRequest`
*  [申请百度地图key](http://developer.baidu.com/map)，替换掉AndroidMenifest.xml 中的baidu key


###[Document](http://leancloud.cn/docs/realtime.html)

###[API](http://leancloud.cn/docs/api/android/doc/index.html)

#####其他文档、源代码阅读指南、设计思路在 [wiki](https://github.com/leancloud/leanchat-android/wiki) 里面
