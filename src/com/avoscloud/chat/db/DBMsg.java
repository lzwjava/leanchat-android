package com.avoscloud.chat.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.avoscloud.chat.avobject.User;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.entity.Msg;
import com.avoscloud.chat.entity.RoomType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lzw on 14-5-28.
 */
public class DBMsg {
  public static final String MESSAGES = "messages";
  public static final String FROM_PEER_ID = "fromPeerId";
  public static final String CONVID = "convid";
  public static final String TIMESTAMP = "timestamp";
  public static final String OBJECT_ID = "objectId";
  public static final String CONTENT = "content";
  public static final String STATUS = "status";
  public static final String TYPE = "type";
  public static final String TO_PEER_ID = "toPeerId";
  public static final String ROOM_TYPE = "roomType";
  public static final String OWNER_ID = "ownerId";

  public static void createTable(SQLiteDatabase db) {
    db.execSQL("create table if not exists messages (id integer primary key, objectId varchar(63) unique not null," +
        "ownerId varchar(255) not null,fromPeerId varchar(255) not null, convid varchar(255) not null ," +
        "toPeerId varchar(255), content varchar(1023)," +
        " status integer,type integer,roomType integer,timestamp varchar(63))");
  }

  public static void dropTable(SQLiteDatabase db) {
    db.execSQL("drop table if exists messages");
  }

  public static int insertMsg(Msg msg) {
    List<Msg> msgs = new ArrayList<Msg>();
    msgs.add(msg);
    return insertMsgs(msgs);
  }

  public static int insertMsgs(List<Msg> msgs) {
    DBHelper dbHelper = new DBHelper(App.ctx, App.DB_NAME, App.DB_VER);
    if (msgs == null || msgs.size() == 0) {
      return 0;
    }
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.beginTransaction();
    int n = 0;
    try {
      for (Msg msg : msgs) {
        ContentValues cv = new ContentValues();
        cv.put(OBJECT_ID, msg.getObjectId());
        cv.put(TIMESTAMP, msg.getTimestamp() + "");
        cv.put(FROM_PEER_ID, msg.getFromPeerId());
        cv.put(STATUS, msg.getStatus().getValue());
        cv.put(ROOM_TYPE, msg.getRoomType().getValue());
        cv.put(CONVID, msg.getConvid());
        cv.put(TO_PEER_ID, msg.getToPeerId());
        cv.put(OWNER_ID, User.curUserId());
        cv.put(TYPE, msg.getType().getValue());
        cv.put(CONTENT, msg.getContent());
        db.insert(MESSAGES, null, cv);
        n++;
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
      db.close();
    }
    return n;
  }

  public static List<Msg> getMsgs(DBHelper dbHelper, String convid, int size) {
    List<Msg> msgs = new ArrayList<Msg>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    assert db != null;
    Cursor c = db.query(MESSAGES, null, "convid=?", new String[]{convid}, null, null, TIMESTAMP + " desc", size + "");
    while (c.moveToNext()) {
      Msg msg = createMsgByCursor(c);
      msgs.add(msg);
    }
    c.close();
    Collections.reverse(msgs);
    db.close();
    return msgs;
  }

  public static Msg createMsgByCursor(Cursor c) {
    Msg msg = new Msg();
    msg.setFromPeerId(c.getString(c.getColumnIndex(FROM_PEER_ID)));
    msg.setContent(c.getString(c.getColumnIndex(CONTENT)));
    Msg.Status status = Msg.Status.fromInt(c.getInt(c.getColumnIndex(STATUS)));
    msg.setStatus(status);
    msg.setConvid(c.getString(c.getColumnIndex(CONVID)));
    msg.setObjectId(c.getString(c.getColumnIndex(OBJECT_ID)));
    int roomTypeInt = c.getInt(c.getColumnIndex(ROOM_TYPE));
    RoomType roomType = RoomType.fromInt(roomTypeInt);
    msg.setRoomType(roomType);
    String toPeerId = c.getString(c.getColumnIndex(TO_PEER_ID));
    msg.setToPeerId(toPeerId);
    msg.setTimestamp(Long.parseLong(c.getString(c.getColumnIndex(TIMESTAMP))));
    Msg.Type type = Msg.Type.fromInt(c.getInt(c.getColumnIndex(TYPE)));
    msg.setType(type);
    return msg;
  }

  public static List<Msg> getRecentMsgs(String ownerId) {
    DBHelper dbHelper = new DBHelper(App.ctx, App.DB_NAME, App.DB_VER);
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = db.query(true, MESSAGES, null, "ownerId=?",
        new String[]{ownerId}, CONVID, null, TIMESTAMP + " desc", null);
    List<Msg> msgs = new ArrayList<Msg>();
    while (c.moveToNext()) {
      Msg msg = createMsgByCursor(c);
      msgs.add(msg);
    }
    c.close();
    db.close();
    return msgs;
  }

  public static int updateStatusAndTimestamp(String objecctId, Msg.Status status, long timestamp) {
    ContentValues cv = new ContentValues();
    cv.put(STATUS, status.getValue());
    cv.put(TIMESTAMP, timestamp + "");
    String objectId = objecctId;
    return updateMessage(objectId, cv);
  }

  public static int updateMessage(String objectId, ContentValues cv) {
    DBHelper dbHelper = new DBHelper(App.ctx, App.DB_NAME, App.DB_VER);
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    int updateN = db.update(MESSAGES, cv, "objectId=?", new String[]{objectId});
    db.close();
    return updateN;
  }

  public static int updateStatus(String objectId, Msg.Status status) {
    ContentValues cv = new ContentValues();
    cv.put(STATUS, status.getValue());
    return updateMessage(objectId, cv);
  }
}
