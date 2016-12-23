package com.example.nick.munny;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawer;
    private ImageView addItem;
    private static TextView balance;
    private static Button navBalance;
    private static final int BALANCE_RETURN_VAL = 0;
    private static final int ADD_ITEM_VAL = 1;
    private static final int EDIT_ITEM_VAL = 2;
    private static double prevCost = 0;
    //Button closeDialog;

    public static final String PREF_NAME = "name";
    private static final String PREF_BALANCE = "balance";

    private static MunnyDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addItem = (ImageView) findViewById(R.id.add_item);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivityForResult(intent, ADD_ITEM_VAL);
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        balance = (TextView) findViewById(R.id.balance);
        navBalance = (Button) header.findViewById(R.id.curr_balance);
        navBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditBalanceActivity.class);
                startActivityForResult(intent, BALANCE_RETURN_VAL);
            }
        });

        dbHelper = new MunnyDBHelper(this);

        //SharedPreferences pref = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        //String balanceStr = pref.getString(PREF_BALANCE, null);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String balanceStr = sharedPref.getString("balance", "");

        if (balanceStr == "") {// || balance.getText().toString().equals("")) {
            setPreferences();
        }
        else {
            balance.setText(balanceStr);
            navBalance.setText(balance.getText().toString());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        ItemListFragment fragment = new ItemListFragment();
        Bundle bundle = new Bundle();

        if (id == R.id.nav_all) {
            bundle.putInt("days", 0);
            bundle.putString("type", "");
        } else if (id == R.id.nav_1month) {
            bundle.putInt("days", 30);
            bundle.putString("type", "");
        } else if (id == R.id.nav_3months) {
            bundle.putInt("days", 90);
            bundle.putString("type", "");
        } else if (id == R.id.nav_6months) {
            bundle.putInt("days", 180);
            bundle.putString("type", "");
        } else if (id == R.id.nav_food) {
            bundle.putInt("days", 0);
            bundle.putString("type", "Food");
        } else if (id == R.id.nav_drink) {
            bundle.putInt("days", 0);
            bundle.putString("type", "Drink");
        } else if (id == R.id.nav_clothes) {
            bundle.putInt("days", 0);
            bundle.putString("type", "Clothes");
        } else if (id == R.id.nav_automotive) {
            bundle.putInt("days", 0);
            bundle.putString("type", "Automotive");
        } else if (id == R.id.nav_other) {
            bundle.putInt("days", 0);
            bundle.putString("type", "Other");
        }
        fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setPreferences() {
        Intent intent = new Intent(MainActivity.this, EditBalanceActivity.class);
        startActivityForResult(intent, BALANCE_RETURN_VAL);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (BALANCE_RETURN_VAL) : {
                if (resultCode == Activity.RESULT_OK) {
                    balance.setText(fixDigits(data.getStringExtra("newBalance")));
                    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    navBalance.setText(balance.getText().toString());
                    editor.putString("balance", balance.getText().toString());
                    editor.commit();
                    /*getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                            .edit()
                            .putString(PREF_BALANCE, "newBalance")
                            .commit();*/
                }
                break;
            }
            case (ADD_ITEM_VAL) : {
                if (resultCode == Activity.RESULT_OK) {
                    balance.setText(fixDigits(String.valueOf(Double.parseDouble(balance.getText().toString()) +
                                    Double.parseDouble(data.getStringExtra("cost")))));
                    //Toast.makeText(getApplication(), data.getStringExtra("cost"), Toast.LENGTH_SHORT).show();
                    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    navBalance.setText(balance.getText().toString());
                    editor.putString("balance", balance.getText().toString());
                    editor.commit();
                }
                break;
            }
        }
    }

    private static String fixDigits(String balanceStr) {
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
            if(balanceStr.length() - indexChar > 2) {
                balanceStr = balanceStr.substring(0, indexChar + 3);
            }
            else {
                while ((balanceStr.length() - indexChar) < 3) {
                    balanceStr += "0";
                }
            }
            if(indexChar == 0) {
                balanceStr = "0" + balanceStr;
            }
        }
        return balanceStr;
    }

    public static class ItemListFragment extends ListFragment {
        private ImageView miniAdd;
        private Cursor cursor;
        private SimpleCursorAdapter cursorAdapter;

        public ItemListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.item_listview, container, false);
            rootView.setBackgroundColor(Color.WHITE);
            //LinearLayout items = (LinearLayout) rootView.findViewById(R.id.fragment_linear_questions);

            Bundle bundle = this.getArguments();
            int days = bundle.getInt("days");
            String type = bundle.getString("type");

            ListView lv = (ListView) rootView.findViewById(android.R.id.list);
            lv.setItemsCanFocus(false);

            if(days == 0 && type.length() == 0) {
                cursor = dbHelper.getAllMunnies();
            }
            else if(days != 0){
                cursor = dbHelper.getAllMunniesWithinDays(days);
            }
            else {
                cursor = dbHelper.getAllMunniesWithType(type);
            }
            String [] columns = new String[] {
                    MunnyDBHelper.MUNNY_COLUMN_ID,
                    MunnyDBHelper.MUNNY_COLUMN_COST,
                    MunnyDBHelper.MUNNY_COLUMN_DESCRIPTION,
                    MunnyDBHelper.MUNNY_COLUMN_DATE_STRING,
                    MunnyDBHelper.MUNNY_COLUMN_IMAGE,
                    MunnyDBHelper.MUNNY_COLUMN_TYPE
            };
            int [] widgets = new int[] {
                    R.id.item_id,
                    R.id.item_cost,
                    R.id.item_desc,
                    R.id.item_date,
                    R.id.item_image
            };

            cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.munny_entry,
                    cursor, columns, widgets, 0);
            cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    if (view.getId() == R.id.item_image) {
                        view.setBackgroundColor(Color.WHITE);
                        // Get the byte array from the database.
                        byte[] iconByteArray = cursor.getBlob(columnIndex);

                        // Convert the byte array to a Bitmap beginning at the first byte and ending at the last.
                        Bitmap iconBitmap = BitmapFactory.decodeByteArray(iconByteArray, 0, iconByteArray.length);

                        // rotate
                        int w = iconBitmap.getWidth();
                        int h = iconBitmap.getHeight();

                        Matrix mtx = new Matrix();
                        mtx.postRotate(90);

                        Bitmap rotBitmap = Bitmap.createBitmap(iconBitmap, 0, 0, w, h, mtx, true);
                        // Set the bitmap.
                        ImageView iconImageView = (ImageView) view;
                        iconImageView.setImageBitmap(rotBitmap);

                        return true;
                    } else {  // Process the rest of the adapter with default settings.
                        return false;
                    }
                }
            });

            miniAdd = (ImageView) rootView.findViewById(R.id.mini_add_item);
            miniAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AddItemActivity.class);
                    startActivityForResult(intent, ADD_ITEM_VAL);
                }
            });

            lv.setAdapter(cursorAdapter);

            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                       int pos, long id) {
            Intent intent = new Intent(getActivity(), EditItemActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("id", (Integer.valueOf(((TextView) arg1.findViewById(R.id.item_id)).getText().toString())));
            intent.putExtras(bundle);
            startActivityForResult(intent, EDIT_ITEM_VAL);
            return true;
        }
    });
    //cursor.close();
    return rootView;
}

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        //Object a = getListView().getItemAtPosition(position);

        Intent intent = new Intent(getActivity(), EditItemActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("id", (Integer.valueOf(((TextView) v.findViewById(R.id.item_id)).getText().toString())));
        intent.putExtras(bundle);
        startActivityForResult(intent, EDIT_ITEM_VAL);
    }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch(requestCode) {
                case (ADD_ITEM_VAL) : {
                    if (resultCode == Activity.RESULT_OK) {
                        balance.setText(fixDigits(String.valueOf(Double.parseDouble(balance.getText().toString()) +
                                Double.parseDouble(data.getStringExtra("cost")))));
                        Toast.makeText(getActivity(), data.getStringExtra("cost"), Toast.LENGTH_SHORT).show();
                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        navBalance.setText(balance.getText().toString());
                        editor.putString("balance", balance.getText().toString());
                        editor.commit();
                        //cursor.requery();
                        //cursorAdapter.notifyDataSetChanged();
                    }
                    break;
                }
                case (EDIT_ITEM_VAL) : {
                    if (resultCode == Activity.RESULT_OK) {
                        balance.setText(fixDigits(String.valueOf(Double.parseDouble(balance.getText().toString()) +
                                Double.parseDouble(data.getStringExtra("cost")) - Double.parseDouble(data.getStringExtra("prevCost")))));
                        Toast.makeText(getActivity(), data.getStringExtra("cost"), Toast.LENGTH_SHORT).show();
                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        navBalance.setText(balance.getText().toString());
                        editor.putString("balance", balance.getText().toString());
                        editor.commit();
                        //cursor.requery();
                        //cursorAdapter.notifyDataSetChanged();
                    }
                    break;
                }
            }
        }
    }
}
