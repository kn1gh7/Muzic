package com.knight.knight.muzic.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import com.knight.knight.muzic.MusicListActivity;
import com.knight.knight.muzic.R;
import com.knight.knight.muzic.service.MusicBackgroundService;
import com.knight.knight.muzic.utils.LogHelper;

/**
 * Created by kn1gh7 on 18/6/16.
 */
public class NotificationManager extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 12;
    private MusicBackgroundService mService;
    private MediaControllerCompat mController;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat.TransportControls mTransportControls;
    private NotificationManagerCompat mNotificationManager;
    private MediaMetadataCompat mMetadata;
    private PlaybackStateCompat mPlaybackState;
    private LogHelper logHelper;
    private boolean notificationStarted;

    public NotificationManager(MusicBackgroundService mService) {
        logHelper = new LogHelper(new ComponentName(mService, NotificationManager.class));
        this.mService = mService;
        mSessionToken = mService.getSessionToken();
        try {
            mController = new MediaControllerCompat(mService, mSessionToken);
            mTransportControls = mController.getTransportControls();
            mNotificationManager = NotificationManagerCompat.from(mService);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Notification createNotification() {
        logHelper.loge("updateNotificationMetadata. mMetadata=" + mMetadata);
        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService);
        int playPauseButtonPosition = 0;

        addPlayPauseAction(notificationBuilder);

        MediaDescriptionCompat description = mMetadata.getDescription();

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] rawArt;
        Bitmap art = null;
        BitmapFactory.Options bfo = new BitmapFactory.Options();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mmr.setDataSource(mService,
                Uri.parse(uri + "/" + description.getMediaId()));
        rawArt = mmr.getEmbeddedPicture();

        // if rawArt is null then no cover art is embedded in the file or is not
        // recognized as such.
        if (null != rawArt)
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.length, bfo);

        MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                .setMediaId(description.getMediaId())
                .setTitle(description.getTitle())
                .setSubtitle(description.getSubtitle())
                .setDescription(description.getDescription())
                .setIconUri(description.getIconUri())
                .setIconBitmap(art)
                .build();

        String fetchArtUrl = null;
        if (mediaDescription.getIconBitmap() != null)
            art = mediaDescription.getIconBitmap();
        if (mediaDescription.getIconUri() != null) {
            // This sample assumes the iconUri will be a valid URL formatted String, but
            // it can actually be any valid Android Uri formatted String.
            // async fetch the album art icon
            String artUrl = mediaDescription.getIconUri().toString();
            /*art = AlbumArtCache.getInstance().getBigImage(artUrl);*/
            if (art == null) {
                fetchArtUrl = artUrl;
                // use a placeholder art while the remote art is being downloaded
                art = BitmapFactory.decodeResource(mService.getResources(),
                        R.drawable.dummy);
            }
        }
        int mNotificationColor = mService.getResources().getColor(R.color.colorPrimary);
        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(
                                new int[]{playPauseButtonPosition})  // show only play/pause in compact view
                        .setMediaSession(mSessionToken))
                .setColor(mNotificationColor)
                .setSmallIcon(R.drawable.dummy)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setUsesChronometer(true)
                .setContentIntent(createContentIntent(mediaDescription))
                .setContentTitle(mediaDescription.getTitle())
                .setContentText(mediaDescription.getSubtitle())
                .setLargeIcon(art);

        return notificationBuilder.build();
    }

    public void startNotification() {
        if (!notificationStarted) {
            mMetadata = mController.getMetadata();
            mPlaybackState = mController.getPlaybackState();

            Notification notification = createNotification();
            mController.registerCallback(mCb);
            IntentFilter filter = new IntentFilter();
            filter.addAction("120");
            mService.registerReceiver(this, filter);

            mService.startForeground(NOTIFICATION_ID, notification);
            notificationStarted = true;
        }
    }

    public void stopNotification() {
        notificationStarted = false;
        logHelper.loge("stopNotification");
        mController.unregisterCallback(mCb);
        try {
            mNotificationManager.cancel(12);
            mService.unregisterReceiver(this);
        } catch (IllegalArgumentException ex) {
            // ignore if the receiver is not registered.
        }
        mService.stopForeground(true);
    }

    private MediaControllerCompat.Callback mCb = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            logHelper.loge("onPlaybackStateChanged");
            mPlaybackState = state;
            int mState = mPlaybackState.getState();

            if (mState == PlaybackStateCompat.STATE_NONE
                    || mState == PlaybackStateCompat.STATE_PAUSED
                    || mState == PlaybackStateCompat.STATE_PLAYING) {
                Notification notification = createNotification();
                if (notification != null)
                    mNotificationManager.notify(NOTIFICATION_ID, notification);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
                            mController.getTransportControls().stop();
                        }
                    }
                }, 10000);
            } else if (mState == PlaybackStateCompat.STATE_STOPPED) {
                stopNotification();
            }
            super.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            logHelper.loge("onMetadatachanged");
            mMetadata = metadata;
            Notification notification = createNotification();
            if (notification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
            super.onMetadataChanged(metadata);
        }

        @Override
        public void onSessionDestroyed() {
            logHelper.loge("onSessionDestroyed");
            super.onSessionDestroyed();
            MediaSessionCompat.Token newToken = mService.getSessionToken();
            if (!newToken.equals(mSessionToken)) {
                mSessionToken = newToken;
            }
            if (mController != null) {
                mController.unregisterCallback(mCb);
            }
            if (mSessionToken != null) {
                try {
                    mController = new MediaControllerCompat(mService, mSessionToken);
                    mTransportControls = mController.getTransportControls();
                    mController.registerCallback(mCb);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void addPlayPauseAction(NotificationCompat.Builder notificationBuilder) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mService, 99,
                new Intent("120"), PendingIntent.FLAG_CANCEL_CURRENT);
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING)
            notificationBuilder.addAction(R.drawable.pause_filled, "Pause",
                    pendingIntent);
        else
            notificationBuilder.addAction(R.drawable.play_circle_outline, "Play",
                    pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        logHelper.loge("onReceive Called " + mPlaybackState.getState());
        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            mTransportControls.pause();
            startNotification();
        } else if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
            mTransportControls.play();
            startNotification();
        } else {
            stopNotification();
        }
    }

    private PendingIntent createContentIntent(MediaDescriptionCompat description) {
        Intent openUI = new Intent(mService, MusicListActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mService, 99, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
/*
    private class BitmapLoader extends AsyncTask<Void, Void, Bitmap> {
        String mediaId;

        public BitmapLoader(String mediaId) {
            this.mediaId = mediaId;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {


            return art;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            MediaDescriptionCompat mediaDescriptionCompat = musicListByMediaId.get(mediaId).getDescription();

            MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                    .setMediaId(mediaDescriptionCompat.getMediaId())
                    .setTitle(mediaDescriptionCompat.getTitle())
                    .setSubtitle(mediaDescriptionCompat.getSubtitle())
                    .setDescription(mediaDescriptionCompat.getDescription())
                    .setIconUri(mediaDescriptionCompat.getIconUri())
                    .setIconBitmap(bitmap)
                    .build();

            musicListByMediaId.put(mediaId, new MediaBrowserCompat.MediaItem(mediaDescription,
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
            super.onPostExecute(bitmap);
        }
    }*/
}
