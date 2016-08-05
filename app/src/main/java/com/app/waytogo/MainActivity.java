package com.app.waytogo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.app.waytogo.fragment.CardPaymentFragment;
import com.app.waytogo.fragment.DriverFragment;
import com.app.waytogo.fragment.HomeFragment;
import com.app.waytogo.fragment.SettingsFragment;
import com.app.waytogo.support.UrlGenerator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {

    private Toolbar mToolbar;
    private ConnectivityManager cm;
    private FragmentDrawer drawerFragment;
    private int mPage = 0;
    private SharedPreferences mySharedPreferences, myDriverSharedPreferences, myFoodSharedPreferences;
    private Editor editor, editorDriver, editorFood;
    private String USER = "user_pref";
    private String DRIVER = "driver_pref";
    private String FOOD = "food_pref";
    private int isDriverMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            Bundle b = getIntent().getExtras();
            mPage = b.getInt("page");
        } catch (NullPointerException e) {
            mPage = 0;
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mySharedPreferences = getSharedPreferences(USER, MODE_PRIVATE);
        editor = mySharedPreferences.edit();
        myDriverSharedPreferences = getSharedPreferences(DRIVER, MODE_PRIVATE);
        editorDriver = myDriverSharedPreferences.edit();
        myFoodSharedPreferences = getSharedPreferences(FOOD, MODE_PRIVATE);
        editorFood = myFoodSharedPreferences.edit();

        drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // display the first navigation drawer view on app launch
        displayView(mPage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        supportInvalidateOptionsMenu();
        int id = item.getItemId();

        if (id == R.id.action_gift) {
            Intent i_share = new Intent(Intent.ACTION_SEND);
            i_share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.strShare));
            i_share.setType("text/plain");
            startActivity(Intent.createChooser(i_share, "Invite Via.."));
            return true;
        }

        if (id == R.id.action_steeringwheel_inactive) {
            isDriverMode = 1;
            if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
                switchToDriverMode();
            } else {
                networkConnectivity();
            }
            return true;
        }

        if (id == R.id.action_steeringwheel_active) {
            isDriverMode = 0;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        //String strIsDriver = mySharedPreferences.getString("strIsDriver", "");

        String strIsDriver = mySharedPreferences.getString("strIsDriver", "");

        if ((strIsDriver.equals("") == true) || (strIsDriver.equals("pending") == true)) {
            menu.findItem(R.id.action_gift).setVisible(true);
            menu.findItem(R.id.action_steeringwheel_inactive).setVisible(false);
            menu.findItem(R.id.action_steeringwheel_active).setVisible(false);
        }
        if ((strIsDriver.equals("driver") == true) && (isDriverMode == 0)) {
            menu.findItem(R.id.action_gift).setVisible(false);
            menu.findItem(R.id.action_steeringwheel_inactive).setVisible(true);
            menu.findItem(R.id.action_steeringwheel_active).setVisible(false);
        }
        if ((strIsDriver.equals("driver") == true) && (isDriverMode == 1)) {
            menu.findItem(R.id.action_gift).setVisible(false);
            menu.findItem(R.id.action_steeringwheel_inactive).setVisible(false);
            menu.findItem(R.id.action_steeringwheel_active).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                title = getString(R.string.title_home);
                break;
            case 1:
                fragment = new CardPaymentFragment();
                title = getString(R.string.title_home);
                break;
            case 2:
                String strIsDriver = mySharedPreferences.getString("strIsDriver", "");
                if (strIsDriver.equals("") == true) {
                    fragment = new DriverFragment();
                    title = getString(R.string.title_home);
                } else if (strIsDriver.equals("pending") == true) {
                    String strDriveAcitve = getResources().getString(R.string.strdriveralert);
                    showDriverAlert(strDriveAcitve);
                } else if (strIsDriver.equals("driver") == true) {
                    String strDriveAcitve = getResources().getString(R.string.strdriverconfirm);
                    showDriverAlert(strDriveAcitve);
                }
                break;
            case 3:
                Intent i_share = new Intent(Intent.ACTION_SEND);
                i_share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.strShare));
                i_share.setType("text/plain");
                startActivity(Intent.createChooser(i_share, "Invite Via.."));
                break;
            case 4:
                fragment = new SettingsFragment();
                title = getString(R.string.title_home);
                break;
            case 5:
                confirmLogout();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }

    public void showDriverAlert(String strMessage) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(strMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.strgotit,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    public void switchToDriverMode() {
        // TODO Auto-generated method stub
        AsyncHttpClient client = new AsyncHttpClient();
        // Http Request Params Object
        RequestParams params = new RequestParams();

        // Show ProgressBar
        final ProgressDialog pdialog = new ProgressDialog(MainActivity.this);
        pdialog.setMessage("Switching to Driver mode...");
        pdialog.setCancelable(false);
        pdialog.show();
        // Make Http call to getusers.php

        String url = null;
        url = UrlGenerator.updateDriverStatus();

        params.add("iUserID", mySharedPreferences.getString("strUserId", ""));
        params.add("status", "online");

        client.post(url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(String response) {
                // Hide ProgressBar
                pdialog.hide();

                JSONObject jObject;
                try {
                    jObject = new JSONObject(response);

                    String strStatus = jObject.optString("status");
                    String strMessage = jObject.optString("message");

                    if (strStatus.equals("0") == true) {
                        //editor.putString("strEmail", strMessage);
                        Intent i_driver = new Intent(MainActivity.this, DriverActivity.class);
                        startActivity(i_driver);
                        MainActivity.this.finish();
                    } else {
                        Toast.makeText(MainActivity.this, strMessage, Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // When error occured
            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                // TODO Auto-generated method stub
                // Hide ProgressBar
                pdialog.hide();
                if (statusCode == 404) {
                    //Toast.makeText(MainActivity.this, "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    //Toast.makeText(MainActivity.this, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                } else {
                    networkConnectivity();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();
        }
    }


    private void networkConnectivity() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.title)
                .setMessage(R.string.message)
                .setCancelable(false)
                .setPositiveButton(R.string.strok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(R.string.strlogoutMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.stryes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //editor.putString("isLogin", "0");
                        editor.clear();
                        editorDriver.clear();
                        editorFood.clear();
                        editor.commit();
                        editorDriver.commit();
                        editorFood.commit();

                        Intent i_logout = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i_logout);
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.strno, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}