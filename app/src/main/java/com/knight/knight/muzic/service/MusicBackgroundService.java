package com.knight.knight.muzic.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by kn1gh7 on 11/6/16.
 */

public class MusicBackgroundService extends MediaBrowserServiceCompat {
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
        MediaSessionCompat mSession = new MediaSessionCompat(getApplicationContext(),
                MusicBackgroundService.class.getSimpleName());
        mSession.setCallback(new MediaSessionCallbackManager(getApplicationContext()));
        setSessionToken(mSession.getSessionToken());

        Log.e("MusicBackgroundService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }
}
