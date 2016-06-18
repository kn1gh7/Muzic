package com.knight.knight.muzic.service;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.text.TextUtilsCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * Created by kn1gh7 on 11/6/16.
 */

public class MediaSessionCallbackManager extends MediaSessionCompat.Callback {
    MediaPlayer mPlayer;
    Context mContext;
    String lastMediaID;

    public MediaSessionCallbackManager(Context context) {
        this.mContext = context;
    }

    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
        Log.e("MediaSessionCallbackmgr", "onMediaButtonEvent");
        return super.onMediaButtonEvent(mediaButtonEvent);
    }

    @Override
    public void onPlayFromUri(Uri uri, Bundle extras) {
        super.onPlayFromUri(uri, extras);
        Log.e("MediaSessionCallbackmgr", "onPlayFromUri");
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        Log.e("MediaSessionCallbackmgr", "onPlayFromMediaId");
        super.onPlayFromMediaId(mediaId, extras);
        if (mPlayer != null && mPlayer.isPlaying()) {
            ((PlaybackStateCallback)mContext).onPause();
            mPlayer.stop();
            if (TextUtils.equals(lastMediaID, mediaId)) {
                mPlayer.release();
                mPlayer = null;
                lastMediaID = null;
                return;
            } else {
                mPlayer.reset();
            }
        }

        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }

        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            mPlayer.setDataSource(mContext,
                    Uri.parse(uri + "/" + mediaId));
            mPlayer.prepare();
            mPlayer.start();
            ((PlaybackStateCallback)mContext).onPlay(mediaId);
            lastMediaID = mediaId;
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    Log.e("MediaSessionCallbackmgr", "onCompletionListener");
                    ((PlaybackStateCallback)mContext).onPause();
                    mPlayer.release();
                    mPlayer = null;
                    lastMediaID = null;
                    return;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        Log.e("MediaSessionCallbackmgr", "onPause()");
        super.onPause();
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            lastMediaID = null;
        }
    }

    @Override
    public void onStop() {
        Log.e("MediaSessionCallbackmgr", "onStop()");
        super.onStop();
    }

    public interface PlaybackStateCallback {
        void onPlay(String mediaId);

        void onPause();
    }
}
