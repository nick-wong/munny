package com.example.nick.munny;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nick on 7/20/2016.
 */
public class MunnyDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "MunnySQLite.db";
    private static final int DATABASE_VERSION = 2;

    public static final String MUNNY_TABLE_NAME = "MUNNY";
    public static final String MUNNY_COLUMN_ID = "_id";
    public static final String MUNNY_COLUMN_COST = "cost";
    public static final String MUNNY_COLUMN_DESCRIPTION = "description";
    public static final String MUNNY_COLUMN_DATE = "date";
    public static final String MUNNY_COLUMN_DATE_STRING = "date_string";
    public static final String MUNNY_COLUMN_TYPE = "type";
    public static final String MUNNY_COLUMN_IMAGE = "image";

    public MunnyDBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + MUNNY_TABLE_NAME +
                        "(" + MUNNY_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        MUNNY_COLUMN_COST + " TEXT, " +
                        MUNNY_COLUMN_DESCRIPTION + " TEXT, " +
                        MUNNY_COLUMN_DATE + " TEXT, " +
                        MUNNY_COLUMN_DATE_STRING + " TEXT, " +
                        MUNNY_COLUMN_TYPE + " TEXT, " +
                        MUNNY_COLUMN_IMAGE + " BLOB)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MUNNY_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertMunny(String cost, String description, String date, String date_string,
                               String type, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(MUNNY_COLUMN_COST, cost);
        contentValues.put(MUNNY_COLUMN_DESCRIPTION, description);
        contentValues.put(MUNNY_COLUMN_DATE, date);
        contentValues.put(MUNNY_COLUMN_DATE_STRING, date_string);
        contentValues.put(MUNNY_COLUMN_TYPE, type);
        contentValues.put(MUNNY_COLUMN_IMAGE, image);

        db.insert(MUNNY_TABLE_NAME, null, contentValues);
        return true;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, MUNNY_TABLE_NAME);
        return numRows;
    }

    public boolean updateMunny(Integer id, String cost, String description, String date,
                               String date_string, String type, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MUNNY_COLUMN_COST, cost);
        contentValues.put(MUNNY_COLUMN_DESCRIPTION, description);
        contentValues.put(MUNNY_COLUMN_DATE, date);
        contentValues.put(MUNNY_COLUMN_DATE_STRING, date_string);
        contentValues.put(MUNNY_COLUMN_TYPE, type);
        contentValues.put(MUNNY_COLUMN_IMAGE, image);
        db.update(MUNNY_TABLE_NAME, contentValues, MUNNY_COLUMN_ID + " = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteMunny(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(MUNNY_TABLE_NAME,
                MUNNY_COLUMN_ID + " = ? ",
                new String[] { Integer.toString(id) });
    }

    public Cursor getMunny(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("SELECT * FROM " + MUNNY_TABLE_NAME + " WHERE " +
                MUNNY_COLUMN_ID + "=?", new String[]{Integer.toString(id)});
        return res;
    }

    public Cursor getAllMunnies() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + MUNNY_TABLE_NAME +
                                   " ORDER BY datetime(" + MUNNY_COLUMN_ID + ") DESC", null );
        return res;
    }

    public Cursor getAllMunniesWithinDays(int days) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + MUNNY_TABLE_NAME +
                        " WHERE " + MUNNY_COLUMN_DATE + " >= date('now', '-" + days + " days') " +
                        "AND " + MUNNY_COLUMN_DATE + " < date('now') " +
                        "ORDER BY date,_id DESC", null );
        return res;
    }

    public Cursor getAllMunniesWithType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + MUNNY_TABLE_NAME +
                " WHERE " + MUNNY_COLUMN_TYPE + " = ? " +
                " ORDER BY date,_id DESC", new String[]{type});
        return res;
    }
}

