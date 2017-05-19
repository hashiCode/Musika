package com.hashicode.musika;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.util.Pair;

/**
 * Created by takahashi on 1/18/17.
 */

public class AlbumCache {

    private static final int MAX_ALBUM_ART_CACHE_SIZE = 12*1024*1024;  // 12 MB

    private final LruCache<String, Pair<Bitmap, Bitmap>> cache;

    private static final int MAX_ART_WIDTH = 800;  // pixels
    private static final int MAX_ART_HEIGHT = 480;  // pixels

    private static AlbumCache albumCache;

    public static AlbumCache getInstance(){
        if(albumCache==null){
            albumCache = new AlbumCache();
        }
        return albumCache;
    }

    private AlbumCache(){
        cache = new LruCache<>(MAX_ALBUM_ART_CACHE_SIZE);
    }

    public Bitmap getAlbumArt(String uri){
        Pair<Bitmap, Bitmap> bitmaps = cache.get(uri);
        if(bitmaps!=null) {
            return bitmaps.first;
        }
        return null;
    }

    public Bitmap getLargeAlbumArt(String uri){
        Pair<Bitmap, Bitmap> bitmaps = cache.get(uri);
        if(bitmaps!=null) {
            return bitmaps.second;
        }
        return null;
    }

    public void fetchAlbumArt(final String uri, final AlbumCacheCallback cacheCallback){
        new AsyncTask<Void, Void, Pair<Bitmap, Bitmap>>() {

            @Override
            protected Pair<Bitmap, Bitmap> doInBackground(Void... params) {
                Bitmap originalBitmap = BitmapFactory.decodeFile(uri);
                Bitmap bitmap = scale(originalBitmap);
                Bitmap rescale = rescale(originalBitmap,uri);
                Pair<Bitmap, Bitmap> images = new Pair<Bitmap, Bitmap>(bitmap, rescale);
                cache.put(uri, images);
                return images;
            }

            @Override
            protected void onPostExecute(Pair<Bitmap, Bitmap> bitmaps) {
                if(bitmaps!=null){
                    cacheCallback.onAlbumFetched(bitmaps);
                }
            }

        }.execute();
    }

    private Bitmap rescale(Bitmap originalBitmap, String uri) {
        int scaleFactor = Math.min(originalBitmap.getWidth()/MAX_ART_WIDTH, originalBitmap.getHeight()/MAX_ART_HEIGHT);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(uri, bmOptions);
    }

    private Bitmap scale(Bitmap bitmap){
        double scaleFactor = Math.min(
                ((double) 128)/bitmap.getWidth(), ((double) 128)/bitmap.getHeight());
        return Bitmap.createScaledBitmap(bitmap,
                (int) (bitmap.getWidth() * scaleFactor), (int) (bitmap.getHeight() * scaleFactor), false);
    }

    public interface AlbumCacheCallback{
        void onAlbumFetched(Pair<Bitmap, Bitmap> bitMaps);
    }


}
