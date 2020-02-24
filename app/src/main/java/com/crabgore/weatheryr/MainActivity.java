package com.crabgore.weatheryr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.crabgore.weatheryr.activities.BaseActivity;
import com.crabgore.weatheryr.activities.SettingsActivity;
import com.crabgore.weatheryr.adapters.TabAdapter;
import com.crabgore.weatheryr.database.CitiesTable;
import com.crabgore.weatheryr.database.DatabaseHelper;
import com.crabgore.weatheryr.fragments.FutureFragment;
import com.crabgore.weatheryr.fragments.ShareFragment;
import com.crabgore.weatheryr.fragments.WeatherFragment;
import com.crabgore.weatheryr.fragments.WebViewFragment;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int SETTINGS = 1;
    public static SQLiteDatabase database;
    public static Location mLocation;
    private Toolbar toolbar;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private ArrayList<String> titles = new ArrayList<>();
    SharedPreferences.Editor editor;

    private LocationCallback locationCallback;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        editor = mSettings.edit();

        checkPermissions();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean openActivityOnce = true;
        boolean openDialogOnce = true;
        if (requestCode == 42) {
            for (int i = 0; i < grantResults.length; i++) {
                String permission = permissions[i];

                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {
                        editor.putBoolean(PERMISSION, false);
                        editor.apply();
                        fragments.clear();
                        titles.clear();
                        start();
                    } else {
                        if (openDialogOnce) {
                            alertView();
                            openDialogOnce = false;
                        }
                    }
                } else if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (openActivityOnce) {
                        editor.putBoolean(PERMISSION, true);
                        editor.apply();
                        fragments.clear();
                        titles.clear();
                        initLocation();
                        openActivityOnce = false;
                    }
                }
            }
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

        if (id == R.id.myLoc) {
            if (mSettings.getBoolean(PERMISSION, true)) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                getMyLoc(location);
                            } else
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_location), Toast.LENGTH_SHORT).show();
                        });
            } else checkPermissions();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            WebViewFragment details = new WebViewFragment();
            navBarMenu(details);
        } else if (id == R.id.nav_share) {
            ShareFragment details = new ShareFragment();
            navBarMenu(details);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS);
        } else if (id == R.id.nav_like) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.crabgore.weatheryr"));
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) recreate();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        OnBackPressedListener backPressedListener = null;
        for (Fragment fragment : fm.getFragments()) {
            if (fragment instanceof OnBackPressedListener) {
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

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            requestPermissions(permissions, 42);
        } else {
            initLocation();
        }
    }

    private void setCallBack() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20 * 1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        mLocation = location;
                    }
                }
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void start() {
        initToolbar();
        initDB();
        initSideMenu(toolbar);
        settingsCheck();
        initLists();
        checkOrientation();
    }

    @SuppressLint("MissingPermission")
    private void initLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                mLocation = location;
                start();
            } else {
                Toast.makeText(this, getResources().getString(R.string.no_location),
                        Toast.LENGTH_SHORT).show();
                setCallBack();
                start();
            }
        });
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

    private void initLists() {
        WeatherFragment weatherFragment = new WeatherFragment();
        FutureFragment futureFragment = new FutureFragment();

        fragments.add(weatherFragment);
        fragments.add(futureFragment);
        titles.add(getResources().getString(R.string.today));
        titles.add(getResources().getString(R.string.forecast));
    }

    private void settingsCheck() {
        String s = mSettings.getString(CHOSEN_CITY, "");

        if (s.equals("")) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS);
        }
    }

    private void checkOrientation() {
        boolean isLand = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (isLand) initFragments();
        else initPager();
    }

    private void initFragments() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.weather, new WeatherFragment())
                .add(R.id.forecast, new FutureFragment())
                .commit();
    }

    private void initPager() {
        TabAdapter myTabAdapter = new TabAdapter(getSupportFragmentManager(), fragments, titles);

        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(myTabAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(getWindow().getStatusBarColor());

    }

    private void alertView() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.Theme_MaterialComponents_DayNight_Dialog_Alert);

        dialog.setTitle(getResources().getString(R.string.denied))
                .setMessage(getResources().getString(R.string.denied_message))
                .setNegativeButton(getResources().getString(R.string.sure), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    editor.putBoolean(PERMISSION, false);
                    editor.apply();
                    fragments.clear();
                    titles.clear();
                    start();
                })
                .setPositiveButton(getResources().getString(R.string.retry), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    checkPermissions();
                }).show();
    }

    private void getMyLoc(Location location) {
        String currentLocation = getAddressByLoc(location);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(CHOSEN_CITY, currentLocation);

        if (!SettingsActivity.cities.contains(currentLocation)) {
            CitiesTable.addNewCityWeather(currentLocation, database);
            editor.putInt(CHOSEN_CITY_ID, SettingsActivity.cities.size());
        } else editor.putInt(CHOSEN_CITY_ID, SettingsActivity.cities.indexOf(currentLocation));

        editor.apply();

        SettingsActivity.saveCityInFile(this, currentLocation);

        checkOrientation();
    }

    private String getAddressByLoc(Location loc) {
        final Geocoder geo = new Geocoder(this, Locale.ENGLISH);
        List<Address> list = null;
        if (loc != null) {
            try {
                list = geo.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            Toast.makeText(this, getResources().getString(R.string.no_location), Toast.LENGTH_SHORT).show();

        assert list != null;
        String cityName = list.get(0).getLocality();
        String countryCode = list.get(0).getCountryCode();

        return cityName + "," + countryCode;
    }

    private void navBarMenu(Fragment details) {
        String backStateName = details.getClass().getName();
        details.setArguments(getIntent().getExtras());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, details)
                .addToBackStack(backStateName)
                .commit();
    }
}
