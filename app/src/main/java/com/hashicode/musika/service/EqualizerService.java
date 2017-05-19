package com.hashicode.musika.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;

import com.hashicode.musika.Constants;
import com.hashicode.musika.R;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by takahashi on 2/13/17.
 */

public class EqualizerService extends BroadcastReceiver {

    public static final String EQUALIZER_PARAMETERS = "equalizerParameters";


    private Equalizer equalizer;
    private BassBoost bassBoost;

    public EqualizerService(MediaPlayer mediaPlayer, Context context){
        equalizer = new Equalizer(Integer.MAX_VALUE, mediaPlayer.getAudioSessionId());
        bassBoost = new BassBoost(Integer.MAX_VALUE, mediaPlayer.getAudioSessionId());
        registerAction(context);
        saveEqualizerInfo(context);
    }

    private void saveEqualizerInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.musika_preference), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        short numberOfBands = equalizer.getNumberOfBands();
        short[] bandLevelRange = equalizer.getBandLevelRange();
        edit.putInt(Constants.NUM_OF_BANDS, numberOfBands);
        edit.putInt(Constants.MIN_RANGE, bandLevelRange[0]);
        edit.putInt(Constants.MAX_RANGE, bandLevelRange[1]);
        Set<String> bandNames = new TreeSet<>();
        for(short i=0; i<numberOfBands; i++){
            String bandName = (equalizer.getCenterFreq(i)/1000)+"Hz";
            bandNames.add(bandName);
        }
        edit.putStringSet(Constants.BAND_NAMES, bandNames);
        edit.commit();

    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }



    private void registerAction(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(EQUALIZER_PARAMETERS);
        context.registerReceiver(this, filter);
    }
}
