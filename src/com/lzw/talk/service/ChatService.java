package com.lzw.talk.service;

import com.avos.avoscloud.*;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBHelper;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
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

  public static void insertDBMsg(Msg msg) {
    List<Msg> msgs = new ArrayList<Msg>();
    msgs.add(msg);
    DBHelper dbHelper = new DBHelper(App.ctx, App.DB_NAME, App.DB_VER);
    DBMsg.insertMsgs(dbHelper, msgs);
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

  public static void sendImageMsg(User user, String imagePath) throws IOException, AVException {
    AVFile file = AVFile.withAbsoluteLocalPath("img", imagePath);
    file.save();
    String url = file.getUrl();
    String sendText = imagePath + "&" + url;
    Msg msg = sendMessage(ChatService.getPeerId(user), Msg.TYPE_IMAGE, sendText);
    DBMsg.insertMsg(msg);
  }

  public static void sendTextMsg(User user, String content) {
    String peerId = getPeerId(user);
    int type = Msg.TYPE_TEXT;
    Msg msg = sendMessage(peerId, type, content);
    DBMsg.insertMsg(msg);
  }

  public static Msg sendMessage(String peerId, int type, String content) {
    Msg msg;
    msg = new Msg();
    msg.setStatus(Msg.STATUS_SEND_START);
    msg.setContent(content);
    msg.setTimestamp(System.currentTimeMillis());
    msg.setFromPeerId(getSelfId());
    msg.setToPeerIds(Utils.oneToList(peerId));
    msg.setObjectId(Utils.uuid());
    msg.setType(type);

    AVMessage avMsg = msg.toAVMessage();
    Session session = getSession();
    session.sendMessage(avMsg);
    return msg;
  }

  public static void openSession() {
    Session session = getSession();
    session.open(new LinkedList<String>());
  }
}
