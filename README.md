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
* [Leanchat-cloud-code](https://github.com/leancloud/leanchat-cloudcode)，Leanchat 云代码后端

## 部署项目
见[如何替换 AppId 部署应用](https://github.com/leancloud/leanchat-android/wiki/%E5%A6%82%E4%BD%95%E6%9B%BF%E6%8D%A2-AppId-%E9%83%A8%E7%BD%B2%E5%BA%94%E7%94%A8)

###[Document](http://leancloud.cn/docs/realtime.html)

###[API](http://leancloud.cn/docs/api/android/doc/index.html)

#####其他文档、源代码阅读指南、设计思路在 [wiki](https://github.com/leancloud/leanchat-android/wiki) 里面，强烈建议您先读读[代码结构](https://github.com/leancloud/leanchat-android/wiki/%E4%BB%A3%E7%A0%81%E7%BB%93%E6%9E%84)一节。
