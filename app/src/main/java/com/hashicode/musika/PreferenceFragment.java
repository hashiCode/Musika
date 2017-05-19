package com.hashicode.musika;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by takahashi on 4/11/17.
 */

public class PreferenceFragment extends android.preference.PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_SHOW_NOTIFICATION = "mn_show_notification";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case PREF_SHOW_NOTIFICATION:

                break;
            default :
        }
    }


}
