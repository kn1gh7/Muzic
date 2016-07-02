package com.knight.knight.muzic.service;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.knight.knight.muzic.QueueManager;
import com.knight.knight.muzic.notification.NotificationManager;
import com.knight.knight.muzic.utils.LogHelper;

import java.util.List;

/**
 * Created by kn1gh7 on 11/6/16.
 */

public class MusicBackgroundService extends MediaBrowserServiceCompat
        implements MediaSessionCallbackManager.PlaybackStateCallback {
    MediaSessionCompat mSession;
    PlaybackStateCompat.Builder playbackBuilder;
    NotificationManager notificationManager;
    private QueueManager qManager;
    private LogHelper logHelper;

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("__ROOT__", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(qManager.getPlayList());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        logHelper = new LogHelper(new ComponentName(this, MusicBackgroundService.class));
        qManager = new QueueManager(getApplicationContext());
        mSession = new MediaSessionCompat(getApplicationContext(),
                MusicBackgroundService.class.getSimpleName());

        mSession.setCallback(new MediaSessionCallbackManager(this, qManager));

        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        playbackBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)
                .setState(PlaybackStateCompat.STATE_NONE,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1);

        PlaybackStateCompat state = playbackBuilder.build();

        mSession.setPlaybackState(state);

        mSession.setActive(false);

        setSessionToken(mSession.getSessionToken());
        notificationManager = new NotificationManager(this);
        logHelper.loge("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        MediaButtonReceiver.handleIntent(mSession, intent);
        return START_STICKY;
    }

    @Override
    public void onPlay(String mediaId) {
        qManager.setCurrentPlayingMediaId(mediaId);
        playbackBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1);
        mSession.setActive(true);
        mSession.setMetadata(qManager.getCurrentMetadata());
        mSession.setPlaybackState(playbackBuilder.build());
        notificationManager.startNotification();

        startService(new Intent(getApplicationContext(), MusicBackgroundService.class));
    }

    public MediaDescriptionCompat getCurrentMediaDescription() {
        return qManager.getCurrentMetadata().getDescription();
    }

    @Override
    public void onPause() {
        playbackBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1);
        mSession.setPlaybackState(playbackBuilder.build());
    }

    @Override
    public void onStop() {
        logHelper.loge("onStop");
        playbackBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1);
        mSession.setPlaybackState(playbackBuilder.build());
        notificationManager.stopNotification();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        logHelper.loge("onDestroy");
        super.onDestroy();
    }
}
