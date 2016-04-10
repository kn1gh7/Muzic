package com.knight.knight.muzic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.knight.knight.muzic.R;
import com.knight.knight.muzic.model.MusicItemModel;
import com.knight.knight.muzic.viewholder.MusicListViewHolder;

import java.util.List;

/**
 * Created by kn1gh7 on 9/4/16.
 */
public class MusicListAdapter extends RecyclerView.Adapter<MusicListViewHolder> {
    Context context;
    List<MusicItemModel> musicList;

    public MusicListAdapter(Context context, List<MusicItemModel> musicList) {
        this.context = context;
        this.musicList = musicList;
    }

    @Override
    public MusicListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MusicListViewHolder musicViewHolder = new MusicListViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.music_item_layout, null));
        return musicViewHolder;
    }

    @Override
    public void onBindViewHolder(MusicListViewHolder holder, int position) {
        MusicItemModel musicItem = musicList.get(position);
        holder.title.setText(musicItem.getTitle());
        holder.displayName.setText(musicItem.getDisplayName());
        holder.albumName.setText(musicItem.getAlbumName());
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }
}
