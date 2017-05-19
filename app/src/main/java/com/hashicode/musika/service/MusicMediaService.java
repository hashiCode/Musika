package com.hashicode.musika.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.hashicode.musika.provider.MusicProvider;

import java.util.List;

import static com.hashicode.musika.provider.MediaIdUtil.ROOT_MEDIA_ID;

/**
 * Created by takahashi on 1/5/17.
 */

public class MusicMediaService extends MediaBrowserServiceCompat implements PlaybackServieCallback, MusicQueueCallback {

    private MediaSessionCompat mediaSessionCompat;
    private MusicProvider musicProvider;
    private PlaybackStateCompat.Builder mStateBuilder;
    private PlaybackService playbackService;
    private NotificationService notificationService;


    //TODO add mediaPlayer

    @Override
    public void onCreate() {
        super.onCreate();
        musicProvider = new MusicProvider(this.getApplicationContext());
        musicProvider.retrieveMusicAsync(null);

        mediaSessionCompat = new MediaSessionCompat(this, MusicMediaService.class.getSimpleName());
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        Context context = getApplicationContext();
        mediaButtonIntent.setClass(context, MusicMediaService.class);
        PendingIntent pendindIntent =
                PendingIntent.getService(context, 0, mediaButtonIntent, 0);
        mediaSessionCompat.setMediaButtonReceiver(pendindIntent);
        playbackService = new PlaybackService(this.getApplicationContext(),this, musicProvider);
        mediaSessionCompat.setCallback(playbackService.getMediaSessionCallback());
        mediaSessionCompat.setActive(true);
        setSessionToken(mediaSessionCompat.getSessionToken());

        notificationService = new NotificationService(this);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(ROOT_MEDIA_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        if(musicProvider.hasInitialized()) {
            List<MediaBrowserCompat.MediaItem> children = this.musicProvider.onLoadChildren(parentId, getResources());
            result.sendResult(children);
        }else{
            result.detach();
            musicProvider.retrieveMusicAsync(new MusicProvider.MusicProviderInitializedCallback() {
                @Override
                public void postInitialized() {
                    result.sendResult(musicProvider.onLoadChildren(parentId, getResources()));
                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationService.stopNotification();
        playbackService.destroy();
        mediaSessionCompat.release();
    }

    @Override
    public void onPlay() {
        mediaSessionCompat.setActive(true);
        startService(new Intent(getApplicationContext(), MusicMediaService.class));
    }

    @Override
    public void onPause() {
        mediaSessionCompat.setActive(false);
        stopForeground(true);
    }

    @Override
    public void onStop() {
        stopSelf();
    }

    @Override
    public void onPlayBackStateChange(PlaybackStateCompat state) {
        mediaSessionCompat.setPlaybackState(state);
        if(state.getState()==PlaybackStateCompat.STATE_PLAYING || state.getState() == PlaybackStateCompat.STATE_PAUSED){
            notificationService.startNotification();
        }
    }

    @Override
    public void onMetadataChange(MediaMetadataCompat mediaMetadataCompat) {
        mediaSessionCompat.setMetadata(mediaMetadataCompat);
    }


    public MediaSessionCompat getMediaSessionCompat() {
        return mediaSessionCompat;
    }

    @Override
    public void onQueueCreated(List<MediaSessionCompat.QueueItem> queue) {
        mediaSessionCompat.setQueue(queue);
    }
}
