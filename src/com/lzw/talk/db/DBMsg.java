package com.lzw.talk.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.lzw.talk.entity.Msg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 14-5-28.
 */
public class DBMsg {
  public static final String CHAT = "chat";
  public static final String TXT = "txt";
  public static final String FROM_ID = "from_id";
  public static final String TO_ID = "to_id";
  public static final String CREATED = "created";

  public static void createTable(SQLiteDatabase db) {
    db.execSQL("create table if not exists chat (id integer primary key, from_id varchar(255), " +
        "to_id varchar(255), txt varchar(1023), created integer)");
  }

  public static int insertMsg(DBHelper dbHelper,Msg msg){
    List<Msg> msgs=new ArrayList<Msg>();
    msgs.add(msg);
    return insertMsgs(dbHelper,msgs);
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
        cv.put(FROM_ID, msg.getFrom());
        cv.put(TO_ID, msg.getTo());
        cv.put(CREATED, msg.getCreated());
        cv.put(TXT, msg.getTxt());
        db.insert(CHAT, null, cv);
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
    Cursor c = db.query(CHAT, new String[]{FROM_ID,
        TO_ID, TXT, CREATED},
        "from_id =? and to_id = ? or from_id = ? and to_id = ?", new String[]{me, he, he, me}, null, null, CREATED,
        "1000");
    while (c.moveToNext()) {
      Msg msg = new Msg();
      msg.setTo(c.getString(c.getColumnIndex(TO_ID)));
      msg.setFrom(c.getString(c.getColumnIndex(FROM_ID)));
      msg.setTxt(c.getString(c.getColumnIndex(TXT)));
      msg.setCreated(c.getInt(c.getColumnIndex(CREATED)));
      msgs.add(msg);
    }
    c.close();
    return msgs;
  }
}
