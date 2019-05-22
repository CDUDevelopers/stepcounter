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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class UserDatabase {
    private static final String TAG = "User Database";
    private static final String DB_NAME = "UserDatabase.db";
    private static final int DATABASE_VERSION = 7;
    //todo dont forget to increment version every time the tables are changed

    private static final String LOGIN_TABLE = "logins";
    private static final String LOGIN_COLUMN1 = "username";
    private static final String LOGIN_COLUMN2 = "password";

    //todo add gender to the relevant places
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

    private static final String EXERCISE_TABLE = "exercise";
    private static final String EXERCISE_COLUMN1 = "username";
    private static final String EXERCISE_COLUMN2 = "date";
    private static final String EXERCISE_COLUMN3 = "steps";
    private static final String EXERCISE_COLUMN4 = "calories";
    private static final String EXERCISE_COLUMN5 = "distance";
    private static final String EXERCISE_COLUMN6 = "id";
    private static final String EXERCISE_COLUMN7 = "exerciseTime";
    private static final String EXERCISE_COLUMN8 = "exerciseType";

    private static final String MAP_TABLE = "mapRoute";
    private static final String MAP_COLUMN1 = "id";
    private static final String MAP_COLUMN2 = "routeID";
    private static final String MAP_COLUMN3 = "latCoord";
    private static final String MAP_COLUMN4 = "longCoord";

    private static final long DAY_IN_MS = 1000 * 60 * 60 * 24;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    //----------------------------------------------------------------------------------------------

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
            db.execSQL("create table if not exists " + USER_TABLE + " (" + USER_COLUMN1 + " text primary key not null, " + USER_COLUMN2 + " integer, " + USER_COLUMN3 +" integer, " + USER_COLUMN4 + " integer, " + USER_COLUMN5 + " real, " + USER_COLUMN6 + " integer, " + USER_COLUMN7 + " integer, " + USER_COLUMN8 + " integer, " + USER_COLUMN9 + " text);");
            db.execSQL("create table if not exists " + HISTORIC_TABLE + " (" + HISTORIC_COLUMN7 + " integer primary key, " + HISTORIC_COLUMN1 + " text not null, " + HISTORIC_COLUMN2 + " text not null, " + HISTORIC_COLUMN3 + " integer, " + HISTORIC_COLUMN4 +" integer, " + HISTORIC_COLUMN5 + " integer, " + HISTORIC_COLUMN6 + " real, "  + HISTORIC_COLUMN8 + " integer);");
            db.execSQL("create table if not exists " + EXERCISE_TABLE + " (" + EXERCISE_COLUMN6 + " integer primary key, " + EXERCISE_COLUMN1 + " text not null, " + EXERCISE_COLUMN2 + " text not null, " + EXERCISE_COLUMN3 + " integer, " + EXERCISE_COLUMN4 +" integer, " + EXERCISE_COLUMN5 + " integer, " + EXERCISE_COLUMN7 + " integer," + EXERCISE_COLUMN8 + " text);");
            db.execSQL("create table if not exists " + MAP_TABLE + " (" + MAP_COLUMN1 + " integer primary key not null, " + MAP_COLUMN2 + " integer not null, " + MAP_COLUMN3 + " real, " + MAP_COLUMN4 + " real);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + LOGIN_TABLE  + ";");
            db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE + ";");
            db.execSQL("DROP TABLE IF EXISTS "  + HISTORIC_TABLE + ";");
            db.execSQL("DROP TABLE IF EXISTS "  + EXERCISE_TABLE + ";");
            db.execSQL("DROP TABLE IF EXISTS "  + MAP_TABLE + ";");
            onCreate(db);
        }
    }

    public void open() throws SQLException {
        db = DBHelper.getWritableDatabase();
    }

    public void close() {
        DBHelper.close();
    }

    //----------------------------------------------------------------------------------------------

    public int AddUser(String username, String password) throws SQLException {
        //return -1 for existing user, 0 for successful completion, and 1 for other errors
        Cursor accounts;
        Boolean cont = false;

        accounts = db.rawQuery("select * from " + LOGIN_TABLE + " where " + LOGIN_COLUMN1 + " = ?;", new String[]{username});
        if (accounts != null) {
            if(accounts.getCount() == 0)
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
            db.insert(LOGIN_TABLE, null, values);

            Date date = getDateNoTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            String currentDate = format.format(date);

            values = new ContentValues();
            values.put(USER_COLUMN1, username);
            values.put(USER_COLUMN2, 0);
            values.put(USER_COLUMN3, 0);
            values.put(USER_COLUMN4, 0);
            values.put(USER_COLUMN5, -1);
            values.put(USER_COLUMN6, -1);
            values.put(USER_COLUMN7, -1);
            values.put(USER_COLUMN8, 0);
            values.put(USER_COLUMN9, currentDate);
            db.insert(USER_TABLE, null, values);

            return 0;
        }else {
            return 1;
        }
    }

    public boolean Login(String username, String password) throws SQLException {
        Cursor accounts = db.rawQuery("select * from " + LOGIN_TABLE + " where " + LOGIN_COLUMN1 + " = ? and " + LOGIN_COLUMN2 + " = ?;", new String[]{username,password});
        if (accounts != null) {
            if(accounts.getCount() > 0)
            {
                return true;
            }
        }
        return false;
    }

    public User populateUserData(String username) {
        User user = new User();
        Cursor userData = db.rawQuery("select * from " + USER_TABLE + " where " + USER_COLUMN1 + " = ?;", new String[] {username});

        if (userData != null) {
            userData.moveToFirst();
            user.updateUsername(username);
            user.updateSteps(userData.getInt(1));
            user.updateCalories(userData.getInt(2));
            user.updateDistance(userData.getInt(3));
            user.updateWeight(userData.getFloat(4));
            user.updateHeight(userData.getInt(5));
            user.updateAge(userData.getInt(6));
            user.updateExerciseTime(userData.getLong(7));

            if (!saveUser(user)) {
                System.out.println("user information database write failed");
            }
        }
        return user;
    }

    //----------------------------------------------------------------------------------------------

    public Boolean saveUser(User user) {
        Cursor userData = db.rawQuery("select * from " + USER_TABLE + " where " + USER_COLUMN1 + " = ?;", new String[] {user.getUsername()});

        if (userData != null) {
            userData.moveToFirst();
            Date date = getDateNoTime();

            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            Date dataDate = null;

            try {
                dataDate = format.parse(userData.getString(8));
            } catch (ParseException e) {
                e.printStackTrace();
                System.out.println("tried to retrieve an incorrectly formatted date from the user database (Table users)");
                //todo fix the broken date if possible
                return false;
            }
            if (date.after(dataDate)) {
                updateHistoricDB(user);
                user.updateSteps(0);
                user.updateCalories(0);
                user.updateDistance(0);
                user.updateExerciseTime(0);
            }
            updateUserDB(user);
        }
        return true;
    }

    public void updateUserDB(User user) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);

        Cursor UserData = db.rawQuery("select * from " + USER_TABLE + " where " + USER_COLUMN1 + " = ?;", new String[] {user.getUsername()});
        if (UserData != null) {
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
        Cursor userData = db.rawQuery("select * from " + USER_TABLE + " where " + USER_COLUMN1 + " = ?;", new String[] {user.getUsername()});
        userData.moveToFirst();
        Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ?" + " and " + HISTORIC_COLUMN2 + " = ?;", new String[] {user.getUsername(), userData.getString(8)});
        if (hist != null) {
            if(hist.getCount() == 1) {
                ContentValues values = new ContentValues();
                values.put(HISTORIC_COLUMN1, user.getUsername());
                values.put(HISTORIC_COLUMN2, userData.getString(8));
                values.put(HISTORIC_COLUMN3, userData.getInt(1));
                values.put(HISTORIC_COLUMN4, userData.getInt(2));
                values.put(HISTORIC_COLUMN5, userData.getInt(3));
                values.put(HISTORIC_COLUMN6, userData.getFloat(4));
                values.put(HISTORIC_COLUMN8, userData.getFloat(7));
                db.update(HISTORIC_TABLE, values, HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " = ?", new String[] {user.getUsername(), userData.getString(8)});
            } else if (hist.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(HISTORIC_COLUMN1, user.getUsername());
                values.put(HISTORIC_COLUMN2, userData.getString(8));
                values.put(HISTORIC_COLUMN3, userData.getInt(1));
                values.put(HISTORIC_COLUMN4, userData.getInt(2));
                values.put(HISTORIC_COLUMN5, userData.getInt(3));
                values.put(HISTORIC_COLUMN6, userData.getFloat(4));
                values.put(HISTORIC_COLUMN8, userData.getFloat(7));
                db.insert(HISTORIC_TABLE, null, values);

            } else {
                System.out.println("historic table returned more than 1 entry for a person on the same day");
            }
        }
    }

    public void updateExerciseDB(String username, int steps, int calories, int distance, long exerciseTime, String exerciseType, ArrayList<Double> lat, ArrayList<Double> lng) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);

        ContentValues values = new ContentValues();
        values.put(EXERCISE_COLUMN1, username);
        values.put(EXERCISE_COLUMN2, currentDate);
        values.put(EXERCISE_COLUMN3, steps);
        values.put(EXERCISE_COLUMN4, calories);
        values.put(EXERCISE_COLUMN5, distance);
        values.put(EXERCISE_COLUMN7, exerciseTime);
        values.put(EXERCISE_COLUMN8, exerciseType);
        long routeID = db.insert(EXERCISE_TABLE, null, values);

        //todo uncomment this method when rajan get the maps working
        updateMapDB(routeID, lat, lng);
    }

    private void updateMapDB(long routeID, ArrayList<Double> lat, ArrayList<Double> lon) {
        ContentValues values;
        int i = 0;

        while (i > lat.size()) {
            values = new ContentValues();
            values.put(MAP_COLUMN2, routeID);
            values.put(MAP_COLUMN3, lat.get(i));
            values.put(MAP_COLUMN4, lon.get(i));
            db.insert(MAP_TABLE, null, values);
            i++;
        }
    }

    //----------------------------------------------------------------------------------------------

    public boolean changeUsername(String oldUsername, String newUsername, User user) {
        //todo fix this to account for extra tables
        Cursor accounts = db.rawQuery("select * from " + LOGIN_TABLE + " where " + LOGIN_COLUMN1 + " = ?;", new String[]{newUsername});
        Boolean cont = false;

        if (accounts != null) {
            if(accounts.getCount() == 0)
            {
                cont = true;
            }else {
                return false;
            }
        }
        if (cont) {
            Cursor userData = db.rawQuery("select * from " + USER_TABLE + " where " + USER_COLUMN1 + " = ?;", new String[] {oldUsername});
            userData.moveToFirst();
            ContentValues values = new ContentValues();
            values.put(LOGIN_COLUMN1, newUsername);
            values.put(LOGIN_COLUMN2, accounts.getString(1));
            db.update(LOGIN_TABLE, values, LOGIN_COLUMN1 + " = ?", new String[] {oldUsername});

            values = new ContentValues();
            values.put(USER_COLUMN1, newUsername);

            user.updateUsername(newUsername);
            saveUser(user);
        }
        return true;
    }

    //gets the date without the time value
    private Date getDateNoTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    //----------------------------------------------------------------------------------------------


    public int getDaysSteps(User user, int offset) {
        Boolean cont = false;
        int total = 0;
        Date date;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

        if (offset > 0) {
            cont =true;
        } else if (offset == 0) {
            return user.getSteps();
        }
        if (cont) {
            date = new Date(getDateNoTime().getTime() - (offset * DAY_IN_MS));
            String targetDate = format.format(date);

            //todo check the between statement is correctly bound
            Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " = ?;", new String[] {user.getUsername(), targetDate});
            hist.moveToFirst();

            int i = 0;
            while (i < hist.getCount()) {
                total = total + hist.getInt(2);
                hist.moveToNext();
                i++;
            }
        }
        return total;
    }
    public int getWeeklySteps(User user) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (7 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername()});
        hist.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < hist.getCount()) {
            total = total + hist.getInt(2);
            hist.moveToNext();
            i++;
        }
        return total;
    }
    public int getMonthlySteps(User user) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (30 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername()});
        hist.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < hist.getCount()) {
            total = total + hist.getInt(2);
            hist.moveToNext();
            i++;
        }
        return total;
    }

    public int getDaysCalories(User user, int offset) {
        Boolean cont = false;
        int total = 0;
        Date date;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

        if (offset > 0) {
            cont =true;
        } else if (offset == 0) {
            return user.getCalories();
        }
        if (cont) {
            date = new Date(getDateNoTime().getTime() - (offset * DAY_IN_MS));
            String targetDate = format.format(date);

            //todo check the between statement is correctly bound
            Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " = ?;", new String[] {user.getUsername(), targetDate});
            hist.moveToFirst();

            int i = 0;
            while (i < hist.getCount()) {
                total = total + hist.getInt(3);
                hist.moveToNext();
                i++;
            }
        }
        return total;
    }
    public int getWeeklyCalories(User user) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (7 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername()});
        hist.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < hist.getCount()) {
            total = total + hist.getInt(3);
            hist.moveToNext();
            i++;
        }
        return total;
    }
    public int getMonthlyCalories(User user) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (30 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername()});
        hist.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < hist.getCount()) {
            total = total + hist.getInt(3);
            hist.moveToNext();
            i++;
        }
        return total;
    }

    public int getDaysDistance(User user, int offset) {
        Boolean cont = false;
        int total = 0;
        Date date;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

        if (offset > 0) {
            cont =true;
        } else if (offset == 0) {
            return user.getDistance();
        }
        if (cont) {
            date = new Date(getDateNoTime().getTime() - (offset * DAY_IN_MS));
            String targetDate = format.format(date);

            //todo check the between statement is correctly bound
            Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " = ?;", new String[] {user.getUsername(), targetDate});
            hist.moveToFirst();

            int i = 0;
            while (i < hist.getCount()) {
                total = total + hist.getInt(4);
                hist.moveToNext();
                i++;
            }
        }
        return total;
    }
    public int getWeeklyDistance(User user) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (7 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername()});
        hist.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < hist.getCount()) {
            total = total + hist.getInt(4);
            hist.moveToNext();
            i++;
        }
        return total;
    }
    public int getMonthlyDistance(User user) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (30 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor hist = db.rawQuery("select * from " + HISTORIC_TABLE + " where " + HISTORIC_COLUMN1 + " = ? and " + HISTORIC_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername()});
        hist.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < hist.getCount()) {
            total = total + hist.getInt(4);
            hist.moveToNext();
            i++;
        }
        return total;
    }

    //----------------------------------------------------------------------------------------------

    public int getDayExerciseSteps(User user, String exerciseType) {
        int total = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(getDateNoTime());

        Cursor exerc = db.rawQuery("select * from " + EXERCISE_TABLE + " where " + EXERCISE_COLUMN1 + " = ? and " + EXERCISE_COLUMN2 + " = ? and " + EXERCISE_COLUMN8 + " = ?;", new String[] {user.getUsername(), currentDate, exerciseType});
        exerc.moveToFirst();

        int i = 0;
        while (i < exerc.getCount()) {
            total = total + exerc.getInt(2);
            exerc.moveToNext();
            i++;
        }

        return total;
    }
    public int getWeekExerciseSteps(User user, String exerciseType) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (7 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor exerc = db.rawQuery("select * from " + EXERCISE_TABLE + " where " + EXERCISE_COLUMN1 + " = ? and " + EXERCISE_COLUMN8 + " = ? and " + EXERCISE_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername(), exerciseType});
        exerc.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < exerc.getCount()) {
            total = total + exerc.getInt(2);
            exerc.moveToNext();
        }
        return total;
    }
    public int getMonthExerciseSteps(User user, String exerciseType) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (30 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor exerc = db.rawQuery("select * from " + EXERCISE_TABLE + " where " + EXERCISE_COLUMN1 + " = ? and " + EXERCISE_COLUMN8 + " = ? and " + EXERCISE_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername(), exerciseType});
        exerc.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < exerc.getCount()) {
            total = total + exerc.getInt(2);
            exerc.moveToNext();
        }
        return total;
    }

    public int getDayExerciseCalories(User user, String exerciseType) {
        int total = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(getDateNoTime());

        Cursor exerc = db.rawQuery("select * from " + EXERCISE_TABLE + " where " + EXERCISE_COLUMN1 + " = ? and " + EXERCISE_COLUMN2 + " = ? and " + EXERCISE_COLUMN8 + " = ?;", new String[] {user.getUsername(), currentDate, exerciseType});
        exerc.moveToFirst();

        int i = 0;
        while (i < exerc.getCount()) {
            total = total + exerc.getInt(3);
            exerc.moveToNext();
            i++;
        }

        return total;
    }
    public int getWeekExerciseCalories(User user, String exerciseType) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (7 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor exerc = db.rawQuery("select * from " + EXERCISE_TABLE + " where " + EXERCISE_COLUMN1 + " = ? and " + EXERCISE_COLUMN8 + " = ? and " + EXERCISE_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername(), exerciseType});
        exerc.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < exerc.getCount()) {
            total = total + exerc.getInt(3);
            exerc.moveToNext();
        }
        return total;
    }
    public int getMonthExerciseCalories(User user, String exerciseType) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (30 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor exerc = db.rawQuery("select * from " + EXERCISE_TABLE + " where " + EXERCISE_COLUMN1 + " = ? and " + EXERCISE_COLUMN8 + " = ? and " + EXERCISE_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername(), exerciseType});
        exerc.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < exerc.getCount()) {
            total = total + exerc.getInt(3);
            exerc.moveToNext();
        }
        return total;
    }

    public int getDayExerciseDistance(User user, String exerciseType) {
        int total = 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(getDateNoTime());

        Cursor exerc = db.rawQuery("select * from " + EXERCISE_TABLE + " where " + EXERCISE_COLUMN1 + " = ? and " + EXERCISE_COLUMN2 + " = ? and " + EXERCISE_COLUMN8 + " = ?;", new String[] {user.getUsername(), currentDate, exerciseType});
        exerc.moveToFirst();

        int i = 0;
        while (i < exerc.getCount()) {
            total = total + exerc.getInt(4);
            exerc.moveToNext();
            i++;
        }

        return total;
    }
    public int getWeekExerciseDistance(User user, String exerciseType) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (7 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor exerc = db.rawQuery("select * from " + EXERCISE_TABLE + " where " + EXERCISE_COLUMN1 + " = ? and " + EXERCISE_COLUMN8 + " = ? and " + EXERCISE_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername(), exerciseType});
        exerc.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < exerc.getCount()) {
            total = total + exerc.getInt(4);
            exerc.moveToNext();
        }
        return total;
    }
    public int getMonthExerciseDistance(User user, String exerciseType) {
        Date date = getDateNoTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);
        date = new Date(getDateNoTime().getTime() - (30 * DAY_IN_MS));
        String previousDate = format.format(date);

        //todo check the between statement is correctly bound
        Cursor exerc = db.rawQuery("select * from " + EXERCISE_TABLE + " where " + EXERCISE_COLUMN1 + " = ? and " + EXERCISE_COLUMN8 + " = ? and " + EXERCISE_COLUMN2 + " between " + currentDate + " and " + previousDate + ";", new String[] {user.getUsername(), exerciseType});
        exerc.moveToFirst();
        int total = 0;
        int i = 0;

        while (i < exerc.getCount()) {
            total = total + exerc.getInt(4);
            exerc.moveToNext();
        }
        return total;
    }

    //----------------------------------------------------------------------------------------------

    public void stepCheat(int steps, int cal, int dist, int offset) {
        Date date = new Date(getDateNoTime().getTime() - (offset * DAY_IN_MS));
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = format.format(date);

        ContentValues values = new ContentValues();
        values.put(HISTORIC_COLUMN1, "username");
        values.put(HISTORIC_COLUMN2, currentDate);
        values.put(HISTORIC_COLUMN3, steps);
        values.put(HISTORIC_COLUMN4, cal);
        values.put(HISTORIC_COLUMN5, dist);
        values.put(HISTORIC_COLUMN6, -1);
        values.put(HISTORIC_COLUMN8, 300000);
        db.insert(HISTORIC_TABLE, null, values);
    }
}