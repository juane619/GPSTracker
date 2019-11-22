package com.juane.arduino.gpstracker.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.juane.arduino.gpstracker.R;
import com.juane.arduino.gpstracker.service.RequestService;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;

    private Switch alarmSwitch;
    private Switch realTimeSwitch;
    private Button showLocationButton;

    private ProgressBar pBarProgress;

    Intent intentRequestService;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        alarmSwitch = root.findViewById(R.id.alarmSwitch);
        realTimeSwitch = root.findViewById(R.id.realTimeSwitch);
        showLocationButton = root.findViewById(R.id.showLocationButton);
        pBarProgress = root.findViewById(R.id.progressBar);

        intentRequestService = new Intent(getActivity(), RequestService.class);

        setAlarmSwitch();
        setRealTimeSwitch();
        setShowLocationButton();
        setProgressBar();

        return root;
    }

    private void setProgressBar() {
        pBarProgress.setVisibility(View.GONE);
    }


    private void setRealTimeSwitch() {
        realTimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(realTimeSwitch.isChecked()){
                    Log.i(TAG, "Real Time ON");
                }else{
                    Log.i(TAG, "Real Time OFF");
                }
            }
        });
    }

    private void setAlarmSwitch() {
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(alarmSwitch.isChecked()){
                    Log.i(TAG, "Switch alarm ON");
                    getActivity().startService(intentRequestService);
                }else{
                    Log.i(TAG, "Switch alarm OFF");
                    getActivity().stopService(intentRequestService);
                }
            }
        });
    }

    private void setShowLocationButton() {
        showLocationButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                BottomNavigationView navView = getActivity().findViewById(R.id.navigation);
                navView.setSelectedItemId(R.id.tab2);
            }
        });
    }
}