package com.hashicode.musika;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hashicode.musika.provider.MediaIdUtil;

import java.util.List;

/**
 * Created by takahashi on 1/12/17.
 */

public class BrowserFragment extends Fragment {

    public static final String BUNDLE_MEDIA_ID = "mediaId";
    private RecyclerViewBrowseAdapter recyclerViewBrowseAdapter;
    private String mediaId;

    public String getMediaId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(BUNDLE_MEDIA_ID);
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browse_fragment, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.browse_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewBrowseAdapter = new RecyclerViewBrowseAdapter(this.getMusicActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this.getActivity(), linearLayoutManager.getOrientation()));
        recyclerView.setAdapter(recyclerViewBrowseAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        MediaBrowserCompat mediaBrowser = getMusicActivity().getMediaBrowser();
        mediaId = getMediaId();

        if (mediaBrowser.isConnected()) {
            connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserCompat mediaBrowser = getMusicActivity().getMediaBrowser();
        if(mediaBrowser!=null && mediaBrowser.isConnected() && mediaId!=null) {
            mediaBrowser.unsubscribe(mediaId);
        }
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(getMusicActivity());
        if(mediaController!=null){
            mediaController.unregisterCallback(mMediaControllerCallback);
        }
    }

    public MusicActivity getMusicActivity(){
        return (MusicActivity) getActivity();
    }

    public void connect() {
        if (isDetached()) {
            return;
        }
        MediaBrowserCompat mediaBrowser = getMusicActivity().getMediaBrowser();
        if (mediaId == null) {
            mediaId = mediaBrowser.getRoot();
        }
        updateToolbar(mediaBrowser, mediaId);
        prepareMediaBrowser(mediaBrowser, mediaId);

    }

    private void updateToolbar(MediaBrowserCompat mediaBrowser, String mediaId) {
        if (MediaIdUtil.ROOT_MEDIA_ID.equals(mediaId)) {
            getMusicActivity().getToolbar().setTitle(getResources().getString(R.string.app_name));
            return;
        }
       mediaBrowser.getItem(mediaId, new MediaBrowserCompat.ItemCallback() {
           @Override
           public void onItemLoaded(MediaBrowserCompat.MediaItem item) {
               Toolbar toolbar = getMusicActivity().getToolbar();
               toolbar.setTitle(item.getDescription().getTitle());
           }
       });
    }

    private void prepareMediaBrowser(MediaBrowserCompat mediaBrowser, String mediaId) {
        mediaBrowser.unsubscribe(mediaId);
        mediaBrowser.subscribe(mediaId, subscriptionCallback);

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(getMusicActivity());
        if(mediaController!=null){
            mediaController.registerCallback(mMediaControllerCallback);
        }
    }

    private final MediaBrowserCompat.SubscriptionCallback subscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        Log.d(this.getClass().getName() , "fragment onChildrenLoaded, parentId=" + parentId +
                                "  count=" + children.size());
                        recyclerViewBrowseAdapter.getMediaItemList().clear();
                        for (MediaBrowserCompat.MediaItem item : children) {
                            recyclerViewBrowseAdapter.getMediaItemList().add(item);
                        }
                        recyclerViewBrowseAdapter.notifyDataSetChanged();
                    } catch (Throwable t) {
                        Log.e(this.getClass().getName(), "Error on childrenloaded", t);
                    }
                }

            };

    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    super.onMetadataChanged(metadata);
                    if (metadata == null) {
                        return;
                    }
                    Log.d(this.getClass().getName(), "Received metadata change to media "+
                            metadata.getDescription().getMediaId());
                    recyclerViewBrowseAdapter.notifyDataSetChanged();
                }

                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                    super.onPlaybackStateChanged(state);
                    Log.d(this.getClass().getName(), "Received state change: "+ state);
                    recyclerViewBrowseAdapter.notifyDataSetChanged();
                }
            };
}
