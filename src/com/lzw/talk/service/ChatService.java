package com.lzw.talk.service;

import android.util.Log;
import com.avos.avoscloud.*;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBHelper;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.entity.RecentMsg;
import com.lzw.talk.util.Logger;
import com.lzw.talk.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lzw on 14-7-9.
 */
public class ChatService {

  public static List<User> findChatUsers() throws AVException {
    List<User> users = getAllUsers();
    App.registerBatchUserCache(users);
    users.remove(User.curUser());
    return users;
  }

  public static List<User> getAllUsers() throws AVException {
    AVQuery<User> q = AVUser.getQuery(User.class);
    return q.find();
  }

  public static <T extends AVUser> String getPeerId(T user) {
    return user.getObjectId();
  }

  public static String getSelfId() {
    return getPeerId(User.curUser());
  }

  public static <T extends AVUser> void withUsersToWatch(List<T> users, boolean watch) {
    List<String> peerIds = new ArrayList<String>();
    for (AVUser user : users) {
      peerIds.add(getPeerId(user));
    }
    String selfId = getPeerId(User.curUser());
    Session session = SessionManager.getInstance(selfId);
    if (watch) {
      session.watchPeers(peerIds);
    }
  }

  public static <T extends AVUser> void withUserToWatch(T user, boolean watch) {
    List<T> users = new ArrayList<T>();
    users.add(user);
    withUsersToWatch(users, watch);
  }

  public static Session getSession() {
    return SessionManager.getInstance(getPeerId(User.curUser()));
  }

  public static void sendResponseMessage(Msg msg) {
    Msg resMsg = new Msg();
    resMsg.setType(Msg.TYPE_RESPONSE);
    resMsg.setToPeerIds(Utils.oneToList(msg.getFromPeerId()));
    resMsg.setFromPeerId(getSelfId());
    resMsg.setContent(msg.getTimestamp() + "");
    resMsg.setObjectId(msg.getObjectId());
    Session session = getSession();
    session.sendMessage(resMsg.toAVMessage());
  }

  public static void sendAudioMsg(User user, String path, String msgId) throws IOException, AVException {
    sendFileMsg(user, msgId, Msg.TYPE_AUDIO, path);
  }

  public static void sendImageMsg(User user, String filePath, String msgId) throws IOException, AVException {
    sendFileMsg(user, msgId, Msg.TYPE_IMAGE, filePath);
  }

  public static void sendFileMsg(User user, String objectId, int type, String filePath) throws IOException, AVException {
    AVFile file = AVFile.withAbsoluteLocalPath(objectId, filePath);
    file.save();
    String url = file.getUrl();
    String sendText = filePath + "&" + url;
    Msg msg = sendMessage(ChatService.getPeerId(user), type, sendText, objectId);
    DBMsg.insertMsg(msg);
  }

  public static void sendTextMsg(User user, String content) {
    String peerId = getPeerId(user);
    int type = Msg.TYPE_TEXT;
    Msg msg = sendMessage(peerId, type, content);
    Log.i("lzw","sendTextMsg fromId="+msg.getFromPeerId()+" toId="+msg.getToPeerIds());
    DBMsg.insertMsg(msg);
  }

  public static Msg sendMessage(String peerId, int type, String content) {
    String objectId = Utils.uuid();
    return sendMessage(peerId, type, content, objectId);
  }

  public static Msg sendMessage(String toPeerId, int type, String content, String objectId) {
    Msg msg;
    msg = new Msg();
    msg.setStatus(Msg.STATUS_SEND_START);
    msg.setContent(content);
    msg.setTimestamp(System.currentTimeMillis());
    msg.setFromPeerId(getSelfId());
    msg.setToPeerIds(Utils.oneToList(toPeerId));
    msg.setObjectId(objectId);
    msg.setType(type);
    Log.i("lzw","sendMsg fromPeerId="+getSelfId()+" toPeerId="+toPeerId);

    AVMessage avMsg = msg.toAVMessage();
    Session session = getSession();
    session.sendMessage(avMsg);
    return msg;
  }

  public static void openSession() {
    Session session = getSession();
    session.open(new LinkedList<String>());
  }

  public static HashMap<String, String> parseUri(String uri) {
    String[] parts = uri.split("&");
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("path", parts[0]);
    map.put("url", parts[1]);
    return map;
  }

  public static void sendLocationMessage(String peerId, String address, double latitude, double longtitude) {
    String content = address + "&" + latitude + "&" + longtitude;
    Logger.d("content=" + content);
    Msg msg = sendMessage(peerId, Msg.TYPE_LOCATION, content);
    DBMsg.insertMsg(msg);
  }

  public static List<RecentMsg> getRecentMsgs() throws AVException {
    List<Msg> msgs = DBMsg.getRecentMsgs();
    cacheUserFromMsgs(msgs);
    ArrayList<RecentMsg> recentMsgs = new ArrayList<RecentMsg>();
    for (Msg msg : msgs) {
      RecentMsg recentMsg = new RecentMsg();
      String chatUserId = msg.getChatUserId();
      recentMsg.toUser = App.lookupUser(chatUserId);
      Logger.d("toUser="+recentMsg.toUser.getUsername()
      +" fromId="+msg.getFromPeerId()+" toPeerId="+msg.getToPeerIds());
      recentMsg.msg = msg;
      recentMsgs.add(recentMsg);
    }
    return recentMsgs;
  }

  public static void cacheUserFromMsgs(List<Msg> msgs) throws AVException {
    List<String> uncachedId = new ArrayList<String>();
    for (Msg msg : msgs) {
      String chatUserId = msg.getChatUserId();
      uncachedId.add(chatUserId);
    }
    UserService.cacheUser(uncachedId);
  }
}
