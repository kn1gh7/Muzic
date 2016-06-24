package com.knight.knight.muzic.service;

import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;

import java.util.List;

/**
 * Created by kn1gh7 on 11/6/16.
 */

public class MediaBrowserCallbackManager {
    public Callback mCallback;

    public MediaBrowserCallbackManager(Callback callback) {
        mCallback = callback;
    }

    public MediaBrowserCompat.SubscriptionCallback getmSubscriptionCallback() {
        return mSubscriptionCallback;
    }

    public MediaBrowserCompat.ConnectionCallback getmConnectionCallback() {
        return mConnectionCallback;
    }

    MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new  MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            super.onConnected();
            mCallback.onConnectedWithService();
        }
    };

    MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId,
                                     List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            mCallback.onMusicListFetched(children);
        }

        @Override
        public void onError(@NonNull String parentId) {
            super.onError(parentId);
        }
    };

    public interface Callback {
        void onConnectedWithService();

        void onMusicListFetched(List<MediaBrowserCompat.MediaItem> musicList);
    }
}
