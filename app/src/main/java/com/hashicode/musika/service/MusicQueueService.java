package com.hashicode.musika.service;

import android.graphics.Bitmap;
import android.support.v4.media.session.MediaSessionCompat;

import com.hashicode.musika.MediaMetadataWrapper;
import com.hashicode.musika.provider.MediaIdUtil;
import com.hashicode.musika.provider.MusicProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by takahashi on 2/1/17.
 */
public class MusicQueueService {

    private MusicProvider musicProvider;
    private MusicQueueCallback callback;
    private List<MediaSessionCompat.QueueItem> queueItems = new ArrayList<>();
    private int currentMediaIndex;

    public MusicQueueService(MusicProvider musicProvider, MusicQueueCallback callback) {
        this.musicProvider = musicProvider;
        currentMediaIndex = 0;
        this.callback  = callback;
    }

    public void createQueueFromMediaId(String mediaId){
        String[] categories = MediaIdUtil.getCategories(mediaId);
        //musics
        if(categories.length==1){
            Collection<MediaMetadataWrapper> musics = musicProvider.getMusics();
            makeQueue(musics);
        }else{  //artists
            String artist= categories[1];
            List<MediaMetadataWrapper> musicsByArtist = musicProvider.getMusicsByArtist(artist);
            makeQueue(musicsByArtist);
        }
        setInitialIndex(mediaId);
        callback.onQueueCreated(this.queueItems);
    }

    private void setInitialIndex(String mediaId) {
        String pureMediaId = MediaIdUtil.getLeaf(mediaId);
        for(int i=0; i<queueItems.size(); i++){
            MediaSessionCompat.QueueItem item = queueItems.get(i);
            String itemMediaId = item.getDescription().getMediaId();
            if(pureMediaId.equals(itemMediaId)){
                currentMediaIndex = i;
                break;
            }

        }
    }

    private void makeQueue(Collection<MediaMetadataWrapper> medias) {
        if(medias!=null){
            this.queueItems.clear();
            int id=0;
            for(MediaMetadataWrapper media : medias){
                MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(media.getMediaMetadataCompat().getDescription(),id++);
                this.queueItems.add(item);
            }
        }
    }


    public MediaMetadataWrapper getCurrentMediaMetadata(){
        MediaSessionCompat.QueueItem queueItem = queueItems.get(currentMediaIndex);
        if(queueItem!=null){
            String mediaId = queueItem.getDescription().getMediaId();
            return musicProvider.getMusicByMediaId(mediaId);
        }
        return null;
    }

    public void updateAlbumArt(Bitmap albumArt, Bitmap albumIcon, String mediaId) {
        musicProvider.updateAlbumArt(albumArt, albumIcon, mediaId);
    }


    public void skipNext(){
        if(currentMediaIndex<queueItems.size()-1){
            currentMediaIndex +=1;
        }
        else{
            currentMediaIndex = 0;
        }
    }

    public void skipPrevious(){
        if(currentMediaIndex>0){
            currentMediaIndex -=1;
        }
        else{
            currentMediaIndex = queueItems.size()-1;
        }
    }


}
