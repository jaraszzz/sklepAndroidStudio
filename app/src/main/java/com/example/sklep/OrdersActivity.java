package com.example.sklep;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {
    private ListView listViewOrders;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Zamówienia");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        dbHelper = new DatabaseHelper(this);
        listViewOrders = findViewById(R.id.list_view_orders);
        loadOrders();
    }

    private void loadOrders() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_ORDERS, null);

        List<String> orders = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String orderDetails = "Customer: " + cursor.getString(1) +
                        "\nComputer: " + cursor.getString(2) +
                        "\nQuantity: " + cursor.getInt(3) +
                        "\nKeyboard: " + cursor.getString(4) +
                        "\nMouse: " + cursor.getString(5) +
                        "\nAccesory: " + cursor.getString(6) +
                        "\nOrder Date: " + cursor.getString(7) +
                        "\nTotal Price: " + cursor.getInt(8);
                orders.add(orderDetails);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orders);
        listViewOrders.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
