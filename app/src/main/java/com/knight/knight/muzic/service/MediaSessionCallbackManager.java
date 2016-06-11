package com.knight.knight.muzic.service;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;

import java.io.IOException;

/**
 * Created by kn1gh7 on 11/6/16.
 */

public class MediaSessionCallbackManager extends MediaSessionCompat.Callback {
    MediaPlayer mPlayer;
    Context mContext;

    public MediaSessionCallbackManager(Context context) {
        this.mContext = context;
    }

    @Override
    public void onPlayFromUri(Uri uri, Bundle extras) {
        super.onPlayFromUri(uri, extras);
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        super.onPlayFromMediaId(mediaId, extras);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            /*lastMusicID = null;*/
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
