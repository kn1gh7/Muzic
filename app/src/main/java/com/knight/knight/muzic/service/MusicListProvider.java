package com.knight.knight.muzic.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.util.Log;
import android.view.View;

import com.knight.knight.muzic.model.MusicItemModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kn1gh7 on 11/6/16.
 */

public class MusicListProvider {
    Context context;
    private List<MediaBrowserCompat.MediaItem> completeMusicList;

    public MusicListProvider(Context context) {
        this.context = context;
        completeMusicList = new ArrayList<MediaBrowserCompat.MediaItem>();
    }

    public List<MediaBrowserCompat.MediaItem> getMusicList() {
        ContentResolver contentResolver = context.getContentResolver();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.i("URI", uri.toString());
        Cursor cursor = contentResolver.query(uri, null, selection, null, null);
        if (cursor == null) {
            // query failed, handle error.
            /*errorView.setText("Cursor returned Null");
            errorView.setVisibility(View.VISIBLE);
            musiclist_recyclerView.setVisibility(View.GONE);*/
            completeMusicList = null;
        } else if (!cursor.moveToFirst()) {
            // no media on the device
            /*errorView.setText("No Items Found");
            errorView.setVisibility(View.VISIBLE);
            musiclist_recyclerView.setVisibility(View.GONE);*/
            completeMusicList = null;
        } else {
            completeMusicList.clear();
            int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int displayNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int albumNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

            do {
                long thisId = cursor.getLong(idColumn);
                String thisTitle = cursor.getString(titleColumn);
                String displayName = cursor.getString(displayNameColumn);
                String albumName = cursor.getString(albumNameColumn);
                String albumID = cursor.getString(idColumn);
                MusicItemModel musicItemModel = new MusicItemModel();
                musicItemModel.setTitle(thisTitle);
                musicItemModel.setDisplayName(displayName);
                musicItemModel.setAlbumName(albumName);
                musicItemModel.setAlbumId(albumID);

                MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                        .setMediaId(albumID)
                        .setTitle(thisTitle)
                        .setSubtitle(albumName)
                        .setDescription(displayName)
                        .build();
                completeMusicList.add(new MediaBrowserCompat.MediaItem(mediaDescription,
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
                // ...process entry...
            } while (cursor.moveToNext());

            /*errorView.setText("");
            errorView.setVisibility(View.GONE);
            musiclist_recyclerView.setVisibility(View.VISIBLE);*/
        }

        return completeMusicList;
    }
}
