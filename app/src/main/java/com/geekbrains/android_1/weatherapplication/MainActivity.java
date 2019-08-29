package com.geekbrains.android_1.weatherapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.Activities.SettingsActivity;
import com.geekbrains.android_1.weatherapplication.Fragments.AboutFragment;
import com.geekbrains.android_1.weatherapplication.Fragments.FutureFragment;
import com.geekbrains.android_1.weatherapplication.Fragments.ShareFragment;
import com.geekbrains.android_1.weatherapplication.Fragments.WebViewFragment;
import com.geekbrains.android_1.weatherapplication.database.CitiesTable;
import com.geekbrains.android_1.weatherapplication.database.DatabaseHelper;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int SETTINGS = 1;
    public static SQLiteDatabase database;
    public static Location location;
    public LocationManager locationManager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLocation();

        initToolbar();

        initDB();

        initSideMenu(toolbar);

        settingsCheck();
    }

    private void initLocation() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            requestPermissions(permissions, 42);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        assert locationManager != null;
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null){
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null){
                location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
        }
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    private void initDB() {
        database = new DatabaseHelper(getApplicationContext()).getWritableDatabase();
    }

    private void initSideMenu(Toolbar toolbar) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void settingsCheck(){
        String s = mSettings.getString(CHOSEN_CITY, "");

        if (s.equals("")){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS);
        }
        if (id == R.id.action_info){
            WebViewFragment details = new WebViewFragment();
            navBarMenu(details);
        }
        if (id == R.id.myLoc){
            getMyLoc();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getMyLoc() {
        String currentLocation = getAddressByLoc(location);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(CHOSEN_CITY, currentLocation);
        if (!SettingsActivity.cities.contains(currentLocation)) {
            CitiesTable.addNewCityWeather(currentLocation, database);
            editor.putInt(CHOSEN_CITY_ID, SettingsActivity.cities.size());
        } else editor.putInt(CHOSEN_CITY_ID, SettingsActivity.cities.indexOf(currentLocation));
        editor.apply();
        recreate();
    }

    private String getAddressByLoc(Location loc) {
        final Geocoder geo = new Geocoder(this, Locale.ENGLISH);
        List<Address> list = null;
        try {
            list = geo.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert list != null;
        String cityName = list.get(0).getLocality();
        String countryCode = list.get(0).getCountryCode();

        return cityName + "," + countryCode;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_future) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                FutureFragment details = new FutureFragment();
                navBarMenu(details);
            }
        } else if (id == R.id.nav_share) {
            ShareFragment details = new ShareFragment();
            navBarMenu(details);
        } else if (id == R.id.nav_about) {
            AboutFragment details = new AboutFragment();
            navBarMenu(details);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        recreate();
    }

    private void navBarMenu(Fragment details){
        String backStateName = details.getClass().getName();
        details.setArguments(getIntent().getExtras());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main, details)
                    .addToBackStack(backStateName)
                    .commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        OnBackPressedListener backPressedListener = null;
        for (Fragment fragment: fm.getFragments()) {
            if (fragment instanceof  OnBackPressedListener) {
                backPressedListener = (OnBackPressedListener) fragment;
                break;
            }
        }

        if (backPressedListener != null) {
            backPressedListener.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
