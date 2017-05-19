package com.hashicode.musika.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by takahashi on 10/5/16.
 */

public class LocalMusicProviderSource {

    private ContentResolver contentResolver;
    private Map<Long, String> albumArtUriCache = new HashMap<>();

    public LocalMusicProviderSource(Context context){
        this.contentResolver = context.getContentResolver();

    }


    public List<MediaMetadataCompat> getAudios(){
        List<MediaMetadataCompat> audios = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            // query failed, handle error.
        } else if (!cursor.moveToFirst()) {
            // no media on the device
        } else {
            do{
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int mediaIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int mediaPathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int mediaDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                long albumId = cursor.getLong(albumIdIndex);
                String albumArtUri = getAlbumArt(albumId);
                audios.add(new MediaMetadataCompat.Builder().
                        putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, cursor.getString(mediaIdIndex)).
                        putString(MediaMetadataCompat.METADATA_KEY_ARTIST, cursor.getString(artistIndex)).
                        putString(MediaMetadataCompat.METADATA_KEY_ALBUM, cursor.getString(albumIndex)).
                        putString(MediaMetadataCompat.METADATA_KEY_TITLE, cursor.getString(titleIndex)).
                        putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, cursor.getString(mediaPathIndex)).
                        putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtUri).
                        putLong(MediaMetadataCompat.METADATA_KEY_DURATION, cursor.getLong(mediaDuration)).
                        build());

            } while(cursor.moveToNext());
        }
        albumArtUriCache.clear();
        cursor.close();
        return audios;
    }

    private String getAlbumArt(Long albumId) {
        String albumUri = "";
        if(albumArtUriCache.containsKey(albumId)){
            albumUri = albumArtUriCache.get(albumId);
        }
        else{
            Cursor cursor = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID + " = ?",
                    new String[]{Long.toString(albumId)},
                    null
            );
            if (cursor.moveToFirst()) {
                albumUri = cursor.getString(0);
            }
            cursor.close();
            albumArtUriCache.put(albumId, albumUri);
        }
        return albumUri;
    }
}
