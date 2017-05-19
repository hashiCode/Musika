package com.hashicode.musika.provider;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by takahashi on 1/11/17.
 */
public final class MediaIdUtil {

    public static final String ROOT_MEDIA_ID = "root_mediaid";
    public static final String BROWSE_MUSICS_MEDIA_ID = "musics_mediaid";
    public static final String BROSWE_ARTISTS_MEDIA_ID = "artists_mediaid";

    private static final String CATEGORIES_SEPARATOR = "%";
    private static final String LEAF_SEPARATOR = "$";

    //media types
    public static final String MUSICS_TYPE = "musics";
    public static final String ARTISTS_TYPE = "artists";
    public static final String ARTIST_TYPE = "artist";
    public static final String MUSIC_TYPE = "music";

    public static String assembleMediaId(String musicId, String... categories){
        String mediaId = "";
        List<String> categoriesAsList = Arrays.asList(categories);
        Iterator<String> categoriesIterator = categoriesAsList.iterator();
        while(categoriesIterator.hasNext()){
            mediaId+=categoriesIterator.next();
            if(categoriesIterator.hasNext()){
                mediaId+= CATEGORIES_SEPARATOR;
            }
        }
        if(musicId!=null){
            mediaId+= LEAF_SEPARATOR +musicId;
        }
        return mediaId;
    }

    public static String[] getCategories(String mediaId){
        int pos = mediaId.indexOf(LEAF_SEPARATOR);
        if (pos >= 0) {
            mediaId = mediaId.substring(0, pos);
        }
        return mediaId.split(String.valueOf(CATEGORIES_SEPARATOR));
    }

    public static String getLeaf(String mediaId){
        int pos = mediaId.indexOf(LEAF_SEPARATOR);
        if (pos >= 0) {
            return mediaId.substring(pos+1, mediaId.length());
        }
        return "";
    }

    private static boolean isLeaf(String mediaId){
        return mediaId.contains(LEAF_SEPARATOR);
    }

    public static String getMediaType(@NonNull String mediaId){
        if(mediaId.equals(BROWSE_MUSICS_MEDIA_ID)){
            return MUSICS_TYPE;
        }
        if(mediaId.equals(BROSWE_ARTISTS_MEDIA_ID)){
            return ARTISTS_TYPE;
        }
        if(!isLeaf(mediaId)){
            return ARTIST_TYPE;
        }
        return MUSIC_TYPE;
    }

}
