package org.pring.converse;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
  public static final String TABLE_TOPICS = "topics";

  public DataBaseHelper(Context context) {
    super(context, "topics.db", null, 1);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + TABLE_TOPICS
        + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "id INTEGER, "
        + "category_id INTEGER, " + "topic TEXT, " + "last_used INTEGER);");

    db.execSQL("CREATE UNIQUE INDEX data_idx ON " + TABLE_TOPICS
        + "(id, category_id);");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
}