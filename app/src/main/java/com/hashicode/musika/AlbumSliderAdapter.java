package com.hashicode.musika;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

/**
 * Created by takahashi on 4/26/17.
 */

public class AlbumSliderAdapter extends FragmentStatePagerAdapter {

    //previous, current, next
    private final int ALBUM_TO_SHOW = 3;
    private List<MediaSessionCompat.QueueItem> queueItems;

    public AlbumSliderAdapter(FragmentManager fm, List<MediaSessionCompat.QueueItem> queueItems) {
        super(fm);
        this.queueItems = queueItems;
    }

    @Override
    public Fragment getItem(int position) {
        AlbumFullPlayerFragment fragment = new AlbumFullPlayerFragment();
        MediaSessionCompat.QueueItem queueItem = queueItems.get(position);
        Uri iconUri = queueItem.getDescription().getIconUri();
        Bundle bundle = new Bundle();
        bundle.putString(AlbumFullPlayerFragment.KEY_ALBUM_ART_URI,
                iconUri != null ? iconUri.toString() : null);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return queueItems.size();
    }
}
