package com.hashicode.musika;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

/**
 * Created by takahashi on 1/4/17.
 */

public class PlaybackFragment extends Fragment {

    public static final String KEY_CURRENT_MEDIA_NAME = "currentMediaName";
    public static final String KEY_CURRENT_ARTIST_NAME = "currentArtistName";
    public static final String KEY_CURRENT_ALBUM_ICON_URI = "currentAlbumIcon";

    private ImageButton playPauseButton;
    private TextView titleTextView;
    private TextView artisitTextView;
    private ImageView albumImageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playback_fragment, container, false);
        playPauseButton = (ImageButton) rootView.findViewById(R.id.play_pause);
        titleTextView = (TextView) rootView.findViewById(R.id.title);
        artisitTextView = (TextView) rootView.findViewById(R.id.artist);
        albumImageView = (ImageView) rootView.findViewById(R.id.album_art);
        playPauseButton.setOnClickListener(new PlayPauseClickListener(getActivity()));

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(getActivity());
                if(mediaController!=null){
                    MediaMetadataCompat metadata = mediaController.getMetadata();
                    if(metadata!=null){
                        Intent intent = new Intent(getActivity(), FullMusicPlayerActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(KEY_CURRENT_MEDIA_NAME, metadata.getDescription().getTitle());
                        intent.putExtra(KEY_CURRENT_ARTIST_NAME, metadata.getDescription().getSubtitle());
                        intent.putExtra(KEY_CURRENT_ALBUM_ICON_URI,  metadata.getDescription().getIconUri()!= null ?
                                metadata.getDescription().getIconUri().toString(): null);

                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }

                }


            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(this.getClass().getName(), "fragment.onStart");
        connect();

    }

    private void onPlaybackStateChange(PlaybackStateCompat playbackState) {
        if (playbackState == null) {
            return;
        }
        if (getActivity() == null) {
            Log.w(this.getClass().getSimpleName(), "Activity is null. Ignoring");
            return;
        }
        boolean enablePlay = false;
        switch (playbackState.getState()) {
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
                enablePlay = true;
                break;
            case PlaybackStateCompat.STATE_ERROR:
                Toast.makeText(getActivity(), playbackState.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
        }

        if (enablePlay) {
            playPauseButton.setImageDrawable(
                    ContextCompat.getDrawable(getActivity(), R.drawable.ic_play_arrow_black_36dp));
        } else {
            playPauseButton.setImageDrawable(
                    ContextCompat.getDrawable(getActivity(), R.drawable.ic_pause_white_36dp));
        }
    }

    private void onMetadataChange(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        titleTextView.setText(metadata.getDescription().getTitle());
        artisitTextView.setText(metadata.getDescription().getSubtitle());
        if (metadata.getDescription().getIconUri() != null) {
            Uri iconUri = metadata.getDescription().getIconUri();
            AlbumCache albumCache = AlbumCache.getInstance();
            Bitmap albumArt = albumCache.getAlbumArt(iconUri.getPath());
            if(albumArt!=null){
                albumImageView.setImageBitmap(albumArt);
            }
            else{
                albumCache.fetchAlbumArt(iconUri.getPath(), new AlbumCache.AlbumCacheCallback() {
                    @Override
                    public void onAlbumFetched(Pair<Bitmap, Bitmap> bitmap) {
                        albumImageView.setImageBitmap(bitmap.second);
                    }
                });
            }
        }else{
            albumImageView.setImageResource(R.drawable.ic_music_note_white_24dp);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(this.getClass().getName(), "fragment.stop");
        MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(getActivity());
        if (mediaControllerCompat != null) {
            mediaControllerCompat.unregisterCallback(mCallback);
        }
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


    public void connect() {
        MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(getActivity());
        if (mediaControllerCompat != null) {
            onMetadataChange(mediaControllerCompat.getMetadata());
            onPlaybackStateChange(mediaControllerCompat.getPlaybackState());
            mediaControllerCompat.registerCallback(mCallback);
        }
    }
}
