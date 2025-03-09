package com.example.sklep;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "sklep_db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CUSTOMER = "customer";
    public static final String COLUMN_COMPUTER = "computer";
    public static final String COLUMN_KEYBOARD = "keyboard";
    public static final String COLUMN_MOUSE = "mouse";
    public static final String COLUMN_ACCESSORY = "accessory";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_DATE = "order_date";
    public static final String COLUMN_TOTAL_PRICE = "total_price";
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_LOGIN = "login";
    private static final String COLUMN_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CUSTOMER + " TEXT, "
                + COLUMN_COMPUTER + " TEXT, "
                + COLUMN_QUANTITY + " INTEGER, "
                + COLUMN_KEYBOARD + " TEXT, "
                + COLUMN_MOUSE + " TEXT, "
                + COLUMN_ACCESSORY + " TEXT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_TOTAL_PRICE + " INTEGER)";
        db.execSQL(CREATE_ORDERS_TABLE);

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_LOGIN + " TEXT, "
                + COLUMN_PASSWORD + " TEXT)";

        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean registerUser(String email, String login, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_LOGIN, login);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String login, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_LOGIN + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{login, password});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }
}


