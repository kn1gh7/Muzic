package com.knight.knight.muzic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.knight.knight.muzic.R;
import com.knight.knight.muzic.callbackInterfaces.MusicItemClicked;
import com.knight.knight.muzic.model.MusicItemModel;
import com.knight.knight.muzic.viewholder.MusicListViewHolder;

import java.util.List;

/**
 * Created by kn1gh7 on 9/4/16.
 */
public class MusicListAdapter extends RecyclerView.Adapter<MusicListViewHolder> {
    private static final int INVALID_POSITION = -1;
    Context context;
    MusicItemClicked startMusicCallback;
    List<MediaBrowserCompat.MediaItem> musicList;
    int lastPlayingPosition;

    public MusicListAdapter(Context context,
                            MusicItemClicked startMusicCallback,
                            List<MediaBrowserCompat.MediaItem> musicList) {
        this.context = context;
        this.musicList = musicList;
        this.startMusicCallback = startMusicCallback;
        lastPlayingPosition = INVALID_POSITION;
    }

    @Override
    public MusicListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MusicListViewHolder musicViewHolder = new MusicListViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.music_item_layout, null));

        return musicViewHolder;
    }

    @Override
    public void onBindViewHolder(final MusicListViewHolder holder, final int position) {
        final MediaBrowserCompat.MediaItem musicItem = musicList.get(position);
        MediaDescriptionCompat mDescription = musicItem.getDescription();
        holder.title.setText(musicItem.getDescription().getTitle());
        holder.displayName.setText(mDescription.getDescription());
        holder.albumName.setText(mDescription.getSubtitle());
        //holder.music_item_parent.setActivated(musicItem.isPlayable());

        /*holder.music_item_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastPlayingPosition != INVALID_POSITION) {
                    musicList.get(lastPlayingPosition).setPlaying(false);
                    MusicListAdapter.this.notifyItemChanged(lastPlayingPosition);
                }
                if (position != lastPlayingPosition)
                    musicItem.setPlaying(!musicItem.isPlaying());

                lastPlayingPosition = position;
                startMusicCallback.onMusicItemClicked(musicItem.getAlbumId());
                MusicListAdapter.this.notifyItemChanged(position);
            }
        });*/

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] rawArt;
        Bitmap art;
        BitmapFactory.Options bfo = new BitmapFactory.Options();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mmr.setDataSource(context.getApplicationContext(),
                Uri.parse(uri + "/" + musicItem.getDescription().getMediaId()));
        rawArt = mmr.getEmbeddedPicture();

        // if rawArt is null then no cover art is embedded in the file or is not
        // recognized as such.
        if (null != rawArt) {
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);

            holder.albumPhoto.setImageBitmap(art);
            Log.i("Art ", "Not Null");
        } else {
            Log.i("Art ", "Null");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.albumPhoto.setImageDrawable(context.getDrawable(R.drawable.dummy));
            }
        }
        // Code that uses the cover art retrieved below.

    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }
}
