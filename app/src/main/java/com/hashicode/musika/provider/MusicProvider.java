package com.hashicode.musika.provider;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.hashicode.musika.MediaMetadataWrapper;
import com.hashicode.musika.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.hashicode.musika.provider.MediaIdUtil.BROSWE_ARTISTS_MEDIA_ID;
import static com.hashicode.musika.provider.MediaIdUtil.BROWSE_MUSICS_MEDIA_ID;
import static com.hashicode.musika.provider.MediaIdUtil.ROOT_MEDIA_ID;

/**
 * Created by takahashi on 10/5/16.
 */

public class MusicProvider {

    private LocalMusicProviderSource localMusicProviderSource;
//    private List<MediaMetadataCompat> musics = new ArrayList<>();
    private Map<String, MediaMetadataWrapper> musicByMediaId = new LinkedHashMap<>();
    private Map<String, List<MediaMetadataWrapper>> musicsByArtist = new TreeMap<>();

    private String state = State.CREATED;


    public MusicProvider(Context context){
        this.localMusicProviderSource = new LocalMusicProviderSource(context);
    }

    public void retrieveMusicAsync(final MusicProviderInitializedCallback postCallback){
        if(hasInitialized()){
            if(postCallback!=null) {
                postCallback.postInitialized();
            }
            return;
        }
        // Asynchronously load the music catalog in a separate thread
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                retrieveMusic();
                return null;
            }

