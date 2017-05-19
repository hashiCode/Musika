package com.hashicode.musika;

import android.support.v4.app.SupportActivity;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;

/**
 * Created by takahashi on 4/26/17.
 */

public class PlayPauseClickListener implements View.OnClickListener {

    public SupportActivity activity;

    public PlayPauseClickListener(SupportActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(activity);
        if (mediaController != null) {
            PlaybackStateCompat playbackState = mediaController.getPlaybackState();
            final int state = playbackState == null ?
                    PlaybackStateCompat.STATE_NONE : playbackState.getState();
            if (state == PlaybackStateCompat.STATE_PAUSED ||
                    state == PlaybackStateCompat.STATE_STOPPED ||
                    state == PlaybackStateCompat.STATE_NONE) {
                mediaController.getTransportControls().play();
            } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                    state == PlaybackStateCompat.STATE_BUFFERING ||
                    state == PlaybackStateCompat.STATE_CONNECTING) {
                mediaController.getTransportControls().pause();
            }
        }
    }
}
