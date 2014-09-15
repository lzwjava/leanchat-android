package com.lzw.talk.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.lzw.talk.entity.Msg;
import com.lzw.talk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-5-28.
 */
public class DBMsg {
  public static final String MESSAGES = "messages";
  public static final String FROM_PEER_ID = "fromPeerId";
  public static final String TO_PEER_ID = "toPeerId";
  public static final String TIMESTAMP = "timestamp";
  public static final String OBJECT_ID = "objectId";
  public static final String CONTENT = "content";

  public static void createTable(SQLiteDatabase db) {
    db.execSQL("create table if not exists messages (id integer primary key, objectId varchar(63)," +
        "fromPeerId varchar(255), toPeerId varchar(255), content varchar(1023), timestamp varchar(63))");
  }

  public static int insertMsg(DBHelper dbHelper, Msg msg) {
    List<Msg> msgs = new ArrayList<Msg>();
    msgs.add(msg);
    return insertMsgs(dbHelper, msgs);
  }

  public static int insertMsgs(DBHelper dbHelper, List<Msg> msgs) {
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
        cv.put(TO_PEER_ID, msg.getToPeerIds().get(0));
        cv.put(CONTENT, msg.getContent());
        db.insert(MESSAGES, null, cv);
        n++;
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
    return n;
  }

  public static List<Msg> getMsgs(DBHelper dbHelper, String me, String he) {
    List<Msg> msgs = new ArrayList<Msg>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    assert db != null;
    Cursor c = db.query(MESSAGES, new String[]{FROM_PEER_ID,
            TO_PEER_ID, CONTENT, TIMESTAMP,OBJECT_ID},
        "fromPeerId =? and toPeerId = ? or fromPeerId = ? and toPeerId = ?",
        new String[]{me, he, he, me}, null, null,
        TIMESTAMP,
        "1000");
    while (c.moveToNext()) {
      Msg msg = new Msg();
      String id = c.getString(c.getColumnIndex(TO_PEER_ID));
      msg.setToPeerIds(Utils.oneToList(id));
      msg.setFromPeerId(c.getString(c.getColumnIndex(FROM_PEER_ID)));
      msg.setContent(c.getString(c.getColumnIndex(CONTENT)));
      msg.setObjectId(c.getString(c.getColumnIndex(OBJECT_ID)));
      msg.setTimestamp(Long.parseLong(c.getString(c.getColumnIndex(TIMESTAMP))));
      msgs.add(msg);
    }
    c.close();
    return msgs;
  }
}
