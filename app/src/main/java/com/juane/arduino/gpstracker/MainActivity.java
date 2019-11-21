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
import com.juane.arduino.gpstracker.ui.notifications.NotificationsFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NoSwipePager viewPager;
    private BottomBarAdapter pagerAdapter;
    BottomNavigationView navigation;

    HomeFragment frag1 = new HomeFragment();
    MapFragment frag2 = new MapFragment();
    NotificationsFragment frag3 = new NotificationsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = findViewById(R.id.navigation);
        viewPager = findViewById(R.id.viewPager);

        //optimisation
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPagingEnabled(false);

        pagerAdapter = new BottomBarAdapter(getSupportFragmentManager());

        pagerAdapter.addFragments(frag1);
        pagerAdapter.addFragments(frag2);
        pagerAdapter.addFragments(frag3);

        viewPager.setAdapter(pagerAdapter);

        //Handling the tab clicks
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.tab1:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.tab2:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.tab3:
                        viewPager.setCurrentItem(2);
                        return true;

                }
                return false;
            }
        });
    }
}
