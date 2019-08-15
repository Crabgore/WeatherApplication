package com.geekbrains.android_1.weatherapplication;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.Activities.Settings;
import com.geekbrains.android_1.weatherapplication.Fragments.AboutFragment;
import com.geekbrains.android_1.weatherapplication.Fragments.FutureFragment;
import com.geekbrains.android_1.weatherapplication.Fragments.ShareFragment;
import com.geekbrains.android_1.weatherapplication.Fragments.WebViewFragment;
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

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initSideMenu(toolbar);

        settingsCheck();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivityForResult(intent, SETTINGS);
        }
        if (id == R.id.action_info){
            WebViewFragment details = new WebViewFragment();
            navBarMenu(details);


//            String url = "https://ru.wikipedia.org/wiki/" + mSettings.getString(CHOSEN_CITY, "");
//            Uri uri = Uri.parse(url);
//            Intent browser = new Intent(Intent.ACTION_VIEW, uri);
//            startActivity(browser);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
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

    private void settingsCheck(){

        String s = mSettings.getString(CHOSEN_CITY, "");

        assert s != null;
        if (s.equals("")){
            Intent intent = new Intent(MainActivity.this, Settings.class);

            startActivityForResult(intent, SETTINGS);
        }
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
