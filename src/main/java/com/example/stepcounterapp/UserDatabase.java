package com.example.stepcounterapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class UserDatabase {
    private static final String TAG = "User Database";
    private static final String DB_NAME = "UserDatabase.db";

    private static final String LOGIN_TABLE = "logins";
    private static final String LOGIN_COLUMN1 = "username";
    private static final String LOGIN_COLUMN2 = "password";

    private static final String USER_TABLE = "users";
    private static final String USER_COLUMN1 = "username";
    private static final String USER_COLUMN2 = "steps";
    private static final String USER_COLUMN3 = "calories";
    private static final String USER_COLUMN4 = "distance";
    private static final String USER_COLUMN5 = "weight";
    private static final String USER_COLUMN6 = "height";
    private static final String USER_COLUMN7 = "age";
    private static final String USER_COLUMN8 = "exerciseTime";
    private static final String USER_COLUMN9 = "date";

    private static final String HISTORIC_TABLE = "history";
    private static final String HISTORIC_COLUMN1 = "username";
    private static final String HISTORIC_COLUMN2 = "date";
    private static final String HISTORIC_COLUMN3 = "steps";
    private static final String HISTORIC_COLUMN4 = "calories";
    private static final String HISTORIC_COLUMN5 = "distance";
    private static final String HISTORIC_COLUMN6 = "weight";
    private static final String HISTORIC_COLUMN7 = "id";
    private static final String HISTORIC_COLUMN8 = "exerciseTime";

    private static final int DATABASE_VERSION = 5;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public UserDatabase(Context ctx) {
        DBHelper = new DatabaseHelper(ctx);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table if not exists " + LOGIN_TABLE + " (" + LOGIN_COLUMN1 + " text primary key not null, " + LOGIN_COLUMN2 + " text not null);");
            db.execSQL("create table if not exists " + USER_TABLE + " (" + USER_COLUMN1 + " text primary key not null, " + USER_COLUMN2 + " integer, " + USER_COLUMN3 +" integer, " + USER_COLUMN4 + " integer, " + USER_COLUMN5 + " real, " + USER_COLUMN6 + " integer, " + USER_COLUMN7 + " integer, " + USER_COLUMN8 + " real, " + USER_COLUMN9 + " text);");
            db.execSQL("create table if not exists " + HISTORIC_TABLE + " (" + HISTORIC_COLUMN7 + " integer primary key, " + HISTORIC_COLUMN1 + " text not null, " + HISTORIC_COLUMN2 + " text not null, " + HISTORIC_COLUMN3 + " integer, " + HISTORIC_COLUMN4 +" integer, " + HISTORIC_COLUMN5 + " integer, " + HISTORIC_COLUMN6 + " real, "  + HISTORIC_COLUMN8 + " real);");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + LOGIN_TABLE  + ";");
            db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE + ";");
            db.execSQL("DROP TABLE IF EXISTS "  + HISTORIC_TABLE + ";");
            onCreate(db);
        }
    }


    public void open() throws SQLException {
        db = DBHelper.getWritableDatabase();
    }


    public void close() {
        DBHelper.close();
    }

    public int AddUser(String username, String password) throws SQLException {
        //return -1 for existing user, 0 for successful completion, and 1 for other errors
        Cursor result;
        Boolean cont = false;

        result = db.rawQuery("select * from " + LOGIN_TABLE + " where " + LOGIN_COLUMN1 + " = ?;", new String[]{username});
        if (result != null) {
            if(result.getCount() == 0)
            {
                cont = true;
            }else {
                return -1;
            }
        }
        if (cont) {
            ContentValues values = new ContentValues();
            values.put(LOGIN_COLUMN1, username);
            values.put(LOGIN_COLUMN2, password);
            //todo depide on and add any more data that will be required for login
            db.insert(LOGIN_TABLE, null, values);

            //todo check the formatting on the date (think its fine double check)
            Date date = getDateNoTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            String currentDate = format.format(date);

            //todo if a value is -1 it has not been provided
            values = new ContentValues();
            values.put(USER_COLUMN1, username);
            values.put(USER_COLUMN2, 0);
            values.put(USER_COLUMN3, 0);
            values.put(USER_COLUMN4, 0);
            values.put(USER_COLUMN5, -1);
            values.put(USER_COLUMN6, -1);
            values.put(USER_COLUMN7, -1);
            values.put(USER_COLUMN8, 0.0);
            values.put(USER_COLUMN9, currentDate);
            //todo same as above
            db.insert(USER_TABLE, null, values);

            return 0;
        }else {
            return 1;
        }
    }

    public boolean Login(String username, String password) throws SQLException {
        Cursor cursor = db.rawQuery("select * from " + LOGIN_TABLE + " where " + LOGIN_COLUMN1 + " = ? and " + LOGIN_COLUMN2 + " = ?;", new String[]{username,password});
        if (cursor != null) {
            if(cursor.getCount() > 0)
            {
                return true;
            }
        }
        return false;
    }

    public User saveUser(User user) throws ParseException {
        Cursor cursor = db.rawQuery("select * from " + USER_TABLE + " where " + USER_COLUMN1 + " = ?;", new String[] {user.getUsername()});

        if (cursor != null) {
            cursor.moveToFirst();
            Date date = getDateNoTime();

            //todo handle exception here
            //todo check if this fires incorrectly because of time but not date
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            Date dataDate = format.parse(cursor.getString(8));

            if (date.after(dataDate)) {
                updateHistoricDB(user);
                user.updateSteps(0);
                user.updateCalories(0);
                user.updateDistance(0);
                user.updateExerciseTime(0);
                updateUserDB(user);
            }
        }
        return user;
    }

    public void updateUserDB(User user) throws ParseException {//todo handle exception here
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);

        Cursor cursor = db.rawQuery("select * from " + USER_TABLE + " where " + USER_COLUMN1 + " = ?;", new String[] {user.getUsername()});
        if (cursor != null) {
            ContentValues values = new ContentValues();
            values.put(USER_COLUMN1, user.getUsername());
            values.put(USER_COLUMN2, user.getSteps());
            values.put(USER_COLUMN3, user.getCalories());
            values.put(USER_COLUMN4, user.getDistance());
            values.put(USER_COLUMN5, user.getWeight());
            values.put(USER_COLUMN6, user.getHeight());
            values.put(USER_COLUMN7, user.getAge());
            values.put(USER_COLUMN8, user.getExerciseTime());
            values.put(USER_COLUMN9, currentDate);
            db.update(USER_TABLE, values, USER_COLUMN1 + " = ?", new String[] {user.getUsername()});
        } else {
            ContentValues values = new ContentValues();
            values.put(USER_COLUMN1, user.getUsername());
            values.put(USER_COLUMN2, user.getSteps());
            values.put(USER_COLUMN3, user.getCalories());
            values.put(USER_COLUMN4, user.getDistance());
            values.put(USER_COLUMN5, user.getWeight());
            values.put(USER_COLUMN6, user.getHeight());
            values.put(USER_COLUMN7, user.getAge());
            values.put(USER_COLUMN8, user.getExerciseTime());
            values.put(USER_COLUMN9, currentDate);
            db.insert(USER_TABLE, null, values);
        }
    }

    public void updateHistoricDB(User user) {
        Cursor cursor = db.rawQuery("select * from " + USER_TABLE + " where " + USER_COLUMN1 + " = ?;", new String[] {user.getUsername()});
        cursor.moveToFirst();
        Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ?" + " and " + HISTORIC_COLUMN2 + " = ?;", new String[] {user.getUsername(), cursor.getString(8)});
        if (hist != null) {
            if(hist.getCount() == 1) {
                //todo dont know if casting all variables as objects instead of strings will work
                ContentValues values = new ContentValues();
                values.put(HISTORIC_COLUMN1, user.getUsername());
                values.put(HISTORIC_COLUMN2, cursor.getString(8));
                values.put(HISTORIC_COLUMN3, cursor.getInt(1));
                values.put(HISTORIC_COLUMN4, cursor.getInt(2));
                values.put(HISTORIC_COLUMN5, cursor.getInt(3));
                values.put(HISTORIC_COLUMN6, cursor.getFloat(4));
                values.put(HISTORIC_COLUMN8, cursor.getFloat(7));
                db.update(HISTORIC_TABLE, values, HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " = ?", new String[] {user.getUsername(), cursor.getString(8)});
            } else if (hist.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(HISTORIC_COLUMN1, user.getUsername());
                values.put(HISTORIC_COLUMN2, cursor.getString(8));
                values.put(HISTORIC_COLUMN3, cursor.getInt(1));
                values.put(HISTORIC_COLUMN4, cursor.getInt(2));
                values.put(HISTORIC_COLUMN5, cursor.getInt(3));
                values.put(HISTORIC_COLUMN6, cursor.getFloat(4));
                values.put(HISTORIC_COLUMN8, cursor.getFloat(7));
                db.insert(HISTORIC_TABLE, null, values);

            } else {
                System.out.println("historic table returned more than 1 entry for a person on the same day");
            }
        }
    }

    public User populateUserData(String username) throws ParseException {
        User user = new User();
        //todo fetch user info add to user object
        Cursor cursor = db.rawQuery("select * from " + USER_TABLE + " where " + USER_COLUMN1 + " = ?;", new String[] {username});
        if (cursor != null) {
            cursor.moveToFirst();
            user.updateUsername(username);
            user.updateSteps(cursor.getInt(1));
            user.updateCalories(cursor.getInt(2));
            user.updateDistance(cursor.getInt(3));
            user.updateWeight(cursor.getFloat(4));
            user.updateHeight(cursor.getInt(5));
            user.updateAge(cursor.getInt(6));
            user.updateExerciseTime(cursor.getFloat(7));

            user = saveUser(user);
        }
        return user;
    }

    //gets the date without the time
    private Date getDateNoTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}