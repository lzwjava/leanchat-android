##[下载](http://fir.im/leanchat)

## Leanchat 项目构成
* [Leanchat-android](https://github.com/leancloud/leanchat)
* [Leanchat-ios](https://github.com/leancloud/leanchat-ios)
* [Leanchat-cloud-code](https://github.com/leancloud/leanchat-cloudcode)，Leanchat 云代码后端

用 LeanCloud 实时通信 SDK做的沟通工具  Leanchat

![img](./screenshot/im361.png)

![img](./screenshot/im362.png)

![img](./screenshot/im363.png)

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

* [AVOS Cloud SDK](https://cn.avoscloud.com/docs/sdk_down.html)，基础模块和实时通信模块即可。
* [AdventureCloud](https://github.com/avoscloud/AdventureCloud) ，相关的服务器代码

## 搭建
*  申请应用，替换App.java中的appId,appKey
*  fork [AdventureCloud](https://github.com/avoscloud/AdventureCloud)，部署到自己的云代码中去
*  建表`AddRequest`
*  [申请百度地图key](http://developer.baidu.com/map)，替换掉AndroidMenifest.xml 中的baidu key

## 更新日志

###v1.1.2
* 新的一套录音、音量显示UI
* 增加检查更新功能
* 限制了只能竖屏
* 取消自定义的dialog
* 修复点击顶部栏通知进入错误的聊天室的bug
* 可能修复fragment ui重叠的bug

###v1.1.3
* 使用新的聊天协议，以便和ios版互通

## 发布步骤
* 将 AndroidManifest.xml 文件的 百度map key改为 生产环境的key
* 改版本号、版本名字
* 将App.java 中的debug设为false，打开发送崩溃日志的选项
* 上传apk文件到fir.im/leanchat，以及后台数据库（用于自动更新）
* 写更新日志

## Summary

总的来说，四步加聊天功能：

1. open session（打开一个会话）
2. watch peerIds（关注一些人，会收到相应的上线下线通知）
3. send message 
4. handle received message

## Open session

```java
final String selfId = getPeerId(curUser);
List<String> peerIds = new LinkedList<String>();
Session session = SessionManager.getInstance(selfId);
session.open(selfId, peerIds);
```

## Watch peers when need

```java
String selfId = getPeerId(User.curUser());
Session session = SessionManager.getInstance(selfId);
session.watchPeers(peerIds);
```
## Send message to peer

```java
List<String> ids = new ArrayList<String>();
ids.add(getPeerId(chatUser));
String selfId = getPeerId(curUser)
Session session = SessionManager.getInstance(selfId);
session.sendMessage(json, ids);
```

## Handle everything
接受消息继承一个类就可以了，这里能做到非常细粒度的控制又很方便，

```java
public class Receiver extends AVMessageReceiver {

  //session打开后调用
  @Override  
  public void onSessionOpen(Context context, Session session) {
  }

  //因断网session暂停时调用
  @Override
  public void onSessionPaused(Context context, Session session) {
  }

  //网络重新连接上时调用
  @Override
  public void onSessionResumed(Context context, Session session) {
  }

  //接受消息
  @Override
  public void onMessage(Context context, Session session, String msg, String fromPeerId) {
  }

  //消息发送成功
  @Override
  public void onMessageSent(Context context, Session session, String msg, List<String> receivers) {
  }

  //消息发送失败
  @Override
  public void onMessageFailure(Context context, Session session, String msg, List<String> receivers) {
  }

  //上线通知
  @Override
  public void onStatusOnline(Context context, Session session, List<String> peerIds) {
  }

  //下线通知
  @Override
  public void onStatusOffline(Context context, Session session, List<String> peerIds) {
  }

  //出错时调用
  @Override
  public void onError(Context context, Session session, Throwable e) {
  }
}
```

###[Document](http://leancloud.cn/docs/realtime.html)

###[API](http://leancloud.cn/docs/api/android/doc/index.html)
