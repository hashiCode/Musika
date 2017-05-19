package com.hashicode.musika;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by takahashi on 1/12/17.
 */
public class MediaViewHolder extends RecyclerView.ViewHolder{

    private ImageView imageView;
    private TextView artistInitialTextView;
    private TextView primaryTextView;
    private TextView secondayTextView;

    public MediaViewHolder(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
        primaryTextView = (TextView) itemView.findViewById(R.id.primaryText);
        secondayTextView = (TextView) itemView.findViewById(R.id.secondaryText);
        artistInitialTextView = (TextView) itemView.findViewById(R.id.artist_initial);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public TextView getPrimaryTextView() {
        return primaryTextView;
    }

    public TextView getSecondayTextView() {
        return secondayTextView;
    }

    public TextView getArtistInitialTextView() {
        return artistInitialTextView;
    }

}
