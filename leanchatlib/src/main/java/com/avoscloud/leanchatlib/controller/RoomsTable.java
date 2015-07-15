package com.avoscloud.leanchatlib.controller;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.avoscloud.leanchatlib.model.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 最近对话的列表，这里称之为 Rooms
 * Created by lzw on 15/2/12.
 */
public class RoomsTable {
  private static final String ROOMS_TABLE = "rooms";
  private static final String ROOM_CONVID = "convid";
  private static final String ROOM_UNREAD_COUNT = "unread_count";
  private static final String ROOM_ID = "id";

  private static class SQL {
    private static final String CREATE_ROOMS_TABLE =
        "CREATE TABLE IF NOT EXISTS " + ROOMS_TABLE + "(" +
            ROOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ROOM_CONVID + " VARCHAR(63) UNIQUE NOT NULL, " +
            ROOM_UNREAD_COUNT + " INTEGER DEFAULT 0)";
    private static final String UPDATE_ROOMS_INCREASE_UNREAD_COUNT_WHERE_CONVID =
        "UPDATE " + ROOMS_TABLE + " SET " + ROOM_UNREAD_COUNT + " = "
            + ROOM_UNREAD_COUNT + " + 1 WHERE " + ROOM_CONVID + " =?";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + ROOMS_TABLE;
  }

  private DBHelper dbHelper;
  private static Map<String, RoomsTable> roomsTableInstances = new ConcurrentHashMap<>();

  private RoomsTable(DBHelper dbHelper) {
    this.dbHelper = dbHelper;
  }

  public synchronized static RoomsTable getInstanceByUserId(String userId) {
    RoomsTable roomsTable = roomsTableInstances.get(userId);
    if (roomsTable == null) {
      roomsTable = new RoomsTable(new DBHelper(ChatManager.getContext(), userId));
    }
    return roomsTable;
  }

  void createTable(SQLiteDatabase db) {
    db.execSQL(SQL.CREATE_ROOMS_TABLE);
  }

  void dropTable(SQLiteDatabase db) {
    db.execSQL(SQL.DROP_TABLE);
  }

  private static String getWhereClause(String... columns) {
    List<String> conditions = new ArrayList<String>();
    for (String column : columns) {
      conditions.add(column + " = ? ");
    }
    return TextUtils.join(" and ", conditions);
  }

  public List<Room> selectRooms() {
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor c = db.query(ROOMS_TABLE, null, null, null, null, null, null);
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
    room.setConversationId(c.getString(c.getColumnIndex(ROOM_CONVID)));
    room.setUnreadCount(c.getInt(c.getColumnIndex(ROOM_UNREAD_COUNT)));
    return room;
  }

  public void insertRoom(String convid) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(ROOM_CONVID, convid);
    db.insertWithOnConflict(ROOMS_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
  }

  public void deleteRoom(String convid) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(ROOMS_TABLE, getWhereClause(ROOM_CONVID), new String[]{convid});
  }

  public void increaseUnreadCount(String convid) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.execSQL(SQL.UPDATE_ROOMS_INCREASE_UNREAD_COUNT_WHERE_CONVID, new String[]{convid});
  }

  public void clearUnread(String convid) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues cv = new ContentValues();
    cv.put(ROOM_UNREAD_COUNT, 0);
    db.update(ROOMS_TABLE, cv, getWhereClause(ROOM_CONVID),
        new String[]{convid});
  }
}
