package com.chdman.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.preference.Preference;
import com.chdman.R;
import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.chdman.utils.Chdman;

public class PreferenceFragment extends PreferenceFragmentCompat {
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference deleteSource = findPreference("deletesource");
        if (deleteSource != null) {
            deleteSource.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference pref, Object newValue) {
                    Chdman.deleteInput = Boolean.parseBoolean(newValue.toString());
                    return true;
                }  
            });
        }
        Preference theme = findPreference("theme");
        theme.setSummary(getPreferenceManager().getSharedPreferences().getString("theme", "Light"));
        if (theme != null) {
            theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference pref, Object newValue) {
                    switch (newValue.toString()) {
                        case "Light":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            pref.setSummary("Light");
                            break;
                        case "Dark":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            pref.setSummary("Dark");
                            break;
                        case "Follow System":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            pref.setSummary("Follow Systwm");
                            break;
                    }
                    return true;
                }  
            });
        }
    }
}
