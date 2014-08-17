#奇遇

一个展示AVOS聊天功能的Demo 

![img](https://github.com/lzwjava/Adventure/blob/master/readme/shot1.png)

总的来说，四步加聊天功能：

1. open session
2. watch peerIds
3. send message
4. handle received message

## Open session with empty peers

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
