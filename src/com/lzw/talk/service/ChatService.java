package com.lzw.talk.service;

import com.avos.avoscloud.*;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBHelper;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-7-9.
 */
public class ChatService {

  public static List<User> findChatUsers() throws AVException {
    List<User> users = getAllUsers();
    users.remove(User.curUser());
    return users;
  }

  public static List<User> getAllUsers() throws AVException {
    AVQuery<User> q = AVUser.getQuery(User.class);
    return q.find();
  }

  public static void insertDBMsg(AVMessage avMsg) {
    Msg msg = Msg.fromAVMessage(avMsg);
    List<Msg> msgs = new ArrayList<Msg>();
    msgs.add(msg);
    DBHelper dbHelper = new DBHelper(App.cxt, App.DB_NAME, App.DB_VER);
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
}
