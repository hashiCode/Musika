package com.hashicode.musika.service;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by takahashi on 2/1/17.
 */

public interface PlaybackServieCallback {

    void onPlay();

    void onPause();

    void onStop();

    void onPlayBackStateChange(PlaybackStateCompat state);

    void onMetadataChange(MediaMetadataCompat mediaMetadataCompat);
}
