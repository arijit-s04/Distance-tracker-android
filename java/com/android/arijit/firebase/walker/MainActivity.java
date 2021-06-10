package com.android.arijit.firebase.walker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemReselectedListener {
    private BottomNavigationView bottomNavigationView;
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
                fm.popBackStack();
                return true;
//                fragment = HomeFragment.newInstance(null, null);
            case R.id.navigation_history:
                if(fm.getBackStackEntryCount() > 0){
                    fm.popBackStack();
                }
                fragment = HistoryFragment.newInstance(null, null);
                break;
            case R.id.navigation_settings:
                if(fm.getBackStackEntryCount() > 0){
                    fm.popBackStack();
                }
                fragment = SettiingsFragment.newInstance(null, null);
                break;
            default:
                fragment = null;
        }
        return loadFragment(fragment);
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {}

    @Override
    public void onBackPressed() {
        if(bottomNavigationView.getSelectedItemId() == R.id.navigation_home){
            super.onBackPressed();
            finish();
        }
        else{
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    /**
 @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }
    @Override
    protected void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        Log.i(TAG, "onSaveInstanceState: ");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState: ");
    }
    */
}