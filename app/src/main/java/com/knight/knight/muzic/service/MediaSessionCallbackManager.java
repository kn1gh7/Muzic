package com.knight.knight.muzic.service;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.knight.knight.muzic.QueueManager;

import java.io.IOException;

/**
 * Created by kn1gh7 on 11/6/16.
 */

public class MediaSessionCallbackManager extends MediaSessionCompat.Callback
        implements OnCompletionListener, OnPreparedListener, OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    MediaPlayer mPlayer;
    Context mContext;
    String currentMediaId;
    AudioManager audioManager;
    int mCurrentAudioFocus;
    QueueManager qManager;

    public MediaSessionCallbackManager(Context context, QueueManager qManager) {
        this.mContext = context;
        this.qManager = qManager;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mCurrentAudioFocus = AudioManager.AUDIOFOCUS_LOSS;
    }

    private void initializePlayer() { //Sets Player to idle state
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnErrorListener(this);
        }
    }

    private void handleStopPlayback() {
        ((PlaybackStateCallback)mContext).onStop();
        if (mPlayer != null) {
            mPlayer.release();
            releaseAudioFocus();
        }
        mPlayer = null;
        currentMediaId = null;
    }

    private boolean getAudioFocus() {
        if (audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocus = AudioManager.AUDIOFOCUS_GAIN;
            return true;
        }

        return false;
    }

    private void releaseAudioFocus() {
        if (mCurrentAudioFocus == AudioManager.AUDIOFOCUS_GAIN) {
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mCurrentAudioFocus = AudioManager.AUDIOFOCUS_LOSS;
            }
        }
    }

    private void playNewMediaId(String mediaId) {
        Log.e("MediaSessionCallbackmgr", "onPlayFromMediaId");
        if (mPlayer != null) {
            handleStopPlayback();
        }

        initializePlayer();
        setPlayer(mediaId);
    }

    private void handleStartPlayback() {
        if (getAudioFocus()) {
            mPlayer.start();
            ((PlaybackStateCallback) mContext).onPlay(currentMediaId);
        }
    }

    private void handlePausePlayback() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            ((PlaybackStateCallback)mContext).onPause();
            releaseAudioFocus();
        }
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        playNewMediaId(mediaId);
        super.onPlayFromMediaId(mediaId, extras);
    }

    @Override
    public void onPlay() {
        if (mPlayer == null || currentMediaId == null) {
            handleStopPlayback();
            return;
        }

        handleStartPlayback();
        super.onPlay();
    }

    private void setPlayer(String mediaId) {
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            mPlayer.setDataSource(mContext,
                    Uri.parse(uri + "/" + mediaId));
            mPlayer.prepareAsync();
            currentMediaId = mediaId;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        Log.e("MediaSessionCallbackmgr", "onPause()");
        super.onPause();
        handlePausePlayback();
    }

    @Override
    public void onStop() {
        Log.e("MediaSessionCallbackmgr", "onStop()");
        super.onStop();
        handleStopPlayback();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e("MediaSessionCallbackmgr", "onCompletionListener");
        handleStopPlayback();
        if (qManager.getNextMediaItem() != null) {
            playNewMediaId(qManager.getNextMediaItem().getMediaId());
        }
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
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            handleStopPlayback();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            handlePausePlayback();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            handleStartPlayback();
        }
    }

    public interface PlaybackStateCallback {
        void onPlay(String mediaId);
        void onPause();
        void onStop();
    }
}
