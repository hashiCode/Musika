package com.hashicode.musika;

import android.support.v4.media.MediaMetadataCompat;

/**
 * Created by takahashi on 3/1/17.
 */

public final class MediaMetadataWrapper {

    private MediaMetadataCompat mediaMetadataCompat;

    public MediaMetadataWrapper(MediaMetadataCompat mediaMetadataCompat){
        this.mediaMetadataCompat = mediaMetadataCompat;
    }


    public MediaMetadataCompat getMediaMetadataCompat() {
        return mediaMetadataCompat;
    }

    public void setMediaMetadataCompat(MediaMetadataCompat mediaMetadataCompat) {
        this.mediaMetadataCompat = mediaMetadataCompat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaMetadataWrapper that = (MediaMetadataWrapper) o;

        return mediaMetadataCompat != null ? mediaMetadataCompat.equals(that.mediaMetadataCompat) : that.mediaMetadataCompat == null;

    }

    @Override
    public int hashCode() {
        return mediaMetadataCompat != null ? mediaMetadataCompat.hashCode() : 0;
    }
}
