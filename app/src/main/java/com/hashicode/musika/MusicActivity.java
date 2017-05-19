package com.hashicode.musika;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hashicode.musika.service.MusicMediaService;


public class MusicActivity extends BaseActivity
        implements  MediaItemCallback {

    private static final String MEDIA_ID = "mediaId";
    private static final String BROWSE_FRAG_TAG="browseFragment";
    private MediaBrowserCompat mediaBrowserCompat;

    private PlaybackFragment playbackFragment;

    private final static int PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private final static int PERMISSION_ACCESS_FINE_LOCATION = 2;
    private final static String TAG = "MusicActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_main);
        setupDrawer();
        mediaBrowserCompat = new MediaBrowserCompat(this,
                new ComponentName(this, MusicMediaService.class), mConnectionCallback, null);

        initializeBrowserFragment(savedInstanceState);

        Log.d(this.getClass().getName(), "MediaBrowse created");
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String mediaId = getBrowserFragment().getMediaId();
        if (mediaId != null) {
            outState.putString(MEDIA_ID, mediaId);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mediaBrowserCompat.connect();

                    Log.d(this.getClass().getName(), "Permission granted. MediaBrowse connecting");
                } else {
                        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                        finish();
                }
                break;
            }
        }
    }
    

    @Override
    protected void onStart() {
        super.onStart();
        playbackFragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_playback_controls);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ_EXTERNAL_STORAGE);

        } else {

            mediaBrowserCompat.connect();

            Log.d(this.getClass().getName(), "MediaBrowse connected");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_FINE_LOCATION);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(this);
        if(mediaController!=null) {
            mediaController.unregisterCallback(mMediaControllerCallback);
        }
        mediaBrowserCompat.disconnect();
        Log.d(this.getClass().getName(), "MediaBrowse disconnected");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }

    private void initializeBrowserFragment(Bundle savedInstanceState) {
        String mediaId = null;
        if(savedInstanceState!=null){
            mediaId = savedInstanceState.getString(MEDIA_ID);
        }

        prepareBrowse(mediaId);


    }

    private void prepareBrowse(String mediaId) {
        BrowserFragment browserFragment = getBrowserFragment();
        //if it's diferent, reload the content
        if(browserFragment==null || !TextUtils.equals(mediaId, browserFragment.getMediaId())){
            browserFragment = new BrowserFragment();
            Bundle bundle = new Bundle();
            bundle.putString(BrowserFragment.BUNDLE_MEDIA_ID, mediaId);
            browserFragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, browserFragment, BROWSE_FRAG_TAG);
            if (mediaId != null) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        initializeBrowserFragment(null);
    }


    private BrowserFragment getBrowserFragment(){
        return (BrowserFragment) getSupportFragmentManager().findFragmentByTag(BROWSE_FRAG_TAG);
    }

    private void setupGoogleFitApi() {
        Log.d(TAG, "Adding Sensor");

    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(mediaBrowserCompat.getSessionToken());


                        //TODO linkar controles de audio
                        playbackFragment.connect();
                    } catch (RemoteException e) {
                        Log.e(this.getClass().getName(), "could not connect media controller", e);
                    }
                }
            };


    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                    Log.d(this.getClass().getName(),"Playback state changed");
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    Log.d(this.getClass().getName(),"MediaMetadataCompat changed");
                }
            };


    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        if(mediaController!=null) {
            MediaControllerCompat.setMediaController(this, mediaController);
            mediaController.registerCallback(mMediaControllerCallback);
            Log.d(this.getClass().getName(), "MediaController registered");
        }
        getBrowserFragment().connect();


    }

    public MediaBrowserCompat getMediaBrowser(){
        return this.mediaBrowserCompat;
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem mediaItem){
        if(mediaItem.isBrowsable()){
            Toast.makeText(this, "Clicked on browseable item", Toast.LENGTH_SHORT).show();
            prepareBrowse(mediaItem.getMediaId());
        } else if (mediaItem.isPlayable()){
            MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(this);
            if(mediaControllerCompat!=null){
                MediaMetadataCompat currentMediaMetadata = mediaControllerCompat.getMetadata();
                if(currentMediaMetadata == null || !currentMediaMetadata.getDescription().getMediaId().equals(mediaItem.getMediaId())) {
                    mediaControllerCompat.getTransportControls().playFromMediaId(mediaItem.getMediaId(), null);
                }
            }
            else{
                Log.e(this.getClass().getCanonicalName(), "MediaController is null");
            }
            Toast.makeText(this, "Playing "+mediaItem.getDescription().getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Log.e(this.getClass().getName(), "Unknow media type");
        }

    }

}
