package com.example.sklep;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //email w tle
    //zapisanie zdjec w bazie
    private DatabaseHelper dbHelper;
    private SmsManager smsManager;
    private EditText customer;
    private Spinner spinnerComputer, spinnerKeyboard, spinnerMouse, spinnerCamera;
    private SeekBar quantityValue;
    private CheckBox checkboxKeyboard, checkboxMouse, checkboxWebcam;
    private TextView textQuantityValue, textPrice, user;
    private Button buttonSaveOrder;
    private ImageView imageComputer, imageKeyboard, imageMouse, imageCamera;
    public static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.place_order);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        SharedPreferences temp = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String username = temp.getString("username", null);
        user = findViewById(R.id.user);
        user.setText(getString(R.string.loggedas) + " " + username);

        dbHelper = new DatabaseHelper(this);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);

        customer = findViewById(R.id.customer);

        spinnerComputer = findViewById(R.id.spinner_computer);
        spinnerKeyboard = findViewById(R.id.spinner_keyboard);
        spinnerMouse = findViewById(R.id.spinner_mouse);
        spinnerCamera = findViewById(R.id.spinner_camera);

        quantityValue = findViewById(R.id.quantity_value);

        checkboxKeyboard = findViewById(R.id.checkbox_keyboard);
        checkboxMouse = findViewById(R.id.checkbox_mouse);
        checkboxWebcam = findViewById(R.id.checkbox_camera);

        textQuantityValue = findViewById(R.id.text_quantity_value);

        textPrice = findViewById(R.id.text_price);
        buttonSaveOrder = findViewById(R.id.bsave_order);

        imageComputer = findViewById(R.id.image_computer);
        imageKeyboard = findViewById(R.id.image_keyboard);
        imageMouse = findViewById(R.id.image_mouse);
        imageCamera = findViewById(R.id.image_camera);

        populateSpinners();

        setupImageForSpinner(spinnerComputer, imageComputer, new int[]{
                R.drawable.desktop, R.drawable.laptop, R.drawable.gamingpc
        });

        setupImageForSpinner(spinnerKeyboard, imageKeyboard, new int[]{
                R.drawable.membrane, R.drawable.mechanical, R.drawable.wirelesskeyboard
        });

        setupImageForSpinner(spinnerMouse, imageMouse, new int[]{
                R.drawable.mouse, R.drawable.mousewireless, R.drawable.gamingmouse
        });

        setupImageForSpinner(spinnerCamera, imageCamera, new int[]{
                R.drawable.webcam, R.drawable.monitor, R.drawable.monitorgaming
        });

        restoreCart();

        quantityValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int quantity = progress + 1;
                textQuantityValue.setText(String.valueOf(quantity));
                calculatePrice();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        checkboxKeyboard.setOnCheckedChangeListener((buttonView, isChecked) -> calculatePrice());
        checkboxMouse.setOnCheckedChangeListener((buttonView, isChecked) -> calculatePrice());
        checkboxWebcam.setOnCheckedChangeListener((buttonView, isChecked) -> calculatePrice());

        buttonSaveOrder.setOnClickListener(v -> {
            saveOrder();
        });

        calculatePrice();

        addDynamicShortcuts(this);
    }


    private void setupImageForSpinner(Spinner spinner, ImageView imageView, int[] images) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                imageView.setImageResource(images[position]);
                calculatePrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void populateSpinners() {
        String[] computers = {"Desktop - 1000 zł", "Laptop - 1500 zł", "GamingPC - 2000 zł"};
        String[] keyboards = {"Membrane - 100 zł", "Mechanical - 125 zł", "Wireless - 150 zł"};
        String[] mice = {"Optical - 90 zł", "Wireless - 100 zł", "Gaming Mouse - 120 zł"};
        String[] webcams = {"Webcam - 200 zł", "Monitor 60Hz - 500 zł", "Monitor 144Hz - 700 zł"};

        setupSpinner(spinnerComputer, computers);
        setupSpinner(spinnerKeyboard, keyboards);
        setupSpinner(spinnerMouse, mice);
        setupSpinner(spinnerCamera, webcams);
    }

    private void setupSpinner(Spinner spinner, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void calculatePrice() {
        int price = 0;

        String selectedComputer = spinnerComputer.getSelectedItem().toString();
        int quantity = quantityValue.getProgress() + 1;
        price += Integer.parseInt(selectedComputer.split(" ")[selectedComputer.split(" ").length - 2]) * quantity;

        if (checkboxKeyboard.isChecked()) {
            String selectedKeyboard = spinnerKeyboard.getSelectedItem().toString();
            price += Integer.parseInt(selectedKeyboard.split(" ")[selectedKeyboard.split(" ").length - 2]);
        }

        if (checkboxMouse.isChecked()) {
            String selectedMouse = spinnerMouse.getSelectedItem().toString();
            price += Integer.parseInt(selectedMouse.split(" ")[selectedMouse.split(" ").length - 2]);
        }

        if (checkboxWebcam.isChecked()) {
            String selectedWebcam = spinnerCamera.getSelectedItem().toString();
            price += Integer.parseInt(selectedWebcam.split(" ")[selectedWebcam.split(" ").length - 2]);
        }
        textPrice.setText("Razem: " + price + " zł");
    }

    private void saveOrder() {
        String selectedComputer = spinnerComputer.getSelectedItem().toString();
        String selectedKeyboard = checkboxKeyboard.isChecked() ? spinnerKeyboard.getSelectedItem().toString() : null;
        String selectedMouse = checkboxMouse.isChecked() ? spinnerMouse.getSelectedItem().toString() : null;
        String selectedWebcam = checkboxWebcam.isChecked() ? spinnerCamera.getSelectedItem().toString() : null;

        if (customer.getText().toString().isEmpty()) {
            customer.setError(getString(R.string.empty_field));
            customer.requestFocus();
            return;
        }

        int quantity = quantityValue.getProgress() + 1;

        String[] temp = textPrice.getText().toString().split(" ");
        int totalPrice = Integer.parseInt(temp[1]);

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_CUSTOMER, customer.getText().toString());
        values.put(DatabaseHelper.COLUMN_COMPUTER, selectedComputer);
        values.put(DatabaseHelper.COLUMN_KEYBOARD, selectedKeyboard);
        values.put(DatabaseHelper.COLUMN_MOUSE, selectedMouse);
        values.put(DatabaseHelper.COLUMN_ACCESSORY, selectedWebcam);
        values.put(DatabaseHelper.COLUMN_QUANTITY, quantity);
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_TOTAL_PRICE, totalPrice);

        db.insert(DatabaseHelper.TABLE_ORDERS, null, values);
        db.close();

        Toast.makeText(this, "Order saved successfully", Toast.LENGTH_SHORT).show();

        reset();
    }

    private void shareCart() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, messageCreator());
        Intent chooser = Intent.createChooser(shareIntent, "Share Cart");
        if (shareIntent.resolveActivity(this.getPackageManager()) != null) {
            this.startActivity(chooser);
        } else {
            Toast.makeText(this, "No app detected capable of sharing cart.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSmsCart() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode("123456789")));
        intent.putExtra("sms_body", messageCreator());
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "SMS FAILED", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendEmail() {
        String emailAddress = "test@gmail.com";
        String text = messageCreator();

        if (!emailAddress.isEmpty() && !text.isEmpty()) {
            Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
            mailIntent.setData(Uri.parse("mailto:" + emailAddress));
            mailIntent.putExtra(Intent.EXTRA_SUBJECT, "New Message");
            mailIntent.putExtra(Intent.EXTRA_TEXT, text);
            try {
                startActivity(Intent.createChooser(mailIntent, "Send Email"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No email client installed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter an email address and message", Toast.LENGTH_SHORT).show();
        }
    }

    public void addDynamicShortcuts(Context context) {
        ShortcutManager shortcutManager = (ShortcutManager) getSystemService(Context.SHORTCUT_SERVICE);
        List<ShortcutInfo> shortcutInfoList = new ArrayList<>();

        ShortcutInfo shortcutZlozZamowienia = new ShortcutInfo.Builder(context, "zzamowienia_shortcut")
                .setShortLabel(getString(R.string.place_order))
                .setLongLabel(getString(R.string.place_order))
                .setIcon(Icon.createWithResource(context, R.drawable.ic_zloz_zamowienia_foreground))
                .setIntent(new Intent(Intent.ACTION_VIEW, null, context, MainActivity.class))
                .build();

        ShortcutInfo shortcutZamowienia = new ShortcutInfo.Builder(context, "zamowienia_shortcut")
                .setShortLabel(getString(R.string.show_order_list))
                .setLongLabel(getString(R.string.show_order_list))
                .setIcon(Icon.createWithResource(context, R.drawable.ic_zamowienia_foreground))
                .setIntent(new Intent(Intent.ACTION_VIEW, null, context, OrdersActivity.class))
                .build();

        shortcutInfoList.add(shortcutZlozZamowienia);
        shortcutInfoList.add(shortcutZamowienia);

        shortcutManager.setDynamicShortcuts(shortcutInfoList);
    }

    private void saveCart() {
        SharedPreferences userPrefs = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String username = userPrefs.getString("username", null);

        SharedPreferences sharedPreferences = getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(username + "_customer", customer.getText().toString());
        editor.putString(username + "_selectedComputer", spinnerComputer.getSelectedItem().toString());
        editor.putString(username + "_selectedKeyboard", checkboxKeyboard.isChecked() ? spinnerKeyboard.getSelectedItem().toString() : null);
        editor.putString(username + "_selectedMouse", checkboxMouse.isChecked() ? spinnerMouse.getSelectedItem().toString() : null);
        editor.putString(username + "_selectedWebcam", checkboxWebcam.isChecked() ? spinnerCamera.getSelectedItem().toString() : null);
        editor.putInt(username + "_quantity", quantityValue.getProgress() + 1);

        String[] temp = textPrice.getText().toString().split(" ");
        int totalPrice = Integer.parseInt(temp[1]);
        editor.putInt(username + "_totalPrice", totalPrice);

        editor.apply();
    }


    private void restoreCart() {
        SharedPreferences userPrefs = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String username = userPrefs.getString("username", null);

        SharedPreferences sharedPreferences = getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);

        String customerString = sharedPreferences.getString(username + "_customer", "");
        String selectedComputer = sharedPreferences.getString(username + "_selectedComputer", "Desktop - 1000 zł");
        String selectedKeyboard = sharedPreferences.getString(username + "_selectedKeyboard", null);
        String selectedMouse = sharedPreferences.getString(username + "_selectedMouse", null);
        String selectedWebcam = sharedPreferences.getString(username + "_selectedWebcam", null);
        int quantity = sharedPreferences.getInt(username + "_quantity", 1);
        int totalPrice = sharedPreferences.getInt(username + "_totalPrice", 1000);

        customer.setText(customerString);
        setSpinnerSelection(spinnerComputer, selectedComputer);
        setSpinnerSelection(spinnerKeyboard, selectedKeyboard);
        setSpinnerSelection(spinnerMouse, selectedMouse);
        setSpinnerSelection(spinnerCamera, selectedWebcam);

        quantityValue.setProgress(quantity - 1);
        textQuantityValue.setText(String.valueOf(quantity));

        textPrice.setText(getString(R.string.total) + " " + totalPrice + " zł");

        checkboxKeyboard.setChecked(selectedKeyboard != null);
        checkboxMouse.setChecked(selectedMouse != null);
        checkboxWebcam.setChecked(selectedWebcam != null);
    }


    private void sendWithSmsManager() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            String destinationAddress = "605679136";
            String text = messageCreator();
            if (!destinationAddress.equals("") && !text.equals("")) {
                smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(destinationAddress, null, text, null, null);
                Toast.makeText(MainActivity.this, "SMS send with smsManager", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No Permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.orders_list) {
            startActivity(new Intent(this, OrdersActivity.class));
            return true;

        } else if (item.getItemId() == R.id.send_sms) {
            Toast.makeText(this, "Sending SMS", Toast.LENGTH_SHORT).show();
            sendWithSmsManager();
            return true;
        } else if (item.getItemId() == R.id.share_cart) {
            Toast.makeText(this, "Sharing your Cart", Toast.LENGTH_SHORT).show();
            shareCart();
            return true;
        } else if (item.getItemId() == R.id.autor) {
            Toast.makeText(this, "Autor: Jarosław Matyjasik", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.save_cart) {
            saveCart();
            Toast.makeText(this, "Cart saved", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.send_email) {
            sendEmail();
            Toast.makeText(this, "Sending E-mail", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.logout) {
            logOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    private void logOut() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
        Toast.makeText(this, "User logged out", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void reset() {
        customer.setText("");
        checkboxKeyboard.setChecked(false);
        checkboxMouse.setChecked(false);
        checkboxWebcam.setChecked(false);
        spinnerComputer.setSelection(0);
        spinnerKeyboard.setSelection(0);
        spinnerMouse.setSelection(0);
        spinnerCamera.setSelection(0);
        quantityValue.setProgress(0);
        textPrice.setText("Razem: 1000");
        SharedPreferences sharedPreferences = getSharedPreferences("CartPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private String messageCreator() {
        String selectedComputer = spinnerComputer.getSelectedItem().toString();
        String selectedKeyboard = checkboxKeyboard.isChecked() ? spinnerKeyboard.getSelectedItem().toString() : null;
        String selectedMouse = checkboxMouse.isChecked() ? spinnerMouse.getSelectedItem().toString() : null;
        String selectedWebcam = checkboxWebcam.isChecked() ? spinnerCamera.getSelectedItem().toString() : null;

        int quantity = quantityValue.getProgress() + 1;

        String[] temp = textPrice.getText().toString().split(" ");
        int totalPrice = Integer.parseInt(temp[1]);
        String message = "";
        message = selectedComputer + " " + getString(R.string.quantity) + quantity + " ";

        if (selectedKeyboard != null) {
            message += selectedKeyboard + " ";
        }
        if (selectedMouse != null) {
            message += selectedMouse + " ";
        }
        if (selectedWebcam != null) {
            message += selectedWebcam + " ";
        }
        message += getString(R.string.total) + totalPrice;

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        return message;
    }

    private void setSpinnerSelection(Spinner spinner, String selectedItem) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(selectedItem)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}