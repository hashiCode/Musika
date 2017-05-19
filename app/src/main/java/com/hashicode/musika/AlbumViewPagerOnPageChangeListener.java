package com.hashicode.musika;

import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

/**
 * Created by takahashi on 5/2/17.
 */

public class AlbumViewPagerOnPageChangeListener implements OnPageChangeListener {

    private MediaControllerCompat mediaController;
    private int currentPage=0;
    private int previousState=0;

    private boolean enableControls = false;


    public AlbumViewPagerOnPageChangeListener(MediaControllerCompat mediaController) {
        this.mediaController = mediaController;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        currentPage = position;

    }

    @Override
    public void onPageSelected(int position) {
        if(enableControls) {
            if (position > currentPage) {
                mediaController.getTransportControls().skipToNext();
            } else {
                mediaController.getTransportControls().skipToPrevious();
            }
            enableControls=false;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //user interacting, so enable controls
        if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                && state == ViewPager.SCROLL_STATE_SETTLING){
            enableControls=true;
        }
        else{
            enableControls = false;
        }
        previousState=state;

    }
}
