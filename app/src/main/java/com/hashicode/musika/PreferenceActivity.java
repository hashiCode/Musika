package com.hashicode.musika;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by takahashi on 5/18/17.
 */

public class PreferenceActivity extends AppCompatActivity {

    private PreferenceFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = new PreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

}
