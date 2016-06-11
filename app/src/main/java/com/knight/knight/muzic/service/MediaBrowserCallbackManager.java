package com.knight.knight.muzic.service;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;

import java.util.List;

/**
 * Created by kn1gh7 on 11/6/16.
 */

public class MediaBrowserCallbackManager {
    public MediaBrowserCompat.ConnectionCallback mConnectionCallback;
    public Callback mCallback;
    public MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback;

    public MediaBrowserCallbackManager(Callback callback) {
        mCallback = callback;
        mConnectionCallback = new ConnectionManger();
        mSubscriptionCallback = new SubscriptionManager();
    }

    public MediaBrowserCompat.SubscriptionCallback getmSubscriptionCallback() {
        return mSubscriptionCallback;
    }

    public MediaBrowserCompat.ConnectionCallback getmConnectionCallback() {
        return mConnectionCallback;
    }

    public class ConnectionManger extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            super.onConnected();
            mCallback.onConnectedWithService();
        }
    }

    public class SubscriptionManager extends MediaBrowserCompat.SubscriptionCallback {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            mCallback.onMusicListFetched(children);
        }

        @Override
        public void onError(@NonNull String parentId) {
            super.onError(parentId);
        }
    }

    public interface Callback {
        void onConnectedWithService();

        void onMusicListFetched(List<MediaBrowserCompat.MediaItem> musicList);
    }
}
