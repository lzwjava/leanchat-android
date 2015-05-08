package com.avoscloud.leanchatlib.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.avoscloud.leanchatlib.model.Room;

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
      roomsTable = new RoomsTable(DBHelper.getCurrentUserInstance());
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
    Cursor c = db.rawQuery("SELECT * FROM rooms", null);
    List<Room> rooms = new ArrayList<>();
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
    return room;
  }

  public boolean isRoomExists(String convid) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    Cursor cursor = db.query(ROOMS_TABLE, new String[]{CONVID}, CONVID + "=?", new String[]{convid}, null, null, null);
    int count = cursor.getCount();
    cursor.close();
    if (count > 0) {
      return true;
    } else {
      return false;
    }
  }

  public void insertRoom(String convid) {
    if (!isRoomExists(convid)) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      Cursor cursor = db.query(ROOMS_TABLE, null, "convid=?", new String[]{convid}, null, null, null);
      if (!cursor.moveToNext()) {
        ContentValues cv = new ContentValues();
        cv.put(CONVID, convid);
        db.insert(ROOMS_TABLE, null, cv);
      }
      cursor.close();
    }
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
