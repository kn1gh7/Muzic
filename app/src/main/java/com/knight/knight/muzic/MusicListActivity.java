package com.knight.knight.muzic;

import android.Manifest;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.widget.TextView;

import com.knight.knight.muzic.adapter.MusicListAdapter;
import com.knight.knight.muzic.callbackInterfaces.MusicItemClicked;
import com.knight.knight.muzic.service.MediaBrowserCallbackManager;
import com.knight.knight.muzic.service.MusicBackgroundService;
import com.knight.knight.muzic.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends AppCompatActivity implements
        MusicItemClicked,
        MediaBrowserCallbackManager.Callback {
    MusicListAdapter musicListAdapter;
    TextView errorView;
    RecyclerView musiclist_recyclerView;
    String lastMusicID;
    MediaBrowserCompat mBrowser;
    MediaBrowserCallbackManager mMediaBrowserCallbackMgr;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 69;
    private LogHelper logHelper;
    private int mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        initializeView();
        initializeOthers();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            initializeMediaBrowser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeMediaBrowser();
                }
                return;
            }
        }
    }

    private void initializeView() {
        musiclist_recyclerView = (RecyclerView) findViewById(R.id.playerlistview);
        errorView = (TextView) findViewById(R.id.errorview);
    }

    private void initializeOthers() {
        logHelper = new LogHelper(new ComponentName(this, MusicListActivity.class));
        musiclist_recyclerView.setLayoutManager(
                new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL,
                        false));

    }

    private void initializeMediaBrowser() {
        mMediaBrowserCallbackMgr = new MediaBrowserCallbackManager(this);
        mBrowser = new MediaBrowserCompat(this,
                new ComponentName(getApplicationContext(),
                        MusicBackgroundService.class),
                mMediaBrowserCallbackMgr.getmConnectionCallback(),
                null);
        mBrowser.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicListAdapter != null)
            musicListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMusicItemClicked(String musicID) {
        logHelper.Loge("playing MusicItem: " + musicID);
        if (musicID.equals(lastMusicID)) {
            if (mState == PlaybackStateCompat.STATE_PAUSED || mState == PlaybackStateCompat.STATE_NONE) {
                getSupportMediaController().getTransportControls().play();
            } else if (mState == PlaybackStateCompat.STATE_PLAYING){
                getSupportMediaController().getTransportControls().pause();
            }
        } else {
            getSupportMediaController().getTransportControls().playFromMediaId(musicID, null);
            lastMusicID = musicID;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastMusicID = null;
    }

    @Override
    public void onConnectedWithService() {
        MediaSessionCompat.Token token = mBrowser.getSessionToken();
        try {
            MediaControllerCompat mediaController = new MediaControllerCompat(MusicListActivity.this, token);
            setSupportMediaController(mediaController);
            mediaController.registerCallback(playlistDataChangeCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String root = mBrowser.getRoot();
        mBrowser.unsubscribe(root);
        mBrowser.subscribe(root,
                mMediaBrowserCallbackMgr.getmSubscriptionCallback());
    }

    private MediaControllerCompat.Callback playlistDataChangeCallback =
            new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            mState = state.getState();
            super.onPlaybackStateChanged(state);
            musicListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
        }
    };

    @Override
    protected void onDestroy() {
        if (mBrowser != null && mBrowser.isConnected()) {
            getSupportMediaController().unregisterCallback(playlistDataChangeCallback);
            mBrowser.disconnect();
        }

        if (mState != PlaybackStateCompat.STATE_PLAYING) {
            getSupportMediaController().getTransportControls().stop();
        }
        super.onDestroy();

    }

    @Override
    public void onMusicListFetched(List<MediaBrowserCompat.MediaItem> musicList) {
        musicListAdapter = new MusicListAdapter(MusicListActivity.this,
                MusicListActivity.this,
                musicList);
        musiclist_recyclerView.setAdapter(musicListAdapter);
    }
}