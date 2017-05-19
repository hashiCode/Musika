package com.hashicode.musika;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by takahashi on 5/9/17.
 */

public class EqualizerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.equalizer_activity);

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.musika_preference), Context.MODE_PRIVATE);
        int numOfBands = sharedPreferences.getInt(Constants.NUM_OF_BANDS,0);
        Toast.makeText(this, "NumOBands "+numOfBands, Toast.LENGTH_LONG).show();
    }
}
