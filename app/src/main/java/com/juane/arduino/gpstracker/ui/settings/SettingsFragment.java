package com.juane.arduino.gpstracker.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.juane.arduino.gpstracker.R;
import com.juane.arduino.gpstracker.service.RequestService;
import com.juane.arduino.gpstracker.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SettingsFragment";

    private static boolean isURLValidated = false;
    private static boolean isDistanceValidated = false;
    private static boolean parametersEmpty = false;

    private EditTextPreference editTextPreferenceURL;
    private EditTextPreference editTextPreferenceUser;
    private EditTextPreference editTextPreferencePassword;
    private EditTextPreference editTextPreferenceDistance;
    private EditTextPreference editTextPreferenceChatId;
    private EditTextPreference editTextPreferenceMessage;
    private ListPreference listPreferenceTime;
    private ListPreference listPreferenceSound;

    @Override
    public void onResume() {
        super.onResume();

        if(RequestService.isRunning()){
            disableAllPreferences();

            Utils.showServiceIsRunningDialog(getActivity(), null);
        }else{
            enableAllPreferences();
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        editTextPreferenceURL = (EditTextPreference) Objects.requireNonNull(findPreference(getResources().getString(R.string.key_url)));
        editTextPreferenceUser = (EditTextPreference) Objects.requireNonNull(findPreference(getResources().getString(R.string.key_user)));
        editTextPreferencePassword = (EditTextPreference) Objects.requireNonNull(findPreference(getResources().getString(R.string.key_password)));
        editTextPreferenceDistance = (EditTextPreference) Objects.requireNonNull(findPreference(getResources().getString(R.string.key_distance)));
        listPreferenceTime = (ListPreference) Objects.requireNonNull(findPreference(getResources().getString(R.string.key_intervalTime)));
        editTextPreferenceChatId = (EditTextPreference) Objects.requireNonNull(findPreference(getResources().getString(R.string.key_chatid)));
        editTextPreferenceMessage = (EditTextPreference) Objects.requireNonNull(findPreference(getResources().getString(R.string.key_message)));
        listPreferenceSound = (ListPreference) Objects.requireNonNull(findPreference(getResources().getString(R.string.key_soundNotification)));

        bindSummaryValue(editTextPreferenceURL);
        bindSummaryValue(editTextPreferenceUser);
        bindSummaryValue(editTextPreferencePassword);
        bindSummaryValue(editTextPreferenceDistance);
        bindSummaryValue(listPreferenceTime);
        bindSummaryValue(editTextPreferenceChatId);
        bindSummaryValue(editTextPreferenceMessage);
        bindSummaryValue(listPreferenceSound);
    }

    private void bindSummaryValue(Preference preference) {
        preference.setOnPreferenceChangeListener(listener);

        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            //String oldValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), "");
            String stringValue = newValue.toString();
            parametersEmpty = false;

            if (stringValue.isEmpty()) {
                //Utils.showInvalidParameterDialog(getActivity(), null);
                //parametersEmpty = true;
                preference.setSummary(stringValue);
                //getActivity().findViewById(preference.getLayoutResource());
            } else {
                if (preference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);
                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                } else if (preference instanceof EditTextPreference) {
                    preference.setSummary(stringValue);

                    if (preference.getKey().equals(getResources().getString(R.string.key_url))) { //URL
                        // validate URL
                        try {
                            URL url = new URL(stringValue);
                            isURLValidated = true;
                        } catch (MalformedURLException e) {
                            //e.printStackTrace();
                            Log.w(TAG, "Malformed URL..");
                            isURLValidated = false;
                            Utils.showInvalidParameterDialog(getActivity(),  "URL");
                        }
                    } else if (preference.getKey().equals(getResources().getString(R.string.key_distance))) { //DISTANCE
                        isDistanceValidated = true;

                            if (Double.parseDouble(stringValue) < 0) {
                                Log.e(TAG, "Invalid distance..");
                                isDistanceValidated = false;
                                Utils.showInvalidParameterDialog(getActivity(), "distance");
                            }
                    }else if(preference.getKey().equals(getResources().getString(R.string.key_password))){
                        preference.setSummary("******");
                    }
                }
            }
            return true;
        }
    };

    public static boolean isSettingsValidated() {
        return !parametersEmpty && isDistanceValidated && isURLValidated;
    }

    private void enableAllPreferences() {
        editTextPreferenceURL.setEnabled(true);
        editTextPreferenceDistance.setEnabled(true);
        listPreferenceTime.setEnabled(true);
        editTextPreferenceChatId.setEnabled(true);
        editTextPreferenceMessage.setEnabled(true);
    }

    private void disableAllPreferences() {
        editTextPreferenceURL.setEnabled(false);
        editTextPreferenceDistance.setEnabled(false);
        listPreferenceTime.setEnabled(false);
        editTextPreferenceChatId.setEnabled(false);
        editTextPreferenceMessage.setEnabled(false);
    }
}