package com.example.nick.munny;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditItemActivity extends Activity {
    private Button mPickDate;
    private ImageView mImage;
    private int mYear;
    private int mMonth;
    private int mDay;
    private int typePos = 0;
    private EditText mPrice, mDesc;
    private Button mCancel, mDone;
    private Spinner mType;
    private ImageView mDelete;
    private Button sign;

    private String currType = "";
    private byte[] imageByte = null;
    private String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private String[] types = {"Select...", "Food", "Drink", "Clothes", "Automotive", "Other"};
    private static final int DATE_DIALOG_ID = 0;

    private static final int TAKE_PICTURE_REQUEST_B = 100;

    private Bitmap mCameraBitmap;

    private MunnyDBHelper dbHelper;
    private String date;
    private Cursor cursor;
    private int id;
    private String type = "";
    private String prevCost = "";


    Map<String, Integer> map = new HashMap<String,Integer>();
    private int[] images = {R.drawable.ic_food, R.drawable.ic_drink, R.drawable.ic_clothes,
            R.drawable.ic_car, R.drawable.ic_other};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_dialog);

        id = getIntent().getExtras().getInt("id");

        // capture our View elements
        mPickDate = (Button) findViewById(R.id.editDate);
        mImage = (ImageView) findViewById(R.id.edit_picture);
        mCancel = (Button) findViewById(R.id.cancel_edit_item);
        mDone = (Button) findViewById(R.id.done_edit_item);
        mPrice = (EditText) findViewById(R.id.edit_price);
        mDesc = (EditText) findViewById(R.id.edit_description);
        mType = (Spinner) findViewById(R.id.edit_type);
        mDelete = (ImageView) findViewById(R.id.delete_item);
        sign = (Button) findViewById(R.id.sign2);

        dbHelper = new MunnyDBHelper(this);
        cursor = dbHelper.getMunny(id);

        map.put("FOOD", R.drawable.ic_food);
        map.put("DRINK", R.drawable.ic_drink);
        map.put("CLOTHES", R.drawable.ic_clothes);
        map.put("AUTOMOTIVE", R.drawable.ic_car);
        map.put("OTHER", R.drawable.ic_other);

        // add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sign.getText().equals("-")) {
                    sign.setText("+");
                }
                else {
                    sign.setText("-");
                }
            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImageCapture();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean fieldsFilled = true;
                //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                //writeToFile(sdf.format(new Date()));
                currType = mType.getSelectedItem().toString();

                if(mDesc.getText().toString().length() == 0) {
                    mDesc.setHintTextColor(Color.RED);
                    fieldsFilled = false;
                }
                if(((TextView)mType.getChildAt(0)).getText().equals("Select...")) {
                    ((TextView)mType.getChildAt(0)).setTextColor(Color.RED);
                    fieldsFilled = false;
                }
                if(mPrice.getText().toString().length() == 0) {
                    mPrice.setHintTextColor(Color.RED);
                    Toast.makeText(getApplicationContext(), mPrice.getText().toString(), Toast.LENGTH_SHORT);
                    fieldsFilled = false;
                }
                if(imageByte == null && fieldsFilled) {
                    Drawable d = getResources().getDrawable(map.get(currType.toUpperCase()));
                    Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    imageByte = stream.toByteArray();
                }
                if(fieldsFilled) {
                    if (dbHelper.updateMunny(id,fixDigits(mPrice.getText().toString()),
                            mDesc.getText().toString(), date, mPickDate.getText().toString(),
                            currType, imageByte)) {
                        Toast.makeText(getApplicationContext(), "Activity Updated", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        mPrice.setText(fixDigits(mPrice.getText().toString()));
                        resultIntent.putExtra("cost", mPrice.getText().toString());
                        resultIntent.putExtra("prevCost", prevCost);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please fill everything in.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ArrayAdapter<String>adapter = new ArrayAdapter<String>(EditItemActivity.this,
                android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                dbHelper.deleteMunny(id);
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //Do your No progress
                                break;
                        }
                    }
                };
                AlertDialog.Builder ab = new AlertDialog.Builder(new ContextThemeWrapper(EditItemActivity.this, R.style.myDialog));
                ab.setMessage("Are you sure to delete?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        if (cursor.moveToFirst()) {

            String cost = cursor.getString(cursor.getColumnIndex("cost"));
            prevCost = cost;
            if(!cost.contains("-")) {
                sign.setText("+");
            }
            else {
                cost = cost.substring(1,cost.length());
            }
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String date_db = cursor.getString(cursor.getColumnIndex("date"));
            String date_str = cursor.getString(cursor.getColumnIndex("date_string"));
            type = cursor.getString(cursor.getColumnIndex("type"));
            types[0] = type;
            byte[] image = cursor.getBlob(cursor.getColumnIndex("image"));
            mPrice.setText(cost);
            mDesc.setText(description);
            imageByte = image;
            Bitmap iconBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

            // rotate
            int w = iconBitmap.getWidth();
            int h = iconBitmap.getHeight();

            Matrix mtx = new Matrix();
            mtx.postRotate(90);

            Bitmap rotBitmap = Bitmap.createBitmap(iconBitmap, 0, 0, w, h, mtx, true);
            // Set the bitmap.
            mImage.setImageBitmap(rotBitmap);

            //((TextView)mType.getChildAt(0)).setText(type);

            // get the current date
            mYear = Integer.parseInt(date_db.substring(0,4));
            mMonth = Integer.parseInt(date_db.substring(5,7));
            mDay = Integer.parseInt(date_db.substring(8,10));
        }

        // Apply the adapter to the spinner
        mType.setAdapter(adapter);

        mType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                typePos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        updateDate();
        cursor.close();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }

    private void updateDate() {
        String dateNum = "" + (mYear * 10000 + (mMonth) * 100 + mDay);
        date = dateNum.substring(0,4) + "-" + dateNum.substring(4, 6) + "-" +
                dateNum.substring(6, 8);
        mPickDate.setText(
                new StringBuilder()
                        .append(months[mMonth-1]).append(" ")
                        .append(mDay).append(", ")
                        .append(mYear).append(" "));
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDate();
                }
            };

    private String fixDigits(String balanceStr) {
        if(!balanceStr.contains(".")) {
            if(balanceStr.equals("")) {
                balanceStr = "0.00";
            }
            else {
                balanceStr = (balanceStr + ".00");
            }
        }
        else {
            int indexChar = balanceStr.indexOf(".");
            if(indexChar == 0) {
                balanceStr = "0" + balanceStr;
            }
            if(balanceStr.length() - indexChar > 2) {
                balanceStr = balanceStr.substring(0, indexChar + 3);
            }
            else {
                while ((balanceStr.length() - indexChar) < 3) {
                    balanceStr += "0";
                }
            }
        }
        if(sign.getText().equals("-") && !balanceStr.contains("-")) {
            balanceStr = "-"+balanceStr;
        }
        return balanceStr;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST_B) {
            if (resultCode == RESULT_OK) {
                // Recycle the previous bitmap.
                if (mCameraBitmap != null) {
                    mCameraBitmap.recycle();
                    mCameraBitmap = null;
                }
                Bundle extras = data.getExtras();
                imageByte = extras.getByteArray(CameraActivity.EXTRA_CAMERA_DATA);
                if (imageByte != null) {
                    Bitmap tmpPic;
                    tmpPic = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                    int w = tmpPic.getWidth();
                    int h = tmpPic.getHeight();

                    Matrix mtx = new Matrix();
                    mtx.postRotate(90);

                    mCameraBitmap = Bitmap.createBitmap(tmpPic, 0, 0, w, h, mtx, true);
                    mImage.setImageBitmap(mCameraBitmap);
                }
            } else {
                mCameraBitmap = null;
            }
        }
    }

    private void startImageCapture() {
        startActivityForResult(new Intent(EditItemActivity.this, CameraActivity.class), TAKE_PICTURE_REQUEST_B);
    }
}