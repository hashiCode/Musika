package com.hashicode.musika;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hashicode.musika.provider.MediaIdUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by takahashi on 1/12/17.
 */

public class RecyclerViewBrowseAdapter extends RecyclerView.Adapter<MediaViewHolder> {

    private List<MediaBrowserCompat.MediaItem> mediaItemList = new ArrayList<>();
    private MediaItemCallback mediaItemCallback;
    private AlbumCache albumCache;

    public RecyclerViewBrowseAdapter(MediaItemCallback callBack){
        mediaItemCallback = callBack;
        albumCache = AlbumCache.getInstance();
    }

    public List<MediaBrowserCompat.MediaItem> getMediaItemList() {
        return mediaItemList;
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item, parent, false);
        return new MediaViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final MediaViewHolder holder, final int position) {
        final MediaBrowserCompat.MediaItem mediaItem = mediaItemList.get(position);
        MediaDescriptionCompat description = mediaItem.getDescription();
        setIcon(holder, mediaItem);
        //todo do the bind
        holder.getPrimaryTextView().setText(description.getTitle());
        holder.getSecondayTextView().setText(description.getSubtitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaItemCallback.onMediaItemSelected(mediaItem);
            }
        });
    }

    private void setIcon(final MediaViewHolder holder, MediaBrowserCompat.MediaItem mediaItem) {
        MediaDescriptionCompat description = mediaItem.getDescription();
        String mediaId = mediaItem.getMediaId();
        String mediaType = MediaIdUtil.getMediaType(mediaId);
        switch (mediaType){
            case MediaIdUtil.MUSICS_TYPE :
            case MediaIdUtil.ARTISTS_TYPE :
                holder.getImageView().setVisibility(View.VISIBLE);
                holder.getArtistInitialTextView().setVisibility(View.GONE);
                holder.getImageView().setImageURI(description.getIconUri());
                break;
            case MediaIdUtil.ARTIST_TYPE:
                holder.getImageView().setVisibility(View.GONE);
                holder.getArtistInitialTextView().setVisibility(View.VISIBLE);
                holder.getArtistInitialTextView().setText(String.valueOf(description.getTitle().charAt(0)));
                break;
            case MediaIdUtil.MUSIC_TYPE :
                holder.getImageView().setVisibility(View.VISIBLE);
                holder.getArtistInitialTextView().setVisibility(View.GONE);
                Uri uriIcon = description.getIconUri();
                if(uriIcon!=null) {
                    Bitmap albumArt = albumCache.getAlbumArt(uriIcon.getPath());
                    if (albumArt != null) {
                        holder.getImageView().setImageBitmap(albumArt);
                    } else {
                        albumCache.fetchAlbumArt(uriIcon.getPath(), new AlbumCache.AlbumCacheCallback() {
                            @Override
                            public void onAlbumFetched(Pair<Bitmap, Bitmap> bitmap) {
                                if (bitmap != null) {
                                    holder.getImageView().setImageBitmap(bitmap.first);
                                }
                            }
                        });
                    }
                }else{
                    holder.getImageView().setImageResource(R.drawable.ic_music_note_white_24dp);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mediaItemList.size();
    }

}
