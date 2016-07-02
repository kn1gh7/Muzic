package com.knight.knight.muzic.service;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import com.knight.knight.muzic.QueueManager;
import com.knight.knight.muzic.playback.MediaPlayerPlaybackManager;
import com.knight.knight.muzic.playback.PlaybackManager;

/**
 * Created by kn1gh7 on 11/6/16.
 * Class currently manages Queue for songs to be played,
 * Next song, previous songs, etc
 *
 * It also manages the AudioFocus, which will be seperated later.
 * The idea is to incorporate Exoplayer into the app and the kind
 * of player that will be handling the Song Playing task will be
 * decided here.
 */

public class MediaSessionCallbackManager extends MediaSessionCompat.Callback
        implements AudioManager.OnAudioFocusChangeListener, PlaybackManager.PlaybackCallbackManager {
    Context mContext;
    String requestedMediaId; //MediaId Requested
    String currentMediaId;   //Currently playing MediaId
    AudioManager audioManager;
    int mCurrentAudioFocus;
    QueueManager qManager;
    MediaPlayerPlaybackManager mediaPlaybackManager;

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        requestedMediaId = mediaId;
        mediaPlaybackManager.playFromMediaId(mediaId);
        super.onPlayFromMediaId(mediaId, extras);
    }

    @Override
    public void onPlay() {
        if (currentMediaId != null) {
            mediaPlaybackManager.play();
        } else {
            mediaPlaybackManager.stop();
            currentMediaId = null;
            requestedMediaId = null;
        }
        super.onPlay();
    }

    @Override
    public void onPause() {
        mediaPlaybackManager.pause();
        super.onPause();
    }

    public MediaSessionCallbackManager(Context context, QueueManager qManager) {
        this.mContext = context;
        this.qManager = qManager;
        mediaPlaybackManager = new MediaPlayerPlaybackManager(mContext, this);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mCurrentAudioFocus = AudioManager.AUDIOFOCUS_LOSS;
    }

    public boolean getAudioFocus() {
        if (audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocus = AudioManager.AUDIOFOCUS_GAIN;
            return true;
        }

        return false;
    }

    public void releaseAudioFocus() {
        if (mCurrentAudioFocus == AudioManager.AUDIOFOCUS_GAIN) {
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mCurrentAudioFocus = AudioManager.AUDIOFOCUS_LOSS;
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            mediaPlaybackManager.stop();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            mediaPlaybackManager.pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            mediaPlaybackManager.handleStartPlayback();
        }
    }

    @Override
    public void onPlaybackCompleted() {
        if (qManager.getNextMediaItem() != null) {
            requestedMediaId = qManager.getNextMediaItem().getMediaId();
            mediaPlaybackManager.playFromMediaId(requestedMediaId);
        }
    }

    @Override
    public void onPlaybackStarted() {
        currentMediaId = requestedMediaId;
        ((PlaybackStateCallback) mContext).onPlay(currentMediaId);
    }

    @Override
    public void onPlaybackPaused() {
        ((PlaybackStateCallback)mContext).onPause();
        releaseAudioFocus();
    }

    @Override
    public void onPlaybackStopped() {
        ((PlaybackStateCallback)mContext).onStop();
        releaseAudioFocus();
    }

    public interface PlaybackStateCallback {
        void onPlay(String mediaId);
        void onPause();
        void onStop();
    }
}