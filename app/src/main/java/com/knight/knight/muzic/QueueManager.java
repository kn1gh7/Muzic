package com.knight.knight.muzic;

import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import com.knight.knight.muzic.service.MusicListProvider;
import java.util.List;

/**
 * Created by kn1gh7 on 25/6/16.
 */
public class QueueManager {
    private String currentPlayingMediaId;
    private int currentPlayingIndex;
    private MusicListProvider mListProvider;

    public QueueManager(Context context) {
        mListProvider = new MusicListProvider(context);
    }

    public List<MediaBrowserCompat.MediaItem> getPlayList() {
        return mListProvider.getPlayList();
    }

    public MediaMetadataCompat getCurrentMetadata() {
        return mListProvider.getMetaDataForMediaId(currentPlayingMediaId);
    }

    public void setCurrentPlayingMediaId(String currentPlayingMediaId) {
        this.currentPlayingMediaId = currentPlayingMediaId;
        this.currentPlayingIndex = mListProvider.getIndexForMediaId(currentPlayingMediaId);
    }

    public void setCurrentPlayingIndex(int index) {
        if (mListProvider.getMediaItemAtIndex(index) == null)
            throw new IndexOutOfBoundsException("Setting current Playing index for Invalid Position in Playlist");

        this.currentPlayingIndex = index;
        this.currentPlayingMediaId = mListProvider.getMediaItemAtIndex(currentPlayingIndex).getMediaId();
    }

    public MediaBrowserCompat.MediaItem getNextMediaItem() {
        if (mListProvider.getMediaItemAtIndex(currentPlayingIndex + 1) == null)
            return null;

        return mListProvider.getMediaItemAtIndex(currentPlayingIndex + 1);
    }

    public MediaBrowserCompat.MediaItem getPreviousMediaItem() {
        if (currentPlayingIndex - 1 < 0) {
            return null;
        }

        return mListProvider.getMediaItemAtIndex(currentPlayingIndex - 1);
    }
}
