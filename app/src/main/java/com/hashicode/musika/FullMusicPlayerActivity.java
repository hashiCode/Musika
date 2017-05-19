package com.hashicode.musika;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hashicode.musika.service.MusicMediaService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by takahashi on 4/18/17.
 */

public class FullMusicPlayerActivity extends AppCompatActivity {


    private ViewPager albumViewPager;
    private MediaBrowserCompat mediaBrowserCompat;
    private AlbumViewPagerOnPageChangeListener onPageChangeListener;

    private ImageButton playPauseButton, skipPrevious, skipNext;
    private TextView textViewMusicName;
    private TextView textViewArtistName;

    private SeekBar seekBar;
    private TextView startText;
    private TextView endText;

    private MediaMetadataCompat mediaMetadata;
    private PlaybackStateCompat playbackState;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture scheduledFuture;
    private Handler handler = new Handler();
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullmusic_activity);
        Bundle extras = getIntent().getExtras();
        textViewMusicName = (TextView) findViewById(R.id.music_name);
        textViewArtistName = (TextView) findViewById(R.id.artist_name);
        albumViewPager = (ViewPager) findViewById(R.id.album_slider);

        this.skipPrevious = (ImageButton) findViewById(R.id.skip_prev);
        this.skipNext = (ImageButton) findViewById(R.id.skip_next);
        this.playPauseButton = (ImageButton) findViewById(R.id.play_pause);

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        startText = (TextView) findViewById(R.id.startText);
        endText = (TextView) findViewById(R.id.endText);

        setupClickListener();

        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MusicMediaService.class), mediaBrowseCallbak,null);
        setupInitialContent(extras);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                startText.setText(DateUtils.formatElapsedTime(progress/1000));
                long totalDUrarion = mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                endText.setText(DateUtils.formatElapsedTime((totalDUrarion-progress)/1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                unscheduleSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(FullMusicPlayerActivity.this);
                if(mediaController!=null){
                    mediaController.getTransportControls().seekTo(seekBar.getProgress());
                }
                scheduleSeekbarUpdate();
            }
        });

    }

    private void setupInitialContent(Bundle extras) {
        textViewMusicName.setText(extras.getString(PlaybackFragment.KEY_CURRENT_MEDIA_NAME));
        textViewArtistName.setText(extras.getString(PlaybackFragment.KEY_CURRENT_ARTIST_NAME));

    }

    private void setupClickListener() {
        SkipButtonClikListener skipButtonClikListener = new SkipButtonClikListener(this);
        skipPrevious.setOnClickListener(skipButtonClikListener);
        skipNext.setOnClickListener(skipButtonClikListener);
        playPauseButton.setOnClickListener(new PlayPauseClickListener(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserCompat.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(this);
        if(mediaControllerCompat!=null){
            mediaControllerCompat.unregisterCallback(mCallback);
        }
        if(onPageChangeListener!=null) {
            albumViewPager.removeOnPageChangeListener(onPageChangeListener);
        }
        mediaBrowserCompat.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unscheduleSeekbarUpdate();
        scheduledExecutorService.shutdown();
    }

    private final MediaBrowserCompat.ConnectionCallback mediaBrowseCallbak =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(this.getClass().getSimpleName(), "connected");
                    connectToMediaSession(mediaBrowserCompat.getSessionToken());
                }
            };


    private void connectToMediaSession(MediaSessionCompat.Token sessionToken) {
        try {
            MediaControllerCompat mediaController = new MediaControllerCompat(this, sessionToken);
            MediaControllerCompat.setMediaController(this, mediaController);
            mediaController.registerCallback(mCallback);
            MediaMetadataCompat metadata = mediaController.getMetadata();
            PlaybackStateCompat playbackState = mediaController.getPlaybackState();
            if(metadata!=null){
                setupAlbumViewPager(mediaController, metadata);
                onMetadataChange(metadata);
                onPlaybackStateChange(playbackState);
                updateMetadataPosition(metadata, playbackState);
            }
            else{
                finish();
            }
        } catch (RemoteException e) {
            Log.e(this.getLocalClassName(), "Error connection to connect to media session",e);
        }
    }

    private void updateMetadataPosition(MediaMetadataCompat metadata, PlaybackStateCompat playbackState) {
        long totalDuration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        seekBar.setMax((int)totalDuration);
        long currentPosition = playbackState.getPosition();
        long remainingDuration = totalDuration - currentPosition;
        startText.setText(DateUtils.formatElapsedTime(currentPosition/1000));
        endText.setText(DateUtils.formatElapsedTime(remainingDuration/1000));
        seekBar.setProgress((int) currentPosition);
    }

    private void setupAlbumViewPager(MediaControllerCompat mediaController, MediaMetadataCompat metadata) {
        List<MediaSessionCompat.QueueItem> queue =mediaController.getQueue();
        albumViewPager.setAdapter(new AlbumSliderAdapter(getSupportFragmentManager(),queue));
        int currentQueueItem = getCurrentQueueItem(queue, metadata.getDescription().getMediaId());
        albumViewPager.setCurrentItem(currentQueueItem);
        onPageChangeListener = new AlbumViewPagerOnPageChangeListener(mediaController);
        albumViewPager.addOnPageChangeListener(onPageChangeListener);

    }

    private int getCurrentQueueItem(List<MediaSessionCompat.QueueItem> queue, String mediaId) {
        for(int i=0; i<queue.size(); i++){
            MediaSessionCompat.QueueItem queueItem = queue.get(i);
            if(queueItem.getDescription().getMediaId().equals(mediaId)){
                return i;
            }
        }
        Log.e(this.getClass().getSimpleName(), "Should not happen. Return 0");
        return 0;
    }


    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            Log.d(this.getClass().getName(), "Received playback state change to state " + state.getState());
            onPlaybackStateChange(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) {
                return;
            }
            Log.d(this.getClass().getName(), "Received metadata state change to mediaId="+
                    metadata.getDescription().getMediaId() +
                    " song="+metadata.getDescription().getTitle());
            onMetadataChange(metadata);
        }
    };

    private void onMetadataChange(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        this.mediaMetadata = metadata;
        textViewMusicName.setText(metadata.getDescription().getTitle());
        textViewArtistName.setText(metadata.getDescription().getSubtitle());
        int currentQueueItem = getCurrentQueueItem(MediaControllerCompat.getMediaController(this).getQueue(),
                metadata.getDescription().getMediaId());
        int albumCurrentItem = albumViewPager.getCurrentItem();
        if(currentQueueItem!=albumCurrentItem) {
            albumViewPager.setCurrentItem(currentQueueItem);
        }
        long totalDuration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        seekBar.setMax((int)totalDuration);
        seekBar.setProgress(0);
        startText.setText(DateUtils.formatElapsedTime(0));
        endText.setText(DateUtils.formatElapsedTime(totalDuration/1000));
    }

    private void onPlaybackStateChange(PlaybackStateCompat playbackState) {
        if (playbackState == null) {
            return;
        }
        this.playbackState = playbackState;
        boolean enablePlay = false;
        switch (playbackState.getState()) {
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
                enablePlay = true;
                updateMediaProgress();
                unscheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_ERROR:
                Toast.makeText(this, playbackState.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                scheduleSeekbarUpdate();
                break;
        }

        if (enablePlay) {
            playPauseButton.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_white_48dp));
        } else {
            playPauseButton.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_pause_white_48dp));
        }
    }

    private class SkipButtonClikListener implements View.OnClickListener{

        public FullMusicPlayerActivity fullMusicPlayerActivity;

        public SkipButtonClikListener(FullMusicPlayerActivity fullMusicPlayerActivity) {
            this.fullMusicPlayerActivity = fullMusicPlayerActivity;
        }

        @Override
        public void onClick(View v) {
            MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(fullMusicPlayerActivity);
            if(mediaController!=null) {
                PlaybackStateCompat playbackState = mediaController.getPlaybackState();
                final int state = playbackState == null ?
                        PlaybackStateCompat.STATE_NONE : playbackState.getState();
                if (state == PlaybackStateCompat.STATE_PAUSED ||
                        state == PlaybackStateCompat.STATE_PLAYING ||
                        state == PlaybackStateCompat.STATE_BUFFERING) {
                    if(v.getId() == R.id.skip_next){
                        mediaController.getTransportControls().skipToNext();
                    }
                    else{
                        mediaController.getTransportControls().skipToPrevious();
                    }
                }

            }
        }
    }

    private final Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            updateMediaProgress();
        }
    };

    private void updateMediaProgress() {
        if(playbackState==null){
            return;
        }
        long currentPosition = playbackState.getPosition();
        if(playbackState.getState()!=PlaybackStateCompat.STATE_PAUSED){
            long delta =SystemClock.elapsedRealtime() -playbackState.getLastPositionUpdateTime();
            currentPosition+= (delta * playbackState.getPlaybackSpeed());
        }
        seekBar.setProgress((int) currentPosition);
    }

    public void scheduleSeekbarUpdate(){
        unscheduleSeekbarUpdate();
        if(!scheduledExecutorService.isShutdown()){
            scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    handler.post(updateProgress);
                }
            }, PROGRESS_UPDATE_INITIAL_INTERVAL, PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    public void unscheduleSeekbarUpdate(){
        if(scheduledFuture!=null){
            scheduledFuture.cancel(false);
        }
    }


}