            @Override
            protected void onPostExecute(Void current) {
                if(postCallback!=null) {
                    postCallback.postInitialized();
                }
            }
        }.execute();
    }


    private synchronized void retrieveMusic(){
        if(state.equals(State.CREATED)) {
            state = State.INITIALIZING;
            List<MediaMetadataCompat> audios = localMusicProviderSource.getAudios();
            for (MediaMetadataCompat media : audios) {
                String artist = media.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                if (!musicsByArtist.containsKey(artist)) {
                    musicsByArtist.put(artist, new ArrayList<MediaMetadataWrapper>());
                }
                musicsByArtist.get(artist).add(new MediaMetadataWrapper(media));
                musicByMediaId.put(media.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID), new MediaMetadataWrapper(media));
            }
            state = State.INITIALIZED;
        }

    }


    public List<MediaBrowserCompat.MediaItem> onLoadChildren(String parentId, Resources resources) {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();

        if(parentId.equals(ROOT_MEDIA_ID)){
            result.addAll(createRootsMediaItem(parentId, resources));
        }
        else if (BROWSE_MUSICS_MEDIA_ID.equals(parentId)){
            for(MediaMetadataWrapper mediaMetadataCompat : musicByMediaId.values()){
                result.add(createMediaMetadataMediaItem(mediaMetadataCompat.getMediaMetadataCompat(), resources, BROWSE_MUSICS_MEDIA_ID));
            }
        }
        else if (BROSWE_ARTISTS_MEDIA_ID.equals(parentId)){
            for(Map.Entry<String, List<MediaMetadataWrapper>> artistAndMedias : musicsByArtist.entrySet()){
                result.add(createArtistMediaItem(artistAndMedias, resources));
            }
        }
        else if (parentId.contains(BROSWE_ARTISTS_MEDIA_ID)){
            String artist = MediaIdUtil.getCategories(parentId)[1];
            for(MediaMetadataWrapper mediaMetadataCompat : musicsByArtist.get(artist)){
                result.add(createMediaMetadataMediaItem(mediaMetadataCompat.getMediaMetadataCompat(), resources, BROSWE_ARTISTS_MEDIA_ID, artist));
            }
        }


        return result;
    }

    private MediaBrowserCompat.MediaItem createMediaMetadataMediaItem(MediaMetadataCompat mediaMetadataCompat, Resources resources, String... parentCategories) {
        MediaMetadataCompat mediaMetadatahierarchy = new MediaMetadataCompat.Builder(mediaMetadataCompat)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, MediaIdUtil.assembleMediaId(mediaMetadataCompat.getDescription().getMediaId(), parentCategories))
                .build();
        return new MediaBrowserCompat.MediaItem(mediaMetadatahierarchy.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    private MediaBrowserCompat.MediaItem createArtistMediaItem(Map.Entry<String, List<MediaMetadataWrapper>> artistMedias, Resources resources) {
        MediaDescriptionCompat artistDescription = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIdUtil.assembleMediaId(null, BROSWE_ARTISTS_MEDIA_ID,artistMedias.getKey()))
                .setTitle(artistMedias.getKey())
                .setSubtitle(resources.getString(R.string.total_music, new Integer(artistMedias.getValue().size())))
                .setIconUri(Uri.parse("android.resource://com.hashicode.musika/drawable/ic_library_music_white_24dp"))
//                .setSubtitle(resources.getString(R.string.browse_genre_subtitle))
//                .setIconUri(Uri.parse("android.resource://" +
//                        "com.example.android.uamp/drawable/ic_by_genre"))
        .build();
        return new MediaBrowserCompat.MediaItem(artistDescription,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private List<MediaBrowserCompat.MediaItem> createRootsMediaItem(String parentId, Resources resources) {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        MediaDescriptionCompat musicsDescription = new MediaDescriptionCompat.Builder()
                .setMediaId(BROWSE_MUSICS_MEDIA_ID)
                .setTitle(resources.getString(R.string.browse_musics))
                .setSubtitle(resources.getString(R.string.total_music, new Integer(musicByMediaId.size()) ))
//                .setSubtitle(resources.getString(R.string.browse_genre_subtitle))
//                .setIconUri(Uri.parse("android.resource://" +
//                        "com.example.android.uamp/drawable/ic_by_genre"))
                .setIconUri(Uri.parse("android.resource://com.hashicode.musika/drawable/ic_library_music_white_24dp"))
                .build();
        result.add(new MediaBrowserCompat.MediaItem(musicsDescription,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

        MediaDescriptionCompat artistsDescription = new MediaDescriptionCompat.Builder()
                .setMediaId(BROSWE_ARTISTS_MEDIA_ID)
                .setTitle(resources.getString(R.string.browse_artists))
                .setSubtitle(resources.getString(R.string.total_artist, new Integer(musicsByArtist.keySet().size()) ))
                .setIconUri(Uri.parse("android.resource://com.hashicode.musika/drawable/account_multiple"))
//                .setSubtitle(resources.getString(R.string.browse_genre_subtitle))
//                .setIconUri(Uri.parse("android.resource://" +
//                        "com.example.android.uamp/drawable/ic_by_genre"))
                .build();
        result.add(new MediaBrowserCompat.MediaItem(artistsDescription,
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

        return result;
    }

    public boolean hasInitialized(){
        return state.equals(State.INITIALIZED);
    }


    private class State{
        public static final String CREATED = "created";
        public static final String INITIALIZING = "initializing";
        public static final String INITIALIZED = "initialized";
    }

    public interface MusicProviderInitializedCallback{

        void postInitialized();
    }

    public Collection<MediaMetadataWrapper> getMusics() {
        return musicByMediaId.values();
    }

    public List<MediaMetadataWrapper> getMusicsByArtist(String artist) {
        return musicsByArtist.get(artist);
    }

    public MediaMetadataWrapper getMusicByMediaId(String mediaId) {
        return musicByMediaId.get(mediaId);
    }

    public void updateAlbumArt(Bitmap albumArt, Bitmap albumIcon, String mediaId){
        MediaMetadataWrapper mediaMetadataWrapper = musicByMediaId.get(mediaId);

        mediaMetadataWrapper.setMediaMetadataCompat(new MediaMetadataCompat.Builder(mediaMetadataWrapper.getMediaMetadataCompat())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, albumIcon).build());

    }
}
