package com.hashicode.musika.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;

import com.hashicode.musika.AlbumCache;
import com.hashicode.musika.MusicActivity;
import com.hashicode.musika.R;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

/**
 * Created by takahashi on 2/13/17.
 */

public class NotificationService extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 204;
    private static final int REQUEST_CODE = 2206;

    private MusicMediaService musicMediaService;
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.TransportControls transportControls;
    private NotificationManagerCompat notificationManagerCompat;

    private final String PAUSE_ACTION = "pauseAction";
    private final String PLAY_ACTION = "playAction";
    private final String NEXT_ACTION = "nextAction";
    private final String PREVIOUS_ACTION = "previousAction";

    private PlaybackStateCompat playbackState;
    private MediaMetadataCompat mediaMetadata;

    private PendingIntent pauseIntent;
    private PendingIntent playIntent;
    private PendingIntent nextIntent;
    private PendingIntent previousIntent;

    private boolean started = false;

    public NotificationService(MusicMediaService musicMediaService){
        this.musicMediaService = musicMediaService;
        mediaController = new MediaControllerCompat(musicMediaService, musicMediaService.getMediaSessionCompat());
        transportControls = mediaController.getTransportControls();

        this.notificationManagerCompat = NotificationManagerCompat.from(musicMediaService);
        pauseIntent = PendingIntent.getBroadcast(musicMediaService,REQUEST_CODE, new Intent(PAUSE_ACTION),PendingIntent.FLAG_CANCEL_CURRENT);
        playIntent = PendingIntent.getBroadcast(musicMediaService,REQUEST_CODE, new Intent(PLAY_ACTION),PendingIntent.FLAG_CANCEL_CURRENT);
        nextIntent = PendingIntent.getBroadcast(musicMediaService,REQUEST_CODE, new Intent(NEXT_ACTION),PendingIntent.FLAG_CANCEL_CURRENT);
        previousIntent = PendingIntent.getBroadcast(musicMediaService,REQUEST_CODE, new Intent(PREVIOUS_ACTION),PendingIntent.FLAG_CANCEL_CURRENT);

        notificationManagerCompat.cancelAll();
    }

    private final MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            playbackState = state;
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                    state.getState() == PlaybackStateCompat.STATE_NONE) {
                stopNotification();
            } else {
                Notification notification = createNotification();
                if (notification != null) {
                    notificationManagerCompat.notify(NOTIFICATION_ID, notification);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mediaMetadata = metadata;
            Notification notification = createNotification();
            if (notification != null) {
                notificationManagerCompat.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();

            mediaController.unregisterCallback(callback);
        }
    };

    public void stopNotification() {
        if(started) {
            started = false;
            mediaController.unregisterCallback(callback);
            try {
                notificationManagerCompat.cancel(NOTIFICATION_ID);
                musicMediaService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            musicMediaService.stopForeground(true);
        }
    }

    public void startNotification(){
        if(!started) {

            mediaMetadata = mediaController.getMetadata();
            playbackState = mediaController.getPlaybackState();
            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                started=true;
                mediaController.registerCallback(callback);
                IntentFilter filter = new IntentFilter();
                filter.addAction(PLAY_ACTION);
                filter.addAction(PAUSE_ACTION);
                filter.addAction(PREVIOUS_ACTION);
                filter.addAction(NEXT_ACTION);
                musicMediaService.registerReceiver(this, filter);

                musicMediaService.startForeground(NOTIFICATION_ID, notification);
            }
        }
    }

    private Notification createNotification() {
        if(playbackState== null || mediaMetadata == null){
            return null;
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(musicMediaService);
        addActions(notificationBuilder);

        MediaDescriptionCompat description = mediaMetadata.getDescription();

        notificationBuilder.setStyle(new NotificationCompat.MediaStyle()
                .setMediaSession(musicMediaService.getSessionToken()).setShowActionsInCompactView(0,1,2));

        //album art
        Bitmap albumArt=null;
        if(description.getIconUri()!=null){
            AlbumCache albumCache = AlbumCache.getInstance();
            albumArt = albumCache.getLargeAlbumArt(description.getIconUri().getPath());
            if(albumArt==null){
                fetchAlbumAndSetNotification(notificationBuilder, albumCache, description.getIconUri().getPath());
            }
        }
        if(albumArt!=null){
            notificationBuilder.setLargeIcon(albumArt);
        }
        else{
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(musicMediaService.getResources(), R.drawable.ic_music_note_white_48dp));
        }

        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSmallIcon(R.drawable.ic_music_note_white_24dp);
        setNotificationPlaybackState(notificationBuilder);

        return notificationBuilder.build();
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        if (playbackState == null) {
            musicMediaService.stopForeground(true);
            return;
        }
        if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING
                && playbackState.getPosition() >= 0) {
            builder
                    .setWhen(System.currentTimeMillis() - playbackState.getPosition())
                    .setShowWhen(true)
                    .setUsesChronometer(true);
        } else {
            builder
                    .setWhen(0)
                    .setShowWhen(false)
                    .setUsesChronometer(false);
        }

        builder.setOngoing(playbackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    private void fetchAlbumAndSetNotification(final NotificationCompat.Builder notificationBuilder, AlbumCache albumCache, String path) {
        albumCache.fetchAlbumArt(path, new AlbumCache.AlbumCacheCallback() {
            @Override
            public void onAlbumFetched(Pair<Bitmap, Bitmap> bitmap) {
                notificationBuilder.setLargeIcon(bitmap.second);
            }
        });
    }

    private void addActions(NotificationCompat.Builder notificationBuilder) {
        Intent intent = new Intent(musicMediaService.getApplicationContext(), MusicActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(musicMediaService.getApplicationContext(),0,intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
        notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,"", previousIntent);
        PendingIntent playOrPause;
        int icon;
        if(playbackState.getState() == PlaybackStateCompat.STATE_PLAYING || playbackState.getState()==PlaybackStateCompat.STATE_BUFFERING){
            playOrPause = pauseIntent;
            icon = R.drawable.ic_pause_white_24dp;
        }else{
            playOrPause = playIntent;
            icon = R.drawable.ic_play_arrow_white_24dp;
        }
        notificationBuilder.addAction(icon,"", playOrPause);


        notificationBuilder.addAction(R.drawable.ic_skip_next_white_24dp,"", nextIntent);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case PAUSE_ACTION:
                transportControls.pause();
                break;
            case PLAY_ACTION:
                transportControls.play();
                break;
            case NEXT_ACTION :
                transportControls.skipToNext();
                break;
            case PREVIOUS_ACTION :
                transportControls.skipToPrevious();
                break;
            default:
                Log.w(this.getClass().getName(), "Unknown intent ignored. Action="+action);
        }
    }
}
