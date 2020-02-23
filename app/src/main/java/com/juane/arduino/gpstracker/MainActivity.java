package com.juane.arduino.gpstracker;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.juane.arduino.gpstracker.pager.BottomBarAdapter;
import com.juane.arduino.gpstracker.pager.NoSwipePager;
import com.juane.arduino.gpstracker.ui.home.HomeFragment;
import com.juane.arduino.gpstracker.ui.map.MapFragment;
import com.juane.arduino.gpstracker.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private NoSwipePager viewPager;

    private BottomNavigationView navigation;
    private HomeFragment homeFragment = new HomeFragment();
    private MapFragment mapFragment = new MapFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = findViewById(R.id.navigation);
        viewPager = findViewById(R.id.viewPager);

        //optimisation
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPagingEnabled(false);

        BottomBarAdapter pagerAdapter = new BottomBarAdapter(getSupportFragmentManager());

        pagerAdapter.addFragments(homeFragment);
        pagerAdapter.addFragments(mapFragment);
        pagerAdapter.addFragments(settingsFragment);

        viewPager.setAdapter(pagerAdapter);

        //Handling the tab clicks
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.homeTabId:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.mapTabId:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.settingsTabId:
                        viewPager.setCurrentItem(2);
                        return true;

                }
                return false;
            }
        });
    }

    public void changeTab(int tabId) {
        navigation.setSelectedItemId(tabId);
    }

    public MapFragment getMapFragment(){
        return mapFragment;
    }
}
