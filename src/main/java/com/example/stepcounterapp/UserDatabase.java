package com.example.stepcounterapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class userDatabase {
    private static final String TAG = "User Database";
    private static final String DATABASE_TABLE = "users";
    private static final int DATABASE_VERSION = 1;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public userDatabase(Context ctx) {
        DBHelper = new DatabaseHelper(ctx);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context)
        {
            super(context, "Environment.getExternalStorageDirectory().getPath()"+"userDatabase", null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table users (_id integer primary key autoincrement, username text not null, password text not null);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS users");
            onCreate(db);
        }
    }


    public void open() throws SQLException {
        db = DBHelper.getWritableDatabase();
    }


    public void close() {
        DBHelper.close();
    }

    public long AddUser(String username, String password) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("username", username);
        initialValues.put("password", password);
        return db.insert(DATABASE_TABLE, null, initialValues);

    }

    public boolean Login(String username, String password) throws SQLException {
        Cursor mCursor = db.rawQuery("SELECT * FROM " + DATABASE_TABLE + " WHERE username=? AND password=?", new String[]{username,password});
        if (mCursor != null) {
            if(mCursor.getCount() > 0)
            {
                return true;
            }
        }
        return false;
    }

}