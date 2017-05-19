package com.hashicode.musika;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by takahashi on 4/26/17.
 */

public class AlbumFullPlayerFragment extends Fragment {

    public static final String KEY_ALBUM_ART_URI = "albumArtUri";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.album_fragment, container, false);
        final ImageView albumArtImage =(ImageView) rootView.findViewById(R.id.album_art);
        String iconUri = getArguments().getString(KEY_ALBUM_ART_URI);
        AlbumCache albumCache = AlbumCache.getInstance();
        if(iconUri!=null) {
            Bitmap albumArtBitmap = albumCache.getLargeAlbumArt(iconUri);
            if(albumArtBitmap!=null){
                albumArtImage.setImageBitmap(albumArtBitmap);
            }
            else{
                albumCache.fetchAlbumArt(iconUri, new AlbumCache.AlbumCacheCallback() {
                    @Override
                    public void onAlbumFetched(Pair<Bitmap, Bitmap> bitMaps) {
                        albumArtImage.setImageBitmap(bitMaps.second);
                    }
                });
            }
        }
        else{
            albumArtImage.setImageResource(R.drawable.ic_music_note_white_48dp);
        }

        return rootView;
    }
}
