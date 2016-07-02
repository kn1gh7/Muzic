package com.knight.knight.muzic.playback;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import com.knight.knight.muzic.service.MediaSessionCallbackManager;
import com.knight.knight.muzic.utils.LogHelper;

import java.io.IOException;

/**
 * Created by kn1gh7 on 2/7/16.
 */
public class MediaPlayerPlaybackManager implements PlaybackManager,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener {
    Context mContext;
    MediaPlayer mPlayer;
    LogHelper logHelper;
    MediaSessionCallbackManager mCallback;

    public MediaPlayerPlaybackManager(Context context, MediaSessionCallbackManager callback) {
        mContext = context;
        mCallback = callback;
        logHelper = new LogHelper(new ComponentName(mContext, MediaPlayerPlaybackManager.class));
    }

    private void initializePlayer() { //Sets Player to idle state
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnErrorListener(this);
        }
    }

    public void handleStopPlayback() {
        mCallback.onPlaybackStopped();
        if (mPlayer != null) {
            mPlayer.release();
        }
        mPlayer = null;
    }


    private void playNewMediaId(String mediaId) {
        logHelper.loge("onPlayFromMediaId");
        if (mPlayer != null) {
            handleStopPlayback();
        }

        initializePlayer();
        setPlayer(mediaId);
    }

    public void handleStartPlayback() {
        if (mCallback.getAudioFocus()) {
            mPlayer.start();
            mCallback.onPlaybackStarted();
        }
    }

    public void handlePausePlayback() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            mCallback.onPlaybackPaused();
        }
    }

    private void setPlayer(String mediaId) {
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            mPlayer.setDataSource(mContext,
                    Uri.parse(uri + "/" + mediaId));
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        logHelper.loge("onCompletion");
        handleStopPlayback();

        mCallback.onPlaybackCompleted();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        handleStartPlayback();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        handleStopPlayback();
        return false;
    }

    @Override
    public void playFromMediaId(String mediaId) {
        playNewMediaId(mediaId);
    }

    @Override
    public void play() {
        if (mPlayer == null) {
            handleStopPlayback();
            return;
        }

        handleStartPlayback();
    }

    @Override
    public void stop() {
        logHelper.loge("onStop()");
        handleStopPlayback();
    }

    @Override
    public void pause() {
        logHelper.loge("onPause()");
        handlePausePlayback();
    }
}
