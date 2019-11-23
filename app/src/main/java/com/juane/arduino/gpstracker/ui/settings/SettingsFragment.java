package com.juane.arduino.gpstracker.ui.settings;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.juane.arduino.gpstracker.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SettingsViewModel settingsViewModel;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        bindSummaryValue(findPreference("url_text"));
        bindSummaryValue(findPreference("distance_text"));
        bindSummaryValue(findPreference("interval_time"));
        bindSummaryValue(findPreference("phone_number"));
        bindSummaryValue(findPreference("message_text"));
    }

    private static void bindSummaryValue(Preference preference){
        preference.setOnPreferenceChangeListener(listener);

        listener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if(preference instanceof ListPreference){
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index > 0 ? listPreference.getEntries()[index]:null);
            } else if(preference instanceof EditTextPreference){
                preference.setSummary(stringValue);
            }
//            else if(preference instanceof RingtonePreference){
//                if(stringValue.isEmpty()) {
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
            return true;
        }
    };
}