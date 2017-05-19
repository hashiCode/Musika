package com.hashicode.musika.service;


import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

/**
 * Created by takahashi on 4/26/17.
 */

public interface MusicQueueCallback {

    void onQueueCreated(List<MediaSessionCompat.QueueItem> queue);
}
