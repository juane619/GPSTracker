package com.juane.arduino.gpstracker;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.juane.arduino.gpstracker.pager.BottomBarAdapter;
import com.juane.arduino.gpstracker.pager.NoSwipePager;
import com.juane.arduino.gpstracker.ui.home.HomeFragment;
import com.juane.arduino.gpstracker.ui.map.MapFragment;
import com.juane.arduino.gpstracker.ui.settings.SettingsFragment;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private NoSwipePager viewPager;

    private HomeFragment frag1 = new HomeFragment();
    private MapFragment frag2 = new MapFragment();
    private SettingsFragment frag3 = new SettingsFragment();

    private File fileOS = null;
    private File fileOSAux = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setFiles();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        viewPager = findViewById(R.id.viewPager);

        //optimisation
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPagingEnabled(false);

        BottomBarAdapter pagerAdapter = new BottomBarAdapter(getSupportFragmentManager());

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

    public BottomBarAdapter getBottomBarAdapter() {
        return (BottomBarAdapter) viewPager.getAdapter();
    }

    private void setFiles() {
        String FILE_NAME = this.getResources().getString(R.string.path_main_filegps);
        String FILE_NAME_AUX = this.getResources().getString(R.string.path_auxfilegps);

        try {
//            url = new URL(SOURCE_URL);
//            Log.i(TAG, "SOURCE_URL: " + SOURCE_URL);

            if (isExternalDirectoryPresent()) {
                if(getExternalFilesDir(null) != null) {
                    fileOS = new File(Objects.requireNonNull(getExternalFilesDir(null)).getPath() + "/" + FILE_NAME);
                    fileOSAux = new File(Objects.requireNonNull(getExternalFilesDir(null)).getPath() + "/" + FILE_NAME_AUX);
                }

                if (!fileOS.exists()) {
                    Log.i(TAG, "File not exists! " + fileOS.getAbsolutePath());

                    if (fileOS.createNewFile()) {
                        Log.i(TAG, "File created!");
                    }
                }

                if (fileOSAux.exists()) {
                    Log.i(TAG, "File aux not exists! " + fileOSAux.getAbsolutePath());

                    if (fileOSAux.createNewFile()) {
                        Log.i(TAG, "File aux created!");
                    }
                }
            } else {
                Log.e(TAG, "Not external storage!");
            }
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
            //e.printStackTrace();
        }
    }

    //Check If SD Card is present or not method
    private boolean isExternalDirectoryPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
}
