package com.example.sklep;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        onCreate(db);
    }
}
