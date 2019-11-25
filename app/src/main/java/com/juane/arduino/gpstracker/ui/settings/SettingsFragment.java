package com.juane.arduino.gpstracker.ui.settings;

import android.os.Bundle;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.juane.arduino.gpstracker.R;
import com.juane.arduino.gpstracker.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SettingsFragment";
    private SettingsViewModel settingsViewModel;

    private static boolean isURLValidated = false;
    private static boolean isDistanceValidated = false;
    private static boolean isMobileValidated = false;
    private static boolean parametersEmpty = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        bindSummaryValue(Objects.requireNonNull(findPreference(getResources().getString(R.string.key_url))));
        bindSummaryValue(Objects.requireNonNull(findPreference(getResources().getString(R.string.key_distance))));
        bindSummaryValue(Objects.requireNonNull(findPreference(getResources().getString(R.string.key_intervalTime))));
        bindSummaryValue(Objects.requireNonNull(findPreference(getResources().getString(R.string.key_phone))));
        bindSummaryValue(Objects.requireNonNull(findPreference(getResources().getString(R.string.key_message))));
    }

    private void bindSummaryValue(Preference preference) {
        preference.setOnPreferenceChangeListener(listener);

        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String oldValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), "");
            String stringValue = newValue.toString();
            parametersEmpty = false;

            if (stringValue.isEmpty()) {
                Utils.showInvalidParameterDialog(getActivity(), null);

                parametersEmpty = true;
            } else {
                if (preference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);
                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                } else if (preference instanceof EditTextPreference) {
                    if (preference.getKey().equals(getResources().getString(R.string.key_url))) { //URL
                        // validate URL
                        try {
                            isURLValidated = true;
                            URL url = new URL(stringValue);
                            preference.setSummary(stringValue);
                        } catch (MalformedURLException e) {
                            //e.printStackTrace();
                            Log.e(TAG, "Malformed URL..");
                            isURLValidated = false;
                            Utils.showInvalidParameterDialog(getActivity(),  "URL");
                        }
                    } else if (preference.getKey().equals(getResources().getString(R.string.key_phone))) { //PHONE
                        preference.setSummary(stringValue);
                        isMobileValidated = true;

                        if (stringValue.isEmpty() || !Utils.isValidMobile(stringValue)) {
                            Log.e(TAG, "Invalid mobile phone..");
                            isMobileValidated = false;
                            Utils.showInvalidParameterDialog(getActivity(), "mobile phone");
                        }
                    }
                    if (preference.getKey().equals(getResources().getString(R.string.key_distance))) { //DISTANCE
                        preference.setSummary(stringValue);
                        isDistanceValidated = true;

                        if (!Utils.isValidMobile(stringValue)) {
                            if (stringValue.isEmpty() || Double.parseDouble(stringValue) < 0) {
                                Log.e(TAG, "Invalid distance..");
                                isDistanceValidated = false;
                                Utils.showInvalidParameterDialog(getActivity(), "distance");
                            }
                        }
                    }
                }

//            else if(preference instanceof RingtonePreference){
//                if(stringValue.parametersEmpty()) {
//                    preference.setSummary("Silent");
//                }else{
//                    Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
//
//                    if(ringtone == null){
//                        preference.setSummary("Choose notification ringtone");
//                    }else{
//                        preference.setSummary(ringtone.getTitle(preference.getContext()));
//                    }
//                }
//            }
            }
            return true;
        }
    };

    public static boolean isSettingsValidated() {
        return !parametersEmpty && isDistanceValidated && isURLValidated && isMobileValidated;
    }
}