package com.avoscloud.chat.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.util.ParcelableUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by lzw on 14-5-28.
 */
public class MsgsTable {
  public static final String MSG_ID = "msg_id";
  public static final String CONVID = "convid";
  public static final String TIME = "time";
  public static final String OBJECT = "object";
  public static final String MSGS_TABLE = "msgs";
  private static final String MSG_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `msgs` " +
      "(`id` INTEGER PRIMARY KEY AUTOINCREMENT, `msg_id` VARCHAR(63) UNIQUE NOT NULL,`convid` VARCHAR(63) NOT NULL," +
      "`object` BLOB NOT NULL,`time` VARCHAR(63) NOT NULL)";
  private static final String DROP_MSG_TABLE_SQL = "drop table if exists msgs";
  private static MsgsTable msgsTable;
  private DBHelper dbHelper;

  private MsgsTable(DBHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  public synchronized static MsgsTable getCurrentUserInstance() {
    if (msgsTable == null) {
      msgsTable = new MsgsTable(DBHelper.getCurrentUserInstance(App.ctx));
    }
    return msgsTable;
  }

  static AVIMTypedMessage createMsgByCursor(Cursor c) {
    byte[] msgBytes = c.getBlob(c.getColumnIndex(OBJECT));
    if (msgBytes != null) {
      AVIMTypedMessage msg = (AVIMTypedMessage) ParcelableUtil.unmarshall(msgBytes, AVIMTypedMessage.CREATOR);
      return msg;
    } else {
      return null;
    }
  }

  void createTable(SQLiteDatabase db) {
    db.execSQL(MSG_TABLE_SQL);
  }

  void dropTable(SQLiteDatabase db) {
    db.execSQL(DROP_MSG_TABLE_SQL);
  }

  public int insertMsg(AVIMTypedMessage msg) {
    List<AVIMTypedMessage> msgs = Arrays.asList(msg);
    return insertMsgs(msgs);
  }

  public byte[] marshallMsg(AVIMTypedMessage msg) {
    byte[] msgBytes = ParcelableUtil.marshall(msg);
    if (msgBytes == null) {
      throw new NullPointerException("msg bytes is null");
    }
    return msgBytes;
  }

  int insertMsgs(List<AVIMTypedMessage> msgs) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.beginTransaction();
    int n = 0;
    try {
      for (AVIMTypedMessage msg : msgs) {
        ContentValues cv = new ContentValues();
        cv.put(MSG_ID, msg.getMessageId());
        cv.put(TIME, msg.getTimestamp() + "");
        cv.put(CONVID, msg.getConversationId());
        cv.put(OBJECT, marshallMsg(msg));
        db.insert(MSGS_TABLE, null, cv);
        n++;
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
    return n;
  }

  public List<AVIMTypedMessage> selectMsgs(String convid, long maxTime, int limit) {
    List<AVIMTypedMessage> msgs = new ArrayList<AVIMTypedMessage>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = db.query(MSGS_TABLE, null, "convid=? and time<?", new String[]{convid, maxTime + ""}, null, null,
        TIME + " desc",
        limit + "");
    while (c.moveToNext()) {
      AVIMTypedMessage msg = createMsgByCursor(c);
      msgs.add(msg);
    }
    c.close();
    Collections.reverse(msgs);
    return msgs;
  }

  public AVIMTypedMessage getMsgByMsgId(String msgId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = db.query(MSGS_TABLE, null, "msg_id=?", new String[]{msgId}, null, null, null);
    AVIMTypedMessage msg = null;
    if (c.moveToNext()) {
      msg = createMsgByCursor(c);
    }
    return msg;
  }

  public void deleteMsgs(String convid) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(MSGS_TABLE, "convid=?", new String[]{convid});
  }

  /*public static int updateStatusAndTimestamp(String msgId, AVIMTypedMessage status, long timestamp) {
    ContentValues cv = new ContentValues();
    cv.put(STATUS, status.getValue());
    cv.put(TIMESTAMP, timestamp + "");
    String objectId = msgId;
    return updateMsg(objectId, cv);
  }*/

  public int updateMsg(String msgId, AVIMTypedMessage msg) {
    byte[] bytes = marshallMsg(msg);
    ContentValues cv = new ContentValues();
    cv.put(OBJECT, bytes);
    return updateMsg(msgId, cv);
  }

  private AVIMTypedMessage selectMsgByMsgId(String msgId) {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = db.query(MSGS_TABLE, null, "msg_id=?", new String[]{msgId}, null, null, null);
    AVIMTypedMessage msg;
    if (c.moveToNext()) {
      msg = createMsgByCursor(c);
    } else {
      msg = null;
    }
    c.close();
    return msg;
  }

  public int updateStatus(String msgId, AVIMMessage.AVIMMessageStatus status) {
    AVIMTypedMessage msg = selectMsgByMsgId(msgId);
    if (msg.getMessageStatus() != status) {
      msg.setMessageStatus(status);
      return updateMsg(msgId, msg);
    } else {
      return 0;
    }
  }

  private int updateMsg(String msgId, ContentValues cv) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    int updateN = db.update(MSGS_TABLE, cv, "msg_id=?", new String[]{msgId});
    return updateN;
  }

  void close() {
    msgsTable = null;
  }

  public void updateFailedMsg(AVIMTypedMessage msg, String tmpId) {
    ContentValues cv = new ContentValues();
    cv.put(OBJECT, marshallMsg(msg));
    cv.put(TIME, msg.getTimestamp());
    cv.put(MSG_ID, msg.getMessageId());
    updateMsg(tmpId, cv);
  }
}
