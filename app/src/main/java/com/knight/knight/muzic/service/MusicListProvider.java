package com.knight.knight.muzic.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.knight.knight.muzic.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kn1gh7 on 11/6/16.
 */

public class MusicListProvider {
    Context context;
    private List<MediaMetadataCompat> completeMusicList;
    private HashMap<String, MediaMetadataCompat> musicListByMediaId;
    private List<MediaBrowserCompat.MediaItem> myPlayList;
    boolean isListPrepared;

    public MusicListProvider(Context context) {
        this.context = context;
        completeMusicList = new ArrayList<MediaMetadataCompat>();
        musicListByMediaId = new HashMap<>();
    }

    public void createMusicList() {
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
        } else if (!cursor.moveToFirst()) { /* no media on the device errorView.setText("No Items Found");*/
            /*errorView.setVisibility(View.VISIBLE);
            musiclist_recyclerView.setVisibility(View.GONE);*/
            completeMusicList = null;
        } else {
            completeMusicList.clear();
            int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int displayNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int albumNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int composerColumn = cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER);

            do {
                long thisId = cursor.getLong(idColumn);
                String thisTitle = cursor.getString(titleColumn);
                String displayName = cursor.getString(displayNameColumn);
                String albumName = cursor.getString(albumNameColumn);
                String albumID = cursor.getString(idColumn);
                long duration = cursor.getLong(durationColumn);
                String composerName = cursor.getString(composerColumn);
                MediaMetadataCompat metadataCompat = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, thisTitle)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, displayName)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumName)
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, albumID)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                        .putString(MediaMetadataCompat.METADATA_KEY_COMPOSER, composerName)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, uri + "/" + albumID)
                        .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, uri + "/" + albumID)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, uri + "/" + albumID)
                        .build();
                /*MusicItemModel musicItemModel = new MusicItemModel();
                musicItemModel.setTitle(thisTitle);
                musicItemModel.setDisplayName(displayName);
                musicItemModel.setAlbumName(albumName);
                musicItemModel.setAlbumId(albumID);*/

                completeMusicList.add(metadataCompat);

                musicListByMediaId.put(albumID, metadataCompat);
                // ...process entry...
            } while (cursor.moveToNext());

            /*errorView.setText("");
            errorView.setVisibility(View.GONE);
            musiclist_recyclerView.setVisibility(View.VISIBLE);*/
        }
    }

    public List<MediaBrowserCompat.MediaItem> getPlayList() {
        if (myPlayList != null && isListPrepared)
            return myPlayList;

        createMusicList();

        if (myPlayList == null)
            myPlayList = new ArrayList<>();

        for (MediaMetadataCompat metadataCompat : completeMusicList) {
            MediaBrowserCompat.MediaItem mediaItem =
                    new MediaBrowserCompat.MediaItem(metadataCompat.getDescription(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
            myPlayList.add(mediaItem);
        }
        isListPrepared = true;
        return myPlayList;
    }

    public int getIndexForMediaId(String mediaId) {
        int indexInCurrentPlaylist = -1;
        for (MediaBrowserCompat.MediaItem mediaItem : myPlayList) {
            indexInCurrentPlaylist++;
            if (mediaItem.getMediaId().equals(mediaId))
                break;
        }
        return indexInCurrentPlaylist;
    }

    public MediaBrowserCompat.MediaItem getMediaItemAtIndex(int index) {
        if (index < 0 || index >= myPlayList.size())
            return null;

        return myPlayList.get(index);
    }

    public MediaMetadataCompat getMetaDataForMediaId(String mediaId) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] rawArt;
        Bitmap art = null;
        BitmapFactory.Options bfo = new BitmapFactory.Options();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mmr.setDataSource(context.getApplicationContext(),
                Uri.parse(uri + "/" + mediaId));
        rawArt = mmr.getEmbeddedPicture();

        // if rawArt is null then no cover art is embedded in the file or is not
        // recognized as such.
        if (null != rawArt)
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);
        else
            art = BitmapFactory.decodeResource(context.getResources(), R.drawable.dummy);

        MediaMetadataCompat oldMetadata = musicListByMediaId.get(mediaId);
        MediaMetadataCompat newMetadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                        oldMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,
                        oldMetadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,
                        oldMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                        oldMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                        oldMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))
                .putString(MediaMetadataCompat.METADATA_KEY_COMPOSER,
                        oldMetadata.getString(MediaMetadataCompat.METADATA_KEY_COMPOSER))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                        oldMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI,
                        oldMetadata.getString(MediaMetadataCompat.METADATA_KEY_ART_URI))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                        oldMetadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI))
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        art)
                .build();

        musicListByMediaId.put(mediaId, newMetadata);

        return musicListByMediaId.get(mediaId);
    }

    private class BitmapLoader extends AsyncTask<Void, Void, Bitmap> {
        String mediaId;

        public BitmapLoader(String mediaId) {
            this.mediaId = mediaId;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            byte[] rawArt;
            Bitmap art = null;
            BitmapFactory.Options bfo = new BitmapFactory.Options();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            mmr.setDataSource(context.getApplicationContext(),
                    Uri.parse(uri + "/" + mediaId));
            rawArt = mmr.getEmbeddedPicture();

            // if rawArt is null then no cover art is embedded in the file or is not
            // recognized as such.
            if (null != rawArt)
                art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);

            return art;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            MediaDescriptionCompat mediaDescriptionCompat = musicListByMediaId.get(mediaId).getDescription();

            MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                    .setMediaId(mediaDescriptionCompat.getMediaId())
                    .setTitle(mediaDescriptionCompat.getTitle())
                    .setSubtitle(mediaDescriptionCompat.getSubtitle())
                    .setDescription(mediaDescriptionCompat.getDescription())
                    .setIconUri(mediaDescriptionCompat.getIconUri())
                    .setIconBitmap(bitmap)
                    .build();
/*
            musicListByMediaId.put(mediaId, new MediaBrowserCompat.MediaItem(mediaDescription,
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));*/
            super.onPostExecute(bitmap);
        }
    }
}
