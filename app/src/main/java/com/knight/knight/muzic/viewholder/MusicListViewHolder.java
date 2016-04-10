package com.knight.knight.muzic.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.knight.knight.muzic.R;

/**
 * Created by kn1gh7 on 9/4/16.
 */
public class MusicListViewHolder extends RecyclerView.ViewHolder {
    public TextView title, displayName, albumName;
    public MusicListViewHolder(View view) {
        super(view);
        title = (TextView) view.findViewById(R.id.title);
        displayName = (TextView) view.findViewById(R.id.display_name);
        albumName = (TextView) view.findViewById(R.id.album_name);
    }
}
