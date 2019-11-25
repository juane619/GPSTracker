package com.juane.arduino.gpstracker.ui.home;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

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

    private Intent intentRequestService;
    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "RECIBIENDO MENSAJE DEL SERVICE");

            switch (msg.what) {
                case RequestService.MSG_PROBLEM_STARTING:
                    alarmSwitch.setChecked(false);
                    Toast.makeText(getContext(), "URL malformed..", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Toast.makeText(getActivity(), "Attached..", Toast.LENGTH_SHORT).show();

            try {
                Message msg = Message.obtain(null, RequestService.MSG_PROBLEM_STARTING);
                msg.arg2 = 7878;
                msg.replyTo = mMessenger;
                mService.send(msg);
            }
            catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            Toast.makeText(getActivity(), "Disconnected..", Toast.LENGTH_SHORT).show();
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        alarmSwitch = root.findViewById(R.id.alarmSwitch);
        realTimeSwitch = root.findViewById(R.id.realTimeSwitch);
        showLocationButton = root.findViewById(R.id.showLocationButton);

        intentRequestService = new Intent(getActivity(), RequestService.class);
        doBindService();

        setAlarmSwitch();
        setRealTimeSwitch();
        setShowLocationButton();



        return root;
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
                    if(getActivity() != null && !RequestService.isRunning()) {
                        getActivity().startService(intentRequestService);
                    }
                }else{
                    Log.i(TAG, "Switch alarm OFF");
                    //if(getActivity() != null && RequestService.isRunning())
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
                if(getActivity() != null) {
                    BottomNavigationView navView = getActivity().findViewById(R.id.navigation);
                    navView.setSelectedItemId(R.id.tab2);
                }
            }
        });
    }

    void doBindService() {
        getActivity().bindService(intentRequestService, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.i(TAG, "Binding..");
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, 4646, 4646);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
            Log.i(TAG, "UnBinding..");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            doUnbindService();
        }
        catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
    }
}