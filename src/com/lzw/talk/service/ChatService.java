package com.lzw.talk.service;

import com.avos.avoscloud.*;
import com.lzw.talk.avobject.User;
import com.lzw.talk.base.App;
import com.lzw.talk.db.DBHelper;
import com.lzw.talk.db.DBMsg;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.util.MyUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-7-9.
 */
public class ChatService {

  public static List<AVUser> findChatUsers() throws AVException {
    AVQuery<AVUser> q = AVUser.getQuery();
    List<AVUser> users = q.find();
    users.remove(User.curUser());
    return users;
  }

  public static void insertDBMsg(String json) {
    Msg msg = new Msg(json);
    msg.setCreated(MyUtils.currentSecs());
    List<Msg> msgs = new ArrayList<Msg>();
    msgs.add(msg);
    DBHelper dbHelper = new DBHelper(App.cxt, App.DB_NAME, App.DB_VER);
    DBMsg.insertMsgs(dbHelper, msgs);
  }

  public static String getPeerId(AVUser user) {
    return user.getObjectId();
  }

  public static void withUsers(List<AVUser> users, boolean watch) {
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

  public static void withUserToWatch(AVUser user, boolean watch) {
    List<AVUser> users = new ArrayList<AVUser>();
    users.add(user);
    withUsers(users, watch);
  }
}
