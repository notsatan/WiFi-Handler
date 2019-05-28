package com.tinyideas.wifi;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String accessWifiStatePermission;
    String changeWifiStatePermission;
    String wakeLockPermission;
    String changeNetworkStatePermission;

    static final int REQUEST_CODE = 12;
    static final int ACTIVITY_CODE = -1;

    private static Switch switch_main;
    private WifiManager wifiManager;

    private LinearLayout mainLayout;
    private CardHandler cardHandler;

    private View divider;

    private final static String DEBUG_TAG = "tempDebugTag";

    public static String SETTINGS_PACKAGE = Settings.ACTION_WIRELESS_SETTINGS;
    public static String HOTSPOT_SETTINGS_CLASS = "";

    //Intent turnOnHotspot = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
    Intent turnOnHotspot = new Intent("android.settings.panel.action.INTERNET_CONNECTIVITY");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if (getActionBar() != null)
            getActionBar().hide();

        cardHandler = new CardHandler((CardView) findViewById(R.id.mainActivity_shareData_card),
                (CardView) findViewById(R.id.mainActivity_receiveData_card));

        accessWifiStatePermission = Manifest.permission.ACCESS_WIFI_STATE;
        changeWifiStatePermission = Manifest.permission.CHANGE_WIFI_STATE;
        wakeLockPermission = Manifest.permission.WAKE_LOCK;
        changeNetworkStatePermission = Manifest.permission.CHANGE_NETWORK_STATE;

        mainLayout = (LinearLayout) findViewById(R.id.mainActivity_linearLayoutMain);
        switch_main = (Switch) findViewById(R.id.mainActivity_toggleButton);
        divider = (View) findViewById(R.id.divider);

        wifiManager = (WifiManager)
                this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        getDeviceDetails();

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.title_bar_background));

        // First, we check for the required permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(accessWifiStatePermission) == PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{accessWifiStatePermission}, REQUEST_CODE);

            if (checkSelfPermission(changeWifiStatePermission) == PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{changeWifiStatePermission}, REQUEST_CODE);

            if (checkSelfPermission(wakeLockPermission) == PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{wakeLockPermission}, REQUEST_CODE);

            if (checkSelfPermission(changeNetworkStatePermission) == PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{changeNetworkStatePermission}, REQUEST_CODE);
        }

        final Handler handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                check();
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(r);
    }

    private void getDeviceDetails() {
        String manufacturer = Build.MANUFACTURER;

        if (manufacturer.equalsIgnoreCase("Oppo"))  {
            Log.d(DEBUG_TAG, "Found an Oppo Device");
            //SETTINGS_PACKAGE = "com.coloros.wirelesssettings";
            //HOTSPOT_SETTINGS_CLASS = "com.coloros.wirelesssettings.OppoWirelessSettingsActivity";
        } else if (manufacturer.equalsIgnoreCase("Redmi") || manufacturer.equalsIgnoreCase("Xiaomi"))   {
            Log.d(DEBUG_TAG, "Found a Redmi Device\n\n\tManufacturer:" + manufacturer);

        } else if (manufacturer.equalsIgnoreCase("Vivo"))   {
            Log.d(DEBUG_TAG, "Found a Vivo Device!");
        }
    }

    public boolean check() {
        if (ApManager.isApOn(getBaseContext())) {
            cardHandler.setDataSharing();
            return true;
        } else if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED ||
                wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            cardHandler.setDataReceiving();
            return true;
        } else {
            cardHandler.normalize();
            return false;
        }
    }

    private void launchHotspotSettings() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        if (HOTSPOT_SETTINGS_CLASS.length() > 2 && SETTINGS_PACKAGE.length() > 2) {
            ComponentName componentName = new ComponentName(SETTINGS_PACKAGE, HOTSPOT_SETTINGS_CLASS);
            intent.setComponent(componentName);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else
            intent = new Intent(SETTINGS_PACKAGE);
        startActivity(intent);
    }

    public void toggle(View view) {
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED ||
                wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            wifiManager.setWifiEnabled(false);
            check();
        } else if (ApManager.isApOn(getBaseContext())) {
            try {
                ApManager.configApState(getBaseContext());
                SystemClock.sleep(500);

                if (ApManager.isApOn(getBaseContext()))
                    startActivityForResult(turnOnHotspot, REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void share_data(View view) {
        try {
            if (!ApManager.isApOn(getBaseContext())) {
                ApManager.configApState(getBaseContext());
                SystemClock.sleep(1000);

                if (!ApManager.isApOn(getBaseContext()))
                    launchHotspotSettings();
            } else {
                ApManager.configApState(getBaseContext());
                SystemClock.sleep(1000);

                if (ApManager.isApOn(getBaseContext()))
                    launchHotspotSettings();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receive_data(View view) {
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            wifiManager.setWifiEnabled(true);
        } else {
            wifiManager.setWifiEnabled(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Please grant all permissions", Toast.LENGTH_SHORT).show();
                        requestPermissions(new String[]{accessWifiStatePermission}, REQUEST_CODE);
                    }

                    if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Please grant all permissions", Toast.LENGTH_SHORT).show();
                        requestPermissions(new String[]{changeWifiStatePermission}, REQUEST_CODE);
                    }

                    if (grantResults[2] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Please grant all permissions", Toast.LENGTH_SHORT).show();
                        requestPermissions(new String[]{wakeLockPermission}, REQUEST_CODE);
                    }

                    if (grantResults[3] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Please grant all permissions", Toast.LENGTH_SHORT).show();
                        requestPermissions(new String[]{changeNetworkStatePermission}, REQUEST_CODE);
                    }
                }
        }
    }

    public class CardHandler {
        CardView shareData_card;
        CardView receiveData_card;

        TextView shareData_text;
        TextView receiveData_text;

        public CardHandler(CardView shareData_card, CardView receiveData_card) {
            this.shareData_card = shareData_card;
            this.receiveData_card = receiveData_card;

            shareData_text = findViewById(R.id.mainActivity_shareData_text);
            receiveData_text = findViewById(R.id.mainActivity_receiveData_text);
        }

        public void setDataSharing() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                receiveData_card.setElevation(3.0f);
                shareData_card.setElevation(25.0f);
            }

            shareData_card.setCardBackgroundColor(getResources().getColor(R.color.azure));

            receiveData_card.animate()
                    .alpha(0.0f)
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            receiveData_card.setVisibility(View.GONE);
                            switch_main.setChecked(true);
                            mainLayout.setGravity(Gravity.CENTER_VERTICAL);
                            super.onAnimationEnd(animation);
                        }
                    });

            divider.animate()
                    .alpha(0.0f)
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            divider.setVisibility(View.GONE);
                            super.onAnimationEnd(animation);
                        }
                    });

            shareData_text.setTypeface(null, Typeface.BOLD);
            receiveData_text.setTypeface(null, Typeface.NORMAL);
        }

        public void setDataReceiving() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                shareData_card.setElevation(3.0f);
                receiveData_card.setElevation(25.0f);
            }

            receiveData_card.setCardBackgroundColor(getResources().getColor(R.color.azure));

            shareData_card.animate()
                    .alpha(0.0f)
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            shareData_card.setVisibility(View.GONE);
                            switch_main.setChecked(true);
                            mainLayout.setGravity(Gravity.CENTER_VERTICAL);
                            super.onAnimationEnd(animation);
                        }
                    });

            divider.animate()
                    .alpha(0.0f)
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            divider.setVisibility(View.GONE);
                            super.onAnimationEnd(animation);
                        }
                    });

            receiveData_text.setTypeface(null, Typeface.BOLD);
            shareData_text.setTypeface(null, Typeface.NORMAL);
        }

        public void normalize() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                receiveData_card.setElevation(12.0f);
                shareData_card.setElevation(12.0f);
            }

            if (shareData_card.getVisibility() == View.VISIBLE) {
                mainLayout.setGravity(Gravity.TOP);
            } else if (receiveData_card.getVisibility() == View.VISIBLE) {
                mainLayout.setGravity(Gravity.BOTTOM);
            }


            shareData_card.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mainLayout.setGravity(Gravity.NO_GRAVITY);
                            shareData_card.setCardBackgroundColor(getResources().getColor(R.color.white));
                            shareData_text.setTypeface(null, Typeface.NORMAL);
                            shareData_card.setVisibility(View.VISIBLE);
                            super.onAnimationEnd(animation);
                        }
                    });

            receiveData_card.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mainLayout.setGravity(Gravity.NO_GRAVITY);
                            receiveData_card.setCardBackgroundColor(getResources().getColor(R.color.white));
                            receiveData_text.setTypeface(null, Typeface.NORMAL);
                            receiveData_card.setVisibility(View.VISIBLE);
                            super.onAnimationEnd(animation);
                        }
                    });

            divider.animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mainLayout.setGravity(Gravity.NO_GRAVITY);
                            divider.setVisibility(View.VISIBLE);
                            super.onAnimationEnd(animation);
                        }
                    });

            switch_main.setChecked(false);
            mainLayout.setBackgroundColor(Color.WHITE);
        }
    }
}
