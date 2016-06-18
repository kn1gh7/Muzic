package com.knight.knight.muzic;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.knight.knight.muzic.adapter.MusicListAdapter;
import com.knight.knight.muzic.callbackInterfaces.MusicItemClicked;
import com.knight.knight.muzic.service.MediaBrowserCallbackManager;
import com.knight.knight.muzic.service.MusicBackgroundService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity implements MusicItemClicked, MediaBrowserCallbackManager.Callback {
    MediaPlayer mediaPlayer;
    List<MediaBrowserCompat.MediaItem> musicList;
    MusicListAdapter musicListAdapter;
    TextView errorView;
    RecyclerView musiclist_recyclerView;
    String lastMusicID;
    MediaBrowserCompat mBrowser;
    MediaBrowserCallbackManager mMediaBrowserCallbackMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        mMediaBrowserCallbackMgr = new MediaBrowserCallbackManager(this);
        musicList = new ArrayList<MediaBrowserCompat.MediaItem>();
        mBrowser = new MediaBrowserCompat(this,
                new ComponentName(getApplicationContext(),
                        MusicBackgroundService.class),
                mMediaBrowserCallbackMgr.getmConnectionCallback(),
                null);
        mBrowser.connect();

        musiclist_recyclerView = (RecyclerView) findViewById(R.id.playerlistview);
        errorView = (TextView) findViewById(R.id.errorview);

        musiclist_recyclerView.setLayoutManager(
                new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL,
                        false));

        musicListAdapter = new MusicListAdapter(MusicListActivity.this,
                MusicListActivity.this,
                musicList);
        musiclist_recyclerView.setAdapter(musicListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicListAdapter != null)
            musicListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMusicItemClicked(String musicID) {
        getSupportMediaController().getTransportControls().playFromMediaId(musicID, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //getSupportMediaController().getTransportControls().pause();
    }

    @Override
    public void onConnectedWithService() {
        MediaSessionCompat.Token token = mBrowser.getSessionToken();
        try {
            setSupportMediaController(new MediaControllerCompat(MusicListActivity.this, token));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        lastMusicID = mBrowser.getRoot();
        mBrowser.unsubscribe(lastMusicID);
        mBrowser.subscribe(lastMusicID,
                mMediaBrowserCallbackMgr.getmSubscriptionCallback());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBrowser.disconnect();
    }

    @Override
    public void onMusicListFetched(List<MediaBrowserCompat.MediaItem> musicList) {
        musicListAdapter = new MusicListAdapter(MusicListActivity.this,
                MusicListActivity.this,
                musicList);
        musiclist_recyclerView.setAdapter(musicListAdapter);
    }
}