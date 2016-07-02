package com.knight.knight.muzic.playback;

/**
 * Created by kn1gh7 on 2/7/16.
 *
 * This interface needs to be implemented by those class which
 * wants to manage the song playing tasks.
 * Currently the song playing is managed only by MediaPlayer,
 * later even Exoplayer will be involved.
 */
public interface PlaybackManager {
    /**
     * Used when playing from MediaId
     */
    public void playFromMediaId(String mediaId);

    /**
     * Used when player is resumed
     */
    public void play();

    /**
     * Used to stop Playback completely
     */
    public void stop();

    /**
     * Used to pause playback
     */
    public void pause();

    /**
     * Implement this interface to receive callbacks from Player.
     */
    public interface PlaybackCallbackManager {
        public void onPlaybackCompleted();
        public void onPlaybackStarted();
        public void onPlaybackPaused();
        public void onPlaybackStopped();
    }
}
