package com.avoscloud.chat.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avoscloud.chat.base.App;
import com.avoscloud.chat.entity.Room;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzw on 15/2/12.
 */
public class RoomsTable {
  public static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS `rooms`";
  private static final String CONVID = "convid";
  private static final String UNREAD_COUNT = "unread_count";
  private static final String CONVS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `rooms` " +
      "(`id` INTEGER PRIMARY KEY AUTOINCREMENT,`convid` VARCHAR(63) UNIQUE NOT NULL, " +
      "`unread_count` INTEGER DEFAULT 0)";
  private static final String ROOMS_TABLE = "rooms";
  private static RoomsTable roomsTable;
  private DBHelper dbHelper;

  private RoomsTable(DBHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  public synchronized static RoomsTable getCurrentUserInstance() {
    if (roomsTable == null) {
      roomsTable = new RoomsTable(DBHelper.getCurrentUserInstance(App.ctx));
    }
    return roomsTable;
  }

  void createTable(SQLiteDatabase db) {
    db.execSQL(CONVS_TABLE_SQL);
  }

  void dropTable(SQLiteDatabase db) {
    db.execSQL(DROP_TABLE_SQL);
  }

  public List<Room> selectRooms() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = db.rawQuery("SELECT * FROM rooms LEFT JOIN (SELECT msgs.object,MAX(time) as time ,msgs.convid as msg_convid FROM msgs GROUP BY msgs.convid) ON rooms.convid=msg_convid ORDER BY time DESC", null);
    List<Room> rooms = new ArrayList<Room>();
    while (c.moveToNext()) {
      Room room = createRoomByCursor(c);
      rooms.add(room);
    }
    c.close();
    return rooms;
  }

  private Room createRoomByCursor(Cursor c) {
    Room room = new Room();
    room.setConvid(c.getString(c.getColumnIndex(CONVID)));
    room.setUnreadCount(c.getInt(c.getColumnIndex(UNREAD_COUNT)));
    AVIMTypedMessage lastMsg = MsgsTable.createMsgByCursor(c);
    room.setLastMsg(lastMsg);
    return room;
  }

  public void insertRoom(String convid) {
    ContentValues cv = new ContentValues();
    cv.put(CONVID, convid);
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.insert(ROOMS_TABLE, null, cv);
  }

  public void deleteRoom(String convid) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(ROOMS_TABLE, "convid=?", new String[]{convid});
  }

  public void increaseUnreadCount(String convid) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.execSQL("UPDATE rooms SET unread_count=unread_count+1 WHERE convid=?", new String[]{convid});
  }

  public void clearUnread(String convid) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(UNREAD_COUNT, 0);
    db.update(ROOMS_TABLE, cv, "convid=?", new String[]{convid});
  }

  void close() {
    roomsTable = null;
  }
}
