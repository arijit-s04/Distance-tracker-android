package com.android.arijit.firebase.walker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemReselectedListener {
    public static BottomNavigationView bottomNavigationView;
    private String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate: ");
        bottomNavigationView = findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemReselectedListener(this);
        if(savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.main_fragment_container, HomeFragment.newInstance(null, null))
                    .commit();
        }

    }

    private boolean loadFragment(Fragment fragment){
        if(fragment == null)    return false;

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("stack")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.main_fragment_container,fragment)
                .commit();

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull  MenuItem item) {
        Fragment fragment;
        FragmentManager fm = getSupportFragmentManager();
        switch (item.getItemId()){
            case R.id.navigation_home:
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_home)
                        .setIcon(R.drawable.ic_baseline_home_24);
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_settings)
                        .setIcon(R.drawable.ic_outline_settings_24);
                fm.popBackStack();
                return true;
            case R.id.navigation_history:
                if(fm.getBackStackEntryCount() > 0){
                    fm.popBackStack();
                }
                fragment = HistoryFragment.newInstance(null, null);
                break;
            case R.id.navigation_settings:
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_home)
                        .setIcon(R.drawable.ic_outline_home_24);
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_settings)
                        .setIcon(R.drawable.ic_baseline_settings_24);
                if(fm.getBackStackEntryCount() > 0){
                    fm.popBackStack();
                }
                fragment = SettingsFragment.newInstance(null, null);
                break;
            default:
                fragment = null;
        }
        return loadFragment(fragment);
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {}

//    @Override
//    public void onBackPressed() {
//        if(bottomNavigationView.getSelectedItemId() == R.id.navigation_home){
//            super.onBackPressed();
//            finish();
//        }
//        else{
//            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
//        }
//    }

}