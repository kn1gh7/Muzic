package com.knight.knight.muzic.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by kn1gh7 on 11/6/16.
 */

public class MusicBackgroundService extends MediaBrowserServiceCompat {
    MediaSessionCompat mSession;

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("__ROOT__", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(new MusicListProvider(getApplicationContext())
                        .getMusicList());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSession = new MediaSessionCompat(getApplicationContext(),
                MusicBackgroundService.class.getSimpleName());

        mSession.setCallback(new MediaSessionCallbackManager(this));

        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)
                .setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1)
                .build();

        mSession.setPlaybackState(state);

        mSession.setActive(true);

        setSessionToken(mSession.getSessionToken());

        Log.e("MusicBackgroundService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mSession, intent);
        return START_STICKY;
    }
}
