package com.knight.knight.muzic;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.knight.knight.muzic.adapter.MusicListAdapter;
import com.knight.knight.muzic.callbackInterfaces.MusicItemClicked;
import com.knight.knight.muzic.model.MusicItemModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity implements MusicItemClicked {
    MediaPlayer mediaPlayer;
    List<MusicItemModel> musicList;
    MusicListAdapter musicListAdapter;
    TextView errorView;
    RecyclerView musiclist_recyclerView;
    String lastMusicID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        musicList = new ArrayList<MusicItemModel>();

        musiclist_recyclerView = (RecyclerView) findViewById(R.id.playerlistview);
        errorView = (TextView) findViewById(R.id.errorview);

        musiclist_recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        musicListAdapter = new MusicListAdapter(MusicListActivity.this, MusicListActivity.this, musicList);
        musiclist_recyclerView.setAdapter(musicListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMusicList(musicList);
        musicListAdapter.notifyDataSetChanged();

    }

    private void setMusicList(List<MusicItemModel> musicList) {
        ContentResolver contentResolver = getContentResolver();

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.i("URI", uri.toString());
        Cursor cursor = contentResolver.query(uri, null, selection, null, null);
        if (cursor == null) {
            // query failed, handle error.
            errorView.setText("Cursor returned Null");
            errorView.setVisibility(View.VISIBLE);
            musiclist_recyclerView.setVisibility(View.GONE);
        } else if (!cursor.moveToFirst()) {
            // no media on the device
            errorView.setText("No Items Found");
            errorView.setVisibility(View.VISIBLE);
            musiclist_recyclerView.setVisibility(View.GONE);
        } else {
            musicList.clear();
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

                musicList.add(musicItemModel);
                // ...process entry...
            } while (cursor.moveToNext());

            errorView.setText("");
            errorView.setVisibility(View.GONE);
            musiclist_recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMusicItemClicked(String musicID) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            if (musicID.equals(lastMusicID)) {
                mediaPlayer.release();
                mediaPlayer = null;
                return;
            } else {
                lastMusicID = musicID;
            }
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(uri + "/" + musicID));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            lastMusicID = null;
        }
    }
}
